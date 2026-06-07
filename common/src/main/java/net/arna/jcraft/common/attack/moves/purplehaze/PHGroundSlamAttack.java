package net.arna.jcraft.common.attack.moves.purplehaze;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.PurpleHazeCloudEntity;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class PHGroundSlamAttack extends AbstractSimpleAttack<PHGroundSlamAttack, AbstractPurpleHazeEntity<?, ?>> {
    public PHGroundSlamAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                              float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<PHGroundSlamAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AbstractPurpleHazeEntity<?, ?> attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        final PurpleHazeCloudEntity cloud = new PurpleHazeCloudEntity(attacker.level(), 3.0f, attacker.getPoisonType());
        cloud.copyPosition(attacker);
        cloud.setOwner(user);
        attacker.level().addFreshEntity(cloud);

        return targets;
    }

    @Override
    protected @NonNull PHGroundSlamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PHGroundSlamAttack copy() {
        return copyExtras(new PHGroundSlamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<PHGroundSlamAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PHGroundSlamAttack>, PHGroundSlamAttack> buildCodec(RecordCodecBuilder.Instance<PHGroundSlamAttack> instance) {
            return attackDefault(instance, PHGroundSlamAttack::new);
        }
    }
}
