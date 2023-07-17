package net.arna.jcraft.common.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

/**
 * Class that needs to be instantiated per-player to contain temporary data relating to their current state.
 * Used to handle stand-off attacks.
 */
public abstract class JCraftSpec {
    public PlayerEntity player;

    public int moveStun = 0;
    public int attackID = -1; // Client-only
    public Attack curAttack;
    public Attack previousAttack;
    public AttackQueue queuedAttack;
    public int armorPoints = 0;

    public String getName() { return "UNNAMED"; }
    public String getDescription() { return "UNDESCRIBED"; }
    public String getDetails() { return "UNFINISHED"; }
    public List<Attack> getAttacks() { return null; }
    public int getId() { return 0; }

    public void InitHeavyAttack(ServerWorld serverWorld) {
    }

    public void InitBarrage(ServerWorld serverWorld) {
    }

    public void InitSpecial1(ServerWorld serverWorld) {
    }

    public void InitSpecial2(ServerWorld serverWorld) {
    }

    public void InitSpecial3(ServerWorld serverWorld) {
    }

    public void InitUlt(ServerWorld serverWorld) {
    }

    public boolean canAttack() {
        ITimeStop timeStop = (ITimeStop) player;
        return this.moveStun < 1 && timeStop.getTimeStopTicks() < 1 && !player.hasStatusEffect(JStatusRegister.DAZED);
    }

    public boolean handleAttack(ServerWorld serverWorld, Attack attack, String cooldownName) {
        NbtCompound playerData = ((IEntityDataSaver) player).getPersistentData();
        int cd = playerData.getInt(cooldownName);
        if (cd > 0) return false;
        moveStun = attack.moveStun;
        playerData.putInt(cooldownName, attack.cooldown * 20);
        curAttack = attack;
        armorPoints = attack.armor;

        PlayerLookup.world(serverWorld).forEach(
                serverPlayer -> PlayerAnimPacket.sendSpec(player, serverPlayer, curAttack.animation, moveStun, curAttack.id));
        return true;
    }

    public void cancelAttack() {
        curAttack = null;
        queuedAttack = null;
        moveStun = 0;

        if (player == null) return;
        // Cancel player animation if he exists
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(13);
        buf.writeInt(player.getId());
        ServerWorld serverWorld = (ServerWorld) player.getWorld();
        for (ServerPlayerEntity sendPlayer : serverWorld.getPlayers())
            ServerChannelFeedbackPacket.send(sendPlayer, buf);
    }

    public void specialAttack(Attack attack, List<LivingEntity> hurt) {

    }

    public boolean shouldSneak() {
        return false;
    }

    public void processAttackClient() {
    }

    public void tickSpec() {
        if (player.isSpectator()) return;

        World world = player.getWorld();

        if (world.isClient()) {
            //JCraft.LOGGER.info("CLIENT: Ticking spec " + this);

            if (moveStun > 0) {
                //JCraft.LOGGER.info("CLIENT: Movestun is " + moveStun);

                player.setSneaking(shouldSneak());

                // Process attack
                moveStun--;
                processAttackClient();
            }
        } else {
            //JCraft.LOGGER.info("SERVER: Ticking spec " + this);

            ServerWorld serverWorld = (ServerWorld) world;
            Attack attack = this.curAttack;

            if (moveStun > 0) {
                //JCraft.LOGGER.info("SERVER: Movestun is " + moveStun);

                // Likely will be changed later, but at the moment this serves to prevent animations breaking
                player.setSneaking(shouldSneak());

                // Process attack
                moveStun--;
                if (attack != null) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5, 9, true, false));

                    int realInitTime = attack.moveStun - attack.initTime;
                    int stunS = (int) (attack.stun * 20f);

                    if ((attack.attackType == AttackType.BOX && this.moveStun == realInitTime)
                            || (attack.attackType == AttackType.MULTIHIT && attack.attackTimes.contains(attack.moveStun - this.moveStun))) {
                        double yawRad = Math.toRadians(player.getYaw() + 90);
                        Vec3d rotVec = new Vec3d(Math.cos(yawRad), 0, Math.sin(yawRad)); // Previously player.getRotationVector() but that allowed them to aim vertically
                        Vec3d hitPos = player.getPos().add(0, player.getHeight() / 2 - attack.offset, 0).add(rotVec.multiply(attack.attackDist));
                        ArrayList<Entity> exclude = new ArrayList<>(player.getPassengerList());
                        exclude.add(player);
                        if (player.hasVehicle())
                            exclude.add(player.getVehicle());

                        List<LivingEntity> hurt = JUtils.generateHitbox(world,
                                hitPos,
                                attack.hitboxSize,
                                List.copyOf(exclude)
                        );
                        for (Attack.HitboxData data : attack.extraHitboxes) {
                            List<LivingEntity> extraHurt = JUtils.generateHitbox(world,
                                    hitPos.add(rotVec.multiply(data.forwardOffset)).add(0, data.verticalOffset, 0), data.hitboxSize, exclude);
                            for (LivingEntity hurtEntity : extraHurt)
                                if (!hurt.contains(hurtEntity)) hurt.add(hurtEntity);
                        }
                        if (!hurt.isEmpty()) {
                            Random random = new Random();
                            JCraft.createParticle((ServerWorld) world,
                                    hitPos.x + random.nextDouble(-0.5, 0.5),
                                    hitPos.y + random.nextDouble(-0.5, 0.5),
                                    hitPos.z + random.nextDouble(-0.5, 0.5),
                                    attack.hitspark + 1);

                            if (attack.impactSound != null)
                                JUtils.serverPlaySound(attack.impactSound, serverWorld, hitPos);

                            float kb = attack.knockback;
                            Vec3d kbVec = rotVec.multiply(kb).add(new Vec3d(0.0, Math.abs(attack.knockback) / 4, 0.0));

                            DamageSource playerSource = DamageSource.player(player);
                            for (LivingEntity livingEntity : hurt) {
                                if (livingEntity instanceof StandEntity) continue;
                                damageLogic(
                                        world,
                                        livingEntity,
                                        kbVec,
                                        stunS,
                                        attack.stunType,
                                        attack.overrideStun,
                                        attack.damage,
                                        attack.lift,
                                        attack.getEffectiveBlockstun(),
                                        playerSource,
                                        player,
                                        attack.canBackstab,
                                        attack.unblockable && !attack.ubEffectsOnly
                                );
                            }

                            this.specialAttack(attack, hurt);
                        }
                    }
                }
            } else if (this.queuedAttack != null) {
                switch (this.queuedAttack) {
                    case HEAVY -> this.InitHeavyAttack(serverWorld);
                    case BARRAGE -> this.InitBarrage(serverWorld);
                    case ULTIMATE -> this.InitUlt(serverWorld);
                    case SPECIAL1 -> this.InitSpecial1(serverWorld);
                    case SPECIAL2 -> this.InitSpecial2(serverWorld);
                    case SPECIAL3 -> this.InitSpecial3(serverWorld);
                }
                this.queuedAttack = null;
            }

            if (this.curAttack != this.previousAttack && this.curAttack != null) {
                this.previousAttack = this.curAttack;
            }
        }
    }

}
