package net.arna.jcraft.common.entity.damage;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class JDamageSources {

    public static final RegistryKey<DamageType> STAND = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, JCraft.id("stand"));
    public static final RegistryKey<DamageType> WHITE_SNAKE_POISON = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, JCraft.id("wspoison"));
    public static final RegistryKey<DamageType> BLEEDING = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, JCraft.id("jbleeding"));
    public static final RegistryKey<DamageType> PHPOISON = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, JCraft.id("phpoison"));

    public static DamageSource create(World world, RegistryKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), source, attacker);
    }

    public static DamageSource create(World world, RegistryKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
    }

    public static DamageSource create(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }

    public static @NotNull DamageSource stand(StandEntity<?, ?> stand) {
        return create(stand.getWorld(), STAND, stand.getUser());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull DamageSource whitesnakePoison(Entity user) {
        return create(user.getWorld(), WHITE_SNAKE_POISON, user);
    }

    public static @NotNull DamageSource bleeding(World world) {
        return create(world, BLEEDING);
    }

    public static @NotNull DamageSource phpoison(World world) {
        return create(world, PHPOISON);
    }
}
