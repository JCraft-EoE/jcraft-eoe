package net.arna.jcraft.common.entity.projectile;

import com.mojang.datafixers.util.Pair;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.api.spec.SpecTypeUtil;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.item.BloodBottleItem;
import net.arna.jcraft.common.spec.VampireSpec;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemTossProjectile extends AbstractArrow {

    protected static final EntityDataAccessor<ItemStack> ITEM;

    protected static final EntityDataAccessor<Integer> RICOCHETS;

    static {
        ITEM = SynchedEntityData.defineId(ItemTossProjectile.class, EntityDataSerializers.ITEM_STACK);
        RICOCHETS = SynchedEntityData.defineId(ItemTossProjectile.class, EntityDataSerializers.INT);
    }

    public ItemTossProjectile(final Level level) {
        super(JEntityTypeRegistry.ITEM_TOSS_PROJECTILE.get(), level);
        setItem(ItemStack.EMPTY);
    }

    public ItemTossProjectile(final LivingEntity shooter, final Level level, final ItemStack item) {
        super(JEntityTypeRegistry.ITEM_TOSS_PROJECTILE.get(), shooter, level);
        setItem(item);
        if (getItem().is(JTagRegistry.HEAVY_IMPACT)) {
            this.setBaseDamage(2d);
            this.setKnockback(4);
        }
        else {
            this.setBaseDamage(0d);
            this.setKnockback(0);
        }
    }

    public ItemStack getItem() {
        return this.entityData.get(ITEM);
    }

    protected void setItem(@NotNull ItemStack item) {
        this.entityData.set(ITEM, item.copyWithCount(1));
    }

    public int getRicochets() {
        return this.entityData.get(RICOCHETS);
    }

    protected void incrementRicochets() {
        this.entityData.set(RICOCHETS, getRicochets() + 1);
    }

    @Override
    protected ItemStack getPickupItem() {
        return getItem();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ITEM, ItemStack.EMPTY);
        this.entityData.define(RICOCHETS, 0);
    }

    @Override
    public boolean isOnFire() {
        // this might be better to move to doPostHurtEffects
        return getItem().is(JTagRegistry.BURNS_ON_IMPACT);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        if (getItem().is(JTagRegistry.BLINDS_ON_IMPACT)) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40));
        }
        if (getItem().is(JTagRegistry.SLOWS_ON_IMPACT)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60));
        }
        if (getItem().is(JTagRegistry.POISONS_ON_IMPACT)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 60));
        }
    }

    protected boolean maybeExplode() {
        if (!level().isClientSide && (getItem().is(JTagRegistry.EXPLODES_ON_IMPACT) ||
                (getOwner() instanceof AbstractKillerQueenEntity<?,?>))) {
            final boolean grief = level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            level().explode(this, getX(), getY(), getZ(), 1, grief, Level.ExplosionInteraction.MOB);
            discard();
            return true;
        }
        return false;
    }

    @Override
    protected void onHit(HitResult result) {
        final HitResult.Type type = result.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)result);
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)result;
            this.onHitBlock(blockHitResult);
        }
    }

    @Override
    protected void onHitEntity(final EntityHitResult result) {
        // this part has been heavily inspired by AbstractArrow
        Entity entity = result.getEntity();
        Entity entity2 = this.getOwner();
        DamageSource damageSource;
        if (entity2 == null) {
            damageSource = this.damageSources().arrow(this, this);
        } else {
            damageSource = this.damageSources().arrow(this, entity2);
            if (entity2 instanceof LivingEntity) {
                ((LivingEntity)entity2).setLastHurtMob(entity);
            }
        }

        boolean bl = entity.getType() == EntityType.ENDERMAN;
        if (this.isOnFire() && !bl) {
            entity.setSecondsOnFire(5);
        }

        entity.hurt(damageSource, (float)getBaseDamage());
        if (bl) {
            return;
        }

        boolean effectActivated = false;

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            // handle knockback
            if (this.getKnockback() > 0) {
                double d = Math.max(0, 1f - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                Vec3 vec3 = this.getDeltaMovement().multiply(1f, 0f, 1f).normalize().scale(getKnockback() * 0.6 * d);
                if (vec3.lengthSqr() > 0) {
                    livingEntity.push(vec3.x, 0.1, vec3.z);
                }
            }
//            if (!this.level().isClientSide && entity2 instanceof LivingEntity) {
//                EnchantmentHelper.doPostHurtEffects(livingEntity, entity2);
//                EnchantmentHelper.doPostDamageEffects((LivingEntity)entity2, livingEntity);
//            }
            this.doPostHurtEffects(livingEntity);
//            if (entity2 != null && livingEntity != entity2 && livingEntity instanceof Player && entity2 instanceof ServerPlayer && !this.isSilent()) {
//                ((ServerPlayer)entity2).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
//            }
        }

        if (maybeExplode()) {
            return;
        }

        // force stand on target
        if (entity instanceof LivingEntity livingEntity && (livingEntity instanceof ServerPlayer ||
                livingEntity.getType().is(JTagRegistry.CAN_HAVE_STAND)) && getItem().is(JItemRegistry.STAND_DISC.get())) {
            // get NBT
            StandType itemStand = null;
            int itemSkin = 0;
            final CompoundTag data = getItem().getOrCreateTag();
            if (data.contains("StandID")) {
                itemStand = StandTypeUtil.readFromNBT(data, "StandID");
            }
            if (data.contains("Skin", Tag.TAG_INT)) {
                itemSkin = data.getInt("Skin");
            }
            // apply stand
            if (itemStand != null && !JCraft.getExclusiveStandsData().isStandUsed(itemStand)) {
                final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(livingEntity);
                if (standData.getType() == null) { // don't override current stand
                    standData.setTypeAndSkin(itemStand, itemSkin);
                    JCraft.summon(level(), livingEntity);
                }
                effectActivated = true;
            }
        }
        // force equip thrown stuff
        if (entity instanceof Mob mob && getItem().is(JTagRegistry.EQUIPABLES)) {
            mob.equipItemIfPossible(getItem());
            effectActivated = true;
        }

        // TODO spec obtainment items

        // TODO stand upgrade items

        // force feed
        if (entity instanceof LivingEntity livingEntity && getItem().isEdible()) {
            // FIXME Vampires can't be force-fed blood bottles
            if (getItem().getItem() instanceof BloodBottleItem && livingEntity instanceof Player player) {
                final CommonVampireComponent vampireComponent = JComponentPlatformUtils.getVampirism(player);
                if (vampireComponent.isVampire()) {
                    final CompoundTag nbt = getItem().getOrCreateTag();
                    int blood = (int)Math.floor(nbt.getFloat("Blood"));
                    vampireComponent.setBlood(Math.min(VampireSpec.MAX_BLOOD, vampireComponent.getBlood() + blood));
                    effectActivated = true;
                }
            }
            else if (livingEntity instanceof Player player) {
                if (!JComponentPlatformUtils.getVampirism(player).isVampire()) {
                    livingEntity.eat(level(), getItem());
                    if (getItem().getItem() instanceof BowlFoodItem) {
                        setItem(Items.BOWL.getDefaultInstance());
                        dropItem(result.getLocation()); // FIXME doesn't work?
                    }
                    effectActivated = true;
                }
            }
        }

        // slab iron on iron golem
        if (entity instanceof IronGolem golem && getItem().is(Items.IRON_INGOT)) {
            float prevHealHealth = golem.getHealth();
            golem.heal(25.0F);
            if (golem.getHealth() != prevHealHealth) {
                float g = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
                this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, g);
            }
            effectActivated = true;
        }

        // force equip saddles
        if (entity instanceof Saddleable saddleable && getItem().is(Items.SADDLE)) {
            saddleable.equipSaddle(null);float g = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            this.playSound(saddleable.getSaddleSoundEvent(), 1.0F, g);
            effectActivated = true;
        }

        // force equip horse armor
        if (entity instanceof AbstractHorse horse && horse.isArmor(getItem())) {
            // this is needed because the equipArmor method of the horse wants a player
            horse.inventory.setItem(1, getItem());
            effectActivated = true;
        }

        // TODO also, what about throwing a sword, bone meal, fire charge, shears,

        // end of inspired part
        if (!this.level().isClientSide && getItem().is(JTagRegistry.EXPLODES_ON_IMPACT)) {
            entity.hurt(this.damageSources().arrow(this, entity2), 6f);
        }
        // a little bit more inspiration
        if (this.getPierceLevel() <= 0) {
            // drop if not used
            if (!(effectActivated || getItem().is(JTagRegistry.BLINDS_ON_IMPACT) ||
                    getItem().is(JTagRegistry.BURNS_ON_IMPACT) ||
                    getItem().is(JTagRegistry.HEAVY_IMPACT) ||
                    getItem().is(JTagRegistry.EXPLODES_ON_IMPACT) ||
                    getItem().is(JTagRegistry.SLOWS_ON_IMPACT) ||
                    getItem().is(JTagRegistry.POISONS_ON_IMPACT))) {
                dropItem(result.getLocation());
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(final BlockHitResult result) {
        final BlockState blockstate = level().getBlockState(result.getBlockPos());
        // notify the block it has been hit with something
        blockstate.onProjectileHit(level(), blockstate, result, this);

        final BlockState hitBlock = level().getBlockState(result.getBlockPos());
        double hardness = hitBlock.getBlock().defaultDestroyTime();
        if (hardness < 0d) { // unbreakable
            hardness = Double.MAX_VALUE;
        }

        if (maybeExplode()) {
            return;
        }

        // ricochet
        incrementRicochets();
        if (getRicochets() <= maxRicochets()) {
            // calculate penetrative value and decide if it should land
            final Vec3i intNormal = result.getDirection().getNormal();
            final Vec3 normal = new Vec3(intNormal.getX(), intNormal.getY(), intNormal.getZ());
            final Vec3 impactVec = getDeltaMovement();

            // a*b = |a|*|b|*cos(φ) , a*b = a.dotProduct(b)
            final double impactAngleRad = Math.acos(normal.dot(impactVec.normalize())) - Math.PI / 2.0;
            final double impactAngleDeg = Math.toDegrees(impactAngleRad);

            // Ek = mv^2/2
            final double kineticEnergy = mass() * impactVec.lengthSqr() / 2;
            final double penAngle = penetrationAngle() + hardness * 5; // This is bs but so is minecraft physics

            final boolean lowEnergy = kineticEnergy < 0.001;
            if (impactAngleDeg <= penAngle && !lowEnergy) { // If penetrated or ran out of energy
                setDeltaMovement(impactVec.add(normal).scale(0.5 / hardness));
                // don't hit the ground
                return;
            }
        }

        // hit the ground
        final ItemStack item = getItem();
        // blocks get placed if possible
        final BlockPos pos = result.getBlockPos().relative(result.getDirection());
        if (item.getItem() instanceof BlockItem block) {
            if (item.is(JTagRegistry.BRITTLE) && hardness >= Blocks.STONE.defaultDestroyTime()) {
                // brittle things get destroyed
            }
            else if (InteractionResult.SUCCESS != block.place(new BlockPlaceContext(new UseOnContext(level(), null, InteractionHand.MAIN_HAND, item, result)))) {
                dropItem(result.getLocation());
            }
        }
        // axe get used
        else if (item.getItem() instanceof AxeItem axe) {
            BlockState blockState = level().getBlockState(result.getBlockPos());
            Optional<BlockState> stripped = axe.getStripped(blockState);
            if (stripped.isPresent()) {
                level().setBlockAndUpdate(result.getBlockPos(), stripped.get());
                item.hurtAndBreak(1, (LivingEntity)getOwner(), owner -> {/* do nothing */});
            }
            dropItem(result.getLocation());
        }
        // hoe get used
        else if (item.getItem() instanceof HoeItem) {
            final UseOnContext context = new UseOnContext(level(), null, InteractionHand.MAIN_HAND, item, result);
            final Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = HoeItem.TILLABLES.get(level().getBlockState(result.getBlockPos()).getBlock());
            if (pair != null) {
                Predicate<UseOnContext> predicate = pair.getFirst();
                Consumer<UseOnContext> consumer =    pair.getSecond();
                if (predicate.test(context)) { // check if tilling is possible
                    if (!level().isClientSide) {
                        consumer.accept(context); // actual tilling happening
                        context.getItemInHand().hurtAndBreak(1, (LivingEntity) getOwner(), owner -> {});
                    }
                }
            }
            dropItem(result.getLocation());
        }
        // buckets empty their content if any
        else if (item.getItem() instanceof BucketItem bucket && bucket.content != Fluids.EMPTY) {
            if (bucket.emptyContents(null, level(), result.getBlockPos(), result)) {
                bucket.checkExtraContent(null, level(), item, pos);
                setItem(Items.BUCKET.getDefaultInstance());
            }
            dropItem(result.getLocation());
        }
        // music disks fling into a jukebox
        else if (item.is(ItemTags.MUSIC_DISCS)) {
            final BlockState box = level().getBlockState(result.getBlockPos());
            if (box.is(Blocks.JUKEBOX) && !box.getValue(JukeboxBlock.HAS_RECORD)) {
                BlockEntity blockEntity = level().getBlockEntity(result.getBlockPos());
                if (blockEntity instanceof JukeboxBlockEntity boxEntity) {
                    boxEntity.setFirstItem(item.copy());
                }
                else { // shouldn't happen, but who knows?
                    dropItem(result.getLocation());
                }
            }
            else {
                dropItem(result.getLocation());
            }
        }
        // arrows get flinged
        // TODO implement
        // potions get activated
        else if (item.getItem() instanceof PotionItem potion) {
            final ThrownPotion thrown = new ThrownPotion(level(), result.getLocation().x, result.getLocation().y, result.getLocation().z);
            final Potion effect = PotionUtils.getPotion(item);
            thrown.setItem(item);
            if (potion instanceof LingeringPotionItem) {
                thrown.makeAreaOfEffectCloud(item, effect);
            }
            else {
                thrown.applySplash(PotionUtils.getMobEffects(item), null);
            }
            int i = effect.hasInstantEffects() ? 2007 : 2002;
            this.level().levelEvent(i, pos, PotionUtils.getColor(item));
        }
        // XP bottle gets activated
        else if (item.getItem() instanceof ExperienceBottleItem) {
            if (!level().isClientSide()) {
                level().levelEvent(2002, pos, PotionUtils.getColor(Potions.WATER));
                int amount = 3 + level().random.nextInt(5) + level().random.nextInt(5);
                ExperienceOrb.award((ServerLevel)level(), result.getLocation(), amount);
            }
        }
        // spawn eggs get activated
        else if (item.getItem() instanceof SpawnEggItem egg) {
            final EntityType<?> entityType2 = egg.getType(item.getTag());
            if (!level().isClientSide()) {
                entityType2.spawn((ServerLevel)level(), item, null, pos, MobSpawnType.SPAWN_EGG, true, false);
            }
        }
        // rest just get dropped
        else {
            dropItem(result.getLocation());
        }
        inGround = true;
        discard();
    }

    /**
     * Drops the item behind this projectile at the specified location. Does NOT discard this projectile.
     */
    private void dropItem(final Vec3 pos) {
        final Vec3 vec3 = pos.subtract(getX(), getY(), getZ());
        setDeltaMovement(vec3);
        final Vec3 vecN = vec3.normalize().scale(0.05d);
        Containers.dropItemStack(level(), getX() - vecN.x, getY() - vecN.y, getZ() - vecN.z, getItem());
    }

    /**
     * Maximal number of ricocheting this projectile can do.
     */
    public int maxRicochets() {
        if (getItem().is(JTagRegistry.SUPER_BOUNCY)) {
            return 15;
        }
        if (getItem().is(JTagRegistry.BOUNCY)) {
            return 10;
        }
        if (getItem().is(JTagRegistry.SOMEWHAT_BOUNCY)) {
            return 5;
        }
        return 0;
    }

    public double penetrationAngle() {
        if (getItem().is(JTagRegistry.ACUTE)) {
            return 15d;
        }
        if (getItem().is(JTagRegistry.OBTUSE)) {
            return 60d;
        }
        return 45d;
    }

    /**
     * Mass of the projectile.
     */
    public double mass() {
        if (getItem().is(JTagRegistry.VERY_HEAVY)) {
            return 50d;
        }
        if (getItem().is(JTagRegistry.HEAVY)) {
            return 10d;
        }
        if (getItem().is(JTagRegistry.LIGHT)) {
            return 0.1;
        }
        return 1d;
    }
}
