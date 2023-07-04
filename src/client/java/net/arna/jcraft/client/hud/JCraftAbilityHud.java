package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.util.RenderUtils;
import net.arna.jcraft.common.JConfig;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JCraftUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
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

    public static final int iconSpacing = 9;

    static final IconPos LIGHT = new IconPos("light", 10, iconSpacing * 2);
    static final IconPos HEAVY = new IconPos("heavy", 10, iconSpacing * 5); // 2 + 3 * N
    static final IconPos BARRAGE = new IconPos("barrage", 10, iconSpacing * 8);
    static final IconPos UTILITY = new IconPos("utility", 10, iconSpacing * 11);
    static final IconPos SPECIAL_1 = new IconPos("special1", 10, iconSpacing * 14);
    static final IconPos SPECIAL_2 = new IconPos("special2", 10, iconSpacing * 17);
    static final IconPos SPECIAL_3 = new IconPos("special3", 10, iconSpacing * 20);
    static final IconPos ULT = new IconPos("ult", 10, iconSpacing * 23);

    // Spec-only icons
    static final IconPos SPEC_SPECIAL_1 = new IconPos("special1", 10, iconSpacing * 11);
    static final IconPos SPEC_SPECIAL_2 = new IconPos("special2", 10, iconSpacing * 14);
    static final IconPos SPEC_SPECIAL_3 = new IconPos("special3", 10, iconSpacing * 17);
    static final IconPos SPEC_ULT = new IconPos("ult", 10, iconSpacing * 20);


    final List<IconPos> STANDICONS = Arrays.asList(LIGHT, HEAVY, BARRAGE, UTILITY, SPECIAL_1, SPECIAL_2, SPECIAL_3, ULT);
    final List<IconPos> SPECICONS = Arrays.asList(HEAVY, BARRAGE, SPEC_SPECIAL_1, SPEC_SPECIAL_2, SPEC_SPECIAL_3, SPEC_ULT);

    private static int getHudX(int scaledX) {
        switch (JConfig.UI_POSITION) {
            case LEFT -> {
                return (int) (scaledX * 0.01f);
            }
            case RIGHT -> {
                return (int) (scaledX * 0.95f);
            }
            case MIDDLE -> {
                return (int) (scaledX * 0.55f);
            }
            default -> {
                JCraft.LOGGER.error("JCraft UI position is set to an invalid value!");
                return 10;
            }
        }
    }

    @Override
    public void onHudRender(MatrixStack matrices, float tickDelta) {
        if (!JConfig.ICON_HUD) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        int selectedX = getHudX(client.getWindow().getScaledWidth());

        if (player != null) {
            StandEntity stand = ( (IEntityDataSaver)player ).getStand();
            JCraftSpec spec = JCraftUtils.getSpec(player);
            if (stand == null) {
                // Render cooldown HUD for specs
                if (spec != null) {
                    for (int i = 0; i < SPECICONS.size(); i++) {
                        IconPos iconPos = SPECICONS.get(i);
                        int iconY = iconPos.y();

                        if (JConfig.UI_POSITION == JConfig.UIPos.MIDDLE) { // Special positioning for middle HUD position
                            iconY += iconSpacing * 11;
                            if (i > 3) {
                                iconY -= iconSpacing * 12;
                                if (i == 4) selectedX += 28; // 22x22 border with 6px spacing
                            }
                        }

                        //TODO: icon doesn't render if the related move is not on cooldown (spec only)
                        //here, check if the cooldown is <= 0 then stop/continue
                        //renderBorder(matrices, selectedX, iconY);
                        //renderIcon(matrices, selectedX, iconY, spec.getName().toLowerCase(), iconPos.name(), i);
                    }
                }
            } else {
                // Render cooldown HUD for stands
                for (int i = 0; i < STANDICONS.size(); i++) {
                    IconPos iconPos = STANDICONS.get(i);
                    int iconY = iconPos.y();

                    if (JConfig.UI_POSITION == JConfig.UIPos.MIDDLE) { // Special positioning for middle HUD position
                        iconY += iconSpacing * 11;
                        if (i > 3) {
                            iconY -= iconSpacing * 12;
                            if (i == 4) selectedX += 28; // 22x22 border with 6px spacing
                        }
                    }

                    renderBorder(matrices, selectedX, iconY);
                    renderIcon(matrices, selectedX, iconY, stand.getType().getUntranslatedName(), iconPos.name(), i);
                }
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
            texture = JCraft.id("textures/gui/ability_icons/fallback/"+ fallback + ".png");
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
