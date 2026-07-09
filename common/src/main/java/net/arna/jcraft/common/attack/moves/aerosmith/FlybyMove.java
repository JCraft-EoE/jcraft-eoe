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
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
public class FlybyMove extends AbstractMove<FlybyMove, AerosmithEntity> {

    private float range;

    public FlybyMove(final int cooldown, final float range) {
        super(cooldown, 0, 0, 0);

        withRange(range);
    }

    public FlybyMove withRange(final float range) {
        this.range = range;
        return getThis();
    }

    @Override
    public void onInitiate(final AerosmithEntity attacker) {
        final LivingEntity user = attacker.getUser();

        if (user != null) {
            final Vec3 start = user.position().add(GravityChangerAPI.getEyeOffset(user));
            final HitResult goal = JUtils.raycastAll(user, start, start.add(user.getLookAngle().scale(range)), ClipContext.Fluid.NONE);
            final Vec3 target = goal.getLocation();

            attacker.setFlyState(AerosmithEntity.FlyState.FLYBY);
            attacker.setFlyTarget(target);

            if (!attacker.isRemote()) attacker.setRemote(true);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public @NonNull MoveType<FlybyMove> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull FlybyMove getThis() {
        return this;
    }

    @Override
    public @NonNull FlybyMove copy() {
        return copyExtras(new FlybyMove(getCooldown(), getRange()));
    }

    public static class Type extends AbstractMove.Type<FlybyMove> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<FlybyMove, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(FlybyMove::getRange);
        }

        protected Products.P3<RecordCodecBuilder.Mu<FlybyMove>, BaseMoveExtras, Integer, Float>
        bombDefault(RecordCodecBuilder.Instance<FlybyMove> instance) {
            return instance.group(extras(), cooldown(), range());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlybyMove>, FlybyMove> buildCodec(final RecordCodecBuilder.Instance<FlybyMove> instance) {
            return bombDefault(instance).apply(instance, applyExtras(FlybyMove::new));
        }
    }
}