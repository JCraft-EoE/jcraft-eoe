package net.arna.jcraft.common.entity.damage;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class JDamageSources {
    public static @NotNull DamageSource stand(StandEntity<?, ?> stand) {
        return new EntityDamageSource("stand", stand.getUser()).setBypassesArmor();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull DamageSource whitesnakePoison(Entity user) {
        return new EntityDamageSource("wspoison", user);
    }

    public static @NotNull DamageSource bleeding() {
        return new DamageSource("jbleeding");
    }

    public static @NotNull DamageSource phpoison() {
        return new DamageSource("phpoison").setBypassesArmor().setUnblockable();
    }
}
