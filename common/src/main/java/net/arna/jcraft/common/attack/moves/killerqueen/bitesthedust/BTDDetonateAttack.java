package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.attack.moves.killerqueen.KQDetonateAttack;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

public final class BTDDetonateAttack extends AbstractMove<BTDDetonateAttack, KQBTDEntity> {
    public BTDDetonateAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NotNull MoveType<BTDDetonateAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final KQBTDEntity attacker, final LivingEntity user) {
        final LivingEntity btdEntity = attacker.getBtdEntity().get();
        final Vec3 btdPos = attacker.getBtdPos();
        if (btdEntity == null) {
            return Set.of();
        }

        KQDetonateAttack.explode(attacker, user, btdEntity.position(), 20.0f, 4.4);
        btdEntity.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0, true, false));

        final Vec3 pos = btdEntity.position();
        final ServerLevel serverLevel = (ServerLevel) attacker.level();
        JCraft.createParticle(serverLevel, pos.x, pos.y + 7, pos.z, JParticleType.BITES_THE_DUST);
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

                JCraft.createParticle(serverLevel, lPos.x, lPos.y, lPos.z, JParticleType.BOOM);
            }
        }

        btdEntity.teleportToWithTicket(btdPos.x, btdPos.y, btdPos.z);
        attacker.setBtdEntity(new WeakReference<>(null));
        attacker.setBtdPos(null);

        return Set.of();
    }

    @Override
    protected @NonNull BTDDetonateAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BTDDetonateAttack copy() {
        return copyExtras(new BTDDetonateAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<BTDDetonateAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BTDDetonateAttack>, BTDDetonateAttack> buildCodec(RecordCodecBuilder.Instance<BTDDetonateAttack> instance) {
            return baseDefault(instance, BTDDetonateAttack::new);
        }
    }
}
