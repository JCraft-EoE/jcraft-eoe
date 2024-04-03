package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    private static @Unique int shouldLoadStands = 0;

    // Prevent stand entities from being loaded from NBT, they will be reconstructed instead.
    // Loading stands from NBT tends to break them.
    @Inject(method = "getEntityFromNbt", at = @At("HEAD"), cancellable = true)
    private static void doNotLoadStandEntities(NbtCompound nbt, World world, CallbackInfoReturnable<Optional<Entity>> cir) {
        if (shouldLoadStands > 0) return;

        EntityType<?> entityType = Registries.ENTITY_TYPE.get(new Identifier(nbt.getString("id")));
        if (StandType.getEntityTypes().contains(entityType)) cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "method_17843", at = @At("HEAD"))
    private static void doLoadStandsWhenLoadingArmorStandPre(NbtCompound nbtCompound, World world, Function<Entity, Entity> function,
                                                          Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (entity instanceof ArmorStandEntity) shouldLoadStands = Math.max(1, shouldLoadStands + 1);
    }

    @Inject(method = "method_17843", at = @At("RETURN"))
    private static void doLoadStandsWhenLoadingArmorStandPost(NbtCompound nbtCompound, World world, Function<Entity, Entity> function,
                                                          Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (entity instanceof ArmorStandEntity) shouldLoadStands--;
    }
}
