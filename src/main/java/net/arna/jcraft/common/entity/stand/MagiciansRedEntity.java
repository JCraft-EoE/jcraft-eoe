package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.magiciansred.*;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class MagiciansRedEntity extends StandEntity<MagiciansRedEntity, MagiciansRedEntity.State> {
    public static final RedirectAttack REDIRECT = new RedirectAttack(100, 7, 10, 0.75f);
    public static final SimpleAttack<MagiciansRedEntity> LIGHT = SimpleAttack.<MagiciansRedEntity>lightAttack(5, 8, 5f, 16, 0.75f, 0.75f, -0.1f)
            .withCrouchingVariant(REDIRECT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final KnockdownAttack<MagiciansRedEntity> HEAVY = new KnockdownAttack<MagiciansRedEntity>(280, 12, 22, 7f, 1.75f, 10, .5f, 1f, 0.6f, 40)
            .withSound(JSoundRegistry.MR_HEAVY)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withInfo(Text.literal("Low Kick"), Text.literal("medium windup knockdown"));
    public static final FlamethrowerAttack FLAMETHROWER = new FlamethrowerAttack(340, 0, 60, 0.4f, 0, 2, 0.25f, 0.75f, 0, 3)
            .withArmor(1)
            .withSound(JSoundRegistry.MR_BARRAGE)
            .withInfo(Text.literal("Flamethrower"), Text.literal("fast reliable damage cash-out tool, no stun, burns for 3 seconds"));
    public static final CrossfireAttack CROSSFIRE = new CrossfireAttack(240, 8, 10, 0.75f)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withInfo(Text.literal("Crossfire"), Text.literal("fires 3 stunning ankhs"));
    public static final CrossfireVariationAttack CROSSFIRE_VARIATION = new CrossfireVariationAttack(600, 12, 17, 0.75f)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withInfo(Text.literal("Crossfire Variation"), Text.literal("summons 6 ankhs that orbit around the user, crouch to increase orbit distance"));
    public static final CrossfireHurricaneAttack CROSSFIRE_HURRICANE = new CrossfireHurricaneAttack(1200, 18, 22, 0.75f)
            .withSound(JSoundRegistry.MR_ULT)
            .withInfo(Text.literal("Crossfire Hurricane"), Text.literal("summons slow, homing fire hurricane that knocks down, lasts for 3 seconds after hitting anything"));
    public static final RedBindAttack RED_BIND = new RedBindAttack(400, 12, 22, 0.75f, 3, 15, 1.5f, 0, 0)
            .withSound(JSoundRegistry.MR_REDBIND)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Red Bind"), Text.literal("on hit, wraps opponent in fiery rings that launch them in the direction they are hit"));
    public static final LifeDetectorAttack LIFE_DETECTOR = new LifeDetectorAttack(500, 13, 20, 0.75f)
            .withCrouchingVariant(RED_BIND)
            .withSound(JSoundRegistry.MR_REDBIND)
            .withInfo(Text.literal("Life Detector"), Text.literal("tracks down nearby life, lasts 15s"));

    public MagiciansRedEntity(World worldIn) {
        super(StandType.MAGICIANS_RED, worldIn, JSoundRegistry.MR_SUMMON);
        idleRotation = 225f;

        description = "Tailor-made, Blazing ZONER";

        pros = List.of(
                "incredible setups",
                "high damage",
                "two knockdowns"
        );

        cons = List.of(
                "easily blockable projectiles",
                "slower than average",
                "no mobility options",
                "no armored options"
        );

        freespace = """
                    PASSIVE: Fire Resistance
    
                    BNBs:
                        -the happy camper
                        M1>Low Kick>Variation/Life Detector
                        
                        -the "omg i have setups????"
                        M1>Red Bind>dash past enemy>Life Detector/Variation>any physical hit
                        
                        -the "this move is fire"
                        M1>Red Bind>Hurricane""";

        super.initialize();
    }

    @Override
    protected void registerMoves(MoveMap<MagiciansRedEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.LIGHT).withCrouchingVariant(State.REDIRECT);
        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveType.BARRAGE, FLAMETHROWER, State.BARRAGE);
        moves.register(MoveType.SPECIAL1, CROSSFIRE, State.CROSSFIRE);
        moves.register(MoveType.SPECIAL2, CROSSFIRE_VARIATION, State.CROSSFIRE_VARIATION);
        moves.register(MoveType.SPECIAL3, RED_BIND, State.RED_BIND);
        moves.register(MoveType.ULT, CROSSFIRE_HURRICANE, State.CROSSFIRE_HURRICANE);
        moves.register(MoveType.UTIL, LIFE_DETECTOR, State.DETECTOR).withCrouchingVariant(State.RED_BIND);
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;
        getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20, 0, true, false));
        CROSSFIRE_HURRICANE.tickHurricane(this);
    }

    // Animation code
    public enum State implements StandAnimationState<MagiciansRedEntity> {
        IDLE(builder -> builder.loop("animation.mr.idle")),
        LIGHT(builder -> builder.playAndHold("animation.mr.light")),
        BLOCK(builder -> builder.loop("animation.mr.block")),
        HEAVY(builder -> builder.playAndHold("animation.mr.heavy")),
        BARRAGE(builder -> builder.playAndHold("animation.mr.barrage")),
        CROSSFIRE(builder -> builder.playAndHold("animation.mr.crossfire")),
        CROSSFIRE_HURRICANE(builder -> builder.playAndHold("animation.mr.crossfirehurricane")),
        CROSSFIRE_VARIATION(builder -> builder.playAndHold("animation.mr.crossfirevariation")),
        REDIRECT(builder -> builder.playAndHold("animation.mr.redirect")),
        RED_BIND(builder -> builder.playAndHold("animation.mr.redbind")),
        DETECTOR(builder -> builder.playAndHold("animation.mr.detector"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MagiciansRedEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.mr.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
