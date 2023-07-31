package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.SilverChariotEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class RapierProjectile extends PersistentProjectileEntity implements IAnimatable {
    public static final Identifier POSSESSED_TEXTURE = JCraft.id("textures/entity/stands/silver_chariot/rapier_possessed.png");
    public static final Identifier ARMOR_OFF_TEXTURE = JCraft.id("textures/entity/stands/silver_chariot/rapier_no_armor.png");
    private static final TrackedData<Integer> SKIN;
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private StandEntity<?, ?> origin;
    private int ticksInAir, bouncesLeft = 5;

    static {
        SKIN = DataTracker.registerData(RapierProjectile.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public RapierProjectile(EntityType<? extends RapierProjectile> entityType, World world) {
        super(entityType, world);
    }

    public RapierProjectile(World world, LivingEntity owner, StandEntity<?, ?> silverChariot) {
        super(JEntityTypeRegistry.RAPIER, owner, world);
        this.setOwner(owner);
        this.origin = silverChariot;
    }

    public int getSkin() {
        return this.dataTracker.get(SKIN);
    }

    public void setSkin(int skin) {
        this.dataTracker.set(SKIN, skin);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SKIN, 0);
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(JObjectRegistry.KNIFE);
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient) {
            if (!inGround) {
                Vec3d vel = getVelocity();
                for (double i = 0; i < 3.0; i++)
                    world.addParticle(
                            ParticleTypes.ELECTRIC_SPARK,
                            MathHelper.lerp(i / 3.0, getX(), prevX), MathHelper.lerp(i / 3.0, getY(), prevY), MathHelper.lerp(i / 3.0, getZ(), prevZ),
                            vel.x, vel.y, vel.z
                    );
            }
        } else if (origin == null || !origin.isAlive() || ++ticksInAir > 640)
            discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onEntityHit((EntityHitResult) hitResult);
            this.world.emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            if (bouncesLeft-- > 0) {
                Vec3i normal = blockHitResult.getSide().getVector();
                addVelocity(normal.getX(), normal.getY(), normal.getZ());
            } else {
                this.onBlockHit(blockHitResult);
                BlockPos blockPos = blockHitResult.getBlockPos();
                this.world.emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Emitter.of(this, this.world.getBlockState(blockPos)));
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (world.isClient) return;
        Entity owner = this.getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;
        if (this.isOnFire()) entity.setOnFireFor(5);

        JUtils.projectileDamageLogic(this, world, entity, Vec3d.ZERO, 20, 1, false, 2, 6);
        playSound(SoundEvents.ITEM_TRIDENT_HIT, 1, 1);
        discard();
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        if (player == getOwner()) {
            if (((IEntityDataSaver) player).getStand() instanceof SilverChariotEntity silverChariot) {
                silverChariot.setHasRapier(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.ticksInAir = tag.getShort("life");
    }

    // Animations
    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
