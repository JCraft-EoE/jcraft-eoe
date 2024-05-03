package net.arna.jcraft.common.network.c2s;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.callbacks.JServerPlayerInputCallback;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;

public class PlayerInputPacket {
    private static final int HOLD_TIMEOUT_TICKS = 3; // 0.15s
    private static final Map<ServerPlayerEntity, Object2BooleanMap<MoveInputType>> successMap = new WeakHashMap<>();
    private static final int MOVEMENT_INPUT_TYPES;

    static {
        MOVEMENT_INPUT_TYPES = MovementInputType.values().length;

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                InputStateManager sm = getInputStateManager(player);

                // Handle held inputs
                if (!sm.heldInputs.isEmpty()) {
                    sm.heldInputs.forEach(
                        (type, integer) -> {
                            //JCraft.LOGGER.info("Holding: " + type + ", with remaining heartbeat time: " + integer);

                            if (integer == 0) { // Marked for unprocessed removal by handleMoveInput(), which shouldn't mutate heldInputs.keySet()
                                sm.heldInputs.remove(type);
                            } else {
                                Integer newValue = integer - 1;
                                // JUtils.canHoldMove() may change after a holdable button was pressed if the player swaps their abilities
                                if (newValue <= 0 || !JUtils.canHoldMove(player, type)) {
                                    server.execute(() -> {
                                        boolean success = true;
                                        JServerPlayerInputCallback.EVENT.invoker().onPlayerInput(player, type, false, success);

                                        StandEntity<?, ?> stand = JUtils.getStand(player);
                                        if (stand != null && stand.allowMoveHandling()) {
                                            stand.onUserMoveInput(type, false, success);
                                            success = false; // If a stand is out, the move input success belongs to it.
                                        }

                                        JSpec<?, ?> spec = JUtils.getSpec(player);
                                        if (spec != null)
                                            spec.onUserMoveInput(type, false, success);
                                    });
                                    sm.heldInputs.remove(type);
                                } else
                                    sm.heldInputs.put(type, newValue);
                            }
                        }
                    );

                    sm.heldInputs.keySet().forEach(type -> handleMoveInput(server, player, type));
                }

                int forward = sm.calcForward();
                int side = sm.calcSide();
                JComponents.getMiscData(player).updateRemoteInputs(forward, side, sm.jumping);

                StandEntity<?, ?> stand = JUtils.getStand(player);
                if (stand != null) stand.updateRemoteInputs(forward, side, sm.jumping, sm.sneaking);

                if (sm.dashing) JCraft.tryDash(forward, side, player);

                if (!sm.jumping) continue;

                if (JCraft.isDashing(player))
                    // 5s cooldown for superjumping
                    JComponents.getCooldowns(player).setCooldown(CooldownType.DASH, 100);

                checkComboBreak(player);
            }
        });
    }

    public static PacketByteBuf write(Object2BooleanMap<MovementInputType> movementInput, Object2BooleanMap<MoveInputType> moveInput) {
        PacketByteBuf buf = PacketByteBufs.create();
        writeInput(buf, movementInput);
        writeInput(buf, moveInput);
        return buf;
    }

    private static void writeInput(PacketByteBuf buf, @Nullable Object2BooleanMap<? extends Enum<?>> input) {
        if (input == null) {
            buf.writeVarInt(0);
            return;
        }
        buf.writeVarInt(input.size());
        for (Object2BooleanMap.Entry<? extends Enum<?>> entry : input.object2BooleanEntrySet()) {
            buf.writeEnumConstant(entry.getKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        InputStateManager sm = getInputStateManager(player);
        handleMovementInput(server, player, buf, sm);
        handleMoveInput(player, buf, sm);
    }

    public static void handleHold(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        buf.readVarInt(); // Throwaway Movement input data
        int count = buf.readVarInt();
        if (count > MoveInputType.types) {
            player.networkHandler.disconnect(Text.of("Illegal input packet!"));
        }

        InputStateManager sm = getInputStateManager(player);
        for (int i = 0; i < count; i++) {
            MoveInputType type = buf.readEnumConstant(MoveInputType.class);
            buf.readBoolean(); // Throwaway hold input data
            if (JUtils.canHoldMove(player, type))
                sm.heldInputs.put(type, HOLD_TIMEOUT_TICKS);
        }
    }

    private static void handleMovementInput(MinecraftServer server, ServerPlayerEntity player, PacketByteBuf buf, InputStateManager sm) {
        int count = buf.readVarInt();
        if (count > MOVEMENT_INPUT_TYPES) {
            player.networkHandler.disconnect(Text.of("Illegal input packet!"));
        }

        for (int i = 0; i < count; i++) {
            MovementInputType type = buf.readEnumConstant(MovementInputType.class);
            boolean pressed = buf.readBoolean();

            switch (type) {
                case FORWARD -> sm.forward = pressed;
                case BACKWARD -> sm.backward = pressed;
                case LEFT -> sm.left = pressed;
                case RIGHT -> sm.right = pressed;
            }

            if (type == MovementInputType.JUMP) sm.jumping = pressed;

            if (type == MovementInputType.CROUCH) sm.sneaking = pressed;

            if (type == MovementInputType.DASH) {
                sm.dashing = pressed;
                if (pressed) server.execute(() -> JCraft.tryDash(sm.calcForward(), sm.calcSide(), player));
            }
        }

        if (sm.jumping) server.execute(() -> checkComboBreak(player));
    }


    private static void handleMoveInput(ServerPlayerEntity player, PacketByteBuf buf, InputStateManager sm) {
        int count = buf.readVarInt();
        if (count > MoveInputType.types) {
            player.networkHandler.disconnect(Text.of("Illegal input packet!"));
        }

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        for (int i = 0; i < count; i++) {
            MoveInputType type = buf.readEnumConstant(MoveInputType.class);
            boolean pressed = buf.readBoolean();

            if (JUtils.canHoldMove(player, type)) {
                if (pressed) sm.heldInputs.put(type, HOLD_TIMEOUT_TICKS);
                else sm.heldInputs.put(type, 0);
            }

            if (pressed) handleMoveInput(server, player, type).thenAccept(b -> {
                successMap.computeIfAbsent(player, p -> new Object2BooleanOpenHashMap<>()).put(type, b.booleanValue());

                server.execute(() -> {
                    JServerPlayerInputCallback.EVENT.invoker().onPlayerInput(player, type, true, b);
                    boolean success = b;

                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null && stand.allowMoveHandling()) {
                        stand.onUserMoveInput(type, true, success);
                        success = false; // If a stand is out, the move input success belongs to it.
                    }

                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null)
                        spec.onUserMoveInput(type, true, success);
                });
            });
            else {
                boolean b = successMap.computeIfAbsent(player, p -> new Object2BooleanOpenHashMap<>()).getOrDefault(type, false);

                server.execute(() -> {
                    JServerPlayerInputCallback.EVENT.invoker().onPlayerInput(player, type, false, b);
                    boolean success = b;

                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null && stand.allowMoveHandling()) {
                        stand.onUserMoveInput(type, false, success);
                        success = false; // If a stand is out, the move input success belongs to it.
                    }

                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null)
                        spec.onUserMoveInput(type, false, success);
                });
            }
        }
    }

    /**
     * javadoc pliz :>
     * @param player that sent the input
     * @return
     */
    private static CompletableFuture<Boolean> handleMoveInput(MinecraftServer server, ServerPlayerEntity player, MoveInputType type) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ServerWorld world = (ServerWorld) player.getWorld();
        server.execute(() -> {
            switch (type) {
                case STAND_SUMMON -> {
                    PacketByteBuf buf2 = PacketByteBufs.create();
                    buf2.writeShort(6);
                    buf2.writeInt(0);
                    ServerChannelFeedbackPacket.send(player, buf2);

                    StandComponent standData = JComponents.getStandData(player);
                    StandEntity<?, ?> stand = standData.getStand();
                    if (stand != null) {
                        int moveStun = stand.getMoveStun();
                        if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT) {
                            stand.queueMove(MoveInputType.STAND_SUMMON);
                            future.complete(false);
                        } else {
                            stand.desummon();
                            future.complete(true);
                        }
                    } else if (world != null) {
                        JCraft.summon(world, player);
                        future.complete(true);
                    }
                }
                case LIGHT -> {
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand == null || !stand.allowMoveHandling()) return;

                    future.complete(initStandMove(stand, MoveInputType.LIGHT));
                }
                case UTILITY -> {
                    boolean s;
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null) s = initStandMove(stand, MoveInputType.UTILITY);
                    else {
                        StandEntity<?, ?> stand2 = JCraft.summon(world, player);
                        if (stand2 != null) s = stand2.initMove(MoveType.UTILITY);
                        else s = false;
                    }

                    future.complete(s);
                }
                default -> future.complete(initStandOrSpecMove(player, type));
            }
        });

        return future;
    }

    private static boolean initStandOrSpecMove(ServerPlayerEntity player, MoveInputType type) {
        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null && stand.allowMoveHandling()) return initStandMove(stand, type);
        else {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec == null) return false;

            if (spec.initMove(type.getMoveType())) return true;
            if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                spec.queuedMove = type;

            return false;
        }
    }

    private static boolean initStandMove(StandEntity<?, ?> stand, MoveInputType type) {
        if (!stand.blocking) {
            int moveStun = stand.getMoveStun();

            if (stand.initMove(type.getMoveType())) return true;
            if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT)
                stand.queueMove(type);
        }

        return false;
    }

    private static void checkComboBreak(ServerPlayerEntity player) {
        // Combo break if stunned, jumping and crouching
        InputStateManager sm = getInputStateManager(player);
        if (sm == null || !sm.jumping || !player.isSneaking() || JUtils.isBlocking(player)) return;

        StatusEffectInstance stun = player.getStatusEffect(JStatusRegistry.DAZED);
        if (stun != null) JCraft.comboBreak((ServerWorld) player.getWorld(), player, stun);
    }

    public static InputStateManager getInputStateManager(ServerPlayerEntity player) {
        return ((IJInputStateManagerHolder) player).jcraft$getJInputStateManager();
    }
}
