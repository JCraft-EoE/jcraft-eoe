package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

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

        GETreeEntity tree = new GETreeEntity(JEntityTypeRegistry.GE_TREE, attacker.getWorld(), user.getRotationVector().multiply(1.33));
        tree.setMaster(user);

        Direction gravity = GravityChangerAPI.getGravityDirection(attacker);
        Vec3d midPos = RotationUtil.vecPlayerToWorld(0.0, attacker.getHeight() * 0.25, 0.0, gravity);
        GravityChangerAPI.setDefaultGravityDirection(tree, gravity);
        Vec2f corrected = RotationUtil.rotWorldToPlayer(-attacker.getYaw(), -attacker.getPitch(), gravity);
        tree.refreshPositionAndAngles(attacker.getX() + midPos.x, attacker.getY() + midPos.y, attacker.getZ() + midPos.z, corrected.x, corrected.y);
        attacker.getWorld().spawnEntity(tree);

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
