package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    private static @Unique int jcraft$shouldLoadStands = 0;

    // Prevent stand entities from being loaded from NBT, they will be reconstructed instead.
    // Loading stands from NBT tends to break them.
    @WrapMethod(method = "create(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;")
    private static Optional<Entity> jcraft$doNotLoadStandEntities(final CompoundTag tag, final Level level, final Operation<Optional<Entity>> original) {
        if (jcraft$shouldLoadStands <= 0) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(tag.getString("id")));
            if (StandTypeUtil.streamEntityTypes().toList().contains(entityType)) {
                return Optional.empty();
            }
        }
        return original.call(tag, level);
    }

    @Inject(method = "method_17843", at = @At("HEAD"))
    private static void jcraft$doLoadStandsWhenLoadingArmorStandPre(CompoundTag compoundTag, Level level, Function<Entity, Entity> function,
                                                                    Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (entity instanceof ArmorStand) {
            jcraft$shouldLoadStands = Math.max(1, jcraft$shouldLoadStands + 1);
        }
    }

    @Inject(method = "method_17843", at = @At("RETURN"))
    private static void jcraft$doLoadStandsWhenLoadingArmorStandPost(CompoundTag compoundTag, Level level, Function<Entity, Entity> function,
                                                                     Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (entity instanceof ArmorStand) {
            jcraft$shouldLoadStands--;
        }
    }

}
