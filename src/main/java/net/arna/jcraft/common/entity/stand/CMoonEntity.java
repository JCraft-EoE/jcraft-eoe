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
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//todo: 3d, rotatable shockwave particle effect
//todo: particles on gravpunch and both slams
public class CMoonEntity extends StandEntity<CMoonEntity, CMoonEntity.State> {
    public static final int GRAVITY_CHANGE_DURATION = 600;
    public static final SimpleAttack<CMoonEntity> INVERSION_PUNCH = SimpleAttack.<CMoonEntity>lightAttack(6, 12,
            5f, 9, 0.75f, 0.75f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withTargetProcessor(CMoonEntity::addInversionPunchInversion)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(Text.literal("Inversion Punch"), Text.literal("very low stun, delayed slowness"));
    public static final SimpleAttack<CMoonEntity> LIGHT_FOLLOWUP = new SimpleAttack<CMoonEntity>(
            0, 6, 12, 0.75f, 6, 7, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo finisher"));
    public static final SimpleAttack<CMoonEntity> PUNCH = SimpleAttack.<CMoonEntity>lightAttack(5, 7,
            5f, 10, 0.75f, 0.75f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(INVERSION_PUNCH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final MainBarrageAttack<CMoonEntity> BARRAGE = new MainBarrageAttack<CMoonEntity>(280, 0, 50,
            0.75f, 0.75f, 20, 2f, 0.25f, 0f, 4, Blocks.OBSIDIAN.getHardness())
            .withSound(JSoundRegistry.CMOON_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withTargetProcessor(CMoonEntity::addBarrageInversion)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, medium stun"));
    public static final SimpleAttack<CMoonEntity> GUT_PUNCH = new SimpleAttack<CMoonEntity>(200, 19, 30,
            1f, 8f, 10, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.CMOON_DONUT)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withLaunch()
            .withExtraHitBox(0, 0.25, 1.25)
            .withInfo(Text.literal("Gut Punch"), Text.literal("slow, uninterruptible combo finisher"));
    public static final LaunchAttack LAUNCH = new LaunchAttack(260, 14, 21, 0.75f,
            5f, 19, 1.75f, 0.9f, 0.3f)
            .withSound(JSoundRegistry.CMOON_GROUNDSHOOT)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withInfo(Text.literal("Block Launch"), Text.literal("lifts a block from the ground and launches it at a delay/crouching and using this button resets the delay on nearby blocks"));
    public static final GravPunchAttack GRAV_PUNCH = new GravPunchAttack(300, 20, 32, 1f,
            8f, 45, 1.75f, 0.35f, -0.3f)
            .withSound(JSoundRegistry.CMOON_GRAV_PUNCH)
            .withImpactSound(JSoundRegistry.CMOON_GRAV_PUNCH_HIT)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withExtraHitBox(1d)
            .withInfo(Text.literal("Only One Punch"), Text.literal("inverts enemy gravity and floats on hit (3s), high stun"));
    public static final GroundSlamAttack GROUND_SLAM = new GroundSlamAttack(240, 10, 18,
            1f, 7f,  17, 3f, 0.2f, 1.4f)
            .withSound(JSoundRegistry.CMOON_GROUNDSLAM)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withTargetProcessor(CMoonEntity::addInversion)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Ground Slam"), Text.literal("launches downwards, combo starter/extender, knocks down if it hits while user is crouching"));
    public static final GravityShiftMove GRAV_SHIFT = new GravityShiftMove(1400, 20, 32, 1f)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT)
            .withInfo(Text.literal("Gravity Shift"), Text.literal("""
                    increases user jump height, changes the gravity of everything in a 64 block radius
                    Types: REPULSE, ATTRACT, NONE
                    swap between types by pressing the key again"""));
    public static final GravityShiftPulseMove GRAV_SHIFT_PULSE = new GravityShiftPulseMove(1400, 20, 32, 1f)
            .withCrouchingVariant(GRAV_SHIFT)
            .withSound(JSoundRegistry.CMOON_GRAVSHIFT_DIRECTIONAL)
            .withInfo(Text.literal("Gravity Shift Pulse"), Text.literal("changes the gravitational direction of nearby entities " +
                    "to the direction the user is looking in"));
    public static final GravitationalHopMove GRAVITATIONAL_HOP = new GravitationalHopMove(340)
            .withInfo(Text.literal("Gravitational Hop/Local Gravity Change"),
                    Text.literal("jumps up and grants 2s slow falling/crouch to change your gravitational direction"));
    private static final TrackedData<Integer> SHIFT_TYPE;
    private static final TrackedData<Integer> SHIFT_TIME;
    private final List<Inversion> inversions = new ArrayList<>();

    static {
        SHIFT_TIME = DataTracker.registerData(CMoonEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SHIFT_TYPE = DataTracker.registerData(CMoonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

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
                    going up?
                    M1>Barrage>jump>Block Launch>M1>Only One Punch>Block Launch (Projectile Hit)>...
                        ...Grav. Hop>Ground Slam
                        ...Gut Punch""";

        auraColors = new Vec3f[]{
                new Vec3f(0.4f, 1.0f, 0.6f),
                new Vec3f(1.0f, 0.4f, 0.6f),
                new Vec3f(0.4f, 0.8f, 1.0f),
                new Vec3f(1.0f, 0.2f, 0.6f)
        };
    }

    private static void addInversion(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        attacker.inversions.add(new Inversion(40, 0.5f, target));
    }

    private static void addBarrageInversion(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        attacker.inversions.add(new Inversion(40, 0.25f, target));
    }

    private static void addInversionPunchInversion(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        attacker.inversions.add(new Inversion(70, 0.5f, target, true));
    }

    public int getShiftTime() {
        return dataTracker.get(SHIFT_TIME);
    }

    public void setShiftTime(int sTime) {
        dataTracker.set(SHIFT_TIME, sTime);
    }

    public int getShiftType() {
        return dataTracker.get(SHIFT_TYPE);
    }

    public void setShiftType(int sType) {
        dataTracker.set(SHIFT_TYPE, sType);
    }

    @Override
    protected void registerMoves(MoveMap<CMoonEntity, State> moves) {
        moves.register(MoveType.LIGHT, PUNCH, State.LIGHT).withCrouchingVariant(State.INVERSION_PUNCH);
        moves.register(MoveType.HEAVY, GUT_PUNCH, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, GRAV_PUNCH, State.GRAV_PUNCH);
        moves.register(MoveType.SPECIAL2, LAUNCH, State.GROUND_SHOOT);
        moves.register(MoveType.SPECIAL3, GROUND_SLAM, State.GROUND_SLAM);
        moves.register(MoveType.ULTIMATE, GRAV_SHIFT_PULSE, State.DIRECTIONAL_SHIFT).withCrouchingVariant(State.GRAV_SHIFT);

        moves.register(MoveType.UTILITY, GRAVITATIONAL_HOP);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(SHIFT_TIME, 0);
        getDataTracker().startTracking(SHIFT_TYPE, 0);
    }

    @Override
    public boolean shouldOffsetHeight() {
        // Ground slam forces no height offset
        if (curMove != null && curMove.getMoveType() == MoveType.SPECIAL3)
            return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public void initMove(MoveType type) {
        switch (type) {
            case SPECIAL2 -> {
                if (hasUser() && getUserOrThrow().isSneaking()) world.getEntitiesByClass(BlockProjectile.class,
                                getBoundingBox().expand(16), p -> p.isAlive() && p.getMaster() == getUser())
                        .forEach(BlockProjectile::markRefresh);
                else super.initMove(type);
            }
            case ULTIMATE -> {
                if (getShiftTime() <= 0) {
                    super.initMove(type);
                } else {
                    int shiftType = getShiftType();
                    if (++shiftType > 2)
                        shiftType = 0;
                    setShiftType(shiftType);
                }
            }
            case LIGHT -> {
                if (curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
                    AbstractMove<?, ? super CMoonEntity> followup = curMove.getFollowup();
                    if (followup != null) setMove(followup, (State) followup.getAnimation());
                } else super.initMove(type);
            }
            default -> super.initMove(type);
        }
    }

    @Override
    public void standBlock() {
        LivingEntity user = getUser();
        if (user == null) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = world.getEntitiesByClass(ProjectileEntity.class, getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

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
        Vec3d pos = getPos();
        int sTime = getShiftTime();

        if (world.isClient) {
            if (sTime > 0) {
                for (int h = 0; h < 256; ++h) {
                    Vec3d vel = Vec3d.ZERO;
                    double x = pos.x + random.nextTriangular(0, 100);
                    double y = pos.y + random.nextTriangular(0, 10);
                    double z = pos.z + random.nextTriangular(0, 100);
                    switch (getShiftType()) {
                        case (0) -> vel = new Vec3d(x, y, z).subtract(pos);
                        case (1) -> vel = pos.subtract(x, y, z);
                    }
                    world.addParticle(
                            ParticleTypes.REVERSE_PORTAL,
                            x, y, z,
                            vel.x, vel.y, vel.z);
                }
            }

            return;
        }

        for (int i = 0; i < inversions.size(); i++) {
            Inversion inversion = inversions.get(i);
            int time = inversion.getTime();
            inversion.setTime(time - 1);

            if (time < 1) {
                LivingEntity entity = inversion.getEntity();
                damage(inversion.getDamage(), DamageSource.mob(user), entity);
                inversions.remove(i);

                if (inversion.doSlow)
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, true, false));
                i--;
            }
        }

        GRAV_SHIFT_PULSE.tickGravShift(this);

        if (sTime <= 0 || user.hasStatusEffect(JStatusRegistry.DAZED)) return;
        List<Entity> toCatch = world.getEntitiesByClass(Entity.class, getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

        toCatch.remove(this);
        toCatch.remove(user);

        for (Entity entity : toCatch) {
            if (entity instanceof BlockProjectile block && block.getMaster() == user)
                continue;
            //Vec3d vel = entity.getVelocity();
            switch (getShiftType()) {
                //case (0) -> entity.addVelocity(-vel.x / 3.0, -0.1, -vel.z / 3.0);
                case (0) -> entity.setVelocity(
                        entity.getVelocity().add(entity.getPos().subtract(pos).normalize().multiply(0.1))
                );
                case (1) -> entity.setVelocity(
                        entity.getVelocity().add(pos.subtract(entity.getPos()).normalize().multiply(0.1))
                );
            }

            if (entity instanceof ServerPlayerEntity serverPlayerEntity)
                serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
            entity.velocityModified = true;
        }

        setShiftTime(sTime - 1);
    }

    @Override
    protected @NonNull CMoonEntity getThis() {
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
        IDLE(builder -> builder.loop("animation.cmoon.idle")),
        LIGHT(builder -> builder.playAndHold("animation.cmoon.light")),
        BLOCK(builder -> builder.loop("animation.cmoon.block")),
        DONUT(builder -> builder.playAndHold("animation.cmoon.donut")),
        BARRAGE(builder -> builder.loop("animation.cmoon.barrage")),
        GRAV_PUNCH(builder -> builder.playAndHold("animation.cmoon.gravpunch")),
        GROUND_SLAM(builder -> builder.playAndHold("animation.cmoon.groundslam")),
        GROUND_SHOOT(builder -> builder.playAndHold("animation.cmoon.groundshoot")),
        GRAV_SHIFT(builder -> builder.playAndHold("animation.cmoon.gravshift")),
        DIRECTIONAL_SHIFT(builder -> builder.playAndHold("animation.cmoon.directionalshift")),
        INVERSION_PUNCH(builder -> builder.playAndHold("animation.cmoon.inversionpunch")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.cmoon.light_followup"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CMoonEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
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
