package net.arna.jcraft.client.gui.hud;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.util.RenderUtils;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

@UtilityClass
public class JCraftAbilityHud {
    /// ICON HUD
    public static final Identifier GUI_ICONS_TEXTURE = new Identifier("textures/gui/icons.png");

    final IconPos ICON = new IconPos("icon", 10, 18 * 3 + 18);

    private static final int iconSpacing = 8;

    // Stand icons
    private static final IconPos LIGHT = new IconPos("light", 0, iconSpacing * 2); // 2 + 3 * N
    private static final IconPos HEAVY = new IconPos("heavy", 0, iconSpacing * 5);
    private static final IconPos BARRAGE = new IconPos("barrage", 0, iconSpacing * 8);
    private static final IconPos ULT = new IconPos("ult", 0, iconSpacing * 23);
    private static final IconPos SPECIAL_1 = new IconPos("special1", 0, iconSpacing * 14);
    private static final IconPos SPECIAL_2 = new IconPos("special2", 0, iconSpacing * 17);
    private static final IconPos SPECIAL_3 = new IconPos("special3", 0, iconSpacing * 20);
    private static final IconPos UTILITY = new IconPos("utility", 0, iconSpacing * 11);

    private static final IconPos MID_SPECIAL_1 = new IconPos("special1", 24, iconSpacing * 11);
    private static final IconPos MID_SPECIAL_2 = new IconPos("special2", 24, iconSpacing * 14);
    private static final IconPos MID_SPECIAL_3 = new IconPos("special3", 24, iconSpacing * 17);
    private static final IconPos MID_ULT = new IconPos("ult", 24, iconSpacing * 20);

    // Universal icons
    private static final IconPos COMBO_BREAKER = new IconPos("combobreaker", 24, iconSpacing * 2);
    private static final IconPos COOLDOWN_CANCEL = new IconPos("cooldowncancel", 24, iconSpacing * 5);
    private static final IconPos DASH = new IconPos("dash", 24, iconSpacing * 8);

    // Spec-only icons
    private static final IconPos SPEC_HEAVY = new IconPos("heavy", 0, iconSpacing * 5);
    private static final IconPos SPEC_BARRAGE = new IconPos("barrage", 0, iconSpacing * 8);
    private static final IconPos SPEC_SPECIAL_1 = new IconPos("special1", 0, iconSpacing * 11);
    private static final IconPos SPEC_SPECIAL_2 = new IconPos("special2", 0, iconSpacing * 14);
    private static final IconPos SPEC_SPECIAL_3 = new IconPos("special3", 0, iconSpacing * 17);
    private static final IconPos SPEC_ULT = new IconPos("ult", 0, iconSpacing * 20);

    private static final Map<CooldownType, IconPos> STAND_ICONS = ImmutableMap.<CooldownType, IconPos>builder()
            .put(CooldownType.STAND_LIGHT, LIGHT)
            .put(CooldownType.STAND_HEAVY, HEAVY)
            .put(CooldownType.STAND_BARRAGE, BARRAGE)
            .put(CooldownType.STAND_ULTIMATE, ULT)
            .put(CooldownType.STAND_SP1, SPECIAL_1)
            .put(CooldownType.STAND_SP2, SPECIAL_2)
            .put(CooldownType.STAND_SP3, SPECIAL_3)
            .put(CooldownType.UTILITY, UTILITY)
            .build();
    // Used for JConfig.UIPos.MIDDLE, to prevent overwhelming verticality
    private static final Map<CooldownType, IconPos> STAND_ICONS_MID = ImmutableMap.<CooldownType, IconPos>builder()
            .put(CooldownType.STAND_LIGHT, LIGHT)
            .put(CooldownType.STAND_HEAVY, HEAVY)
            .put(CooldownType.STAND_BARRAGE, BARRAGE)
            .put(CooldownType.STAND_ULTIMATE, MID_ULT)
            .put(CooldownType.STAND_SP1, MID_SPECIAL_1)
            .put(CooldownType.STAND_SP2, MID_SPECIAL_2)
            .put(CooldownType.STAND_SP3, MID_SPECIAL_3)
            .put(CooldownType.UTILITY, UTILITY)
            .build();
    private static final Map<CooldownType, IconPos> UNIVERSAL_ICONS = ImmutableMap.<CooldownType, IconPos>builder()
            .put(CooldownType.COMBO_BREAKER, COMBO_BREAKER)
            .put(CooldownType.COOLDOWN_CANCEL, COOLDOWN_CANCEL)
            .put(CooldownType.DASH, DASH)
            .build();
    private static final Map<CooldownType, IconPos> SPEC_ICONS = ImmutableMap.<CooldownType, IconPos>builder()
            .put(CooldownType.HEAVY, SPEC_HEAVY)
            .put(CooldownType.BARRAGE, SPEC_BARRAGE)
            .put(CooldownType.ULTIMATE, SPEC_ULT)
            .put(CooldownType.SPECIAL1, SPEC_SPECIAL_1)
            .put(CooldownType.SPECIAL2, SPEC_SPECIAL_2)
            .put(CooldownType.SPECIAL3, SPEC_SPECIAL_3)
            .build();

