package net.arna.jcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.hud.EpitaphOverlay;
import net.arna.jcraft.client.hud.JCraftAbilityHud;
import net.arna.jcraft.client.hud.JCraftHudOverlay;
import net.arna.jcraft.client.net.ClientPacketHandler;
import net.arna.jcraft.client.particle.*;
import net.arna.jcraft.client.registry.JClientEventsRegistry;
import net.arna.jcraft.client.util.ClientEntityHandlerImpl;
import net.arna.jcraft.common.JConfig;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.ShaderDeactivationPacket;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.client.registry.JArmorRendererRegister;
import net.arna.jcraft.client.registry.JEntityRendererRegister;
import net.arna.jcraft.client.registry.JRenderLayerRegistry;
import net.arna.jcraft.client.renderer.block.ShaderTestBlockEntityRenderer;
import net.arna.jcraft.client.renderer.item.BigItemRenderer;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.network.c2s.StandControlPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.*;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.client.util.JClientUtils.activeTimestops;

public class JCraftClient implements ClientModInitializer {

    private final List<String> comboRemarks = List.of("admin rdm!!!", "baby combo", "caught lackin", "kinda ez", "skill issue", "cancelled on twitter", "sent to bulgaria", "down bad");
    public static DefaultedList<Double> clientCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);
    public static int comboCounter = 0;
    public static int ticksSinceCounted = 0;

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

    @Override
    public void onInitializeClient() {
        JCraft.setClientEntityHandler(ClientEntityHandlerImpl.INSTANCE);

        //Rendering
        JRenderLayerRegistry.init();
        RenderHandler.init();
        JClientEventsRegistry.registerClientEvents();
        ZaWarudoShaderHandler.INSTANCE.init();
        CrimsonShaderHandler.INSTANCE.init();

        // Particle registration
        ParticleFactoryRegistry particleFactoryRegistry = ParticleFactoryRegistry.getInstance();
        particleFactoryRegistry.register(JParticleTypeRegistry.COMBO_BREAK, ComboBreakerParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_1, provider -> new HitsparkParticle.Factory(provider, 0.5f));
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_2, provider -> new HitsparkParticle.Factory(provider, 1f));
        particleFactoryRegistry.register(JParticleTypeRegistry.KCPARTICLE, KCParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BACKSTAB, BackstabParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.SPEEDPARTICLE, SpeedParticle.Factory::new);

        // Renderer registration
        JEntityRendererRegister.registerEntityRenderers();
        JArmorRendererRegister.registerArmorRenderers();
        BlockEntityRendererFactories.register(JBlockEntityTypeRegistry.SHADER_TEST_BLOCK_ENTITY, ShaderTestBlockEntityRenderer::new);

        // Keybind registration
        standSummon = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.standsummon", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.category.jcraft"));
        heavyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.heavy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category.jcraft"));
        barrageKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.barrage", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.category.jcraft"));
        ultKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.ult", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category.jcraft"));
        special1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.special1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.jcraft"));
        special2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.special2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.category.jcraft"));
        special3Key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.special3", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.category.jcraft"));
        comboBreaker = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.combobreaker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.category.jcraft"));
        cooldownCancel = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.cooldowncancel", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, "key.category.jcraft"));
        utility = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.utility", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, "key.category.jcraft"));
        dash = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jcraft.dash", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, "key.category.jcraft"));

        ClientTickEvents.END_CLIENT_TICK.register(this::tickClient);
        ClientTickEvents.END_WORLD_TICK.register(new SkyBoxManager());
        ClientTickEvents.END_CLIENT_TICK.register(new JCraftAbilityHud());

        ClientPlayNetworking.registerGlobalReceiver(ServerChannelFeedbackPacket.ID, (client, handler, buf, sender) -> ClientPacketHandler.handleChannelFeedback(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ShaderActivationPacket.ID, (client, handler, buf, sender) -> ClientPacketHandler.handleShaderActivation(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ShaderDeactivationPacket.ID, (client, handler, buf, sender) -> ClientPacketHandler.handleShaderDeactivation(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(TimeAccelStatePacket.ID, (client, handler, buf, sender) -> ClientPacketHandler.handleTimeAccelState(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(JCraft.id("epitaph_state"), (client, handler, buf, responseSender) -> ClientPacketHandler.handleEpitaphOverlayState(buf));

        HudRenderCallback.EVENT.register(new JCraftHudOverlay());
        HudRenderCallback.EVENT.register(this::renderHud);
        HudRenderCallback.EVENT.register(new JCraftAbilityHud());
//        HudRenderCallback.EVENT.register((matrices, tickDelta) -> EpitaphOverlay.render(matrices));

        // Run when the MinecraftClient instance is fully initialized.
        MinecraftClient.getInstance().submit(EpitaphOverlay::preload);

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
        switch (JConfig.UI_POSITION) {
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
    private void renderHud(MatrixStack matrixStack, float v) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ticksSinceCounted++;

        int selectedX = getHudX(client.getWindow().getScaledWidth());
        int selectedY = client.getWindow().getScaledHeight();

        boolean useIcons = JConfig.ICON_HUD;

        switch (JConfig.UI_POSITION) {
            case LEFT -> selectedY /= 20f;
            case MIDDLE -> selectedY /= 3f;
            case RIGHT -> selectedY /= 2.25f;
        }

        int i = 0;
        TextRenderer textRenderer = client.inGameHud.getTextRenderer();
        if (player.world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && comboCounter > 0) {

            String remark = "epic tod free download";
            if (comboCounter < comboRemarks.size() * 7) {
                remark = comboRemarks.get(Math.floorDiv(comboCounter, 7));
            }

            // Combo Counter rendering
            textRenderer.drawWithShadow(
                    matrixStack,
                    remark + " - " + comboCounter,
                    selectedX + (ticksSinceCounted < 5 ? player.getRandom().nextFloat() * 5f : 0) +
                            ( (JConfig.UI_POSITION == JConfig.UIPos.MIDDLE && useIcons) ? 54f : 0 ),
                    selectedY * (1.15f) + (ticksSinceCounted < 5 ? player.getRandom().nextFloat() * 5f : 0),
                    ColorUtils.HSBAtoRGBA(comboCounter / 360f - 1f, 1f, 1f, 0.8f)
                    , true
            );
        }

        // Cooldown rendering, for icon hud see JCraftHudOverlay
        if (useIcons) return;
        boolean standOn = ((IEntityDataSaver)player).getStand() != null;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        for (Double cooldown : clientCooldowns) {
            i++;
            if (cooldown != 0) {

                String keyBindText = switch (i) {
                    case (1) -> "M1";
                    case (12), (2) -> generateName(heavyKey.getBoundKeyTranslationKey());
                    case (13), (3) -> generateName(barrageKey.getBoundKeyTranslationKey());
                    case (14), (4) -> generateName(ultKey.getBoundKeyTranslationKey());
                    case (15), (5) -> generateName(special1Key.getBoundKeyTranslationKey());
                    case (16), (6) -> generateName(special2Key.getBoundKeyTranslationKey());
                    case (17), (7) -> generateName(special3Key.getBoundKeyTranslationKey());
                    case (8) -> generateName(utility.getBoundKeyTranslationKey());
                    case (9) -> generateName(comboBreaker.getBoundKeyTranslationKey());
                    case (10) -> generateName(cooldownCancel.getBoundKeyTranslationKey());
                    case (11) -> generateName(dash.getBoundKeyTranslationKey());
                    default -> "unknown";
                };

                boolean isSpec = i > 11;
                float defaultAlpha = 0.65f;
                int xOffset = 0;

                String finalText = keyBindText + " - " + MathHelper.clamp(cooldown, 0.0, 9999.0) + "s";

                if (i < 8 || isSpec) {
                    if (!isSpec) finalText = "s." + finalText;

                    if ((isSpec && standOn) || (!isSpec && !standOn)) {
                        xOffset = 48;
                        defaultAlpha = 0.3f;
                    }
                }

                float offsetY = selectedY * (1.25f) + (isSpec ? 9 * (i - 9) : 9 * i);
                //RenderSystem.setShaderTexture(0, BIND_BG);
                //DrawableHelper.drawTexture(matrixStack, maxX + xOffset + 6, (int) offsetY - 2, 0, 0, 10, 10, 10, 10);
                textRenderer.drawWithShadow(
                        matrixStack,
                        finalText,
                        selectedX + xOffset,
                        offsetY,
                        ColorUtils.HSBAtoRGBA(0.3f - (float) (double) cooldown * 10f / 720f, (cooldown < 1.6) ? 0.0f : 1.0f, 1.0f, (cooldown < 1.6) ? 1.0f : defaultAlpha)
                        , true
                );
            }
        }
        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }

    private void tickClient(MinecraftClient minecraftClient) {
        ClientPlayerEntity player = minecraftClient.player;

        if (minecraftClient.isPaused() && minecraftClient.isInSingleplayer()) return;

        // Timestop handling (nearly identical to serverside, but toStop is obtained in user.world instead of server world)
        ArrayList<DimValues> newActiveTimestops = new ArrayList<>();

        for (DimValues timestop : activeTimestops) {
            Entity user = timestop.user;
            //JCraft.LOGGER.info("CLIENT: Ticking timestop " + timestop + " with user " + user + " and duration " + timestop.timer);

            if (user != null && user.isAlive() && timestop.timer-- > 0) {
                Vec3d pos = timestop.pos;

                List<? extends Entity> toStop = user.world.getEntitiesByClass(Entity.class,
                        new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toStop) {
                    if ( entity == user || entity == ((IEntityDataSaver)user).getStand() ) continue;
                    ITimeStop ts = ((ITimeStop) entity);
                    ts.setTimeStopTicks(2);
                }

                newActiveTimestops.add(timestop);
            }
        }

        activeTimestops = newActiveTimestops;

        // Handle JCraft inputs (stand, spec, universal controls)
        if (player != null) {
            GameOptions go = minecraftClient.options;

            StandEntity stand = ((IEntityDataSaver)player).getStand();
            boolean standOn = stand != null;

            //todo: reformat this into 3 packets (input packet, stand block packet, attack packet)
            if (player.isAlive()) { // Send movement inputs to server
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(0);
                buf.writeBoolean(go.forwardKey.isPressed()); // W
                buf.writeBoolean(go.leftKey.isPressed()); // A
                buf.writeBoolean(go.backKey.isPressed()); // S
                buf.writeBoolean(go.rightKey.isPressed()); // D
                buf.writeBoolean(go.jumpKey.isPressed()); // Space
                buf.writeBoolean(dash.isPressed()); // Dash
                sendStandControlPacket(buf);
            } else { // Reset cooldowns on death
                clientCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);
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
            // Combo Breaker (11)
            if (comboBreaker.isPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(11);
                sendStandControlPacket(buf);
            }
            // Cooldown Cancel (13)
            if (cooldownCancel.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(13);
                sendStandControlPacket(buf);
            }
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

    public static boolean shouldNotRenderClone(PlayerCloneEntity clone) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && clone.getMasterId().equals(player.getUuid());
    }
}
