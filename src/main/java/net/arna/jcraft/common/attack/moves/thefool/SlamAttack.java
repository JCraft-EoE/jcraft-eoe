package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class SlamAttack extends AbstractSimpleAttack<SlamAttack, TheFoolEntity> {
    public static final IntMoveVariable VARIANT = new IntMoveVariable();

    public SlamAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                      float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        attacker.playSound(JSoundRegistry.IMPACT_11, 1, 1);

        switch (ctx.getInt(VARIANT)) {
            case 2 -> {
                Vec3d leftVec = user.getRotationVector().rotateY(1.75f);
                for (int i = 0; i < 8; i++) {
                    leftVec = leftVec.rotateY(-3.141592f / 8).normalize();
                    TheFoolEntity.createFoolishSand(attacker.getWorld(), attacker.getBlockPos(),
                            new Vec3d(leftVec.x / 4, 0.25, leftVec.z / 4));
                }
            }
            case 3 -> {
                Vec3d rotVec = user.getRotationVector();
                for (double i = 0; i < 7; i++)
                    for (double y = 0; y < i; y++) {
                        double hDiv = 4.5 * (1 + y / i);
                        TheFoolEntity.createFoolishSand(attacker.getWorld(), attacker.getBlockPos(),
                                new Vec3d(rotVec.x * Math.sqrt(i) / hDiv, y / 4.3, rotVec.z * Math.sqrt(i) / hDiv));
                    }
            }
        }

        return targets;
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(VARIANT);
    }

    @Override
    protected @NonNull SlamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SlamAttack copy() {
        return copyExtras(new SlamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
