package net.arna.jcraft;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.midnightdust.lib.config.MidnightConfig;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.hud.JCraftHudOverlay;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.client.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.client.network.s2c.ShaderDeactivationPacket;
import net.arna.jcraft.client.particle.ComboBreakerParticle;
import net.arna.jcraft.client.particle.CooldownCancelParticle;
import net.arna.jcraft.client.particle.HitsparkParticle;
import net.arna.jcraft.client.particle.KCParticle;
import net.arna.jcraft.client.renderer.block.ShaderTestBlockEntityRenderer;
import net.arna.jcraft.client.renderer.item.BigItemRenderer;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.network.c2s.StandControlPacket;
import net.arna.jcraft.common.util.ColorUtils;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static net.arna.jcraft.JCraft.MOD_ID;

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

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(MOD_ID, JClientConfig.class);

        //Rendering
        JRenderLayerRegistry.init();
        RenderHandler.init();
        JEventsRegister.registerClientEvents();
        ZaWarudoShaderHandler.INSTANCE.init();

        // Particle registration
        ParticleFactoryRegistry.getInstance().register(JParticleTypeRegistry.COMBO_BREAK, ComboBreakerParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(JParticleTypeRegistry.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(JParticleTypeRegistry.HITSPARK_1, provider -> new HitsparkParticle.Factory(provider, 0.5f));
        ParticleFactoryRegistry.getInstance().register(JParticleTypeRegistry.HITSPARK_2, provider -> new HitsparkParticle.Factory(provider, 1f));
        ParticleFactoryRegistry.getInstance().register(JParticleTypeRegistry.KCPARTICLE, KCParticle.Factory::new);

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

        ClientTickEvents.END_CLIENT_TICK.register(this::tickClient);
        ClientTickEvents.END_WORLD_TICK.register(new SkyBoxManager());
        ClientPlayNetworking.registerGlobalReceiver(ServerChannelFeedbackPacket.ID, ServerChannelFeedbackPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(ShaderActivationPacket.ID, ShaderActivationPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(ShaderDeactivationPacket.ID, ShaderDeactivationPacket::handle);

        HudRenderCallback.EVENT.register(new JCraftHudOverlay());
        HudRenderCallback.EVENT.register(this::renderHud);

        Identifier itemId = JObjectRegistry.ITEMS.get(JObjectRegistry.DEBUG_WAND);
        BigItemRenderer itemRenderer = new BigItemRenderer(itemId);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(itemRenderer);
        BuiltinItemRendererRegistry.INSTANCE.register(JObjectRegistry.DEBUG_WAND, itemRenderer);
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            out.accept(new ModelIdentifier(itemId + "_gui", "inventory"));
            out.accept(new ModelIdentifier(itemId + "_handheld", "inventory"));
        });
    }

    private void renderHud(MatrixStack matrixStack, float v) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ticksSinceCounted += 1;
        int maxX = (int) (client.getWindow().getScaledWidth() * 0.75);
        int maxY = (int) (client.getWindow().getScaledHeight() * 0.85);
        int midX = (client.getWindow().getScaledWidth() / 2);
        int midY = (client.getWindow().getScaledHeight() / 2);

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
                    maxX + (ticksSinceCounted < 5 ? player.getRandom().nextFloat() * 5f : 0),
                    midY * (1.15f) + (ticksSinceCounted < 5 ? player.getRandom().nextFloat() * 5f : 0),
                    ColorUtils.HSBAtoRGBA(comboCounter / 360f - 1f, 1f, 1f, 0.8f)
                    , true
            );
        }

        boolean standOn = player.getFirstPassenger() instanceof StandEntity;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Cooldown rendering
        for (Double cooldown : clientCooldowns) {
            i++;
            if (cooldown != 0) {

                String keyBindText = "unknown";
                switch (i) {
                    case (1):
                        keyBindText = "M1";
                        break;
                    case (11):
                    case (2):
                        keyBindText = GenerateName(heavyKey.getBoundKeyTranslationKey());
                        break;
                    case (12):
                    case (3):
                        keyBindText = GenerateName(barrageKey.getBoundKeyTranslationKey());
                        break;
                    case (13):
                    case (4):
                        keyBindText = GenerateName(ultKey.getBoundKeyTranslationKey());
                        break;
                    case (14):
                    case (5):
                        keyBindText = GenerateName(special1Key.getBoundKeyTranslationKey());
                        break;
                    case (15):
                    case (6):
                        keyBindText = GenerateName(special2Key.getBoundKeyTranslationKey());
                        break;
                    case (16):
                    case (7):
                        keyBindText = GenerateName(special3Key.getBoundKeyTranslationKey());
                        break;

                    case (8):
                        keyBindText = GenerateName(utility.getBoundKeyTranslationKey());
                        break;
                    case (9):
                        keyBindText = GenerateName(comboBreaker.getBoundKeyTranslationKey());
                        break;
                    case (10):
                        keyBindText = GenerateName(cooldownCancel.getBoundKeyTranslationKey());
                        break;
                }

                boolean isSpec = i > 10;
                float defaultAlpha = 0.65f;
                int xOffset = 0;

                String finalText = keyBindText + " - " + MathHelper.clamp(cooldown, 0.0, 9999.0) + "s";

                if (i < 8 || isSpec) {
                    if (!isSpec) {
                        finalText = "s." + finalText;
                    }

                    if ((isSpec && standOn) || (!isSpec && !standOn)) {
                        xOffset = 48;
                        defaultAlpha = 0.3f;
                    }
                }

                float offsetY = midY * (1.25f) + (isSpec ? 9 * (i - 9) : 9 * i);

                        /*
                        RenderSystem.setShaderTexture(0, BIND_BG);
                        DrawableHelper.drawTexture(matrixStack, maxX + xOffset + 6, (int) offsetY - 2, 0, 0, 10, 10, 10, 10);
                         */

                textRenderer.drawWithShadow(
                        matrixStack,
                        finalText,
                        maxX + xOffset,
                        offsetY,
                        ColorUtils.HSBAtoRGBA(0.3f - (float) (double) cooldown * 10f / 720f, (cooldown < 1.6) ? 0.0f : 1.0f, 1.0f, (cooldown < 1.6) ? 1.0f : defaultAlpha)
                        , true
                );
            }
        }
        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }

    private void tickClient(MinecraftClient minecraftClient) {
        GameOptions go = MinecraftClient.getInstance().options;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (go != null && player != null) {
            if (player.isAlive()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(0);
                buf.writeBoolean(go.forwardKey.isPressed()); // W
                buf.writeBoolean(go.leftKey.isPressed()); // A
                buf.writeBoolean(go.backKey.isPressed()); // S
                buf.writeBoolean(go.rightKey.isPressed()); // D
                buf.writeBoolean(go.jumpKey.isPressed()); // Space
                StandControlPacket.send(buf);
            } else {
                clientCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);
            }

            if (player.getFirstPassenger() instanceof StandEntity) {
                // Block (3)
                PacketByteBuf buf = PacketByteBufs.create();
                boolean rmb = go.useKey.wasPressed() || go.useKey.isPressed();
                buf.writeShort(3);
                buf.writeBoolean(rmb);
                StandControlPacket.send(buf);
            }

            // Light attack (2)
            if (go.attackKey.isPressed()) { // wasPressed() simply doesn't work
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(2);
                StandControlPacket.send(buf);
            }
            // Middle Click (10)
            if (utility.isPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(10);
                StandControlPacket.send(buf);
            }
        }
        // (De)summon (1)
        if (standSummon.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(1);
            StandControlPacket.send(buf);
        }
        // Heavy (4)
        if (heavyKey.isPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(4);
            StandControlPacket.send(buf);
        }
        // Barrage (5)
        if (barrageKey.isPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(5);
            StandControlPacket.send(buf);
        }
        // Special 1 (6)
        if (special1Key.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(6);
            StandControlPacket.send(buf);
        }
        // Ult (7)
        if (ultKey.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(7);
            StandControlPacket.send(buf);
        }
        // Special 2 (8)
        if (special2Key.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(8);
            StandControlPacket.send(buf);
        }
        // Special 3 (9)
        if (special3Key.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(9);
            StandControlPacket.send(buf);
        }
        // Combo Breaker (11)
        if (comboBreaker.isPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(11);
            StandControlPacket.send(buf);
        }
        // Cooldown Cancel (13)
        if (cooldownCancel.wasPressed()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(13);
            StandControlPacket.send(buf);
        }
    }

    private String GenerateName(String str) {
        String[] components = str.split("\\.");
        String last = components[components.length - 1];
        String secondLast = components[components.length - 2] + " ";
        if (components[components.length - 2].equals("keyboard")) {
            secondLast = "";
        }

        return StringUtils.capitalize(secondLast) + StringUtils.capitalize(last);
    }

    /*
    public static class EntityPacketOnClient {
        @Environment(EnvType.CLIENT)
        public static void onPacket(PacketContext context, PacketByteBuf byteBuf) {
            EntityType<?> type = Registries.ENTITY_TYPE.get(byteBuf.readVarInt());
            UUID entityUUID = byteBuf.readUuid();
            int entityID = byteBuf.readVarInt();
            double x = byteBuf.readDouble();
            double y = byteBuf.readDouble();
            double z = byteBuf.readDouble();
            float pitch = (byteBuf.readByte() * 360) / 256.0F;
            float yaw = (byteBuf.readByte() * 360) / 256.0F;
            context.getTaskQueue().execute(() -> {
                @SuppressWarnings("resource")
                ClientWorld world = MinecraftClient.getInstance().world;
                Entity entity = type.create(world);
                if (entity != null) {
                    entity.updatePosition(x, y, z);
                    entity.updateTrackedPosition(x, y, z);
                    entity.setPitch(pitch);
                    entity.setYaw(yaw);
                    entity.setId(entityID);
                    entity.setUuid(entityUUID);
                    world.addEntity(entityID, entity);
                }
            });
        }
    }

    public static class EntityPacket {
        public static final Identifier ID = new Identifier(JCraft.MOD_ID, "spawn_entity");

        public static Packet<ClientPlayPacketListener> createPacket(Entity entity) {
            PacketByteBuf buf = createBuffer();
            buf.writeVarInt(Registries.ENTITY_TYPE.getRawId(entity.getType()));
            buf.writeUuid(entity.getUuid());
            buf.writeVarInt(entity.getId());
            buf.writeDouble(entity.getX());
            buf.writeDouble(entity.getY());
            buf.writeDouble(entity.getZ());
            buf.writeByte(MathHelper.floor(entity.getPitch() * 256.0F / 360.0F));
            buf.writeByte(MathHelper.floor(entity.getYaw() * 256.0F / 360.0F));
            buf.writeFloat(entity.getPitch());
            buf.writeFloat(entity.getYaw());
            return ServerPlayNetworking.createS2CPacket(ID, buf);
        }

        private static PacketByteBuf createBuffer() {
            return new PacketByteBuf(Unpooled.buffer());
        }
    }
     */
}