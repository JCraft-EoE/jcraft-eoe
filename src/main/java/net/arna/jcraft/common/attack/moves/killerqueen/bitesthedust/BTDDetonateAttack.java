package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.List;
import java.util.Set;

public class BTDDetonateAttack extends AbstractMove<BTDDetonateAttack, AbstractKillerQueenEntity<?, ?>> {
    public BTDDetonateAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AbstractKillerQueenEntity<?, ?> attacker, LivingEntity user, MoveContext ctx) {
        LivingEntity btdEntity = ctx.get(BTDPlantAttack.BTD_ENTITY);
        Vec3d btdPos = ctx.get(BTDPlantAttack.BTD_POS);
        if (btdEntity == null) return Set.of();

        attacker.getWorld().createExplosion(user, btdEntity.getX(), btdEntity.getY() + btdEntity.getHeight() / 2, btdEntity.getZ(), 2f, World.ExplosionSourceType.NONE);
        btdEntity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false));

        Vec3d pos = btdEntity.getPos();
        JCraft.createParticle((ServerWorld) attacker.getWorld(), pos.x, pos.y + 5, pos.z, JParticleType.BITES_THE_DUST);
        Vec3d v1 = pos.add(3, 3, 3);
        Vec3d v2 = pos.add(-3, -3, -3);
        List<LivingEntity> list = attacker.getWorld().getEntitiesByClass(LivingEntity.class, new Box(v1, v2),
                EntityPredicates.VALID_LIVING_ENTITY.and(e -> e != user.getVehicle() && e != user && e != attacker && e != btdEntity));

        for (LivingEntity l : list)
            if (l.squaredDistanceTo(pos) < 9) {
                if (l.squaredDistanceTo(pos) < 2.25)
                    attacker.getWorld().createExplosion(user, l.getX(), l.getY() + l.getHeight() / 2, l.getZ(), 1.5f, World.ExplosionSourceType.NONE);
                else attacker.getWorld().createExplosion(user, l.getX(), l.getY() + l.getHeight() / 2, l.getZ(), 1f, World.ExplosionSourceType.NONE);
            }

        btdEntity.teleport(btdPos.x, btdPos.y, btdPos.z);
        ctx.set(BTDPlantAttack.BTD_ENTITY, null);
        ctx.set(BTDPlantAttack.BTD_POS, null);

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
}
