package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.starplatinum.BlockBreakingAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public abstract sealed class AbstractStarPlatinumEntity<E extends AbstractStarPlatinumEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits StarPlatinumEntity, SPTWEntity {
    public static final BlockBreakingAttack<AbstractStarPlatinumEntity<?, ?>> GROUND_BREAKER = new BlockBreakingAttack<AbstractStarPlatinumEntity<?, ?>>(
            0, 20, 30, 1f, 10f, 12, 2f, 1.5f, 0.5f)
            .withSound(JSoundRegistry.STAR_BREAKER)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withExtraHitBox(1.5)
            .withBlockStun(9)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.translatable("jcraft.starplatinum.crhvy"),
                    Component.literal("slow, uninterruptible launcher, breaks the ground")
            );
    public static final SimpleAttack<AbstractStarPlatinumEntity<?, ?>> STAR_BREAKER = new SimpleAttack<AbstractStarPlatinumEntity<?, ?>>(
            0, 20, 30, 1f, 10f, 14, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.STAR_BREAKER)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withExtraHitBox(1.5)
            .withBlockStun(9)
            .withHyperArmor()
            .withLaunch()
            .withCrouchingVariant(GROUND_BREAKER)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.hvy"),
                    Component.literal("slow, uninterruptible launcher")
            );

    protected AbstractStarPlatinumEntity(StandType type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) {
            return;
        }
        super.desummon();
    }
}
