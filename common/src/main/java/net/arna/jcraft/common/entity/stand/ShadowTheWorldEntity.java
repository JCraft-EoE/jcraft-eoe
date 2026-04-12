package net.arna.jcraft.common.entity.stand;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.shadowtheworld.ImpalingThrustAttack;
import net.arna.jcraft.common.attack.moves.shadowtheworld.STWChargeAttack;
import net.arna.jcraft.common.attack.moves.shadowtheworld.STWCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.overheaven.LungeAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/The_World">Shadow The World</a>.
 * @see JStandTypeRegistry#SHADOW_THE_WORLD
 * @see net.arna.jcraft.client.renderer.entity.stands.ShadowTheWorldRenderer ShadowTheWorldRenderer
 * @see STWCounterAttack
 */
@Getter
public final class ShadowTheWorldEntity extends AbstractTheWorldEntity<ShadowTheWorldEntity, ShadowTheWorldEntity.State> {
    public static final String DESUMMON_CONTROLLER = "desummon";

    public static final MoveSet<ShadowTheWorldEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.SHADOW_THE_WORLD,
            ShadowTheWorldEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-45f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.shadow_the_world"))
                    .proCount(5)
                    .conCount(4)
                    .freeSpace(Component.literal("""
                The user is allowed to use spec moves as soon as Shadow The World is performing one.
                Desummons itself upon finishing a move.
                """))
                    .skinName(Component.literal("Contrast"))
                    .skinName(Component.literal("Frost"))
                    .skinName(Component.literal("Predator"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STW_WARBLE))
            .build();

