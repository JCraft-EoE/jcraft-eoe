package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.UppercutAttack;
import net.arna.jcraft.common.attack.moves.starplatinum.BlockBreakingAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static net.arna.jcraft.registry.JStatusRegistry.PHPOISON;

public abstract sealed class AbstractPurpleHazeEntity<E extends AbstractPurpleHazeEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits PurpleHazeDistortionEntity {
    public static final KnockdownAttack<AbstractPurpleHazeEntity<?, ?>> BACKHAND_FOLLOWUP = new KnockdownAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 13, 20, 0.75f, 6f, 13, 1.75f, 0.5f, 0.35f, 35)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Hammerfist"), Text.literal("knockdown"));
    public static final UppercutAttack<AbstractPurpleHazeEntity<?, ?>> BACKHAND = new UppercutAttack<AbstractPurpleHazeEntity<?, ?>>(20,
            6, 14, 0.75f, 6f, 20, 1.5f, 0.25f, -0.6f, 0.75f)
            .withTargetPostProcessor((attacker, target, kbVec, damageSource, blocking) -> {
                if (!blocking)
                    attacker.infect(target, 5 * 20);
            })
            .withFollowup(BACKHAND_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withExtraHitBox(0, 0.35, 1.25)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(Text.literal("Backhand"), Text.literal("launches vertically, infects (5s) on hit"));

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT_FOLLOWUP = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            0, 9, 20, 0.75f, 6f, 13, 1.6f, 1.25f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(Text.literal("Kick"), Text.literal("fast combo finisher"));

    public static final SimpleAttack<AbstractPurpleHazeEntity<?, ?>> LIGHT = new SimpleAttack<AbstractPurpleHazeEntity<?, ?>>(
            30, 6, 9, 0.75f, 5f, 10, 1.5f, 0.25f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BACKHAND)
            .withInfo(Text.literal("Punch"), Text.literal("fast combo starter"));

    public static final MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>> BARRAGE = new MainBarrageAttack<AbstractPurpleHazeEntity<?, ?>>(280,
            0, 40, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.getHardness())
            //.withSound(JSoundRegistry.STAR_PLATINUM_BARRAGE)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));

    public static void infect(LivingEntity target, int ticks) {
        StatusEffectInstance instance = target.getStatusEffect(PHPOISON);
        if (instance != null)
            target.addStatusEffect(new StatusEffectInstance(PHPOISON, instance.getDuration() + ticks));
        else
            target.addStatusEffect(new StatusEffectInstance(PHPOISON, ticks));
    }

    protected AbstractPurpleHazeEntity(StandType type, World worldIn) {
        super(type, worldIn, JSoundRegistry.STAR_PLATINUM_SUMMON);
        idleRotation = 225f;

        description = "High Speed RUSHDOWN";

        pros = List.of(
        );

        cons = List.of(
        );

        freespace =
                """
                        BNBs:
                        """;
    }
}
