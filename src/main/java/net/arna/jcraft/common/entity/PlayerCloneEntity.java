package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.ai.goal.CloneAttackGoal;
import net.arna.jcraft.common.util.IOwnable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;

public class PlayerCloneEntity extends HostileEntity implements RangedAttackMob, IOwnable {
    public PlayerCloneEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.armorDropChances, 1F);
        Arrays.fill(this.handDropChances, 1F);
        updateAttackType();
        navigation = getNavigation();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final BowAttackGoal<PlayerCloneEntity> bowAttackGoal = new BowAttackGoal(this, 1.0, 30, 15.0F);
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

    public boolean switched = false; // Has this clone switched to a thin version?
    public PlayerCloneEntity switchedTo; // The thin clone instance

    private LivingEntity persistTarget = null;
    private LivingEntity master;
    private final EntityNavigation navigation;
    private int disabledSlots;


    static {
        MASTERNAME = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.STRING);
        SAND = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    @Override
    public LivingEntity getMaster() {
        return master;
    }
    @Override
    public void setMaster(LivingEntity m) {
        this.master = m;
        Text mName = m.getName();
        setCustomName(mName);
        setMasterName(mName.getString());
    }
    private static final TrackedData<String> MASTERNAME;
    public String getMasterName() { return dataTracker.get(MASTERNAME); }
    private void setMasterName(String state) { dataTracker.set(MASTERNAME, state); }

    private static final TrackedData<Boolean> SAND;
    public boolean isSand() { return dataTracker.get(SAND); }
    public void markSand() {
        dataTracker.set(SAND, true);
        TheFoolEntity.applySandCloneModifiers(this);
    }

    private static final String unsetMasterName = "%unset_master_name";
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(MASTERNAME, unsetMasterName);
        dataTracker.startTracking(SAND, false);
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
        nbt.putString("MasterName", getMasterName());
        nbt.putInt("DisabledSlots", disabledSlots);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setMasterName(nbt.getString("MasterName"));
        disabledSlots = nbt.getInt("DisabledSlots");
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
        if (player != master)
            return ActionResult.FAIL;

        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.NAME_TAG)) {
            if (player.isSpectator()) {
                return ActionResult.SUCCESS;
            } else if (player.world.isClient) {
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
    }

    @Override
    public void tick() {
        super.tick();
        if (switched && switchedTo.age > 10) { // Remove outdated clones
            discard();
            return;
        }

        boolean client = this.world.isClient();

        if (client) {
            if (isSand() && age % 4 == 0)
                world.addParticle(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState()),
                        getX() + getRandom().nextTriangular(0, 0.5),
                        getRandomBodyY(),
                        getZ() + getRandom().nextTriangular(0, 0.5)
                        , 0, 0, 0
                );

            JCraft.getClientEntityHandler().playerCloneEntityClientTick(this);
        } else if (master == null) {
            // Run every 2 seconds (player lists are rather expensive)
            if (age % 40 == 0) {
                // If the master name is set, but the master isn't (when loaded via NBT data), find master
                String masterName = this.getMasterName();
                if (!masterName.equals(unsetMasterName))
                    for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world((ServerWorld) world))
                        if (serverPlayerEntity.getName().getString().equals(masterName))
                            this.master = serverPlayerEntity;
            }

            LivingEntity attacker = getAttacker();
            if (attacker != null) setTarget(attacker);
        } else { // Server & Master isn't null
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
        if (target == master) return false;
        return super.canTarget(target);
    }

    public void updateAttackType() {
        if (world != null && !world.isClient) {
            goalSelector.remove(this.cloneAttackGoal);
            goalSelector.remove(this.bowAttackGoal);
            ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
            if (itemStack.isOf(Items.BOW))
                goalSelector.add(2, this.bowAttackGoal);
            else
                goalSelector.add(2, this.cloneAttackGoal);
        }
    }

    // Ranged attack handling
    public void attack(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getArrowType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * 0.2, f, 1.6F, 2f);
        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(persistentProjectileEntity);
    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
    }
}
