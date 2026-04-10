package net.arna.jcraft.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.client.util.TrackedKeyBinding;
import net.arna.jcraft.common.network.c2s.PlayerInputPacket;
import net.arna.jcraft.common.network.c2s.StandBlockPacket;
import net.arna.jcraft.common.tickable.Timestops;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin_logic.Jangler;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.arna.jcraft.client.JCraftClient.*;
import static net.arna.jcraft.client.gui.hud.JCraftAbilityHud.cooldownTypeToKeybind;
import static net.arna.jcraft.client.gui.hud.JCraftAbilityHud.getHudX;
import static net.arna.jcraft.client.util.JClientUtils.activeTimestops;

@Environment(EnvType.CLIENT)
public class JClientEvents {

    // Tracks the game-time tick for each stand user (by UUID)
    // the 100-block menacing radius. Cleared when they leave range.
    private static final Map<UUID, Integer> menacingEntryTimes = new HashMap<>();

    public static void onLast(final PoseStack matrixStack, final Vec3 cameraPos) {
        matrixStack.pushPose();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        RenderHandler.beginBufferedRendering(matrixStack);

        if (RenderHandler.MATRIX4F != null) {
            RenderSystem.getModelViewMatrix().get(RenderHandler.MATRIX4F);
        }
        RenderHandler.renderBufferedBatches(matrixStack);
        RenderHandler.endBufferedRendering(matrixStack);

        matrixStack.popPose();
    }

    public static void afterTranslucent(final PoseStack matrixStack, final Vec3 cameraPos, final LevelRenderer worldRenderer) {
        matrixStack.pushPose();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderHandler.MATRIX4F = new Matrix4f(RenderSystem.getModelViewMatrix());
        matrixStack.popPose();
    }

    public static void clientPlayerJoin(final LocalPlayer clientPlayerEntity) {
        if (clientPlayerEntity == null) {
            JCraft.LOGGER.fatal("onPlayReady was called with invalid client player!");
            return;
        }

        // Sync initial prediction option
        // NetworkManager.sendToServer(JPacketRegistry.C2S_PREDICTION_TRIGGER, PredictionTriggerPacket.write(JClientConfig.getInstance().isClientsidePrediction()));
    }

