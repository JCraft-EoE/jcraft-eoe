package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.attack.moves.shared.UppercutAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class BrawlerSpec extends JSpec<BrawlerSpec, BrawlerSpec.State> {
    public static final UppercutAttack<BrawlerSpec> HEAVY = new UppercutAttack<BrawlerSpec>(30, 10,
            21, 1f, 6f, 15, 1.5f, 0.3f, 0f, 0.3f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Uppercut"), Text.literal("medium speed"));
    public static final SimpleAttack<BrawlerSpec> TORNADO = new SimpleAttack<BrawlerSpec>(280, 12,
            20, 1f, 7f, 20, 1.6f, 0.4f, -0.1f)
            .withCrouchingVariant(HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withArmor(3)
            .withInfo(Text.literal("Tornado Kick"), Text.literal("3 points of armor, high stun"));
    public static final SimpleMultiHitAttack<BrawlerSpec> COMBO = new SimpleMultiHitAttack<BrawlerSpec>(360,
            26, 1f, 4, 15, 1.5f, 0.2f, -0.1f, IntSet.of(5, 10, 19))
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withBlockStun(5)
            .withInfo(Text.literal("Combo"), Text.literal("hits 3 times, combo starter/extender"));
    public static final SimpleAttack<BrawlerSpec> GUT = new SimpleAttack<BrawlerSpec>(120, 11, 18,
            1f, 6f, 16, 1.5f, 0.4f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(Text.literal("Gut Punch"), Text.literal("good stun"));
    public static final KnockdownAttack<BrawlerSpec> SWEEP = new KnockdownAttack<BrawlerSpec>(30, 11, 18,
            1f, 5f, 16, 1.5f, 0.6f, 0.85f, 25)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withStaticY()
            .withInfo(Text.literal("SWEEP"), Text.literal("knocks down"));
    public static final SimpleAttack<BrawlerSpec> LOW_KICK = new SimpleAttack<BrawlerSpec>(30, 6, 11,
            1f, 4f, 10, 1.25f, 0.15f, 0.35f)
            .withCrouchingVariant(SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withExtraHitBox(0.25, 0, 1)
            .withStaticY()
            .withHitAnimation(HitPropertyComponent.HitAnimation.LOW)
            .withInfo(Text.literal("Right Low Kick"), Text.literal("fast jab"));

    public BrawlerSpec(PlayerEntity player) {
        super(SpecType.BRAWLER, player);
    }

    @Override
    protected void registerMoves(MoveMap<BrawlerSpec, State> moves) {
        moves.register(MoveType.HEAVY, TORNADO, CooldownType.HEAVY, State.TORNADO).withCrouchingVariant(State.HEAVY);
        moves.register(MoveType.BARRAGE, COMBO, CooldownType.BARRAGE, State.COMBO);
        moves.register(MoveType.SPECIAL1, LOW_KICK, CooldownType.SPECIAL1, State.LOW_KICK).withCrouchingVariant(State.SWEEP);
        moves.register(MoveType.SPECIAL2, GUT, CooldownType.SPECIAL2, State.GUT);
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
