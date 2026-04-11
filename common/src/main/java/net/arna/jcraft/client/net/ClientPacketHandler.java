package net.arna.jcraft.client.net;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.PacketSink;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.gui.ServerConfigUI;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.renderer.effects.AttackHitboxEffectRenderer;
import net.arna.jcraft.client.renderer.effects.TimeErasePredictionEffectRenderer;
import net.arna.jcraft.client.rendering.DamageIndicatorManager;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.arna.jcraft.client.rendering.handler.MandomRewindShaderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.data.AttackerDataLoader;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.splatter.Splatter;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.arna.jcraft.api.registry.JPacketRegistry.*;

/**
 * Packets sent to players must be sent with the appropriate method;
 * {@link NetworkManager#sendToPlayer(ServerPlayer, ResourceLocation, FriendlyByteBuf)} vs. {@link NetworkManager#sendToPlayers(Iterable, ResourceLocation, FriendlyByteBuf)}
 * Iterating through a list of players to send the same buffer object will cause out of bounds packet reads on all but the first iterated client.
 * This is due to {@link NetworkManager#collectPackets(PacketSink, NetworkManager.Side, ResourceLocation, FriendlyByteBuf)} consuming the buffer.
 */

@UtilityClass
public class ClientPacketHandler {
    public static void init() {
        register(S2C_SERVER_CHANNEL_FEEDBACK, ClientPacketHandler::handleChannelFeedback);
        register(S2C_PLAYER_ANIMATION, ClientPacketHandler::handleAnimation);
        register(S2C_SHADER_ACTIVATION, ClientPacketHandler::handleShaderActivation);
        register(S2C_SHADER_DEACTIVATION, ClientPacketHandler::handleShaderDeactivation);
        register(S2C_TIME_ACCELERATION_STATE, ClientPacketHandler::handleTimeAccelState);
        register(S2C_EPITAPH_STATE, ClientPacketHandler::handleEpitaphOverlayState);
        register(S2C_TIME_ERASE_PREDICTION_STATE, ClientPacketHandler::handlePredictionState);
        register(S2C_SERVER_CONFIG, ClientPacketHandler::handleServerConfig);
        register(S2C_J_EXPLOSION, ClientPacketHandler::handleJExplosion);
        register(S2C_COMBO_COUNTER, ClientPacketHandler::handleComboCounter);
        register(S2C_TIME_STOP, ClientPacketHandler::handleTimeStop);
        register(S2C_SPLATTER, ClientPacketHandler::handleSplatter);
        register(S2C_STAND_HURT, ClientPacketHandler::handleStandHurt);
        register(S2C_PREDICTION_UPDATE, ClientPacketHandler::handlePrediction);
        register(S2C_MAGNETIC_FIELD_PARTICLE, ClientPacketHandler::handleMagneticFieldParticle);
        register(S2C_ATTACKER_DATA, ClientPacketHandler::handleAttackerData);
        register(S2C_MANDOM_DATA, ClientPacketHandler::handleMandomData);
        register(S2C_IPS_TRIGGERED, ClientPacketHandler::handleIPSTriggered);
        register(S2C_DAMAGE_NUMBER, ClientPacketHandler::handleDamageNumber);
    }

