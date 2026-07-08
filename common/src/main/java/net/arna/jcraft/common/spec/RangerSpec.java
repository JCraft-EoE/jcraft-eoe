package net.arna.jcraft.common.spec;

import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.SpecData;
import net.arna.jcraft.common.attack.moves.ranger.RangerHolsterMove;
import net.arna.jcraft.common.attack.moves.ranger.RangerRollMove;
import net.arna.jcraft.common.attack.moves.ranger.RangerSlideMove;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleUppercutAttack;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class RangerSpec extends JSpec<RangerSpec, RangerSpec.State> {
    public static final MoveSet<RangerSpec, State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.RANGER, RangerSpec::registerMoves, RangerSpec.class, State.class);
    public static final SpecData DATA = SpecData.builder()
            .name(Component.translatable("spec.jcraft.ranger"))
            .description(Component.literal("Mobile skirmisher"))
            .details(Component.literal("""
                    Roll and Slide let you weave through fights and reposition.
                    Slide carries hit enemies, launching them up for juggles."""))
            .build();

    public static final SimpleAttack<RangerSpec> BUTTSTROKE = new SimpleAttack<RangerSpec>(80, 8, 13,
            1f, 6f, 18, 1.5f, 0.3f, 0.5f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Buttstroke"),
                    Component.literal("Heavy downwards blow with high stun. Slower, longer cooldown.")
            );
    public static final SimpleUppercutAttack<RangerSpec> SHOULDER_BASH = new SimpleUppercutAttack<RangerSpec>(30, 7, 14,
            1f, 5f, 14, 1.5f, 0.2f, 0f, 0.4f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withArmor(1)
            .withStaticY()
            .withInfo(
                    Component.literal("Shoulder Bash"),
                    Component.literal("Springs up from a squat, launching enemies. 1 point of armor.")
            );
    public static final SimpleAttack<RangerSpec> WHIP = new SimpleAttack<RangerSpec>(15, 5, 8,
            1f, 4f, 7, 1.25f, 0.2f, 0f)
            .withAerialVariant(BUTTSTROKE)
            .withCrouchingVariant(SHOULDER_BASH)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Whip"),
                    Component.literal("Fast strike with the held item.")
            );

    public static final RangerRollMove ROLL = new RangerRollMove(100, 0, 20, 0f)
            .withInfo(
                    Component.literal("Roll"),
                    Component.literal("Combat roll that greatly reduces your hitbox and negates fall damage, but exhausts you at the end.")
            );

    public static final RangerSlideMove SLIDE = new RangerSlideMove(140, 0, 30, 0f, 10)
            .withCrouchingVariant(ROLL)
            .withInfo(
                    Component.literal("Slide"),
                    Component.literal("Held. Fast crawl that carries hit enemies, launching them up for juggles.")
            );

    public static final RangerHolsterMove HOLSTER = new RangerHolsterMove(10, 0, 0, 0f)
            .withInfo(
                    Component.literal("Holster"),
                    Component.literal("Stows the held non-stackable item, or draws the stowed one. Swaps if holding another.")
            );

    public RangerSpec(LivingEntity livingEntity) {
        super(JSpecTypeRegistry.RANGER.get(), livingEntity);
    }

    private static void registerMoves(MoveMap<RangerSpec, State> moves) {
        MoveMap.Entry<RangerSpec, State> hvy = moves.register(MoveClass.HEAVY, WHIP, CooldownType.HEAVY, State.WHIP);
        hvy.withCrouchingVariant(State.BASH);
        hvy.withAerialVariant(State.BUTTSTROKE);

        moves.register(MoveClass.BARRAGE, SLIDE, CooldownType.BARRAGE, State.SLIDE_START)
                .withCrouchingVariant(State.ROLL);

        moves.register(MoveClass.SPECIAL2, HOLSTER, CooldownType.SPECIAL2, null);
    }

    @Override
    public RangerSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<RangerSpec> {
        WHIP("whip"),
        BUTTSTROKE("buttstroke"),
        BASH("bash"),
        ROLL("roll"),
        SLIDE_START("slide_start"),
        SLIDE_LOOP("slide_loop"),
        ;

        private final String key;

        State(String key) {
            this.key = key;
        }

        @Override
        public String getKey(RangerSpec spec) {
            return key;
        }
    }
}
