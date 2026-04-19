package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.projectile.AerobombProjectile;
import net.arna.jcraft.common.entity.projectile.ItemTossProjectile;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
public class ItemDropAttack extends AbstractMove<ItemDropAttack, AerosmithEntity> {

    private float range;
    @Setter
    private Vec3 dropLocation;
    private float speed;
    private boolean returning;

    public ItemDropAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float range) {
        super(cooldown, windup, duration, moveDistance);

        withRange(range);
    }

    public ItemDropAttack withRange(final float range) {
        this.range = range;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        if (user == null) {
            return Set.of();
        }
        final ItemStack itemStack = user.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            return Set.of();
        }
        attacker.setHoldItem(itemStack.copyWithCount(1));
        if (!(user instanceof Player player) || !player.isCreative()) {
            itemStack.setCount(itemStack.getCount() - 1);
        }
        final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult goal = JUtils.raycastAll(user, userEyePos, userEyePos.add(rotVec.scale(getRange())), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));
        setDropLocation(goal.getLocation().add(0d, 10d, 0d));
        speed = (float)dropLocation.subtract(attacker.position()).scale(2d / getDuration()).length();
        attacker.setRemote(true);
        attacker.setDeltaMovement(dropLocation.subtract(attacker.position()).normalize().scale(speed));
        return Set.of();
    }

    @Override
    public void tick(final AerosmithEntity attacker) {
        if (dropLocation != null) {
            if (attacker.position().distanceTo(dropLocation) <= 1) {
                // TODO play the animation
                dropItem(attacker);
                returning = true;
                dropLocation = null;
            }
            else {
                attacker.setDeltaMovement(dropLocation.subtract(attacker.position()).normalize().scale(speed));
            }
        }
        if (returning && attacker.hasUser()) {
            final LivingEntity user = attacker.getUserOrThrow();
            if (attacker.position().distanceTo(user.getEyePosition()) <= 1) {
                attacker.setDeltaMovement(Vec3.ZERO);
                attacker.setRemote(false);
                returning = false;
            }
            else {
                final Vec3 diff = user.getEyePosition().subtract(attacker.position());
                attacker.setDeltaMovement(diff.normalize().scale(speed));
                attacker.setYRot(Mth.wrapDegrees(-(float)Math.atan2(diff.x(), diff.z()) * 57.2957763671875f));
            }
        }
    }

    private void dropItem(AerosmithEntity attacker) {
        if (!attacker.hasUser()) {
            return;
        }
        ItemTossProjectile itemProjectile = new ItemTossProjectile(attacker.getUserOrThrow(), attacker.level(), attacker.getHoldItem());
        attacker.setHoldItem(null);
        itemProjectile.setOwner(attacker.hasUser() ? attacker.getUserOrThrow() : attacker);
        itemProjectile.setPos(attacker.position().subtract(0d, 1d, 0d));
        itemProjectile.setXRot(attacker.getXRot());
        itemProjectile.xRotO = attacker.xRotO;
        itemProjectile.setYRot(attacker.getYRot());
        itemProjectile.yRotO = attacker.yRotO;
        itemProjectile.setDeltaMovement(attacker.getDeltaMovement().normalize().scale(1d/16));
        attacker.level().addFreshEntity(itemProjectile);
    }

    @Override
    public @NonNull MoveType<ItemDropAttack> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull ItemDropAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ItemDropAttack copy() {
        return copyExtras(new ItemDropAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getRange()));
    }

    public static class Type extends AbstractMove.Type<ItemDropAttack> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<ItemDropAttack, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(ItemDropAttack::getRange);
        }

        protected Products.P6<RecordCodecBuilder.Mu<ItemDropAttack>, BaseMoveExtras, Integer, Integer, Integer, Float, Float>
        bombDefault(RecordCodecBuilder.Instance<ItemDropAttack> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), range());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ItemDropAttack>, ItemDropAttack> buildCodec(final RecordCodecBuilder.Instance<ItemDropAttack> instance) {
            return bombDefault(instance).apply(instance, applyExtras(ItemDropAttack::new));
        }
    }
}
