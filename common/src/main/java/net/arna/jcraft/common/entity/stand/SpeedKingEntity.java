package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.speedking.FlamePunchAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireGrabAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireGrabHitAttack;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.common.attack.moves.speedking.ThermalShockwaveAttack;
import net.arna.jcraft.common.attack.moves.speedking.HeatTrapAttack;
import net.arna.jcraft.common.attack.moves.speedking.HeatTrapManager;
import net.arna.jcraft.common.attack.moves.speedking.ImbueItemAttack;
import net.arna.jcraft.common.attack.moves.speedking.PureHeatAccumulationAttack;
import net.arna.jcraft.common.attack.moves.speedking.HeatWavesAttack;
import net.arna.jcraft.common.attack.moves.speedking.OverheatAttack;
import net.arna.jcraft.common.attack.moves.speedking.SiroccoAttack;
import net.arna.jcraft.common.attack.moves.speedking.UpdraftAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.actions.LungeAction;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.attack.StateContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public class SpeedKingEntity extends StandEntity<SpeedKingEntity, SpeedKingEntity.State> {
    public static final MoveSet<SpeedKingEntity, State> MOVE_SET = MoveSetManager.create(
            JStandTypeRegistry.SPEED_KING, "default", SpeedKingEntity::registerMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleRotation(315f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.speed_king"))
                    .freeSpace(Component.literal("""
                BNBs:
                    -the dutch oven
                    Punch>Barrage>Head Smack>Fire grab>Overheat"""))

                    .skinName(Component.literal("Toy"))
                    .skinName(Component.literal("Lavender"))
                    .skinName(Component.literal("Rudolph"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STAND_SUMMON))
            .build();

    public static final SimpleAttack<SpeedKingEntity> PUNCH_FOLLOWUP = new SimpleAttack<SpeedKingEntity>(
            0, 7, 11, 0.75f, 5f, 8, 1.5f, 1f, 0)
            .withAnim(State.PUNCH_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withBlockStun(4)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo finisher")
            );

    public static final OverheatAttack OVERHEAT = new OverheatAttack(60, 10, 10, 1f)
            .withInfo(
                    Component.literal("Overheat"),
                    Component.literal("detonates all stored heat on every target simultaneously: damage scales with heat level")
            );

    public static final FlamePunchAttack FLAME_PUNCH = new FlamePunchAttack(20, 9, 15, 0.75f, 5f, 16, 2f, 0.3f, -0.1f,
            3)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Flame Punch"),
                    Component.literal("slower heat-imbued punch, sets target on fire")
            );

    public static final SimpleAttack<SpeedKingEntity> PUNCH = SimpleAttack.<SpeedKingEntity>lightAttack(5, 7, 0.75f, 4, 10, 0.1f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(PUNCH_FOLLOWUP)
            .withCrouchingVariant(OVERHEAT)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );

    public static final KnockdownAttack<SpeedKingEntity> HEAD_SMACK = new KnockdownAttack<SpeedKingEntity>(0, 16, 20, 1f, 8f, 25, 2f, 0.4f, 0.1f, 35)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withArmor(3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Head Smack"),
                    Component.literal("armored aerial head punch causing knockdown")
            );

    public static final SimpleAttack<SpeedKingEntity> LUNGE = new SimpleAttack<SpeedKingEntity>(100, 14, 25,
            1f, 8f, 12, 2f, 1.5f, -0.2f)
            .withInitAction(LungeAction.lunge(0.75f, 0.15f).onGround())
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withCrouchingVariant(FLAME_PUNCH)
            .withAerialVariant(HEAD_SMACK)
            .withInfo(
                    Component.literal("Lunge"),
                    Component.literal("charge forward with heat, uninterruptible launcher"));

    public static final MainBarrageAttack<SpeedKingEntity> HEAT_BARRAGE = new MainBarrageAttack<SpeedKingEntity>(200,
            0, 35, 0.75f, 0.8f, 30, 2f, 0.25f, 0f, 2, Blocks.STONE.defaultDestroyTime())
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Component.literal("Heat Barrage"),
                    Component.literal("fast high damage barrage, 5-6 hearts total")
            );

    public static final FireGrabHitAttack FIRE_GRAB_HIT = new FireGrabHitAttack(0, 40, 0.75f, 2f, 24, 2f, 0.4f, 0f,
            IntSet.of(10, 20, 30, 40), 3, 1.5f)
            .withStunType(StunType.UNBURSTABLE)
            .withLift(false)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withInfo(Component.literal("Fire Grab Hit"), Component.empty());

    public static final FireGrabAttack FIRE_GRAB = new FireGrabAttack(
            100, 10, 16, 1.5f, 0f, 24, 2.0f, 0f, 0.0f, FIRE_GRAB_HIT, StateContainer.of(State.FIRE_GRAB),
            40, 1.0)
            .withInfo(
                    Component.literal("Fire Grab"),
                    Component.literal("grabs enemy: 3 hits then a launcher")
            );

    public static final HeatWavesAttack HEAT_WAVES = new HeatWavesAttack(
            300, 20, 30, 0.75f, 4, 40f, 1.2f, 1.5f, 2)
            .withInfo(
                    Component.literal("Heat Waves"),
                    Component.literal("shoots projectiles that create heat wave explosions on impact")
            );

    public static final HeatTrapAttack HEAT_TRAP = new HeatTrapAttack(400, 5, 12, 0.75f)
            .withCrouchingVariant(HEAT_WAVES)
            .withInfo(
                    Component.literal("Heat Trap"),
                    Component.literal("fires projectiles that store heat on targets")
            );

    public static final PureHeatAccumulationAttack PURE_HEAT_ACCUMULATION = new PureHeatAccumulationAttack(
            600, 15, 25, 1f, 0f, 0, 7f, 0, 1)
            .withInfo(
                    Component.literal("Pure Heat Accumulation"),
                    Component.literal("charges the ground and coats a wide area in heat traps")
            );

    public static final ThermalShockwaveAttack THERMAL_SHOCKWAVE = new ThermalShockwaveAttack(
            600, 14, 50, 0.75f, 4f, 20, 2.5f, 0.5f, 0f)
            .withInfo(
                    Component.literal("Thermal Shockwave"),
                    Component.literal("Speed King imbues the ground in front of him with heat, spreading it forward")
            );

    public static final ImbueItemAttack IMBUE_ITEM = new ImbueItemAttack(
            150, 12, 18, 0.75f, 300, 3.0f, 100)
            .withInfo(
                    Component.literal("Imbue Item with Heat"),
                    Component.literal("heats nearby items/blocks, causes boiling on pickup")
            );

    public static final SiroccoAttack SIROCCO = new SiroccoAttack(
            200, 1, 12, 0f, 4.0)
            .withSound(JSoundRegistry.WHOOSH)
            .withInfo(
                    Component.literal("Sirocco"),
                    Component.literal("superheats the air behind you — flings you and nearby enemies forward")
            );

    public static final UpdraftAttack UPDRAFT = new UpdraftAttack(
            400, 10, 15, 0f, 160, 0.45, 25.0, 1.2)
            .withCrouchingVariant(IMBUE_ITEM)
            .withAerialVariant(SIROCCO)
            .withInfo(
                    Component.literal("Updraft"),
                    Component.literal("creates wind launcher that sends you up after 3 seconds")
            );

    public SpeedKingEntity(Level worldIn) {
        super(JStandTypeRegistry.SPEED_KING.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.4f, 0.1f),
                new Vector3f(1.0f, 0.1f, 0.1f),
                new Vector3f(0.8f, 0.4f, 1.0f),
                new Vector3f(0.4f, 0.2f, 0.1f)
        };
    }

    private static void registerMoves(MoveMap<SpeedKingEntity, State> moves) {
        final var punch = moves.register(MoveClass.LIGHT, PUNCH, State.PUNCH);
        punch.withCrouchingVariant(State.OVERHEAT);
        punch.withFollowup(State.PUNCH_FOLLOWUP);

        final var lunge = moves.register(MoveClass.HEAVY, LUNGE, State.LUNGE);
        lunge.withCrouchingVariant(State.FLAME_PUNCH);
        lunge.withAerialVariant(State.HEAD_SMACK);
        moves.register(MoveClass.BARRAGE, HEAT_BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, FIRE_GRAB, State.FIRE_GRAB);
        moves.register(MoveClass.SPECIAL2, HEAT_TRAP, State.HEAT_TRAP).withCrouchingVariant(State.HEAT_WAVES);
        moves.register(MoveClass.SPECIAL3, PURE_HEAT_ACCUMULATION, State.PURE_HEAT_ACCUMULATION);

        moves.register(MoveClass.ULTIMATE, THERMAL_SHOCKWAVE, State.THERMAL_SHOCKWAVE);

        moves.register(MoveClass.UTILITY, UPDRAFT, State.UPDRAFT).withAerialVariant(State.SIROCCO);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.LIGHT && !getUserOrThrow().isCrouching()
                && getCurrentMove() != null && getCurrentMove().getMoveClass() == MoveClass.LIGHT
                && getMoveStun() < getCurrentMove().getWindupPoint()) {
            if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        }
        return super.initMove(moveClass);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            UpdraftAttack.tickUpdraftPads(level());
            ImbueItemAttack.tickImbuedItems(level());
            if (hasUser() && level() instanceof ServerLevel serverLevel) {
                HeatTrapManager.tick(serverLevel, this);
                OverheatAttack.tickBursts(serverLevel, this);
            }
        }

        if (!hasUser()) {
            return;
        }

        getUserOrThrow().setTicksFrozen(0);
    }

    @Override
    public SpeedKingEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<SpeedKingEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.speed_king.idle", AzPlayBehaviors.LOOP)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.speed_king.block", AzPlayBehaviors.LOOP)),
        PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PUNCH_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        FLAME_PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.flame_punch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LUNGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.lunge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAD_SMACK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.head_smack", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.barrage", AzPlayBehaviors.LOOP)),
        FIRE_GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        IMBUE_ITEM(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.imbue_item", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PURE_HEAT_ACCUMULATION(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.heat_accumilation", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        THERMAL_SHOCKWAVE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.thermal_shockwave", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
        HEAT_WAVES(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.heat_waves", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAT_TRAP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.flashbang", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        OVERHEAT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.overheat", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
        UPDRAFT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.updraft", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
        SIROCCO(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.sirocco", AzPlayBehaviors.HOLD_ON_LAST_FRAME)) //TODO: garlic please make anim
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SpeedKingEntity attacker) {
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
