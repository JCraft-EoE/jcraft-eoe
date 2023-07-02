package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.util.RenderUtils;
import net.arna.jcraft.common.entity.StandEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;

public class JCraftAbilityHud extends DrawableHelper implements HudRenderCallback, ClientTickEvents.EndTick {
    public static DefaultedList<Double> cachedCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);

    public static final Identifier GUI_ICONS_TEXTURE = new Identifier("textures/gui/icons.png");

    final IconPos ICON = new IconPos("icon", 10, 18 * 3 + 18);

    final IconPos LIGHT = new IconPos("light", 10, 9 * 2);
    final IconPos HEAVY = new IconPos("heavy", 10, 9 * 5);
    final IconPos BARRAGE = new IconPos("barrage", 10, 9 * 8);

    final IconPos SPECIAL_1 = new IconPos("special1", 10, 9 * 11);
    final IconPos SPECIAL_2 = new IconPos("special2", 10, 9 * 14);
    final IconPos SPECIAL_3 = new IconPos("special3", 10, 9 * 17);

    final IconPos ULT = new IconPos("ult", 10, 9 * 20);

    final List<IconPos> ICONS = Arrays.asList(LIGHT, HEAVY, BARRAGE, ULT, SPECIAL_1, SPECIAL_2, SPECIAL_3);

    @Override
    public void onHudRender(MatrixStack matrices, float tickDelta) {
        var player = MinecraftClient.getInstance().player;
        if (player != null && player.getFirstPassenger() instanceof StandEntity stand) {
            for (IconPos iconPos : ICONS) {
                renderBorder(matrices, iconPos.x(), iconPos.y());
                renderIcon(matrices, iconPos.x(), iconPos.y(), stand.getType().getUntranslatedName(), iconPos.name(), ICONS.indexOf(iconPos));
            }
        }
    }

    public void renderIcon(MatrixStack matrices, int x, int y, String standName, String icon, int index){
        Identifier texture = JCraft.id("textures/gui/ability_icons/" + standName + "/"+ icon + ".png");
        renderIcon(matrices,  x, y, texture, index, icon);
    }

    public void renderIcon(MatrixStack matrices, int x, int y, Identifier texture, int index, String fallback){
        matrices.push();

        if (isTextureAvailable(texture)) {
            RenderSystem.setShaderTexture(0, texture);
        } else {
            texture = JCraft.id("textures/gui/ability_icons/starplatinum/"+ fallback + ".png");
            RenderSystem.setShaderTexture(0, texture);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        drawTexture(matrices, x + 2 , y + 2,0,0,18,18, 18 ,18);
        renderCooldown(x, y, index);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    public void renderBorder(MatrixStack matrices, int x, int y){
        matrices.push();
        RenderSystem.setShaderTexture(0, JCraft.id("textures/gui/ability_icons/border.png"));
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        drawTexture(matrices, x , y,0,0,22,22, 22 ,22);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static boolean isTextureAvailable(Identifier textureLocation) {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        try {
            return resourceManager.getResource(textureLocation).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public void renderCooldown(int x, int y, int index){
        DefaultedList<Double> cooldowns = JCraftClient.clientCooldowns;
        int i = 0;
        for (double cd : cooldowns) {
            if (index == i && cd > 0.0D && cachedCooldowns.get(i) != 0) {
                cd = normalize(cd, 0, cachedCooldowns.get(i));

                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator2 = Tessellator.getInstance();
                BufferBuilder bufferBuilder2 = tessellator2.getBuffer();
                RenderUtils.renderGuiQuad(bufferBuilder2, x, y + MathHelper.floor(22.0 * (1.0 - cd)), 22, MathHelper.ceil(22.0 * cd), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
            i++;
        }
    }

    double normalize(double value, double min, double max) {
        return  ((value - min) / (max - min));
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        DefaultedList<Double> cooldowns = JCraftClient.clientCooldowns;
        int index = 0;
        for (double cd : cooldowns) {
            if (cd > 0 && cachedCooldowns.get(index) == 0) {
                cachedCooldowns.set(index, cd);
            } else if (cd <= 0 && cachedCooldowns.get(index) != 0) {
                cachedCooldowns.set(index, 0.0);
            }
            index++;
        }
    }

    record IconPos(String name, int x, int y){}
}
