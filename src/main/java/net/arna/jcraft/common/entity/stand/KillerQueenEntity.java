package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.killerqueen.CoinTossAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.KQGrabAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.KQGrabHitAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.SheerHeartAttackAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.function.Consumer;

public final class KillerQueenEntity extends AbstractKillerQueenEntity<KillerQueenEntity, KillerQueenEntity.State> {
    public static final SimpleAttack<KillerQueenEntity> HEAVY = new SimpleAttack<KillerQueenEntity>(
            200, 16, 24, 0.75f, 9f, 10, 2f, 1.75f, 0f)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withSound(JSoundRegistry.KQ_UPPERCUT)
            .withSound(JSoundRegistry.KQ_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withHyperArmor()
            .withLaunch()
            .withInfo(Text.literal("Haymaker"), Text.literal("slow, uninterruptible launcher"));
    public static final SheerHeartAttackAttack SHEER_HEART_ATTACK = new SheerHeartAttackAttack(1000, 16, 20, 1f)
//            .withSound(JSoundRegistry.KQ_SHA)
            .withInfo(Text.literal("Sheer Heart Attack"), Text.literal("creates an automatic, heat-seeking sub-stand that explodes on contact, reflects 25% damage back to owner"));
    public static final KQGrabHitAttack GRAB_HIT = new KQGrabHitAttack(0, 13, 20, 1f, 8)
            .withInfo(Text.literal("Grab (hit)"), Text.empty());
    public static final KQGrabAttack GRAB = new KQGrabAttack(300, 12, 20, 0.75f,
            0f, 20, 1.75f, 0.1f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withInfo(Text.literal("Grab"), Text.literal("grabs opponent by the face, then detonates them, launching them upwards"));
    public static final CoinTossAttack COIN_TOSS = new CoinTossAttack(240);

    // Light chain implementation
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LOW = AbstractKillerQueenEntity.LOW.copy().withAnim(KQBTDEntity.State.LOW);
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT_FOLLOWUP = AbstractKillerQueenEntity.LIGHT_FOLLOWUP.copy().withAnim(KQBTDEntity.State.LIGHT_FOLLOWUP).withFollowup(LOW);
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT = AbstractKillerQueenEntity.LIGHT.copy().withFollowup(LIGHT_FOLLOWUP);

    public KillerQueenEntity(World worldIn) {
        super(StandType.KILLER_QUEEN, worldIn, null);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.7f, 0.8f),
                new Vector3f(1f, 1f, 1f),
                new Vector3f(0.5f, 0.2f, 0.6f),
                new Vector3f(0.4f, 0.7f, 1.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<KillerQueenEntity, KillerQueenEntity.State> moves) {
        super.registerMoves(moves);

        // Barrage and util are registered by the super class.
        moves.registerImmediate(MoveType.LIGHT, LIGHT, getLightState());

        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveType.SPECIAL1, BOMB_PLANT, State.BOMB_PLANT);
        moves.register(MoveType.SPECIAL2, GRAB, State.GRAB);
        moves.register(MoveType.SPECIAL3, COIN_TOSS); // No special state
        moves.register(MoveType.ULTIMATE, SHEER_HEART_ATTACK, State.SHA);
    }

    // Move-set
    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.SPECIAL1)
            if (coin != null) coin.discard();

       return super.initMove(type);
    }

    @Override
    @NonNull
    public KillerQueenEntity getThis() {
        return this;
    }

    // Animations
    public enum State implements StandAnimationState<KillerQueenEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.killerqueen.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.killerqueen.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.heavy"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.killerqueen.barrage"))),
        DETONATE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.detonate"))),
        BOMB_PLANT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.bombplant"))),
        SHA(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.sha"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.light_followup"))),
        LOW(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.low"))),
        GRAB(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.grab"))),
        GRAB_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.killerqueen.grab_hit")));


        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KillerQueenEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.killerqueen.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }

    @Override
    protected State getLightState() {
        return State.LIGHT;
    }

    @Override
    protected State getBarrageState() {
        return State.BARRAGE;
    }

    @Override
    protected State getDetonateState() {
        return State.DETONATE;
    }
}
