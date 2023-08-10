package net.arna.jcraft.common.component.impl;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.MiscComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class MiscComponentImpl implements MiscComponent {
    private final Entity entity;
    @Getter
    private boolean thin;
    @Getter
    private Vec3d desiredVelocity = Vec3d.ZERO;
    @Getter
    private UUID slavedTo;
    private int damageTimer;

    public MiscComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setThin(boolean thin) {
        this.thin = thin;
        sync();
    }

    @Override
    public void updateRemoteInputs(int forward, int sideways, boolean jumping) {
        if (!(entity instanceof PlayerEntity player)) return;

        Vec3d v = new Vec3d(forward, 0, sideways).normalize();

        Vec3d rotVec = player.getRotationVector();
        rotVec = new Vec3d(rotVec.x, 0, rotVec.z).normalize();

        float moveSpeed = player.getMovementSpeed();
        desiredVelocity = rotVec.multiply(v.x * moveSpeed) // W/S
                .add(rotVec.rotateY(1.5707963f).multiply(v.z * moveSpeed)); // A/D
        if (jumping && player.isOnGround())
            desiredVelocity = desiredVelocity.add(0, player.getJumpBoostVelocityModifier() * 0.42F, 0);
    }

    @Override
    public void setSlavedTo(UUID slavedTo) {
        this.slavedTo = slavedTo;
        sync();
    }

    @Override
    public void startDamageTimer() {
        this.damageTimer = 600;
        sync();
    }

    @Override
    public boolean isOnDamageTimer() {
        return damageTimer > 0;
    }

    @Override
    public void tick() {
        if (damageTimer > 0) damageTimer--;
    }

    private void sync() {
        JComponents.MISC.sync(entity);
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        thin = tag.getBoolean("Thin");

        NbtCompound dvComp = tag.getCompound("DesiredVelocity");
        desiredVelocity = new Vec3d(dvComp.getDouble("X"), dvComp.getDouble("Y"), dvComp.getDouble("Z"));

        damageTimer = tag.getInt("DamageTimer");
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        tag.putBoolean("Thin", thin);

        NbtCompound dvComp = new NbtCompound();
        dvComp.putDouble("X", desiredVelocity.getX());
        dvComp.putDouble("Y", desiredVelocity.getY());
        dvComp.putDouble("Z", desiredVelocity.getZ());
        tag.put("DesiredVelocity", dvComp);

        tag.putInt("DamageTimer", damageTimer);
    }
}
