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
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.common.attack.moves.killerqueen.*;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Killer_Queen">Killer Queen</a>.
 * @see JStandTypeRegistry#KILLER_QUEEN
 * @see net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack BombPlantAttack
 * @see CoinTossMove
 * @see KQDetonateAttack DetonateAttack
 * @see net.arna.jcraft.common.attack.moves.killerqueen.ExplosiveDashAttack ExplosiveDashAttack
 * @see KQGrabAttack
 * @see KQGrabAttack
 * @see SheerHeartAttackAttack
 */
public final class KillerQueenEntity extends AbstractKillerQueenEntity<KillerQueenEntity, KillerQueenEntity.State> {
    public static final MoveSet<KillerQueenEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.KILLER_QUEEN,
            KillerQueenEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-30f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.killerqueen"))
                    .proCount(4)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                BNBs:
                    -Standard bomb plant confirm and SHA setup
                    Light~Light>Barrage>Bomb plant>Detonate(>Sheer Heart Attack)
                
                    -Confirm while bomb plant is on cd
                    Light~Light>Barrage>Heavy(>Sheer Heart Attack)"""))
                    .skinName(Component.literal("Gunpowder"))
                    .skinName(Component.literal("Deadly"))
                    .skinName(Component.literal("1999"))
                    .build())
            .build();
    public static final Supplier<IPoseModifier> POSE = AbstractKillerQueenEntity.POSE;

    public static final SimpleAttack<KillerQueenEntity> HEAVY = new SimpleAttack<KillerQueenEntity>(0,
            16, 24, 0.75f, 9f, 10, 2f, 1.75f, 0f)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withSound(JSoundRegistry.KQ_UPPERCUT)
            .withSound(JSoundRegistry.KQ_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Haymaker"),
                    Component.literal("slow, uninterruptible launcher")
            );
    public static final SheerHeartAttackAttack SHEER_HEART_ATTACK = new SheerHeartAttackAttack(1000, 16, 20, 1f)
//            .withSound(JSoundRegistry.KQ_SHA)
            .withInfo(
                    Component.literal("Sheer Heart Attack"),
                    Component.literal("creates an automatic, heat-seeking sub-stand that explodes on contact, reflects 25% damage back to owner")
            );
    public static final KQGrabHitAttack GRAB_HIT = new KQGrabHitAttack(0, 13, 20, 1f, 8)
            .withInfo(
                    Component.literal("Grab (hit)"),
                    Component.empty()
            );
    public static final KQGrabAttack GRAB = new KQGrabAttack(300, 12, 20, 0.75f,
            0f, 20, 1.75f, 0.1f, 0f, GRAB_HIT)
            .withInfo(
                    Component.literal("Grab"),
                    Component.literal("grabs opponent by the face, then detonates them, launching them upwards")
            );
    public static final CoinTossMove COIN_TOSS = new CoinTossMove(240);

    // Light chain implementation
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LOW = AbstractKillerQueenEntity.LOW.copy().withAnim(KQBTDEntity.State.LOW);
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT_FOLLOWUP = AbstractKillerQueenEntity.LIGHT_FOLLOWUP.copy().withAnim(KQBTDEntity.State.LIGHT_FOLLOWUP).withFollowup(LOW);
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT = AbstractKillerQueenEntity.LIGHT.copy().withFollowup(LIGHT_FOLLOWUP);

    public KillerQueenEntity(Level worldIn) {
        super(JStandTypeRegistry.KILLER_QUEEN.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.7f, 0.8f),
                new Vector3f(1f, 1f, 1f),
                new Vector3f(0.5f, 0.2f, 0.6f),
                new Vector3f(0.4f, 0.7f, 1.0f)
        };
    }

    private static void registerMoves(MoveMap<KillerQueenEntity, State> moves) {
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);
        moves.register(MoveClass.UTILITY, EXPLOSIVE_DASH); // No special state for this one.

        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveClass.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveClass.SPECIAL1, BOMB_PLANT, State.BOMB_PLANT);
        moves.register(MoveClass.SPECIAL2, GRAB, State.GRAB);
        moves.register(MoveClass.SPECIAL3, COIN_TOSS); // No special state
        moves.register(MoveClass.ULTIMATE, SHEER_HEART_ATTACK, State.SHA);
    }

    // Move-set
    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.SPECIAL1) {
            if (coin != null) {
                coin.discard();
            }
        }

        return super.initMove(moveClass);
    }

    @Override
    @NonNull
    public KillerQueenEntity getThis() {
        return this;
    }

    // Animations
    public enum State implements StandAnimationState<KillerQueenEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.killerqueen.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.killerqueen.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.barrage", AzPlayBehaviors.LOOP)),
        DETONATE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.detonate", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BOMB_PLANT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.bombplant", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SHA(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.sha", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LOW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.low", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.killerqueen.grab_hit", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KillerQueenEntity attacker) {
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