    public static void renderHud(final GuiGraphics ctx, final float tickDelta) {
        final Minecraft client = Minecraft.getInstance();
        final LocalPlayer player = client.player;
        if (player == null) {
            JCraft.LOGGER.fatal("Attempted to render hud with no player!");
            return;
        }

        framesSinceCounted++;

        final JClientConfig config = JClientConfig.getInstance();

        final boolean isMid = config.getUiPosition() == JClientConfig.UIPos.MIDDLE;
        final boolean useIcons = config.isIconHud();

        final StandEntity<?, ?> stand = JUtils.getStand(player);
        final Font textRenderer = client.gui.getFont();

        int selectedX = getHudX(client.getWindow().getGuiScaledWidth(), 128) + config.getHorizontalHudOffset();
        int selectedY = client.getWindow().getGuiScaledHeight() + config.getVerticalHudOffset();

        switch (config.getUiPosition()) {
            case LEFT -> selectedY /= 20;
            case MIDDLE -> selectedY /= 3;
            case RIGHT -> selectedY = (int) (selectedY / 2.25f);
        }

        // Draw text HUD
        if (!useIcons) {
            final CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(player);

            final CooldownType[] values = CooldownType.values();
            for (int i = 0; i < values.length; i++) {
                final CooldownType type = values[i];
                final int cooldownTicks = cooldowns.getCooldown(type);

                if (cooldownTicks == 0) {
                    continue;
                }
                final double cooldown = (cooldownTicks - tickDelta) / 20d;

                // These are (mainly) based off of keybindings which are client-only and thus have
                // to be done here and cannot be done in CooldownType.
                final String keyBindText = cooldownTypeToKeybind(type, false);

                CooldownType.Category category = type.getCategory();

                final boolean isSpec = category == CooldownType.Category.SPEC;
                final boolean isUniversal = category == CooldownType.Category.UNIVERSAL;
                float defaultAlpha = 0.65f;
                int xOffset = 0;

                String finalText = keyBindText + " - " + JCraftClient.decimalFormat.get().format(Mth.clamp(cooldown, 0.0, 9999.0)) + "s";

                if (category == CooldownType.Category.STAND || isSpec) {
                    if (!isSpec) {
                        finalText = "s." + finalText;
                    }

                    if ((isSpec && stand != null) || (!isSpec && stand == null)) {
                        xOffset = 48;
                        defaultAlpha = 0.3f;
                    }
                }

                int offsetIndex = i;
                if (isSpec) {
                    offsetIndex -= 7;
                } else if (isUniversal) {
                    offsetIndex -= 6;
                }
                final float offsetY = selectedY * 1.25f + 9f * offsetIndex;

                ctx.drawString(
                        textRenderer,
                        finalText,
                        selectedX + xOffset,
                        (int) offsetY,
                        ColorUtils.HSBAtoRGBA(0.3f - (float) cooldown * 10f / 720f, (cooldown < 1.6) ? 0.0f : 1.0f, 1.0f, (cooldown < 1.6) ? 1.0f : defaultAlpha)
                );

            }
        }

        // Draw Combo Counter
        if (comboCounter > 0 && JClientConfig.getInstance().isComboCounter() && framesSinceCounted <= 180) {
            String remark = "epic tod free download";
            if (comboCounter < JCraftClient.comboRemarks.size() * 7) {
                remark = comboRemarks.get(Math.floorDiv(comboCounter, 7));
            }

            final boolean recentHit = framesSinceCounted < 5;

            final RandomSource random = player.getRandom();

            if (comboStarted && ++framesSinceComboStarted > 59) {
                comboStarted = false;
            }

            boolean ipsTriggered = IPSTriggerFramesLeft-- > 0;
            if (ipsTriggered) {
                selectedX += random.nextFloat() * IPSTriggerFramesLeft / 20.0f;
                selectedY += random.nextFloat() * IPSTriggerFramesLeft / 20.0f;
            }

            ctx.drawString(
                    textRenderer,
                    comboCounter + " - (" + Math.round(damageScaling * 100f) + "%) - " + remark,
                    (int) (selectedX + (isMid && useIcons ? 54f : 0) + (recentHit ? tickDelta * random.nextFloat() * 5f : 0)),
                    (int) (selectedY * (1.15f) + (recentHit ? tickDelta * random.nextFloat() * 5f : 0)),
                    ipsTriggered ?
                            ColorUtils.HSBAtoRGBA(0.5f, (IPS_TRIGGER_FRAMES - IPSTriggerFramesLeft) / (float)IPS_TRIGGER_FRAMES, 1f, 0.8f) :
                            ColorUtils.HSBAtoRGBA(comboCounter / 360f - 1f, comboStarted ? framesSinceComboStarted / 60f : 1f, 1f, 0.8f)
            );
        }
    }

