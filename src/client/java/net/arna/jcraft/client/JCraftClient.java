package net.arna.jcraft.client;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.gravity.util.GravityChannelClient;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.net.ClientPacketHandler;
import net.arna.jcraft.client.particle.*;
import net.arna.jcraft.client.registry.*;
import net.arna.jcraft.client.renderer.block.CoffinTileRenderer;
import net.arna.jcraft.client.renderer.block.ShaderTestBlockEntityRenderer;
import net.arna.jcraft.client.renderer.effects.*;
import net.arna.jcraft.client.renderer.item.BigItemRenderer;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.client.rendering.handler.*;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.client.util.ClientEntityHandlerImpl;
import net.arna.jcraft.client.util.TrackedKeyBinding;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.c2s.PlayerInputPacket;
import net.arna.jcraft.common.network.c2s.StandBlockPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JPacketRegistry;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static net.arna.jcraft.client.gui.hud.JCraftAbilityHud.getHudX;
import static net.arna.jcraft.client.util.JClientUtils.activeTimestops;

public class JCraftClient implements ClientModInitializer {
    // Keybinds
    private static TrackedKeyBinding standSummon, heavyKey, barrageKey, ultKey, special1Key, special2Key, special3Key,
            comboBreaker, cooldownCancel, utility, dash;
    @SuppressWarnings({"ConstantValue", "DataFlowIssue"}) // Not the case here cuz of the lazy getter.
    @Getter(lazy = true)
    private static final Map<TrackedKeyBinding, MoveInputType> bindings = ImmutableMap.<TrackedKeyBinding, MoveInputType>builder()
            .put(standSummon, MoveInputType.STAND_SUMMON)
            .put(TrackedKeyBinding.wrap(MinecraftClient.getInstance().options.attackKey), MoveInputType.LIGHT)
            .put(heavyKey, MoveInputType.HEAVY)
            .put(barrageKey, MoveInputType.BARRAGE)
            .put(special1Key, MoveInputType.SPECIAL1)
            .put(special2Key, MoveInputType.SPECIAL2)
            .put(special3Key, MoveInputType.SPECIAL3)
            .put(ultKey, MoveInputType.ULTIMATE)
            .put(utility, MoveInputType.UTILITY)
            .build();
    @Getter(lazy = true)
    private static final Map<TrackedKeyBinding, MovementInputType> movementBindings = createMovementBindingsMap();
    @Getter(lazy = true)
    private static final TrackedKeyBinding trackedUseKey = TrackedKeyBinding.wrap(MinecraftClient.getInstance().options.useKey);
    private static Supplier<DecimalFormat> decimalFormat = Suppliers.memoize(JCraftClient::createDecimalFormat);
    private static boolean comboStarted = false;
    private static int framesSinceComboStarted = 0;

    @Override
    public void onInitializeClient() {
        JCraft.setClientEntityHandler(ClientEntityHandlerImpl.INSTANCE);

        AutoConfig.register(JClientConfig.class, JanksonConfigSerializer::new);
        JClientConfig.load();

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new DecimalFormatUpdater());

        GravityChannelClient.init();

        // Rendering
        JRenderLayerRegistry.init();
        RenderHandler.init();
        JClientEventsRegistry.registerClientEvents();
        JModelPredicateProviderRegistry.register();

        InversionShaderHandler.INSTANCE.init();
        ZaWarudoShaderHandler.INSTANCE.init();
        CrimsonShaderHandler.INSTANCE.init();
        EpitaphVignetteShaderHandler.INSTANCE.init();
        UIShaderHandler.INSTANCE.init(); // Should be last

