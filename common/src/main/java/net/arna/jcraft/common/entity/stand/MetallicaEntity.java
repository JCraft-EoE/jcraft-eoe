package net.arna.jcraft.common.entity.stand;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.ai.AttackerBrainInfo;
import net.arna.jcraft.common.attack.actions.CancelSpecMoveAction;
import net.arna.jcraft.common.attack.actions.EffectAction;
import net.arna.jcraft.common.attack.actions.MetallicaAddIronAction;
import net.arna.jcraft.common.attack.actions.UserAnimationAction;
import net.arna.jcraft.common.attack.conditions.MetallicaIronCondition;
import net.arna.jcraft.common.attack.moves.metallica.*;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.entity.projectile.RazorProjectile;
import net.arna.jcraft.common.entity.projectile.ScalpelProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Metallica">Metallica</a>.
 * @see JStandTypeRegistry#METALLICA
 * @see net.arna.jcraft.client.renderer.entity.stands.MetallicaRenderer MetallicaRenderer
 * @see HarvestMove
 */
public class MetallicaEntity extends StandEntity<MetallicaEntity, MetallicaEntity.State> {
    public static final MoveSet<MetallicaEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.METALLICA,
            MetallicaEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleDistance(0f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.metallica"))
                    .proCount(3)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                Contains up to 80 units of iron.
                Requires iron to create objects used in attacks."""))
                    .skinName(Component.literal("Lead"))
                    .skinName(Component.literal("Brass"))
                    .skinName(Component.literal("Hollow"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.METALLICA_SUMMON))
            .build();

    public static final EntityDataAccessor<Optional<BlockPos>> SIPHON_POS = SynchedEntityData.defineId(MetallicaEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    public static final EntityDataAccessor<Float> IRON = SynchedEntityData.defineId(MetallicaEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> INVISIBLE = SynchedEntityData.defineId(MetallicaEntity.class, EntityDataSerializers.BOOLEAN);
    public static final float IRON_MAX = 80.0f;

    public static final GiveScalpelMove GIVE_SCALPEL = new GiveScalpelMove(0, 8, 9, 0)
            .withInfo(
                    Component.literal("Give Scalpel"),
                    Component.empty()
            )
            .withInitAction(UserAnimationAction.play("mtl.gsl"))
            .withCondition(MetallicaIronCondition.atLeast(ScalpelProjectile.IRON_COST));
    public static final SimpleAttack<MetallicaEntity> LIGHT_LAUNCH = new SimpleAttack<MetallicaEntity>(0,
            18, 22, 0.75f, 5f, 6,1.7f,  1.25f, 0.2f)
            .withLaunch()
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Launch"),
                    Component.empty()
            );
    public static final SimpleAttack<MetallicaEntity> LIGHT_FOLLOWUP_2 = new SimpleAttack<MetallicaEntity>(0,
                    12, 22, 0.75f, 3f, 10,1.6f,  0.25f, 0.2f)
            .withSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withAnim(State.LIGHT_FINAL)
            .withImpactSound(JSoundRegistry.IMPACT_7)
            .withFinisher(16, LIGHT_LAUNCH)
            .withInfo(
                    Component.literal("Impale"),
                    Component.literal("quick combo starter")
            );
    public static final SimpleAttack<MetallicaEntity> LIGHT_FOLLOWUP = SimpleAttack.<MetallicaEntity>lightAttack(
                    6, 15, 0.75f, 4f, 14, 0.25f, 0.2f)
            .withSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withFollowup(LIGHT_FOLLOWUP_2)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInfo(
                    Component.literal("Slice (2nd Hit)"),
                    Component.literal("quick combo starter")
            );
    public static final SimpleAttack<MetallicaEntity> LIGHT = SimpleAttack.<MetallicaEntity>lightAttack(
                    6, 10, 0.75f, 4f, 11, 0.15f, 0.2f)
            .withSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(GIVE_SCALPEL)
            // .withAerialVariant(AIR_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInfo(
                    Component.literal("Slice"),
                    Component.literal("quick combo starter")
            );
    public static final BarrageAttack<MetallicaEntity> BARRAGE = new BarrageAttack<MetallicaEntity>(240, 0,
            30, 0.75f, 0.8f, 20, 1.6f, 0.25f, 0f, 3)
            .withSound(JSoundRegistry.METALLICA_BARRAGE)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun, smaller hitbox than most barrages")
            )
            .withInitAction(UserAnimationAction.play("mtl.brg"));
    public static final KnockdownAttack<MetallicaEntity> SWEEP = new KnockdownAttack<MetallicaEntity>(0,
            7, 14, 0.75f, 5f, 8, 1.5f, 0.3f, 0.4f, 35)
            .withSound(JSoundRegistry.METALLICA_BLADE_SWIPE)
            .withImpactSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withExtraHitBox(1.75, -0.4, 0.85)
            .withStaticY()
            .withInfo(
                    Component.literal("Sweep"),
                    Component.literal("""
                            Fast 1.5s knockdown.
                            §1Requires at least 25% iron to be usable.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.swp"))
            .withCondition(MetallicaIronCondition.atLeast(IRON_MAX / 4.0f));
    public static final SimpleAttack<MetallicaEntity> CLEAVE = new SimpleAttack<MetallicaEntity>(0,
            12, 21, 1.5f, 6.5f, 11,2.5f,  2.0f, 0.2f)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withAnim(State.CLEAVE)
            .withLaunch()
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withExtraHitBox(3.0, 0.5, 1.5)
            .withInfo(
                    Component.literal("Cleave"),
                    Component.literal("""
                            Interruptible, very far-reaching followup.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.clv"))
            .withCondition(MetallicaIronCondition.atLeast(IRON_MAX / 2.0f));
    public static final SimpleUppercutAttack<MetallicaEntity> SMASH = new SimpleUppercutAttack<MetallicaEntity>(0,
            11, 21, 1.0f, 7.5f, 18,2.0f,  2.0f, 0.2f, -0.5f)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withCrouchingVariant(SWEEP)
            .withFollowup(CLEAVE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHyperArmor()
            .withExtraHitBox(2.0, 0.5, 1.5)
            .markRanged()
            .withInfo(
                    Component.literal("Smash"),
                    Component.literal("""
                            Uninterruptible combo starter.
                            Very far-reaching.
                            §1Requires at least 50% iron to be usable.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.sms"))
            .withCondition(MetallicaIronCondition.atLeast(IRON_MAX / 2.0f));
    public static final RemoteScalpelMove REMOTE_SCALPEL_MOVE = new RemoteScalpelMove(0, 7, 12, 0)
            .withInfo(
                    Component.literal("Scalpel Toss (Remote)"),
                    Component.literal("""
                                    Decently fast, very low cooldown.
                                    Creates 3 scalpels at the pointed location.
                                    They will only fire if in the presence of a magnetic field.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.rs"))
            .withCondition(MetallicaIronCondition.atLeast(ScalpelProjectile.IRON_COST));
    public static final FanTossAttack FAN_TOSS = new FanTossAttack(0, 7, 12, 0.75f)
            .withSound(JSoundRegistry.METALLICA_SCALPEL_SUMMON)
            .withInfo(
                    Component.literal("Scalpel Toss (Wide)"),
                    Component.literal("""
                                    Decently fast, very low cooldown.
                                    Fires 5 scalpels in a fan pattern.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.ft"))
            .withCondition(MetallicaIronCondition.atLeast(ScalpelProjectile.IRON_COST));
    public static final PreciseTossAttack PRECISE_TOSS = new PreciseTossAttack(0, 7, 12, 0.75f)
            .withSound(JSoundRegistry.METALLICA_SCALPEL_SUMMON)
            .withCrouchingVariant(REMOTE_SCALPEL_MOVE)
            .withAerialVariant(FAN_TOSS)
            .withInfo(
                    Component.literal("Scalpel Toss (Precise)"),
                    Component.literal("""
                                    Decently fast, very low cooldown.
                                    Fires 3 scalpels in the exact pointed direction.
                                    Scalpels disappear after 15s in the ground, and may be picked up to regain iron.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.pt"))
            .withCondition(MetallicaIronCondition.atLeast(ScalpelProjectile.IRON_COST));

    public static final ExplodeMagneticFieldMove EXPLODE_MAGNETIC_FIELD = new ExplodeMagneticFieldMove(140, 10, 20)
            .withInfo(
                    Component.literal("Explode Magnetic Field"),
                    Component.literal("""
                            Reverses the polarity of the nearest aimed magnetic field, then detonates it.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.emf"));
    public static final RazorCoughAttack RAZOR_COUGH_ATTACK = new RazorCoughAttack(140, 10, 20)
            .withInfo(
                    Component.literal("Internal Attack"),
                    Component.literal("""
                            All living entities within magnetic fields will begin to vomit razors on the ground.
                            Increases Hypoxia for all affected entities.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.rca"));
    public static final CreateMagneticFieldMove CREATE_MAGNETIC_FIELD = new CreateMagneticFieldMove(200, 5, 15)
            .withCrouchingVariant(RAZOR_COUGH_ATTACK)
            // .withAerialVariant(EXPLODE_MAGNETIC_FIELD)
            .withInfo(
                    Component.literal("Place Magnetic Field"),
                    Component.literal("""
                            12 meter range.
                            Must be pointed at a block.
                            Summons an attractive ferromagnetic field which lasts for 1 minute.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.sfk"))
            .withCondition(MetallicaIronCondition.atLeast(CreateMagneticFieldMove.IRON_COST));
    /* public static final InternalAttack INTERNAL_ATTACK = new InternalAttack(200, 10, 15)
            .withCrouchingVariant(CREATE_MAGNETIC_FIELD)
            .withInfo(
                    Component.literal("Internal Attack"),
                    Component.literal("""
                            12 meter range.
                            Uses the opponent's own iron to make them vomit razor blades, which may damage them if stepped on.
                            The razors may be collected by Metallica's user for iron.
                            Applies Hypoxia for 3 seconds.
                            Cannot attack hypoxic targets.
                            This attack does not interrupt other moves.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.ita")); */
    public static final SimpleAttack<MetallicaEntity> GRAB_HIT_FINAL = new SimpleAttack<MetallicaEntity>(0,
            18, 24, 0.5f, 4f, 9, 2f, 1.2f, 0f)
            // .withImpactSound(JSoundRegistry.IMPACT_1)
            .withAction(EffectAction.inflict(JStatusRegistry.HYPOXIA, 200, 0, false, true))
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Grab (Final Hit)"),
                    Component.empty()
            )
            .withAction(MetallicaAddIronAction.addIron(15.0f));
    public static final SimpleAttack<MetallicaEntity> GRAB_HIT = new SimpleAttack<MetallicaEntity>(0,
            13, 24, 0.5f, 4f, 10, 2f, 0f, 0f)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(
                    Component.literal("Grab (Second Hit)"),
                    Component.empty())
            .withFinisher(14, GRAB_HIT_FINAL)
            .withAction(MetallicaAddIronAction.addIron(10.0f))
            .withInitAction(UserAnimationAction.play("mtl.grab_hit").force());
    public static final GrabAttack<MetallicaEntity, State> GRAB = new GrabAttack<>(280,
            9, 20, 0.5f, 0, 15, 1.5f, 0, 0, GRAB_HIT,
            StateContainer.of(State.GRAB_HIT), 17, 0.4)
            .withInfo(
                    Component.literal("Grab"),
                    Component.literal("""
                            Unblockable, inflicts Hypoxia (10s).
                            Restores 30 iron.
                            Cannot be used alongside spec moves and will override them.""")
            )
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInitAction(CancelSpecMoveAction.cancelSpecMove())
            .withInitAction(UserAnimationAction.play("mtl.grab"));
    public static final InvisibilityMove GO_INVISIBLE = new InvisibilityMove(20, 10, 15)
            .withSound(JSoundRegistry.METALLICA_INVISIBILITY)
            .withInfo(
                    Component.literal("Invisibility"),
                    Component.literal("""
                            Projects a field of iron particles that reflect light away from the user.
                            Uses 5 iron per second.
                            Cannot be queued.""")
            )
            .withInitAction(UserAnimationAction.play("mtl.ivs"))
            .withCondition(MetallicaIronCondition.atLeast(5.0f));
    public static final HarvestMove HARVEST = new HarvestMove(60 * 20, 0.75f, 3)
            .withCrouchingVariant(GO_INVISIBLE)
            .withInfo(
                    Component.literal("Harvest Iron"),
                    Component.literal("""
                            Harvests 1 iron with a 0.15s interval from the looked at block.
                            3 times faster if harvesting from an iron block.
                            5m max range.
                            Cannot be queued.""")
            );
    public static final BisectAttack BISECT = new BisectAttack(0, 1, 11, 0.75f)
            .withInitAction(UserAnimationAction.play("mtl.bsc_fire"));
    public static final BisectChargeMove BISECT_CHARGE = new BisectChargeMove(30 * 20, 81, 80, 0.75f, 12)
            .withInfo(
                    Component.literal("Bisect"),
                    Component.literal("""
                            Chargeable projectile that consumes iron over time to become larger and more powerful.
                            Unblockable.""")
            )
            .withFollowup(BISECT)
            .withInitAction(UserAnimationAction.play("mtl.bsc"));
    private static final BlockParticleOption FAKE_BLOOD = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_WIRE.defaultBlockState());

    @Getter
    @Setter
    private int bisectChargeTime = 0;
    private CommonMiscComponent miscComponent;

    public MetallicaEntity(Level worldIn) {
        super(JStandTypeRegistry.METALLICA.get(), worldIn);

        auraColors = new Vector3f[] {
                new Vector3f(0.1f, 0.1f, 0.4f),
                new Vector3f(0.2f, 0.1f, 0.3f),
                new Vector3f(0.2f, 0.2f, 0.05f),
                new Vector3f(0.3f, 0.01f, 0.1f),
        };
    }

    @Override
    protected @NonNull AABB makeBoundingBox() {
        return AABB.ofSize(getPosition(0f).add(0,0.5,0),0.5,1,0.5);
    }

    @Override
    public Vector3f getAuraColor() {
        if (isInvisible() && (isIdle() || blocking)) return new Vector3f(0.0f, 0.0f, 0.0f);
        return super.getAuraColor();
    }

    public Vector3f getMoshColor() {
        if (getSkin() == 0) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        return getAuraColor();
    }

    @Override
    public void setUser(@Nullable LivingEntity user) {
        super.setUser(user);
        if (user == null) return;
        miscComponent = JComponentPlatformUtils.getMiscData(getUser());
        if (miscComponent == null) return;
        setIron(miscComponent.getMetallicaIron());
    }

    private boolean shouldThrowScalpels() {
        final float iron = getIron();
        if (iron < ScalpelProjectile.IRON_COST) return false;
        final float choice = random.nextFloat();
        // per actionable tick
        return choice <= 0.05 * (iron / IRON_MAX);
    }

    @Override
    public MoveSelectionResult overrideMoveExecution(AbstractMove<?, ? super MetallicaEntity> selectedAttack, AttackerBrainInfo info, Mob mob,
                                                     LivingEntity target, JumpControl mobJumpControl, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack,
                                                     double distance, int enemyMoveStun, int stunTicks) {
        if (
                (selectedAttack instanceof PreciseTossAttack precise) ||
                (selectedAttack instanceof FanTossAttack fan) ||
                (selectedAttack instanceof RemoteScalpelMove remote)
        ) {
            if (!shouldThrowScalpels()) return MoveSelectionResult.STOP;
        }

        return MoveSelectionResult.PASS;
    }

    private static void registerMoves(MoveMap<MetallicaEntity, MetallicaEntity.State> moves) {
        var light = moves.register(MoveClass.LIGHT, LIGHT, State.LIGHT);
        light.withFollowup(State.LIGHT_FOLLOWUP).withFollowup(State.LIGHT_FINAL);
        light.withCrouchingVariant(State.GIVE_SCALPEL);

        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        var heavy = moves.register(MoveClass.HEAVY, SMASH, State.SMASH);
        heavy.withFollowup(State.CLEAVE);
        heavy.withCrouchingVariant(State.SWEEP);

        var sp1 = moves.register(MoveClass.SPECIAL1, PRECISE_TOSS, State.PRECISE_TOSS);
        sp1.withCrouchingVariant(State.NONE);
        sp1.withAerialVariant(State.FAN_TOSS);

        moves.register(MoveClass.SPECIAL2, CREATE_MAGNETIC_FIELD, State.NONE).withCrouchingVariant(State.NONE);
        moves.register(MoveClass.SPECIAL3, GRAB, State.NONE);

        moves.register(MoveClass.ULTIMATE, BISECT_CHARGE, State.BISECT).withFollowup(State.NONE);

        moves.register(MoveClass.UTILITY, HARVEST, State.HARVEST).withCrouchingVariant(State.NONE);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        if (tryFollowUp(moveClass, MoveClass.HEAVY)) return true;
        if (moveClass == MoveClass.SPECIAL2 && canAttack()) {
            if (getUserOrThrow().isShiftKeyDown()) {
                setMove(RAZOR_COUGH_ATTACK, State.NONE);
                return true;
            }
        }
        return super.initMove(moveClass);
    }

    @Override
    public void queueMove(MoveInputType type) {
        if (type == MoveInputType.UTILITY) return;
        super.queueMove(type);
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.SWEEP) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SIPHON_POS, Optional.empty());
        entityData.define(IRON, IRON_MAX);
        entityData.define(INVISIBLE, false);
    }

    public float getIron() {
        return entityData.get(IRON);
    }

    public void setIron(float iron) {
        entityData.set(IRON, iron);
        if (miscComponent == null) return;
        miscComponent.setMetallicaIron(iron);
    }

    public void addIron(float add) {
        setIron(Mth.clamp(entityData.get(IRON) + add, 0f, IRON_MAX));
    }

    public boolean drainIron(float r) {
        float iron = getIron();
        if (iron < r) return false;
        setIron(iron - r);
        return true;
    }

    private final Set<AbstractArrow> pickingUp = new HashSet<>(12);

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (isInvisible() && !isIdle() && !isBlocking()) {
                JCraft.getClientEntityHandler().displayMetallicaAura(this);
            }

            if (getState() == State.GRAB_HIT) {
                final Vec3 toUser = getUserOrThrow().position().subtract(position()).normalize().scale(0.5);
                final Vec3 midVec = GravityChangerAPI.getEyeOffset(this).add(position());
                for (int i = 0; i < 3; i++) {
                    level().addParticle(random.nextBoolean() ? ParticleTypes.ELECTRIC_SPARK : FAKE_BLOOD,
                            midVec.x + random.nextGaussian() * 0.2 - 0.1,
                            midVec.y + random.nextGaussian() * 0.2 - 0.1,
                            midVec.z + random.nextGaussian() * 0.2 - 0.1,
                            toUser.x, toUser.y, toUser.z
                    );
                }
            }
        } else {
            // Allow Metallica-using mobs to pick up metal projectiles
            if (getUser() instanceof Mob mob) {
                if (getIron() >= MetallicaEntity.IRON_MAX) return;

                final AABB aabb = mob.getBoundingBox();
                final List<AbstractArrow> projectiles = level().getEntitiesOfClass(AbstractArrow.class, aabb.inflate(1.0));

                for (AbstractArrow proj : projectiles) {
                    if (pickingUp.contains(proj)) continue;
                    if (!proj.inGround) continue;

                    if ( (proj instanceof ScalpelProjectile scalpelProjectile) || (proj instanceof RazorProjectile razor) ) {
                        proj.setDeltaMovement(Vec3.ZERO);
                        proj.setNoPhysics(true);
                        proj.setNoGravity(true);
                        proj.inGround = false;
                        pickingUp.add(proj);
                    }
                }

                pickingUp.removeIf(a -> !a.isAlive());

                if (pickingUp.isEmpty()) return;

                final Vec3 target = mob.position();
                boolean clearAll = false;

                for (AbstractArrow proj : pickingUp) {
                    if (!proj.isAlive()) continue;

                    final Vec3 toward = target.subtract(proj.position()).normalize().scale(0.02);
                    proj.push(toward.x, toward.y, toward.z);

                    if (proj.distanceToSqr(mob) <= aabb.getSize() * aabb.getSize()) {
                        ironProjectilePickup(mob, 5.0f);
                        proj.kill();
                        if (getIron() >= MetallicaEntity.IRON_MAX) {
                            clearAll = true;
                            break;
                        }
                    }
                }

                if (clearAll) {
                    for (AbstractArrow proj : pickingUp) {
                        proj.setNoPhysics(false);
                        proj.setNoGravity(false);
                    }

                    pickingUp.clear();
                }
            }
        }
    }

    public static boolean ironProjectilePickup(@NonNull LivingEntity grabber, final float ironCost) {
        if (JComponentPlatformUtils.getStandComponent(grabber).getStand() instanceof MetallicaEntity metallica) {
            if (metallica.getIron() < MetallicaEntity.IRON_MAX) {
                metallica.addIron(ironCost);
                return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable Mob standUserPassiveAI() {
        final Mob mob = super.standUserPassiveAI();
        if (mob != null && hasUser()) {
            if (getIron() < IRON_MAX) {
                Objects.requireNonNull(getUser()).setShiftKeyDown(false);
                initMove(MoveClass.UTILITY);
            }
        }
        return mob;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return !damageSource.is(DamageTypes.GENERIC_KILL) && !damageSource.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    @Override
    public boolean isInvisible() {
        return entityData.get(INVISIBLE);
    }

    @Override
    public @NonNull MetallicaEntity getThis() {
        return this;
    }

    public Optional<BlockPos> getSiphonPos() {
        return entityData.get(SIPHON_POS);
    }

    // Animations
    public enum State implements StandAnimationState<MetallicaEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.metallica.idle", AzPlayBehaviors.LOOP)),
        NONE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.metallica.idle", AzPlayBehaviors.LOOP)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.metallica.block", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.light2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FINAL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.light3", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PRECISE_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.precise_toss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        FAN_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.fan_toss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HARVEST(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.harvest", AzPlayBehaviors.LOOP)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.barrage", AzPlayBehaviors.LOOP)),
        SMASH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.smash", AzPlayBehaviors.LOOP)),
        CLEAVE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.cleave", AzPlayBehaviors.LOOP)),
        SWEEP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.sweep", AzPlayBehaviors.LOOP)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.grab_hit", AzPlayBehaviors.LOOP)),
        BISECT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.bisect", AzPlayBehaviors.LOOP)),
        GIVE_SCALPEL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.metallica.give_scalpel", AzPlayBehaviors.LOOP)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MetallicaEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected MetallicaEntity.State[] getStateValues() {
        return MetallicaEntity.State.values();
    }

    @Override
    public MetallicaEntity.State getBlockState() {
        return MetallicaEntity.State.BLOCK;
    }
}
