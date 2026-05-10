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
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.item.KnifeBundleItem;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Set;

@Getter
public class ItemDropAttack extends AbstractMove<ItemDropAttack, AerosmithEntity> {
    public static final float BASE_DROP_RANGE = 1.5f;
    public static final float DROP_RANGE_INCREASE = 0.01f;

    private float range;
    private float dropRange;
    @Nullable @Setter
    private Vec3 dropLocation;

    public ItemDropAttack(final int cooldown,  final float range) {
        super(cooldown, 0, 0, 0);

        withRange(range);
    }

    public ItemDropAttack withRange(final float range) {
        this.range = range;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        if (user != null) {
            final ItemStack itemStack = user.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.isEmpty() && !itemStack.is(JTagRegistry.UNTHROWABLE)) {
                return Set.of();
            }
            attacker.setHeldItem(itemStack.copyWithCount(1));
            if (!(user instanceof Player player) || !player.isCreative()) {
                itemStack.setCount(itemStack.getCount() - 1);
            }
            final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
            final Vec3 rotVec = user.getLookAngle();
            final HitResult goal = JUtils.raycastAll(user, userEyePos, userEyePos.add(rotVec.scale(getRange())), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));

            dropLocation = goal.getLocation().add(0d, 6d, 0d);

            attacker.setFlyState(AerosmithEntity.FlyState.FLYBY);
            attacker.lookAt(EntityAnchorArgument.Anchor.FEET, dropLocation);
            attacker.setFlyTarget(dropLocation);

            dropRange = BASE_DROP_RANGE;

            if (!attacker.isRemote()) attacker.setRemote(true);
        }

        return Set.of();
    }

    @Override
    public boolean conditionsMet(AerosmithEntity attacker) {
        return super.conditionsMet(attacker) && JUtils.isHoldingSomethingThrowable(attacker.getUser());
    }

    @Override
    public void tick(final AerosmithEntity attacker) {
        if (dropLocation == null) {
            dropRange = BASE_DROP_RANGE;
            return;
        }

        if (attacker.distanceToSqr(dropLocation) <= dropRange * dropRange) {
            // TODO play the animation
            attacker.playSound(JSoundRegistry.AS_BOMB_DROP.get());
            dropItem(attacker);
            dropLocation = null;
            attacker.setFlyState(AerosmithEntity.FlyState.RETURN);
        }

        dropRange += DROP_RANGE_INCREASE;
    }

    private void dropItem(final AerosmithEntity attacker) {
        // knife bundle needs extra care
        if (attacker.getHeldItem().getItem() instanceof KnifeBundleItem) {
            for (int i = 0; i < 9; i++) {
                KnifeProjectile knife = new KnifeProjectile(attacker.level(), attacker);
                knife.setPos(attacker.position().subtract(0d, 2.5d, 0d));
                knife.setPos(knife.position().add(
                        attacker.level().random.triangle(0, 0.5),
                        attacker.level().random.triangle(0, 0.5),
                        attacker.level().random.triangle(0, 0.5)
                ));
                knife.shootFromRotation(attacker, attacker.getXRot(),attacker.getYRot(), 0f, -0.2f, 0f);
                attacker.level().addFreshEntity(knife);
            }
        }
        else {
            var projectile = JUtils.tossItem(attacker, attacker.level(), attacker.getHeldItem(), -0.5f, 0f, true);
            if (projectile != null) {
                projectile.setPos(projectile.position().subtract(0d, 2.5d, 0d));
            }
        }
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
        return copyExtras(
                new ItemDropAttack(getCooldown(), getRange())
        );
    }

    public void clearDropLocation() {
        dropLocation = null;
    }

    public static class Type extends AbstractMove.Type<ItemDropAttack> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<ItemDropAttack, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(ItemDropAttack::getRange);
        }

        protected Products.P3<RecordCodecBuilder.Mu<ItemDropAttack>, BaseMoveExtras, Integer, Float>
        bombDefault(RecordCodecBuilder.Instance<ItemDropAttack> instance) {
            return instance.group(extras(), cooldown(), range());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ItemDropAttack>, ItemDropAttack> buildCodec(final RecordCodecBuilder.Instance<ItemDropAttack> instance) {
            return bombDefault(instance).apply(instance, applyExtras(ItemDropAttack::new));
        }
    }
}
