package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonSpinComponentImpl implements CommonSpinComponent {

    protected final LivingEntity entity;

    @Getter
    protected Byte slot;

    protected final List<ResourceLocation> observedNatureBlocks = new ArrayList<>();
    protected final List<ResourceLocation> observedNatureEntities = new ArrayList<>();

    protected CommonSpinComponentImpl(final @NonNull LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setSlot(final Byte slot) {
        this.slot = slot;
        sync(entity);
    }

    @Override
    public void cancelSpinning() {
        this.slot = null;
        sync(entity);
    }

    @Override
    public void endSpinning() {
        this.slot = null;
        sync(entity);
    }

    public void observeNature(final HitResult observation, final LevelReader level) {
        if (observation.getType() == HitResult.Type.BLOCK) {
            final BlockState blockState = level.getBlockState(((BlockHitResult)observation).getBlockPos());
            final var keyOpt = level.registryAccess().registryOrThrow(Registries.BLOCK).getResourceKey(blockState.getBlock());
            if (keyOpt.isPresent() && blockState.is(JTagRegistry.OBSERVABLE_NATURE_BLOCKS)) {
                final ResourceLocation location = keyOpt.get().location();
                if (!observedNatureBlocks.contains(location)) {
                    observedNatureBlocks.add(location);
                    if (entity instanceof final ServerPlayer player) {
                        JAdvancementTriggerRegistry.OBSERVED_NATURE.trigger(player);
                        player.sendSystemMessage(Component.translatable("jcraft.spin.nature_observation.succeeded"));
                    }
                }
                else if (entity instanceof final ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable("jcraft.spin.nature_observation.repeated"));
                }
            }
        }
        else if (observation.getType() == HitResult.Type.ENTITY) {
            final EntityType<?> entityType = ((EntityHitResult)observation).getEntity().getType();
            final var keyOpt = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getResourceKey(entityType);
            if (keyOpt.isPresent() && entityType.is(JTagRegistry.OBSERVABLE_NATURE_ENTITIES)) {
                final ResourceLocation location = keyOpt.get().location();
                if (!observedNatureEntities.contains(location)) {
                    observedNatureEntities.add(location);
                    if (entity instanceof ServerPlayer player) {
                        JAdvancementTriggerRegistry.OBSERVED_NATURE.trigger(player);
                        player.sendSystemMessage(Component.translatable("jcraft.spin.nature_observation.succeeded"));
                    }
                }
                else if (entity instanceof final ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable("jcraft.spin.nature_observation.repeated"));
                }
            }
        }
        else if (entity instanceof final ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("jcraft.spin.nature_observation.failed"));
        }
    }

    @Override
    public int natureObservationAmount() {
        return observedNatureBlocks.size() + observedNatureEntities.size();
    }

    @Override
    public void resetNatureObservations() {
        observedNatureBlocks.clear();
        observedNatureEntities.clear();
    }

    @Override
    public void goldenRatioC(final int amount) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100)); // TODO replace placeholder
    }

    @Override
    public void goldenRatioB(final int amount) {
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100)); // TODO replace placeholder
    }

    @Override
    public void goldenRatioA(final int amount) {
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100)); // TODO replace placeholder
    }

    public void tick() {
        // check the current selected slot for changes
        if (entity instanceof final Player player) {
            if (slot != null) {
                // if slot was in hotbar and changed, cancel spinning
                if (slot != Inventory.SLOT_OFFHAND && slot != (byte)player.getInventory().selected) {
                    cancelSpinning();
                }
                // else if slot was in offhand and mainhand is now hovering over valid spin item, switch
                else if (slot == Inventory.SLOT_OFFHAND) {
                    final ItemStack switcharoo = player.getMainHandItem();
                    if (!switcharoo.isEmpty() && !switcharoo.is(JTagRegistry.UNTHROWABLE) && switcharoo.getCount() == 1) {
                        setSlot((byte)player.getInventory().selected);
                    }
                }
            }
        }
        // actual spinning tick
        if (slot != null) {
            JCraft.LOGGER.info("Spinning: " + (slot == Inventory.SLOT_OFFHAND ? entity.getOffhandItem() : entity.getMainHandItem()).getItem());
        }
    }

    public void sync(final Entity entity) {
        // intentionally left empty
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return true;
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        buf.writeBoolean(slot != null);
        if (slot != null) {
            buf.writeByte(slot);
        }
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            slot = buf.readByte();
        }
        else {
            slot = null;
        }
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        observedNatureBlocks.clear();
        observedNatureEntities.clear();
        final ListTag blockList = tag.getList("ObservedBlocks", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < blockList.size(); i++) {
            final CompoundTag block = blockList.getCompound(i);
            final String location = block.getString("Location");
            if (!location.isEmpty()) {
                final ResourceLocation rl = ResourceLocation.tryParse(location);
                if (rl != null && !observedNatureBlocks.contains(rl)) {
                    observedNatureBlocks.add(rl);
                }
            }
        }
        final ListTag entityList = tag.getList("ObservedEntities", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entityList.size(); i++) {
            final CompoundTag entity = entityList.getCompound(i);
            final String location = entity.getString("Location");
            if (!location.isEmpty()) {
                final ResourceLocation rl = ResourceLocation.tryParse(location);
                if (rl != null && !observedNatureEntities.contains(rl)) {
                    observedNatureEntities.add(rl);
                }
            }
        }
        if (entity instanceof final ServerPlayer player) {
            JAdvancementTriggerRegistry.OBSERVED_NATURE.trigger(player);
        }
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        final ListTag blockList = new ListTag();
        for (final ResourceLocation observedNatureBlock : observedNatureBlocks) {
            final CompoundTag location = new CompoundTag();
            location.putString("Location", observedNatureBlock.toString());
            blockList.add(location);
        }
        tag.put("ObservedBlocks", blockList);
        final ListTag entityList = new ListTag();
        for (final ResourceLocation observedNatureEntity : observedNatureEntities) {
            final CompoundTag location = new CompoundTag();
            location.putString("Location", observedNatureEntity.toString());
            entityList.add(location);
        }
        tag.put("ObservedEntities", entityList);
    }

}
