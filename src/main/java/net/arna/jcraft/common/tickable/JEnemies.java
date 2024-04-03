package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static net.arna.jcraft.common.entity.stand.StandEntity.standUserAI;

public class JEnemies {
    private static final HashMap<MobEntity, RegistryKey<World>> enemies = new HashMap<>();
    public static void add(MobEntity entity) {
        add(entity, entity.getWorld().getRegistryKey());
    }
    public static void add(MobEntity entity, RegistryKey<World> registryKey) {
        if (enemies.containsKey(entity))
            return;
        enemies.put(entity, registryKey);
    }

    public static void tick(MinecraftServer server) {
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
                    // Target priority
                    LivingEntity biggestAttacker = enemy.getDamageTracker().getBiggestAttacker();
                    LivingEntity primeAdversary = enemy.getPrimeAdversary();
                    LivingEntity target = enemy.getTarget();
                    if (primeAdversary != null && primeAdversary.isAlive() && stand.canTarget(primeAdversary))
                        standUserAI(enemy, primeAdversary, stand);
                    else if (target != null && target.isAlive() && stand.canTarget(target))
                        standUserAI(enemy, target, stand);
                    else if (biggestAttacker != null && biggestAttacker.isAlive() && stand.canTarget(biggestAttacker))
                        enemy.setTarget(biggestAttacker);
                }
            }
        }
    }
}
