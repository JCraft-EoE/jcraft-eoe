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
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class SpinSpec extends JSpec<SpinSpec, SpinSpec.State> {
    public static final MoveSet<SpinSpec, SpinSpec.State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.SPIN, SpinSpec::registerMoves, SpinSpec.class, SpinSpec.State.class);
    public static final SpecData DATA = SpecData.builder()
            .name(Component.translatable("spec.jcraft.spin"))
            .description(Component.literal("Oppressive long-range"))
            .details(Component.literal("""
                    i cant keep calling them "tricksters" because 80% of jojo is just making your ability to weird shit"""))
            .build();

    public static final SimpleAttack<SpinSpec> KICK = new SimpleAttack<SpinSpec>(0, 8, 14,
            1f, 5.5f, 7, 1.4f, 0.85f, 0.0f)
            .withAnim(State.KICK)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withLaunch()
            .withInfo(Component.literal("Kick"), Component.literal("Fast, short-ranged jab."));

    public static final SimpleAttack<SpinSpec> KNEE = new SimpleAttack<SpinSpec>(0, 6, 11,
            1f, 4f, 10, 1.25f, 0.15f, 0.35f)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withExtraHitBox(0.25, 0, 1)
            .withStaticY()
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withFollowupFrame(5)
            .withFollowup(KICK)
            .withInfo(Component.literal("Knee"), Component.literal("Fast, short-ranged jab."));

    public SpinSpec(LivingEntity livingEntity) {
        super(JSpecTypeRegistry.SPIN.get(), livingEntity);
    }

    private static void registerMoves(MoveMap<SpinSpec, SpinSpec.State> moves) {
        moves.registerImmediate(MoveClass.HEAVY, KNEE, State.KNEE);
    }

    @Override
    public SpinSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<SpinSpec> {
        KNEE("sp.kn"),
        KICK("sp.kk"),

        ;

        private final String key;

        State(String key) {
            this.key = key;
        }

        @Override
        public String getKey(SpinSpec spec) {
            return key;
        }
    }
}
