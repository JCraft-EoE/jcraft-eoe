package net.arna.jcraft.common.attack.moves.hamon;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Collection;
import java.util.Set;

public final class ImproviserAttack extends AbstractSimpleAttack<ImproviserAttack, HamonSpec> {
    public static final float CHARGE_COST = 10.0F;
    public ImproviserAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<ImproviserAttack> getMoveType() {
        return ImproviserAttack.Type.INSTANCE;
    }

    @Override
    public void onInitiate(HamonSpec attacker) {
        super.onInitiate(attacker);

        attacker.drainCharge(CHARGE_COST);
        attacker.setUseHamonNext(false);
    }

    @Override
    public void tick(HamonSpec attacker) {
        super.tick(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(HamonSpec attacker, LivingEntity user) {
        final ItemStack mainStack = user.getMainHandItem();
        final Multimap<Attribute, AttributeModifier> modifiers = mainStack.getAttributeModifiers(EquipmentSlot.MAINHAND);

        final float damage = (float) applyAttributeModifiers(1.0, modifiers.get(Attributes.ATTACK_DAMAGE));
        withDamage(damage);

        final float knockback = (float) applyAttributeModifiers(1.25, modifiers.get(Attributes.ATTACK_KNOCKBACK));
        withKnockback(knockback);

        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (JUtils.getSpec(user) instanceof HamonSpec hamonSpec) {
            for (LivingEntity target : targets) {
                hamonSpec.processTarget(target);
                EnchantmentHelper.doPostDamageEffects(user, target);

                final int fireLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, mainStack);
                if (fireLevel > 0) target.setSecondsOnFire(fireLevel * 4);
            }
        }

        return targets;
    }

    public static double applyAttributeModifiers(double baseValue, final Collection<AttributeModifier> modifiers) {
        double add = 0;
        double multBase = 0;
        double multTotal = 1;

        for (AttributeModifier mod : modifiers) {
            final double amt = mod.getAmount();
            switch (mod.getOperation()) {
                case ADDITION -> add += amt;
                case MULTIPLY_BASE -> multBase += amt;
                case MULTIPLY_TOTAL -> multTotal *= 1 + amt;
            }
        }

        return (baseValue + add) * (1 + multBase) * multTotal;
    }

    @Override
    protected @NonNull ImproviserAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ImproviserAttack copy() {
        return copyExtras(new ImproviserAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<ImproviserAttack> {
        public static final ImproviserAttack.Type INSTANCE = new ImproviserAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ImproviserAttack>, ImproviserAttack> buildCodec(RecordCodecBuilder.Instance<ImproviserAttack> instance) {
            return attackDefault(instance, ImproviserAttack::new);
        }
    }
}
