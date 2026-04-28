package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class StandArrowEntity extends AbstractArrow {

    public StandArrowEntity(Level level) {
        super(JEntityTypeRegistry.STAND_ARROW_PROJECTILE.get(), level);
    }

    public StandArrowEntity(LivingEntity shooter, Level level) {
        super(JEntityTypeRegistry.STAND_ARROW_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return new ItemStack(JItemRegistry.STAND_ARROW.get());
    }

    @Override
    protected void onHitEntity(final @NonNull EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof final LivingEntity mob) {
            final Level level = mob.level();
            if (level.isClientSide()) {
                return;
            }
            final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(mob);
            if (standData.getType() == null && !mob.getType().is(JTagRegistry.CAN_NEVER_HAVE_STAND)) {
                standData.setType(StandTypeUtil.getRandomRegular(random));
                mob.unRide();
                JCraft.summon(mob.level(), mob);
            } else {
                // Bounce off the entity
                setDeltaMovement(getDeltaMovement().scale(-0.1));
                setYRot(getYRot() + 180.0f);
                yRotO += 180.0f;
            }
        }
    }
}
