package net.arna.jcraft;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.arna.jcraft.client.hud.JCraftHudOverlay;
import net.arna.jcraft.entity.StandEntity;
import net.arna.jcraft.particle.ComboBreakerParticle;
import net.arna.jcraft.particle.CooldownCancelParticle;
import net.arna.jcraft.particle.HitsparkParticle;
import net.arna.jcraft.particle.KCParticle;
import net.arna.jcraft.registry.ModArmorRendererRegister;
import net.arna.jcraft.registry.ModEntityRendererRegister;
import net.arna.jcraft.util.IJCraftAnimatedPlayer;
import net.arna.jcraft.util.ITimeStop;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class JCraftClient implements ClientModInitializer {

    public static Shader timeeraseShader;
    private final List<String> comboRemarks = List.of("admin rdm!!!", "baby combo", "caught lackin", "kinda ez", "skill issue", "cancelled on twitter", "sent to bulgaria");
    private DefaultedList<Double> clientCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);
    private int comboCounter = 0;
    private int ticksSinceCounted = 0;
    public static final Map<Integer, DefaultParticleType> particles = Map.ofEntries(
            Map.entry(-1, ParticleTypes.FLASH),
            Map.entry(0, JCraft.COMBO_BREAK),
            Map.entry(1, JCraft.COOLDOWN_CANCEL),
            Map.entry(2, JCraft.HITSPARK_1),
            Map.entry(3, JCraft.HITSPARK_2)
    );

    // TODO: Render Layers
    /*
    public static final RenderLayer TIMEERASE_RENDER_LAYER = RenderLayerAccessor.invokeOf(
            "end_portal",
            VertexFormats.POSITION,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder().shader(
                    new RenderPhase.Shader(() -> timeeraseShader))
                    .texture(RenderPhase.Textures.create().add(new Identifier("jcraft:textures/environment/te_sky.png"), false, false)
                            .add(new Identifier("jcraft:textures/environment/te_swirl.png"), false, false).build()).build(false)
    );
     */

    public static int HSBAtoRGBA(float hue, float saturation, float brightness, float alpha) {
        int r = 0, g = 0, b = 0;
        int a = (int) (alpha * 255.0f + 0.5f);
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 1 -> {
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 2 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                }
                case 3 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 4 -> {
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                }
            }
        }
        return (a << 24) | (r << 16) | (g << 8) | b; // Bit shifting magic
    }

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
        // Particle registration
        ParticleFactoryRegistry.getInstance().register(JCraft.COMBO_BREAK, ComboBreakerParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(JCraft.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(JCraft.HITSPARK_1, provider ->  new HitsparkParticle.Factory(provider, 0.5f));
        ParticleFactoryRegistry.getInstance().register(JCraft.HITSPARK_2, provider ->  new HitsparkParticle.Factory(provider, 1f));
        ParticleFactoryRegistry.getInstance().register(JCraft.KCPARTICLE, KCParticle.Factory::new);

        // Renderer registration
        ModEntityRendererRegister.registerEntityRenderers();
        ModArmorRendererRegister.registerArmorRenderers();

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
                    ClientPlayNetworking.send(JCraft.standControlChannel, buf);
                } else {
                    clientCooldowns = DefaultedList.ofSize(JCraft.cooldowns.size(), 0.0);
                }

                if (player.getFirstPassenger() instanceof StandEntity) {
                    // Block (3)
                    boolean rmb = go.useKey.wasPressed() || go.useKey.isPressed();
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeShort(3);
                    buf.writeBoolean(rmb);
                    ClientPlayNetworking.send(JCraft.standControlChannel, buf);
                }

                // Light attack (2)
                if (go.attackKey.isPressed()) { // wasPressed() simply doesn't work
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeShort(2);
                    ClientPlayNetworking.send(JCraft.standControlChannel, buf);
                }
                // Middle Click (10)
                if (utility.isPressed()) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeShort(10);
                    ClientPlayNetworking.send(JCraft.standControlChannel, buf);
                }
            }
            // (De)summon (1)
            if (standSummon.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(1);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Heavy (4)
            if (heavyKey.isPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(4);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Barrage (5)
            if (barrageKey.isPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(5);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Special 1 (6)
            if (special1Key.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(6);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Ult (7)
            if (ultKey.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(7);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Special 2 (8)
            if (special2Key.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(8);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Special 3 (9)
            if (special3Key.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(9);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Combo Breaker (11)
            if (comboBreaker.isPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(11);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
            // Cooldown Cancel (13)
            if (cooldownCancel.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(13);
                ClientPlayNetworking.send(JCraft.standControlChannel, buf);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(JCraft.serverFeedbackChannel, (client, handler, buf, responseSender) -> {
            short control = buf.readShort();

            // Show hitboxes gamerule
            switch (control) {
                case (1) -> {
                    double v1x = buf.readDouble();
                    double v2x = buf.readDouble();

                    double v1y = buf.readDouble();
                    double v2y = buf.readDouble();

                    double v1z = buf.readDouble();
                    double v2z = buf.readDouble();

                    client.execute(() -> {
                        Random random = new Random();
                        for (int h = 0; h < 128; ++h) {
                            client.world.addParticle(
                                    ParticleTypes.WAX_ON,
                                    v1x,
                                    random.nextDouble(v1y, v2y),
                                    random.nextDouble(v1z, v2z),
                                    0.0, 0.0, 0.0);
                        }
                        for (int h = 0; h < 128; ++h) {
                            client.world.addParticle(
                                    ParticleTypes.WAX_ON,
                                    v2x,
                                    random.nextDouble(v1y, v2y),
                                    random.nextDouble(v1z, v2z),
                                    0.0, 0.0, 0.0);
                        }
                        for (int h = 0; h < 128; ++h) {
                            client.world.addParticle(
                                    ParticleTypes.WAX_ON,
                                    random.nextDouble(v1x, v2x),
                                    v1y,
                                    random.nextDouble(v1z, v2z),
                                    0.0, 0.0, 0.0);
                        }
                        for (int h = 0; h < 128; ++h) {
                            client.world.addParticle(
                                    ParticleTypes.WAX_ON,
                                    random.nextDouble(v1x, v2x),
                                    v2y,
                                    random.nextDouble(v1z, v2z),
                                    0.0, 0.0, 0.0);
                        }
                        for (int h = 0; h < 128; ++h) {
                            client.world.addParticle(
                                    ParticleTypes.WAX_ON,
                                    random.nextDouble(v1x, v2x),
                                    random.nextDouble(v1y, v2y),
                                    v1z,
                                    0.0, 0.0, 0.0);
                        }
                        for (int h = 0; h < 128; ++h) {
                            client.world.addParticle(
                                    ParticleTypes.WAX_ON,
                                    random.nextDouble(v1x, v2x),
                                    random.nextDouble(v1y, v2y),
                                    v2z,
                                    0.0, 0.0, 0.0);
                        }
                    });
                }

                // Time erase trackers
                case (2) -> {
                    Random random = new Random();
                    double posX = buf.readDouble();
                    double posY = buf.readDouble();
                    double posZ = buf.readDouble();

                    double sizeX = MathHelper.clamp(buf.readDouble(), 0.1, 100);
                    double sizeY = MathHelper.clamp(buf.readDouble(), 0.1, 100);
                    double sizeZ = MathHelper.clamp(buf.readDouble(), 0.1, 100);

                    for (int h = 0; h < 8; ++h) {
                        client.world.addParticle(
                                JCraft.KCPARTICLE,
                                posX + random.nextDouble(sizeX) - sizeX / 2,
                                posY + random.nextDouble(sizeY),
                                posZ + random.nextDouble(sizeZ) - sizeZ / 2,
                                0.0, 0.0, 0.0
                        );
                    }
                }

                // Cooldown tracking
                case (3) -> {
                    int index = buf.readInt(); // Doesn't start at 0
                    double cd = buf.readDouble();
                    clientCooldowns.set(index - 1, cd);
                }

                // KQ bomb tracker
                case (4) -> {
                    Random random = new Random();
                    double v1x = buf.readDouble();
                    double v1y = buf.readDouble();
                    double v1z = buf.readDouble();

                    double v2x = buf.readDouble();
                    double v2y = buf.readDouble();
                    double v2z = buf.readDouble();

                    boolean inRange = buf.readBoolean();

                    Vec3d mid = new Vec3d(v1x, v1y + v2y / 2, v1z);

                    client.execute(() -> {
                        for (int h = 0; h < 16; ++h) {
                            double x = v1x + random.nextDouble(v2x) - v2x / 2;
                            double y = v1y + random.nextDouble(v2y);
                            double z = v1z + random.nextDouble(v2z) - v2z / 2;
                            Vec3d towardsVector = mid.subtract(x, y, z).normalize().multiply(0.1);

                            client.world.addParticle(
                                    inRange ? ParticleTypes.WAX_ON : ParticleTypes.WITCH,
                                    x, y, z,
                                    towardsVector.x, towardsVector.y, towardsVector.z);
                        }
                    });
                }

                // WS acid spew
                case (5) -> {
                    Random random = new Random();
                    double epx = buf.readDouble();
                    double epy = buf.readDouble();
                    double epz = buf.readDouble();

                    double hpx = buf.readDouble();
                    double hpy = buf.readDouble();
                    double hpz = buf.readDouble();

                    client.execute(() -> {
                        for (int h = 0; h < 256; ++h) {
                            double x = hpx + random.nextDouble(2) - 1;
                            double y = hpy + random.nextDouble(2) - 1;
                            double z = hpz + random.nextDouble(2) - 1;
                            Vec3d awayVector = new Vec3d(x, y, z).subtract(epx, epy + 0.5, epz).normalize().multiply(0.3);

                            client.world.addParticle(
                                    ParticleTypes.SPIT,
                                    x, y, z,
                                    awayVector.x, awayVector.y, awayVector.z);
                        }
                    });
                }

                // Combo counter
                case (6) -> {
                    comboCounter = buf.readInt();
                    ticksSinceCounted = 0;
                }

                // Anubis swings
                case (7) -> {
                    int state = buf.readInt();
                    int entID = buf.readInt();

                    client.execute(() -> {
                        Entity ent = client.world.getEntityById(entID);
                        if (ent instanceof PlayerEntity player) {
                            String arm = (player.getMainArm() == Arm.RIGHT) ? "r" : "l";
                            var animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                            //ex. swing1L - standing thrust w/ left arm
                            var anim = PlayerAnimationRegistry.getAnimation(new Identifier(JCraft.MOD_ID, "animation.anubis.swing" + state + arm));
                            animationContainer.setAnimation(new KeyframeAnimationPlayer(anim));
                        }
                    });
                }

                // Generic single particle
                case (8) -> {
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    int id = buf.readInt();

                    client.execute(() -> client.world.addParticle(
                            particles.get(id), true,
                            x, y, z,
                            0, 0, 0));
                }

                // Crossfire hurricane
                case (10) -> {
                    Random random = new Random();
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();

                    client.execute(() -> {
                        for (int h = 0; h < 360; ++h) {
                            client.world.addParticle(
                                    random.nextInt(0, 5) > 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                                    x + Math.sin(h) * 4 + random.nextGaussian() * 2, y + random.nextGaussian() * 1.5, z + Math.cos(h) * 4 + random.nextGaussian() * 2,
                                    Math.sin(h + 1.57) / 4, 0, Math.cos(h + 1.57) / 4);
                        }
                    });
                }

                // Fool Dust Cloud
                case (11) -> {
                    Random random = new Random();
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();

                    client.execute(() -> {
                        for (int h = 0; h < 256; ++h) {
                            client.world.addParticle(
                                    new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState()),
                                    x + random.nextGaussian() * 2,
                                    y + random.nextGaussian() * 2,
                                    z + random.nextGaussian() * 2,
                                    0, 0, 0);
                        }
                    });
                }

                // Spec Animations
                case (12) -> {
                    int entID = buf.readInt();
                    String animPath = buf.readString(); // i know exactly how unoptimized this is but i fail to care

                    client.execute(() -> {
                        Entity ent = client.world.getEntityById(entID);
                        if (ent instanceof PlayerEntity player) {
                            ModifierLayer<IAnimation> animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                            KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(new Identifier(JCraft.MOD_ID, "animation." + animPath));
                            //KeyframeAnimation.AnimationBuilder builder = anim.mutableCopy();
                            //builder.getPart("head")
                            animationContainer.setAnimation(new KeyframeAnimationPlayer(anim));
                        }
                    });
                }

                // Reset Player Animation
                case (13) -> {
                    int entID = buf.readInt();

                    client.execute(() -> {
                        Entity ent = client.world.getEntityById(entID);
                        if (ent instanceof PlayerEntity player) {
                            ModifierLayer<IAnimation> animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                            animationContainer.setAnimation(null);
                        }
                    });
                }

                // Clientside TS
                case (14) -> {
                    int entID = buf.readInt();
                    int ticks = buf.readInt();

                    client.execute(() -> {
                        Entity ent = client.world.getEntityById(entID);
                        if (ent != null) {
                            ((ITimeStop)ent).setTimeStopTicks(ticks);
                        }
                    });
                }
            }
        });

        HudRenderCallback.EVENT.register(new JCraftHudOverlay());

        HudRenderCallback.EVENT.register(new HudRenderCallback() {
            private static final Identifier BIND_BG = new Identifier(JCraft.MOD_ID, "textures/gui/bind_bg.png");
            @Override
            public void onHudRender(MatrixStack matrixStack, float tickDelta) {
                MinecraftClient client = MinecraftClient.getInstance();
                ClientPlayerEntity player = client.player;
                ticksSinceCounted += 1;
                int maxX = (int)(client.getWindow().getScaledWidth() * 0.75);
                int maxY = (int)(client.getWindow().getScaledHeight() * 0.85);
                int midX = (client.getWindow().getScaledWidth() / 2);
                int midY = (client.getWindow().getScaledHeight() / 2);

                int i = 0;
                TextRenderer textRenderer = client.inGameHud.getTextRenderer();
                if (player.world.getGameRules().getBoolean(JCraft.COMBO_COUNTER) && comboCounter > 0) {

                    String remark = "epic tod free download";
                    if (comboCounter < comboRemarks.size() * 7) { remark = comboRemarks.get(Math.floorDiv(comboCounter, 7)); }

                    // Combo Counter rendering
                    textRenderer.drawWithShadow(
                            matrixStack,
                            remark + " - " + comboCounter,
                            maxX + (ticksSinceCounted < 5 ? player.getRandom().nextFloat()*5f : 0),
                            midY * (1.15f) + (ticksSinceCounted < 5 ? player.getRandom().nextFloat()*5f : 0),
                            HSBAtoRGBA(comboCounter / 360f - 1f, 1f, 1f, 0.8f)
                            , true
                    );
                }

                boolean standOn = player.getFirstPassenger() instanceof StandEntity;

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1,1,1,1);

                // Cooldown rendering
                for (Double cooldown : clientCooldowns) {
                    i++;
                    if (cooldown != 0) {

                        String keyBindText = "unknown";
                        switch (i) {
                            case (1): keyBindText = "M1"; break;
                            case (11):
                            case (2): keyBindText = GenerateName(heavyKey.getBoundKeyTranslationKey()); break;
                            case (12):
                            case (3): keyBindText = GenerateName(barrageKey.getBoundKeyTranslationKey()); break;
                            case (13):
                            case (4): keyBindText = GenerateName(ultKey.getBoundKeyTranslationKey()); break;
                            case (14):
                            case (5): keyBindText = GenerateName(special1Key.getBoundKeyTranslationKey()); break;
                            case (15):
                            case (6): keyBindText = GenerateName(special2Key.getBoundKeyTranslationKey()); break;
                            case (16):
                            case (7): keyBindText = GenerateName(special3Key.getBoundKeyTranslationKey()); break;

                            case (8): keyBindText = GenerateName(utility.getBoundKeyTranslationKey()); break;
                            case (9): keyBindText = GenerateName(comboBreaker.getBoundKeyTranslationKey()); break;
                            case (10): keyBindText = GenerateName(cooldownCancel.getBoundKeyTranslationKey()); break;
                        }

                        boolean isSpec = i > 10;
                        float defaultAlpha = 0.65f;
                        int xOffset = 0;

                        String finalText = keyBindText + " - " + MathHelper.clamp(cooldown, 0.0, 9999.0) + "s";

                        if (i < 8 || isSpec) {
                            if (!isSpec) { finalText = "s." + finalText; }

                            if ((isSpec && standOn) || (!isSpec && !standOn)) {
                                xOffset = 48;
                                defaultAlpha = 0.3f;
                            }
                        }

                        float offsetY = midY * (1.25f) + (isSpec ? 9 * (i-9) : 9 * i);

                        /*
                        RenderSystem.setShaderTexture(0, BIND_BG);
                        DrawableHelper.drawTexture(matrixStack, maxX + xOffset + 6, (int) offsetY - 2, 0, 0, 10, 10, 10, 10);
                         */

                        textRenderer.drawWithShadow(
                                matrixStack,
                                finalText,
                                maxX + xOffset,
                                offsetY,
                                HSBAtoRGBA(0.3f - (float) (double) cooldown * 10f / 720f, (cooldown < 1.6) ? 0.0f : 1.0f, 1.0f, (cooldown < 1.6) ? 1.0f : defaultAlpha)
                                , true
                        );
                    }
                }
                RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
            }
        });
    }

    private String GenerateName(String str) {
        String[] components = str.split("\\.");
        String last = components[components.length-1];
        String secondLast = components[components.length-2] + " ";
        if (components[components.length-2].equals("keyboard")) { secondLast = ""; }

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