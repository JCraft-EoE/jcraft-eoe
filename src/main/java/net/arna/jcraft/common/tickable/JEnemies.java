package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.*;

import static net.arna.jcraft.common.entity.stand.StandEntity.standUserAI;

/**
 * Stores and updates all MobEntities that use Stands.
 */
public class JEnemies {
    private static final HashMap<MobEntity, RegistryKey<World>> enemies = new HashMap<>();
    /**
     * A queue designed to prevent any ConcurrentModificationException.
     * Stores to-be JEnemies temporarily if they were attempted to be registered while {@link JEnemies#ticking} is true.
     */
    private static final PriorityQueue<MobEntity> queuedEnemies = new PriorityQueue<>();
    private static boolean ticking = false;

    public static void add(MobEntity entity) {
        if (enemies.containsKey(entity))
            return;

        if (ticking) {
            queuedEnemies.add(entity);
        } else {
            add(entity, entity.getWorld().getRegistryKey());
        }
    }
    public static void add(MobEntity entity, RegistryKey<World> registryKey) {
        enemies.put(entity, registryKey);
    }

    public static void tick(MinecraftServer server) {
        ticking = true;

        while (!queuedEnemies.isEmpty()) {
            MobEntity queued = queuedEnemies.peek();
            add(queued, queued.getWorld().getRegistryKey());
            queuedEnemies.remove();
        }

        Iterator<Map.Entry<MobEntity, RegistryKey<World>>> iter = enemies.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<MobEntity, RegistryKey<World>> enemyData = iter.next();
            MobEntity enemy = enemyData.getKey();

            if (!enemy.isAlive()) {
                iter.remove();
                continue;
            }

            ServerWorld world = server.getWorld(enemyData.getValue());

            if (enemy.isAiDisabled()) continue;
            StandComponent standComponent = JComponents.getStandData(enemy);
            if (standComponent.getType() != null) {
                StandEntity<?, ?> stand = standComponent.getStand();
                if (stand == null) {
                    JCraft.summon(world, enemy);
                } else {
                    LivingEntity target = enemy.getTarget();
                    if (target != null && target.isAlive()) {
                        standUserAI(enemy, target, stand);
                    } else {
                        // Targeting priority: top to bottom
                        LinkedList<LivingEntity> targets = new LinkedList<>();
                        targets.add(enemy.getPrimeAdversary());
                        var damageRec = enemy.getDamageTracker().getBiggestFall();
                        if (damageRec != null) {
                            var targetEntity = damageRec.damageSource().getAttacker();
                            if (targetEntity instanceof LivingEntity living) {
                                targets.add(living);
                            }
                        }

                        targets.add(enemy.getAttacker());
                        // Shouldn't use canTarget because that applies a PlayerEntity only filter.
                        targets.stream()
                                .filter(potentialTarget -> potentialTarget != null &&
                                        potentialTarget.isAlive() &&
                                        enemy.canSee(potentialTarget) &&
                                        potentialTarget.canTakeDamage())
                                .findFirst()
                                .ifPresent(
                                        selectedTarget -> {
                                            enemy.setTarget(selectedTarget);
                                            standUserAI(enemy, selectedTarget, stand);
                                        }
                                );
                    }
                }
            }
        }

        ticking = false;
    }
}
