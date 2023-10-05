package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingAttack;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;
import java.util.Set;

@Getter
public class SmiteAttack extends AbstractEffectInflictingAttack<SmiteAttack, TheWorldOverHeavenEntity> {
    public static final MoveVariable<Vec3d> LIGHTNING_POS = new MoveVariable<>(Vec3d.class);
    private static final MoveVariable<LightningEntity> BOLT = new MoveVariable<>(LightningEntity.class);
    private final boolean aerial;

    public SmiteAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                       float hitboxSize, float knockback, float offset, boolean aerial) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset,
                List.of(new StatusEffectInstance(StatusEffects.LEVITATION, 7, 9, true, false)));
        this.aerial = ranged = aerial;
        this.withHitSpark(null);
    }

    @Override
    public void onInitiate(TheWorldOverHeavenEntity attacker) {
        super.onInitiate(attacker);

        LivingEntity user = attacker.getUserOrThrow();
        MoveContext ctx = attacker.getMoveContext();
        if (!aerial) ctx.set(LIGHTNING_POS, user.getPos());
        else {
            Vec3d eP = user.getEyePos();
            Vec3d rangeMod = user.getRotationVector().multiply(24);
            EntityHitResult eHit = ProjectileUtil.raycast(user, eP, eP.add(rangeMod),
                    user.getBoundingBox().expand(24),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                    576 // Squared
            );

            if (eHit != null) ctx.set(LIGHTNING_POS, eHit.getPos());
            else ctx.set(LIGHTNING_POS, attacker.getWorld().raycast(new RaycastContext(eP, eP.add(rangeMod),
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user)).getPos());
        }

        Vec3d lightningPos = ctx.get(LIGHTNING_POS);
        AreaEffectCloudEntity effectCloud = new AreaEffectCloudEntity(attacker.getWorld(),
                lightningPos.x, lightningPos.y, lightningPos.z);
        effectCloud.setOwner(user);
        effectCloud.setRadius(getDamage() / 2f);
        effectCloud.setWaitTime(10);
        effectCloud.setRadiusGrowth(-0.5f);

        attacker.getWorld().spawnEntity(effectCloud);

        attacker.getWorld().playSound(null, lightningPos.x, lightningPos.y, lightningPos.z,
                JSoundRegistry.TWOH_CHARGE, SoundCategory.PLAYERS, 1, 1);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheWorldOverHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        Vec3d lP = ctx.get(LIGHTNING_POS);

        LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, attacker.getWorld());
        bolt.setCosmetic(true);
        bolt.setPosition(lP);
        ctx.set(BOLT, bolt);

        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        attacker.getWorld().spawnEntity(bolt);
        return targets;
    }

    @Override
    protected Set<Box> calculateBoxes(TheWorldOverHeavenEntity attacker, LivingEntity user, Vec3d rotVec, Vec3d upVec, Vec3d hPos, Vec3d fPos) {
        return Set.of(createBox(attacker.getMoveContext().get(LIGHTNING_POS), getHitboxSize()));
    }

    @Override
    protected void processTarget(TheWorldOverHeavenEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        target.onStruckByLightning((ServerWorld) attacker.getWorld(), attacker.getMoveContext().get(BOLT));
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(LIGHTNING_POS);
        ctx.register(BOLT);
    }

    @Override
    protected @NonNull SmiteAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SmiteAttack copy() {
        return copyExtras(new SmiteAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), isAerial()));
    }
}
