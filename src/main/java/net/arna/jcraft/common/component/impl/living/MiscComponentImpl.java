package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.MiscComponent;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MiscComponentImpl implements MiscComponent {
    private final Entity entity;
    @Getter
    private Vec3d desiredVelocity = Vec3d.ZERO;
    @Getter
    private @Nullable UUID slavedTo = null;
    private LivingEntity master = null;
    private int damageTimer;
    private int knifeTimer;
    @Getter
    private int stuckKnifeCount;
    @Getter
    private int armoredHitTicks;
    @Getter
    private int hoverTime;
    private boolean prevNoGrav;
    @Getter
    private float attackSpeedMult;

    public MiscComponentImpl(Entity entity) {
        this.entity = entity;
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
    public void setHoverTime(int hoverTime) {
        this.hoverTime = hoverTime;
    }

    @Override
    public boolean getPrevNoGrav() {
        return prevNoGrav;
    }
    @Override
    public void setPrevNoGrav(boolean prevNoGrav) {
        this.prevNoGrav = prevNoGrav;
    }

    @Override
    public void stab() {
        if (++stuckKnifeCount > 16)
            stuckKnifeCount = 16;
        updateKnifeTimer();
    }

    @Override
    public void displayArmoredHit() {
        entity.playSound(JSoundRegistry.ARMORED_HIT, 1.0F, 1.0F);
        armoredHitTicks = 10;
        sync();
    }

    @Override
    public void setAttackSpeedMult(float speedMult) {
        this.attackSpeedMult = speedMult;
        sync();
    }

    @Override
    public void tick() {
        if (damageTimer > 0) damageTimer--;
        if (armoredHitTicks > 0) armoredHitTicks--;

        if (entity.getWorld().isClient()) return;

        if (slavedTo != null) {
            if (master == null) {
                if (entity.age % 20 == 0) {
                    //TODO: make SlavedTo properly load from NBT for non-players
                    master = entity.getWorld().getPlayerByUuid(slavedTo);
                }
            } else {
                if (entity instanceof MobEntity mob) { // Targeting and movement for mobs
                    if (mob.getTarget() == master)
                        mob.setTarget(null);

                    LivingEntity victim = master.getAttacking();
                    if (victim == null) {
                        LivingEntity adv = master.getPrimeAdversary();
                        if (adv != null && adv.isAlive()) mob.setTarget(adv);
                    } else if (victim.isAlive()) mob.setTarget(victim);

                    if (mob.squaredDistanceTo(entity) > 256)
                        mob.getNavigation().startMovingTo(entity, 1);
                }
            }
        }

        if (stuckKnifeCount <= 0) {
            if (--knifeTimer <= 0) {
                stuckKnifeCount--;
                updateKnifeTimer();
            }
        }
    }

    public LivingEntity getMaster() {
        return master;
    }

    private void updateKnifeTimer() {
        knifeTimer = 20 * (30 - stuckKnifeCount);
        sync();
    }

    private void sync() {
        JComponents.MISC.sync(entity);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player.squaredDistanceTo(entity) <= 6400;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        MiscComponent.super.writeSyncPacket(buf, recipient);
        buf.writeVarInt(armoredHitTicks);
        buf.writeVarInt(stuckKnifeCount);
        buf.writeFloat(attackSpeedMult);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        MiscComponent.super.applySyncPacket(buf);
        armoredHitTicks = buf.readVarInt();
        stuckKnifeCount = buf.readVarInt();
        attackSpeedMult = buf.readFloat();
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        NbtCompound dvComp = tag.getCompound("DesiredVelocity");
        desiredVelocity = new Vec3d(dvComp.getDouble("X"), dvComp.getDouble("Y"), dvComp.getDouble("Z"));
        damageTimer = tag.getInt("DamageTimer");
        if (tag.containsUuid("SlavedTo"))
            slavedTo = tag.getUuid("SlavedTo");
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        NbtCompound dvComp = new NbtCompound();
        dvComp.putDouble("X", desiredVelocity.getX());
        dvComp.putDouble("Y", desiredVelocity.getY());
        dvComp.putDouble("Z", desiredVelocity.getZ());
        tag.put("DesiredVelocity", dvComp);
        tag.putInt("DamageTimer", damageTimer);
        if (slavedTo != null)
            tag.putUuid("SlavedTo", slavedTo);
    }
}
