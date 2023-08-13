package net.arna.jcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.gravity.util.GravityChannelClient;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.net.ClientPacketHandler;
import net.arna.jcraft.client.particle.*;
import net.arna.jcraft.client.registry.JArmorRendererRegistry;
import net.arna.jcraft.client.registry.JClientEventsRegistry;
import net.arna.jcraft.client.registry.JEntityRendererRegister;
import net.arna.jcraft.client.registry.JRenderLayerRegistry;
import net.arna.jcraft.client.renderer.block.ShaderTestBlockEntityRenderer;
import net.arna.jcraft.client.renderer.effects.AttackHitBoxEffectRenderer;
import net.arna.jcraft.client.renderer.effects.SplatterEffectRenderer;
import net.arna.jcraft.client.renderer.effects.TimeAccelerationEffectRenderer;
import net.arna.jcraft.client.renderer.effects.TimeErasePredictionEffectRenderer;
import net.arna.jcraft.client.renderer.item.BigItemRenderer;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.arna.jcraft.client.rendering.handler.EpitaphVignetteShaderHandler;
import net.arna.jcraft.client.rendering.handler.UIShaderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.client.util.ClientEntityHandlerImpl;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.c2s.InputSyncPacket;
import net.arna.jcraft.common.network.c2s.StandControlPacket;
import net.arna.jcraft.common.util.ColorUtils;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.DimValues;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.arna.jcraft.client.util.JClientUtils.activeTimestops;

public class JCraftClient implements ClientModInitializer {
    // Combo counting
    private final List<String> comboRemarks = List.of("admin rdm!!!", "baby combo", "caught lackin", "kinda ez", "skill issue", "cancelled on twitter", "sent to bulgaria", "down bad");
    public static int comboCounter = 0;
    public static float damageScaling = 1.00f;
    public static int framesSinceCounted = 0;

