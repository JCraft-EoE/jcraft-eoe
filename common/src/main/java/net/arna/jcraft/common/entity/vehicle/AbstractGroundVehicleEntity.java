package net.arna.jcraft.common.entity.vehicle;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public abstract class AbstractGroundVehicleEntity extends LivingEntity {

    public static final String DEATH_CONTROLLER = "death";
    public static final String SHAKE_CONTROLLER = "shake";
    public static final String MOVEMENT_CONTROLLER = "movement";
    public static final String STEERING_CONTROLLER = "steering";
    public static final String HIT_CONTROLLER = "hit";

    protected static final AzCommand DEATH_CMD = AzCommand.create(DEATH_CONTROLLER, "explode", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand SHAKE_CMD = AzCommand.create(SHAKE_CONTROLLER, "shake", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand MOVE_FORWARD_CMD = AzCommand.create(MOVEMENT_CONTROLLER, "forward", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand MOVE_BACKWARD_CMD = AzCommand.create(MOVEMENT_CONTROLLER, "back", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand STEER_NEUTRAL_CMD = AzCommand.create(STEERING_CONTROLLER, "steer_neutral", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand STEER_LEFT_CMD = AzCommand.create(STEERING_CONTROLLER, "steer_left", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand STEER_RIGHT_CMD = AzCommand.create(STEERING_CONTROLLER, "steer_right", AzPlayBehaviors.PLAY_ONCE);
    protected static final AzCommand HIT_CMD = AzCommand.create(HIT_CONTROLLER, "hit", AzPlayBehaviors.PLAY_ONCE);

    // Movement Inputs
    protected boolean
    left = false,
    right = false,
    forward = false,
    back = false,

    space = false,
    sneak = false;

    @Getter
    protected float oldYRot = 0.0f;

    @Getter @Setter
    private Entity owner;

    protected boolean stopsWithoutDriver = true;
    protected boolean pushBelow = true;

    private static final EntityDataAccessor<Float> DAMAGE;
    private static final EntityDataAccessor<Integer> HURT_TIME;
    private static final EntityDataAccessor<Boolean> TURN_LEFT, TURN_RIGHT, MOVE_FORWARD, MOVE_BACK;
    static {
        DAMAGE = SynchedEntityData.defineId(AbstractGroundVehicleEntity.class, EntityDataSerializers.FLOAT);

        TURN_LEFT = SynchedEntityData.defineId(AbstractGroundVehicleEntity.class, EntityDataSerializers.BOOLEAN);
        TURN_RIGHT = SynchedEntityData.defineId(AbstractGroundVehicleEntity.class, EntityDataSerializers.BOOLEAN);
        MOVE_FORWARD = SynchedEntityData.defineId(AbstractGroundVehicleEntity.class, EntityDataSerializers.BOOLEAN);
        MOVE_BACK = SynchedEntityData.defineId(AbstractGroundVehicleEntity.class, EntityDataSerializers.BOOLEAN);

        HURT_TIME = SynchedEntityData.defineId(AbstractGroundVehicleEntity.class, EntityDataSerializers.INT);
    }

    public AbstractGroundVehicleEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected @NonNull Entity.MovementEmission getMovementEmission() {
        return MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        entityData.define(DAMAGE, 0.0F);

        entityData.define(TURN_LEFT, false);
        entityData.define(TURN_RIGHT, false);
        entityData.define(MOVE_FORWARD, false);
        entityData.define(MOVE_BACK, false);

        entityData.define(HURT_TIME, 0);
    }

    @Override
    public boolean canCollideWith(@NonNull Entity entity) { return Boat.canVehicleCollide(this, entity); }
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() { return isAlive(); } // Makes it selectable by players
    public boolean isAlive() { return !isRemoved(); }

    public void updateInputs(int forward, int left, boolean jump, boolean sneak) {
        if (forward == 0) { this.forward = false; this.back = false; }
        if (forward > 0) { this.forward = true; this.back = false; }
        if (forward < 0) { this.forward = false; this.back = true; }
        if (left == 0) { this.left = false; this.right = false; }
        if (left > 0) { this.left = true; this.right = false; }
        if (left < 0) { this.left = false; this.right = true; }

        entityData.set(TURN_LEFT, this.left);
        entityData.set(TURN_RIGHT, this.right);
        entityData.set(MOVE_FORWARD, this.forward);
        entityData.set(MOVE_BACK, this.back);

        this.space = jump;
        this.sneak = sneak;
    }

    public final boolean steeringLeft() { return entityData.get(TURN_LEFT); }
    public final boolean steeringRignt() { return entityData.get(TURN_RIGHT); }
    public final boolean movingForward() { return entityData.get(MOVE_FORWARD); }
    public final boolean movingBack() { return entityData.get(MOVE_BACK); }

    public final float getDamage() { return entityData.get(DAMAGE); }
    public final void setDamage(float damage) { entityData.set(DAMAGE, damage); }

    public final int getHurtTime() { return entityData.get(HURT_TIME); }
    public final void setHurtTime(int time) { entityData.set(HURT_TIME, time); }

    @Override
    public void tick() {
        super.tick();

        final Level level = level();
        final boolean client = level.isClientSide();

        if (stopsWithoutDriver && !isVehicle()) updateInputs(0, 0, false, false);

        if (client) JCraft.getClientEntityHandler().vehicleMovementTick(this);
        else movementTick(forward, left, back, right, space, sneak);

        if (isControlledByLocalInstance()) move(MoverType.SELF, getDeltaMovement());

        // Soft collisions beneath, hard collisions elsewhere.
        if (pushBelow && !client) {
            final Direction gravity = GravityChangerAPI.getGravityDirection(this);
            final Vec3 down = new Vec3(gravity.getStepX(), gravity.getStepY(), gravity.getStepZ());

            final AABB beneathBox = getBoundingBox().move(down);
            for (final Entity entity : level.getEntities(this, beneathBox)) {
                if (entity.isPushable()) {
                    JUtils.addVelocity(entity, entity.position().subtract(position()).normalize().scale(0.1));
                }
            }
        }

        final int hurtTime = getHurtTime();
        if (hurtTime > 0) setHurtTime(hurtTime - 1);
    }

    public abstract void movementTick(final boolean w, final boolean a, final boolean s, final boolean d, final boolean space, final boolean sneak);

    @Override
    public boolean hurt(final @NonNull DamageSource source, float amount) {
        if (super.hurt(source, amount)) {
            setHurtTime(5);
            return true;
        }

        return false;
    }

    public float getGroundFriction() { // Lifted from Boat.getGroundFriction()
        AABB aABB = getBoundingBox();
        AABB aABB2 = new AABB(aABB.minX, aABB.minY - 0.001, aABB.minZ, aABB.maxX, aABB.minY, aABB.maxZ);
        int i = Mth.floor(aABB2.minX) - 1;
        int j = Mth.ceil(aABB2.maxX) + 1;
        int k = Mth.floor(aABB2.minY) - 1;
        int l = Mth.ceil(aABB2.maxY) + 1;
        int m = Mth.floor(aABB2.minZ) - 1;
        int n = Mth.ceil(aABB2.maxZ) + 1;
        VoxelShape voxelShape = Shapes.create(aABB2);
        float f = 0.0F;
        int o = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for(int p = i; p < j; ++p) {
            for(int q = m; q < n; ++q) {
                int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
                if (r != 2) {
                    for(int s = k; s < l; ++s) {
                        if (r <= 0 || s != k && s != l - 1) {
                            mutableBlockPos.set(p, s, q);
                            BlockState blockState = this.level().getBlockState(mutableBlockPos);
                            if (!(blockState.getBlock() instanceof WaterlilyBlock) && Shapes.joinIsNotEmpty(blockState.getCollisionShape(level(), mutableBlockPos).move(p, s, q), voxelShape, BooleanOp.AND)) {
                                f += blockState.getBlock().getFriction();
                                ++o;
                            }
                        }
                    }
                }
            }
        }

        return f / (float)o;
    }

    @Override
    public @NonNull Iterable<ItemStack> getArmorSlots() { return Collections.emptyList(); }
    @Override
    public @NonNull ItemStack getItemBySlot(final @NonNull EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override
    public void setItemSlot(final @NonNull EquipmentSlot slot, final @NonNull ItemStack stack) { }
    @Override
    public @NonNull HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof LivingEntity livingEntity) return livingEntity;
        return null;
    }

}
