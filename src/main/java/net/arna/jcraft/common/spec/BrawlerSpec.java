package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class BrawlerSpec extends JSpec<BrawlerSpec, BrawlerSpec.State> {
    public static final SimpleAttack<BrawlerSpec> HEAVY = new SimpleAttack<BrawlerSpec>(340, 10,
            21, 1f, 6f, 15, 1.5f, 0.8f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withInfo(Text.literal("Uppercut"), Text.literal("uninterruptible, medium speed"));
    public static final SimpleMultiHitAttack<BrawlerSpec> COMBO = new SimpleMultiHitAttack<BrawlerSpec>(400,
            26, 1f, 4, 15, 1.5f, 0.6f, -0.1f, IntSet.of(5, 10, 19))
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withBlockStun(5)
            .withInfo(Text.literal("Combo"), Text.literal("hits 3 times, combo starter/extender"));
    public static final SimpleAttack<BrawlerSpec> GUT = new SimpleAttack<BrawlerSpec>(340, 11, 18,
            1f, 6f, 16, 1.5f, 0.8f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Gut Punch"), Text.literal("good stun"));
    public static final KnockdownAttack<BrawlerSpec> SWEEP = new KnockdownAttack<BrawlerSpec>(300, 11, 18,
            1f, 5f, 16, 1.5f, 0.6f, 0.65f, 25)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(Text.literal("SWEEP"), Text.literal("knocks down"));

    public BrawlerSpec(PlayerEntity player) {
        super(SpecType.BRAWLER, player);
    }

    @Override
    protected void registerMoves(MoveMap<BrawlerSpec, State> moves) {
        moves.register(MoveType.HEAVY, HEAVY, CooldownType.HEAVY, State.HEAVY);
        moves.register(MoveType.BARRAGE, COMBO, CooldownType.BARRAGE, State.COMBO);
        moves.register(MoveType.SPECIAL1, GUT, CooldownType.SPECIAL1, State.GUT);
        moves.register(MoveType.SPECIAL2, SWEEP, CooldownType.SPECIAL2, State.SWEEP);
    }

    @Override
    protected BrawlerSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<BrawlerSpec> {
        HEAVY("br.upct"),
        COMBO("br.3hit"),
        GUT("br.gut"),
        SWEEP("br.low");

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
