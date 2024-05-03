package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.goldexperience.BerryBushAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.LifeGiverAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.OverclockAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.TreeAttack;
import net.arna.jcraft.common.attack.moves.shared.HealMove;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.Consumer;

public class GoldExperienceEntity extends StandEntity<GoldExperienceEntity, GoldExperienceEntity.State> {
    // JCraft.lightCooldown -> 0 | 0.5f -> 0.35f
    public static final BerryBushAttack BERRY_BUSH = new BerryBushAttack(120, 16, 20,
            1.25f, 4f, 5, 1.5f, 0.75f, 0.2f)
            .withAnim(State.LIFE_GIVER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Text.literal("Place Berry Bush"),
                    Text.literal("places an almost-ripe berry bush on the ground, this move cannot be aimed up or down")
            );
    public static final SimpleAttack<GoldExperienceEntity> LIGHT_FOLLOWUP = new SimpleAttack<GoldExperienceEntity>(
            0, 7, 12, 0.75f, 6, 7, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<GoldExperienceEntity> LIGHT = new SimpleAttack<GoldExperienceEntity>(
            15, 6, 9, 0.75f, 5f, 7, 1.5f, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BERRY_BUSH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter, low stun")
            );
    public static final SimpleAttack<GoldExperienceEntity> HEAVY = new SimpleAttack<GoldExperienceEntity>(
            200, 13, 22, 1f, 9f, 10, 1.5f, 1.5f, 0f)
            .withExtraHitBox(new HitBoxData(0, 0, 1.25))
//            .withSound(JSoundRegistry.GE_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Text.literal("Shoulder Smash"),
                    Text.literal("slow, uninterruptible combo finisher")
            );
    public static final MainBarrageAttack<GoldExperienceEntity> BARRAGE = new MainBarrageAttack<GoldExperienceEntity>(
            280, 0, 30, 0.75f, 1f, 20, 2f, 0.25f, 0f, 3, Blocks.OAK_PLANKS.getHardness())
            .withSound(JSoundRegistry.GE_BARRAGE)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, high stun")
            );
    public static final HealMove<GoldExperienceEntity> HEAL_OTHERS = new HealMove<GoldExperienceEntity>(520, 10,
            16, 1f, 1.25f,
            0f, 4f, HealMove.HealTarget.TARGETS)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Text.literal("Healing Hand (others)"),
                    Text.empty()
            );
    public static final HealMove<GoldExperienceEntity> HEAL_SELF = new HealMove<GoldExperienceEntity>(520, 10,
            14, 1f, 0,
            0, 4f, HealMove.HealTarget.USER)
            .withCrouchingVariant(HEAL_OTHERS)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Text.literal("Healing Hand"),
                    Text.literal("standing: heals user for 2 hearts, crouching: heals others for 2 hearts, pacifies angered mobs")
            );
    public static final TreeAttack TREE = new TreeAttack(280, 10, 24, 1f, 5f,
            15, 1.75f, 0.2f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withSound(JSoundRegistry.GE_TREE)
            .withInfo(
                    Text.literal("Tree Summon"),
                    Text.literal("two-hitting launch")
            );
    public static final LifeGiverAttack LIFE_GIVER = new LifeGiverAttack(400, 16, 25, 1f)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Text.literal("Life Giver"),
                    Text.literal("""
                            STANDING: turns any stackable item into a snake, lasts for 25s and stuns for 0.5s on hit
                            CROUCHING: turns any stackable item into a frog, lasts for 15s and reflects damage, follows user
                            AERIAL: turns any item into a butterfly, lasts forever""")
            );
    public static final OverclockAttack OVERCLOCK = new OverclockAttack(920, 22, 31, 1f,
            9f, 60, 2f, 0.9f, 0f)
