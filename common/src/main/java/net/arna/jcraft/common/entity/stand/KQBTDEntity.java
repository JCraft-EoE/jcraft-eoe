package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.util.client.RenderUtils;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.registry.JMarkerExtractorRegistry;
import net.arna.jcraft.api.registry.JMarkerInjectorRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust.*;
import net.arna.jcraft.common.attack.moves.shared.GrabAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Bites_the_Dust">Killer Queen Bites The Dust</a>.
 * @see JStandTypeRegistry#KILLER_QUEEN_BITES_THE_DUST
 * @see net.arna.jcraft.client.renderer.entity.stands.KQBTDRenderer KQBTDRenderer
 * @see BTDDetonateAttack
 * @see BTDGrabHitAttack
 * @see BTDPlantAttack
 * @see BubbleAttack
 * @see BubbleCounterAttack
 * @see ElbowAttack
 */
public final class KQBTDEntity extends AbstractKillerQueenEntity<KQBTDEntity, KQBTDEntity.State> {
    public static final MoveSet<KQBTDEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.KILLER_QUEEN_BITES_THE_DUST,
            KQBTDEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-30f)
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.kqbtd"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                BNBs:
                -the kitty cat
                Light~Low>Barrage>Bomb Plant/Bites the Dust Plant
                
                -the ol razzle dazzle
                (Already bomb planted) Light~Low>Barrage>Light>Elbow>Detonate"""))
                    .skinName(Component.literal("Veiled"))
                    .skinName(Component.literal("Back from the Dead"))
                    .skinName(Component.literal("Garf"))
                    .build())
            .summonData(SummonData.builder()
                    .sound(JSoundRegistry.KQBTD_SUMMON)
                    .playGenericSound(true)
                    .build())
            .build();
    public static final Supplier<IPoseModifier> POSE = AbstractKillerQueenEntity.POSE;

    public static final ElbowAttack ELBOW = new ElbowAttack(0, 5, 9, 0.75f,
            7.5f, 10, 1f, 1.1f, 0f)
            .withSound(JSoundRegistry.KQBTD_ELBOW)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withBlockStun(3)
            .withInfo(
                    Component.literal("Elbow"),
                    Component.literal("fast, short-range knockback, very low blockstun")
            );
    public static final BubbleCounterAttack BUBBLE_COUNTER = new BubbleCounterAttack(480, 5, 20, 1f)
            .withInfo(
                    Component.literal("Stray Cat Counter"),
                    Component.literal("0.25s windup counter, turns opponent into your primary bomb")
            );
    public static final BubbleAttack BUBBLE = new BubbleAttack(60, 15, 18, 0.75f)
            .withCrouchingVariant(BUBBLE_COUNTER)
            .withSound(JSoundRegistry.KQ_UPPERCUT)
            .withInfo(
                    Component.literal("Stray Cat Bubble"),
                    Component.literal("launches an explosive bubble guided by your view rotation")
            );
    public static final BTDDetonateAttack BTD_DETONATE = new BTDDetonateAttack(20, 5, 6, 0.75f, 200)
            .withSound(JSoundRegistry.KQ_DETONATE)
            .withInfo(
                    Component.literal("Detonate"),
                    Component.empty()
            );
    public static final BTDPlantAttack BTD_PLANT = new BTDPlantAttack(800,
            14, 24, 1f, 10, 1.5f, 0f, BTDPlantAttack.ENTITY_STUFF_TO_SAVE, JMarkerExtractorRegistry.ALL.get(), JMarkerInjectorRegistry.ALL.get())
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withBlockStun(8)
            .withInfo(
                    Component.literal("Bites the Dust Plant"),
                    Component.literal("press the same button to detonate, sending the affected enemy back to their previous location")
            );
    public static final BTDGrabHitAttack GRAB_HIT = new BTDGrabHitAttack(0, 42, 0.75f,
            5f, 15, 2f, 0f, 0.5f, IntSet.of(8, 22, 32))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(
                    Component.literal("Takedown (hit)"),
                    Component.empty()
            );
    public static final GrabAttack<KQBTDEntity, State> GRAB = new GrabAttack<>(220,
            12, 28,0.75f, 0f, 20, 1.75f, 0.1f, 0f, GRAB_HIT,
            StateContainer.of(State.GRAB_HIT), 31, 1)
            .withInfo(
                    Component.literal("Takedown"),
                    Component.literal("high damage grab")
            );

    // Light chain implementation
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LOW = AbstractKillerQueenEntity.LOW.copy().withAnim(State.LOW);
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT_FOLLOWUP = AbstractKillerQueenEntity.LIGHT_FOLLOWUP.copy().withAnim(State.LIGHT_FOLLOWUP).withFollowup(LOW);
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT = AbstractKillerQueenEntity.LIGHT.copy().withFollowup(LIGHT_FOLLOWUP);

    public KQBTDEntity(Level worldIn) {
        super(JStandTypeRegistry.KILLER_QUEEN_BITES_THE_DUST.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.7f, 0.8f),
                new Vector3f(0.3f, 1.0f, 0.5f),
                new Vector3f(0.8f, 0.2f, 0.2f),
                new Vector3f(0.8f, 0.6f, 0.2f)
        };
    }

    @Override
    public Vector3f getAuraColor() {
        final int skin = getSkin();
        final float deltaTick = (float) RenderUtils.getCurrentTick() * 50 % 2.0f;
        return switch (skin) {
            case 1 ->
                new Vector3f(auraColors[skin]).mul(1.0f, deltaTick, 1.0f);
            case 2 ->
                new Vector3f(auraColors[skin]).mul(deltaTick, 1.0f, 1.0f);
            case 3 ->
                new Vector3f(auraColors[skin]).mul(1.0f, deltaTick, deltaTick);
            default ->
                new Vector3f(auraColors[skin]).mul(deltaTick * 0.5f, deltaTick, 1.0f);
        };
    }

    private static void registerMoves(MoveMap<KQBTDEntity, State> moves) {
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);
        moves.register(MoveClass.UTILITY, EXPLOSIVE_DASH); // No special state for this one.

        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveClass.HEAVY, ELBOW, State.HEAVY);
        moves.register(MoveClass.SPECIAL1, BOMB_PLANT, State.BOMB_PLANT);
        moves.register(MoveClass.SPECIAL2, BUBBLE, State.BUBBLE).withCrouchingVariant(State.BUBBLE_COUNTER);
        moves.register(MoveClass.SPECIAL3, GRAB, State.GRAB);
        moves.register(MoveClass.ULTIMATE, BTD_PLANT, State.BTD_PLANT);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.ULTIMATE) {
            final BTDPlantAttack btdPlantAttack = getMove(BTDPlantAttack.class);
            if (btdPlantAttack != null && btdPlantAttack.getEntityMarker() != null) {
                return handleMove(BTD_DETONATE, CooldownType.ULTIMATE, State.DETONATE);
            } else {
                return handleMove(MoveClass.ULTIMATE);
            }
        } else {
            return super.initMove(moveClass);
        }
    }

    @Override
    @NonNull
    public KQBTDEntity getThis() {
        return this;
    }

    // Animations
    public enum State implements StandAnimationState<KQBTDEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.kqbtd.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.kqbtd.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.barrage", AzPlayBehaviors.LOOP)),
        DETONATE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.detonate", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BOMB_PLANT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.bombplant", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BUBBLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.bubble", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LOW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.low", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BUBBLE_COUNTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.bubblecounter", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER_MISS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.counter_miss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BTD_PLANT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.btdplant", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kqbtd.grab_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KQBTDEntity attacker) {
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

    @Override
    protected State getDetonateState() {
        return State.DETONATE;
    }
}
