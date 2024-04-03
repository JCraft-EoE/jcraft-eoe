package net.arna.jcraft.common.entity;

import com.mojang.authlib.GameProfile;
import net.arna.jcraft.common.entity.ai.goal.CloneAttackGoal;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class PlayerCloneEntity extends HostileEntity implements RangedAttackMob, IOwnable {
    private static final TrackedData<Optional<UUID>> MASTER;
    private static final TrackedData<String> MASTER_NAME;
    private static final TrackedData<Boolean> SAND, RENDER_FOR_MASTER;
    private static final TrackedData<Byte> PART_MASK;
    private final BowAttackGoal<PlayerCloneEntity> bowAttackGoal = new BowAttackGoal<>(this, 1.0, 30, 15.0F);
    private final CloneAttackGoal cloneAttackGoal = new CloneAttackGoal(this, 1) {
        public void stop() {
            super.stop();
            PlayerCloneEntity.this.setAttacking(false);
        }

        public void start() {
            super.start();
            PlayerCloneEntity.this.setAttacking(true);
        }
    };
    private boolean allowItemExchange = true;

    private GameProfile gameProfile;

    private LivingEntity persistTarget = null;
    private LivingEntity master;
    private int cooldown, maxCooldown;
    private final EntityNavigation navigation;
    private int disabledSlots;

    static {
        MASTER = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        MASTER_NAME = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.STRING);
        SAND = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        RENDER_FOR_MASTER = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        PART_MASK = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.BYTE);
    }

    public PlayerCloneEntity(World world) {
        super(JEntityTypeRegistry.PLAYER_CLONE, world);
        Arrays.fill(armorDropChances, 2.0F);
        Arrays.fill(handDropChances, 2.0F);

        updateAttackType();

        navigation = getNavigation();
        cooldown = 0;
        maxCooldown = 10;
    }

    public void disableDrops() {
        Arrays.fill(armorDropChances, 0.0F);
        Arrays.fill(handDropChances, 0.0F);
    }

    public void disableItemExchange() {
        allowItemExchange = false;
    }

    @Override
    public LivingEntity getMaster() {
        return master;
    }

    public GameProfile getGameProfile() {
        if ((gameProfile == null || gameProfile.getId() == null || gameProfile.getName() == null ||
                !gameProfile.getId().equals(getMasterId()) || !gameProfile.getName().equals(getMasterName())) &&
                getMasterId() != null && getMasterName() != null)
            gameProfile = new GameProfile(getMasterId(), getMasterName());

        return gameProfile;
    }

    @Override
    public void setMaster(LivingEntity m) {
        this.master = m;
        Text mName = m.getName();
        setCustomName(mName);
        dataTracker.set(MASTER, Optional.of(m.getUuid()));
        dataTracker.set(MASTER_NAME, m.getEntityName());

        if (!(m instanceof ServerPlayerEntity player)) return;
        byte partMask = 0;
        for (PlayerModelPart part : PlayerModelPart.values())
            if (player.isPartVisible(part))
                partMask |= (byte) part.getBitFlag();

        dataTracker.set(PART_MASK, partMask);
        setLeftHanded(player.getMainArm() == Arm.LEFT);
    }

    public UUID getMasterId() {
        return dataTracker.get(MASTER).orElse(null);
    }

    public String getMasterName() {
        return dataTracker.get(MASTER_NAME);
    }

    public boolean shouldRenderForMaster() {
        return dataTracker.get(RENDER_FOR_MASTER);
    }

    public void setShouldRenderForMaster(boolean shouldRenderForMaster) {
        dataTracker.set(RENDER_FOR_MASTER, shouldRenderForMaster);
    }

    public boolean isSand() {
        return dataTracker.get(SAND);
    }

    public void markSand() {
        dataTracker.set(SAND, true);
    }

    public byte getPartMask() {
        return dataTracker.get(PART_MASK);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(MASTER, Optional.empty());
        dataTracker.startTracking(MASTER_NAME, null);
        dataTracker.startTracking(SAND, false);
        dataTracker.startTracking(RENDER_FOR_MASTER, true);
        dataTracker.startTracking(PART_MASK, (byte) 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(5, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putUuid("Master", getMasterId());
        nbt.putString("MasterName", getMasterName());
        nbt.putInt("DisabledSlots", disabledSlots);
        nbt.putByte("PartMask", getPartMask());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Master")) { // If one is here, then the rest should be too (unless the player manually modified NBT)
            dataTracker.set(MASTER, Optional.ofNullable(nbt.getUuid("Master")));
            dataTracker.set(MASTER_NAME, nbt.getString("MasterName"));
            disabledSlots = nbt.getInt("DisabledSlots");
            dataTracker.set(PART_MASK, nbt.getByte("PartMask"));
        }
        updateAttackType();
    }

    // Equipment handling
    private EquipmentSlot getSlotFromPosition(Vec3d hitPos) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        double d = hitPos.y;
        EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
        if (d >= 0.1 && d < 0.55 && this.hasStackEquipped(equipmentSlot2)) {
            equipmentSlot = EquipmentSlot.FEET;
        } else if (d >= 0.9 && d < 1.6 && this.hasStackEquipped(EquipmentSlot.CHEST)) {
            equipmentSlot = EquipmentSlot.CHEST;
        } else if (d >= 0.4 && d < 1.2 && this.hasStackEquipped(EquipmentSlot.LEGS)) {
            equipmentSlot = EquipmentSlot.LEGS;
        } else if (d >= 1.6 && this.hasStackEquipped(EquipmentSlot.HEAD)) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (!this.hasStackEquipped(EquipmentSlot.MAINHAND) && this.hasStackEquipped(EquipmentSlot.OFFHAND)) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        }

        return equipmentSlot;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if (player != master || !player.isSneaking() || !allowItemExchange)
            return ActionResult.FAIL;

        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.NAME_TAG)) {
            if (player.isSpectator()) {
                return ActionResult.SUCCESS;
            } else if (player.getWorld().isClient) {
                return ActionResult.CONSUME;
            } else {
                EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
                if (itemStack.isEmpty()) {
                    EquipmentSlot equipmentSlot2 = getSlotFromPosition(hitPos);
                    EquipmentSlot equipmentSlot3 = this.isSlotDisabled(equipmentSlot2) ? equipmentSlot : equipmentSlot2;
                    if (this.hasStackEquipped(equipmentSlot3) && this.equip(player, equipmentSlot2, itemStack, hand)) {
                        return ActionResult.SUCCESS;
                    }
                } else {
                    if (this.isSlotDisabled(equipmentSlot))
                        return ActionResult.FAIL;
                    if (this.equip(player, equipmentSlot, itemStack, hand))
                        return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            }
        } else {
            return ActionResult.PASS;
        }
    }

    private boolean isSlotDisabled(EquipmentSlot slot) {
        return (this.disabledSlots & 1 << slot.getEntitySlotId()) != 0;
    }

    private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
        ItemStack itemStack = this.getEquippedStack(slot);
        if (!itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getEntitySlotId() + 8) != 0) {
            return false;
        } else if (itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getEntitySlotId() + 16) != 0) {
            return false;
        } else {
            ItemStack itemStack2;
            if (player.getAbilities().creativeMode && itemStack.isEmpty() && !stack.isEmpty()) {
                itemStack2 = stack.copy();
                itemStack2.setCount(1);
                this.equipStack(slot, itemStack2);
                return true;
            } else if (!stack.isEmpty() && stack.getCount() > 1) {
                if (!itemStack.isEmpty()) {
                    return false;
                } else {
                    itemStack2 = stack.copy();
                    itemStack2.setCount(1);
                    this.equipStack(slot, itemStack2);
                    stack.decrement(1);
                    return true;
                }
            } else {
                this.equipStack(slot, stack);
                player.setStackInHand(hand, itemStack);
                return true;
            }
        }
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        super.equipStack(slot, stack);
        updateAttackType();
        if (slot == EquipmentSlot.MAINHAND) {
            double maxCooldown = 10.0;
            Collection<EntityAttributeModifier> attackSpeedModifiers = getMainHandStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED);
            for (EntityAttributeModifier attackSpeedModifier : attackSpeedModifiers)
                maxCooldown *= -attackSpeedModifier.getValue();
            if (maxCooldown < 0)
                maxCooldown = 0;

            this.maxCooldown = (int) maxCooldown;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClient()) {
            if (isSand() && age % 4 == 0)
                getWorld().addParticle(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState()),
                        getX() + getRandom().nextTriangular(0, 0.5),
                        getRandomBodyY(),
                        getZ() + getRandom().nextTriangular(0, 0.5)
                        , 0, 0, 0
                );

            //JCraft.getClientEntityHandler().playerCloneEntityClientTick(this);
        } else if (master == null) {
            // Run every 2 seconds (player lists are rather expensive)
            if (age % 40 == 0) {
                // If the master id is set, but the master isn't (when loaded via NBT data), find master
                UUID master = this.getMasterId();
                if (master != null)
                    for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world((ServerWorld) getWorld()))
                        if (serverPlayerEntity.getUuid().equals(master))
                            this.master = serverPlayerEntity;
            }

            LivingEntity attacker = getAttacker();
            if (attacker != null) setTarget(attacker);
        } else { // Serverside, & Master isn't null
            cooldown--;

            if (persistTarget == null) {
                // Prioritize what the master is attacking, then what is attacking him
                LivingEntity attacking = master.getAttacking();
                if (attacking != null && attacking.isAlive())
                    persistTarget = attacking;

                LivingEntity attacker = master.getAttacker();
                if (attacker != null && attacker.isAlive())
                    persistTarget = attacker;

                if (squaredDistanceTo(master) > 100)
                    navigation.startMovingTo(master, 1);
            } else if (persistTarget.isAlive() && canTarget(persistTarget)) {
                this.setTarget(this.persistTarget);
            } else { // This is called once, usually when the opponent dies
                persistTarget = null;
                this.setTarget(null);
                if (!navigation.isIdle())
                    navigation.stop();
            }
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return target != master && target != this &&
                (!(target instanceof PlayerCloneEntity clone) || !clone.getMasterId().equals(getMasterId())) &&
                super.canTarget(target);
    }

    public void updateAttackType() {
        if (getWorld() == null || getWorld().isClient) return;
        goalSelector.remove(this.cloneAttackGoal);
        goalSelector.remove(this.bowAttackGoal);
        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        if (itemStack.isOf(Items.BOW))
            goalSelector.add(2, this.bowAttackGoal);
        else goalSelector.add(2, this.cloneAttackGoal);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    // Ranged attack handling
    public void attack(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.33) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * 0.2, f, 1.6F, 2f);
        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getWorld().spawnEntity(persistentProjectileEntity);
    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
    }

    public static DefaultAttributeContainer.Builder createCloneAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void startCooldown() {
        this.cooldown = maxCooldown;
    }
}
