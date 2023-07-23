package net.arna.jcraft.mixin;

import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ISpec, IComboCounter {

    @Shadow
    public abstract void increaseStat(Stat<?> stat, int amount);

    // Remote input sync (serverside)
    private Vec3d desiredVelocity = Vec3d.ZERO;
    public Vec3d getDesiredVelocity() {
        return desiredVelocity;
    }
    public void updateRemoteInputs(int f, int s, boolean j) {
        PlayerEntity player = ((PlayerEntity) (Object) this);

        Vec3d v = new Vec3d(f, 0, s).normalize();

        Vec3d rotVec = player.getRotationVector();
        rotVec = new Vec3d(rotVec.x, 0, rotVec.z).normalize();

        float moveSpeed = player.getMovementSpeed();
        desiredVelocity = rotVec.multiply(v.x * moveSpeed) // W/S
                .add(rotVec.rotateY(1.5707963f).multiply(v.z * moveSpeed)); // A/D
        if (j && player.isOnGround())
            desiredVelocity = desiredVelocity.add(0, player.getJumpBoostVelocityModifier() * 0.42F, 0);
    }

    // Spec instance storage
    private JCraftSpec spec;

    @Override
    public JCraftSpec getSpec() {
        return spec;
    }

    @Override
    public void setClientSpec(JCraftSpec spec) {
        this.spec = spec;
    }

    /**
     * Sets the player's spec on the serverside.
     * Also handles synchronization with client.
     */
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setSpec(JCraftSpec spec) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(5);
        buf.writeInt(spec == null ? 0 : spec.getId());
        ServerChannelFeedbackPacket.send( ((ServerPlayerEntity)(Object)this), buf );

        this.spec = spec;
    }

    // Combo tracking
    private int comboCount = 1;
    private LivingEntity lastAttacked;

    @Override
    public LivingEntity getLastAttacked() {
        return lastAttacked;
    }

    @Override
    public void setLastAttacked(LivingEntity l) {
        lastAttacked = l;
    }

    @Override
    public int jcraft$getComboCount() {
        return comboCount;
    }

    @Override
    public void jcraft$setComboCount(int i) {
        comboCount = i;
    }

    @Override
    public void incrementComboCount() {
        comboCount++;
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void jcraft$playerTick(CallbackInfo info) {
        if (spec != null) {
            spec.tickSpec();
        }

        if (lastAttacked != null && lastAttacked.isAlive()) {
            LivingEntity attacker = lastAttacked.getAttacker();
            if (attacker != null && attacker != (Object) this) {
                lastAttacked = null;
                comboCount = 0;

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(6);
                buf.writeInt(0);
                if (PlayerEntity.class.cast(this) instanceof ServerPlayerEntity serverPlayerEntity) {
                    ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                }
            }
        }
    }

    // KNOCKDOWN and poison preventing pose updating
    @Inject(cancellable = true, at = @At("HEAD"), method = "updatePose")
    public void jcraft$updatePose(CallbackInfo info) {
        if (
                ((PlayerEntity) (Object) this).hasStatusEffect(JStatusRegister.KNOCKDOWN)
                        || ((PlayerEntity) (Object) this).hasStatusEffect(JStatusRegister.WSPOISON)
        ) {
            info.cancel();
        }
    }

    // Can't M1 in TS or during spec moves, LivingEntity does not override this
    @Inject(cancellable = true, method = "attack", at = @At("HEAD"))
    public void jcraft$attack(Entity target, CallbackInfo info) {
        if (((ITimeStop) this).getTimeStopTicks() > 0) {
            info.cancel();
        }
        if (spec != null && spec.moveStun > 0) {
            info.cancel();
        }
    }

    // Counter hook - player entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "applyDamage")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        PlayerEntity player = ((PlayerEntity) (Object) this);

        if (player.getFirstPassenger() instanceof StandEntity stand) {
            Attack attack = stand.curAttack;
            if (attack != null) {
                if (attack.attackType == AttackType.COUNTER && stand.getMoveStun() < (attack.moveStun - attack.initTime)) {
                    stand.counter(source.getAttacker(), source); // Initiate counter
                    player.removeStatusEffect(JStatusRegister.DAZED);
                    info.cancel();
                }
            }
        }
    }
}
