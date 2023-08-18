package net.arna.jcraft.common.attack.moves.killerqueen;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DetonateAttack extends AbstractMove<DetonateAttack, AbstractKillerQueenEntity<?, ?>> {
    public DetonateAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NotNull Set<LivingEntity> perform(AbstractKillerQueenEntity<?, ?> stand, LivingEntity user, MoveContext ctx) {
        Entity bombEntity = ctx.get(BombPlantAttack.BOMB_ENTITY);
        Vec3d bombPos = ctx.get(BombPlantAttack.BOMB_POS);

        if (bombEntity instanceof LivingEntity livingEntity) {
            explode(stand, user, livingEntity.getPos());
        } else {
            Vec3d finalBombPos = null;

            if (bombEntity != null) {
                finalBombPos = bombEntity.getPos();
                if (bombEntity instanceof ItemEntity)
                    bombEntity.kill();
            }
            if (bombPos != null) finalBombPos = bombPos;

            if (finalBombPos != null)
                explode(stand, user, finalBombPos);
        }

        ctx.set(BombPlantAttack.BOMB_ENTITY, null);
        ctx.set(BombPlantAttack.BOMB_POS, null);

        return Set.of();
    }

    @Override
    protected DetonateAttack getThis() {
        return this;
    }

    public static void explode(AbstractKillerQueenEntity<?, ?> stand, Entity user, Vec3d pos) {
        ServerWorld serverWorld = (ServerWorld) stand.world;

        JCraft.createParticle(serverWorld, pos.x, pos.y, pos.z, JParticleType.BOOM);
        JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE, serverWorld, pos, 96);

        DamageSource damageSource = JDamageSources.stand(stand);
        Set<? extends LivingEntity> toExplode = AbstractSimpleAttack.findHits(stand, pos, 4.4, damageSource);

        for (LivingEntity living : toExplode) {
            Vec3d kbVec = living.getEyePos().subtract(pos).normalize();
            StandEntity.damageLogic(stand.world, living, kbVec, 2, 3, true, 11f, false, 4, damageSource, user);
            living.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false));
        }
    }
}
