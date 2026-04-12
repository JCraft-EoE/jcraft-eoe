package net.arna.jcraft.client;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.pose.PoseModifiers;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.client.renderer.armor.*;
import net.arna.jcraft.client.rendering.DamageIndicatorManager;
import net.arna.jcraft.client.particle.DamageNumberParticle;
import net.arna.jcraft.client.rendering.StandUserPoseLoader;
import net.arna.jcraft.client.gravity.util.GravityChannelClient;
import net.arna.jcraft.client.gui.hud.JCraftAbilityHud;
import net.arna.jcraft.client.net.ClientPacketHandler;
import net.arna.jcraft.client.particle.*;
import net.arna.jcraft.client.registry.*;
import net.arna.jcraft.client.renderer.effects.AttackHitboxEffectRenderer;
import net.arna.jcraft.client.renderer.effects.TimeErasePredictionEffectRenderer;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.client.rendering.handler.*;
import net.arna.jcraft.client.util.ClientEntityHandlerImpl;
import net.arna.jcraft.client.util.TrackedKeyBinding;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.item.CosplayItem;
import net.arna.jcraft.common.util.MovementInputType;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class JCraftClient {
    // Keybinds
    public static TrackedKeyBinding standSummon, heavyKey, barrageKey, ultKey, special1Key, special2Key, special3Key,
            comboBreaker, cooldownCancel, utility, dash;
    @SuppressWarnings({"ConstantValue", "DataFlowIssue"}) // Not the case here cuz of the lazy getter.
    @Getter(lazy = true)
    private static final Map<TrackedKeyBinding, MoveInputType> bindings = ImmutableMap.<TrackedKeyBinding, MoveInputType>builder()
            .put(standSummon, MoveInputType.STAND_SUMMON)
            .put(TrackedKeyBinding.wrap(Minecraft.getInstance().options.keyAttack), MoveInputType.LIGHT)
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
    private static final TrackedKeyBinding trackedUseKey = TrackedKeyBinding.wrap(Minecraft.getInstance().options.keyUse);
    public static Supplier<DecimalFormat> decimalFormat = Suppliers.memoize(JCraftClient::createDecimalFormat);
    // public static KeyMapping menuKey;
    public static boolean comboStarted = false;
    public static int framesSinceComboStarted = 0;

    public static void init() {
        JCraft.setClientEntityHandler(ClientEntityHandlerImpl.INSTANCE);

        AutoConfig.register(JClientConfig.class, JanksonConfigSerializer::new);
        JClientConfig.load();

        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new DecimalFormatUpdater());
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, StandUserPoseLoader::onReload);

        GravityChannelClient.init();

        // Rendering
        JRenderLayerRegistry.init();
        RenderHandler.init();
        JClientEventsRegistry.registerClientEvents();
        JCraftAbilityHud.init();
        PoseModifiers.register();

        initCosplay();

        SpecialParticleShaderHandler.INSTANCE.init();
        ZaWarudoShaderHandler.INSTANCE.init();
        CrimsonShaderHandler.INSTANCE.init();
        EpitaphVignetteShaderHandler.INSTANCE.init();
        MandomRewindShaderHandler.INSTANCE.init();

        // Renderer registration
        // JArmorRendererRegistry.registerArmorRenderers();

        ClientPacketHandler.init();

        AttackHitboxEffectRenderer.init();
        TimeErasePredictionEffectRenderer.init();
    }

    private static void initCosplay() {
        initCosplay(JItemRegistry.COWBOY_HAT, ArmorRenderer.simple("cowboy_outfit"));
        initCosplay(JItemRegistry.COWBOY_PONCHO, ArmorRenderer.simple("cowboy_poncho"));
        initCosplay(JItemRegistry.COWBOY_GUNBELT_SPURS, CowboyGunbeltRenderer::new);
        initCosplay(JItemRegistry.DIAVOLO_WIG, ArmorRenderer.simple("diavoloclothes"));
        initCosplay(JItemRegistry.DIAVOLO_SHIRT, ArmorRenderer.flutter("diavoloshirt"));
        initCosplay(JItemRegistry.DIAVOLO_PANTS, ArmorRenderer.simple("diavoloclothes"));
        initCosplay(JItemRegistry.DIAVOLO_BOOTS, ArmorRenderer.simple("diavoloclothes"));
        initCosplay(JItemRegistry.DIEGO_HAT, ArmorRenderer.simple("diegooutfit"));
        initCosplay(JItemRegistry.DIEGO_SHIRT, ArmorRenderer.simple("diegooutfit"));
        initCosplay(JItemRegistry.DIEGO_PANTS, ArmorRenderer.simple("diegooutfit"));
        initCosplay(JItemRegistry.DIEGO_BOOTS, ArmorRenderer.simple("diegooutfit"));
        initCosplay(JItemRegistry.DIO_HEADBAND, ArmorRenderer.flutter("diojacket"));
        initCosplay(JItemRegistry.DIO_JACKET, ArmorRenderer.flutter("diojacket"));
        initCosplay(JItemRegistry.DIO_PANTS, DIOtardRenderer::new);
        initCosplay(JItemRegistry.DIO_BOOTS, DIOtardRenderer::new);
        initCosplay(JItemRegistry.DIO_CAPE, DIOCapeRenderer::new);
        initCosplay(JItemRegistry.DIO_P1_WIG, ArmorRenderer.simple("diooutfit"));
        initCosplay(JItemRegistry.DIO_P1_JACKET, ArmorRenderer.simple("diooutfit"));
        initCosplay(JItemRegistry.DIO_P1_PANTS, ArmorRenderer.simple("diooutfit"));
        initCosplay(JItemRegistry.DIO_P1_BOOTS, ArmorRenderer.simple("diooutfit"));
        initCosplay(JItemRegistry.OH_DIO_WIG, ArmorRenderer.simple("oh_diojacket"));
        initCosplay(JItemRegistry.OH_DIO_JACKET, ArmorRenderer.simple("oh_diojacket"));
        initCosplay(JItemRegistry.OH_DIO_PANTS, OhDIOtardRenderer::new);
        initCosplay(JItemRegistry.OH_DIO_BOOTS, OhDIOtardRenderer::new);
        initCosplay(JItemRegistry.DOPPIO_WIG, ArmorRenderer.simple("doppiotop"));
        initCosplay(JItemRegistry.DOPPIO_SHIRT, ArmorRenderer.simple("doppiotop"));
        initCosplay(JItemRegistry.FINAL_KIRA_WIG, FinalKiraArmorRenderer::new);
        initCosplay(JItemRegistry.FINAL_KIRA_JACKET, FinalKiraJacketRenderer::new);
        initCosplay(JItemRegistry.FINAL_KIRA_PANTS, FinalKiraArmorRenderer::new);
        initCosplay(JItemRegistry.FINAL_KIRA_BOOTS, FinalKiraArmorRenderer::new);
        initCosplay(JItemRegistry.GIORNO_WIG, GiornoArmorRenderer::new);
        initCosplay(JItemRegistry.GIORNO_JACKET, GiornoJacketRenderer::new);
        initCosplay(JItemRegistry.GIORNO_PANTS, GiornoArmorRenderer::new);
        initCosplay(JItemRegistry.GIORNO_BOOTS, GiornoArmorRenderer::new);
        initCosplay(JItemRegistry.GYRO_HAT, ArmorRenderer.simple("gyrotop"));
        initCosplay(JItemRegistry.GYRO_SHIRT, ArmorRenderer.simple("gyrotop"));
        initCosplay(JItemRegistry.GYRO_PANTS, GyroBottomRenderer::new);
        initCosplay(JItemRegistry.GYRO_BOOTS, GyroBottomRenderer::new);
        initCosplay(JItemRegistry.HEAVEN_ATTAINED_WIG, ArmorRenderer.simple("heavenattainedoutfit"));
        initCosplay(JItemRegistry.HEAVEN_ATTAINED_SHIRT, ArmorRenderer.simple("heavenattainedoutfit"));
        initCosplay(JItemRegistry.HEAVEN_ATTAINED_PANTS, ArmorRenderer.simple("heavenattainedoutfit"));
        initCosplay(JItemRegistry.HEAVEN_ATTAINED_BOOTS, ArmorRenderer.simple("heavenattainedoutfit"));
        initCosplay(JItemRegistry.JOHNNY_CAP, ArmorRenderer.simple("johnnyclothes"));
        initCosplay(JItemRegistry.JOHNNY_JACKET, ArmorRenderer.simple("johnnyclothes"));
        initCosplay(JItemRegistry.JOHNNY_PANTS, ArmorRenderer.simple("johnnyclothes"));
        initCosplay(JItemRegistry.JOHNNY_BOOTS, ArmorRenderer.simple("johnnyclothes"));
        initCosplay(JItemRegistry.JOTARO_CAP, JotaroArmorRenderer::new);
        initCosplay(JItemRegistry.JOTARO_JACKET, JotaroCoatRenderer::new);
        initCosplay(JItemRegistry.JOTARO_PANTS, JotaroArmorRenderer::new);
        initCosplay(JItemRegistry.JOTARO_BOOTS, JotaroArmorRenderer::new);
        initCosplay(JItemRegistry.JOTARO_P4_CAP, JotaroArmorP4Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P4_JACKET, JotaroCoatP4Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P4_PANTS, JotaroArmorP4Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P4_BOOTS, JotaroArmorP4Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P6_CAP, JotaroArmorP6Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P6_JACKET, JotaroCoatP6Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P6_PANTS, JotaroArmorP6Renderer::new);
        initCosplay(JItemRegistry.JOTARO_P6_BOOTS, JotaroArmorP6Renderer::new);
        initCosplay(JItemRegistry.KAKYOIN_WIG, ArmorRenderer.simple("kakyoinclothes"));
        initCosplay(JItemRegistry.KAKYOIN_COAT, KakyoinCoatRenderer::new);
        initCosplay(JItemRegistry.KAKYOIN_PANTS, ArmorRenderer.simple("kakyoinclothes"));
        initCosplay(JItemRegistry.KAKYOIN_BOOTS, ArmorRenderer.simple("kakyoinclothes"));
        initCosplay(JItemRegistry.MOUNTAIN_TIM_HAT, ArmorRenderer.simple("mountain_tim_clothes"));
        initCosplay(JItemRegistry.MOUNTAIN_TIM_SHIRT, ArmorRenderer.simple("mountain_tim_top"));
        initCosplay(JItemRegistry.MOUNTAIN_TIM_COAT, MountainTimCoatRenderer::new);
        initCosplay(JItemRegistry.MOUNTAIN_TIM_PANTS, MountainTimPantsRenderer::new);
        initCosplay(JItemRegistry.MOUNTAIN_TIM_BOOTS, ArmorRenderer.simple("mountain_tim_clothes"));
        initCosplay(JItemRegistry.POLNAREFF_WIG, ArmorRenderer.simple("polnareffoutfit"));
        initCosplay(JItemRegistry.POLNAREFF_SHIRT, ArmorRenderer.simple("polnareffoutfit"));
        initCosplay(JItemRegistry.POLNAREFF_PANTS, ArmorRenderer.simple("polnareffoutfit"));
        initCosplay(JItemRegistry.POLNAREFF_BOOTS, ArmorRenderer.simple("polnareffoutfit"));
        initCosplay(JItemRegistry.KARS_HEADWRAP, ArmorRenderer.simple("karsoutfit"));
        initCosplay(JItemRegistry.KIRA_WIG, KiraArmorRenderer::new);
        initCosplay(JItemRegistry.KIRA_JACKET, ArmorRenderer.simple("kirajacket"));
        initCosplay(JItemRegistry.KIRA_PANTS, KiraArmorRenderer::new);
        initCosplay(JItemRegistry.KIRA_BOOTS, KiraArmorRenderer::new);
        initCosplay(JItemRegistry.KOSAKU_WIG, KosakuArmorRenderer::new);
        initCosplay(JItemRegistry.KOSAKU_JACKET, KosakuJacketRenderer::new);
        initCosplay(JItemRegistry.KOSAKU_PANTS, KosakuArmorRenderer::new);
        initCosplay(JItemRegistry.KOSAKU_BOOTS, KosakuArmorRenderer::new);
        initCosplay(JItemRegistry.PUCCIS_HAT, ArmorRenderer.simple("puccis_hat"));
        initCosplay(JItemRegistry.PUCCI_ROBE, PucciRobeRenderer::new);
        initCosplay(JItemRegistry.PUCCI_PANTS, ArmorRenderer.simple("puccibottom"));
        initCosplay(JItemRegistry.PUCCI_BOOTS, ArmorRenderer.simple("puccibottom"));
        initCosplay(JItemRegistry.RED_HAT, ArmorRenderer.simple("red_hat"));
        initCosplay(JItemRegistry.RINGO_OUTFIT, RingoOutfitRenderer::new);
        initCosplay(JItemRegistry.RINGO_BOOTS, RingoOutfitRenderer::new);
        initCosplay(JItemRegistry.RISOTTO_CAP, RisottoCapRenderer::new);
        initCosplay(JItemRegistry.RISOTTO_JACKET, ArmorRenderer.simple("risottotop"));
        initCosplay(JItemRegistry.RISOTTO_PANTS, RisottoBottomRenderer::new);
        initCosplay(JItemRegistry.RISOTTO_BOOTS, RisottoBottomRenderer::new);
        AzArmorRendererRegistry.register(ArmorRenderer.simple("stone_mask"), JItemRegistry.STONE_MASK.get());
        initCosplay(JItemRegistry.STRAIZO_PONCHO, ArmorRenderer.simple("straizoponcho"));
        initCosplay(JItemRegistry.STRAIZO_PONCHO, ArmorRenderer.simple("straizoponcho"));
        initCosplay(JItemRegistry.VALENTINE_WIG, ValentineWigRenderer::new);
        initCosplay(JItemRegistry.VALENTINE_JACKET, ValentineTopRenderer::new);
        initCosplay(JItemRegistry.VALENTINE_PANTS, ValentineBottomRenderer::new);
        initCosplay(JItemRegistry.VALENTINE_BOOTS, ValentineBottomRenderer::new);
    }

    private static void initCosplay(CosplayItem<?> cosplay, Supplier<AzArmorRenderer> renderer) {
        for (RegistrySupplier<? extends ArmorItem> item : cosplay) {
            AzArmorRendererRegistry.register(renderer, item.get());
        }
    }
    
    public static void registerKeyBindings(@Nullable Consumer<KeyMapping> register) {
        if (register == null) register = KeyMappingRegistry::register;

        // Keybinding registration
        standSummon = TrackedKeyBinding.createAndRegister("key.jcraft.standsummon", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N, "key.category.jcraft", register);
        heavyKey = TrackedKeyBinding.createAndRegister("key.jcraft.heavy", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R, "key.category.jcraft", register);
        barrageKey = TrackedKeyBinding.createAndRegister("key.jcraft.barrage", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B, "key.category.jcraft", register);
        ultKey = TrackedKeyBinding.createAndRegister("key.jcraft.ultimate", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H, "key.category.jcraft", register);
        special1Key = TrackedKeyBinding.createAndRegister("key.jcraft.special1", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V, "key.category.jcraft", register);
        special2Key = TrackedKeyBinding.createAndRegister("key.jcraft.special2", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G, "key.category.jcraft", register);
        special3Key = TrackedKeyBinding.createAndRegister("key.jcraft.special3", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M, "key.category.jcraft", register);
        //comboBreaker = TrackingKeyBinding.createAndRegister("key.jcraft.combobreaker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.category.jcraft");
        cooldownCancel = TrackedKeyBinding.createAndRegister("key.jcraft.cooldowncancel", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT, "key.category.jcraft", register);
        utility = TrackedKeyBinding.createAndRegister("key.jcraft.utility", InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_5, "key.category.jcraft", register);
        dash = TrackedKeyBinding.createAndRegister("key.jcraft.dash", InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_4, "key.category.jcraft", register);

        // todo: actually finish jcraft menu
        // menuKey = new KeyMapping("key.jcraft.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_DIVIDE, "key.category.jcraft");
        // register.accept(menuKey);
    }

    /// TEXT HUD
    public static final List<String> comboRemarks = List.of("admin rdm!!!", "baby combo", "caught lackin", "kinda ez", "skill issue", "cancelled on twitter", "sent to bulgaria", "down bad");
    public static int comboCounter = 0;
    public static float damageScaling = 1.00f;
    public static int framesSinceCounted = 0;

    public static int IPSTriggerFramesLeft = 0;
    public static final int IPS_TRIGGER_FRAMES = 120;

    public static void markIPSTriggered() {
        IPSTriggerFramesLeft = IPS_TRIGGER_FRAMES;
    }


    public static void markComboStarted() {
        comboStarted = true;
        framesSinceComboStarted = 0;
    }

    private static Map<TrackedKeyBinding, MovementInputType> createMovementBindingsMap() {
        final Options options = Minecraft.getInstance().options;
        return ImmutableMap.<TrackedKeyBinding, MovementInputType>builder()
                .put(TrackedKeyBinding.wrap(options.keyUp), MovementInputType.FORWARD)
                .put(TrackedKeyBinding.wrap(options.keyDown), MovementInputType.BACKWARD)
                .put(TrackedKeyBinding.wrap(options.keyLeft), MovementInputType.LEFT)
                .put(TrackedKeyBinding.wrap(options.keyRight), MovementInputType.RIGHT)
                .put(TrackedKeyBinding.wrap(options.keyJump), MovementInputType.JUMP)
                .put(TrackedKeyBinding.wrap(options.keyShift), MovementInputType.CROUCH)
                .put(dash, MovementInputType.DASH)
                .build();
    }

    /**
     * @return a cleaned-up version of TranslatableText name of button
     */
    public static String generateName(final KeyMapping keyBinding, boolean makeShort) {
        final String str = keyBinding.saveString();
        final String[] components = str.split("\\.");

        String last = components[components.length - 1];
        String secondLast = components[components.length - 2] + " ";

        if (components[components.length - 2].equals("keyboard")) {
            secondLast = "";
        }

        if (makeShort) {
            if (secondLast.length() > 0) secondLast = secondLast.substring(0, 1);
            if (last.length() > 0) last = last.substring(0, 1);
        }

        return StringUtils.capitalize(secondLast) + StringUtils.capitalize(last);
    }

    public static <E extends Enum<E>> Object2BooleanMap<E> getChangedInputs(final Map<TrackedKeyBinding, E> bindings) {
        return bindings.entrySet().stream()
                .filter(entry -> entry.getKey().isChangedThisTick())
                .collect(Object2BooleanOpenHashMap::new, (map, entry) -> map.put(entry.getValue(), entry.getKey().isPressedThisTick()),
                        Object2BooleanMap::putAll);
    }

    @Nullable
    public static StandEntity<?, ?> getStandEntity() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }

        for (Entity e : player.getPassengers()) {
            if (e instanceof StandEntity<?, ?> s) {
                return s;
            }
        }
        return null;
    }

    private static DecimalFormat createDecimalFormat() {
        return new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(
                Locale.forLanguageTag(Minecraft.getInstance().options.languageCode)));
    }

    public static void registerParticleSpriteSets() {
        // TODO Forge version is currently handled separately cuz Forge is ass.
        // See JCraftForgeClient#onParticleFactoryRegistration(RegisterParticleProvidersEvent)

        ParticleProviderRegistry.register(JParticleTypeRegistry.COMBO_BREAK, ComboBreakerParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.HITSPARK_1, provider -> new HitsparkParticle.Factory(provider, 0.4f, 5));
        ParticleProviderRegistry.register(JParticleTypeRegistry.HITSPARK_2, provider -> new HitsparkParticle.Factory(provider, 0.66f, 6));
        ParticleProviderRegistry.register(JParticleTypeRegistry.HITSPARK_3, provider -> new HitsparkParticle.Factory(provider, 1f, 8));
        ParticleProviderRegistry.register(JParticleTypeRegistry.INVERTED_HITSPARK_3, provider -> new InvertedHitsparkParticle.Factory(provider, 1f, 8));
        ParticleProviderRegistry.register(JParticleTypeRegistry.STUN_SLASH, provider -> new HitsparkParticle.Factory(provider, 0.6f, 6));
        ParticleProviderRegistry.register(JParticleTypeRegistry.STUN_PIERCE, provider -> new HitsparkParticle.Factory(provider, 0.6f, 6));
        ParticleProviderRegistry.register(JParticleTypeRegistry.KCPARTICLE, KCParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.BACKSTAB, BackstabParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.SPEED_PARTICLE, SpeedParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.BITES_THE_DUST, BitesTheDustParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.BOOM_1, BoomParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.PIXEL, PixelParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.BLOCKSPARK, provider -> new BlocksparkParticle.Factory(provider, 0.15f));
        ParticleProviderRegistry.register(JParticleTypeRegistry.GO, GoParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.DO, GoParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.AURA_ARC, AuraArcParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.AURA_BLOB, AuraBlobParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.INVERSION, InversionParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.OVERLAP, OverlappingParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.SUN_LOCK_ON, BackstabParticle.Factory::new); // 9 frames, reusing
        ParticleProviderRegistry.register(JParticleTypeRegistry.PURPLE_HAZE_CLOUD, PurpleHazeCloudParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.PURPLE_HAZE_PARTICLE, PurpleHazeErraticParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.DAMAGE_NUMBER, DamageNumberParticle.Factory::new);
        DamageIndicatorManager.setDamageNumberParticle(JParticleTypeRegistry.DAMAGE_NUMBER.get());
        ParticleProviderRegistry.register(JParticleTypeRegistry.HAMON_SPARK, provider -> new HitsparkParticle.Factory(provider, 0.2f, 6));
        ParticleProviderRegistry.register(JParticleTypeRegistry.LEMON, LemonParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.METALLICA_MOSH_1, MoshParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.METALLICA_MOSH_2, MoshParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.METALLICA_MOSH_3, MoshParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.METALLICA_MOSH_4, MoshParticle.Factory::new);
        ParticleProviderRegistry.register(JParticleTypeRegistry.METALLICA_MOSH_5, MoshParticle.Factory::new);
    }

    @Getter
    private static class DecimalFormatUpdater implements PreparableReloadListener {
        private final ResourceLocation fabricId = JCraft.id("decimal_format_updater");

        @Override
        public CompletableFuture<Void> reload(final PreparationBarrier synchronizer, final ResourceManager manager, final ProfilerFiller prepareProfiler,
                                              final ProfilerFiller applyProfiler, final Executor prepareExecutor, final Executor applyExecutor) {
            return synchronizer.wait(Unit.INSTANCE).thenRunAsync(() ->
                    decimalFormat = Suppliers.memoize(JCraftClient::createDecimalFormat), applyExecutor);
        }
    }
}
