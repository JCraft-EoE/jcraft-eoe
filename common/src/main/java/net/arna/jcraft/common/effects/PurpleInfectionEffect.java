package net.arna.jcraft.common.effects;

import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

public class PurpleInfectionEffect extends MobEffect {
    private static final WeakHashMap<LivingEntity, LivingEntity> INFECTORS = new WeakHashMap<>();

    public static void trackInfector(LivingEntity victim, @Nullable LivingEntity infector) {
        if (infector != null) {
            INFECTORS.put(victim, infector);
        }
    }

    public PurpleInfectionEffect() {
        super(MobEffectCategory.HARMFUL, 0xA34AB5);
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        int i = 0b101000 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }

    @Override
    public void applyEffectTick(final LivingEntity entity, final int amplifier) {
        StandType standType = JComponentPlatformUtils.getStandComponent(entity).getType();
        float damage = 0.6666f; // 1/3rd of a heart
        if (standType == JStandTypeRegistry.PURPLE_HAZE_DISTORTION.get()) {
            damage /= 3.0f;
        }
        LivingEntity infector = INFECTORS.get(entity);
        entity.hurt(infector != null ? JDamageSources.phpoison(entity.level(), infector) : JDamageSources.phpoison(entity.level()), damage);
    }
}