    private static void handleDamageNumber(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        float damageAmount = buf.readFloat();

        // Execute on client thread
        client.execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity != null) {
                DamageIndicatorManager.spawnDamageNumber(entity, damageAmount);
            }
        });
    }

    private static void handleIPSTriggered(final @NonNull Minecraft client, FriendlyByteBuf buf) {
        JCraftClient.markIPSTriggered();
    }

    private static void handleAttackerData(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        AttackerDataLoader.readFromBuffer(buf);
    }

    private static final int
            NUM_MAGNETIC_CIRCLES = 16,
            NUM_MAGNETIC_PARTICLES = 32;

    private static void handleMagneticFieldParticle(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final double strength = buf.readDouble();

        final Vec3 pos = new Vec3(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );

        client.execute(() -> {
            for (int i = 0; i < NUM_MAGNETIC_CIRCLES; i++) {
                final double phi = i * Math.PI * 2 / NUM_MAGNETIC_CIRCLES;
                final Vec3 direction = new Vec3(Math.cos(phi), 0, Math.sin(phi));

                final Vec3 basePos = pos.add(direction.scale(strength / 2.0));

                for (int j = 0; j < NUM_MAGNETIC_PARTICLES; j++) {
                    final double phi_2 = j * Math.PI * 2 / NUM_MAGNETIC_PARTICLES;
                    final Vec3 horizontalOffset = direction.scale(Math.cos(phi_2));
                    final double verticalOffset = Math.sin(phi_2);

                    client.level.addParticle(
                            ParticleTypes.ENCHANTED_HIT,
                            basePos.x + horizontalOffset.x,
                            basePos.y + horizontalOffset.y + verticalOffset * strength / 2.0,
                            basePos.z + horizontalOffset.z,
                            0.0, 0.0, 0.0
                    );
                }
            }
        });
    }

    private static void handlePrediction(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        if (client.level == null) {
            return;
        }

        final int size = buf.readInt();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            final int entID = buf.readInt();
            final Vec3 predictedPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

            client.execute(() -> {
                final Entity ent = client.level.getEntity(entID);
                if (ent == null) {
                    return;
                }
                // ent.setPos() is awful in tandem with getTrackedPosition().setPos();
                ent.getPositionCodec().setBase(predictedPos);
            });
        }
    }

    private static void handleTimeStop(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        if (client.level == null) {
            return;
        }

        final boolean isStart = buf.readBoolean();
        final int entID = buf.readInt();

        if (isStart) {
            final Vec3 position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            final ResourceKey<Level> registryKey = buf.readResourceKey(Registries.DIMENSION);
            int time = buf.readInt();

            client.execute(() -> {
                final Entity ent = client.level.getEntity(entID);
                if (!(ent instanceof LivingEntity livingEntity)) {
                    return;
                }
                JClientUtils.activeTimestops.add(new DimensionData(livingEntity, position, registryKey, time));
            });
        } else {
            JClientUtils.removeTimestop(entID);
        }
    }

    private static void register(final ResourceLocation id, final Consumer<FriendlyByteBuf> handler) {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, id, (buf, context) -> {
            handler.accept(buf);
        });
    }

    private static void register(final ResourceLocation id, final BiConsumer<Minecraft, FriendlyByteBuf> handler) {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, id, (buf, context) -> {
            handler.accept(Minecraft.getInstance(), buf);
        });
    }

    public static void handleAnimation(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        if (client.level == null) {
            return;
        }

        final int entID = buf.readInt();
        final String animID = buf.readUtf(); // I know exactly how unoptimized this is, but I fail to care
        final boolean isSpec = buf.readBoolean();

        //JCraft.LOGGER.info("JCRAFT CLIENT:\nRecieving animation packet of animID: " + animID + " for entity ID: " + entID);

        final int moveStun;
        final float animationSpeed;

        if (isSpec) {
            moveStun = buf.readInt();
            animationSpeed = buf.readFloat();
            //JCraft.LOGGER.info("Animation packet is for specs, and has attached moveStun: " + moveStun + " and attackID: " + attackID);
        } else {
            moveStun = 0;
            animationSpeed = 0f;
        }

        client.execute(() -> {
            final Entity ent = client.level.getEntity(entID);
            //JCraft.LOGGER.info("Animation is to be applied to: " + ent);
            if (ent instanceof final Player player) {
                // Animate
                final ModifierLayer<IAnimation> animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                final KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(JCraft.id(animID));
                if (anim == null) {
                    JCraft.LOGGER.error(String.format("Tried to play null animation on player: %s, in world %s", player, client.level));
                    return;
                }

                // Remove last speed modifier, this is rather primitive but will do for now
                if (animationContainer.size() > 0) {
                    animationContainer.removeModifier(0);
                }

                // Synchronize spec values
                if (isSpec) {
                    final JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec == null) {
                        JCraft.LOGGER.error(String.format("Tried to set spec animation values on player without spec: %s, in world %s", player, client.level));
                    } else {
                        //JCraft.LOGGER.info("Spec: " + spec.getName());
                        spec.moveStun = moveStun;

                        //JCraft.LOGGER.info("Speed: " + animationSpeed);
                        animationContainer.addModifierBefore(new SpeedModifier(animationSpeed));
                    }
                }

                //JCraft.LOGGER.info("Animation to be applied: " + anim);
                animationContainer.setAnimation(new KeyframeAnimationPlayer(anim));
            }
        });
    }

    public static void handleChannelFeedback(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        if (client.level == null || client.player == null) {
            return;
        }

        final short control = buf.readShort();
        switch (control) {
            // Attack hit boxes
            case (1) -> {
                final int count = buf.readVarInt();

                final AABB[] boxes = new AABB[count];

                for (int i = 0; i < count; i++) {
                    final double
                        minX = buf.readDouble(),
                        minY = buf.readDouble(),
                        minZ = buf.readDouble(),

                        maxX = buf.readDouble(),
                        maxY = buf.readDouble(),
                        maxZ = buf.readDouble();

                    boxes[i] = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
                }

                // Run on render thread to avoid concurrency issues.
                RenderSystem.recordRenderCall(() -> AttackHitboxEffectRenderer.addHitboxes(boxes));
            }

            // Time erase trackers
            case (2) -> {
                double posX = buf.readDouble();
                double posY = buf.readDouble();
                double posZ = buf.readDouble();
                double sizeX = Mth.clamp(buf.readDouble(), 0.1, 100);
                double sizeY = Mth.clamp(buf.readDouble(), 0.1, 100);
                double sizeZ = Mth.clamp(buf.readDouble(), 0.1, 100);

                client.execute(() -> {
                    final Random random = new Random();

                    for (int h = 0; h < 8; ++h) {
                        client.level.addParticle(
                                JParticleTypeRegistry.KCPARTICLE.get(),
                                posX + random.nextDouble(sizeX) - sizeX / 2,
                                posY + random.nextDouble(sizeY),
                                posZ + random.nextDouble(sizeZ) - sizeZ / 2,
                                0.0, 0.0, 0.0
                        );
                    }
                });
            }

            // Generic single particle
            case (3) -> {
                final double x = buf.readDouble();
                final double y = buf.readDouble();
                final double z = buf.readDouble();
                final JParticleType particleType = buf.readEnum(JParticleType.class);

                client.execute(() -> client.level.addParticle(particleType.getParticleType(), true, x, y, z,
                        0, 0, 0));
            }

            // Complex hit spark
            case (5) -> {
                final double x = buf.readDouble();
                final double y = buf.readDouble();
                final double z = buf.readDouble();
                final JParticleType particleType = buf.readEnum(JParticleType.class);
                final int sparkCount = buf.readInt();
                final double speed = buf.readDouble();

                client.execute(() -> {
                    final Random random = new Random();
                    final SimpleParticleType type = particleType.getParticleType();
                    for (int i = 0; i < sparkCount; i++) {
                        final Vec3 vel = JUtils.randUnitVec(random);
                        client.level.addParticle(type, false,
                                x + random.nextGaussian() * 0.33, y + random.nextGaussian() * 0.33, z + random.nextGaussian() * 0.33,
                                vel.x * speed, vel.y * speed, vel.z * speed);
                    }
                });
            }

            // Return to Zero trackers
            case (7) -> {
                final int entID = buf.readInt();
                final Vec3 originalPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

                client.execute(() -> {
                    final Entity ent = client.level.getEntity(entID);
                    if (ent == null) {
                        return;
                    }
                    final Vec3 currentPos = ent.getEyePosition();
                    final Vec3 originalToCurrent = currentPos.subtract(originalPos).normalize();
                    for (double h = 0; h < currentPos.distanceTo(originalPos); ++h) {
                        client.level.addParticle(
                                ParticleTypes.ELECTRIC_SPARK,
                                originalPos.x + originalToCurrent.x * h, originalPos.y + originalToCurrent.y * h, originalPos.z + originalToCurrent.z * h,
                                -originalToCurrent.x, -originalToCurrent.y, -originalToCurrent.z
                        );
                    }
                });
            }

            // Bites the Dust tracker
            case (9) -> {
                final double v1x = buf.readDouble();
                final double v1y = buf.readDouble();
                final double v1z = buf.readDouble();

                final double v2x = buf.readDouble();
                final double v2y = buf.readDouble();
                final double v2z = buf.readDouble();

                final double oX = buf.readDouble();
                final double oY = buf.readDouble();
                final double oZ = buf.readDouble();

                final boolean inRange = buf.readBoolean();

                client.execute(() -> {
                    final Random random = new Random();

                    for (int h = 0; h < 16; ++h) {
                        final double x = v1x + random.nextDouble(v2x) - v2x / 2;
                        final double y = v1y + random.nextDouble(v2y) - v2y / 2;
                        final double z = v1z + random.nextDouble(v2z) - v2z / 2;

                        client.level.addParticle(
                                inRange ? ParticleTypes.WAX_OFF : ParticleTypes.GLOW,
                                x, y, z, 0, 0, 0);
                    }

                    for (int h = 0; h < 8; ++h) {
                        final double x = oX + random.nextDouble(v2x) - v2x / 2;
                        final double y = oY + random.nextDouble(v2y) - v2y / 2;
                        final double z = oZ + random.nextDouble(v2z) - v2z / 2;

                        client.level.addParticle(
                                ParticleTypes.GLOW,
                                x, y, z, 0, 0, 0);
                    }
                });
            }

            // Crossfire hurricane
            case (10) -> {
                final Random random = new Random();
                final double x = buf.readDouble();
                final double y = buf.readDouble();
                final double z = buf.readDouble();

                client.execute(() -> {
                    for (int h = 0; h < 360; ++h) {
                        client.level.addParticle(
                                random.nextInt(0, 5) > 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                                x + Math.sin(h) * 4 + random.nextGaussian() * 2, y + random.nextGaussian() * 1.5, z + Math.cos(h) * 4 + random.nextGaussian() * 2,
                                Math.sin(h + 1.57) / 4, 0, Math.cos(h + 1.57) / 4);
                    }
                });
            }

            // Fool Dust Cloud
            case (11) -> {
                final double x = buf.readDouble();
                final double y = buf.readDouble();
                final double z = buf.readDouble();
                final double size = buf.readDouble();

                client.execute(() -> {
                    final Random random = new Random();
                    for (int h = 0; h < size * 128; ++h) {
                        client.level.addParticle(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                x + random.nextGaussian() * size,
                                y + random.nextGaussian() * size,
                                z + random.nextGaussian() * size,
                                0, 0, 0);
                    }
                });
            }

            // Reset Player Animation
            case (13) -> {
                final int entID = buf.readInt();

                client.execute(() -> {
                    Entity ent = client.level.getEntity(entID);
                    if (ent instanceof Player player) {
                        ModifierLayer<IAnimation> animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                        animationContainer.setAnimation(null);
                    }
                });
            }
        }
    }

    public static void handleMandomData(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final int entID = buf.readInt();
        final Vec3 originalPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        final float r = buf.readFloat();
        final float g = buf.readFloat();
        final float b = buf.readFloat();

        client.execute(() -> {
            final Entity ent = client.level.getEntity(entID);
            if (ent == null || ent.isInvisible() || JClientUtils.shouldNotRender(ent) || ent instanceof StandEntity<?,?>) {
                return;
            }
            final Vec3 currentPos = ent.position();
            final Vec3 originalToCurrent = currentPos.subtract(originalPos).normalize();
            for (double h = 0; h < currentPos.distanceTo(originalPos); ++h) {
                client.level.addParticle(
                        new DustParticleOptions(new Vector3f(r, g, b), 1.0f),
                        originalPos.x + originalToCurrent.x * h,
                        originalPos.y + originalToCurrent.y * h,
                        originalPos.z + originalToCurrent.z * h,
                        -originalToCurrent.x, -originalToCurrent.y, -originalToCurrent.z
                );
            }
        });
    }

    public static void handleShaderActivation(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final int delay = buf.readInt();
        final int duration = buf.readInt();
        final ShaderActivationPacket.Type type = ShaderActivationPacket.Type.byName(buf.readUtf());
        final Level world = client.level;
        if (world == null) {
            return;
        }

        switch (type) {
            case NONE -> {
            }
            case ZA_WARUDO -> {
                final int id = buf.readInt();
                client.execute(() -> {
                    final Entity sourceShader = world.getEntity(id);
                    if (sourceShader instanceof final LivingEntity livingEntity) {

                        ZaWarudoShaderHandler zaWarudoShaderHandler = ZaWarudoShaderHandler.INSTANCE;
                        zaWarudoShaderHandler.shaderSourceEntity = Optional.of(livingEntity).orElse(client.player);
                        zaWarudoShaderHandler.effectLength = duration;
                        zaWarudoShaderHandler.shouldRender = true;

                    }
                });
            }
            case CRIMSON -> client.execute(() -> {
                if (!JClientConfig.getInstance().isTimeEraseShader()) {
                    return;
                }

                final CrimsonShaderHandler crimsonShaderHandler = CrimsonShaderHandler.INSTANCE;
                crimsonShaderHandler.effectLength = duration;
                crimsonShaderHandler.shouldRender = true;


            });
            case MANDOM_REWIND -> {
                final float r = buf.readFloat();
                final float g = buf.readFloat();
                final float b = buf.readFloat();
                client.execute(() -> {
                    MandomRewindShaderHandler mandomHandler = MandomRewindShaderHandler.INSTANCE;
                    mandomHandler.duration = duration;
                    mandomHandler.shaderColor = new Vector3f(r, g, b);
                    mandomHandler.shouldRender = true;
                });
            }
        }
    }

    public static void handleShaderDeactivation(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final ShaderActivationPacket.Type type = ShaderActivationPacket.Type.byName(buf.readUtf());
        final Level world = client.level;
        if (world != null) {
            switch (type) {
                case NONE -> {
                }
                case ZA_WARUDO -> client.execute(() -> {

                    ZaWarudoShaderHandler zaWarudoShaderHandler = ZaWarudoShaderHandler.INSTANCE;
                    zaWarudoShaderHandler.shouldRender = false;
                    zaWarudoShaderHandler.renderingEffect = false;

                });
                case CRIMSON -> client.execute(() -> {

                    CrimsonShaderHandler crimsonShaderHandler = CrimsonShaderHandler.INSTANCE;
                    crimsonShaderHandler.shouldRender = false;
                    crimsonShaderHandler.renderingEffect = false;

                });
            }
        }
    }

    public static void handleTimeAccelState(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final TimeAccelStatePacket.State state = buf.readEnum(TimeAccelStatePacket.State.class);
        final Entity e = client.level == null ? null : client.level.getEntity(buf.readVarInt());

        if (!(e instanceof final MadeInHeavenEntity mih) || !mih.isAlive()) {
            return;
        }

        switch (state) {
            case START -> {
                final int duration = buf.readVarInt();
                final long startTime = buf.readLong();
                TimeAccelStatePacket.addAcceleration(mih.getId(), (int) (duration - (System.currentTimeMillis() - startTime) / 50), startTime);
            }
            case STOP -> TimeAccelStatePacket.removeAcceleration(mih.getId());
        }
    }

    public static void handleEpitaphOverlayState(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final boolean start = buf.readBoolean();
        client.execute(() -> {
            if (start) {
                EpitaphOverlay.start();
            } else {
                EpitaphOverlay.stop();
            }
        });
    }

    public static void handlePredictionState(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final boolean start = buf.readBoolean();
        final int length = start ? buf.readVarInt() : 0;

        client.execute(() -> {
            if (start) {
                TimeErasePredictionEffectRenderer.startEffect(length);
            } else {
                TimeErasePredictionEffectRenderer.stopEffect();
            }
        });
    }

    public static void handleServerConfig(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final boolean editable = buf.readBoolean();
        final boolean show = buf.readBoolean();

        ConfigOption.readOptions(buf);

        if (show) {
            client.execute(() -> ServerConfigUI.show(editable));
        }
    }

    private static void handleJExplosion(final @NonNull Minecraft client, final FriendlyByteBuf buf) {
        final ClientboundExplodePacket nativePacket = new ClientboundExplodePacket(buf);
        final JExplosionModifier modifier = buf.readBoolean() ? JExplosionModifier.read(buf) : null;

        client.execute(() -> {
            final Explosion explosion = new Explosion(client.level, null, nativePacket.getX(), nativePacket.getY(), nativePacket.getZ(),
                    nativePacket.getPower(), nativePacket.getToBlow());
            ((IJExplosion) explosion).jcraft$setModifier(modifier);
            explosion.finalizeExplosion(true);
            Objects.requireNonNull(client.player).setDeltaMovement(client.player.getDeltaMovement()
                    .add(nativePacket.getKnockbackX(), nativePacket.getKnockbackY(), nativePacket.getKnockbackZ()));
        });
    }

    private static void handleComboCounter(final @NonNull Minecraft minecraftClient, final FriendlyByteBuf buf) {
        JCraftClient.comboCounter = buf.readInt();
        if (JCraftClient.comboCounter == 1) {
            JCraftClient.markComboStarted();
        }

        JCraftClient.damageScaling = buf.readFloat();

        JCraftClient.framesSinceCounted = 0;
    }

    private static void handleSplatter(final Minecraft client, final FriendlyByteBuf buf) {
        final ClientLevel world = client.level;
        if (world == null) {
            return;
        }

        final Splatter splatter = JUtils.getSplatterManager(world).readSplatter(buf);

        long ageMs = splatter.getType().getMaxAge() * 50L;
        AttackHitboxEffectRenderer.addHitbox(splatter.getMainBox(), ageMs, true);
        splatter.getSections().stream()
                .filter(section -> !section.isRemoved())
                .forEach(section -> AttackHitboxEffectRenderer.addHitbox(section.getHitBox(), ageMs, true));
    }

    private static void handleStandHurt(final Minecraft client, final FriendlyByteBuf buf) {
        final int entityId = buf.readVarInt();
        client.execute(() -> {
            if (client.level == null) {
                return;
            }

            final Entity entity = client.level.getEntity(entityId);
            if (!(entity instanceof final LivingEntity living)) {
                return;
            }

            // LivingEntity#handleStatus(byte) case 2, but without the sound
            //living.limbDistance = 1.5f; TODO check this
            living.invulnerableTime = 20;
            living.hurtTime = living.hurtDuration = 10;
            //living.knockbackVelocity = 0f;
        });
    }
}