    public static int getHudX(int scaledX, int rightOffset) {
        return switch (JClientConfig.getInstance().getUiPosition()) {
            case LEFT -> 2;
            case RIGHT -> scaledX - rightOffset;
            case MIDDLE -> (int) (scaledX * 0.55f);
        };
    }

    public static void render(DrawContext ctx, boolean renderCooldownOverlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        boolean isMid = JClientConfig.getInstance().getUiPosition() == JClientConfig.UIPos.MIDDLE;
        boolean useIcons = JClientConfig.getInstance().isIconHud();
        StandEntity<?, ?> stand = JUtils.getStand(player);

        if (useIcons) {
            int selectedX = getHudX(client.getWindow().getScaledWidth(), 48);
            int selectedY = isMid ? iconSpacing * 11 : 0;

            JSpec<?, ?> spec = JUtils.getSpec(player);

            if (stand == null) {
                // Render cooldown HUD for specs
                if (spec != null)
                    renderIcons(ctx, SPEC_ICONS, selectedX, selectedY, spec.getType().getInternalName().toLowerCase(), renderCooldownOverlay);
            } else {
                // Render cooldown HUD for stands
                renderIcons(ctx, isMid ? STAND_ICONS_MID : STAND_ICONS, selectedX, selectedY, stand.getType().getUntranslatedName(), renderCooldownOverlay);
            }

            renderIcons(ctx, UNIVERSAL_ICONS, selectedX, selectedY, "universal", renderCooldownOverlay);
        }
    }

    /**
     * Renders specified list of icons.
     * @param icons list of icons to render
     * @param selectedX x offset (in pixels) accounting for player's config choice
     * @param selectedY y offset (in pixels) accounting for player's config choice
     * @param type decides which resource folder is loaded when rendering icons
     */
    private static void renderIcons(DrawContext ctx, Map<CooldownType, IconPos> icons, int selectedX, int selectedY,
                                    String type, boolean renderCooldownOverlay) {
        icons.forEach((cooldownType, iconPos) -> {
            int iconX = iconPos.x() + selectedX;
            int iconY = iconPos.y() + selectedY;

            double cd = getCooldownProgress(cooldownType);
            if (cd < 0) return;

            if (!renderCooldownOverlay) renderBorder(ctx, iconX, iconY);
            renderIcon(ctx, iconX, iconY, type, iconPos.name(), cd, renderCooldownOverlay);
        });
    }

    public static void renderIcon(DrawContext ctx, int x, int y, String type, String icon, double cd, boolean renderCooldownOverlay) {
        Identifier texture = JCraft.id("textures/gui/ability_icons/" + type + "/"+ icon + ".png");
        renderIcon(ctx,  x, y, texture, icon, cd, renderCooldownOverlay);
    }

    public static void renderIcon(DrawContext ctx, int x, int y, Identifier texture, String fallback, double cd, boolean renderCooldownOverlay) {
        var matrices = ctx.getMatrices();
        matrices.push();

        if (isTextureAvailable(texture)) {
            RenderSystem.setShaderTexture(0, texture);
        } else {
            texture = JCraft.id("textures/gui/ability_icons/fallback/"+ fallback + ".png");
            RenderSystem.setShaderTexture(0, texture);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        if (!renderCooldownOverlay) ctx.drawTexture(texture, x + 2 , y + 2,0,0,18,18, 18 ,18);
        else renderCooldown(cd, x, y);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void renderBorder(DrawContext ctx, int x, int y){
        var matrices = ctx.getMatrices();
        matrices.push();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ctx.drawTexture(JCraft.id("textures/gui/ability_icons/border.png"), x , y,0,0,22,22, 22 ,22);
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
     * @param type The type of the cooldown
     * @return Progress value of cooldown between 0 and 1 if it is present, otherwise defaults to -1
     */
    private static double getCooldownProgress(CooldownType type) {
        CooldownsComponent cooldowns = JComponents.getCooldowns(MinecraftClient.getInstance().player);
        int cooldown = cooldowns.getCooldown(type);
        int initialDuration = cooldowns.getInitialDuration(type);

        return cooldown > 0 && initialDuration != 0 ? normalize(cooldown, 0, initialDuration) : -1;
    }

    public static void renderCooldown(double cd, int x, int y) {
        RenderSystem.disableDepthTest();
        //TODO? RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderUtils.renderGuiQuad(buf, x, y + MathHelper.floor(22.0 * (1.0 - cd)), 22, MathHelper.ceil(22.0 * cd), 255, 255, 255, 127);
        // TODO? RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private static double normalize(double value, double min, double max) {
        return ((value - min) / (max - min));
    }

    private record IconPos(String name, int x, int y){}
}
