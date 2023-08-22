package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public abstract sealed class AbstractStarPlatinumEntity<E extends AbstractStarPlatinumEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits StarPlatinumEntity, SPTWEntity {
    public static final SimpleAttack<AbstractStarPlatinumEntity<?, ?>> STAR_BREAKER = new SimpleAttack<AbstractStarPlatinumEntity<?, ?>>(
            340, 20, 30, 1f, 10f, 14, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.STAR_BREAKER)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withExtraHitBox(1.5)
            .withHyperArmor()
            .withLaunch()
            .withInfo(Text.literal("Star Breaker"), Text.literal("uninterruptible launcher"));

    protected AbstractStarPlatinumEntity(StandType type, World worldIn) {
        super(type, worldIn, JSoundRegistry.STAR_PLATINUM_SUMMON);
        idleRotation = 225f;

        description = "High Speed RUSHDOWN";

        pros = List.of(
                "fast m1",
                "long, damaging combos",
                "low cooldowns"
        );

        cons = List.of(
                "predictable playstyle",
                "weak ranged coverage",
                "weak mixups without inhale"
        );

        freespace =
                """
                        BNBs:
                        ~ represents a queued attack
                                                
                            -the classic
                            M1>Barrage>M1>Low Kick>Advancing Barrage~M1~Star Finger~Star Breaker
                            
                            -the blowback
                            Inhale>...>Star Finger>Star Breaker>Barrage>...

                            -the poke
                            Star Finger>Low Kick>M1>Advancing Barrage~M1>Barrage>M1>Star Breaker""";

        //moves = List.of(light, heavy, barrage, starfinger, inhale, lowkick, starfinger, jump);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }
}
