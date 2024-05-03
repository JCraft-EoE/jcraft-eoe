package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.starplatinum.InhaleAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class StarPlatinumEntity extends AbstractStarPlatinumEntity<StarPlatinumEntity, StarPlatinumEntity.State> {
    public static final UppercutAttack<StarPlatinumEntity> UPPERCUT = new UppercutAttack<StarPlatinumEntity>(20,
            8, 14, 0.75f, 6f, 20, 1.5f, 0.25f, -0.6f, 0.75f)
            .withAnim(State.UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, 0.35, 1.25)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Uppercut"),
                    Text.literal("slower combo starter, launches vertically")
            );
    public static final SimpleAttack<StarPlatinumEntity> LIGHT_FOLLOWUP = new SimpleAttack<StarPlatinumEntity>(
            0, 6, 10, 0.75f, 6f, 8, 1.5f, 1f, -0.25f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<StarPlatinumEntity> LIGHT = SimpleAttack.<StarPlatinumEntity>lightAttack(
            5, 7, 0.75f, 5f, 10, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter")
            );
    public static final MainBarrageAttack<StarPlatinumEntity> BARRAGE = new MainBarrageAttack<StarPlatinumEntity>(280,
            0, 40, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3, Blocks.OBSIDIAN.getHardness())
            .withSound(JSoundRegistry.STAR_PLATINUM_BARRAGE)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, high stun")
            );
    public static final KnockdownAttack<StarPlatinumEntity> GRAB_HIT = new KnockdownAttack<StarPlatinumEntity>(0,
            10, 20, 1f, 6f, 15, 1.75f, 0.4f, 0f, 35)
            .withSound(JSoundRegistry.SPTW_UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHyperArmor()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Takedown (hit)"),
                    Text.empty()
            );
    public static final GrabAttack<StarPlatinumEntity, StarPlatinumEntity.State> GRAB = new GrabAttack<>(280, 8, 20,
            1f, 2f, 20, 1.5f, 0.1f, 0f, GRAB_HIT, State.GRAB_HIT, 11, 0.8)
            .withSound(JSoundRegistry.SPTW_GRAB)
            .withImpactSound(JSoundRegistry.SPTW_GRABHIT)
            .withHitAnimation(null)
            .withBlockableType(BlockableType.BLOCKABLE)
            .withInfo(
                    Text.literal("Takedown"),
                    Text.literal("blockable grab, knocks down")
            );
    public static final SimpleAttack<StarPlatinumEntity> STAR_FINGER = new SimpleAttack<StarPlatinumEntity>(200,
            12, 20, 0.75f, 5f, 30, 1.75f, -0.4f, -0.25f)
            .withCrouchingVariant(GRAB)
            .withSound(JSoundRegistry.STAR_FINGER)
            .withBlockStun(5)
            .withExtraHitBox(2, 0.1, 1)
            .withInfo(
                    Text.literal("Star Finger"),
                    Text.literal("medium windup, combo starter/extender")
            );
    public static final UppercutAttack<StarPlatinumEntity> KNEE_UP = new UppercutAttack<StarPlatinumEntity>(30,
            8, 14, 0.75f, 4f, 13, 1.6f, 0.2f, -0.4f, 0.5f)
            .withSound(JSoundRegistry.STAR_PLATINUM_KNEE)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Upward Knee"),
                    Text.literal("launches upward, larger and higher hitbox, higher stun")
            );
    public static final SimpleAttack<StarPlatinumEntity> KNEE = new SimpleAttack<StarPlatinumEntity>(20,
            7, 12, 0.9f, 6f, 9, 1.5f, 0.3f, 0f)
            .withAerialVariant(KNEE_UP)
            .withSound(JSoundRegistry.STAR_PLATINUM_KNEE)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Knee"),
                    Text.literal("fast poke, low stun")
            );
    public static final ChargeBarrageAttack<StarPlatinumEntity> SHORT_CHARGE_BARRAGE = new ChargeBarrageAttack<StarPlatinumEntity>(280, 5, 25,
            6f, 0.6f, 15, 1.5f, 0.1f, 0f, 3, true)
            .withSound(JSoundRegistry.STAR_PLATINUM_LUNGING_BARRAGE)
            .withBarrageShockwaves()
            .withBackstab(false)
            .withInfo(
                    Text.literal("Lunging Barrage"),
                    Text.literal("fast combo starter/extender, medium stun")
            );
    public static final ChargeBarrageAttack<StarPlatinumEntity> CHARGE_BARRAGE = new ChargeBarrageAttack<StarPlatinumEntity>(280, 5, 55,
            7f, 0.6f, 15, 1.5f, 0.1f, 0f, 3, false)
            .withSound(JSoundRegistry.STAR_PLATINUM_ADVANCING_BARRAGE)
            .withBarrageShockwaves()
            .withBackstab(false)
            .withCrouchingVariant(SHORT_CHARGE_BARRAGE)
            .withInfo(
                    Text.literal("Advancing Barrage"),
                    Text.literal("fast combo starter/extender, medium stun, extremely punishable on whiff")
            );
    public static final JumpMove<StarPlatinumEntity> JUMP = new JumpMove<StarPlatinumEntity>(300, 5,
            14, 1f, 1.5f)
            .withInfo(
                    Text.literal("Stand Jump"),
                    Text.literal("jumps in looked direction with slight upward bias, you must stay on the ground until Star Platinum jumps")
            );
    public static final InhaleAttack INHALE = new InhaleAttack(800, 5, 5, 1f, 80)
            .withInfo(
                    Text.literal("Inhale"),
                    Text.literal("vacuums nearby entities for 4 seconds")
            );
    private static final TrackedData<Integer> INHALE_TIME;

    static {
        INHALE_TIME = DataTracker.registerData(StarPlatinumEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public StarPlatinumEntity(World worldIn) {
        super(StandType.STAR_PLATINUM, worldIn);
        auraColors = new Vector3f[]{
                new Vector3f(0.8f, 0.5f, 1.0f),
                new Vector3f(0.6f, 0.2f, 1.0f),
                new Vector3f(0.2f, 0.8f, 0.6f),
                new Vector3f(0.1f, 0.3f, 1.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<StarPlatinumEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, LIGHT, State.PUNCH);

        moves.register(MoveType.HEAVY, STAR_BREAKER, State.HEAVY).withCrouchingVariant(State.GROUND_BREAKER);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, STAR_FINGER, State.STAR_FINGER).withCrouchingVariant(State.GRAB);
        moves.register(MoveType.SPECIAL2, KNEE, State.KNEE).withAerialVariant(State.KNEE_UP);
        moves.register(MoveType.SPECIAL3, CHARGE_BARRAGE, State.BARRAGE).withCrouchingVariant(State.BARRAGE);
        moves.register(MoveType.ULTIMATE, INHALE, State.INHALE);

        moves.register(MoveType.UTILITY, JUMP, State.JUMP);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(INHALE_TIME, 0);
    }

    public void setInhaleTime(int time) {
        dataTracker.set(INHALE_TIME, time);
    }

    public int getInhaleTime() {
        return dataTracker.get(INHALE_TIME);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super StarPlatinumEntity> followup = curMove.getFollowup();
            if (followup != null) {
                setMove(followup, (State) followup.getAnimation());
                return true;
            }
        }

        return super.initMove(type);
    }

    @Override
    public void tick() {
        super.tick();

        INHALE.tickInhale(this);
    }

    @Override
    @NonNull
    public StarPlatinumEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<StarPlatinumEntity> {
        IDLE((starPlatinum, builder) -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.starplatinum." +
                (starPlatinum.getInhaleTime() > 0 ? "inhaleidle" : "idle")))),
        PUNCH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.starplatinum.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.heavy"))),
        GROUND_BREAKER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.ground_slam"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.starplatinum.barrage"))),
        STAR_FINGER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.star_finger"))),
        INHALE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.inhale"))),
        KNEE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.knee"))),
        KNEE_UP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.knee_up"))),
        JUMP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.jump"))),
        GRAB(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.grab"))),
        GRAB_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.grabhit"))),
        UPPERCUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.uppercut"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.starplatinum.light_followup")));

        private final BiConsumer<StarPlatinumEntity, AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<StarPlatinumEntity, AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(StarPlatinumEntity attacker, AnimationState builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.starplatinum.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
