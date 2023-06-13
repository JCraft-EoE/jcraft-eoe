package net.arna.jcraft.common.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

/*
Specs -
classes that need to be instantiated per-player so as to contain temporary data relating to their current state
they will handle stand-off attacks
 */

public abstract class JCraftSpec {
    public PlayerEntity player;

    public int moveStun;
    public Attack curAttack;
    public Attack previousAttack;
    public AttackQueue queuedAttack;

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

    public boolean CanAttack() {
        ITimeStop timeStop = (ITimeStop) player;
        return this.moveStun < 1 && timeStop.getTimeStopTicks() < 1 && !player.hasStatusEffect(JStatusRegister.DAZED);
    }

    public boolean HandleAttack(ServerWorld serverWorld, Attack attack, String cooldownName) {
        NbtCompound playerData = ((IEntityDataSaver) player).getPersistentData();
        int cd = playerData.getInt(cooldownName);
        if (cd > 0) {
            return false;
        }
        moveStun = attack.moveStun;
        playerData.putInt(cooldownName, attack.cooldown * 20);
        curAttack = attack;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(12);
        buf.writeInt(player.getId());
        buf.writeString(attack.animation);
        for (ServerPlayerEntity sendPlayer : serverWorld.getPlayers()) {
            ServerChannelFeedbackPacket.send(sendPlayer, buf);
        }
        return true;
    }

    public void CancelAttack() {
        curAttack = null;
        queuedAttack = null;
        moveStun = 0;

        if (player == null) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(13);
        buf.writeInt(player.getId());
        ServerWorld serverWorld = (ServerWorld) player.getWorld();
        for (ServerPlayerEntity sendPlayer : serverWorld.getPlayers()) {
            ServerChannelFeedbackPacket.send(sendPlayer, buf);
        }
    }

    public void SpecialAttack(Attack attack, List<LivingEntity> hurt) {

    }

    public void tickSpec() {
        //JCraft.LOGGER.info("ticking spec");
        World world = player.getWorld();

        if (world.isClient()) {

        } else {
            ServerWorld serverWorld = (ServerWorld) world;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            Attack attack = this.curAttack;

            if (moveStun > 0) {
                moveStun -= 1;

                Entity passenger = player.getFirstPassenger();
                //StandEntity stand = null;
                //if (passenger instanceof StandEntity s) { stand = s; }

                if (attack != null) {
                    int realInitTime = attack.moveStun - attack.initTime;
                    int stunS = (int) (attack.stun * 20f);

                    if ((attack.attackType == AttackType.BOX && this.moveStun == realInitTime)
                            || (attack.attackType == AttackType.MULTIHIT && attack.attackTimes.contains(attack.moveStun - this.moveStun))) {
                        Vec3d rotVec = player.getRotationVector();
                        Vec3d hitPos = player.getPos().add(0, player.getHeight() / 2 - attack.offset, 0).add(rotVec.multiply(attack.attackDist));
                        ArrayList<Entity> exclude = new ArrayList<>(player.getPassengerList());
                        exclude.add(player);
                        List<LivingEntity> hurt = JCraftUtils.GenerateHitbox(world,
                                hitPos,
                                attack.hitboxSize,
                                List.copyOf(exclude)
                        );

                        if (!hurt.isEmpty()) {
                            Random random = new Random();
                            JCraft.CreateParticle((ServerWorld) world,
                                    hitPos.x + random.nextDouble(-0.5, 0.5),
                                    hitPos.y + random.nextDouble(-0.5, 0.5),
                                    hitPos.z + random.nextDouble(-0.5, 0.5),
                                    attack.hitspark + 1);

                            if (attack.impactSound != null) {
                                for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.around(serverWorld, hitPos, 32)) {
                                    serverPlayerEntity.networkHandler.sendPacket(
                                            new PlaySoundS2CPacket(attack.impactSound, SoundCategory.PLAYERS, hitPos.x, hitPos.y, hitPos.z, 1, 1, 0)
                                    );
                                }
                            }

                            float kb = attack.knockback;
                            Vec3d kbVec = rotVec.multiply(kb).add(new Vec3d(0.0, Math.abs(attack.knockback) / 4, 0.0));

                            DamageSource playerSource = DamageSource.player(player);
                            for (LivingEntity livingEntity : hurt) {
                                if (livingEntity instanceof StandEntity) continue;
                                damageLogic(world, livingEntity, kbVec, stunS, attack.stunType, attack.overrideStun, attack.damage, attack.lift, attack.getEffectiveBlockstun(), playerSource, player, attack.canBackstab);
                            }

                            this.SpecialAttack(attack, hurt);
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