    public static void tickClient(final Minecraft client) {
        final LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        /*
        if (menuKey.consumeClick()) {
            NetworkManager.sendToServer(JPacketRegistry.C2S_MENU_CALL, new FriendlyByteBuf(Unpooled.buffer()));
            return;
        }
         */

        final StandEntity<?, ?> stand = JUtils.getStand(player);

        // Handle JCraft inputs (stand, spec, universal controls)
        // Regular input (all moves, regular Minecraft movement (WASD and jumping) and dashing)
        if (player.isAlive()) {
            final Object2BooleanMap<MovementInputType> movementInput = getChangedInputs(getMovementBindings());
            final Object2BooleanMap<MoveInputType> moveInput = getChangedInputs(getBindings());

            if (!movementInput.isEmpty() || !moveInput.isEmpty()) {
                NetworkManager.sendToServer(JPacketRegistry.C2S_PLAYER_INPUT, PlayerInputPacket.write(movementInput, moveInput));
            }

            final Object2BooleanMap<MoveInputType> heldMoves = new Object2BooleanOpenHashMap<>();
            getBindings().forEach((key, value) -> {
                if (key.isDown()) {
                    heldMoves.put(value, true);
                }
            });

            if (!heldMoves.isEmpty()) {
                NetworkManager.sendToServer(JPacketRegistry.C2S_PLAYER_INPUT_HOLD, PlayerInputPacket.write(null, heldMoves));
                //ClientPlayNetworking.send(JPacketRegistry.C2S_PLAYER_INPUT_HOLD, PlayerInputPacket.write(null, heldMoves));
            }

        }

        // Block
        if (getTrackedUseKey().isChangedThisTick()) {
            final boolean pressed = getTrackedUseKey().isPressedThisTick();
            NetworkManager.sendToServer(JPacketRegistry.C2S_STAND_BLOCK, StandBlockPacket.write(pressed));
            if (stand != null && stand.isRemoteAndControllable() && pressed) {
                NetworkManager.sendToServer(JPacketRegistry.C2S_REMOTE_STAND_INTERACT, new FriendlyByteBuf(Unpooled.buffer()));
            }
        }

        // Cooldown Cancel
        if (cooldownCancel.isPressedThisTick()) {
            NetworkManager.sendToServer(JPacketRegistry.C2S_COOLDOWN_CANCEL, new FriendlyByteBuf(Unpooled.buffer()));
        }

        if (client.isPaused() && client.isLocalServer()) {
            return;
        }

        // Timestop handling (nearly identical to serverside, but toStop is obtained in user.world instead of server world)
        final Iterator<DimensionData> iter = activeTimestops.iterator();

        while (iter.hasNext()) {
            final DimensionData timestop = iter.next();
            final LivingEntity user = timestop.getUser();

            if (user != null && user.isAlive()) {
                timestop.decreaseTimer();
                if (timestop.getTimer() <= 0) {
                    iter.remove();
                    continue;
                }
            }
            else {
                iter.remove();
                continue;
            }

            final Vec3 pos = timestop.getPos();

            final List<? extends Entity> toStop = user.level().getEntitiesOfClass(Entity.class,
                    new AABB(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), Timestops.TIMESTOP_PREDICATE);

            for (final Entity entity : toStop) {
                if (entity != user && entity != JUtils.getStand(user) && entity != user.getVehicle()) {
                    JComponentPlatformUtils.getTimeStopData(entity)
                            .ifPresent(d -> d.setTicks(2));
                }
            }
        }
        TrackedKeyBinding.resetValues(client.screen != null);

        // Menacing (ゴ/ド) particles — 10-second burst when a stand user enters 100-block radius.
        // Works regardless of whether the local player or target has their stand summoned.
        tickMenacing(client, player);

        // Play jangle sound (from spurs) for all entities
        playJangle();
    }

    private static void tickMenacing(final Minecraft client, final LocalPlayer player) {
        final ClientLevel level = client.level;
        if (level == null) {
            return;
        }

        // Local player must be a stand user themselves
        final StandType type = JComponentPlatformUtils.getStandComponent(player).getType();
        if (StandTypeUtil.isNone(type)) {
            menacingEntryTimes.clear();
            return;
        }

        if (!JClientUtils.shouldRenderStands()) {
            menacingEntryTimes.clear();
            return;
        }

        final double radius = 100.0;
        final double radiusSq = radius * radius;
        final AABB searchBox = AABB.ofSize(player.position(), radius * 2, radius * 2, radius * 2);
        final Set<UUID> inRangeIds = new HashSet<>();

        // find all stand users nearby, for each do
        for (final Player p : level.getEntitiesOfClass(Player.class, searchBox,
                p -> {
                    var pType = JComponentPlatformUtils.getStandComponent(p).getType();
                    return p != player && !p.isSpectator() && !p.isCreative()
                        && p.distanceToSqr(player) <= radiusSq
                        && !p.isInvisible() && !JClientUtils.shouldNotRender(p)
                        && !StandTypeUtil.isNone(pType);
                }
        )) {
            tickMenacing(p, inRangeIds, level, JParticleTypeRegistry.DO);
        }

        // get all other stand users via their stands
        for (final StandEntity<?, ?> stand : level.getEntitiesOfClass(
                StandEntity.class, searchBox,
                stand -> stand.hasUser() && stand.distanceToSqr(player) <= radiusSq && !stand.isInvisible())
        ) {
            final LivingEntity user = stand.getUserOrThrow();
            if (user instanceof Player) { // handled before
                continue;
            }
            if (JClientUtils.shouldNotRender(user)) {
                continue;
            }
            tickMenacing(user, inRangeIds, level, JParticleTypeRegistry.GO);
        }

        // Remove entries for stand users who left range
        menacingEntryTimes.keySet().removeIf(id -> !inRangeIds.contains(id));
    }

