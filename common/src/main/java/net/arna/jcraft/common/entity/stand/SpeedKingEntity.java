package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.common.attack.moves.speedking.FlamePunchAttack;
import net.arna.jcraft.common.attack.moves.speedking.HeadSmackAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireGrabAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireGrabHitAttack;
import net.arna.jcraft.common.attack.moves.speedking.ImbueItemAttack;
import net.arna.jcraft.common.attack.moves.speedking.PureHeatAccumulationAttack;
import net.arna.jcraft.common.attack.moves.speedking.FireSparksAttack;
import net.arna.jcraft.common.attack.moves.speedking.FlashbangAttack;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class SpeedKingEntity extends StandEntity<SpeedKingEntity, SpeedKingEntity.State> {
    public static final MoveSet<SpeedKingEntity, State> MOVE_SET = MoveSetManager.create(
            JStandTypeRegistry.SPEED_KING, "default", SpeedKingEntity::registerMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleRotation(-45f)
            .idleDistance(0.75f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.speed_king"))
                    .freeSpace(Component.literal("""                
                BNBs:
                    -the quickie
                    Punch>Barrage>Head Smack
                    
                    -heat zone
                    Punch>Barrage>Head Smack>Fire Grab,
                
                    -the dutch oven
                    Punch>Barrage>Fire grab>Flashbang"""))

                    .skinName(Component.literal("Rudolph"))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Monarch"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STAND_SUMMON))
            .build();

    
    private static final FlamePunchAttack FLAME_PUNCH = new FlamePunchAttack(20, 14, 15, 0.75f, 6f, 16, 2f, 0.3f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Flame Punch"),
                    Component.literal("slower heat-imbued punch, sets target on fire")
            );


    public static final SimpleAttack<SpeedKingEntity> PUNCH_FOLLOWUP = new SimpleAttack<SpeedKingEntity>(
            0, 7, 11, 0.75f, 6f, 8, 1.5f, 1f, 0)
            .withAnim(State.PUNCH_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<SpeedKingEntity> PUNCH = SimpleAttack.<SpeedKingEntity>lightAttack(
                    5, 7, 0.75f, 5, 10, 0.1f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(PUNCH_FOLLOWUP)
            .withCrouchingVariant(FLAME_PUNCH)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final HeadSmackAttack HEAD_SMACK = new HeadSmackAttack(0, 20, 20, 1f, 8f, 25, 2f, 0.4f, 0.1f, 60, 100)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Head Smack"),
                    Component.literal("heat-covered head punch causing knockdown and blindness")
            );

    public static final MainBarrageAttack<SpeedKingEntity> HEAT_BARRAGE = new MainBarrageAttack<SpeedKingEntity>(200,
            0, 35, 0.75f, 1.2f, 30, 2f, 0.25f, 0f, 2, net.minecraft.world.level.block.Blocks.STONE.defaultDestroyTime())
            .withSound(JSoundRegistry.TW_BARRAGE)
            .withInfo(
                    Component.literal("Heat Barrage"),
                    Component.literal("fast high damage barrage, 5-6 hearts total")
            );

    public static final FireGrabHitAttack FIRE_GRAB_HIT = new FireGrabHitAttack(0, 8, 20, 0.75f,
            4f, 20)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Fire Grab (Hit)"),
                    Component.literal("imbues target with heat")
            );

    public static final FireGrabAttack FIRE_GRAB = new FireGrabAttack(100, 10, 16, 0.75f,
            4f, 30, 2f, 3f, 0f, FIRE_GRAB_HIT, StateContainer.of(State.FIRE_GRAB_HIT), 20, 1)
            .withInfo(
                    Component.literal("Fire Grab"),
                    Component.literal("grab that imbues target with internal heat, causes nausea and fire damage")
            );

    public static final ImbueItemAttack IMBUE_ITEM = new ImbueItemAttack(150, 12, 18, 0.75f)
            .withInfo(
                    Component.literal("Imbue Item with Heat"),
                    Component.literal("heats nearby items/blocks, damages and knockbacks on pickup, deletes the item. ")
            );

    public static final PureHeatAccumulationAttack PURE_HEAT = new PureHeatAccumulationAttack(300, 8, 25, 1f,
            0f, 25, 8f, 0f, 1f)
            .withInfo(
                    Component.literal("Pure Heat Accumulation"),
                    Component.literal("Creates a 5 radius heat zone in front of the user")
            );

    public static final FireSparksAttack FIRE_SPARKS = new FireSparksAttack(800, 20, 30, 0.75f)
            .withInfo(
                    Component.literal("Fire Sparks"),
                    Component.literal("shoots spreading fire sparks that create persistent flame areas")
            );

    public static final FlashbangAttack FLASHBANG = new FlashbangAttack(200, 8, 12, 0.75f)
            .withInfo(
                    Component.literal("Flashbang"),
                    Component.literal("fire sparks that blind nearby targets for repositioning")
            );

    public SpeedKingEntity(Level worldIn) {
        super(JStandTypeRegistry.SPEED_KING.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.4f, 0.1f),
                new Vector3f(1.0f, 0.4f, 0.1f),
                new Vector3f(1.0f, 0.4f, 0.1f),
                new Vector3f(1.0f, 0.4f, 0.1f),
        };
    }

    private static void registerMoves(MoveMap<SpeedKingEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, PUNCH, State.PUNCH);

        moves.register(MoveClass.HEAVY, HEAD_SMACK, State.HEAD_SMACK);
        moves.register(MoveClass.BARRAGE, HEAT_BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, FIRE_GRAB, State.FIRE_GRAB);
        moves.register(MoveClass.SPECIAL2, IMBUE_ITEM, State.IMBUE_ITEM);
        moves.register(MoveClass.SPECIAL3, PURE_HEAT, State.PURE_HEAT);

        moves.register(MoveClass.ULTIMATE, FIRE_SPARKS, State.FIRE_SPARKS);

        moves.register(MoveClass.UTILITY, FLASHBANG, State.FLASHBANG);
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

        if (!hasUser()) {
            return;
        }
    }

    @Override
    @NonNull
    public SpeedKingEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<SpeedKingEntity> { //yea no animations sorry (garlic better get to work)
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.speed_king.idle"))),
        PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.light"))),
        PUNCH_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.light_followup"))),
        FLAME_PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.flame_punch"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.speed_king.block"))),
        HEAD_SMACK(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.idle"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.speed_king.barrage"))),
        FIRE_GRAB(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.grab"))),
        FIRE_GRAB_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.grabhit"))),
        IMBUE_ITEM(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.imbue_item"))),
        PURE_HEAT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.heat_accumilation"))),
        FIRE_SPARKS(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.fire_sparks"))),
        FLASHBANG(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.speed_king.flashbang")));

        private final Consumer<AnimationState<SpeedKingEntity>> animator;

        State(Consumer<AnimationState<SpeedKingEntity>> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SpeedKingEntity attacker, AnimationState<SpeedKingEntity> builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.speed_king.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}