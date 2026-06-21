package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.SpecData;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleUppercutAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class BrawlerSpec extends JSpec<BrawlerSpec, BrawlerSpec.State> {
    public static final MoveSet<BrawlerSpec, State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.BRAWLER, BrawlerSpec::registerMoves, State.class);
    public static final SpecData DATA = SpecData.builder()
            .name(Component.translatable("spec.jcraft.brawler"))
            .description(Component.literal("Close-range pressure and combo extension tool"))
            .details(Component.literal("""
                    Important hitconfirm: (any stand move)~stand.OFF>Combo>stand.ON+(any stand move)"""))
            .build();

    public static final SimpleUppercutAttack<BrawlerSpec> HEAVY = new SimpleUppercutAttack<BrawlerSpec>(0, 10,
            21, 1f, 6f, 15, 1.5f, 0.3f, 0f, 0.3f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Component.literal("Uppercut"), Component.literal("Medium speed, good stun, launches up on hit."));

    public static final SimpleAttack<BrawlerSpec> TORNADO = new SimpleAttack<BrawlerSpec>(200, 12,
            20, 1f, 7f, 20, 1.6f, 0.85f, -0.1f)
            .withCrouchingVariant(HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withArmor(3)
            .withLaunch()
            .withInfo(Component.literal("Tornado Kick"), Component.literal("3 points of armor, high stun, launches away on hit."));

    public static final SimpleMultiHitAttack<BrawlerSpec> COMBO = new SimpleMultiHitAttack<BrawlerSpec>(360,
            26, 1f, 4f, 15, 1.5f, 0.2f, -0.1f, IntSet.of(5, 10, 19))
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withBlockStun(5)
            .withInfo(Component.literal("Combo"), Component.literal("Hits 3 times, vital combo starter/extender."));

    public static final SimpleAttack<BrawlerSpec> GUT = new SimpleAttack<BrawlerSpec>(0, 11, 18,
            1f, 6f, 13, 1.5f, 0.9f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(Component.literal("Gut Punch"), Component.literal("Good stun, slow startup."));

    public static final KnockdownAttack<BrawlerSpec> SWEEP = new KnockdownAttack<BrawlerSpec>(0, 9, 16,
        1f, 5f, 16, 1.5f, 0.6f, 0.85f, 25)
        .withImpactSound(JSoundRegistry.IMPACT_2)
        .withStaticY()
        .withInfo(Component.literal("Sweep"), Component.literal("Knocks down."));

    public static final SimpleAttack<BrawlerSpec> LOW_KICK = new SimpleAttack<BrawlerSpec>(0, 6, 11,
            1f, 4f, 10, 1.25f, 0.15f, 0.35f)
            .withCrouchingVariant(SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withExtraHitBox(0.25, 0, 1)
            .withStaticY()
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(Component.literal("Right Low Kick"), Component.literal("Fast, short-ranged jab."));

    public BrawlerSpec(LivingEntity livingEntity) {
        super(JSpecTypeRegistry.BRAWLER.get(), livingEntity);
    }

    private static void registerMoves(MoveMap<BrawlerSpec, State> moves) {
        moves.register(MoveClass.HEAVY, TORNADO, CooldownType.HEAVY, State.TORNADO).withCrouchingVariant(State.HEAVY);
        moves.register(MoveClass.BARRAGE, COMBO, CooldownType.BARRAGE, State.COMBO);
        moves.register(MoveClass.SPECIAL1, LOW_KICK, CooldownType.SPECIAL1, State.LOW_KICK).withCrouchingVariant(State.SWEEP);
        moves.register(MoveClass.SPECIAL2, GUT, CooldownType.SPECIAL2, State.GUT);
    }

    @Override
    public BrawlerSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<BrawlerSpec> {
        HEAVY("br.upct"),
        TORNADO("br.kck"),
        COMBO("br.3hit"),
        GUT("br.gut"),
        SWEEP("br.low"),
        LOW_KICK("br.lkk");

        private final String key;

        State(String key) {
            this.key = key;
        }

        @Override
        public String getKey(BrawlerSpec spec) {
            return key;
        }
    }
}