        // Particle registration
        ParticleFactoryRegistry particleFactoryRegistry = ParticleFactoryRegistry.getInstance();
        particleFactoryRegistry.register(JParticleTypeRegistry.COMBO_BREAK, ComboBreakerParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_1, provider -> new HitsparkParticle.Factory(provider, 0.4f, 5));
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_2, provider -> new HitsparkParticle.Factory(provider, 0.66f, 6));
        particleFactoryRegistry.register(JParticleTypeRegistry.HITSPARK_3, provider -> new HitsparkParticle.Factory(provider, 1f, 8));
        particleFactoryRegistry.register(JParticleTypeRegistry.KCPARTICLE, KCParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BACKSTAB, BackstabParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.SPEED_PARTICLE, SpeedParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BITES_THE_DUST, BitesTheDustParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BOOM_1, BoomParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.PIXEL, PixelParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.BLOCKSPARK, provider -> new BlocksparkParticle.Factory(provider, 0.15f));
        particleFactoryRegistry.register(JParticleTypeRegistry.GO, GoParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.AURA_ARC, AuraArcParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.AURA_BLOB, AuraBlobParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.INVERSION, InversionParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.SUN_LOCK_ON, BackstabParticle.Factory::new); // 9 frames, reusing
        particleFactoryRegistry.register(JParticleTypeRegistry.PURPLE_HAZE_CLOUD, PurpleHazeCloudParticle.Factory::new);
        particleFactoryRegistry.register(JParticleTypeRegistry.PURPLE_HAZE_PARTICLE, PurpleHazeErraticParticle.Factory::new);

        // Renderer registration
        JEntityRendererRegister.registerEntityRenderers();
        JArmorRendererRegistry.registerArmorRenderers();
        BlockEntityRendererFactories.register(JBlockEntityTypeRegistry.SHADER_TEST_BLOCK_ENTITY, ShaderTestBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(JBlockEntityTypeRegistry.COFFIN_TILE, CoffinTileRenderer::new);

        // This HAS to be registered before TrackingKeyBinding is initialized.
        ClientTickEvents.END_CLIENT_TICK.register(this::tickClient);
        ClientTickEvents.END_WORLD_TICK.register(new SkyBoxManager());

        // Keybinding registration
        standSummon = TrackedKeyBinding.createAndRegister("key.jcraft.standsummon", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.category.jcraft");
        heavyKey = TrackedKeyBinding.createAndRegister("key.jcraft.heavy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category.jcraft");
        barrageKey = TrackedKeyBinding.createAndRegister("key.jcraft.barrage", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.category.jcraft");
        ultKey = TrackedKeyBinding.createAndRegister("key.jcraft.ultimate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category.jcraft");
        special1Key = TrackedKeyBinding.createAndRegister("key.jcraft.special1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.jcraft");
        special2Key = TrackedKeyBinding.createAndRegister("key.jcraft.special2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.category.jcraft");
        special3Key = TrackedKeyBinding.createAndRegister("key.jcraft.special3", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.category.jcraft");
        //comboBreaker = TrackingKeyBinding.createAndRegister("key.jcraft.combobreaker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.category.jcraft");
        cooldownCancel = TrackedKeyBinding.createAndRegister("key.jcraft.cooldowncancel", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, "key.category.jcraft");
        utility = TrackedKeyBinding.createAndRegister("key.jcraft.utility", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, "key.category.jcraft");
        dash = TrackedKeyBinding.createAndRegister("key.jcraft.dash", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, "key.category.jcraft");

        ClientPacketHandler.init();

        AttackHitboxEffectRenderer.init();
        TimeAccelerationEffectRenderer.init();
        TimeErasePredictionEffectRenderer.init();
        SplatterEffectRenderer.init();
        ShockwaveEffectRenderer.init();

        HudRenderCallback.EVENT.register(this::renderHud);

        // Run when the MinecraftClient instance is fully initialized.
        MinecraftClient.getInstance().send(EpitaphOverlay::preload);

        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        Identifier itemId = JObjectRegistry.ITEMS.get(JObjectRegistry.DEBUG_WAND);
        BigItemRenderer itemRenderer = new BigItemRenderer(itemId);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(itemRenderer);
        BuiltinItemRendererRegistry.INSTANCE.register(JObjectRegistry.DEBUG_WAND, itemRenderer);

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            out.accept(new ModelIdentifier(new Identifier(itemId + "_gui"), "inventory"));
            out.accept(new ModelIdentifier(new Identifier(itemId + "_handheld"), "inventory"));
        });
    }

    /// TEXT HUD
    private final List<String> comboRemarks = List.of("admin rdm!!!", "baby combo", "caught lackin", "kinda ez", "skill issue", "cancelled on twitter", "sent to bulgaria", "down bad");
    public static int comboCounter = 0;
    public static float damageScaling = 1.00f;
    public static int framesSinceCounted = 0;

    private void renderHud(DrawContext ctx, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            JCraft.LOGGER.fatal("Attempted to render hud with no player!");
            return;
        }

        framesSinceCounted++;

        boolean isMid = JClientConfig.getInstance().getUiPosition() == JClientConfig.UIPos.MIDDLE;
        boolean useIcons = JClientConfig.getInstance().isIconHud();
        StandEntity<?, ?> stand = JUtils.getStand(player);

        TextRenderer textRenderer = client.inGameHud.getTextRenderer();

        int selectedX = getHudX(client.getWindow().getScaledWidth(), 128);
        int selectedY = client.getWindow().getScaledHeight();

        switch (JClientConfig.getInstance().getUiPosition()) {
            case LEFT -> selectedY /= 20;
            case MIDDLE -> selectedY /= 3;
            case RIGHT -> selectedY = (int) (selectedY / 2.25f);
        }

        // Draw text HUD
        if (!useIcons) {
            CooldownsComponent cooldowns = JComponents.getCooldowns(player);

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
                    case HEAVY, STAND_HEAVY -> generateName(heavyKey.getParent());
                    case BARRAGE, STAND_BARRAGE -> generateName(barrageKey.getParent());
                    case ULTIMATE, STAND_ULTIMATE -> generateName(ultKey.getParent());
                    case SPECIAL1, STAND_SP1 -> generateName(special1Key.getParent());
                    case SPECIAL2, STAND_SP2 -> generateName(special2Key.getParent());
                    case SPECIAL3, STAND_SP3 -> generateName(special3Key.getParent());
                    case UTILITY -> generateName(utility.getParent());
                    case COMBO_BREAKER -> "Combo Breaker";
                    case COOLDOWN_CANCEL -> generateName(cooldownCancel.getParent());
                    case DASH -> generateName(dash.getParent());
                };

                CooldownType.Category category = type.getCategory();

                boolean isSpec = category == CooldownType.Category.SPEC;
                boolean isUniversal = category == CooldownType.Category.UNIVERSAL;
                float defaultAlpha = 0.65f;
                int xOffset = 0;

                String finalText = keyBindText + " - " + decimalFormat.get().format(MathHelper.clamp(cooldown, 0.0, 9999.0)) + "s";

                if (category == CooldownType.Category.STAND || isSpec) {
                    if (!isSpec) finalText = "s." + finalText;

                    if ((isSpec && stand != null) || (!isSpec && stand == null)) {
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

                ctx.drawTextWithShadow(
                        textRenderer,
                        finalText,
                        selectedX + xOffset,
                        (int) offsetY,
                        ColorUtils.HSBAtoRGBA(0.3f - (float) cooldown * 10f / 720f, (cooldown < 1.6) ? 0.0f : 1.0f, 1.0f, (cooldown < 1.6) ? 1.0f : defaultAlpha)
                );

            }
        }

        // Draw Combo Counter
        if (comboCounter > 0 && player.getWorld().getGameRules().getBoolean(JCraft.COMBO_COUNTER) && framesSinceCounted <= 180) {
            String remark = "epic tod free download";
            if (comboCounter < comboRemarks.size() * 7)
                remark = comboRemarks.get(Math.floorDiv(comboCounter, 7));

            boolean recentHit = framesSinceCounted < 5;

            Random random = player.getRandom();

            if (comboStarted && ++framesSinceComboStarted > 59) comboStarted = false;

            ctx.drawTextWithShadow(
                    textRenderer,
                    comboCounter + " - (" + Math.round(damageScaling * 100f) + "%) - " + remark,
                    (int) (selectedX + (isMid && useIcons ? 54f : 0) + (recentHit ? tickDelta * random.nextFloat() * 5f : 0)),
                    (int) (selectedY * (1.15f) + (recentHit ? tickDelta * random.nextFloat() * 5f : 0)),
                    ColorUtils.HSBAtoRGBA(comboCounter / 360f - 1f, comboStarted ? framesSinceComboStarted / 60f : 1f, 1f, 0.8f)
            );
        }
    }

    public static void markComboStarted() {
        comboStarted = true;
        framesSinceComboStarted = 0;
    }

    private static Map<TrackedKeyBinding, MovementInputType> createMovementBindingsMap() {
        GameOptions options = MinecraftClient.getInstance().options;
        return ImmutableMap.<TrackedKeyBinding, MovementInputType>builder()
                .put(TrackedKeyBinding.wrap(options.forwardKey), MovementInputType.FORWARD)
                .put(TrackedKeyBinding.wrap(options.backKey), MovementInputType.BACKWARD)
                .put(TrackedKeyBinding.wrap(options.leftKey), MovementInputType.LEFT)
                .put(TrackedKeyBinding.wrap(options.rightKey), MovementInputType.RIGHT)
                .put(TrackedKeyBinding.wrap(options.jumpKey), MovementInputType.JUMP)
                .put(TrackedKeyBinding.wrap(options.sneakKey), MovementInputType.CROUCH)
                .put(dash, MovementInputType.DASH)
                .build();
    }

    /**
     * @return a cleaned-up version of TranslatableText name of button
     */
    private String generateName(KeyBinding keyBinding) {
        String str = keyBinding.getBoundKeyTranslationKey();
        String[] components = str.split("\\.");
        String last = components[components.length - 1];
        String secondLast = components[components.length - 2] + " ";
        if (components[components.length - 2].equals("keyboard")) secondLast = "";
        return StringUtils.capitalize(secondLast) + StringUtils.capitalize(last);
    }

    private void tickClient(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        StandEntity<?, ?> stand = JUtils.getStand(player);

        // Handle JCraft inputs (stand, spec, universal controls)
        // Regular input (all moves, regular Minecraft movement (WASD and jumping) and dashing)
        if (player.isAlive()) {
            Object2BooleanMap<MovementInputType> movementInput = getChangedInputs(getMovementBindings());
            Object2BooleanMap<MoveInputType> moveInput = getChangedInputs(getBindings());

            if (!movementInput.isEmpty() || !moveInput.isEmpty())
                ClientPlayNetworking.send(JPacketRegistry.C2S_PLAYER_INPUT, PlayerInputPacket.write(movementInput, moveInput));

            Object2BooleanMap<MoveInputType> heldMoves = new Object2BooleanOpenHashMap<>();
            getBindings().forEach((key, value) -> {
                if (key.isDown())
                    heldMoves.put(value, true);
            });

            if (!heldMoves.isEmpty())
                ClientPlayNetworking.send(JPacketRegistry.C2S_PLAYER_INPUT_HOLD, PlayerInputPacket.write(null, heldMoves));
        }

        // Block
        if (getTrackedUseKey().isChangedThisTick()) {
            boolean pressed = getTrackedUseKey().isPressedThisTick();
                ClientPlayNetworking.send(JPacketRegistry.C2S_STAND_BLOCK, StandBlockPacket.write(pressed));
            if (stand != null && stand.isRemoteAndControllable() && pressed)
                ClientPlayNetworking.send(JPacketRegistry.C2S_REMOTE_STAND_INTERACT, PacketByteBufs.create());
        }

        // Cooldown Cancel
        if (cooldownCancel.isPressedThisTick())
            ClientPlayNetworking.send(JPacketRegistry.C2S_COOLDOWN_CANCEL, PacketByteBufs.create());

        if (client.isPaused() && client.isInSingleplayer()) return;

        // Timestop handling (nearly identical to serverside, but toStop is obtained in user.world instead of server world)
        Iterator<DimensionData> iter = activeTimestops.iterator();

        while (iter.hasNext()) {
            DimensionData timestop = iter.next();
            LivingEntity user = timestop.user;

            if (user == null || !user.isAlive() || --timestop.timer <= 0) {
                iter.remove();
                continue;
            }

            Vec3d pos = timestop.pos;

            List<? extends Entity> toStop = user.getWorld().getEntitiesByClass(Entity.class,
                    new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

            for (Entity entity : toStop)
                if (!entity.hasVehicle() && entity != user && entity != JUtils.getStand(user) && entity != user.getVehicle())
                    JComponents.getTimeStopData(entity).setTicks(2);
        }
    }

    private static <E extends Enum<E>> Object2BooleanMap<E> getChangedInputs(Map<TrackedKeyBinding, E> bindings) {
        return bindings.entrySet().stream()
                .filter(entry -> entry.getKey().isChangedThisTick())
                .collect(Object2BooleanOpenHashMap::new, (map, entry) -> map.put(entry.getValue(), entry.getKey().isPressedThisTick()),
                        Object2BooleanMap::putAll);
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

    private static DecimalFormat createDecimalFormat() {
        return new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(
                Locale.forLanguageTag(MinecraftClient.getInstance().options.language)));
    }

    @Getter
    private static class DecimalFormatUpdater implements IdentifiableResourceReloadListener {
        private final Identifier fabricId = JCraft.id("decimal_format_updater");

        @Override
        public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler,
                                              Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
            return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() ->
                    decimalFormat = Suppliers.memoize(JCraftClient::createDecimalFormat), applyExecutor);
        }
    }
}
