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
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
public class PatrolMove extends AbstractMove<PatrolMove, AerosmithEntity> {

    private float range;
    private float radius;

    public PatrolMove(final int cooldown, final float range, final float radius) {
        super(cooldown, 0, 0, 0);

        withRange(range);
        withRadius(radius);
    }

    public PatrolMove withRange(final float range) {
        this.range = range;
        return getThis();
    }

    public PatrolMove withRadius(final float radius) {
        this.radius = radius;
        return getThis();
    }

    @Override
    public void onInitiate(final AerosmithEntity attacker) {
        final LivingEntity user = attacker.getUser();

        if (user != null) {
            final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
            final Vec3 rotVec = user.getLookAngle();
            final HitResult goal = JUtils.raycastAll(
                    user,
                    userEyePos,
                    userEyePos.add(rotVec.scale(getRange())),
                    ClipContext.Fluid.NONE,
                    EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS)
            );

            attacker.setPatrolRadius(radius);
            attacker.setFlyState(AerosmithEntity.FlyState.PATROL);
            attacker.setFlyTarget(goal.getLocation());
            if (!attacker.isRemote()) attacker.setRemote(true);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public @NonNull MoveType<PatrolMove> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull PatrolMove getThis() {
        return this;
    }

    @Override
    public @NonNull PatrolMove copy() {
        return copyExtras(new PatrolMove(getCooldown(), getRange(), getRadius()));
    }

    public static class Type extends AbstractMove.Type<PatrolMove> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<PatrolMove, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(PatrolMove::getRange);
        }
        protected RecordCodecBuilder<PatrolMove, Float> radius() {
            return Codec.FLOAT.fieldOf("radius").forGetter(PatrolMove::getRadius);
        }

        protected Products.P4<RecordCodecBuilder.Mu<PatrolMove>, BaseMoveExtras, Integer, Float, Float>
        bombDefault(RecordCodecBuilder.Instance<PatrolMove> instance) {
            return instance.group(extras(), cooldown(), range(), radius());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PatrolMove>, PatrolMove> buildCodec(final RecordCodecBuilder.Instance<PatrolMove> instance) {
            return bombDefault(instance).apply(instance, applyExtras(PatrolMove::new));
        }
    }
}
