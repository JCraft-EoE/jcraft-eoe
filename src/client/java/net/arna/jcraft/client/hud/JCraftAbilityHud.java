package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.client.util.RenderUtils;
import net.arna.jcraft.common.JConfig;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

public class JCraftAbilityHud extends DrawableHelper implements ClientTickEvents.EndTick {
    public static final JCraftAbilityHud INSTANCE = new JCraftAbilityHud();
    public static final DefaultedList<Double> cachedCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);

    public static final Identifier GUI_ICONS_TEXTURE = new Identifier("textures/gui/icons.png");

    final IconPos ICON = new IconPos("icon", 10, 18 * 3 + 18);

    public static final int iconSpacing = 8;

    // Stand icons
    static final IconPos LIGHT = new IconPos("light", 0, iconSpacing * 2); // 2 + 3 * N
    static final IconPos HEAVY = new IconPos("heavy", 0, iconSpacing * 5);
    static final IconPos BARRAGE = new IconPos("barrage", 0, iconSpacing * 8);
    static final IconPos ULT = new IconPos("ult", 0, iconSpacing * 23);
    static final IconPos SPECIAL_1 = new IconPos("special1", 0, iconSpacing * 14);
    static final IconPos SPECIAL_2 = new IconPos("special2", 0, iconSpacing * 17);
    static final IconPos SPECIAL_3 = new IconPos("special3", 0, iconSpacing * 20);
    static final IconPos UTILITY = new IconPos("utility", 0, iconSpacing * 11);

    static final IconPos MID_SPECIAL_1 = new IconPos("special1", 24, iconSpacing * 11);
    static final IconPos MID_SPECIAL_2 = new IconPos("special2", 24, iconSpacing * 14);
    static final IconPos MID_SPECIAL_3 = new IconPos("special3", 24, iconSpacing * 17);
    static final IconPos MID_ULT = new IconPos("ult", 24, iconSpacing * 20);

    // Universal icons
    static final IconPos COMBO_BREAKER = new IconPos("combobreaker", 24, iconSpacing * 2);
    static final IconPos COOLDOWN_CANCEL = new IconPos("cooldowncancel", 24, iconSpacing * 5);
    static final IconPos DASH = new IconPos("dash", 24, iconSpacing * 8);

    // Spec-only icons
    static final IconPos SPEC_HEAVY = new IconPos("heavy", 0, iconSpacing * 5);
    static final IconPos SPEC_BARRAGE = new IconPos("barrage", 0, iconSpacing * 8);
    static final IconPos SPEC_SPECIAL_1 = new IconPos("special1", 0, iconSpacing * 11);
    static final IconPos SPEC_SPECIAL_2 = new IconPos("special2", 0, iconSpacing * 14);
    static final IconPos SPEC_SPECIAL_3 = new IconPos("special3", 0, iconSpacing * 17);
    static final IconPos SPEC_ULT = new IconPos("ult", 0, iconSpacing * 20);

    private static final List<IconPos> STAND_ICONS = Arrays.asList(LIGHT, HEAVY, BARRAGE, ULT, SPECIAL_1, SPECIAL_2, SPECIAL_3, UTILITY);
    // Used for JConfig.UIPos.MIDDLE, to prevent overwhelming verticality
    private static final List<IconPos> STAND_MID_ICONS = Arrays.asList(LIGHT, HEAVY, BARRAGE, MID_ULT, MID_SPECIAL_1, MID_SPECIAL_2, MID_SPECIAL_3, UTILITY);
    private static final List<IconPos> UNIVERSAL_ICONS = Arrays.asList(COMBO_BREAKER, COOLDOWN_CANCEL, DASH);
    private static final List<IconPos> SPEC_ICONS = Arrays.asList(SPEC_HEAVY, SPEC_BARRAGE, SPEC_ULT, SPEC_SPECIAL_1, SPEC_SPECIAL_2, SPEC_SPECIAL_3);

    private JCraftAbilityHud() {}

    private static int getHudX(int scaledX) {
        switch (JConfig.UI_POSITION) {
            case LEFT -> {
                return 2;
            }
            case RIGHT -> {
                return scaledX - 48;
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

    public static void render(MatrixStack matrices, boolean renderCooldownOverlay) {
        if (!JConfig.ICON_HUD) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        boolean isMid = JConfig.UI_POSITION == JConfig.UIPos.MIDDLE;

        int selectedX = getHudX(client.getWindow().getScaledWidth());
        int selectedY = isMid ? iconSpacing * 11 : 0;

        if (player != null) {
            StandEntity stand = ( (IEntityDataSaver)player ).getStand();
            JCraftSpec spec = JClientUtils.getSpec(player);

            if (stand == null) {
                // Render cooldown HUD for specs
                if (spec != null) renderIcons(matrices, SPEC_ICONS, selectedX, selectedY, 11, spec.getName().toLowerCase(), renderCooldownOverlay);
            } else {
                // Render cooldown HUD for stands
                renderIcons(matrices, isMid ? STAND_MID_ICONS : STAND_ICONS, selectedX, selectedY, 0, stand.getType().getUntranslatedName(), renderCooldownOverlay);
            }

            renderIcons(matrices, UNIVERSAL_ICONS, selectedX, selectedY, 8, "universal", renderCooldownOverlay);
        }
    }

    /**
     * Renders specified list of icons.
     * @param icons list of icons to render
     * @param selectedX x offset (in pixels) accounting for player's config choice
     * @param selectedY y offset (in pixels) accounting for player's config choice
     * @param indexOffset (relative to JCraft.cooldowns)
     * @param type decides which resource folder is loaded when rendering icons
     */
    private static void renderIcons(MatrixStack matrices, List<IconPos> icons, int selectedX, int selectedY, int indexOffset,
                                    String type, boolean renderCooldownOverlay) {
        for (int i = 0; i < icons.size(); i++) {
            IconPos iconPos = icons.get(i);
            int iconX = iconPos.x() + selectedX;
            int iconY = iconPos.y() + selectedY;

            int offset = i + indexOffset;
            double cd = isCoolingDown(offset);

            if (cd < 0) continue;
            if (!renderCooldownOverlay) renderBorder(matrices, iconX, iconY);
            renderIcon(matrices, iconX, iconY, type, iconPos.name(), cd, renderCooldownOverlay);
        }
    }

    public static void renderIcon(MatrixStack matrices, int x, int y, String type, String icon, double cd, boolean renderCooldownOverlay) {
        Identifier texture = JCraft.id("textures/gui/ability_icons/" + type + "/"+ icon + ".png");
        renderIcon(matrices,  x, y, texture, icon, cd, renderCooldownOverlay);
    }

    public static void renderIcon(MatrixStack matrices, int x, int y, Identifier texture, String fallback, double cd, boolean renderCooldownOverlay) {
        matrices.push();

        if (isTextureAvailable(texture)) {
            RenderSystem.setShaderTexture(0, texture);
        } else {
            texture = JCraft.id("textures/gui/ability_icons/fallback/"+ fallback + ".png");
            RenderSystem.setShaderTexture(0, texture);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        if (!renderCooldownOverlay) drawTexture(matrices, x + 2 , y + 2,0,0,18,18, 18 ,18);
        else renderCooldown(cd, x, y);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void renderBorder(MatrixStack matrices, int x, int y){
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

    /**
     * @param index of the cooldown
     * @return value of cooldown if it is present, otherwise defaults to -1
     */
    private static double isCoolingDown(int index) {
        int i = 0;
        for (double cd : JCraftClient.clientCooldowns) {
            if (index == i && cd > 0.0D && cachedCooldowns.get(i) != 0)
                return normalize(cd, 0, cachedCooldowns.get(i));
            i++;
        }
        return -1;
    }

    public static void renderCooldown(double cd, int x, int y) {
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

    static double normalize(double value, double min, double max) {
        return ((value - min) / (max - min));
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
