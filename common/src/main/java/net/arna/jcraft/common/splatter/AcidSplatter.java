package net.arna.jcraft.common.splatter;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JSplatterTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.splatter.Splatter;
import net.arna.jcraft.api.splatter.SplatterFactory;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class AcidSplatter extends Splatter {
    private static final ResourceLocation texture = JCraft.id("textures/effect/splatter/acid.png");

    public AcidSplatter(Level world, Vec3 pos, Direction direction, float xRange, float zRange, int maxAge, @Nullable LivingEntity creator) {
        super(world, pos, direction, xRange, zRange, maxAge, creator);
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public RegistrySupplier<SplatterFactory> getType() {
        return JSplatterTypeRegistry.ACID_SPLATTER_TYPE;
    }

    @Override
    protected void baseTick(List<Runnable> toRunAfterTick) {
        if (getAge() % 4 != 0) return;

        for (LivingEntity hit : getWorld().getEntitiesOfClass(LivingEntity.class, getMainBox(),
                EntitySelector.LIVING_ENTITY_STILL_ALIVE
                        .and(EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                        .and(Predicate.not(JUtils::inTimeErase))
                )) {
            if (!intersects(hit.getBoundingBox())) {
                continue;
            }

            if (getWorld() != null && getCreator() != null &&
                    (hit.isPassengerOfSameVehicle(Objects.requireNonNull(getCreator())) ||
                    (hit instanceof StandEntity<?, ?> stand && stand.getUser() == getCreator()))) {
                continue;
            }

            hit.addEffect(new MobEffectInstance(JStatusRegistry.WSPOISON.get(), 20, 0, true, false));
            hit.hurt(JDamageSources.create(getWorld(), JDamageSources.WHITE_SNAKE_POISON, getCreator()), 2f);
        }
    }
}
