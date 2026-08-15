package net.arna.jcraft.common.network.c2s;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.vehicle.AbstractGroundVehicleEntity;
import net.arna.jcraft.common.events.JServerPlayerInputEvent;
import net.arna.jcraft.common.item.Peacemaker;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;

public class PlayerInputPacket {
    private static final int HOLD_TIMEOUT_TICKS = 3; // 0.15s
    private static final Map<ServerPlayer, Object2BooleanMap<MoveInputType>> successMap = new WeakHashMap<>();
    private static final int MOVEMENT_INPUT_TYPES;

    static {
        MOVEMENT_INPUT_TYPES = MovementInputType.values().length;
        TickEvent.SERVER_PRE.register(PlayerInputPacket::tick);
    }

    public static FriendlyByteBuf write(Object2BooleanMap<MovementInputType> movementInput, Object2BooleanMap<MoveInputType> moveInput) {
        return write(movementInput, moveInput, null);
    }

    public static FriendlyByteBuf write(Object2BooleanMap<MovementInputType> movementInput, Object2BooleanMap<MoveInputType> moveInput, @Nullable Vec3 muzzle) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        writeInput(buf, movementInput);
        writeInput(buf, moveInput);
        writeMuzzle(buf, muzzle);
        return buf;
    }

    private static void writeInput(FriendlyByteBuf buf, @Nullable Object2BooleanMap<? extends Enum<?>> input) {
        if (input == null) {
            buf.writeVarInt(0);
            return;
        }
        buf.writeVarInt(input.size());
        for (Object2BooleanMap.Entry<? extends Enum<?>> entry : input.object2BooleanEntrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    private static void writeMuzzle(FriendlyByteBuf buf, @Nullable Vec3 muzzle) {
        buf.writeBoolean(muzzle != null);
        if (muzzle != null) {
            buf.writeDouble(muzzle.x);
            buf.writeDouble(muzzle.y);
            buf.writeDouble(muzzle.z);
        }
    }

    @Nullable
    private static Vec3 readMuzzle(FriendlyByteBuf buf) {
        if (!buf.isReadable()) {
            return null;
        }
        if (!buf.readBoolean()) {
            return null;
        }
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        MinecraftServer server = context.getPlayer().getServer();

        InputStateManager sm = getInputStateManager(player);
        handleMovementInput(server, player, buf, sm);
        handleMoveInput(player, buf, sm);
    }


    public static void handleHold(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        MinecraftServer server = context.getPlayer().getServer();

        buf.readVarInt(); // Throwaway Movement input data
        int count = buf.readVarInt();
        if (count > MoveInputType.types) {
            player.connection.disconnect(Component.nullToEmpty("Illegal input packet!"));
        }

        InputStateManager sm = getInputStateManager(player);
        for (int i = 0; i < count; i++) {
            MoveInputType type = buf.readEnum(MoveInputType.class);
            buf.readBoolean(); // Throwaway hold input data
            if (JUtils.canHoldMove(player, type)) {
                sm.heldInputs.put(type, HOLD_TIMEOUT_TICKS);
            }
        }
    }

    private static void handleMovementInput(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buf, InputStateManager sm) {
        int count = buf.readVarInt();
        if (count > MOVEMENT_INPUT_TYPES) {
            player.connection.disconnect(Component.nullToEmpty("Illegal input packet!"));
        }

        for (int i = 0; i < count; i++) {
            MovementInputType type = buf.readEnum(MovementInputType.class);
            boolean pressed = buf.readBoolean();

            switch (type) {
                case FORWARD -> sm.forward = pressed;
                case BACKWARD -> sm.backward = pressed;
                case LEFT -> sm.left = pressed;
                case RIGHT -> sm.right = pressed;
            }

            if (type == MovementInputType.JUMP) {
                sm.jumping = pressed;
            }

            if (type == MovementInputType.CROUCH) {
                sm.sneaking = pressed;
            }

            if (type == MovementInputType.DASH) {
                sm.dashing = pressed;
                if (pressed) {
                    server.execute(() -> DashData.tryDash(sm.calcForward(), sm.calcSide(), player));
                }
            }
        }

        if (sm.jumping) {
            server.execute(() -> checkComboBreak(player));
        }
    }


    private static void handleMoveInput(ServerPlayer player, FriendlyByteBuf buf, InputStateManager sm) {
        int count = buf.readVarInt();
        if (count > MoveInputType.types) {
            player.connection.disconnect(Component.nullToEmpty("Illegal input packet!"));
        }

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        MoveInputType[] types = new MoveInputType[count];
        boolean[] pressedStates = new boolean[count];
        for (int i = 0; i < count; i++) {
            MoveInputType type = buf.readEnum(MoveInputType.class);
            boolean pressed = buf.readBoolean();
            types[i] = type;
            pressedStates[i] = pressed;

            if (JUtils.canHoldMove(player, type)) {
                if (pressed) {
                    sm.heldInputs.put(type, HOLD_TIMEOUT_TICKS);
                } else {
                    sm.heldInputs.put(type, 0);
                }
            }
        }

        Vec3 muzzle = readMuzzle(buf);
        for (int i = 0; i < count; i++) {
            MoveInputType type = types[i];
            boolean pressed = pressedStates[i];

            if (pressed) {
                handleMoveInput(server, player, type, muzzle).thenAccept(b -> {
                    successMap.computeIfAbsent(player, p -> new Object2BooleanOpenHashMap<>()).put(type, b.booleanValue());

                    server.execute(() -> {
                        JServerPlayerInputEvent.EVENT.invoker().onPlayerInput(player, type, true, b);
                        boolean success = b;

                        StandEntity<?, ?> stand = JUtils.getStand(player);
                        if (stand != null && stand.allowMoveHandling()) {
                            stand.onUserMoveInput(type, true, success);
                            success = false; // If a stand is out, the move input success belongs to it.
                        }

                        JSpec<?, ?> spec = JUtils.getSpec(player);
                        if (spec != null) {
                            spec.onUserMoveInput(type, true, success);
                        }
                    });
                });
            } else {
                boolean b = successMap.computeIfAbsent(player, p -> new Object2BooleanOpenHashMap<>()).getOrDefault(type, false);

                server.execute(() -> {
                    JServerPlayerInputEvent.EVENT.invoker().onPlayerInput(player, type, false, b);
                    boolean success = b;

                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null && stand.allowMoveHandling()) {
                        stand.onUserMoveInput(type, false, success);
                        success = false; // If a stand is out, the move input success belongs to it.
                    }

                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.onUserMoveInput(type, false, success);
                    }
                });
            }
        }
    }

    /**
     * javadoc pliz :>
     *
     * @param player that sent the input
     * @return
     */
    private static CompletableFuture<Boolean> handleMoveInput(MinecraftServer server, ServerPlayer player, MoveInputType type, @Nullable Vec3 muzzle) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ServerLevel world = (ServerLevel) player.level();
        server.execute(() -> {
            switch (type) {
                case STAND_SUMMON -> {
                    FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
                    buf2.writeShort(6);
                    buf2.writeInt(0);
                    ServerChannelFeedbackPacket.send(player, buf2);

                    CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(player);
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
                    // First check if player is holding a Peacemaker
                    boolean peacemakerHandled = Peacemaker.handleLeftClick(player, muzzle);
                    if (peacemakerHandled) {
                        future.complete(true);
                        return;
                    }

                    // If not handled by Peacemaker, proceed with normal stand logic
                    future.complete(initStandOrSpecMove(player, MoveInputType.LIGHT));
                }
                case TOSS -> {
                    // A held gun takes the pick block key for reloading; without one it still tosses.
                    if (Peacemaker.handleReloadInput(player)) {
                        future.complete(true);
                        return;
                    }

                    future.complete(initStandOrSpecMove(player, MoveInputType.TOSS));
                }
                case UTILITY -> {
                    boolean s;
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null) {
                        s = initStandMove(stand, MoveInputType.UTILITY);
                    } else {
                        StandEntity<?, ?> stand2 = JCraft.summon(world, player);
                        if (stand2 != null && !stand2.wantToBlock) {
                            s = stand2.initMove(MoveClass.UTILITY);
                        } else {
                            s = false;
                        }
                    }

                    future.complete(s);
                }
                default -> future.complete(initStandOrSpecMove(player, type));
            }
        });

        return future;
    }

    private static CompletableFuture<Boolean> handleMoveInput(MinecraftServer server, ServerPlayer player, MoveInputType type) {
        return handleMoveInput(server, player, type, null);
    }

    private static boolean initStandOrSpecMove(ServerPlayer player, MoveInputType type) {
        StandEntity<?, ?> stand = JUtils.getStand(player);

        boolean standExists = stand != null;
        boolean forwardToSpec = !standExists || !stand.allowMoveHandling() || stand.forwardInputToSpec(type);
        boolean success = false;
        
        if (standExists && stand.allowMoveHandling()) {
            success = initStandMove(stand, type);
        }

        if (forwardToSpec) {
            JSpec<?, ?> spec = JUtils.getSpec(player);

            if (spec == null) {
                return success;
            } else if (spec.initMove(type.getMoveClass())) {
                success = true;
            } else if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
                spec.queuedMove = type;
            }
        }

        return success;
    }

    private static boolean initStandMove(StandEntity<?, ?> stand, MoveInputType type) {
        if (!stand.blocking) {
            if (!stand.acceptsUserMoveInit(type))
                return false;

            int moveStun = stand.getMoveStun();

            if (stand.initMove(type.getMoveClass()))
                return true;

            if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT)
                stand.queueMove(type);
        }

        return false;
    }

    private static void checkComboBreak(ServerPlayer player) {
        // Combo break if stunned, jumping and crouching
        InputStateManager sm = getInputStateManager(player);
        final boolean blocking = JUtils.isBlocking(player);

        if (sm == null || !sm.jumping || !player.isShiftKeyDown()) {
            return;
        }

        if (blocking) {
            StandEntity<?, ?> stand = JUtils.getStand(player);

            // This check is redundant, but I'm putting it here if we add spec blocking in the future.
            if (stand == null) {
                JCraft.LOGGER.warn("Player " + player + " was blocking despite having no stand?");
            } else {
                if (stand.getMoveStun() < 2) {
                    return;
                }
                // Else is in blockstun due to an attack

                JCraft.tryPushBlock((ServerLevel) player.level(), player, stand);
            }
        } else {
            MobEffectInstance stun = player.getEffect(JStatusRegistry.DAZED.get());
            if (stun != null) {
                JCraft.comboBreak((ServerLevel) player.level(), player, stun);
            }
        }
    }

    public static InputStateManager getInputStateManager(ServerPlayer player) {
        return ((IJInputStateManagerHolder) player).jcraft$getJInputStateManager();
    }

    private static void tick(MinecraftServer instance) {
        for (ServerPlayer player : instance.getPlayerList().getPlayers()) {
            InputStateManager sm = getInputStateManager(player);

            // Handle held inputs
            if (!sm.heldInputs.isEmpty()) {
                sm.heldInputs.forEach((type, integer) ->
                        handleHeldInput(instance, player, type, integer, sm));

                sm.heldInputs.keySet().forEach(type -> handleMoveInput(instance, player, type));
            }

            int forward = sm.calcForward();
            int side = sm.calcSide();
            JComponentPlatformUtils.getMiscData(player).updateRemoteInputs(forward, side, sm.jumping);

            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (stand != null) {
                stand.updateRemoteInputs(forward, side, sm.jumping, sm.sneaking);
            }

            if (player.getVehicle() instanceof AbstractGroundVehicleEntity groundVehicle) {
                groundVehicle.updateInputs(forward, side, sm.jumping, sm.sneaking);
            }

            if (sm.dashing) {
                DashData.tryDash(forward, side, player);
            }

            if (!sm.jumping) {
                continue;
            }

            if (DashData.isDashing(player))
            // 5s cooldown for superjumping
            {
                JComponentPlatformUtils.getCooldowns(player).setCooldown(CooldownType.DASH, 100);
            }

            checkComboBreak(player);
        }
    }

    private static void handleHeldInput(MinecraftServer instance, ServerPlayer player, MoveInputType type, Integer integer, InputStateManager sm) {
        //JCraft.LOGGER.info("Holding: " + type + ", with remaining heartbeat time: " + integer);

        if (integer == 0) { // Marked for instant removal by handleMoveInput(), which shouldn't mutate heldInputs.keySet()
            sm.heldInputs.remove(type);
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec != null) {
                spec.onUserMoveInput(type, false, false);
            }
        } else {
            Integer newValue = integer - 1;
            // JUtils.canHoldMove() may change after a holdable button was pressed if the player swaps their abilities
            if (newValue <= 0 || !JUtils.canHoldMove(player, type)) {
                instance.execute(() -> {
                    boolean success = true;
                    JServerPlayerInputEvent.EVENT.invoker().onPlayerInput(player, type, false, success);

                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null && stand.allowMoveHandling()) {
                        stand.onUserMoveInput(type, false, success);
                        success = false; // If a stand is out, the move input success belongs to it.
                    }

                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.onUserMoveInput(type, false, success);
                    }
                });
                sm.heldInputs.remove(type);
            } else {
                sm.heldInputs.put(type, newValue);
            }
        }
    }
}
