package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.projectile.BubbleProjectile;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

import java.util.Set;

public class BubbleAttack extends AbstractMove<BubbleAttack, KQBTDEntity> {
    public static final MoveVariable<BubbleProjectile> BUBBLE_PROJECTILE = new MoveVariable<>(BubbleProjectile.class);

    public BubbleAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KQBTDEntity attacker, LivingEntity user, MoveContext ctx) {
        BubbleProjectile bubbleProjectile = new BubbleProjectile(attacker.getWorld(), user);
        bubbleProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        bubbleProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 0.5f, 0f);
        bubbleProjectile.setPosition(attacker.getPos().add(0, 1.25, 0));
        attacker.getWorld().spawnEntity(bubbleProjectile);
        ctx.set(BUBBLE_PROJECTILE, bubbleProjectile);

        JComponents.getBombTracker(user).getMainBomb().setBomb(bubbleProjectile);

        return Set.of();
    }

    public void tickBubble(KQBTDEntity stand) {
        BubbleProjectile bubbleProjectile = stand.getMoveContext().get(BUBBLE_PROJECTILE);
        if (bubbleProjectile != null && !bubbleProjectile.isInGround() && stand.hasUser()) {
            bubbleProjectile.setVelocity(stand.getUserOrThrow().getRotationVector().multiply(0.5));
            bubbleProjectile.velocityModified = true;
        }
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(BUBBLE_PROJECTILE);
    }

    @Override
    protected @NonNull BubbleAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BubbleAttack copy() {
        return copyExtras(new BubbleAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
