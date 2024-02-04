package net.arna.jcraft.client.util;

import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.particle.AuraArcParticle;
import net.arna.jcraft.client.particle.AuraBlobParticle;
import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.IClientEntityHandler;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientEntityHandlerImpl implements IClientEntityHandler {
    public static final ClientEntityHandlerImpl INSTANCE = new ClientEntityHandlerImpl();

    private ClientEntityHandlerImpl() {}

    @Override
    public void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData) {
        ClientWorld clientWorld = (ClientWorld) entity.getWorld();

        Vec3d bombPos = bombData.getBombPos();
        if (bombPos == null) return;

        double width = 1.0;
        double height = 1.0;

        if (!bombData.isBlock) {
            Entity bombEntity = bombData.bombEntity;
            if (bombEntity == null)
                bombEntity = bombData.bombItem.getHolder();
            if (bombEntity == null)
                return;
            width = bombEntity.getWidth();
            height = bombEntity.getHeight();
        }

        DefaultParticleType particleType = ParticleTypes.WITCH; // Far particle
        Vec3d v1 = bombPos.add(3, 3, 3);
        Vec3d v2 = bombPos.add(-3, -3, -3);
        List<LivingEntity> list = clientWorld.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);
        if (bombData.isEntity) list.remove(bombData.bombEntity);
        for (LivingEntity l : list)
            if (l.squaredDistanceTo(bombPos) < 9) {
                particleType = ParticleTypes.WAX_ON; // Near particle
                break;
            }

        Random random = clientWorld.getRandom();

        if (bombData.isEntity) {
            Direction gravity = GravityChangerAPI.getGravityDirection(entity);
            for (int h = 0; h < 16; ++h) {
                Vec3d randOffset = RotationUtil.vecPlayerToWorld(
                        new Vec3d(
                                random.nextTriangular(0, 0.5) * width,
                                random.nextDouble() * height,
                                random.nextTriangular(0, 0.5) * width
                        ),
                        gravity
                );
                clientWorld.addParticle(particleType,
                        bombPos.x + randOffset.x,
                        bombPos.y + randOffset.y,
                        bombPos.z + randOffset.z,
                        0, 0, 0);
            }
        }

        if (bombData.isBlock)
            for (int h = 0; h < 16; ++h)
                clientWorld.addParticle(particleType,
                        bombPos.x + random.nextDouble(),
                        bombPos.y + random.nextDouble(),
                        bombPos.z + random.nextDouble(),
                        0, 0, 0);
    }

    @Override
    public void standEntityClientTick(StandEntity<?, ?> stand) {
        LivingEntity user = stand.getUser();
        if (user == null) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Stand Auras
        if (JClientConfig.getInstance().isStandAuras()) {
            if (stand.squaredDistanceTo(client.player) > 6400) return; // 5 chunk aura render distance
            if (user.isInvisible()) return;

            boolean isFP = client.options.getPerspective().isFirstPerson();
            boolean isOwnerAndFP = user == client.player && isFP;

            ClientWorld clientWorld = (ClientWorld) stand.getWorld();
            Random random = clientWorld.getRandom();

            Direction gravity = GravityChangerAPI.getGravityDirection(stand);

            Vec3f auraColor = stand.getAuraColor();

            if ( (!isOwnerAndFP || stand.isFree())
                    && !(stand.isRemote() && isFP)
                            && random.nextBoolean() )
                displayAuraParticles(clientWorld, random, stand, RotationUtil.vecPlayerToWorld(stand.getWidth(), stand.getHeight(), stand.getWidth(), gravity), gravity, auraColor);
            if ( !isOwnerAndFP && random.nextBoolean() && !JClientUtils.shouldNotRender(user) )
                displayAuraParticles(clientWorld, random, user, RotationUtil.vecPlayerToWorld(user.getWidth(), user.getHeight(), user.getWidth(), gravity), gravity, auraColor);
        }
    }

    private static final double metersPerTickSquared = 9.81 / 400;
    private void displayAuraParticles(ClientWorld clientWorld, Random random, Entity entity, Vec3f maxBox, Direction gravity, Vec3f color) {
        if (JClientUtils.shouldNotRender(entity)) return;

        Vec3d pos = entity.getPos();
        Vec3d vel = Vec3d.of(gravity.getVector()).multiply(-metersPerTickSquared);
        /*
        Vec3d vel = entity.getVelocity();
        if (entity instanceof ClientPlayerEntity)
            vel = entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ);
        vel = vel.subtract(Vec3d.of(gravity.getVector()).multiply(metersPerTickSquared));
         */

        // minecraft is single-threaded :)
        AuraArcParticle.Factory.parent = entity;
        AuraArcParticle.Factory.color = color;
        AuraBlobParticle.Factory.parent = entity;
        AuraBlobParticle.Factory.color = color;

        clientWorld.addParticle(JParticleTypeRegistry.AURA_ARC, false,
                pos.x + maxBox.getX() * random.nextTriangular(0, 1),
                pos.y + maxBox.getY() * random.nextTriangular(0.5, 0.5),
                pos.z + maxBox.getZ() * random.nextTriangular(0, 1),
                vel.x, vel.y, vel.z);

        clientWorld.addParticle(JParticleTypeRegistry.AURA_BLOB, false,
                pos.x + maxBox.getX() * random.nextTriangular(0, 1),
                pos.y + maxBox.getY() * random.nextTriangular(0.5, 0.5),
                pos.z + maxBox.getZ() * random.nextTriangular(0, 1),
                vel.x, vel.y, vel.z);
    }

    @Override
    public void playerCloneEntityClientTick(PlayerCloneEntity entity) {

    }

    @Override
    public void whiteSnakeRemoteClientTick(@NotNull WhiteSnakeEntity whiteSnakeEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (JUtils.getStand(client.player) != whiteSnakeEntity) return;

        GameOptions options = client.options;
        float f = 0, s = 0;
        boolean jump = options.jumpKey.isPressed();
        if (options.forwardKey.isPressed())
            f += 1.0f;
        if (options.backKey.isPressed())
            f += 1.0f;
        if (options.leftKey.isPressed())
            s -= 1.0f;
        if (options.rightKey.isPressed())
            s += 1.0f;

        //JCraft.LOGGER.info("Handling remote movement for: " + whiteSnakeEntity + " with " + f + " " + s + " " + jump);
        whiteSnakeEntity.tickRemoteMovement(f, s, jump);
    }
}
