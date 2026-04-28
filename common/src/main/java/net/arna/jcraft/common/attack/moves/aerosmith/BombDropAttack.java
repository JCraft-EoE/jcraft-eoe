package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.projectile.AerobombProjectile;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Set;

@Getter
public class BombDropAttack extends AbstractMove<BombDropAttack, AerosmithEntity> {

    private float range;
    @Nullable
    private Vec3 dropLocation;

    public BombDropAttack(final int cooldown, final float range) {
        super(cooldown, 0, 0, 0);
        withRange(range);
    }

    public BombDropAttack withRange(final float range) {
        this.range = range;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        if (user != null) {
            final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
            final Vec3 rotVec = user.getLookAngle();
            final HitResult goal = JUtils.raycastAll(user, userEyePos, userEyePos.add(rotVec.scale(getRange())), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));

            dropLocation = goal.getLocation().add(0d, 6d, 0d);

            attacker.setFlyState(AerosmithEntity.FlyState.FLYBY);
            attacker.lookAt(EntityAnchorArgument.Anchor.FEET, dropLocation);
            attacker.setFlyTarget(dropLocation);

            if (!attacker.isRemote()) attacker.setRemote(true);
        }

        return Set.of();
    }

    @Override
    public void tick(final AerosmithEntity attacker) {
        if (dropLocation != null) {
            if (attacker.position().distanceTo(dropLocation) <= 2.25) {
                // TODO play the animation
                dropBomb(attacker);
                dropLocation = null;
                attacker.setFlyState(AerosmithEntity.FlyState.RETURN);
            }
        }
    }

    private void dropBomb(AerosmithEntity attacker) {
        AerobombProjectile bomb = new AerobombProjectile(attacker.level());
        bomb.setOwner(attacker.hasUser() ? attacker.getUserOrThrow() : attacker);
        bomb.setPos(attacker.position().subtract(0d, 1d, 0d));

        bomb.setXRot(attacker.getXRot());
        bomb.xRotO = attacker.xRotO;

        bomb.setYRot(attacker.getYRot());
        bomb.yRotO = attacker.yRotO;

        bomb.setDeltaMovement(attacker.getDeltaMovement().scale(1.0 / 16.0));
        attacker.level().addFreshEntity(bomb);
    }

    @Override
    public @NonNull MoveType<BombDropAttack> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull BombDropAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BombDropAttack copy() {
        return copyExtras(new BombDropAttack(getCooldown(), getRange()));
    }

    public void clearDropLocation() {
        dropLocation = null;
    }

    public static class Type extends AbstractMove.Type<BombDropAttack> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<BombDropAttack, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(BombDropAttack::getRange);
        }

        protected Products.P3<RecordCodecBuilder.Mu<BombDropAttack>, BaseMoveExtras, Integer, Float>
        bombDefault(RecordCodecBuilder.Instance<BombDropAttack> instance) {
            return instance.group(extras(), cooldown(), range());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BombDropAttack>, BombDropAttack> buildCodec(final RecordCodecBuilder.Instance<BombDropAttack> instance) {
            return bombDefault(instance).apply(instance, applyExtras(BombDropAttack::new));
        }
    }
}
