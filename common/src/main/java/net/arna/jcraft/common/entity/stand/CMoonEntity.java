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
import net.arna.jcraft.api.component.living.CommonGravityShiftComponent;
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
    public static final MoveSet<CMoonEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.C_MOON, CMoonEntity::registerMoves, State.class);
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
                    Component.literal("very low stun, delayed slowness")
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
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<CMoonEntity> PUNCH = SimpleAttack.<CMoonEntity>lightAttack(
            5, 7, 0.75f, 5f, 10, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(INVERSION_PUNCH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final MainBarrageAttack<CMoonEntity> BARRAGE = new MainBarrageAttack<CMoonEntity>(280,
            0, 40, 0.75f, 0.75f, 20, 2f, 0.25f, 0f, 4, Blocks.OBSIDIAN.defaultDestroyTime())
            .withSound(JSoundRegistry.CMOON_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withAction(CMoonInversionAction.addInversion(40, 0.25f, false))
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, medium stun")
            );
    public static final SimpleAttack<CMoonEntity> GUT_PUNCH = new SimpleAttack<CMoonEntity>(30,
            19, 30,1f, 8f, 10, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.CMOON_DONUT)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withExtraHitBox(0, 0.25, 1.25)
            .withInfo(
                    Component.literal("Gut Punch"),
                    Component.literal("slow, uninterruptible combo finisher")
            );
    public static final LaunchAttack LAUNCH = new LaunchAttack(60,
            14, 21, 0.75f,5f, 19, 1.75f, 0.9f, 0.3f)
            .withSound(JSoundRegistry.CMOON_GROUNDSHOOT)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Component.literal("Block Launch"),
                    Component.literal("lifts a block from the ground and launches it at a delay/crouching and using this button resets the delay on nearby blocks")
            );
    public static final GravPunchAttack GRAV_PUNCH = new GravPunchAttack(300,
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
    public static final CGroundSlamAttack GROUND_SLAM = new CGroundSlamAttack(18,
            10, 18, 1f, 7f, 17, 3f, 0.2f, 1.4f)
            .withSound(JSoundRegistry.CMOON_GROUNDSLAM)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withAction(CMoonInversionAction.addInversion(40, 0.5f, false))
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withStaticY()
            .withInfo(
                    Component.literal("Ground Slam"),
                    Component.literal("launches downwards, combo starter/extender, knocks down if it hits while user is crouching")
            );
    public static final GravityShiftMove GRAV_SHIFT = new GravityShiftMove(1400, 20, 32, 1f)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT)
            .withInfo(
                    Component.literal("Gravity Shift Radial"),
                    Component.literal("""
                            repulses or attracts entities within 64m
                            lasts 10 seconds
                            swap between attraction/repulsion by pressing ultimate again""")
            );
    public static final GravityShiftPulseMove GRAV_SHIFT_PULSE = new GravityShiftPulseMove(1400, 20, 32, 1f, 16)
            .withCrouchingVariant(GRAV_SHIFT)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT_DIRECTIONAL)
            .withInfo(
                    Component.literal("Gravity Shift Directional"),
                    Component.literal("""
                            changes the gravitational direction of entities within 16m to the direction the user is looking in
                            lasts 30 seconds
                            all affected entities cannot take fall damage
                            affected entities lose the gravity shift if they move 100m away from the user
                            """)
            );
    public static final GravitationalHopMove GRAVITATIONAL_HOP = new GravitationalHopMove(340, 200, 60)
            .withInfo(
                    Component.literal("Gravitational Hop/Local Gravity Change"),
                    Component.literal("if used mid air, jumps up and grants 2s slow falling/otherwise changes your gravitational direction")
            );
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

        moves.register(MoveClass.HEAVY, GUT_PUNCH, State.DONUT);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, GRAV_PUNCH, State.GRAV_PUNCH);
        moves.register(MoveClass.SPECIAL2, LAUNCH, State.GROUND_SHOOT);
        moves.register(MoveClass.SPECIAL3, GROUND_SLAM, State.GROUND_SLAM);
        moves.register(MoveClass.ULTIMATE, GRAV_SHIFT_PULSE, State.DIRECTIONAL_SHIFT).withCrouchingVariant(State.GRAV_SHIFT);

        moves.register(MoveClass.UTILITY, GRAVITATIONAL_HOP);
    }

    @Override
    public boolean shouldOffsetHeight() {
        // Ground slam forces no height offset
        State state = getState();
        if (state == State.GROUND_SLAM || state == State.GROUND_SHOOT) {
            return false;
        }
        return super.shouldOffsetHeight();
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case SPECIAL2 -> {
                if (hasUser() && getUserOrThrow().isShiftKeyDown()) {
                    level().getEntitiesOfClass(BlockProjectile.class,
                                    getBoundingBox().inflate(16), p -> p.isAlive() && p.getMaster() == getUser())
                            .forEach(BlockProjectile::markRefresh);
                } else {
                    return super.initMove(moveClass);
                }
                return true;
            }
            case ULTIMATE -> {
                final CommonGravityShiftComponent shiftComponent = JComponentPlatformUtils.getGravityShift(getUserOrThrow());
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
        GRAV_SHIFT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.gravshift", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DIRECTIONAL_SHIFT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.directionalshift", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        INVERSION_PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.inversionpunch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cmoon.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

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