    private static void tickMenacing(final LivingEntity living, final Set<UUID> inRangeIds, final ClientLevel level, final RegistrySupplier<SimpleParticleType> particle) {
        final UUID uuid = living.getUUID();
        inRangeIds.add(uuid);
        menacingEntryTimes.putIfAbsent(uuid, -1);
        menacingEntryTimes.put(uuid, menacingEntryTimes.get(uuid) + 1);
        if (menacingEntryTimes.get(uuid) >= 80) {
            return;
        }
        final RandomSource rng = level.getRandom();
        if (rng.nextDouble() > 1d/8) {
            return;
        }
        spawnMenacingParticle(level, rng, particle.get());
    }

    private static void spawnMenacingParticle(final ClientLevel level, final RandomSource rng,
                                              final SimpleParticleType type) {
        LivingEntity entity = Minecraft.getInstance().player;
        if (entity == null) {
            return;
        }
        final Vec3 pos = entity.position();
        final float spread = entity.getBbWidth() * 2.0f;
        final double minY = pos.y + entity.getBbHeight() * 0.7;
        final double maxY = pos.y + entity.getBbHeight() * 1.4;
        level.addParticle(type, false,
                pos.x + rng.triangle(0, spread),
                minY + rng.nextDouble() * (maxY - minY),
                pos.z + rng.triangle(0, spread),
                0, 0, 0);
    }

    private static void playJangle() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        SoundManager soundManager = mc.getSoundManager();
        if (player == null || level == null) return;

        List<Entity> entities = level.getEntities(player, AABB.ofSize(player.position(), 20, 20, 20),
                e -> e instanceof LivingEntity);
        if (!player.isCrouching()) entities.add(player);

        for (Entity entity : entities) {
            if (!entity.onGround()) continue;
            Jangler jangler = (Jangler) entity;

            for (ItemStack armorSlot : entity.getArmorSlots()) {
                if (!armorSlot.is(JTagRegistry.BOOTS_WITH_THE_SPURS)) continue;

                double speedMin    = 0.02,  speedMax    = 0.10;
                double intervalMin = 4,    intervalMax = 12;

                double dx = entity.xOld - entity.getX();
                double dy = entity.yOld - entity.getY();
                double dz = entity.zOld - entity.getZ();
                double speed = dx * dx + dy * dy + dz * dz;
                if (speed < speedMin) continue;

                double t     = (speed - speedMin) / (speedMax - speedMin);
                double delta = 1.0 - Mth.clamp(t, 0.0, 1.0);
                int interval = (int) Mth.lerp(delta, intervalMin, intervalMax);

                // Play jangle once every few tick, depending on their speed
                if (entity.tickCount - jangler.jcraft$getLastJangleAge() < interval) continue;

                // We found an armor piece that has spurs for an entity that is moving,
                // and we haven't played this sound in 5 ticks, play jangle sound.
                RandomSource random = player.getRandom();
                float volume = 1f - random.nextFloat() * 0.3f;
                float pitch = 1f - random.nextFloat() * 0.3f;

                SoundSource soundSource = entity.getSoundSource();
                SoundInstance sound = new SimpleSoundInstance(JSoundRegistry.JANGLE.get(), soundSource, volume, pitch,
                        random, entity.getX(), entity.getY(), entity.getZ());
                soundManager.play(sound);
                jangler.jcraft$markJangle();
                break;
            }
        }
    }
}
