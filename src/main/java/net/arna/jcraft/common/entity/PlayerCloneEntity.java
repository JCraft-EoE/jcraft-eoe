package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.ai.goal.CloneAttackGoal;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;

public class PlayerCloneEntity extends HostileEntity implements RangedAttackMob {

    public PlayerCloneEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.armorDropChances, 1F);
        Arrays.fill(this.handDropChances, 1F);
        this.updateAttackType();
    }

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

    public boolean sandClone = false;
    public boolean switched = false; // Has this clone switched to a thin version?
    public PlayerCloneEntity switchedTo; // The thin clone instance

    private LivingEntity persistTarget = null;
    private LivingEntity owner;
    private int disabledSlots;
    private static final TrackedData<String> OWNERNAME;

    static {
        OWNERNAME = DataTracker.registerData(PlayerCloneEntity.class, TrackedDataHandlerRegistry.STRING);
    }

    public LivingEntity getOwner() {
        return this.owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        Text oName = owner.getName();
        this.setCustomName(oName);
        this.setOwnerName(oName.getString());
    }

    public String getOwnerName() {
        return this.dataTracker.get(OWNERNAME);
    }

    public void setOwnerName(String state) {
        this.dataTracker.set(OWNERNAME, state);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(OWNERNAME, "%unset_owner_name");
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
        nbt.putString("OwnerName", this.getOwnerName());
        nbt.putInt("DisabledSlots", this.disabledSlots);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setOwnerName(nbt.getString("OwnerName"));
        this.disabledSlots = nbt.getInt("DisabledSlots");
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
        if (player != owner)
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

        boolean client = this.world.isClient();

        if (client) {
            if (this.age == 1 && this.getType() == JEntityTypeRegister.PLAYERCLONE) {
                // If the one running this instance of tick() is the owner of the clone, check for a thin model and apply if found via server message
                // This is in fact an entirely clientside process and can be considered a "security flaw",
                // but I really doubt anyone would care if someone turned all their clones thin
                ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
                if (clientPlayer != null) {
                    if (this.getOwnerName().equals(clientPlayer.getName().getString()) && clientPlayer.getModel().equals("slim")) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeShort(12);
                        buf.writeUuid(this.getUuid());
                        ClientPlayNetworking.send(JCraft.standControlChannel, buf);
                    }
                }
            }
        } else {
            if (this.switched && this.switchedTo.age > 10) {
                this.discard();
            } // Remove outdated clones

            if (this.owner == null) {
                // Run every 2 seconds (player lists are rather expensive)
                if (this.age % 40 == 0) {
                    // If the owner name is set, but the owner isn't (when loaded via NBT data), find owner
                    String ownerName = this.getOwnerName();
                    if (!ownerName.equals("%unset_owner_name")) {
                        ServerWorld serverWorld = (ServerWorld) this.world;
                        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world(serverWorld)) {
                            if (serverPlayerEntity.getName().getString().equals(ownerName)) {
                                this.owner = serverPlayerEntity;
                            }
                        }
                    }
                }

                LivingEntity attacker = this.getAttacking();
                if (attacker != null) {
                    this.setTarget(attacker);
                }
            } else {
                if (this.persistTarget == null) {
                    LivingEntity attacking = owner.getAttacking();
                    if (attacking != null && attacking.isAlive()) {
                        this.persistTarget = attacking;
                    }

                    if (this.squaredDistanceTo(this.owner) > 100) {
                        this.getNavigation().startMovingTo(this.owner, 1);
                    }
                } else if (this.persistTarget.isAlive() && this.canTarget(this.persistTarget)) {
                    this.setTarget(this.persistTarget);
                } else { // This is called once, usually when the opponent dies
                    this.persistTarget = null;
                    this.setTarget(null);
                    if (!this.getNavigation().isIdle()) {
                        this.getNavigation().stop();
                    }
                }
            }
        }
    }

    public void updateAttackType() {
        if (this.world != null && !this.world.isClient) {
            this.goalSelector.remove(this.cloneAttackGoal);
            this.goalSelector.remove(this.bowAttackGoal);
            ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
            if (itemStack.isOf(Items.BOW)) {
                this.goalSelector.add(2, this.bowAttackGoal);
            } else {
                this.goalSelector.add(2, this.cloneAttackGoal);
            }
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
