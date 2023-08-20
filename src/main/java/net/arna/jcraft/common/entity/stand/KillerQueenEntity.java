package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.killerqueen.CoinTossAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.KQGrabAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.KQGrabHitAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.SheerHeartAttackAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.function.Consumer;

public final class KillerQueenEntity extends AbstractKillerQueenEntity<KillerQueenEntity, KillerQueenEntity.State> {
    public static final SimpleAttack<KillerQueenEntity> HEAVY = new SimpleAttack<KillerQueenEntity>(
            240, 16, 24, 9f, 10, 2f, 1.75f, 0.75f, 0f)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withSound(JSoundRegistry.KQ_UPPERCUT)
            .withSound(JSoundRegistry.KQ_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withHyperArmor()
            .withLaunch()
            .withInfo(Text.literal("Haymaker"), Text.literal("slow, uninterruptible launcher"));
    public static final SheerHeartAttackAttack SHEER_HEART_ATTACK = new SheerHeartAttackAttack(1000, 16, 20, 1f)
//            .withSound(JSoundRegistry.KQ_SHA)
            .withInfo(Text.literal("Sheer Heart Attack"), Text.literal("creates an automatic, heat-seeking sub-stand that explodes on contact, reflects 25% damage back to owner"));
    public static final KQGrabHitAttack GRAB_HIT = new KQGrabHitAttack(0, 13, 20, 1f, 2)
            .withInfo(Text.literal("Grab (hit)"), Text.empty());
    public static final KQGrabAttack GRAB = new KQGrabAttack(440, 12, 20, 0.75f,
            0f, 20, 1.75f, 0.1f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withInfo(Text.literal("Grab"), Text.literal("grabs opponent by the face, then detonates them, launching them upwards"));
    public static final CoinTossAttack COIN_TOSS = new CoinTossAttack(500);

    public KillerQueenEntity(World worldIn) {
        super(StandType.KILLER_QUEEN, worldIn, null);

        super.initialize();
    }

    @Override
    protected void registerMoves(MoveMap<KillerQueenEntity, KillerQueenEntity.State> moves) {
        super.registerMoves(moves);

        // Light, barrage and util are registered by the super class.
        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveType.SPECIAL1, BOMB_PLANT, State.BOMB_PLANT);
        moves.register(MoveType.SPECIAL2, GRAB, State.GRAB);
        moves.register(MoveType.SPECIAL3, COIN_TOSS); // No special state
        moves.register(MoveType.ULT, SHEER_HEART_ATTACK, State.SHA);
    }

    // Move-set
    @Override
    public void initMove(MoveType type) {
        if (!hasUser()) return;

        if (type == MoveType.SPECIAL1) {
            LivingEntity user = getUserOrThrow();
            CooldownsComponent cooldowns = JComponents.getCooldowns(user);
            if (user.isInSneakingPose() && cooldowns.getCooldown(CooldownType.STAND_SP1) < 1) {
                Block downBlock = world.getBlockState(user.getBlockPos().down()).getBlock();
                boolean notAir = downBlock != Blocks.AIR && downBlock != Blocks.CAVE_AIR && downBlock != Blocks.VOID_AIR;
                if (notAir) {
                    bombEntity = null;
                    bombBlock = user.getPos().add(0, -0.5, 0);
                    cooldowns.setCooldown(CooldownType.STAND_SP1, BOMB_PLANT.getCooldown());
                }
            } else {
                handleMove(MoveType.SPECIAL1);
                bombBlock = null;
            }

            if (coin != null) coin.discard();
        } else super.initMove(type);
    }

    @Override
    protected KillerQueenEntity getThis() {
        return this;
    }

    // Animations
    public enum State implements StandAnimationState<KillerQueenEntity> {
        IDLE(builder -> builder.loop("animation.killerqueen.idle")),
        LIGHT(builder -> builder.playAndHold("animation.killerqueen.light")),
        BLOCK(builder -> builder.loop("animation.killerqueen.block")),
        HEAVY(builder -> builder.playAndHold("animation.killerqueen.heavy")),
        BARRAGE(builder -> builder.loop("animation.killerqueen.barrage")),
        DETONATE(builder -> builder.playAndHold("animation.killerqueen.detonate")),
        BOMB_PLANT(builder -> builder.playAndHold("animation.killerqueen.bombplant")),
        SHA(builder -> builder.playAndHold("animation.killerqueen.sha")),
        LOW(builder -> builder.playAndHold("animation.killerqueen.low")),
        GRAB(builder -> builder.playAndHold("animation.killerqueen.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.killerqueen.grab_hit"));


        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KillerQueenEntity stand, AnimationBuilder builder) {
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
    protected State getLowState() {
        return State.LOW;
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
