package net.arna.jcraft.common.attack.moves.vampire;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.tickable.Revivables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public class ReviveMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<ReviveMove<A>, A> {
    public ReviveMove(int cooldown, int windup, int duration, float reviveDistance) {
        super(cooldown, windup, duration, reviveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        MinecraftServer server = user.getServer();
        assert server != null;
        ServerWorld serverWorld = server.getWorld(user.getWorld().getRegistryKey());
        assert serverWorld != null;

        for (Revivables.ReviveData revivable : Revivables.getAround(user.getPos(), getMoveDistance())) {
            EntityType<?> entityType = revivable.getType();

            // Convert Testificates to zombie Testificates
            if (entityType.isIn(EntityTypeTags.RAIDERS) || entityType.equals(EntityType.VILLAGER))
                entityType = EntityType.ZOMBIE_VILLAGER;
            // Humans to zombies
            if (entityType.equals(EntityType.PLAYER))
                entityType = EntityType.ZOMBIE;

            Entity entity = entityType.create(serverWorld);
            if (entity instanceof LivingEntity living)
                if (living.isUndead()) {
                    entity.setPosition(revivable.getPos());
                    entity.age = 1;
                    if (user instanceof ServerPlayerEntity serverPlayer)
                        JComponents.getMiscData(living).setSlavedTo(serverPlayer.getUuid());
                    if (!isBoss(living)) {
                        serverWorld.spawnEntity(entity);
                        Revivables.removeRevivable(revivable);
                    }
                }
        }
        return Set.of();
    }

    public static boolean isBoss(LivingEntity living) {
        //todo: find a better way to check if smth is a boss
        return living.getMaxHealth() >= 80.0f;
    }

    @Override
    protected @NonNull ReviveMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ReviveMove<A> copy() {
        return copyExtras(new ReviveMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
