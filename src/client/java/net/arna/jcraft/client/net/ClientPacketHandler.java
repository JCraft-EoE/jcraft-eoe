package net.arna.jcraft.client.net;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.rendering.handler.CrimsonShaderHandler;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.arna.jcraft.common.entity.MadeInHeavenEntity;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.common.util.IJCraftAnimatedPlayer;
import net.arna.jcraft.common.util.ITimeStop;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
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

    public static void handleChannelFeedback(MinecraftClient client, PacketByteBuf buf) {
        if (client.world == null) return;

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

            // WS acid spew
            case (5) -> {
                double epx = buf.readDouble();
                double epy = buf.readDouble();
                double epz = buf.readDouble();

                double hpx = buf.readDouble();
                double hpy = buf.readDouble();
                double hpz = buf.readDouble();

                client.execute(() -> {
                    Random random = new Random();
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
                JCraftClient.comboCounter = buf.readInt();
                JCraftClient.ticksSinceCounted = 0;
            }

            // WAS ANUBIS SWINGS, NOW OPEN
            case (7) -> {
                /*
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
                 */
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

            // Spec Animations
            case (12) -> {
                int entID = buf.readInt();
                String animPath = buf.readString(); // I know exactly how unoptimized this is, but I fail to care

                client.execute(() -> {
                    Entity ent = client.world.getEntityById(entID);
                    if (ent instanceof PlayerEntity player) {
                        ModifierLayer<IAnimation> animationContainer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
                        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(new Identifier(JCraft.MOD_ID, "animation." + animPath));
                        if (anim == null) {
                            JCraft.LOGGER.error("Tried to play null animation on player " + player + " in world " + client.world);
                            return;
                        }
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
                    if (ent == null) return;
                    ((ITimeStop) ent).setTimeStopTicks(ticks);
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
}
