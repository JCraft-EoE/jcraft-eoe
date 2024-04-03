package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class CoinTossAttack extends AbstractMove<CoinTossAttack, KillerQueenEntity> {
    public static final MoveVariable<ItemEntity> COIN = new MoveVariable<>(ItemEntity.class);

    public CoinTossAttack(int cooldown) {
        super(cooldown, 0, 0, 1f);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KillerQueenEntity attacker, LivingEntity user, MoveContext ctx) {
        ItemEntity coin = ctx.get(COIN);
        Vec3d lookVec = user.getRotationVector().multiply(0.75);
        if (coin != null) coin.discard();
        coin = new ItemEntity(attacker.getWorld(), user.getX(), user.getY() + user.getHeight() * 2 / 3, user.getZ(),
                new ItemStack(JObjectRegistry.KQ_COIN, 1), lookVec.x, lookVec.y, lookVec.z);
        coin.setPickupDelayInfinite();

        attacker.getWorld().spawnEntity(coin);

        JComponents.getBombTracker(user).getMainBomb().setBomb(coin);

        attacker.playSound(JSoundRegistry.COIN_TOSS, 1, 1);

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(COIN);
    }

    @Override
    protected @NonNull CoinTossAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CoinTossAttack copy() {
        return copyExtras(new CoinTossAttack(getCooldown()));
    }
}
