package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CommonMiscComponentImpl implements CommonMiscComponent {
    private final LivingEntity entity;
    @Getter
    private Vec3 desiredVelocity = Vec3.ZERO;
    @Getter
    private @Nullable UUID slavedTo = null;
    @Getter
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
    @Getter
    @Setter
    private float metallicaIron = MetallicaEntity.IRON_MAX;
    @Getter
    @Setter
    private float aerosmithOverheat = 0f;

    public CommonMiscComponentImpl(final LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void updateRemoteInputs(final int forward, final int sideways, final boolean jumping) {
        if (!(entity instanceof Player player)) {
            return;
        }

        final Vec3 v = new Vec3(forward, 0, sideways).normalize();

        Vec3 rotVec = player.getLookAngle();
        rotVec = new Vec3(rotVec.x, 0, rotVec.z).normalize();

        final float moveSpeed = player.getSpeed();
        desiredVelocity = rotVec.scale(v.x * moveSpeed) // W/S
                .add(rotVec.yRot(1.5707963f).scale(v.z * moveSpeed)); // A/D
        if (jumping && player.onGround()) {
            desiredVelocity = desiredVelocity.add(0, player.getJumpBoostPower() * 0.42F, 0);
        }
    }

    @Override
    public void setSlavedTo(final UUID slavedTo) {
        this.slavedTo = slavedTo;
        sync(entity);
    }

    @Override
    public void startDamageTimer() {
        this.damageTimer = 60 * 20;
        sync(entity);
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
        if (++stuckKnifeCount > 16) {
            stuckKnifeCount = 16;
        }
        updateKnifeTimer();
    }

    @Override
    public void displayArmoredHit() {
        entity.playSound(JSoundRegistry.ARMORED_HIT.get(), 1.0F, 1.0F);
        armoredHitTicks = 10;
        sync(entity);
    }

    @Override
    public void setAttackSpeedMult(float speedMult) {
        this.attackSpeedMult = speedMult;
        sync(entity);
    }

    public void tick() {
        if (damageTimer > 0) {
            damageTimer--;
        }
        if (armoredHitTicks > 0) {
            armoredHitTicks--;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        if (slavedTo != null) {
            if (master == null) {
                if (entity.tickCount % 20 == 0) {
                    //TODO: make SlavedTo properly load from NBT for non-players
                    master = entity.level().getPlayerByUUID(slavedTo);
                }
            } else {
                if (entity instanceof Mob mob) { // Targeting and movement for mobs
                    if (mob.getTarget() == master) {
                        mob.setTarget(null);
                    }

                    LivingEntity victim = master.getLastHurtMob();
                    if (victim == null) {
                        LivingEntity adv = master.getKillCredit();
                        if (adv != null && adv.isAlive()) {
                            mob.setTarget(adv);
                        }
                    } else if (victim.isAlive()) {
                        mob.setTarget(victim);
                    }

                    if (mob.distanceToSqr(entity) > 256) {
                        mob.getNavigation().moveTo(entity, 1);
                    }
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

    private void updateKnifeTimer() {
        knifeTimer = 20 * (30 - stuckKnifeCount);
        sync(entity);
    }

    public void sync(Entity entity) {
    }

    public boolean shouldSyncWith(ServerPlayer player) {
        return player.distanceToSqr(entity) <= 6400;
    }

    public void writeSyncPacket(FriendlyByteBuf buf, ServerPlayer recipient) {
        buf.writeVarInt(armoredHitTicks);
        buf.writeVarInt(stuckKnifeCount);
        buf.writeFloat(attackSpeedMult);
    }

    public void applySyncPacket(FriendlyByteBuf buf) {
        armoredHitTicks = buf.readVarInt();
        stuckKnifeCount = buf.readVarInt();
        attackSpeedMult = buf.readFloat();
    }

    public void readFromNbt(@NonNull CompoundTag tag) {
        CompoundTag dvComp = tag.getCompound("DesiredVelocity");
        desiredVelocity = new Vec3(dvComp.getDouble("X"), dvComp.getDouble("Y"), dvComp.getDouble("Z"));
        damageTimer = tag.getInt("DamageTimer");
        metallicaIron = tag.getFloat("MetallicaIron");
        aerosmithOverheat = tag.getFloat("AerosmithOverheat");

        if (tag.hasUUID("SlavedTo")) {
            slavedTo = tag.getUUID("SlavedTo");
        }
    }

    public void writeToNbt(@NonNull CompoundTag tag) {
        CompoundTag dvComp = new CompoundTag();
        dvComp.putDouble("X", desiredVelocity.x());
        dvComp.putDouble("Y", desiredVelocity.y());
        dvComp.putDouble("Z", desiredVelocity.z());
        tag.put("DesiredVelocity", dvComp);
        tag.putInt("DamageTimer", damageTimer);
        tag.putFloat("MetallicaIron", metallicaIron);
        tag.putFloat("AerosmithOverheat", aerosmithOverheat);
        if (slavedTo != null) {
            tag.putUUID("SlavedTo", slavedTo);
        }
    }
}
