package net.arna.jcraft.common.entity;

import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

//todo: possibly merging multiple of these into a bird? it would be essentially another one of these but with more storage

public class GEButterflyEntity extends FlyingMob implements IOwnable {
    public GEButterflyEntity(EntityType<? extends FlyingMob> entityType, Level world) {
        super(entityType, world);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.navigation = new FlyingPathNavigation(this, world);
        Arrays.fill(this.handDropChances, 2.0F);
    }

    private boolean hasMaster = false;
    private LivingEntity master;

    @Override
    public LivingEntity getMaster() {
        return master;
    }

    @Override
    public void setMaster(LivingEntity m) {
        master = m;
        hasMaster = true;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        if (player == master) {
            if (player.getItemInHand(hand).isEmpty()) {
                player.setItemInHand(hand, getMainHandItem());
                discard();
            } else {
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            IDLE.sendForEntity(this);
        }

        if (!hasMaster) {
            return;
        }

        if (master == null) {
            kill();
            return;
        }

        if (distanceToSqr(master) > 16) {
            navigation.moveTo(master, 1.0);
        } else if (navigation.isInProgress()) {
            navigation.stop();
        }
    }

    public static AttributeSupplier.Builder createButterflyAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.FLYING_SPEED, 1.0)
                .add(Attributes.MAX_HEALTH, 10.0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("HasMaster", hasMaster);
        if (master instanceof Player player) {
            nbt.putUUID("MasterUUID", player.getUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        hasMaster = nbt.getBoolean("HasMaster");
        if (nbt.hasUUID("MasterUUID")) {
            setMaster(level().getPlayerByUUID(nbt.getUUID("MasterUUID")));
        }
    }

    public static final AzCommand IDLE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.gebutterfly.idle", AzPlayBehaviors.LOOP);
}