//            .withSound(JSoundRegistry.GE_ULT)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Text.literal("Overclock"),
                    Text.literal("slow, unblockable, devastating stun")
            );
    public static final KnockdownAttack<GoldExperienceEntity> REKKA3 = new KnockdownAttack<GoldExperienceEntity>
            (0, 12, 24, 1f, 6f, 15, 2f, 0.75f, 0f, 50)
            .withAnim(State.REKKA3)
            .withSound(JSoundRegistry.GE_REKKA3)
            .withLaunch()
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withBlockStun(8)
            .withInfo(
                    Text.literal("Rekka (Final Hit)"),
                    Text.literal("knockdown, low blockstun")
            );
    public static final SimpleAttack<GoldExperienceEntity> REKKA2 = new SimpleAttack<GoldExperienceEntity>
            (0, 9, 18, 1f, 5f, 16, 1.75f, 0.5f, 0f)
            .withAnim(State.REKKA2)
            .withSound(JSoundRegistry.GE_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowup(REKKA3)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Rekka (2nd Hit)"),
                    Text.literal("links into Light")
            );
    public static final SimpleAttack<GoldExperienceEntity> REKKA1 = new SimpleAttack<GoldExperienceEntity>
            (160, 7, 14, 1f, 5f, 15, 1.5f, 0.5f, 0f)
            .withAnim(State.REKKA1)
            .withSound(JSoundRegistry.GE_REKKA1)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowup(REKKA2)
            .withExtraHitBox(1.25)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Rekka Series"),
                    Text.literal("a set of three attacks, which cancel into each other during recovery")
            );

    public GoldExperienceEntity(World worldIn) {
        super(StandType.GOLD_EXPERIENCE, worldIn, JSoundRegistry.GE_SUMMON);

        idleRotation = -30f;

        description = "Impenetrable Regenerative DEFENSE";

        pros = List.of(
                "good pressure",
                "above average speed",
                "excellent defense (tree, heal, snake, heavy)",
                "excellent setups"
        );

        cons = List.of(
                "requires setup to become threatening",
                "no horizontal movement tools",
                "snake is unreliable"
        );

        freespace = """
                BNBs:
                    -the giogio
                    M1>Barrage>M1>Tree>Rekka 1~2~3
                    
                    -the superprince of gaming
                    Rekka 1~2>M1>Barrage>M1>Tree>Heavy""";

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.7f, 0.2f),
                new Vector3f(0.3f, 0.6f, 1.0f),
                new Vector3f(1.0f, 0.3f, 0.7f),
                new Vector3f(1.0f, 0.0f, 0.0f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<GoldExperienceEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, HEAL_SELF, State.HEAL_SELF).withCrouchingVariant(State.HEAL);
        moves.register(MoveType.SPECIAL2, REKKA1, State.REKKA1);
        moves.register(MoveType.SPECIAL3, LIFE_GIVER, State.LIFE_GIVER);
        moves.register(MoveType.ULTIMATE, OVERCLOCK, State.OVERCLOCK);

        moves.register(MoveType.UTILITY, TREE, State.TREE);
    }

    // Moveset
    @Override
    public boolean initMove(MoveType type) {
        switch (type) {
            case SPECIAL2 -> {
                LivingEntity user = getUserOrThrow();
                if (user.hasStatusEffect(JStatusRegistry.DAZED)) return false;
                boolean idling = this.getMoveStun() <= 0;
                if (curMove == null || curMove.getMoveType() != MoveType.SPECIAL2) {
                    if (idling) return handleMove(MoveType.SPECIAL2);
                    else return false;
                } else if (curMove.getFollowup() != null && curMove.hasWindupPassed(this))
                    setMove(curMove.getFollowup(), (State) curMove.getFollowup().getAnimation());
            }
            case SPECIAL3 -> {
                if (!canAttack() || !hasUser()) return false;
                LivingEntity user = getUserOrThrow();

                LifeGiverAttack.LifeGiverType toSummon = LifeGiverAttack.LifeGiverType.SNAKE;
                if (user.isOnGround()) {
                    if (user.isSneaking()) toSummon = LifeGiverAttack.LifeGiverType.FROG;
                } else toSummon = LifeGiverAttack.LifeGiverType.BUTTERFLY;
                moveContext.set(LifeGiverAttack.TYPE_TO_SUMMON, toSummon);

                return handleMove(MoveType.SPECIAL3);
            }
            case LIGHT -> {
                if (curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
                    AbstractMove<?, ? super GoldExperienceEntity> followup = curMove.getFollowup();
                    if (followup != null) setMove(followup, (State) followup.getAnimation());
                } else return super.initMove(type);
            }
            default -> {
                return super.initMove(type);
            }
        }

        return true;
    }

    /*
    @Override
    public boolean allowUtilityUse() { // Disables using the utility while sneaking, allowing menu control
        if (getUser().isSneaking()) return false;
        return super.allowUtilityUse();
    }
    @Environment(EnvType.CLIENT)
    boolean inMenu = false;
    @Override
    public void initClientUtility() {
        inMenu = true;
    }
     */

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super GoldExperienceEntity> attack,
                                                              LivingEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance,
                                                              StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return attack == LIFE_GIVER ?
                mob.getMainHandStack().isEmpty() && mob.getOffHandStack().isEmpty() ?
                        MoveSelectionResult.STOP : MoveSelectionResult.USE :
                MoveSelectionResult.PASS;
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.LIFE_GIVER) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    @NonNull
    public GoldExperienceEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<GoldExperienceEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.ge.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.ge.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.heavy"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.ge.barrage"))),
        HEAL_SELF(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.healself"))),
        HEAL(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.heal"))),
        TREE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.tree"))),
        LIFE_GIVER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.lifegiver"))),
        REKKA1(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.rekka1"))),
        REKKA2(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.rekka2"))),
        REKKA3(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.rekka3"))),
        OVERCLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.overclock"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.ge.light_followup")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GoldExperienceEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Nullable
    @Override
    protected String getSummonAnimation() {
        return "animation.ge.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
