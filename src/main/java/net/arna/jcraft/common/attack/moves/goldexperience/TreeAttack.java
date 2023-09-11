package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class TreeAttack extends AbstractSimpleAttack<TreeAttack, GoldExperienceEntity> {
    public TreeAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                      float knockback, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GoldExperienceEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        GETreeEntity tree = new GETreeEntity(JEntityTypeRegistry.GE_TREE, attacker.world);
        tree.setMaster(user);
        tree.copyPositionAndRotation(attacker);
        attacker.world.spawnEntity(tree);

        return targets;
    }

    @Override
    protected @NonNull TreeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull TreeAttack copy() {
        return copyExtras(new TreeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
