package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

//TODO: give MiH a trail during speed slice and heaven's judgement
public class MadeInHeavenEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    // placeholder sound
    public static Attack light = new Attack(2, 0.75f, 8, 5, 1.5, 4f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, SoundEvents.ITEM_TRIDENT_HIT)
            .setInfo("Slice", "quick combo starter");
    public static Attack barrage = new Attack(17, 0.85f, 32, 0, 2, 1.5f, 0.1f, AttackType.BARRAGE, 0.5f, 0, 3, JSoundRegister.IMPACT_1)
            .setInfo("Barrage", "short, knocks back");
    public static Attack speedslice = new Attack(18, 1.25f, 11, 10, 0, 6f, 0.5f, AttackType.BOX, 1f, 0, 0)
            .setRanged(true).setMobility(MobilityType.TELEPORT)
            .setInfo("Speed Slice", "short windup, harming teleport with hitstun and light knockback");
    public static Attack judgement = new Attack(37, 1.25f, 60, 20, 0, 0f, 0.5f, AttackType.BARRAGE, 0, 0, 2, null)
            .setInfo("Heaven's Judgement", "mih rapidly speed slices an area and finishes with a larger one, knocks back");
    public static Attack legcrusher = new Attack(16, 0.75f, 17, 8, 1.25, 7f, 0.25f, AttackType.BOX, 1.5f, 0.2f, 0, JSoundRegister.TW_KICK_HIT)
            .setInfo("Leg Crusher", "combo starter/extender, mih hoofs the enemies legs in a quick, stunning attack");
    public static Attack furychop = new Attack(19, 0.75f, 24, 15, 1.6, 7f, 0.25f, AttackType.BOX, 1f, 0.2f, 0, JSoundRegister.IMPACT_2).setHitspark(2)
            .setInfo("Fury Chop", "combo extender, on hit gives haste(8s) to user and mining fatigue(8s) to victim, on whiff the fatigue goes to user");
    public static Attack donut = new Attack(23, 0.75f, 32, 26, 2.2, 8.5f, 0.0f, AttackType.BOX, 3f, 0.2f, 0, JSoundRegister.IMPACT_4).setArmor(true).setHitspark(2)
            .setInfo("Roundabout Donut", "feigns stand desummon, uninterruptable combo starter");
    public static Attack timeaccel = new Attack(70, 40, 20, 0, AttackType.BOX)
            .setInfo("Time Acceleration", "2s windup, 15s t. accel, enemies standless for 15s after finishing");

    public Vec3d judgementInitPos = Vec3d.ZERO;
    public Vec3d judgementInitRot = Vec3d.ZERO;

    public static TrackedData<Integer> ACCELTIME;

    public MadeInHeavenEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.initialize();
        idleRotation = 225f;

        description = "Lightspeed RUSHDOWN";

        pros = List.of(
                "absurdly good mobility",
                "good zoning tools",
                "good pressure",
                "low cooldowns"
        );

        cons = List.of(
                "zero defensive options barring running away",
                "vulnerable to block"
        );

        freespace =
                "PASSIVE: Speed I\n\n" +
                        "BNBs:\n" +
                        "    (Donut>M1>)Speed Slice>Leg Crusher>Fury Chop>M1>Barrage";

        moves = List.of(light, donut, barrage, legcrusher, timeaccel, furychop, judgement, speedslice);
    }

    static {
        ACCELTIME = DataTracker.registerData(MadeInHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getAccelTime() {
        return this.dataTracker.get(ACCELTIME);
    }

    public void setAccelTime(int aTime) {
        this.dataTracker.set(ACCELTIME, aTime);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(ACCELTIME, 0);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) {
            return;
        }
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(donut, JCraft.standHeavyCD, 4)) {
            //this.playSound(ModSoundRegister.STAR_BREAKER,1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            //this.playSound(ModSoundRegister.MIH_BARRAGE,1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(legcrusher, JCraft.standS1CD, 8)) {
            //this.playSound(ModSoundRegister.MIH_LEGCRUSHER,1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(timeaccel, JCraft.standUltCD, 10)) {
            this.playSound(JSoundRegister.MIH_TACCEL, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(furychop, JCraft.standS2CD, 9)) {
            this.playSound(JSoundRegister.MIH_FURYCHOP, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(judgement, JCraft.standS3CD, 7)) {
            this.playSound(JSoundRegister.MIH_JUDGEMENT, 1, 1);
        }
    }


    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(speedslice, JCraft.standMMBCD, 6)) {
            this.playSound(JSoundRegister.MIH_SPEEDSLICE, 1, 1);
        }
    }

    @Override
    public boolean handleAttack(Attack attack, String cooldownName, int animState) {
        LivingEntity player = this.getUser();
        IEntityDataSaver user = (IEntityDataSaver) player;
        int cooldown = user.getPersistentData().getInt(cooldownName);
        if (cooldown > 0) {
            return false;
        }
        this.curAttack = attack;
        this.setMoveStun(attack.moveStun);

        int cdMult = (this.getAccelTime() > 0) ? 10 : 20;
        user.getPersistentData().putInt(cooldownName, attack.cooldown * cdMult);

        this.setState(animState);
        return true;
    }

    private static final Attack barrageFinisher = new Attack(17, 0.85f, 9, 6, 1.5, 1f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2).setLaunch();

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        if (attack == speedslice) {
            this.curAttack = null;
            SpeedSlice(user, user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(8)), 6, 1, 1.75);
        } else if (attack == judgement) {
            if (this.getMoveStun() > 1) {
                if (this.getMoveStun() < 40) {
                    SpeedSlice(user,
                            judgementInitPos.add(judgementInitRot.multiply(random.nextTriangular(2, 2))),
                            judgementInitPos.add(random.nextTriangular(0, 5), random.nextTriangular(0, 5), random.nextTriangular(0, 5)),
                            1f, 0.1f, 1.75);
                } else {
                    judgementInitPos = user.getPos();
                    judgementInitRot = Vec3d.fromPolar(0, user.getYaw());
                }
            } else {
                SpeedSlice(user,
                        judgementInitPos.subtract(user.getRotationVector().multiply(3)),
                        judgementInitPos.add(judgementInitRot.multiply(10)), 6, 3, 2.0);
            }
        } else if (attack == furychop) {
            if (entities.size() > 0) {
                for (LivingEntity ent : entities) {
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 0));
                }
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 160, 0));
            } else {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 0));
            }
        } else if (attack == timeaccel) {
            this.setAccelTime(300);
        } else if (attack == barrage && this.getMoveStun() < 10) {
            this.curAttack = barrageFinisher;
        }
    }

    private void SpeedSlice(LivingEntity player, Vec3d start, Vec3d destination, float damage, float kb, double size) {
        HitResult hitResult = this.world.raycast(new RaycastContext(start, destination, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        Vec3d pos1 = player.getPos();
        Vec3d pos2 = hitResult.getPos();
        Vec3d towardsVec = pos2.subtract(pos1);

        Vec3d kbVec = towardsVec.normalize();

        DamageSource playerSource = DamageSource.mob(player);

        player.teleport(pos2.x, pos2.y, pos2.z);

        List<LivingEntity> hurtAll = new ArrayList<>();

        double count = Math.round(pos1.distanceTo(pos2));

        for (int i = 0; i < count; i++) {
            Vec3d curPos = pos1.add(towardsVec.multiply(i / count));

            Vec3d vec1 = curPos.add(-size, -size, -size);
            Vec3d vec2 = curPos.add(size, size, size);

            if (this.world.getGameRules().getBoolean(JCraft.SHOW_HITBOXES)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(1);

                buf.writeDouble(vec1.x);
                buf.writeDouble(vec2.x);

                buf.writeDouble(vec1.y);
                buf.writeDouble(vec2.y);

                buf.writeDouble(vec1.z);
                buf.writeDouble(vec2.z);
                for (PlayerEntity serverPlayer : this.world.getPlayers()) {
                    if (serverPlayer instanceof ServerPlayerEntity serverPlayerEntity) {
                        ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                    }
                }
            }

            List<LivingEntity> hurt = this.world.getEntitiesByClass(LivingEntity.class, new Box(vec1, vec2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
            hurt.removeIf(hurtAll::contains);
            hurtAll.addAll(hurt);
        }

        hurtAll.remove(this);
        hurtAll.remove(player);
        //if (!hurtAll.contains(player)) { hurtAll.add(player); }

        for (LivingEntity ent : hurtAll) {
            damageLogic(world, ent, kbVec.multiply(kb).add(0, kb / 4, 0), 20, 1, false, damage, true, playerSource, player);
        }

        this.playSound(JSoundRegister.MIH_ZOOM, 1f, 1f);
    }

    @Override
    public void desummon() {
        if (this.getTSTime() < 1) {
            super.desummon();
        }
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();

        int aTime = getAccelTime();

        if (world.isClient()) {
            if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE) && aTime > 0) {
                ClientWorld clientWorld = (ClientWorld) world;
                clientWorld.setTimeOfDay(clientWorld.getTimeOfDay() + 4800 / aTime);
            }
        } else if (hasUser()) {
            LivingEntity user = this.getUser();
            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);

            Vec3d pos = this.getPos();

            if (!this.world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;

                if (aTime > 1) {
                    List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                            new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                    toCatch.remove(this);
                    toCatch.remove(user);

                    for (Entity entity : toCatch) {
                        if (entity instanceof LivingEntity)
                            continue;
                        entity.tick();
                    }

                    if (serverWorld.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
                        serverWorld.setTimeOfDay(serverWorld.getTimeOfDay() + 4800 / aTime);

                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 2, true, false));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 2, true, false));
                } else if (aTime == 1) {
                    List<LivingEntity> toCatch = world.getEntitiesByClass(LivingEntity.class,
                            new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                    toCatch.remove(this);
                    toCatch.remove(user);

                    for (LivingEntity entity : toCatch) // 15s of Standless to any victims of Time Acceleration
                        entity.addStatusEffect(new StatusEffectInstance(JStatusRegister.STANDLESS, 300, 0, true, false));
                } else if (!user.hasStatusEffect(JStatusRegister.DAZED)) {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false));
                }

                this.setAccelTime(aTime - 1);
            }
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
            default -> controller.setAnimation(builder.loop("animation.mih.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.mih.slice"));
            case 3 -> controller.setAnimation(builder.loop("animation.mih.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.mih.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.mih.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.mih.speedslice"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.mih.judgement"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.mih.legcrusher"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.mih.furychop"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.mih.taccel"));

            //default -> throw new IllegalStateException("Unexpected value: " + this.getState());
        }
        return PlayState.CONTINUE;
    }
}
