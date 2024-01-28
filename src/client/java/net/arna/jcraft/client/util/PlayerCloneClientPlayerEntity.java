package net.arna.jcraft.client.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.arna.jcraft.client.rendering.CloneSkinTracker;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerCloneClientPlayerEntity extends AbstractClientPlayerEntity {
    private static final Set<PlayerCloneClientPlayerEntity> entities = Collections.newSetFromMap(new WeakHashMap<>());
    private static final GameProfile CLONE_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("jcraft$playerClone".getBytes()), null);
    private final PlayerCloneEntity clone;

    static {
        ClientTickEvents.END_WORLD_TICK.register(world -> entities.stream()
                .filter(LivingEntity::isAlive)
                .forEach(PlayerCloneClientPlayerEntity::tick));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) entities.clear();
        });
    }

    public PlayerCloneClientPlayerEntity(PlayerCloneEntity clone) {
        super(Objects.requireNonNull(MinecraftClient.getInstance().world), CLONE_PROFILE, null);
        this.clone = clone;
        entities.add(this);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Nullable
    @Override
    protected PlayerListEntry getPlayerListEntry() {
        return null;
    }

    @Override
    public boolean hasSkinTexture() {
        return CloneSkinTracker.getSkinFor(clone, MinecraftProfileTexture.Type.SKIN) != null;
    }

    @Override
    public Identifier getSkinTexture() {
        return CloneSkinTracker.getSkinFor(clone, MinecraftProfileTexture.Type.SKIN);
    }

    @Override
    public boolean canRenderCapeTexture() {
        return CloneSkinTracker.getSkinFor(clone, MinecraftProfileTexture.Type.CAPE) != null;
    }

    @Nullable
    @Override
    public Identifier getCapeTexture() {
        return CloneSkinTracker.getSkinFor(clone, MinecraftProfileTexture.Type.CAPE);
    }

    @Override
    public boolean canRenderElytraTexture() {
        return CloneSkinTracker.getSkinFor(clone, MinecraftProfileTexture.Type.ELYTRA) != null;
    }

    @Nullable
    @Override
    public Identifier getElytraTexture() {
        return CloneSkinTracker.getSkinFor(clone, MinecraftProfileTexture.Type.ELYTRA);
    }

    @Override
    public String getModel() {
        return CloneSkinTracker.getModelFor(clone);
    }

    @Override
    public boolean shouldRenderName() {
        // Unused because PlayerEntityRenderer extends LivingEntityRenderer which ignores this.
        // Actual implementation is found in LivingEntityRendererMixin.
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // Age is usually incremented by whatever ticked this entity, except we tick it
        // ourselves, so we have to manually increase the age.
        // Age is used for several animation-related values. (Such as the 'breathing' motion of the arms)
        age++;

        setMainArm(clone.isLeftHanded() ? Arm.LEFT : Arm.RIGHT);

        dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, clone.getPartMask());

        preferredHand = clone.preferredHand;

        setStuckArrowCount(clone.getStuckArrowCount());
        setStingerCount(clone.getStingerCount());

        for (EquipmentSlot slot : EquipmentSlot.values())
            equipStack(slot, clone.getEquippedStack(slot));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    public void updateData() {
        prevX = clone.prevX;
        prevY = clone.prevY;
        prevZ = clone.prevZ;

        setPos(clone.getX(), clone.getY(), clone.getZ());

        prevBodyYaw = clone.prevBodyYaw;
        bodyYaw = clone.bodyYaw;
        prevHeadYaw = clone.prevHeadYaw;
        headYaw = clone.headYaw;

        hurtTime = clone.hurtTime;
        maxHurtTime = clone.maxHurtTime;

        lastLimbDistance = clone.lastLimbDistance;
        limbDistance = clone.limbDistance;
        limbAngle = clone.limbAngle;

        handSwinging = clone.handSwinging;
        lastHandSwingProgress = clone.lastHandSwingProgress;
        handSwingProgress = clone.handSwingProgress;
        handSwingTicks = clone.handSwingTicks;

        deathTime = clone.deathTime;
        dead = clone.isDead();
    }
}
