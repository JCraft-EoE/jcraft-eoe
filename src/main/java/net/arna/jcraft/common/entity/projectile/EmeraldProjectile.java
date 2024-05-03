package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EmeraldProjectile extends PersistentProjectileEntity implements GeoEntity {
    private int ticksInAir;
    private int bouncesLeft = 5;
    private boolean reflect = false;

    public EmeraldProjectile(EntityType<? extends EmeraldProjectile> entityType, World world) {
        super(entityType, world);
    }

    public EmeraldProjectile(World world) {
        super(JEntityTypeRegistry.EMERALD, world);
    }

    public EmeraldProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.EMERALD, owner, world);
        setNoGravity(true);
        setOwner(owner);
        setSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK);
    }

    public void withReflect() {
        reflect = true;
    }

    @Override
    public ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    private static final BlockStateParticleEffect EMERALD_PARTICLE = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.EMERALD_BLOCK.getDefaultState());

    @Override
    protected void age() {
        discard();
    }



    @Override
    public void tick() {
        super.tick();

        if (!inGround) {
            ++ticksInAir;
        } else {
            if (getWorld().isClient) {
                double x = getX();
                double y = getY();
                double z = getZ();

                for (int i = 0; i < 8; i++)
                    getWorld().addParticle(EMERALD_PARTICLE, x, y, z,
                            random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
            }
        }

        if (getWorld().isClient) {
            if (random.nextGaussian() < -0.002) {
                double x = getX();
                double y = getY();
                double z = getZ();
                getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z,
                        random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
            }
            return;
        }

        if (ticksInAir > 200)
            discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (reflect) {
            HitResult.Type type = hitResult.getType();
            if (type == HitResult.Type.ENTITY) {
                this.onEntityHit((EntityHitResult) hitResult);
                this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
            } else if (type == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                if (bouncesLeft-- > 0) {
                    Vec3i normal = blockHitResult.getSide().getVector();
                    setVelocity(getVelocity().add(Vec3d.of(normal)).normalize());
                } else {
                    this.onBlockHit(blockHitResult);
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Emitter.of(this, this.getWorld().getBlockState(blockPos)));
                }
            }
        } else super.onCollision(hitResult);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (getWorld().isClient) return;
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (owner != null && owner.hasPassenger(entity) || entity == owner) return;
        if (entity instanceof JAttackEntity attackEntity && attackEntity.getMaster() == owner) return;

        if (isOnFire()) entity.setOnFireFor(5);

        int blockstun = 4;
        int stunT = 10;

        JUtils.projectileDamageLogic(this, getWorld(), entity, Vec3d.ZERO, stunT, 1, false, 1, blockstun, HitPropertyComponent.HitAnimation.MID);
        playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1, 1);
        discard();
    }

    @Override
    protected float getDragInWater() {
        // Not actually drag, just a multiplier
        return 0.8F;
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
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
