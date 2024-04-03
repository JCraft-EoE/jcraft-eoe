package net.arna.jcraft.client.gravity.api;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.gravity.util.GravityChannelClient;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.gravity.util.EntityTags;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.gravity.util.packet.DefaultGravityPacket;
import net.arna.jcraft.common.gravity.util.packet.InvertGravityPacket;
import net.arna.jcraft.common.gravity.util.packet.OverwriteGravityPacket;
import net.arna.jcraft.common.gravity.util.packet.UpdateGravityPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class GravityChangerAPIClient {

    public static void addGravityClient(ClientPlayerEntity entity, Gravity gravity, Identifier verifier, PacketByteBuf verifierInfo) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) return;
        GravityChangerAPI.maybeGetSafe(GravityChangerAPI.GRAVITY_COMPONENT, entity).ifPresent(gc -> {
            gc.addGravity(gravity, false);
            GravityChannelClient.UPDATE_GRAVITY.sendToServer(new UpdateGravityPacket(gravity, false), verifier, verifierInfo);
        });
    }

    public static void setGravityClient(ClientPlayerEntity entity, ArrayList<Gravity> gravity, Identifier verifier, PacketByteBuf verifierInfo) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) return;
        GravityChangerAPI.maybeGetSafe(GravityChangerAPI.GRAVITY_COMPONENT, entity).ifPresent(gc -> {
            gc.setGravity(gravity, false);
            GravityChannelClient.OVERWRITE_GRAVITY.sendToServer(new OverwriteGravityPacket(gravity, false), verifier, verifierInfo);
        });
    }

    public static void setIsInvertedClient(ClientPlayerEntity entity, boolean isInverted, RotationParameters rotationParameters, Identifier verifier, PacketByteBuf verifierInfo) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) return;
        GravityChangerAPI.maybeGetSafe(GravityChangerAPI.GRAVITY_COMPONENT, entity).ifPresent(gc -> {
            gc.invertGravity(isInverted, rotationParameters, false);
            GravityChannelClient.INVERT_GRAVITY.sendToServer(new InvertGravityPacket(isInverted, rotationParameters, false), verifier, verifierInfo);
        });
    }

    public static void clearGravityClient(ClientPlayerEntity entity, RotationParameters rotationParameters, Identifier verifier, PacketByteBuf verifierInfo) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) return;
        GravityChangerAPI.maybeGetSafe(GravityChangerAPI.GRAVITY_COMPONENT, entity).ifPresent(gc -> {
            gc.clearGravity(rotationParameters, false);
            GravityChannelClient.OVERWRITE_GRAVITY.sendToServer(new OverwriteGravityPacket(new ArrayList<>(), false), verifier, verifierInfo);
        });
    }

    public static void setDefaultGravityDirectionClient(ClientPlayerEntity entity, Direction gravityDirection, RotationParameters rotationParameters, Identifier verifier, PacketByteBuf verifierInfo) {
        if (onWrongSide(entity) || !EntityTags.canChangeGravity(entity)) return;
        GravityChangerAPI.maybeGetSafe(GravityChangerAPI.GRAVITY_COMPONENT, entity).ifPresent(gc -> {
            gc.setDefaultGravityDirection(gravityDirection, rotationParameters, false);
            GravityChannelClient.DEFAULT_GRAVITY.sendToServer(new DefaultGravityPacket(gravityDirection, rotationParameters, false), verifier, verifierInfo);
        });
    }

    private static boolean onWrongSide(Entity entity) {
        if (!entity.getWorld().isClient) {
            JCraft.LOGGER.error("GravityChangerAPI function cannot be called from the server, use dedicated server class. ", new Exception());
            return true;
        }
        return false;
    }
}