    public static final SimpleUppercutAttack<ShadowTheWorldEntity> UPPERCUT = new SimpleUppercutAttack<ShadowTheWorldEntity>(0,
            10, 16, 0.75f, 6f, 20, 1.5f, 0.25f, -0.6f, 1.0f)
            .withAnim(State.UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, 0.35, 1.25)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Uppercut"),
                    Component.literal("slower combo starter, launches vertically")
            );
    public static final SimpleAttack<ShadowTheWorldEntity> LIGHT = SimpleAttack.<ShadowTheWorldEntity>lightAttack(
                    5, 7, 0.75f, 5, 10, 0.1f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(UPPERCUT)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final KnockdownAttack<ShadowTheWorldEntity> GUARD_CANCEL = new KnockdownAttack<ShadowTheWorldEntity>(0,
            10, 16, 0.75f, 7f, 12, 1.75f, 2f, 0f, 25)
            .withAnim(State.GUARD_CANCEL)
            .withHyperArmor()
            .withSound(JSoundRegistry.STW_WARBLE)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withLaunch()
            .withInfo(
                    Component.literal("Shoulder Bash"),
                    Component.literal("uninterruptible get-off-me tool, brief knockdown")
            );
    public static final LungeAttack LUNGE = new LungeAttack(0,
            14, 20, 0.75f,8f, 19, 1.6f, 2f, 0f, 10, 6)
            .withCrouchingVariant(GUARD_CANCEL)
            .withSound(JSoundRegistry.STW_WARBLE)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Lunge"),
                    Component.literal("medium speed launcher")
            );
    public static final KnockdownAttack<ShadowTheWorldEntity> KNOCKDOWN = new KnockdownAttack<ShadowTheWorldEntity>(0,
            2, 4, 0.85f, 5f, 20, 1.75f, 2f, 0, 35)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("3 Hit Combo (Finisher)"),
                    Component.empty()
            );
    public static final SimpleMultiHitAttack<ShadowTheWorldEntity> THREE_HIT = new SimpleMultiHitAttack<ShadowTheWorldEntity>(0,
            24, 0.85f, 4f, 15, 1.5f, 0.35f, 0.2f, IntSet.of(6, 14))
            .withFinisher(20, KNOCKDOWN)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("3 Hit Combo"),
                    Component.literal("knocks down")
            );
    public static final ImpalingThrustAttack IMPALING_THRUST_HIT = new ImpalingThrustAttack(0, 1, 11, 0.8f);
    public static final SimpleHoldableMove<ShadowTheWorldEntity> IMPALING_THRUST = new SimpleHoldableMove<ShadowTheWorldEntity>(200,
            61, 60, 0.75f, 10)
            .withFollowup(IMPALING_THRUST_HIT)
            .withInfo(
                    Component.literal("Impaling Thrust"),
                    Component.literal("chargeable attack, Shadow The World prepares an attack, then stops time and hits everything between the start and end")
            )
            .markRanged()
            .withMobilityType(MobilityType.TELEPORT);
    public static final STWChargeAttack CHARGE = new STWChargeAttack(100,
            5, 19, 5.0f, 5f, 20, 1.5f, 0.25f, 0)
            .withSound(JSoundRegistry.TW_CHARGE)
            .withSound(JSoundRegistry.STW_WARBLE)
            .withImpactSound(JSoundRegistry.TW_CHARGE_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withBlockStun(11)
            .withInfo(
                    Component.literal("Forward Charge"),
                    Component.literal("The World detaches from the user and lunges forward, combo starter")
            );
    public static final TimeSkipMove<ShadowTheWorldEntity> TIME_SKIP = new TimeSkipMove<ShadowTheWorldEntity>(200, 7)
            .withSound(JSoundRegistry.TIME_SKIP)
            .withSound(JSoundRegistry.STW_ZAP)
            .withInfo(
                    Component.literal("Timeskip"),
                    Component.literal("7m range")
            );
    public static final TimeStopMove<ShadowTheWorldEntity> TIME_STOP = new TimeStopMove<ShadowTheWorldEntity>(1400,
            20, 30, Either.right(JServerConfig.STW_TIME_STOP_DURATION))
            .withSound(JSoundRegistry.STW_TS)
            .withInfo(
                    Component.literal("Timestop"),
                    Component.literal("2.5 seconds")
            );
    public static final STWCounterAttack COUNTER = new STWCounterAttack(400, 5, 20, 0.75f)
            .withInfo(
                    Component.literal("Counter"),
                    Component.literal("""
                                            if struck by an opponent, you will stun them and teleport behind them
                                            during this, you may not use your spec or move
                                            """)
            );
    private int desummonTime = 6;
    private static final EntityDataAccessor<Boolean> DESUMMONING = SynchedEntityData.defineId(ShadowTheWorldEntity.class, EntityDataSerializers.BOOLEAN);

    public ShadowTheWorldEntity(Level worldIn) {
        super(JStandTypeRegistry.SHADOW_THE_WORLD.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.5f, 0.1f, 0.7f),
                new Vector3f(0.8f, 0.2f, 0.4f),
                new Vector3f(0.2f, 0.6f, 8.0f),
                new Vector3f(0.7f, 0.3f, 1.0f)
        };
    }

    private static final Vector3f INVIS_AURA = new Vector3f(0, 0, 0);
    @Override
    public Vector3f getAuraColor() {
        if (getState() == State.COUNTER) return INVIS_AURA;
        return super.getAuraColor();
    }

    @Override
    public void queueMove(MoveInputType type) {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DESUMMONING, false);
    }

    private static void registerMoves(MoveMap<ShadowTheWorldEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);
        moves.registerImmediate(MoveClass.HEAVY, LUNGE, State.LUNGE);
        moves.register(MoveClass.BARRAGE, THREE_HIT, State.THREE_HIT);

        moves.register(MoveClass.SPECIAL1, COUNTER, State.COUNTER);
        moves.register(MoveClass.SPECIAL2, CHARGE, State.CHARGE);
        moves.register(MoveClass.SPECIAL3, IMPALING_THRUST, State.IMPALING_THRUST_CHARGE).withFollowup(State.IMPALING_THRUST_HIT);

        moves.register(MoveClass.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveClass.UTILITY, TIME_SKIP, State.IDLE);
    }

    private final AzCommand DESUMMON = AzCommand.create(DESUMMON_CONTROLLER, "animation.shadow_the_world.desummon");
    public void startAnimatedDesummon() {
        entityData.set(DESUMMONING, true);
        //todo: playSound(JSoundRegistry.SHADOW_THE_WORLD_DESUMMON);
        if (isFree()) return;
        setFree(true);
        setFreePos(position().toVector3f());
        DESUMMON.sendForEntity(this);
    }

    public boolean isAnimatedDesummoning() {
        return entityData.get(DESUMMONING);
    }

    @Override
    public boolean allowMoveHandling() {
        if (isAnimatedDesummoning()) return false;
        if (getState() == State.CHARGE_HIT) return false;
        final boolean noMove = getCurrentMove() == null;
        return noMove || getCurrentMove().getMoveClass() == MoveClass.SPECIAL3;
    }

    @Override
    public void cancelMove() {
        if (isAnimatedDesummoning()) return;
        super.cancelMove();
    }

    @Override
    public void tick() {
        super.tick();
        if (isAnimatedDesummoning()) {
            if (--desummonTime < 1) discard();
        }
        if (level().isClientSide()) {
            //stw particles?
            return;
        }
        if (tsTime < 1) {
            if ( (getCurrentMove() != null || getState() == State.CHARGE_HIT) && getMoveStun() == 1 && getState() != State.COUNTER) {
                // Stay in final attack pose
                setCurrentMove(null);
                setMoveStun(desummonTime);
                startAnimatedDesummon();
            }
        }
    }

    @Override
    public boolean defaultToNear() {
        return !isAnimatedDesummoning();
    }

    @Override
    public boolean isInvulnerable() {
        if (isAnimatedDesummoning()) return true;
        return super.isInvulnerable();
    }

    @Override
    @NonNull
    public ShadowTheWorldEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<ShadowTheWorldEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.block", AzPlayBehaviors.LOOP)),
        LUNGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.lunge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GUARD_CANCEL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.guard_cancel", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        THREE_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.3hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        IMPALING_THRUST_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.impaling_thrust_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        IMPALING_THRUST_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.impaling_thrust_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.charge_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        UPPERCUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.uppercut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.counter", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIME_STOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.shadow_the_world.timestop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ;
        
        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(ShadowTheWorldEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    /*
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new AnimationController<>(getThis(), "desummon", 0, this::desummonPredicate));
    }

    private static final RawAnimation DESUMMON_SQUEEZE = RawAnimation.begin()."animation.shadow_the_world.desummon");
    private PlayState desummonPredicate(AnimationState<ShadowTheWorldEntity> state) {
        if (isAnimatedDesummoning()) {
            state.getController().setAnimation(DESUMMON_SQUEEZE);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }*/

    @Override
    protected ShadowTheWorldEntity.State[] getStateValues() {
        return ShadowTheWorldEntity.State.values();
    }

    @Override
    public ShadowTheWorldEntity.State getBlockState() {
        return ShadowTheWorldEntity.State.BLOCK;
    }
}
