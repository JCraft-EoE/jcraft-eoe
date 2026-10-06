package net.arna.jcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.aim.GunAimHandler;
import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Unique
    private static final float JCRAFT$GUN_RAISE_SPEED = 0.4f;
    @Unique
    private static final float JCRAFT$AIM_RAISE = 0.3f;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private ItemStack mainHandItem;

    /**
     * Firing and reloading rewrite the gun's NBT, and vanilla compares the held stack by value, so
     * every one of those writes reads as a different item and replays the equip animation, dipping
     * the gun. Re-pointing at the live stack first means the comparison sees the same gun it did
     * last tick. Swapping to a genuinely different item is left alone, so the draw still plays.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void jcraft$holdGunReference(CallbackInfo ci) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof Peacemaker) {
            // Vanilla only adopts the held stack once the hand has dropped out of view, which the
            // raise below never lets happen, so the gun has to be adopted here or the hand would
            // keep rendering whatever was held before it.
            if (!ItemStack.isSameItem(mainHandItem, mainHand)) {
                oMainHandHeight = 0.0f;
                mainHandHeight = 0.0f;
            }
            mainHandItem = mainHand;
        }
    }

    /**
     * Vanilla also lowers the hand by the melee attack cooldown, which every click resets even
     * though a gun never swings, so the gun bobs on each shot. Only first person reads these
     * heights, which is why nothing else shows it. Hold the gun up instead.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void jcraft$keepGunAtRest(CallbackInfo ci) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (player.getMainHandItem().getItem() instanceof Peacemaker) {
            mainHandHeight += Mth.clamp(1.0f - mainHandHeight, -JCRAFT$GUN_RAISE_SPEED, JCRAFT$GUN_RAISE_SPEED);
        }
    }

    @Inject(method = "applyItemArmTransform", at = @At("TAIL"))
    private void jcraft$centerGunOnAim(PoseStack poseStack, HumanoidArm arm, float equippedProgress, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || arm != minecraft.player.getMainArm()) {
            return;
        }
        if (!(minecraft.player.getMainHandItem().getItem() instanceof Peacemaker)) {
            return;
        }
        float aim = GunAimHandler.progress(minecraft.getFrameTime());
        if (aim <= 0f) {
            return;
        }
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        ItemDisplayContext displayContext = arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemTransform transform = minecraft.getItemRenderer()
                .getModel(minecraft.player.getMainHandItem(), minecraft.level, minecraft.player, 0)
                .getTransforms().getTransform(displayContext);
        float displayX = side * transform.translation.x();
        poseStack.translate((-side * 0.56f - displayX) * aim, JCRAFT$AIM_RAISE * aim, 0.0f);
    }
}
