package net.arna.jcraft.common.tickable;

import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.spec.SpecTypeUtil;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.ai.AttackerBrainInfo;
import net.arna.jcraft.common.ai.brain.SpecAttackerBrain;
import net.arna.jcraft.common.ai.brain.StandAttackerBrain;
import net.arna.jcraft.common.ai.brain.StandSpecAttackerBrain;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.Mob;

import java.util.Map;

/**
 * Stores and updates all MobEntities that use Stands.
 */
public class JEnemies {
    /**
     * A Map of Stand and Spec types to their relevant IJAttackerBrain
     * Each IJAttackerBrain instance is a static processor which must be fed instance data
     */
    private static final TickableHashMap<Mob, AttackerBrainInfo> enemies = new TickableHashMap<>();

    public static void add(Mob entity) {
        if (entity.level().isClientSide()) {
            throw new UnsupportedOperationException("Attempted to add an enemy to JEnemies from the clientside!");
        }
        if (entity.getType().is(JTagRegistry.NO_STAND_USER_AI)) {
            return;
        }
        if (enemies.containsKey(entity)) {
            return;
        }

        add(entity, new AttackerBrainInfo(JServerConfig.BASE_AI_LEVEL.getValue()));
    }

    public static void add(Mob entity, AttackerBrainInfo info) {
        enemies.add(entity, info);
    }

    public static void tick() {
        enemies.tick(iter -> {
            final Map.Entry<Mob, AttackerBrainInfo> enemyData = iter.next();
            final Mob mob = enemyData.getKey();

            if (mob.isAlive()) {
                if (mob.isNoAi()) return;

                final var info = enemyData.getValue();
                if (info.getAiLevel() <= 0) return;

                final var standType = JComponentPlatformUtils.getStandComponent(mob).getType();
                final var specType = JComponentPlatformUtils.getSpecData(mob).getType();

                final boolean hasSpec = !SpecTypeUtil.isNone(specType);
                final boolean hasStand = !StandTypeUtil.isNone(standType);

                if (hasStand && hasSpec) StandSpecAttackerBrain.tick(mob, info);
                else if (hasStand) StandAttackerBrain.tick(mob, info);
                else if (hasSpec) SpecAttackerBrain.tick(mob, info);
                else iter.remove();
            } else {
                iter.remove();
            }
        });
    }

    public static boolean contains(Mob mob) {
        return enemies.containsKey(mob);
    }
}
