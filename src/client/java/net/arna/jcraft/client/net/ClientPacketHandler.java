package net.arna.jcraft.client.net;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.hud.EpitaphOverlay;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.JConfig;
import net.arna.jcraft.common.entity.MadeInHeavenEntity;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Random;

@UtilityClass
public class ClientPacketHandler {

    static {
        WorldRenderEvents.START.register(ctx -> {
            if (!ctx.world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) return;

            double acceleration = TimeAccelStatePacket.getAcceleration(ctx.world());

            long currentTime = Util.getMeasuringTimeMs();
            if (acceleration == 0) {
                TimeAccelStatePacket.lastUpdate = currentTime;
                return;
            }

            double multiplier = (currentTime - TimeAccelStatePacket.lastUpdate) / 1000d;
            ctx.world().setTimeOfDay((long) (ctx.world().getTimeOfDay() + acceleration * multiplier));

            TimeAccelStatePacket.lastUpdate = currentTime;
        });
    }

    public static void handleAnimation(MinecraftClient client, PacketByteBuf buf) {
        if (client == null || client.world == null || client.player == null) return;

        int entID = buf.readInt();
        String animID = buf.readString(); // I know exactly how unoptimized this is, but I fail to care
        boolean isSpec = buf.readBoolean();

        //JCraft.LOGGER.info("JCRAFT CLIENT:\nRecieving animation packet of animID: " + animID + " for entity ID: " + entID);

        int moveStun;
        int attackID;
        if (isSpec) {
            moveStun = buf.readInt();
            attackID = buf.readInt();
            //JCraft.LOGGER.info("Animation packet is for specs, and has attached moveStun: " + moveStun + " and attackID: " + attackID);
        } else {
            attackID = 0;
            moveStun = 0;
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
                //JCraft.LOGGER.info("Animation to be applied: " + anim);
                animationContainer.setAnimation(new KeyframeAnimationPlayer(anim));

                // Synchronize spec values
                if (isSpec) {
                    JCraftSpec spec = JClientUtils.getSpec(player);
                    if (spec == null) {
                        JCraft.LOGGER.error("Tried to set spec animation values on player without spec: " + player + ", in world " + client.world);
                        return;
                    }
                    //JCraft.LOGGER.info("Spec: " + spec.getName());
                    spec.moveStun = moveStun;
                    spec.attackID = attackID;
                }
            }
        });
    }

    public static void handleChannelFeedback(MinecraftClient client, PacketByteBuf buf) {
        if (client == null || client.world == null || client.player == null) return;

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
                int index = buf.readInt(); // Doesn't start at 0
                double cd = buf.readDouble();
                JCraftClient.clientCooldowns.set(index - 1, cd);
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
                int specID = buf.readInt();
                client.execute(() -> {
                    JCraftSpec spec = JUtils.getSpecByID(specID);

                    if (spec != null)
                        spec.player = client.player;

                    ((ISpec)(client.player)).setClientSpec(spec);
                });
            }

            // Combo counter
            case (6) -> {
                JCraftClient.comboCounter = buf.readInt();
                JCraftClient.ticksSinceCounted = 0;
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
                int id = buf.readInt();

                client.execute(() -> client.world.addParticle(
                        JParticleTypeRegistry.particles.get(id), true,
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
                int entID = buf.readInt();
                int ticks = buf.readInt();

                client.execute(() -> {
                    Entity ent = client.world.getEntityById(entID);
                    if (ent == null) return;
                    ((ITimeStop) ent).setTimeStopTicks(ticks);
                });
            }

            // TS Synchronization (see JCraft.java startTrackingTimestop())
            case (15) -> {
                int entID = buf.readInt();
                Vec3d position = new Vec3d( buf.readDouble(), buf.readDouble(), buf.readDouble() );
                RegistryKey<World> registryKey = buf.readRegistryKey(Registry.WORLD_KEY);
                int time = buf.readInt();

                client.execute(() -> {
                    Entity ent = client.world.getEntityById(entID);
                    if (ent == null) return;
                    JClientUtils.activeTimestops.add( new DimValues(ent, position, registryKey, time) );
                });
            }
        }
    }

    public static void handleShaderActivation(MinecraftClient client, PacketByteBuf buf) {
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
                if (!JConfig.TE_SHADER) return;
                CrimsonShaderHandler crimsonShaderHandler = CrimsonShaderHandler.INSTANCE;
                crimsonShaderHandler.effectLength = duration;
                crimsonShaderHandler.shouldRender = true;
            });
        }
    }

    public static void handleShaderDeactivation(MinecraftClient client, PacketByteBuf buf) {
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

    public static void handleTimeAccelState(MinecraftClient client, PacketByteBuf buf) {
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
}
