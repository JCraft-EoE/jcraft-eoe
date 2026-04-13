package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.common.attack.moves.speedking.FlamePunchAttack;
import net.arna.jcraft.common.attack.moves.speedking.HeadSmackAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireGrabAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireGrabHitAttack;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.common.attack.moves.speedking.FlashbangAttack;
import net.arna.jcraft.common.attack.moves.speedking.ImbueItemAttack;
import net.arna.jcraft.common.attack.moves.speedking.PureHeatAccumulationAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireSparksAttack;
import net.arna.jcraft.common.attack.moves.speedking.HeatWavesAttack;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public class SpeedKingEntity extends StandEntity<SpeedKingEntity, SpeedKingEntity.State> {
    public static final MoveSet<SpeedKingEntity, State> MOVE_SET = MoveSetManager.create(
            JStandTypeRegistry.SPEED_KING, "default", SpeedKingEntity::registerMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleRotation(270f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.speed_king"))
                    .freeSpace(Component.literal("""
                BNBs:
                    -the quickie
                    Punch>Barrage>Head Smack

                    -the dutch oven
                    Punch>Barrage>Fire grab>Flashbang"""))

                    .skinName(Component.literal("Rudolph"))
                    .skinName(Component.literal("Something"))
                    .skinName(Component.literal("AnotherSomething"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STAND_SUMMON))
            .build();

    public static final SimpleAttack<SpeedKingEntity> PUNCH_FOLLOWUP = new SimpleAttack<SpeedKingEntity>(
            0, 7, 11, 0.75f, 6f, 8, 1.5f, 1f, 0)
            .withAnim(State.PUNCH_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withBlockStun(4)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo finisher")
            );

    private static final FlamePunchAttack FLAME_PUNCH = new FlamePunchAttack(20, 14, 15, 0.75f, 5f, 16, 2f, 0.3f, -0.1f,
            3)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Flame Punch"),
                    Component.literal("slower heat-imbued punch, sets target on fire")
            );

    public static final SimpleAttack<SpeedKingEntity> PUNCH
            = SimpleAttack.<SpeedKingEntity>lightAttack(
                    5, 7, 0.75f, 5, 10, 0.1f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(PUNCH_FOLLOWUP)
            .withCrouchingVariant(FLAME_PUNCH)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );

    public static final HeadSmackAttack HEAD_SMACK = new HeadSmackAttack(0, 16, 20, 1f, 8f, 25, 2f, 0.4f, 0.1f,
            60,100)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withArmor(3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Head Smack"),
                    Component.literal("heat-covered aerial head punch causing knockdown and blindness")
            );

    public static final SimpleAttack<SpeedKingEntity> LUNGE = new SimpleAttack<SpeedKingEntity>(100, 14, 25,
            1f, 8f, 12, 2f, 1.5f, -0.2f)
            .withInitAction(LungeAction.lunge(0.75f, 0.15f).onGround())
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withAerialVariant(HEAD_SMACK)
            .withInfo(
                    Component.literal("Lunge"),
                    Component.literal("charge forward with heat, uninterruptible launcher"));

    public static final MainBarrageAttack<SpeedKingEntity> HEAT_BARRAGE = new MainBarrageAttack<SpeedKingEntity>(200,
            0, 35, 0.75f, 1.2f, 30, 2f, 0.25f, 0f, 2, Blocks.STONE.defaultDestroyTime())
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Component.literal("Heat Barrage"),
                    Component.literal("fast high damage barrage, 5-6 hearts total")
            );

    public static final FireGrabHitAttack FIRE_GRAB_HIT = new FireGrabHitAttack(0, 40, 0.75f, 2f, 24, 2f, 0.4f, 0f,
            IntSet.of(10, 20, 30, 40), 0.9f, 2.0f, 3, 1.5f)
            .withStunType(StunType.UNBURSTABLE)
            .withLift(false) // minor hits don't lift; final blow overrides this in processTarget
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withInfo(Component.literal("Fire Grab Hit"), Component.empty());

    public static final FireGrabAttack FIRE_GRAB = new FireGrabAttack(
            100, 10, 16, 1.5f, 0f, 0, 2.0f, 0f, 0.0f, FIRE_GRAB_HIT, StateContainer.of(State.FIRE_GRAB),
            40, 1.0)
            .withInfo(
                    Component.literal("Fire Grab"),
                    Component.literal("grabs enemy — 3 burning hits then a fiery launcher")
            );

    public static final FlashbangAttack FLASHBANG = new FlashbangAttack(
            200, 8, 12, 0.75f, 7.0f, 1.5, 4, 3, 2.5, 80,
            60, 2.0)
            .withInfo(
                    Component.literal("Flashbang"),
                    Component.literal("tags nearby enemies — detonates after a delay for heavy damage and blindness")
            );

    public static final PureHeatAccumulationAttack PURE_HEAT = new PureHeatAccumulationAttack(
            300, 15, 25, 1f, 1f, 20, 8f, 0, 0, 200, 9.0, 2.0, 5,
            100, 6.0, 3.0, 5)
            .withInfo(
                    Component.literal("Pure Heat Accumulation"),
                    Component.literal("AoE burst causing heat accumulation")
            );

    public static final FireSparksAttack FIRE_SPARKS = new FireSparksAttack(
            0, 20, 240, 0.75f, 5, 5, 30f, 15f, 1.5f)
            .withInfo(
                    Component.literal("Fire Sparks"),
                    Component.literal("holdable attack that shoots spreading sparks causing boiling")
            )
            .withHoldable();

    public static final HeatWavesAttack HEAT_WAVES = new HeatWavesAttack(
            800, 20, 30, 0.75f, 3, 40f, 1.2f, 1.5f, 2)
            .withCrouchingVariant(FIRE_SPARKS)
            .withInfo(
                    Component.literal("Heat Waves"),
                    Component.literal("shoots projectiles that create heat wave explosions on impact")
            );

    public static final ImbueItemAttack IMBUE_ITEM = new ImbueItemAttack(
            150, 12, 18, 0.75f, 300, 6.0f, 1.2, 0.3, 5.0, 1,
            0.4, 200)
            .withInfo(
                    Component.literal("Imbue Item with Heat"),
                    Component.literal("heats nearby items/blocks, causes boiling on pickup")
            );

    public static final SiroccoAttack SIROCCO = new SiroccoAttack(
            200, 1, 12, 0f, 4.0)
            .withInfo(
                    Component.literal("Sirocco"),
                    Component.literal("superheats the air behind you — flings you and nearby enemies forward")
            );

    public static final UpdraftAttack UPDRAFT = new UpdraftAttack(
            400, 10, 15, 0f, 160, 1.8, 25.0, 1.2)
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
        moves.register(MoveClass.LIGHT, PUNCH, State.PUNCH).withFollowup(State.PUNCH_FOLLOWUP);

        moves.register(MoveClass.HEAVY, LUNGE, State.LUNGE).withAerialVariant(State.HEAD_SMACK);
        moves.register(MoveClass.BARRAGE, HEAT_BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, FIRE_GRAB, State.FIRE_GRAB);
        moves.register(MoveClass.SPECIAL2, FLASHBANG, State.FLASHBANG);
        moves.register(MoveClass.SPECIAL3, PURE_HEAT, State.PURE_HEAT);

        moves.register(MoveClass.ULTIMATE, HEAT_WAVES, State.HEAT_WAVES).withCrouchingVariant(State.FIRE_SPARKS);

        moves.register(MoveClass.UTILITY, UPDRAFT, State.UPDRAFT).withAerialVariant(State.SIROCCO);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.LIGHT && getCurrentMove() != null && getCurrentMove().getMoveClass() == MoveClass.LIGHT && getMoveStun() < getCurrentMove().getWindupPoint()) {
            if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        }
        return super.initMove(moveClass);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            FlashbangAttack.tickTimers(level());
            UpdraftAttack.tickUpdraftPads(level());
            ImbueItemAttack.tickImbuedItems(level());
        }

        if (!hasUser()) {
            return;
        }
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
        FLAME_PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.flame_punch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
        LUNGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.lunge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAD_SMACK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.head_smack", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.barrage", AzPlayBehaviors.LOOP)),
        FIRE_GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        IMBUE_ITEM(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.imbue_item", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PURE_HEAT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.heat_accumilation", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        FIRE_SPARKS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.fire_sparks", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAT_WAVES(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.heat_waves", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
        FLASHBANG(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.flashbang", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        UPDRAFT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.updraft", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
        SIROCCO(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.speed_king.sirocco", AzPlayBehaviors.HOLD_ON_LAST_FRAME)), //TODO: garlic please make anim
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
