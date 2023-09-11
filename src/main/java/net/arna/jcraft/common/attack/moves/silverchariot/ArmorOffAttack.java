package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class ArmorOffAttack extends AbstractSimpleAttack<ArmorOffAttack, SilverChariotEntity> {
    public static final IntMoveVariable ARMOR_TIME = new IntMoveVariable();

    public ArmorOffAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        attacker.setMode(SilverChariotEntity.Mode.ARMORLESS);
        ctx.set(ARMOR_TIME, 500);

        return targets;
    }

    public void tickArmor(SilverChariotEntity stand) {
        if (stand.getMode() != SilverChariotEntity.Mode.ARMORLESS) return;

        int armorTime = stand.getMoveContext().getInt(ARMOR_TIME);
        if (--armorTime > 0)
            stand.getMoveContext().setInt(ARMOR_TIME, armorTime);
        else stand.setMode(SilverChariotEntity.Mode.REGULAR);
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(ARMOR_TIME);
    }

    @Override
    protected @NonNull ArmorOffAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ArmorOffAttack copy() {
        return copyExtras(new ArmorOffAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
