package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;

public final class CoinTossMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<CoinTossMove<A>, A> {
    private WeakReference<ItemEntity> coin = new WeakReference<>(null);

    public CoinTossMove(final int cooldown) {
        super(cooldown, 0, 0, 1f);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<CoinTossMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        ItemEntity coin = this.coin.get();
        final Vec3 lookVec = user.getLookAngle().scale(0.75);
        if (coin != null) {
            coin.discard();
        }
        coin = new ItemEntity(attacker.getBaseEntity().level(), user.getX(), user.getY() + user.getBbHeight() * 2 / 3, user.getZ(),
                new ItemStack(JItemRegistry.KQ_COIN.get(), 1), lookVec.x, lookVec.y, lookVec.z);
        coin.setNeverPickUp();

        attacker.getBaseEntity().level().addFreshEntity(coin);

        JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(coin);

        attacker.getBaseEntity().playSound(JSoundRegistry.COIN_TOSS.get(), 1, 1);

        this.coin = new WeakReference<>(coin);

        return Set.of();
    }

    @Override
    protected @NonNull CoinTossMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CoinTossMove<A> copy() {
        return copyExtras(new CoinTossMove<>(getCooldown()));
    }

    public static class Type extends AbstractMove.Type<CoinTossMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<CoinTossMove<?>>, CoinTossMove<?>> buildCodec(RecordCodecBuilder.Instance<CoinTossMove<?>> instance) {
            return instance.group(extras(), cooldown()).apply(instance, applyExtras(CoinTossMove::new));
        }
    }
}
