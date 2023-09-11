package net.arna.jcraft.client.net;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.gui.ServerConfigUI;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.renderer.effects.AttackHitboxEffectRenderer;
import net.arna.jcraft.client.renderer.effects.TimeErasePredictionEffectRenderer;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.splatter.Splatter;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static net.arna.jcraft.registry.JPacketRegistry.*;
import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

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
    }

    private static void handleTimeStop(@NotNull MinecraftClient client, PacketByteBuf buf) {
        if (client.world == null || client.player == null) return;

        boolean isStart = buf.readBoolean();
        int entID = buf.readInt();

        if (isStart) {
            Vec3d position = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            RegistryKey<World> registryKey = buf.readRegistryKey(Registry.WORLD_KEY);
            int time = buf.readInt();

            client.execute(() -> {
                Entity ent = client.world.getEntityById(entID);
                if (!(ent instanceof LivingEntity livingEntity)) return;
                JClientUtils.activeTimestops.add( new DimValues(livingEntity, position, registryKey, time) );
            });
        } else JClientUtils.removeTimestop(entID);
    }

    private static void register(Identifier id, Consumer<PacketByteBuf> handler) {
        registerGlobalReceiver(id, (client, handler1, buf, responseSender) -> handler.accept(buf));
    }

    private static void register(Identifier id, BiConsumer<MinecraftClient, PacketByteBuf> handler) {
        registerGlobalReceiver(id, (client, handler1, buf, responseSender) -> handler.accept(client, buf));
    }

    public static void handleAnimation(@NotNull MinecraftClient client, PacketByteBuf buf) {
        if (client.world == null || client.player == null) return;

        int entID = buf.readInt();
        String animID = buf.readString(); // I know exactly how unoptimized this is, but I fail to care
        boolean isSpec = buf.readBoolean();

        //JCraft.LOGGER.info("JCRAFT CLIENT:\nRecieving animation packet of animID: " + animID + " for entity ID: " + entID);

        int moveStun;
        float animationSpeed;

        if (isSpec) {
            moveStun = buf.readInt();
            animationSpeed = buf.readFloat();
            //JCraft.LOGGER.info("Animation packet is for specs, and has attached moveStun: " + moveStun + " and attackID: " + attackID);
        } else {
            moveStun = 0;
            animationSpeed = 0f;
        }

        client.execute(() -> {
            Entity ent = client.world.getEntityById(entID);
            //JCraft.LOGGER.info("Animation is to be applied to: " + ent);
            if (ent instanceof PlayerEntity player) {
                // Animate
                ModifierLayer<IAnimation> animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(JCraft.id(animID));
                if (anim == null) {
                    JCraft.LOGGER.error("Tried to play null animation on player: " + player + ", in world " + client.world);
                    return;
                }

                // Remove last speed modifier, this is rather primitive but will do for now
                if ( animationContainer.size() > 0 )
                    animationContainer.removeModifier(0);

                // Synchronize spec values
                if (isSpec) {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec == null) {
                        JCraft.LOGGER.error("Tried to set spec animation values on player without spec: " + player + ", in world " + client.world);
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

    public static void handleChannelFeedback(@NotNull MinecraftClient client, PacketByteBuf buf) {
        if (client.world == null || client.player == null) return;

        short control = buf.readShort();
        switch (control) {
            // Attack hit boxes
            case (1) -> {
                int count = buf.readVarInt();

                List<Box> boxes = IntStream.range(0, count)
                        .mapToObj(i -> {
                            double minX = buf.readDouble();
                            double minY = buf.readDouble();
                            double minZ = buf.readDouble();

                            double maxX = buf.readDouble();
                            double maxY = buf.readDouble();
                            double maxZ = buf.readDouble();

                            return new Box(minX, minY, minZ, maxX, maxY, maxZ);
                        })
                        .toList();

                // Run on render thread to avoid concurrency issues.
                RenderSystem.recordRenderCall(() -> AttackHitboxEffectRenderer.addHitboxes(boxes));
            }

            // Time erase trackers
            case (2) -> {
                double posX = buf.readDouble();
                double posY = buf.readDouble();
                double posZ = buf.readDouble();
                double sizeX = MathHelper.clamp(buf.readDouble(), 0.1, 100);
                double sizeY = MathHelper.clamp(buf.readDouble(), 0.1, 100);
                double sizeZ = MathHelper.clamp(buf.readDouble(), 0.1, 100);

                client.execute(() -> {
                    Random random = new Random();

                    for (int h = 0; h < 8; ++h) {
                        client.world.addParticle(
                                JParticleTypeRegistry.KCPARTICLE,
                                posX + random.nextDouble(sizeX) - sizeX / 2,
                                posY + random.nextDouble(sizeY),
                                posZ + random.nextDouble(sizeZ) - sizeZ / 2,
                                0.0, 0.0, 0.0
                        );
                    }
                });
            }

            // Cooldown tracking
            case (3) -> {
                // TODO no longer needed, Cooldowns component is ticked on both sides.
//                int index = buf.readInt(); // Doesn't start at 0
//                double cd = buf.readDouble();
//                JCraftClient.clientCooldowns.set(index - 1, cd);
            }

            // KQ bomb tracker
            case (4) -> {
                double v1x = buf.readDouble();
                double v1y = buf.readDouble();
                double v1z = buf.readDouble();

                double v2x = buf.readDouble();
                double v2y = buf.readDouble();
                double v2z = buf.readDouble();

                boolean inRange = buf.readBoolean();

                Vec3d mid = new Vec3d(v1x, v1y + v2y / 2, v1z);

                client.execute(() -> {
                    Random random = new Random();
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

            // Spec synchronization
            case (5) -> {
                // TODO this should not be necessary anymore as the spec component is synced.
//                int specId = buf.readInt();
//
//                client.execute(() -> {
//                    JCraftSpec spec = SpecType.fromId(specId).createNew();
//
//                    if (spec != null)
//                        spec.player = client.player;
//
//                    ((ISpec)(client.player)).setClientSpec(spec);
//                });
            }

            // WAS Combo counter
            case (6) -> {

            }

            // Return to Zero trackers
            case (7) -> {
                int entID = buf.readInt();
                Vec3d originalPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

                client.execute(() -> {
                    Entity ent = client.world.getEntityById(entID);
                    if (ent == null) return;
                    Vec3d currentPos = ent.getEyePos();
                    Vec3d originalToCurrent = currentPos.subtract(originalPos).normalize();
                    for (double h = 0; h < currentPos.distanceTo(originalPos); ++h) {
                        client.world.addParticle(
                                ParticleTypes.ELECTRIC_SPARK,
                                originalPos.x + originalToCurrent.x * h, originalPos.y + originalToCurrent.y * h, originalPos.z + originalToCurrent.z * h,
                                -originalToCurrent.x, -originalToCurrent.y, -originalToCurrent.z
                        );
                    }
                });
            }

            // Generic single particle
            case (8) -> {
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                JParticleType particleType = buf.readEnumConstant(JParticleType.class);

                client.execute(() -> client.world.addParticle(particleType.getParticleType(), true, x, y, z,
                        0, 0, 0));
            }

            // Bites the Dust tracker
            case (9) -> {
                double v1x = buf.readDouble();
                double v1y = buf.readDouble();
                double v1z = buf.readDouble();

                double v2x = buf.readDouble();
                double v2y = buf.readDouble();
                double v2z = buf.readDouble();

                double oX = buf.readDouble();
                double oY = buf.readDouble();
                double oZ = buf.readDouble();

                boolean inRange = buf.readBoolean();

                Vec3d mid = new Vec3d(v1x, v1y + v2y / 2, v1z);

                client.execute(() -> {
                    Random random = new Random();

                    for (int h = 0; h < 16; ++h) {
                        double x = v1x + random.nextDouble(v2x) - v2x / 2;
                        double y = v1y + random.nextDouble(v2y);
                        double z = v1z + random.nextDouble(v2z) - v2z / 2;
                        Vec3d towardsVector = mid.subtract(x, y, z).normalize().multiply(0.1);

                        client.world.addParticle(
                                inRange ? ParticleTypes.WAX_OFF : ParticleTypes.GLOW,
                                x, y, z,
                                towardsVector.x, towardsVector.y, towardsVector.z);
                    }

                    for (int h = 0; h < 8; ++h) {
                        double x = oX + random.nextDouble(v2x) - v2x / 2;
                        double y = oY + random.nextDouble(v2y);
                        double z = oZ + random.nextDouble(v2z) - v2z / 2;

                        client.world.addParticle(
                                ParticleTypes.GLOW,
                                x, y, z,
                                0, 0, 0);
                    }
                });
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
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                double size = buf.readDouble();

                client.execute(() -> {
                    Random random = new Random();
                    for (int h = 0; h < size * 128; ++h) {
                        client.world.addParticle(
                                new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState()),
                                x + random.nextGaussian() * size,
                                y + random.nextGaussian() * size,
                                z + random.nextGaussian() * size,
                                0, 0, 0);
                    }
                });
            }

            // WAS Spec Animations
            case (12) -> {

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
                // TODO not necessary anymore. TimeStop component is synced
//                int entID = buf.readInt();
//                int ticks = buf.readInt();
//
//                client.execute(() -> {
//                    Entity ent = client.world.getEntityById(entID);
//                    if (ent == null) return;
//                    ((ITimeStop) ent).setTimeStopTicks(ticks);
//                });
            }
        }
    }

    public static void handleShaderActivation(@NotNull MinecraftClient client, PacketByteBuf buf) {
        int delay = buf.readInt();
        int duration = buf.readInt();
        ShaderActivationPacket.Type type = ShaderActivationPacket.Type.byName(buf.readString());
        World world = client.world;
        if (world == null) return;

        switch (type) {
            case NONE -> {
            }
            case ZA_WARUDO -> {
                int id = buf.readInt();
                client.execute(() -> {
                    Entity sourceShader = world.getEntityById(id);
                    if (sourceShader instanceof LivingEntity livingEntity) {
                        ZaWarudoShaderHandler zaWarudoShaderHandler = ZaWarudoShaderHandler.INSTANCE;
                        zaWarudoShaderHandler.shaderSourceEntity = Optional.of(livingEntity).orElse(client.player);
                        zaWarudoShaderHandler.effectLength = duration;
                        zaWarudoShaderHandler.shouldRender = true;
                    }
                });
            }
            case CRIMSON -> client.execute(() -> {
                if (!JClientConfig.getInstance().isTimeEraseShader()) return;
                CrimsonShaderHandler crimsonShaderHandler = CrimsonShaderHandler.INSTANCE;
                crimsonShaderHandler.effectLength = duration;
                crimsonShaderHandler.shouldRender = true;
            });
        }
    }

    public static void handleShaderDeactivation(@NotNull MinecraftClient client, PacketByteBuf buf) {
        ShaderActivationPacket.Type type = ShaderActivationPacket.Type.byName(buf.readString());
        World world = client.world;
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

    public static void handleTimeAccelState(@NotNull MinecraftClient client, PacketByteBuf buf) {
        TimeAccelStatePacket.State state = TimeAccelStatePacket.State.values()[buf.readVarInt()];
        Entity e = client.world == null ? null : client.world.getEntityById(buf.readVarInt());

        if (!(e instanceof MadeInHeavenEntity mih) || !mih.isAlive()) return;

        switch (state) {
            case START -> TimeAccelStatePacket.accelerations.put(mih.getId(), new TimeAccelStatePacket.TimeAcceleration(buf.readVarInt(), mih.getId()));
            case STOP -> TimeAccelStatePacket.accelerations.remove(mih.getId());
        }
    }

    public static void handleEpitaphOverlayState(PacketByteBuf buf) {
        boolean start = buf.readBoolean();
        if (start) EpitaphOverlay.start();
        else EpitaphOverlay.stop();
    }

    public static void handlePredictionState(@NotNull MinecraftClient client, PacketByteBuf buf) {
        boolean start = buf.readBoolean();
        int length = start ? buf.readVarInt() : 0;

        client.execute(() -> {
            if (start) TimeErasePredictionEffectRenderer.startEffect(length);
            else TimeErasePredictionEffectRenderer.stopEffect();
        });
    }

    public static void handleServerConfig(@NotNull MinecraftClient client, PacketByteBuf buf) {
        boolean editable = buf.readBoolean();
        boolean show = buf.readBoolean();

        ConfigOption.readOptions(buf);

        if (show) client.execute(() -> ServerConfigUI.show(editable));
    }

    private static void handleJExplosion(@NotNull MinecraftClient client, PacketByteBuf buf) {
        ExplosionS2CPacket nativePacket = new ExplosionS2CPacket(buf);
        JExplosionModifier modifier = buf.readBoolean() ? JExplosionModifier.read(buf) : null;

        client.execute(() -> {
            Explosion explosion = new Explosion(client.world, null, nativePacket.getX(), nativePacket.getY(), nativePacket.getZ(),
                    nativePacket.getRadius(), nativePacket.getAffectedBlocks());
            ((IJExplosion) explosion).jcraft$setModifier(modifier);
            explosion.affectWorld(true);
            Objects.requireNonNull(client.player).setVelocity(client.player.getVelocity()
                    .add(nativePacket.getPlayerVelocityX(), nativePacket.getPlayerVelocityY(), nativePacket.getPlayerVelocityZ()));
        });
    }

    private static void handleComboCounter(@NotNull MinecraftClient minecraftClient, PacketByteBuf buf) {
        JCraftClient.comboCounter = buf.readInt();
        if (JCraftClient.comboCounter == 1) JCraftClient.markComboStarted();

        JCraftClient.damageScaling = buf.readFloat();

        JCraftClient.framesSinceCounted = 0;
    }

    private static void handleSplatter(MinecraftClient client, PacketByteBuf buf) {
        ClientWorld world = client.world;
        if (world == null) return;

        Splatter splatter = JUtils.getSplatterManager(world).readSplatter(buf);

        long ageMs = splatter.getType().getMaxAge() * 50L;
        AttackHitboxEffectRenderer.addHitbox(splatter.getMainBox(), ageMs, true);
        splatter.getSections().stream()
                .filter(section -> !section.isRemoved())
                .forEach(section -> AttackHitboxEffectRenderer.addHitbox(section.getHitBox(), ageMs, true));
    }

    private static void handleStandHurt(MinecraftClient client, PacketByteBuf buf) {
        int entityId = buf.readVarInt();
        client.execute(() -> {
            if (client.world == null) return;

            Entity entity = client.world.getEntityById(entityId);
            if (!(entity instanceof LivingEntity living)) return;

            // LivingEntity#handleStatus(byte) case 2, but without the sound
            living.limbDistance = 1.5f;
            living.timeUntilRegen = 20;
            living.hurtTime = living.maxHurtTime = 10;
            living.knockbackVelocity = 0f;
        });
    }
}
