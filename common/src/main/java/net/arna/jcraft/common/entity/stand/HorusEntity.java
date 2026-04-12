package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.PlaySoundAction;
import net.arna.jcraft.common.attack.moves.horus.*;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleHoldableMove;
import net.arna.jcraft.common.entity.projectile.LargeIcicleProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Horus">Horus</a>.
 * @see JStandTypeRegistry#HORUS
 * @see net.arna.jcraft.client.renderer.entity.stands.HorusRenderer HorusRenderer
 * @see net.arna.jcraft.common.entity.npc.PetshopEntity PetshopEntity
 * @see HorusBarrageAttack
 * @see HorusDivekickAttack
 */
public class HorusEntity extends StandEntity<HorusEntity, HorusEntity.State> {
    public static final MoveSet<HorusEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.HORUS,
            HorusEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.horus"))
                    .proCount(2)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                BNBs:
                    -bad birdie
                    Light~Light>dash>crouch.Light
                
                """))
                    .skinName(Component.literal("Pearl"))
                    .skinName(Component.literal("Dual"))
                    .skinName(Component.literal("Evil Incarnation"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.HORUS_SUMMON))
            .build();

    public static final SimpleAttack<HorusEntity> LIGHT_CROUCHING_FOLLOWUP = new SimpleAttack<HorusEntity>(0,
            15, 25, 0.75f, 7f, 25, 1.85f, 1.5f, 0.2f)
            .withAnim(State.IMPALE)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withBlockStun(25)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Impale"),
                    Component.literal("slow reset tool, high stun and blockstun")
            );
    public static final SimpleAttack<HorusEntity> LIGHT_FOLLOWUP = new SimpleAttack<HorusEntity>(0,
            9, 13, 0.75f, 6f, 10, 1.5f, 1f, 0.2f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withCrouchingVariant(LIGHT_CROUCHING_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Finisher"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<HorusEntity> LIGHT_LOW = new SimpleAttack<HorusEntity>(9,
                    5, 9, 0.95f, 4f, 8, 1.25f, 0.25f, 0.5f)
            //.withFollowup(LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Component.literal("Low Claw"),
                    Component.literal("faster and further hitting than standing, but doesn't combo into anything")
            );
    public static final SimpleAttack<HorusEntity> LIGHT_AIR = new SimpleAttack<HorusEntity>(11,
                    6, 11, 0.75f, 5f, 12, 1.5f, 0.25f, 0.5f)
            //.withFollowup(LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInfo(
                    Component.literal("Downward Claw"),
                    Component.literal("quick combo starter, meant for air-to-ground")
            );
    public static final SimpleAttack<HorusEntity> LIGHT = SimpleAttack.<HorusEntity>lightAttack(
                    6, 11, 0.75f, 5f, 12, 0.2f, 0f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LIGHT_LOW)
            .withAerialVariant(LIGHT_AIR)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Slash"),
                    Component.literal("quick combo starter, has a standing and crouching followup")
            );
    public static final HorusBarrageAttack BARRAGE = new HorusBarrageAttack(240,
            5, 80,0.75f, 0, 0, 0, 0, 0, 5)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("4s max duration, can be held")
            )
            .withAction(PlaySoundAction.playSound(JSoundRegistry.HORUS_BARRAGE_FIRE));
    public static final HorusDetonateAttack DETONATE = new HorusDetonateAttack(0, 10, 12, 0.75f)
            .withAnim(State.DETONATE)
            .withInfo(
                    Component.literal("Detonate"),
                    Component.empty()
            );
    public static final StompAttack STOMP = new StompAttack(22, 11, 22, 0.75f,
            9f, 12, 1.3f, 0.6f, 0.4f)
            .withFollowup(DETONATE)
            .withSound(JSoundRegistry.HORUS_STOMP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Stomp"),
                    Component.literal("summons a large icicle, press Heavy again to detonate it")
            );
    // Utility
    public static final HorusDivekickAttack DIVEKICK = new HorusDivekickAttack(240,
            8, 25, 8, 6f, 19, 1.5f, 0.23f, 0.3f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Beak Dive"),
                    Component.literal("""
                            Lasts longer the lower you aim while starting the move.
                            Stalls the user in the air when starting.
                            Removes fall damage.""")
            );
    // Special 1
    public static final IceLanceAttack LANCE = new IceLanceAttack(80, 18, 24, 0.75f)
            .withAnim(State.LANCE)
            .withInfo(
                    Component.literal("Ice Lance"),
                    Component.literal("""
                            Also slow, slightly higher cooldown.
                            Fires a large icicle that detonates after 2s.""")
            )
            .withSound(JSoundRegistry.HORUS_LANCE_CHARGE);
    public static final ScatterAttack SCATTER = new ScatterAttack(60, 16, 20, 0.75f)
            .withCrouchingVariant(LANCE)
            .withInfo(
                    Component.literal("Scatter"),
                    Component.literal("""
                                    Relatively slow, very low cooldown.
                                    Fires 6 icicles that bounce off walls.""")
            )
            .withSound(JSoundRegistry.HORUS_SCATTER);
    // Special 2
    public static final IcicleFireAttack CHARGE_FIRE = new IcicleFireAttack(0, 1, 11, 0.75f)
            .withInfo(
                    Component.literal("Icicle Fire"),
                    Component.empty()
            );
    public static final SimpleHoldableMove<HorusEntity> CHARGE_ICICLE = new SimpleHoldableMove<HorusEntity>(
            0, IcicleFireAttack.MAX_ICICLE_CHARGE_TIME + 1, IcicleFireAttack.MAX_ICICLE_CHARGE_TIME, 0.75f, 9)
            .withFollowup(CHARGE_FIRE)
            .withArmor(3)
            .withInfo(
                    Component.literal("Icicle Charge"),
                    Component.literal("""
                            3 armor points while charging
                            Can be held, and released 0.45s in.
                            If charged fully, attack becomes unblockable and launches far."""
                    ));
    // Special 3
    public static final ChasingFreezeAttack PLACE = new ChasingFreezeAttack(200, 8, 14, 0.75f)
            .withInfo(
                    Component.literal("Chasing Freeze"),
                    Component.empty()
            )
            .withSound(JSoundRegistry.HORUS_PlACE_CREEPING_ICE);
    public static final PerfectFreezeAttack PERFECT_FREEZE = new PerfectFreezeAttack(50 * 20, 14, 30,
            0f, 4f, 10, 2.5f, 0.3f, 0)
            .withInfo(
                    Component.literal("Perfect Freeze"),
                    Component.literal("""
                            freezes all nearby enemies
                            summons 3 ice branches to chase opponents
                            stops all nearby projectiles""")
            )
            .withSound(JSoundRegistry.HORUS_PlACE_CREEPING_ICE);

    private WeakReference<LargeIcicleProjectile> lastLargeIcicle = new WeakReference<>(null);

    public HorusEntity(Level world) {
        super(JStandTypeRegistry.HORUS.get(), world);

        auraColors = new Vector3f[]{
                new Vector3f(0.2f, 0.5f, 0.8f),
                new Vector3f(0.3f, 0.6f, 1.0f),
                new Vector3f(1.0f, 0.3f, 0.7f),
                new Vector3f(1.0f, 0.0f, 0.0f)
        };
    }

    public LargeIcicleProjectile getLastLargeIcicle() {
        return lastLargeIcicle.get();
    }

    public void setLastLargeIcicle(LargeIcicleProjectile icicle) {
        lastLargeIcicle = new WeakReference<>(icicle);
    }

    private static void registerMoves(MoveMap<HorusEntity, HorusEntity.State> moves) {
        MoveMap.Entry<HorusEntity, State> light = moves.register(MoveClass.LIGHT, LIGHT, State.LIGHT);
        light.withFollowup(State.LIGHT_FOLLOWUP).withCrouchingVariant(State.IMPALE);
        light.withCrouchingVariant(State.LIGHT_LOW);
        light.withAerialVariant(State.LIGHT_AIR);

        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);
        moves.register(MoveClass.HEAVY, STOMP, State.STOMP).withFollowup(State.DETONATE);

        moves.registerImmediate(MoveClass.SPECIAL1, SCATTER, State.SCATTER);
        moves.register(MoveClass.SPECIAL2, CHARGE_ICICLE, State.CHARGE_ICICLE).withFollowup(State.CHARGE_FIRE);
        moves.register(MoveClass.SPECIAL3, PLACE, State.PLACE);

        moves.register(MoveClass.ULTIMATE, PERFECT_FREEZE, State.ULTIMATE);

        moves.register(MoveClass.UTILITY, DIVEKICK, State.DIVEKICK);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.HEAVY)) {
            return true;
        } else if (moveClass == MoveClass.LIGHT && getCurrentMove() != null && getCurrentMove().getMoveClass() == MoveClass.LIGHT &&
                getMoveStun() < getCurrentMove().getWindupPoint()) {
            AbstractMove<?, ? super HorusEntity> followup = getCurrentMove().getFollowup();
            if (followup != null) {
                if (getUserOrThrow().isDiscrete()) followup = followup.getCrouchingVariant();
                if (followup != null) {
                    setMove(followup, (State) Objects.requireNonNull(followup).getAnimation());
                }
            }
            return true;
        }

        return super.initMove(moveClass);
    }

    @Override
    public void tick() {
        super.tick();
        final int moveStun = getMoveStun();
        final LivingEntity user = JUtils.getUserIfStand(this);
        if (user != null) {
            FrostWalkerEnchantment.onEntityMoved(user, level(), this.getOnPos().above(), 2);
        }
        if (moveStun > IcicleFireAttack.MAX_ICICLE_CHARGE_TIME + 1 || !level().isClientSide()) return;

        if (getState() == State.CHARGE_ICICLE) {
            double completion = moveStun / (IcicleFireAttack.MAX_ICICLE_CHARGE_TIME + 1.0);
            Vec3 direction = getLookAngle().add(GravityChangerAPI.getEyeOffset(this).scale(0.75));
            if (random.nextDouble() >= completion) { // More often the more complete
                Vec3 offset = new Vec3( // Closer in the more complete
                        random.nextGaussian() * completion,
                        random.nextGaussian() * completion,
                        random.nextGaussian() * completion
                );
                level().addParticle(ParticleTypes.SNOWFLAKE,
                        getX() + direction.x + offset.x,
                        getY() + direction.y + offset.y,
                        getZ() + direction.z + offset.z,
                        -offset.x / 6,
                        -offset.y / 6,
                        -offset.z / 6
                );
            }
            level().addParticle(random.nextBoolean() ? ParticleTypes.SPIT : LargeIcicleProjectile.ICE_PARTICLE,
                    getX() + direction.x,
                    getY() + direction.y,
                    getZ() + direction.z,
                    0, 0, 0
            );
        } else if (getState() == State.ULTIMATE && getMoveStun() == PERFECT_FREEZE.getWindupPoint()) {
            for (int i = 0; i < 64; i++) {
                level().addParticle(ParticleTypes.SNOWFLAKE,
                        getX() + random.nextGaussian(),
                        getY() + random.nextGaussian(),
                        getZ() + random.nextGaussian(),
                        random.nextGaussian(), random.nextGaussian(), random.nextGaussian()
                );
                level().addParticle(random.nextBoolean() ? ParticleTypes.SPIT : LargeIcicleProjectile.ICE_PARTICLE,
                        getX() + random.nextGaussian(),
                        getY() + random.nextGaussian(),
                        getZ() + random.nextGaussian(),
                        random.nextGaussian(), random.nextGaussian(), random.nextGaussian()
                );
            }
        }
    }

    @Override
    public @NonNull HorusEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<HorusEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.horus.idle", AzPlayBehaviors.LOOP)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.horus.block", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_LOW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.light_low", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_AIR(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.light_air", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        IMPALE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.impale", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.barrage", AzPlayBehaviors.LOOP)),
        STOMP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.stomp", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DETONATE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.detonate", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DIVEKICK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.divekick", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DIVEKICK_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.divekick_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SCATTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.scatter", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE_ICICLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.charge_icicle", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE_FIRE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.charge_fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PLACE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.place", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ULTIMATE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.ultimate", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LANCE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.horus.lance", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(HorusEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected HorusEntity.State[] getStateValues() {
        return State.values();
    }

    @Override
    public HorusEntity.State getBlockState() {
        return State.BLOCK;
    }
}
