package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackQueue;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TheWorldOverHeavenEntity extends StandEntity<TheWorldOverHeavenEntity, TheWorldOverHeavenEntity.State> {
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 7, 4, 1.5, 6f, 0.75f, AttackType.BOX, 0.55f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 50, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 2, 0, 3, JSoundRegister.IMPACT_1)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack heavy = new Attack(1, 19, 1f, 22, 10, 2, 0f, 0.0f, AttackType.BOX, 1, 0, 0, JSoundRegister.IMPACT_5)
            .setHitspark(2)
            .setUB(true)
            .setInfo("Singularity", "block bypass, low stun, medium windup");
    public static final Attack smite = new Attack(3, 21, 1f, 20, 10, 0, 6f, 0.0f, AttackType.BOX, 1.05f, 0, 0)
            .setBlockstun(13)
            .setInfo("You won't run away!", "summons a stunning lightning bolt at the user/in air summons one at aimed position, launches on hit");
    public static final Attack overwrite = new Attack(6, 0, 1f, 23, 7, 2, 0f, 1.0f, AttackType.BOX, 2, 0, 0, JSoundRegister.IMPACT_5)
            .setHitspark(2)
            .setLaunch()
            .hyperArmor()
            .setUB(false)
            .setInfo("Overwrite (Hit)", "", AttackQueue.SPECIAL1);
    public static final Attack chargeoverwrite = new Attack(8, 30, 70, 71, 0, AttackType.BOX)
            .disableBackstab()
            .setFollowup(overwrite)
            .setInfo("Reality Overwrite",
                    """
                            charges (for a minimum of 1s) an unblockable punch that changes the reality of the hit victims
                            While charging, (de)activate overwrite by pressing:
                            SPECIAL 1 - makes victims unable to look at you
                            SPECIAL 2 - applies every damage over time effect to victims
                            SPECIAL 3 - heals and enslaves mobs""");
    public static final Attack knives = new Attack(4, 19, 0.75f, 22, 16, 1.5, 0f, 0.0f, AttackType.BOX, 1)
            .setBlockstun(6)
            .setRanged(true)
            .setInfo("Aerial Divine Finisher", "");
    public static final Attack delayknives = new Attack(5, 19, 0.75f, 22, 16, 1.5, 0f, 0.0f, AttackType.BOX, 1)
            .setBlockstun(6)
            .setRanged(true)
            .aerialVariation(knives)
            .setInfo("Divine Finisher", "fires 4 stunning knives that launch at a delay/in air summons and launches 8 knives");

    public static final Attack timestop = new Attack(7, 70, 50, 45, 5, AttackType.TIMESTOP)
            .setUB(true)
            .setInfo("Timestop", "5 seconds");
    private static final Attack timeskip = new Attack(-2, 18, 2, 2)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Timeskip", "14m range");

    private static final TrackedData<Integer> OVERWRITE_TYPE;
    private Vec3d lightningPos;
    private final List<LivingEntity> overwriteEnts = new ArrayList<>();
    private final List<Integer> overwriteTimes = new ArrayList<>();
    private float smiteDamage = 6f;

    static {
        OVERWRITE_TYPE = DataTracker.registerData(TheWorldOverHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public TheWorldOverHeavenEntity(World worldIn) {
        super(StandType.THE_WORLD_OVER_HEAVEN, worldIn);
        super.initialize();
        idleRotation = -45f;
        summonAnimDuration = 29;

        pros = List.of(
                "fast m1",
                "longest timestop",
                "unblockable heavy",
                "good ranged coverage"
        );

        cons = List.of(
                "no knockdowns or knockbacks",
                "unsafe pressure"
        );

        description = "Mid Range DOMINATOR";

        freespace =
                """
                        BNBs:
                            the ultrakill
                            M1>Barrage>M1>Knives>Overwrite~S2/S3>dash>Smite>Heavy>M1""";

        moves = List.of(light, heavy, barrage, smite, timestop, delayknives, chargeoverwrite, timeskip);
    }

    public int getOverwriteType() {
        return dataTracker.get(OVERWRITE_TYPE);
    }

    public void setOverwriteType(int type) {
        dataTracker.set(OVERWRITE_TYPE, type);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OVERWRITE_TYPE, 0);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, State.LIGHT);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegister.TWOH_BARRAGE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, State.HEAVY))
            playSound(JSoundRegister.TWOH_HEAVY, 1, 1);
    }

    private void initOverwrite(int type) {
        setOverwriteType(type);
        setAttack(overwrite, State.OVERWRITE);
        playSound(JSoundRegister.TWOH_OVERWRITE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (curAttack == chargeoverwrite && getMoveStun() < 50) {
            initOverwrite(1);
            return;
        }

        if (canAttack() && handleAttack(smite, JCraft.standS1CD, State.SMITE) && hasUser()) {
            LivingEntity user = getUserOrThrow();
            if (user.isOnGround()) {
                smiteDamage = 8f;
                this.lightningPos = user.getPos();
            } else {
                smiteDamage = 6f;
                Vec3d eP = user.getEyePos();
                Vec3d rangeMod = user.getRotationVector().multiply(24);
                EntityHitResult eHit = ProjectileUtil.raycast(user, eP, eP.add(rangeMod),
                        user.getBoundingBox().expand(24),
                        EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                        576 // Squared
                );

                if (eHit != null) lightningPos = eHit.getPos();
                else
                    lightningPos = world.raycast(new RaycastContext(eP, eP.add(rangeMod), RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE, user)).getPos();
            }

            AreaEffectCloudEntity effectCloud = new AreaEffectCloudEntity(world, lightningPos.x, lightningPos.y, lightningPos.z);
            effectCloud.setOwner(user);
            effectCloud.setRadius(smiteDamage / 2f);
            effectCloud.setWaitTime(10);
            effectCloud.setRadiusGrowth(-0.5f);

            world.spawnEntity(effectCloud);

            world.playSound(null, lightningPos.x, lightningPos.y, lightningPos.z, JSoundRegister.TWOH_CHARGE, SoundCategory.PLAYERS, 1, 1);
            playSound(JSoundRegister.TWOH_SMITE, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (curAttack == chargeoverwrite && getMoveStun() < 50) {
            initOverwrite(2);
            return;
        }

        CanAttackData cad = this.canAttackWithData();
        if (!cad.canAttack()) return;

        if (cad.user().isOnGround() && handleAttack(delayknives, JCraft.standS2CD, State.AIR_KNIVES))
            playSound(JSoundRegister.TWOH_AIRKNIVES, 1, 1);
        else if (handleAttack(knives, JCraft.standS2CD, State.THROW)) playSound(JSoundRegister.TWOH_KNIFETHROW, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (curAttack == chargeoverwrite && getMoveStun() < 50) {
            initOverwrite(3);
            return;
        }

        if (canAttack() && handleAttack(chargeoverwrite, JCraft.standS3CD, State.CHARGE_OVERWRITE))
            playSound(JSoundRegister.TWOH_CHARGEOVERWRITE, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(timestop, JCraft.standUltCD, State.TIME_STOP))
            playSound(JSoundRegister.TWOH_TS, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;
        handleAttack(timeskip, JCraft.utilCD, State.IDLE);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        DamageSource damageSource = JDamageSources.stand(this);

        switch (attack.id) {
            case (-2) -> {
                if (tsTime > 0) return;
                timeSkip(14, JSoundRegister.TWOH_TIMESKIP);
            }
            case (1) -> { // TWOH's heavy is a mini-overwrite that ignores block
                for (LivingEntity ent : entities) {
                    stun(ent, 20, 1);
                    ent.damage(damageSource, 0.001f);
                    float damage = 6f;

                    // All stands ignore 10% of armor & armor toughness
                    damage = DamageUtil.getDamageLeft(damage, (float) ent.getArmor() * 0.9f, (float) ent.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * 0.9f);
                    // NOTE THE LACK OF invokeModifyAppliedDamage()

                    float f = damage;
                    damage = Math.max(damage - ent.getAbsorptionAmount(), 0.0F);
                    ent.setAbsorptionAmount(ent.getAbsorptionAmount() - (f - damage));

                    if (damage != 0.0F) {
                        float h = ent.getHealth();
                        ent.setHealth(h - damage);
                        ent.getDamageTracker().onDamage(damageSource, h, damage);
                        if (ent.isDead())
                            ent.onDeath(damageSource);
                    }
                }
            }
            case (3) -> {
                Vec3d lP = lightningPos;

                LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, this.world);
                lightning.setCosmetic(true);
                lightning.setPosition(lP);

                List<? extends Entity> hit = JUtils.generateHitbox(world, lP, 3, Entity.class, List.of(this, user));
                for (Entity ent : hit) {
                    if (ent instanceof LivingEntity living) {
                        LivingEntity target = JUtils.getUserIfStand(living);
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 7, 9, true, false));
                        damageLogic(world, target, Vec3d.ZERO, 21, 1, false, smiteDamage, false, 13, damageSource, user);
                    }

                    ent.onStruckByLightning((ServerWorld) world, lightning);
                }

                world.spawnEntity(lightning);
            }
            case (4) -> {
                for (int i = 0; i < 8; i++) {
                    KnifeProjectile knife = new KnifeProjectile(world, user);
                    knife.setLightning(true);
                    knife.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    knife.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2F, 1F);
                    knife.setPosition(user.getPos().add(
                            random.nextTriangular(0, 0.5),
                            random.nextTriangular(1.5, 0.5),
                            random.nextTriangular(0, 0.5)));
                    world.spawnEntity(knife);
                }
            }
            case (5) -> {
                Vec3d rotVec = user.getRotationVector();

                for (int i = 0; i < 4; i++) {
                    KnifeProjectile knife = new KnifeProjectile(world, user);
                    knife.setDelayedLightning(10 + i * 5);
                    knife.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    knife.setNoGravity(true);
                    knife.setVelocity(new Vec3d(rotVec.x * 0.7, 0, rotVec.z * 0.7).rotateY(1.5708f * i));
                    knife.setPosition(getEyePos());
                    world.spawnEntity(knife);
                }
            }
            case (6) -> {
                for (LivingEntity ent : entities) {
                    ent.removeStatusEffect(JStatusRegister.DAZED);
                    stun(ent, 30, 3);

                    if (getOverwriteType() == 1) {
                        overwriteTimes.add(200);
                        overwriteEnts.add(ent);
                    }

                    if (getOverwriteType() == 2) {
                        ent.setOnFireFor(5);
                        ent.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 1, false, true));
                        ent.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1, false, true));
                        ent.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1, false, true));
                    }

                    if (getOverwriteType() != 3) continue;
                    ent.heal(4f);

                    if (!(ent instanceof MobEntity)) continue;
                    IEntityDataSaver entityDataSaver = (IEntityDataSaver) ent;
                    entityDataSaver.getPersistentData().putUuid("SlavedTo", user.getUuid());
                    overwriteTimes.add(1048576);
                    overwriteEnts.add(ent);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        if (age == 1) {
            this.playSound(JSoundRegister.TWOH_SUMMON, 1f, 1f);
            this.playSound(JSoundRegister.TW_SUMMON, 1f, 1f);

            List<LivingEntity> hit = world.getEntitiesByClass(LivingEntity.class, new Box(
                            getPos().add(-64, -64, -64), getPos().add(64, 64, 64)),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != user));

            for (LivingEntity ent : hit) {
                NbtCompound entityData = ((IEntityDataSaver) ent).getPersistentData();
                if (entityData.contains("SlavedTo")) {
                    if (entityData.getUuid("SlavedTo").equals(user.getUuid())) {
                        overwriteEnts.add(ent);
                        overwriteTimes.add(1048576); // 2 to the whatever
                    }
                }
            }
        }

        if (world.isClient) {
            setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
            return;
        }

        if (getOverwriteType() != 0 && getMoveStun() <= 0) setOverwriteType(0);

        for (int i = 0; i < overwriteTimes.size(); i++) {
            int time = overwriteTimes.get(i);
            overwriteTimes.set(i, time - 1);

            if (time < 1) {
                overwriteTimes.remove(i);
                overwriteEnts.remove(i);
                i--;
            } else {
                LivingEntity entity = overwriteEnts.get(i);

                if (entity instanceof MobEntity mob && time > 200) { // Targeting and movement for mobs
                    LivingEntity victim = user.getAttacking();
                    if (victim == null) {
                        LivingEntity adv = user.getPrimeAdversary();
                        if (adv != null && adv.isAlive()) {
                            mob.setTarget(adv);
                        }
                    } else if (victim.isAlive()) {
                        mob.setTarget(victim);
                    }

                    if (mob.squaredDistanceTo(this) > 256)
                        mob.getNavigation().startMovingTo(this, 1);
                }

                // Inability to look at master
                double range = 1024.0;

                Box box = entity
                        .getBoundingBox()
                        .stretch(entity.getRotationVec(1.0F).multiply(range))
                        .expand(1.0D);
                EntityHitResult hitResult = ProjectileUtil.raycast(
                        entity,
                        entity.getEyePos(),
                        entity.getEyePos().add(entity.getRotationVector().multiply(range)),
                        box,
                        EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                        range);

                if (hitResult == null) continue;
                Entity lookEntity = hitResult.getEntity();

                if (lookEntity != user && lookEntity != this) continue;
                entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, getEyePos().add(
                        random.nextInt() * 10,
                        random.nextInt() * 10,
                        random.nextInt() * 10));
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldOverHeavenEntity> {
        IDLE(builder -> builder.loop("animation.twoh.idle")),
        LIGHT(builder -> builder.playAndHold("animation.twoh.light")),
        BLOCK(builder -> builder.loop("animation.twoh.block")),
        HEAVY(builder -> builder.playAndHold("animation.twoh.heavy")),
        BARRAGE(builder -> builder.loop("animation.twoh.barrage")),
        SMITE(builder -> builder.playAndHold("animation.twoh.smite")),
        TIME_STOP(builder -> builder.playAndHold("animation.twoh.timestop")),
        CHARGE_OVERWRITE(builder -> builder.loop("animation.twoh.chargeoverwrite")),
        OVERWRITE(builder -> builder.playAndHold("animation.twoh.overwrite")),
        THROW(builder -> builder.playAndHold("animation.twoh.throw")),
        AIR_KNIVES(builder -> builder.playAndHold("animation.twoh.airknives"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldOverHeavenEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.twoh.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
