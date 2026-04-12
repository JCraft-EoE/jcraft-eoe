package net.arna.jcraft.common.attack.moves.vampire;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.spec.VampireSpec;
import net.arna.jcraft.common.tickable.Revivables;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class ReviveMove extends AbstractMove<ReviveMove, VampireSpec> {
    public ReviveMove(final int cooldown, final int windup, final int duration, final float reviveDistance) {
        super(cooldown, windup, duration, reviveDistance);
    }

    @Override
    public @NonNull MoveType<ReviveMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final VampireSpec attacker, final LivingEntity user) {
        final MinecraftServer server = user.getServer();
        if (server == null) {
            return Set.of();
        }
        final ServerLevel serverWorld = server.getLevel(user.level().dimension());
        if (serverWorld == null) {
            return Set.of();
        }

        for (Revivables.ReviveData revivable : Revivables.getAround(user.position(), getMoveDistance())) {
            EntityType<?> entityType = revivable.getType();

            // Convert Testificates to zombie Testificates
            if (entityType.is(EntityTypeTags.RAIDERS) || entityType.equals(EntityType.VILLAGER)) {
                entityType = EntityType.ZOMBIE_VILLAGER;
            }
            // Humans to zombies
            if (entityType.equals(EntityType.PLAYER)) {
                entityType = EntityType.ZOMBIE;
            }

            Entity entity = entityType.create(serverWorld);
            if (entity instanceof final LivingEntity living) {
                living.skipDropExperience();
                if (isBoss(living)) {
                    continue;
                }
                if (living.isInvertedHealAndHarm()) {
                    entity.setPos(revivable.getPos());
                    entity.tickCount = 1;
                    if (user instanceof final ServerPlayer serverPlayer) {
                        JComponentPlatformUtils.getMiscData(living).setSlavedTo(serverPlayer.getUUID());
                        serverPlayer.awardStat(JStatRegistry.VAMPIRE_REVIVES.get());
                    }
                    serverWorld.addFreshEntity(entity);
                    Revivables.removeRevivable(revivable);
                }
            }
        }
        return Set.of();
    }

    public static boolean isBoss(final LivingEntity living) {
        //todo: find a better way to check if smth is a boss (possibly using ServerBossEvent)
        return living.getMaxHealth() >= 80.0f;
    }

    @Override
    protected @NonNull ReviveMove getThis() {
        return this;
    }

    @Override
    public @NonNull ReviveMove copy() {
        return copyExtras(new ReviveMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<ReviveMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ReviveMove>, ReviveMove> buildCodec(RecordCodecBuilder.Instance<ReviveMove> instance) {
            return baseDefault(instance, ReviveMove::new);
        }
    }
}
