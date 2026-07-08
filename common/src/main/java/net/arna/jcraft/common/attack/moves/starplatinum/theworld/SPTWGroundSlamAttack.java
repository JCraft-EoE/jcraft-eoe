package net.arna.jcraft.common.attack.moves.starplatinum.theworld;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SPTWEntity;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Set;

public final class SPTWGroundSlamAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<SPTWGroundSlamAttack<A>, A> {
    public SPTWGroundSlamAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                                final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<SPTWGroundSlamAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        Vec3 pos = user.position();
        for (LivingEntity target : targets) {
            Vec3 launchVec = target.position().subtract(pos).normalize().scale(1.3);
            target.push(launchVec.x, launchVec.y + 0.4, launchVec.z);

            target.hurtMarked = true;
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }

        return targets;
    }

    @Override
    protected @NonNull SPTWGroundSlamAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SPTWGroundSlamAttack<A> copy() {
        return copyExtras(new SPTWGroundSlamAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<SPTWGroundSlamAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SPTWGroundSlamAttack<?>>, SPTWGroundSlamAttack<?>> buildCodec(RecordCodecBuilder.Instance<SPTWGroundSlamAttack<?>> instance) {
            return attackDefault(instance, SPTWGroundSlamAttack::new);
        }
    }
}
