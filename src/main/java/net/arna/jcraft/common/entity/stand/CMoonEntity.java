package net.arna.jcraft.common.entity.stand;

import lombok.Data;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.cmoon.*;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.GravityShiftComponent;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CMoonEntity extends StandEntity<CMoonEntity, CMoonEntity.State> {
    public static final int GRAVITY_CHANGE_DURATION = 600;
    public static final SimpleAttack<CMoonEntity> INVERSION_PUNCH = SimpleAttack.<CMoonEntity>lightAttack(6, 12,
                    0.75f, 5f, 9, 0.5f, -0.1f)
            .withAnim(State.INVERSION_PUNCH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withTargetProcessor(CMoonEntity::addInversionPunchInversion)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Inversion Punch"),
                    Text.literal("very low stun, delayed slowness")
            );
    public static final SimpleAttack<CMoonEntity> LIGHT_FOLLOWUP = new SimpleAttack<CMoonEntity>(
            0, 6, 12, 0.75f, 6, 7, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<CMoonEntity> PUNCH = SimpleAttack.<CMoonEntity>lightAttack(5, 7,
                    0.75f, 5f, 10, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(INVERSION_PUNCH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter")
            );
    public static final MainBarrageAttack<CMoonEntity> BARRAGE = new MainBarrageAttack<CMoonEntity>(280, 0,
            40, 0.75f, 0.75f, 20, 2f, 0.25f, 0f, 4, Blocks.OBSIDIAN.getHardness())
            .withSound(JSoundRegistry.CMOON_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withTargetProcessor(CMoonEntity::addBarrageInversion)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, medium stun")
            );
    public static final SimpleAttack<CMoonEntity> GUT_PUNCH = new SimpleAttack<CMoonEntity>(200, 19, 30,
            1f, 8f, 10, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.CMOON_DONUT)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withExtraHitBox(0, 0.25, 1.25)
            .withInfo(
                    Text.literal("Gut Punch"),
                    Text.literal("slow, uninterruptible combo finisher")
            );
    public static final LaunchAttack LAUNCH = new LaunchAttack(260, 14, 21, 0.75f,
            5f, 19, 1.75f, 0.9f, 0.3f)
            .withSound(JSoundRegistry.CMOON_GROUNDSHOOT)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Text.literal("Block Launch"),
                    Text.literal("lifts a block from the ground and launches it at a delay/crouching and using this button resets the delay on nearby blocks")
            );
    public static final GravPunchAttack GRAV_PUNCH = new GravPunchAttack(300, 20, 32, 1f,
            8f, 45, 1.75f, 0.35f, -0.3f)
            .withSound(JSoundRegistry.CMOON_GRAV_PUNCH)
            .withImpactSound(JSoundRegistry.CMOON_GRAV_PUNCH_HIT)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withExtraHitBox(1d)
            .withInfo(
                    Text.literal("Only One Punch"),
                    Text.literal("inverts enemy gravity and floats on hit (3s), high stun")
            );
    public static final GroundSlamAttack GROUND_SLAM = new GroundSlamAttack(240, 10, 18,
            1f, 7f,  17, 3f, 0.2f, 1.4f)
            .withSound(JSoundRegistry.CMOON_GROUNDSLAM)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withStaticY()
            .withInfo(
                    Text.literal("Ground Slam"),
                    Text.literal("launches downwards, combo starter/extender, knocks down if it hits while user is crouching")
            );
    public static final GravityShiftMove GRAV_SHIFT = new GravityShiftMove(1400, 20, 32, 1f)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT)
            .withInfo(
                    Text.literal("Gravity Shift Radial"),
                    Text.literal("""
                    repulses or attracts entities within 64m
                    lasts 10 seconds
                    swap between attraction/repulsion by pressing ultimate again""")
            );
    public static final GravityShiftPulseMove GRAV_SHIFT_PULSE = new GravityShiftPulseMove(1400, 20, 32, 1f)
            .withCrouchingVariant(GRAV_SHIFT)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT_DIRECTIONAL)
            .withInfo(
                    Text.literal("Gravity Shift Directional"),
                    Text.literal("""
                    changes the gravitational direction of entities within 16m to the direction the user is looking in
                    lasts 30 seconds
                    all affected entities cannot take fall damage
                    affected entities lose the gravity shift if they move 100m away from the user
                    """)
            );
    public static final GravitationalHopMove GRAVITATIONAL_HOP = new GravitationalHopMove(340)
            .withInfo(
                    Text.literal("Gravitational Hop/Local Gravity Change"),
                    Text.literal("if used mid air, jumps up and grants 2s slow falling/otherwise changes your gravitational direction")
            );
    private final List<Inversion> inversions = new ArrayList<>();

    public CMoonEntity(World worldIn) {
        super(StandType.C_MOON, worldIn, JSoundRegistry.CMOON_SUMMON);
        idleRotation = 220f;

        pros = List.of(
                "fast m1",
                "very multipurpose",
                "damaging aftereffect",
                "good pressure"
        );

        cons = List.of(
                "execution intensive",
                "lacking in controlled horizontal movement"
        );

        freespace = """
                Passive: Inversion, all physical hits deal an extra half heart after 2s

                    BNBs:
                    -going up?
                    M1>Barrage>jump>Block Launch>M1>Only One Punch>Block Launch (Projectile Hit)>...
                        ...Grav. Hop>Ground Slam
                        ...Gut Punch""";

        auraColors = new Vector3f[]{
                new Vector3f(0.4f, 1.0f, 0.6f),
                new Vector3f(1.0f, 0.4f, 0.6f),
                new Vector3f(0.4f, 0.8f, 1.0f),
                new Vector3f(1.0f, 0.2f, 0.6f)
        };
    }

    private static void addInversion(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource, boolean blocking) {
        attacker.inversions.add(new Inversion(40, 0.5f, target));
    }

    private static void addBarrageInversion(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource, boolean blocking) {
        attacker.inversions.add(new Inversion(40, 0.25f, target));
    }

    private static void addInversionPunchInversion(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource, boolean blocking) {
        attacker.inversions.add(new Inversion(70, 0.5f, target, true));
    }

    @Override
    protected void registerMoves(MoveMap<CMoonEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, PUNCH, State.LIGHT);

        moves.register(MoveType.HEAVY, GUT_PUNCH, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, GRAV_PUNCH, State.GRAV_PUNCH);
        moves.register(MoveType.SPECIAL2, LAUNCH, State.GROUND_SHOOT);
        moves.register(MoveType.SPECIAL3, GROUND_SLAM, State.GROUND_SLAM);
        moves.register(MoveType.ULTIMATE, GRAV_SHIFT_PULSE, State.DIRECTIONAL_SHIFT).withCrouchingVariant(State.GRAV_SHIFT);

        moves.register(MoveType.UTILITY, GRAVITATIONAL_HOP);
    }

    @Override
    public boolean shouldOffsetHeight() {
        // Ground slam forces no height offset
        if (curMove != null && curMove.getMoveType() == MoveType.SPECIAL3)
            return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public boolean initMove(MoveType type) {
        switch (type) {
            case SPECIAL2 -> {
                if (hasUser() && getUserOrThrow().isSneaking()) getWorld().getEntitiesByClass(BlockProjectile.class,
                                getBoundingBox().expand(16), p -> p.isAlive() && p.getMaster() == getUser())
                        .forEach(BlockProjectile::markRefresh);
                else return super.initMove(type);
                return true;
            }
            case ULTIMATE -> {
                GravityShiftComponent shiftComponent = JComponents.getGravityShift(getUserOrThrow());
                if (shiftComponent.isActive())
                    shiftComponent.swapRadialType();
                else return super.initMove(type);
                return true;
            }
            case LIGHT -> {
                if (curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
                    AbstractMove<?, ? super CMoonEntity> followup = curMove.getFollowup();
                    if (followup != null) setMove(followup, (State) followup.getAnimation());
                } else return super.initMove(type);
                return true;
            }
            default -> {
                return super.initMove(type);
            }
        }
    }

    @Override
    public void standBlock() {
        LivingEntity user = getUser();
        if (user == null) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = getWorld().getEntitiesByClass(ProjectileEntity.class, getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == user) continue;
            projectile.setVelocity(projectile.getPos().subtract(getPos()).normalize());
            projectile.velocityModified = true;
        }

        stun(user, 2, 2);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 2, false, false));
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();

        if (getWorld().isClient) return;

        for (int i = 0; i < inversions.size(); i++) {
            Inversion inversion = inversions.get(i);
            int time = inversion.getTime();
            inversion.setTime(time - 1);

            if (time < 1) {
                LivingEntity entity = inversion.getEntity();
                damage(inversion.getDamage(), getWorld().getDamageSources().mobAttack(user), entity);
                inversions.remove(i);

                if (inversion.doSlow)
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, true, false));
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
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.cmoon.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.cmoon.block"))),
        DONUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.donut"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.cmoon.barrage"))),
        GRAV_PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.gravpunch"))),
        GROUND_SLAM(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.groundslam"))),
        GROUND_SHOOT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.groundshoot"))),
        GRAV_SHIFT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.gravshift"))),
        DIRECTIONAL_SHIFT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.directionalshift"))),
        INVERSION_PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.inversionpunch"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.cmoon.light_followup")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CMoonEntity attacker, AnimationState state) {
            animator.accept(state);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.cmoon.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
