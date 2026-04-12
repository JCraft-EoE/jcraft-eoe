package net.arna.jcraft.common.ai;

import net.arna.jcraft.api.attack.enums.StunType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface IJAttackerBrain {
    int MIN_LEVEL = 0; // abysmal dogshit
    int BEGINNER_LEVEL = 3;
    int INTERMEDIATE_LEVEL = 10;
    int COMPETITIVE_LEVEL = 15;
    int MAX_LEVEL = 20; // GOAT

    static void planDefense(final AttackerBrainInfo info) {
        info.tick();

        if (info.getCombatCtx().getAttackerCtx().blocking()) {
            info.setState(AttackerBrainInfo.State.DEFENSE);
            return;
        }

        final MobEffectInstance stun = info.getCombatCtx().getAttackerCtx().stun();
        if (stun == null) return;

        if (stun.getAmplifier() == StunType.BLOCK.ordinal()) {
            info.setState(AttackerBrainInfo.State.DEFENSE);
        } else {
            info.setState(AttackerBrainInfo.State.COMBOED);
        }
    }

    static void pacing(final AttackerBrainInfo info, final CombatInstantContext combatCtx) {
        final int aiLevel = info.getAiLevel();
        final RandomSource random = combatCtx.getAttackerCtx().entity().getRandom();

        if (aiLevel < IJAttackerBrain.INTERMEDIATE_LEVEL) { // [9, 0]
            final float chanceToDoNothing = (IJAttackerBrain.INTERMEDIATE_LEVEL - aiLevel) * 0.11f;
            if (random.nextFloat() > chanceToDoNothing) {
                info.setDesiredNoAttackTime(random.nextInt(20 - aiLevel));
            }
        }
    }

    @Nullable
    static CombatInstantContext target(Mob mob, AttackerBrainInfo info) {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            target = mob.getLastAttacker();
        }
        if (target == null || (target instanceof ServerPlayer player && (player.isCreative() || player.isSpectator()))) {
            return null;
        }

        final CombatInstantContext combatCtx = info.getCombatCtx();
        combatCtx.update(mob, target);
        return combatCtx;
    }

    static int reactionTimeFor(@Range(from = MIN_LEVEL, to = MAX_LEVEL) int aiLevel, RandomSource random) {
        int reactionTime = 10; // 0.5s base
        reactionTime -= aiLevel / 5; // 0.3s at max

        if (aiLevel < MAX_LEVEL) reactionTime += Math.round((random.nextFloat() * 2.0f) * (MAX_LEVEL - aiLevel) / MAX_LEVEL);

        return reactionTime;
    }
}