    // Keybinds
    public static KeyBinding standSummon;
    public static KeyBinding heavyKey;
    public static KeyBinding barrageKey;
    public static KeyBinding ultKey;
    public static KeyBinding special1Key;
    public static KeyBinding special2Key;
    public static KeyBinding special3Key;
    public static KeyBinding comboBreaker;
    public static KeyBinding cooldownCancel;
    public static KeyBinding utility;
    public static KeyBinding dash;
    // TODO this should probably be updated when the Minecraft language is updated.
    @Getter(lazy = true)
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.forLanguageTag(MinecraftClient.getInstance().options.language)));

    @Override
    public void onInitializeClient() {
        JCraft.setClientEntityHandler(ClientEntityHandlerImpl.INSTANCE);
        // MidnightConfig.init(JCraft.MOD_ID, JConfig.class);

        AutoConfig.register(JClientConfig.class, JanksonConfigSerializer::new);
        JClientConfig.load();

        GravityChannelClient.init();

        // Rendering
        JRenderLayerRegistry.init();
        RenderHandler.init();
        JClientEventsRegistry.registerClientEvents();

        ZaWarudoShaderHandler.INSTANCE.init();
        CrimsonShaderHandler.INSTANCE.init();
        EpitaphVignetteShaderHandler.INSTANCE.init();
        UIShaderHandler.INSTANCE.init(); // Should be last

        // Particle registration
        ParticleFactoryRegistry particleFactoryRegistry = ParticleFactoryRegistry.getInstance();
        particleFactoryRegistry.register(JParticleTypeRegistry.COMBO_BREAK, ComboBreakerParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_1, provider -> new HitsparkParticle.Factory(provider, 0.5f));
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_2, provider -> new HitsparkParticle.Factory(provider, 1f));
        particleFactoryRegistry.register(JParticleTypeRegistry.KCPARTICLE, KCParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BACKSTAB, BackstabParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.SPEEDPARTICLE, SpeedParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BITES_THE_DUST, BitesTheDustParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BOOM_1, BoomParticle.Factory::new);

        // Renderer registration
        JEntityRendererRegister.registerEntityRenderers();
        JArmorRendererRegistry.registerArmorRenderers();
        BlockEntityRendererFactories.register(JBlockEntityTypeRegistry.SHADER_TEST_BLOCK_ENTITY, ShaderTestBlockEntityRenderer::new);

        // Keybind registration
        standSummon = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.standsummon", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.category.jcraft"));
        heavyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.heavy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category.jcraft"));
        barrageKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.barrage", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.category.jcraft"));
        ultKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.ult", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category.jcraft"));
        special1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.special1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.jcraft"));
        special2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.special2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.category.jcraft"));
        special3Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.special3", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.category.jcraft"));
        //comboBreaker = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.combobreaker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.category.jcraft"));
        cooldownCancel = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.cooldowncancel", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, "key.category.jcraft"));
        utility = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.utility", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, "key.category.jcraft"));
        dash = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.dash", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, "key.category.jcraft"));

        ClientTickEvents.END_CLIENT_TICK.register(this::tickClient);
        ClientTickEvents.END_WORLD_TICK.register(new SkyBoxManager());

        ClientPacketHandler.init();

        AttackHitBoxEffectRenderer.init();
        TimeAccelerationEffectRenderer.init();
        TimeErasePredictionEffectRenderer.init();
        SplatterEffectRenderer.init();

        HudRenderCallback.EVENT.register(this::renderHud);

        // Run when the MinecraftClient instance is fully initialized.
        MinecraftClient.getInstance().send(EpitaphOverlay::preload);

        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        Identifier itemId = JObjectRegistry.ITEMS.get(JObjectRegistry.DEBUG_WAND);
        BigItemRenderer itemRenderer = new BigItemRenderer(itemId);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(itemRenderer);
        BuiltinItemRendererRegistry.INSTANCE.register(JObjectRegistry.DEBUG_WAND, itemRenderer);

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            out.accept(new ModelIdentifier(itemId + "_gui", "inventory"));
            out.accept(new ModelIdentifier(itemId + "_handheld", "inventory"));
        });
    }

    private static int getHudX(int scaledX) {
        switch (JClientConfig.getInstance().getUiPosition()) {
            case LEFT -> {
                return 2;
            }
            case RIGHT -> {
                return scaledX - 128;
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

    @SuppressWarnings("DataFlowIssue") // If the player is null, we have much larger problems than that
    private void renderHud(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        CooldownsComponent cooldowns = JComponents.getCooldowns(player);
        framesSinceCounted++;

        int selectedX = getHudX(client.getWindow().getScaledWidth());
        int selectedY = client.getWindow().getScaledHeight();

        boolean useIcons = JClientConfig.getInstance().isIconHud();

        switch (JClientConfig.getInstance().getUiPosition()) {
            case LEFT -> selectedY /= 20;
            case MIDDLE -> selectedY /= 3;
            case RIGHT -> selectedY = (int) (selectedY / 2.25f);
        }

        TextRenderer textRenderer = client.inGameHud.getTextRenderer();
        if (comboCounter > 0 && player.world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && framesSinceCounted <= 180) {

            String remark = "epic tod free download";
            if (comboCounter < comboRemarks.size() * 7) {
                remark = comboRemarks.get(Math.floorDiv(comboCounter, 7));
            }

            // Combo Counter rendering
            textRenderer.drawWithShadow(
                    matrixStack,
                    comboCounter + " - (" + Math.round(damageScaling * 100f) + "%) - " + remark,
                    selectedX + (framesSinceCounted < 5 ? player.getRandom().nextFloat() * 5f : 0) +
                            ((JClientConfig.getInstance().getUiPosition() == JClientConfig.UIPos.MIDDLE && useIcons) ? 54f : 0),
                    selectedY * (1.15f) + (framesSinceCounted < 5 ? player.getRandom().nextFloat() * 5f : 0),
                    ColorUtils.HSBAtoRGBA(comboCounter / 360f - 1f, 1f, 1f, 0.8f),
                    true
            );
        }

        // Cooldown rendering, for icon hud see JCraftHudOverlay
        if (useIcons) return;
        boolean standOn = JComponents.getStandData(player).getStand() != null;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        CooldownType[] values = CooldownType.values();
        for (int i = 0; i < values.length; i++) {
            CooldownType type = values[i];
            int cooldownTicks = cooldowns.getCooldown(type);

            if (cooldownTicks == 0) continue;
            double cooldown = (cooldownTicks - tickDelta) / 20d;

            // These are (mainly) based off of keybindings which are client-only and thus have
            // to be done here and cannot be done in CooldownType.
            String keyBindText = switch (type) {
                case STAND_LIGHT -> "M1";
                case HEAVY, STAND_HEAVY -> generateName(heavyKey.getBoundKeyTranslationKey());
                case BARRAGE, STAND_BARRAGE -> generateName(barrageKey.getBoundKeyTranslationKey());
                case ULT, STAND_ULT -> generateName(ultKey.getBoundKeyTranslationKey());
                case SP1, STAND_SP1 -> generateName(special1Key.getBoundKeyTranslationKey());
                case SP2, STAND_SP2 -> generateName(special2Key.getBoundKeyTranslationKey());
                case SP3, STAND_SP3 -> generateName(special3Key.getBoundKeyTranslationKey());
                case UTIL -> generateName(utility.getBoundKeyTranslationKey());
                case COMBO_BREAKER -> "Combo Breaker";
                case COOLDOWN_CANCEL -> generateName(cooldownCancel.getBoundKeyTranslationKey());
                case DASH -> generateName(dash.getBoundKeyTranslationKey());
            };

            CooldownType.Category category = type.getCategory();

            boolean isSpec = category == CooldownType.Category.SPEC;
            boolean isUniversal = category == CooldownType.Category.UNIVERSAL;
            float defaultAlpha = 0.65f;
            int xOffset = 0;

            String finalText = keyBindText + " - " + getDecimalFormat().format(MathHelper.clamp(cooldown, 0.0, 9999.0)) + "s";

            if (category == CooldownType.Category.STAND || isSpec) {
                if (!isSpec) finalText = "s." + finalText;

                if ((isSpec && standOn) || (!isSpec && !standOn)) {
                    xOffset = 48;
                    defaultAlpha = 0.3f;
                }
            }

            int offsetIndex = i;
            if (isSpec)
                offsetIndex -= 7;
            else if (isUniversal)
                offsetIndex -= 6;

            float offsetY = selectedY * 1.25f + 9f * offsetIndex;

            //RenderSystem.setShaderTexture(0, BIND_BG);
            //DrawableHelper.drawTexture(matrixStack, maxX + xOffset + 6, (int) offsetY - 2, 0, 0, 10, 10, 10, 10);
            textRenderer.drawWithShadow(
                    matrixStack,
                    finalText,
                    selectedX + xOffset,
                    offsetY,
                    ColorUtils.HSBAtoRGBA(0.3f - (float) cooldown * 10f / 720f, (cooldown < 1.6) ? 0.0f : 1.0f, 1.0f, (cooldown < 1.6) ? 1.0f : defaultAlpha),
                    true
            );
        }
        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }

    private void tickClient(MinecraftClient minecraftClient) {
        ClientPlayerEntity player = minecraftClient.player;
        if (player == null) return;

        if (minecraftClient.isPaused() && minecraftClient.isInSingleplayer()) return;

        // Timestop handling (nearly identical to serverside, but toStop is obtained in user.world instead of server world)
        ArrayList<DimValues> newActiveTimestops = new ArrayList<>();

        for (DimValues timestop : activeTimestops) {
            LivingEntity user = timestop.user;
            //JCraft.LOGGER.info("CLIENT: Ticking timestop " + timestop + " with user " + user + " and duration " + timestop.timer);

            if (user != null && user.isAlive() && timestop.timer-- > 0) {
                Vec3d pos = timestop.pos;

                List<? extends Entity> toStop = user.world.getEntitiesByClass(Entity.class,
                        new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toStop)
                    if (!entity.hasVehicle() && entity != user && entity != JUtils.getStand(user) && entity != user.getVehicle())
                        JComponents.getTimeStopData(entity).setTicks(2);

                newActiveTimestops.add(timestop);
            }
        }

        activeTimestops = newActiveTimestops;

        // Handle JCraft inputs (stand, spec, universal controls)
        GameOptions go = minecraftClient.options;

        StandEntity<?, ?> stand = JComponents.getStandData(player).getStand();
        boolean standOn = stand != null;

        //todo: reformat this into 2 more packets (stand block packet, attack packet)
        if (player.isAlive()) { // Send movement inputs to server
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(go.forwardKey.isPressed()); // W
            buf.writeBoolean(go.leftKey.isPressed()); // A
            buf.writeBoolean(go.backKey.isPressed()); // S
            buf.writeBoolean(go.rightKey.isPressed()); // D
            buf.writeBoolean(go.jumpKey.isPressed()); // Space
            buf.writeBoolean(dash.isPressed()); // Dash
            ClientPlayNetworking.send(InputSyncPacket.ID, buf);
        }

        // Block (3)
        if (standOn) {
            PacketByteBuf buf = PacketByteBufs.create();
            boolean rmb = go.useKey.wasPressed() || go.useKey.isPressed();
            buf.writeShort(3);
            buf.writeBoolean(rmb);
            sendStandControlPacket(buf);
        }

        // (De)summon (1)
        if (standSummon.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(1);
            sendStandControlPacket(buf);
        }
        // Light attack (2)
        if (go.attackKey.isPressed()) { // wasPressed() simply doesn't work
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(2);
            sendStandControlPacket(buf);
        }
        // Heavy (4)
        if (heavyKey.isPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(4);
            sendStandControlPacket(buf);
        }
        // Barrage (5)
        if (barrageKey.isPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(5);
            sendStandControlPacket(buf);
        }
        // Special 1 (6)
        if (special1Key.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(6);
            sendStandControlPacket(buf);
        }
        // Ult (7)
        if (ultKey.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(7);
            sendStandControlPacket(buf);
        }
        // Special 2 (8)
        if (special2Key.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(8);
            sendStandControlPacket(buf);
        }
        // Special 3 (9)
        if (special3Key.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(9);
            sendStandControlPacket(buf);
        }
        // Utility (10)
        if (utility.isPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(10);
            /*
            if (standOn) {
                if (stand.allowUtilityUse())
                    sendStandControlPacket(buf);
                else
                    stand.initClientUtility();
            } else {
             */
            sendStandControlPacket(buf);
            //}
        }
        // Cooldown Cancel (13)
        if (cooldownCancel.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(13);
            sendStandControlPacket(buf);
        }
    }

    private void sendStandControlPacket(PacketByteBuf buf) {
        ClientPlayNetworking.send(StandControlPacket.ID, buf);
    }

    /**
     * @return cleaned up version of TranslatableText name of button
     */
    private String generateName(String str) {
        String[] components = str.split("\\.");
        String last = components[components.length - 1];
        String secondLast = components[components.length - 2] + " ";
        if (components[components.length - 2].equals("keyboard")) secondLast = "";
        return StringUtils.capitalize(secondLast) + StringUtils.capitalize(last);
    }

    @Nullable
    public static StandEntity<?, ?> getStandEntity() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;

        return player.getPassengerList().stream()
                .filter(e -> e instanceof StandEntity)
                .map(e -> (StandEntity<?, ?>) e)
                .findFirst()
                .orElse(null);
    }
}
