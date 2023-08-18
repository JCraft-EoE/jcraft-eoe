package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import net.arna.jcraft.common.attack.core.base.AbstractMove;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack;
import net.arna.jcraft.common.entity.projectile.BubbleProjectile;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BubbleAttack extends AbstractMove<BubbleAttack, KQBTDEntity> {
    public static final MoveVariable<BubbleProjectile> BUBBLE_PROJECTILE = new MoveVariable<>(BubbleProjectile.class);

    public BubbleAttack(int cooldown, int windup, int moveStun, float moveDistance) {
        super(cooldown, windup, moveStun, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull Set<LivingEntity> perform(KQBTDEntity stand, LivingEntity user, MoveContext ctx) {
        BubbleProjectile bubbleProjectile = new BubbleProjectile(stand.world, user);
        bubbleProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        bubbleProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 0.5f, 0f);
        bubbleProjectile.setPosition(stand.getPos().add(0, 1.25, 0));
        stand.world.spawnEntity(bubbleProjectile);
        ctx.set(BUBBLE_PROJECTILE, bubbleProjectile);

        ctx.set(BombPlantAttack.BOMB_ENTITY, bubbleProjectile);
        ctx.set(BombPlantAttack.BOMB_POS, null);

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
    protected BubbleAttack getThis() {
        return this;
    }
}
