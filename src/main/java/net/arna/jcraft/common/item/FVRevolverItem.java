package net.arna.jcraft.common.item;

import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

public class FVRevolverItem extends Item {
    public FVRevolverItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (user.hasStatusEffect(JStatusRegister.DAZED))
                return TypedActionResult.fail(itemStack);

            NbtCompound data = itemStack.getOrCreateNbt();
            int shots = data.getInt("Shots");
            if (shots < 1)
                return TypedActionResult.fail(user.getStackInHand(hand));
            data.putInt("Shots", shots - 1);

            user.getItemCooldownManager().set(this, 8);

            world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegister.REVOLVER_FIRE, SoundCategory.PLAYERS, 1f, 1f);

            Vec3d dir = user.getRotationVec(1.0F);
            double range = 1024.0;
            Box box = user
                    .getBoundingBox()
                    .stretch(dir.multiply(range))
                    .expand(1.0D);
            EntityHitResult hitResult = ProjectileUtil.raycast(
                    user,
                    user.getEyePos(),
                    user.getEyePos().add(user.getRotationVector().multiply(range)),
                    box,
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                    range
            );

            if (hitResult != null) {
                Entity entity = hitResult.getEntity();
                if (entity instanceof LivingEntity livingEntity)
                    damageLogic(world, livingEntity, dir, 10, 1, false, 5, true, 4, DamageSource.mob(user), user);
            }
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        stack.setDamage(stack.getDamage() + 1);
        if ((stack.getMaxDamage() - stack.getDamage()) <= 0) {
            stack.decrement(1);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack s = new ItemStack(this);
        NbtCompound nbt = s.getOrCreateNbt();
        nbt.putInt("Shots", 6);
        return s;
    }
}
