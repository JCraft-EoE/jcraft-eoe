package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ImbueItemAttack extends AbstractMove<ImbueItemAttack, SpeedKingEntity> {
    private static final double CONTACT_KNOCKBACK = 1.2;
    private static final double UPWARD_KNOCKBACK = 0.3;
    private static final double SEARCH_RADIUS = 5.0;
    private static final int FIRE_DURATION = 1;
    private static final double CONTACT_INFLATE = 0.4;

    private final int heatDuration;
    private final float contactDamage;
    private final int boilingDuration;

    private static final Set<UUID> HEATED_ITEM_IDS = new HashSet<>();
    private static final Set<UUID> TRIGGERED_ITEMS = new HashSet<>();
    private static final Map<UUID, ImbueConfig> ITEM_CONFIGS = new HashMap<>();

    public ImbueItemAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final int heatDuration, final float contactDamage, final int boilingDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.heatDuration = heatDuration;
        this.contactDamage = contactDamage;
        this.boilingDuration = boilingDuration;
    }

    @Override
    public @NonNull MoveType<ImbueItemAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();
        imbueItems(attacker.level(), attacker.position(), user);
        return Set.of();
    }

    private void imbueItems(Level world, Vec3 centerPos, LivingEntity user) {
        AABB searchArea = new AABB(
                centerPos.add(-SEARCH_RADIUS, -3, -SEARCH_RADIUS),
                centerPos.add(SEARCH_RADIUS, 3, SEARCH_RADIUS));

        for (ItemEntity item : world.getEntitiesOfClass(ItemEntity.class, searchArea, EntitySelector.ENTITY_STILL_ALIVE)) {
            item.getItem().getOrCreateTag().putBoolean("SpeedKingHeated", true);
            item.getItem().getOrCreateTag().putLong("HeatedTime", world.getGameTime());
            item.getItem().getOrCreateTag().putUUID("SpeedKingUser", user.getUUID());
            item.setSecondsOnFire(FIRE_DURATION);
            HEATED_ITEM_IDS.add(item.getUUID());
            TRIGGERED_ITEMS.remove(item.getUUID());
            ITEM_CONFIGS.put(item.getUUID(), new ImbueConfig(heatDuration, contactDamage, boilingDuration));

            if (world instanceof ServerLevel serverLevel) {
                Vec3 pos = item.position();
                for (int i = 0; i < 8; i++) {
                    double ox = (world.random.nextDouble() - 0.5) * 0.8;
                    double oy = world.random.nextDouble() * 0.6;
                    double oz = (world.random.nextDouble() - 0.5) * 0.8;
                    serverLevel.sendParticles(ParticleTypes.FLAME, pos.x + ox, pos.y + oy, pos.z + oz,
                            1, 0.0, 0.05, 0.0, 0.05);
                }
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 0.2, pos.z,
                        3, 0.3, 0.1, 0.3, 0.01);
            }
        }

        imbueBlocks(world, centerPos);
    }

    private void imbueBlocks(Level world, Vec3 centerPos) {
        final int radius = 3;
        final BlockPos center = new BlockPos((int) centerPos.x, (int) centerPos.y, (int) centerPos.z);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
                        world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
                    }
                }
            }
        }
    }

    public static void tickImbuedItems(Level level) {
        if (level.isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level;

        HEATED_ITEM_IDS.removeIf(uuid -> {
            var entity = serverLevel.getEntity(uuid);
            if (!(entity instanceof ItemEntity item) || item.isRemoved()) {
                TRIGGERED_ITEMS.remove(uuid);
                ITEM_CONFIGS.remove(uuid);
                return true;
            }

            ImbueConfig cfg = ITEM_CONFIGS.getOrDefault(uuid, ImbueConfig.DEFAULT);

            long heatedAt = item.getItem().getOrCreateTag().getLong("HeatedTime");
            if (level.getGameTime() - heatedAt > cfg.heatDuration) {
                item.getItem().getOrCreateTag().putBoolean("SpeedKingHeated", false);
                TRIGGERED_ITEMS.remove(uuid);
                ITEM_CONFIGS.remove(uuid);
                return true;
            }

            if (TRIGGERED_ITEMS.contains(uuid)) return false;

            UUID ownerUUID = item.getItem().hasTag()
                    ? (item.getItem().getOrCreateTag().hasUUID("SpeedKingUser")
                        ? item.getItem().getOrCreateTag().getUUID("SpeedKingUser")
                        : null)
                    : null;

            AABB contactBox = item.getBoundingBox().inflate(CONTACT_INFLATE);
            List<LivingEntity> touching = level.getEntitiesOfClass(LivingEntity.class, contactBox,
                    e -> e.isAlive() && !e.isSpectator() && !e.getUUID().equals(ownerUUID));

            if (!touching.isEmpty()) {
                Vec3 itemPos = item.position();
                for (LivingEntity target : touching) {
                    target.hurt(target.damageSources().magic(), cfg.contactDamage);
                    Vec3 kb = target.position().subtract(itemPos).normalize().scale(CONTACT_KNOCKBACK);
                    target.setDeltaMovement(target.getDeltaMovement().add(kb.x, UPWARD_KNOCKBACK, kb.z));
                    target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), cfg.boilingDuration, 1, false, true));
                }

                TRIGGERED_ITEMS.add(uuid);
                item.getItem().getOrCreateTag().putBoolean("SpeedKingHeated", false);
            }

            return false;
        });
    }

    @Override
    protected @NonNull ImbueItemAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ImbueItemAttack copy() {
        return copyExtras(new ImbueItemAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                heatDuration, contactDamage, boilingDuration));
    }

    public record ImbueConfig(int heatDuration, float contactDamage, int boilingDuration) {
        public static final ImbueConfig DEFAULT = new ImbueConfig(300, 6.0f, 200);
    }

    public static class Type extends AbstractMove.Type<ImbueItemAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ImbueItemAttack>, ImbueItemAttack> buildCodec(RecordCodecBuilder.Instance<ImbueItemAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new ImbueItemAttack(cd, wu, dur, md, 300, 6.0f, 200));
        }
    }
}
