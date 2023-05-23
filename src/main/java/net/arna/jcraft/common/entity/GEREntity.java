package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GEREntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = new AnimationFactory(this);

    public static Attack light = new Attack(2, 0.75f, 9, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.55f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch/Downward Kick", "quick combo starter, in air: more hitstun, less blockstun");
    public static Attack heavy = new Attack(17, 1f, 19, 10, 1.5, 9f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.IMPACT_2).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Overhead Smash/Overhead Kick", "slow, uninterruptable knockdown, in air: slow combo starter");
    public static Attack barrage = new Attack(14, 0.75f, 30, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage/Kick Barrage", "fast reliable combo starter/extender, high stun, in air: fast combo finisher, knocks back");
    public static Attack healself = new Attack(26, 1f, 14, 10, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand", "standing: heals user for 2 hearts, crouching: heals others for 3 hearts, pacifies angered mobs");
    public static Attack heal = new Attack(26, 1f, 16, 10, 0, 0f, 0f, AttackType.BOX);
    public static Attack laser = new Attack(24, 1f, 20, 10, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Life Beam", "summons a quick, stunning rock projectile that turns into a scorpion a small time after landing").setRanged(true);

    public static Attack counter = new Attack(26, 1f, 35, 5, 0, 0f, 0f, AttackType.COUNTER)
            .setInfo("Nullification", "0.25s windup, 1.5s counter, stuns on hit");

    public static Attack airlight = new Attack(2, 0.75f, 12, 5, 1.25, 4f, 0.75f, AttackType.BOX, 1, 0.33f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Downward Kick", "");
    public static Attack airheavy = new Attack(17, 1f, 24, 14, 1.5, 9f, 0.8f, AttackType.BOX, 2, 0.25f, 0, JSoundRegister.IMPACT_1).setHitspark(2)
            .setInfo("Overhead Kick", "");
    public static Attack airbarrage = new Attack(14, 1f, 48, 0, 1.5, 1f, 0.3f, AttackType.BARRAGE, 1, 0, 3)
            .setInfo("Kick Barrage", ""); //fast combo finisher, knocks back

    public static Attack rtz = new Attack(60, 32, 30, 0, 1, AttackType.BOX)
            .setInfo("Return to Zero", "initial press: saves the state of every entity in a 4 chunk radius (save lasts 1 minute), second press: reverts all states except users\nDoesn't affect player inventories");
    private static int rtzTimer;
    private static final HashMap<Entity, NbtCompound> rtzEntityData = new HashMap<>();

    private static final TrackedData<Integer> FLIGHTTIME;

    public Integer getFlightTime() {
        return this.dataTracker.get(FLIGHTTIME);
    }

    public void setFlightTime(int i) {
        this.dataTracker.set(FLIGHTTIME, i);
    }


    public GEREntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.Initialize();
        idleRotation = 0f;

        description = "Impossible Ascended DEFENSE";

        pros = List.of(
                "very mobile",
                "wide toolkit",
                "excellent defense",
                "setplay/combo tool (life beam)",
                "undo button"
        );

        cons = List.of(
                "low damage output",
                "limited pressure"
        );

        freespace = "BNBs:\n" +
                "the scorpy patty (sets up stand off transition)\n" +
                "(M1>)Barrage>jump>Overhead Kick>Life Beam>M1>Life Beam (second hit)\n" +
                "knockdown experience\n" +
                "M1>Barrage>Life Beam>M1~Overhead Smash>Life Beam (second hit)";

        moves = List.of(light, heavy, barrage, healself, rtz, laser, counter,
                new Attack().setInfo("Flight", "1 second").setMobility(MobilityType.FLIGHT));
    }

    static {
        FLIGHTTIME = DataTracker.registerData(GEREntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FLIGHTTIME, 0);
    }

    // Moveset
    @Override
    public void InitLightAttack() {
        CanAttackData data = CanAttackWithData();
        if (!data.canAttack) {
            return;
        }
        if (data.user.isOnGround()) {
            HandleAttack(light, JCraft.standLightCD, 2);
        } else {
            HandleAttack(airlight, JCraft.standLightCD, 10);
        }
    }

    @Override
    public void InitHeavyAttack() {
        CanAttackData data = CanAttackWithData();
        if (!data.canAttack) {
            return;
        }
        if (data.user.isOnGround()) {
            if (HandleAttack(heavy, JCraft.standHeavyCD, 4))
                this.playSound(JSoundRegister.GER_HEAVY, 1, 1);
        } else {
            if (HandleAttack(airheavy, JCraft.standHeavyCD, 11))
                this.playSound(JSoundRegister.GER_HEAVY, 1, 1);
        }
    }

    @Override
    public void InitBarrage() {
        CanAttackData data = CanAttackWithData();
        if (!data.canAttack) {
            return;
        }
        if (data.user.isOnGround()) {
            if (HandleAttack(barrage, JCraft.standBarrageCD, 5))
                this.playSound(JSoundRegister.GE_BARRAGE, 1, 1);
        } else {
            if (HandleAttack(airbarrage, JCraft.standBarrageCD, 12))
                this.playSound(JSoundRegister.GER_KICKBARRAGE, 1, 1);
        }
    }

    @Override
    public void InitSpecial1() {
        CanAttackData data = this.CanAttackWithData();
        if (!data.canAttack) {
            return;
        }
        if (data.user.isSneaking()) {
            if (HandleAttack(heal, JCraft.standS1CD, 7)) {
                this.playSound(JSoundRegister.GE_HEAL, 1, 1);
            }
        } else if (HandleAttack(healself, JCraft.standS1CD, 6)) {
            this.playSound(JSoundRegister.GE_HEAL, 1, 1);
        }
    }

    @Override
    public void InitMiddleClick() {
        if (!this.CanAttack()) {
            return;
        }
        NbtCompound data = ((IEntityDataSaver) getUser()).getPersistentData();
        if (data.getInt(JCraft.standMMBCD) > 0) {
            return;
        }
        data.putInt(JCraft.standMMBCD, 360); // 18 second flight cd
        setFlightTime(20);
    }

    @Override
    public void InitSpecial2() {
        if (!this.CanAttack()) {
            return;
        }
        if (HandleAttack(laser, JCraft.standS2CD, 8)) {
            this.playSound(JSoundRegister.GER_LASER, 1, 1);
        }
    }

    @Override
    public void InitSpecial3() {
        if (!this.CanAttack()) {
            return;
        }
        if (HandleAttack(counter, JCraft.standS3CD, 9)) {
            this.playSound(JSoundRegister.GE_HEAL, 1, 1);
        }
    }


    private static final int counterStopTime = 20; // Convenience

    @Override
    public void Counter(Entity entity, DamageSource source) {
        super.Counter(entity, source);
        if (entity == null || !hasUser()) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(14);
        buf.writeInt(entity.getId());
        buf.writeInt(counterStopTime);
        for (PlayerEntity sendPlayer : world.getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) sendPlayer, JCraft.serverFeedbackChannel, buf);
        }
        ((ITimeStop) entity).setTimeStopTicks(counterStopTime);

        if (entity.getFirstPassenger() instanceof StandEntity stand) {
            stand.CancelAttack();
        }

        if (entity instanceof LivingEntity living) {
            Stun(living, 10, 0);
        }

        Vec3d eP = this.getEyePos();
        JCraft.CreateParticle((ServerWorld) world, eP.x, eP.y, eP.z, -1);
    }

    @Override
    public void InitUlt() {
        if (!this.CanAttack()) {
            return;
        }
        if (rtzEntityData.isEmpty()) {
            // Setup
            if (HandleAttack(rtz, JCraft.standUltCD, 13)) {
                this.playSound(JSoundRegister.GER_SETUP, 1, 1);
            }
        } else {
            // Fun part
            for (Map.Entry<Entity, NbtCompound> data :
                    rtzEntityData.entrySet()) {
                Entity ent = data.getKey();
                if (!ent.isAlive()) {
                    continue;
                }
                NbtCompound nbt = data.getValue();

                if (ent instanceof PlayerEntity playerEntity) {
                    nbt.put("Inventory", playerEntity.getInventory().writeNbt(new NbtList()));
                    nbt.put("EnderItems", playerEntity.getEnderChestInventory().toNbtList());

                    ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) playerEntity);
                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ent));
                    NbtList list = nbt.getList("Pos", 6);
                    serverPlayer.teleport(list.getDouble(0), list.getDouble(1), list.getDouble(2));
                }

                ent.readNbt(nbt);
            }

            rtzEntityData.clear();
            rtzTimer = 0;
        }
    }

    /*
    @Override
    public boolean SpecificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        if (attack == snake) {
            return !mob.getMainHandStack().isEmpty() || !mob.getOffHandStack().isEmpty();
        }
        if (this.getMoveStun() > 0 && attack == rekka1) {
            return (this.curAttack == rekka1 || this.curAttack == rekka2);
        }
        return false;
    }
     */

    @Override
    public void Desummon() {
        if (getFlightTime() > 0) {
            setFlightTime(0);
            return;
        }
        super.Desummon();
    }

    private static final Attack barrageFinisher = new Attack(17, 1f, 9, 6, 1.75, 1f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2).setLaunch();

    @Override
    public void SpecialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        if (attack == healself) {
            user.heal(4f);
        } else if (attack == heal) {
            for (LivingEntity ent : entities) {
                ent.heal(6f);
                ent.setAttacker(null);

                if (ent instanceof MobEntity mob) {
                    Stun(mob, 10, 0);
                    mob.setTarget(null);
                    mob.setAttacking(null);
                    if (mob instanceof Angerable angerable) {
                        angerable.stopAnger();
                    }
                }
            }
        } else if (attack == heavy) {
            for (LivingEntity l : entities) {
                l.addStatusEffect(new StatusEffectInstance(JStatusRegister.Knockdown, 30, 0, false, false));
            }
        } else if (attack == laser) {
            GERScorpionEntity scorpion = new GERScorpionEntity(JEntityTypeRegister.GERSCORPION, world);
            scorpion.setInitialVel(user.getRotationVector().multiply(2));
            Vec3d ePos = this.getEyePos();
            scorpion.refreshPositionAndAngles(ePos.x, ePos.y, ePos.z, -user.getYaw() - 90f, getPitch());
            scorpion.setOwner(user);
            world.spawnEntity(scorpion);
        } else if (attack == airheavy) {
            for (LivingEntity ent : entities) {
                ent.addVelocity(0, -1, 0);
                ent.velocityModified = true;
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 19, false, false));
            }
        } else if (attack == airbarrage && this.getMoveStun() < 12) {
            this.curAttack = barrageFinisher;
        } else if (attack == rtz) {
            List<Entity> toReturn = world.getEntitiesByClass(Entity.class, this.getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
            toReturn.remove(this);
            toReturn.remove(user);

            for (Entity e : toReturn) {
                NbtCompound data = new NbtCompound();
                e.writeNbt(data);
                rtzEntityData.put(e, data);
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.GER_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            // Must be run on client and server because of fun mod compatibility
            int flightTime = getFlightTime();
            flightTime -= 1;
            setFlightTime(flightTime);
            if (user instanceof PlayerEntity playerEntity) {
                if (!playerEntity.isCreative() && !playerEntity.isSpectator()) {
                    playerEntity.getAbilities().flying = (flightTime > 1);
                }
            } else if (flightTime > 1) {
                double y = user.getY();
                Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);
                // Targetting priority
                LivingEntity targetEntity = user.getDamageTracker().getBiggestAttacker();
                if (targetEntity == null && user instanceof MobEntity mob) {
                    targetEntity = mob.getTarget();
                }
                if (targetEntity == null) {
                    targetEntity = user.getAttacker();
                }
                // If target wasnt found, search in a radius
                Vec3d target = targetEntity != null ? targetEntity.getEyePos() : this.getPos().add(Math.sin(this.age * 0.2) * 3, 0, Math.cos(this.age * 0.2) * 3);

                double dY = MathHelper.clamp(target.getY() - y, -1, 1);
                y += dY;

                vel = vel.add(target.subtract(user.getPos()).normalize()).multiply(0.4);

                user.setVelocity(vel);
                user.setPos(user.getX(), y, user.getZ());

                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 1, true, false));
            }

            if (world.isClient) {
                this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            } else if (rtzTimer > 0) {
                rtzTimer -= 1;
                if (rtzTimer == 0) {
                    rtzEntityData.clear();
                }
            }
            /*
            else if (flightTime > 0 && getMoveStun() > 0) {
                //player.setVelocity(player.getVelocity().multiply(0.999));
                player.velocityModified = true;
            }
             */
        }
    }

    // Animation code
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        switch (this.getState()) {

            default -> controller.setAnimation(builder.loop("animation.ger.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.ger.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.ger.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.ger.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.ger.barrage"));

            case 6 -> controller.setAnimation(builder.playAndHold("animation.ger.healself"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.ger.heal"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.ger.laser"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.ger.counter"));

            case 10 -> controller.setAnimation(builder.playAndHold("animation.ger.airlight"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.ger.airheavy"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.ger.airbarrage"));

            case 13 -> controller.setAnimation(builder.playAndHold("animation.ger.setup"));
        }

        return PlayState.CONTINUE;
    }
}
