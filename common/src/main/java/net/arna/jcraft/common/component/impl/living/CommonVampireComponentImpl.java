package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.food.IFoodData;
import net.arna.jcraft.common.spec.VampireSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;

public abstract class CommonVampireComponentImpl implements CommonVampireComponent {
    private final LivingEntity entity;
    private FoodData foodData = null;
    private boolean isVampire = false;
    @Getter
    private float blood = 20;
    private byte healCount = 0;
    private int regenTick = 0, starveTick = 0;

    public static final int MIN_REGEN_BLOOD = 16; // 75%

    public CommonVampireComponentImpl(final LivingEntity entity) {
        this.entity = entity;

        if (entity instanceof IFoodData iFoodData) {
            foodData = iFoodData.getFoodData();
        }
    }

    public void tick() {
        final Level world = entity.level();
        if (world.isClientSide) {
            return;
        }

        JSpec<?, ?> spec = JUtils.getSpec(entity);
        // FOR NOW, these are intrinsically tied.
        setVampire(spec != null && spec.getType() == JSpecTypeRegistry.VAMPIRE.get());

        if (!isVampire) {
            return;
        }

        // Taking damage when unprotected from sunlight.
        if (world.isDay() &&
                !world.isRaining() &&
                !world.isThundering() &&
                !(entity.getItemBySlot(EquipmentSlot.HEAD).is(JTagRegistry.PROTECTS_FROM_SUN)) &&
                world.canSeeSky(entity.blockPosition())
        ) {
            entity.setSecondsOnFire(1);
            entity.hurt(world.damageSources().dryOut(), 2.0F);
        }

        // Vampires do not have to breathe.
        entity.setAirSupply(entity.getMaxAirSupply());

        if (foodData == null && entity instanceof IFoodData iFoodData) {
            foodData = iFoodData.getFoodData();
        }

        // Replace food logic with blood logic. (Optional)
        if (foodData != null) {
            if (blood < 1 && --starveTick < 1) {
                // Starve
                entity.hurt(world.damageSources().starve(), 1.0F);
                starveTick = 80;
            } else {
                // Regenerate
                float health = entity.getHealth();
                if (health < entity.getMaxHealth() && blood >= MIN_REGEN_BLOOD && --regenTick < 1) {
                    entity.heal(1);

                    // Every third heal takes away a blood unit
                    if (++healCount > 2) {
                        blood--;
                        healCount = 0;
                    }

                    regenTick = 10;
                }
            }

            // Vampires get tired slower
            if (foodData.getExhaustionLevel() > 32.0F) {
                foodData.addExhaustion(-32.0F);
                setBlood(blood - 1);
            }

            // Prevent the player's default hunger from doing anything
            foodData.setFoodLevel(20);
            foodData.setSaturation(0f);
        }
    }

    @Override
    public void setBlood(final float blood) {
        this.blood = Mth.clamp(blood, 0, 20);
        sync(entity);
    }

    @Override
    public boolean isVampire() {
        return isVampire || JUtils.getSpec(entity) instanceof VampireSpec;
    }

    @Override
    public void setVampire(boolean b) {
        if (b == this.isVampire) return;
        this.isVampire = b;
        sync(entity);
    }

    public void sync(final Entity entity) {
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player == entity;
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        buf.writeFloat(blood);
        buf.writeBoolean(isVampire);
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        blood = buf.readFloat();
        isVampire = buf.readBoolean();
    }

    public void readFromNbt(final CompoundTag tag) {
        blood = tag.getFloat("Blood");
        isVampire = tag.getBoolean("Vampire");
    }

    public void writeToNbt(final CompoundTag tag) {
        tag.putFloat("Blood", blood);
        tag.putBoolean("Vampire", isVampire);
    }
}
