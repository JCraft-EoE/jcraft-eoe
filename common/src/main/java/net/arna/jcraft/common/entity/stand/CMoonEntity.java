package net.arna.jcraft.common.entity.stand;

import lombok.Data;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.CMoonInversionAction;
import net.arna.jcraft.common.attack.moves.cmoon.*;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.TossChargeMove;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/C-MOON">C-MOON</a>.
 * @see JStandTypeRegistry#C_MOON
 * @see net.arna.jcraft.client.renderer.entity.stands.CMoonRenderer CMoonRenderer
 * @see GravitationalHopMove
 * @see GravityShiftMove
 * @see GravityShiftPulseMove
 * @see CGroundSlamAttack
 * @see LaunchAttack
 */
public class CMoonEntity extends StandEntity<CMoonEntity, CMoonEntity.State> {
    public static final MoveSet<CMoonEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.C_MOON, CMoonEntity::registerMoves, CMoonEntity.class, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(220f)
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.cmoon"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                Passive: Inversion, all physical hits deal an extra half heart after 2s

                    BNBs:
                    -going up?
                    Light>Barrage>jump>Block Launch>Light>Only One Punch>Block Launch (Projectile Hit)>...
                        ...Grav. Hop>Ground Slam
                        ...Gut Punch"""))
                    .skinName(Component.literal("Inversion"))
                    .skinName(Component.literal("Gravity"))
                    .skinName(Component.literal("Rose"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.CMOON_SUMMON))
            .build();

    public static final int GRAVITY_CHANGE_DURATION = 600; // in ticks

    public static final SimpleAttack<CMoonEntity> INVERSION_PUNCH = SimpleAttack.<CMoonEntity>lightAttack(
            6,12,0.75f, 5f, 9, 0.5f, -0.1f)
            .withAnim(State.INVERSION_PUNCH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withAction(CMoonInversionAction.addInversion(70, 0.5f, true))
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Inversion Punch"),
                    Component.literal("Very low stun, inflicts Slowness with a delayed inversion hit.")
            );
    public static final SimpleAttack<CMoonEntity> LIGHT_FOLLOWUP = new SimpleAttack<CMoonEntity>(0,
            6, 12, 0.75f, 6, 7, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("Quick combo finisher.")
            );
    public static final SimpleAttack<CMoonEntity> PUNCH = SimpleAttack.<CMoonEntity>lightAttack(
            5, 7, 0.75f, 5f, 10, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(INVERSION_PUNCH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("Quick combo starter.")
            );

    public static final MainBarrageAttack<CMoonEntity> BARRAGE = new MainBarrageAttack<CMoonEntity>(280,
            0, 40, 0.75f, 0.75f, 20, 2f, 0.25f, 0f, 4, Blocks.OBSIDIAN.defaultDestroyTime())
            .withSound(JSoundRegistry.CMOON_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withAction(CMoonInversionAction.addInversion(40, 0.25f, false))
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("Fast reliable combo starter/extender, medium stun.")
            );

    public static final CDivekickAttack DIVEKICK = new CDivekickAttack(100,
            9, 18, 7.0f, 8f, 20, 2.0f, 0.3f, 0.3f)
            .withSound(JSoundRegistry.CMOON_BLOCKHALT)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withStaticY()
            .withInfo(
                    Component.literal("Diving Strike"),
                    Component.literal("User hovers, and C-Moon crashes downwards. Use when above the enemy.")
            );
    public static final SimpleAttack<CMoonEntity> GUT_PUNCH = new SimpleAttack<CMoonEntity>(0,
            19, 30,1f, 8f, 10, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.CMOON_DONUT)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withExtraHitBox(0, 0.25, 1.25)
            .withCrouchingVariant(DIVEKICK)
            .withInfo(
                    Component.literal("Gut Punch"),
                    Component.literal("Slow, uninterruptible combo finisher.")
            );

    public static final LaunchAttack<CMoonEntity> LAUNCH_3 = new LaunchAttack<CMoonEntity>(100,
            14, 24, 0.75f,6f, 19, 1.85f, 1.3f, 0.3f, 3)
            .withSound(JSoundRegistry.CMOON_GROUNDSHOOT)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withLaunch()
            .withInfo(
                    Component.literal("Triplet Block Launch"),
                    Component.literal("""
                            Lifts a 3 blocks from the ground and launches them at a delay.
                            """)
            );

    public static final LaunchAttack<CMoonEntity> LAUNCH = new LaunchAttack<CMoonEntity>(60,
            14, 21, 0.75f,5f, 19, 1.75f, 0.9f, 0.3f, 1)
            .withSound(JSoundRegistry.CMOON_GROUNDSHOOT)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withCrouchingVariant(LAUNCH_3)
            .withInfo(
                    Component.literal("Block Launch"),
                    Component.literal("""
                            Lifts a block from the ground and launches it at a delay.
                            Use crouching Utility to reset this delay.
                            """)
            );
    public static final GravPunchAttack<CMoonEntity> GRAV_PUNCH = new GravPunchAttack<CMoonEntity>(300,
            20, 32, 1f,8f, 45, 1.75f, 0.35f, -0.3f)
            .withSound(JSoundRegistry.CMOON_GRAV_PUNCH)
            .withImpactSound(JSoundRegistry.CMOON_GRAV_PUNCH_HIT)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withExtraHitBox(1d)
            .withInfo(
                    Component.literal("Only One Punch"),
                    Component.literal("inverts enemy gravity and floats on hit (3s), high stun")
            );

    public static final CGroundSlamAttack<CMoonEntity> GROUND_SLAM = new CGroundSlamAttack<CMoonEntity>(18,
            10, 18, 1f, 7f, 17, 2f, 0.2f, 0.7f)
            .withSound(JSoundRegistry.CMOON_GROUNDSLAM)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withStaticY()
            .withInfo(
                    Component.literal("Ground Slam"),
                    Component.literal("Launches downwards, combo starter/extender, launches upwards if it hits while user is crouching.")
            );
    public static final GravityShiftMove<CMoonEntity> GRAV_SHIFT = new GravityShiftMove<CMoonEntity>(1400,
            20, 32, 1f)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT)
            .withInfo(
                    Component.literal("Gravity Shift Radial"),
                    Component.literal("""
                            Repulses or attracts entities within 64 meters.
                            Lasts 10 seconds.
                            Swap between attraction/repulsion by pressing ultimate again.""")
            );
    public static final GravityShiftPulseMove<CMoonEntity> GRAV_SHIFT_PULSE = new GravityShiftPulseMove<CMoonEntity>(
            1400, 20, 32, 1f, 16)
            .withCrouchingVariant(GRAV_SHIFT)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT_DIRECTIONAL)
            .withInfo(
                    Component.literal("Gravity Shift Directional"),
                    Component.literal("""
                            Changes the gravitational direction of entities within 16m to the direction the user is looking in.
                            Lasts 30 seconds.
                            All affected entities cannot take fall damage.
                            Affected entities lose the gravity shift if they move 100m away from the user.
                            """)
            );
    public static final GravitationalHopMove<CMoonEntity> GRAVITATIONAL_HOP = new GravitationalHopMove<CMoonEntity>(280, 200, 60)
            .withInfo(
                    Component.literal("Gravitational Hop/Local Gravity Change"),
                    Component.literal("If used mid air, jumps up and grants 2s slow falling, otherwise changes your gravitational direction.")
            );

    // TODO add move info x2
    // TODO balance x2
    public static final CMoonTossMove<CMoonEntity> TOSS = new CMoonTossMove<CMoonEntity>(0, 1, 1, 0.75f)
            .withAnim(CMoonEntity.State.ITEM_TOSS);
    public static final TossChargeMove<CMoonEntity> TOSS_CHARGE = new TossChargeMove<CMoonEntity>(70, 3 * 20 + 1, 3 * 20, 1.0f, 10)
            .withFollowup(TOSS);

    private final List<Inversion> inversions = new ArrayList<>();

    public CMoonEntity(Level worldIn) {
        super(JStandTypeRegistry.C_MOON.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.4f, 1.0f, 0.6f),
                new Vector3f(1.0f, 0.4f, 0.6f),
                new Vector3f(0.4f, 0.8f, 1.0f),
                new Vector3f(1.0f, 0.2f, 0.6f)
        };
    }

    public void addInversion(LivingEntity target, int time, float damage, boolean slow) {
        inversions.add(new Inversion(time, damage, target, slow));
    }

    private static void registerMoves(MoveMap<CMoonEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, PUNCH, State.LIGHT);

        moves.register(MoveClass.HEAVY, GUT_PUNCH, State.DONUT).withCrouchingVariant(State.DIVEKICK);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, GRAV_PUNCH, State.GRAV_PUNCH);
        moves.register(MoveClass.SPECIAL2, LAUNCH, State.GROUND_SHOOT).withCrouchingVariant(State.GROUND_SHOOT_HEAVY);
        moves.register(MoveClass.SPECIAL3, GROUND_SLAM, State.GROUND_SLAM);
        moves.register(MoveClass.ULTIMATE, GRAV_SHIFT_PULSE, State.DIRECTIONAL_SHIFT).withCrouchingVariant(State.GRAV_SHIFT);

        moves.register(MoveClass.UTILITY, GRAVITATIONAL_HOP);

        moves.register(MoveClass.TOSS, TOSS_CHARGE, State.ITEM_TOSS_CHARGE).withFollowup(State.ITEM_TOSS);
    }

    @Override
    public boolean shouldOffsetHeight() {
        final var state = getState();

        if (
                state == State.GROUND_SLAM
                        || state == State.GROUND_SHOOT
                        || state == State.DIVEKICK
                        || state == State.DIVEKICK_HIT
        ) {
            return false;
        }

        return super.shouldOffsetHeight();
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case ULTIMATE -> {
                final var shiftComponent = JComponentPlatformUtils.getGravityShift(getUserOrThrow());

                if (shiftComponent.isActive()) {
                    shiftComponent.swapRadialType();
                } else {
                    return super.initMove(moveClass);
                }
                return true;
            }
            case LIGHT -> {
                if (!tryFollowUp(moveClass, MoveClass.LIGHT)) {
                    return super.initMove(moveClass);
                }
                return true;
            }
            case UTILITY -> {
                if (hasUser() && getUserOrThrow().isShiftKeyDown()) {
                    final var blocks = level().getEntitiesOfClass(BlockProjectile.class, getBoundingBox().inflate(16), p -> p.isAlive() && p.getMaster() == getUser());

                    for (BlockProjectile blockProjectile : blocks) {
                        blockProjectile.markRefresh();
                    }
                } else {
                    return super.initMove(moveClass);
                }
                return true;
            }
            default -> {
                return super.initMove(moveClass);
            }
        }
    }

    @Override
    public void standBlock() {
        final LivingEntity user = getUser();

        if (user == null) {
            return;
        }

        // Projectile deflection
        final List<Projectile> toDeflect = level().getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(0.75f), EntitySelector.ENTITY_STILL_ALIVE);

        for (Projectile projectile : toDeflect) {
            if (projectile.getOwner() == user) {
                continue;
            }
            projectile.setDeltaMovement(projectile.position().subtract(position()).normalize());
            projectile.hurtMarked = true;
        }

        JCraft.stun(user, 2, 2);
        user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 2, false, false));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;
        final LivingEntity user = getUserOrThrow();

        for (int i = 0; i < inversions.size(); i++) {
            final Inversion inversion = inversions.get(i);
            final int time = inversion.getTime();
            inversion.setTime(time - 1);

            if (time < 1) {
                final LivingEntity entity = inversion.getEntity();
                Attacks.damage(this, inversion.getDamage(), level().damageSources().mobAttack(user), entity);
                inversions.remove(i);

                if (inversion.doSlow) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, true, false));
                }
                i--;
            }
        }
    }

    @Override
    @NonNull
    public CMoonEntity getThis() {
        return this;
    }

    @Data
    private static class Inversion {
        private int time;
        private float damage;
        private LivingEntity entity;
        private boolean doSlow = false;

        private Inversion(int time, float damage, LivingEntity entity) {
            this.time = time;
            this.damage = damage;
            this.entity = entity;
        }

        private Inversion(int time, float damage, LivingEntity entity, boolean doSlow) {
            this.time = time;
            this.damage = damage;
            this.entity = entity;
            this.doSlow = doSlow;
        }
    }

    // Animation code
    public enum State implements StandAnimationState<CMoonEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cmoon.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cmoon.block", AzPlayBehaviors.LOOP)),
        DONUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.donut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.barrage", AzPlayBehaviors.LOOP)),
        GRAV_PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.gravpunch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GROUND_SLAM(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.groundslam", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GROUND_SHOOT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.groundshoot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GROUND_SHOOT_HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.groundshoot_heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAV_SHIFT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.gravshift", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DIRECTIONAL_SHIFT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.directionalshift", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        INVERSION_PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.inversionpunch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        DIVEKICK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.divekick", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DIVEKICK_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.divekick_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        ITEM_TOSS_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow", AzPlayBehaviors.PLAY_ONCE));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CMoonEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
