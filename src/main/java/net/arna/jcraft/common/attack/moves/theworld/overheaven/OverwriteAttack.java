package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class OverwriteAttack extends AbstractSimpleAttack<OverwriteAttack, TheWorldOverHeavenEntity> {
    public static final MoveVariable<IntList> OVERWRITE_TIMES = new MoveVariable<>(IntList.class);
    public static final MoveVariable<List<LivingEntity>> OVERWRITE_TARGETS = new MoveVariable<>(new TypeToken<>() {});

    public OverwriteAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected void processTarget(TheWorldOverHeavenEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        MoveContext ctx = attacker.getMoveContext();
        IntList overwriteTimes = ctx.get(OVERWRITE_TIMES);
        List<LivingEntity> overwriteTargets = ctx.get(OVERWRITE_TARGETS);

        switch (attacker.getOverwriteType()) {
            case 1 -> {
                overwriteTimes.add(200);
                overwriteTargets.add(target);
            }
            case 2 -> {
                target.setOnFireFor(5);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 0, false, true));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0, false, true));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 0, false, true));
            }
            case 3 -> {
                target.heal(4f);

                if (!(target instanceof MobEntity)) return;
                JComponents.getMiscData(target).setSlavedTo(attacker.getUserOrThrow().getUuid());
                overwriteTimes.add(1048576);
                overwriteTargets.add(target);
            }
        }
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(OVERWRITE_TIMES, new IntArrayList());
        ctx.register(OVERWRITE_TARGETS, new ArrayList<>());
    }

    @Override
    protected @NonNull OverwriteAttack getThis() {
        return this;
    }

    @Override
    public @NonNull OverwriteAttack copy() {
        return copyExtras(new OverwriteAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
