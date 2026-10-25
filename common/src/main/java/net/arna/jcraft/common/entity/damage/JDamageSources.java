package net.arna.jcraft.common.entity.damage;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

public class JDamageSources {
    public static ResourceKey<DamageType> createDamageType(final String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, JCraft.id(name));
    }

    public static final ResourceKey<DamageType> SPEC = createDamageType("spec");
    public static final ResourceKey<DamageType> STAND = createDamageType("stand");
    public static final ResourceKey<DamageType> WHITE_SNAKE_POISON = createDamageType("wspoison");
    public static final ResourceKey<DamageType> BLEEDING = createDamageType("jbleeding");
    public static final ResourceKey<DamageType> PHPOISON = createDamageType("phpoison");
    public static final ResourceKey<DamageType> AP_BULLET = createDamageType("armor_piercing_bullet");

    public static DamageSource create(final Level world, final ResourceKey<DamageType> key, final @Nullable Entity source, final @Nullable Entity attacker) {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), source, attacker);
    }

    public static DamageSource create(final Level world, final ResourceKey<DamageType> key, final @Nullable Entity attacker) {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), attacker);
    }

    public static DamageSource create(final Level world, final ResourceKey<DamageType> key) {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }

    public static @NonNull DamageSource spec(final LivingEntity attacker) {
        return create(attacker.level(), SPEC, attacker);
    }

    public static @NonNull DamageSource stand(final StandEntity<?, ?> stand) {
        return create(stand.level(), STAND, stand.getUser());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull DamageSource whitesnakePoison(Entity user) {
        return create(user.level(), WHITE_SNAKE_POISON, user);
    }

    public static @NonNull DamageSource bleeding(Level world) {
        return create(world, BLEEDING);
    }

    public static @NonNull DamageSource phpoison(Level world) {
        return create(world, PHPOISON);
    }

    public static @NonNull DamageSource apBullet(Level world, Entity attacker) {
        return create(world, AP_BULLET, attacker);
    }

    public static @NonNull DamageSource phpoison(Level world, Entity attacker) {
        return create(world, PHPOISON, attacker);
    }
}
