package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.attack.moves.killerqueen.KQDetonateAttack;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.marker.EntityMarker;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class BTDDetonateAttack extends AbstractMove<BTDDetonateAttack, KQBTDEntity> {

    @Getter
    private final int reach;

    public BTDDetonateAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final int reach) {
        super(cooldown, windup, duration, moveDistance);
        if (reach < 0) {
            throw new IllegalArgumentException("BTD teleport reach cannot be negative!");
        }
        this.reach = reach;
    }

    @Override
    public @NotNull MoveType<BTDDetonateAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final KQBTDEntity attacker, final LivingEntity user) {
        final BTDPlantAttack btdPlantAttack = attacker.getMove(BTDPlantAttack.class);
        final ServerLevel level = (ServerLevel) attacker.level();
        if (btdPlantAttack == null || btdPlantAttack.getEntityMarker() == null) {
            return Set.of();
        }
        final EntityMarker marker = btdPlantAttack.getEntityMarker();
        final Entity entity = level.getEntity(marker.id());
        if (!(entity instanceof LivingEntity btdEntity)) {
            return Set.of();
        }

        KQDetonateAttack.explode(attacker, user, btdEntity.position(), 20.0f, 4.4);

        final Vec3 pos = btdEntity.position();
        JCraft.createParticle(level, pos.x, pos.y + 7, pos.z, JParticleType.BITES_THE_DUST);
        final Vec3 v1 = pos.add(3, 3, 3);
        final Vec3 v2 = pos.add(-3, -3, -3);
        final List<LivingEntity> list = attacker.level().getEntitiesOfClass(LivingEntity.class, new AABB(v1, v2),
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(e -> e != user.getVehicle() && e != user && e != attacker && e != btdEntity));

        for (LivingEntity l : list) {
            final double sqrDist = l.distanceToSqr(pos);

            if (sqrDist < 9.0) {
                final Vec3 lPos = l.position();

                if (sqrDist < 2.25) {
                    KQDetonateAttack.explode(attacker, user, lPos, 5f, 3.0);
                } else {
                    KQDetonateAttack.explode(attacker, user, lPos, 2f, 2.0);
                }

                JCraft.createParticle(level, lPos.x, lPos.y, lPos.z, JParticleType.BOOM);
            }
        }

        if (btdEntity.isAlive()) {
            if (btdPlantAttack.getEntityMarkerType().shouldLoad(marker, level) && JUtils.nullSafeDistanceSqr(btdEntity, attacker.getUser()) <= reach * reach) {
                btdPlantAttack.getEntityMarkerType().load(marker, level); // takes care of the teleport
            }
            btdEntity.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0, true, false));
        }
        btdPlantAttack.reset();
        return Set.of();
    }

    @Override
    protected @NonNull BTDDetonateAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BTDDetonateAttack copy() {
        return copyExtras(new BTDDetonateAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getReach()));
    }

    public static class Type extends AbstractMove.Type<BTDDetonateAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BTDDetonateAttack>, BTDDetonateAttack> buildCodec(RecordCodecBuilder.Instance<BTDDetonateAttack> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("reach").forGetter(BTDDetonateAttack::getReach)).apply(instance, BTDDetonateAttack::new);
        }
    }
}
