package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.events.JPlayerEntityEvents;
import net.arna.jcraft.common.events.JServerTickEvents;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.arna.jcraft.common.util.EntityInterest;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import static net.arna.jcraft.common.util.EntityInterest.blockAttractionInterest;
import static net.arna.jcraft.common.util.EntityInterest.itemAttractionInterest;

public interface JEventsRegistry {
    static void registerEvents() {
        ServerEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> {
                    // If an item was spawned
                    if (entity instanceof ItemEntity item) {
                        ItemStack stack = item.getStack();

                        if (stack.isOf(JObjectRegistry.ANUBIS)) {
                            item.setPickupDelay(0);
                            return;
                        }

                        if (stack.isOf(JObjectRegistry.FVREVOLVER)) {
                            JCraft.markItemOfInterest(item, EntityInterest.itemAttractionInterest(JObjectRegistry.FVREVOLVER));
                            return;
                        }

                        // ... in the AU
                        if (world.getRegistryKey().equals(JDimensionRegistry.AU_DIMENSION_KEY)) {
                            if (item.getThrower() != null || MockItem.isMockItem(stack)) return;

                            ItemStack mockStack = MockItem.createMockStack(stack); // Convert it to a mock item (incompatible and useless)
                            if (stack.getItem() instanceof BlockItem) // ... and mark down all relevant data
                                mockStack.getOrCreateNbt().putIntArray("AttractPos", new int[]{item.getBlockX(), item.getBlockY(), item.getBlockZ()});
                            item.setStack(mockStack);
                        } else { // ... outside the AU
                            if (MockItem.isMockItem(stack)) {
                                // Mark it as an item of interest, and save relevant data
                                NbtCompound stackData = stack.getOrCreateNbt();
                                if (stackData.contains("AttractPos")) { // if attracted to a specific position
                                    String itemId = stackData.getString("MockItem");
                                    int[] attractPos = stackData.getIntArray("AttractPos");
                                    BlockPos attractBlockPos = new BlockPos(attractPos[0], attractPos[1], attractPos[2]);
                                    if ( // ... if the world has the specified block item
                                            Registry.ITEM.getId(
                                                    world.getBlockState(attractBlockPos).getBlock().asItem()
                                            ).toString().equals(itemId)
                                    )
                                        JCraft.markItemOfInterest(item, blockAttractionInterest(attractBlockPos));
                                } else { // if not attracted to a specific position, it's a general item to attract
                                    JCraft.markItemOfInterest(item, itemAttractionInterest(stack.getItem()));
                                }
                            }
                        }
                    }
                }
        );

        ServerLivingEntityEvents.AFTER_DEATH.register(
                (living, source) -> {
                    if (living instanceof ServerPlayerEntity player) {
                        NbtCompound playerData = ((IEntityDataSaver) player).getPersistentData();
                        // Reset cooldowns upon death
                        for (String cooldownType : JCraft.cooldowns) playerData.putInt(cooldownType, 0);

                        if (source.getAttacker() instanceof LivingEntity killer) {
                            NbtCompound killerData = ((IEntityDataSaver) killer).getPersistentData();
                            killerData.putInt(JCraft.comboBreakerCD, 0);
                        }
                    }
                }
        );

        ServerPlayerEvents.COPY_FROM.register(new JPlayerEntityEvents());

        ServerTickEvents.END_SERVER_TICK.register(JServerTickEvents::serverTick);

        // Send initial values of server config options to the player.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            ConfigUpdatePacket.sendOptionsToClient(handler.getPlayer(), ConfigOption.getImmutableOptions().values()));

        ServerLifecycleEvents.SERVER_STARTING.register(JServerConfig::load);
    }
}
