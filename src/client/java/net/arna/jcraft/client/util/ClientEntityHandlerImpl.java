package net.arna.jcraft.client.util;

import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.particle.AuraArcParticle;
import net.arna.jcraft.client.particle.AuraBlobParticle;
import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.*;
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
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class ClientEntityHandlerImpl implements IClientEntityHandler {
    public static final ClientEntityHandlerImpl INSTANCE = new ClientEntityHandlerImpl();

    private ClientEntityHandlerImpl() {}

    @Override
    public void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData) {
        Vec3d bombPos = bombData.getBombPos();
        if (bombPos == null) return;
        ClientWorld clientWorld = (ClientWorld) entity.getWorld();

        DefaultParticleType particleType = ParticleTypes.WITCH; // Far particle
        Vec3d v1 = bombPos.add(3, 3, 3);
        Vec3d v2 = bombPos.add(-3, -3, -3);
        List<LivingEntity> list = clientWorld.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);

        double xLength = 0, yLength = 0, zLength = 0;
        if (!bombData.isBlock) {
            Entity bombEntity = bombData.bombEntity;
            if (bombEntity == null)
                bombEntity = bombData.bombItem.getHolder();
            if (bombEntity == null)
                return;
            list.remove(bombEntity);
            xLength = bombEntity.getBoundingBox().getXLength();
            yLength = bombEntity.getBoundingBox().getYLength();
            zLength = bombEntity.getBoundingBox().getZLength();
        }

        for (LivingEntity l : list)
            if (l.squaredDistanceTo(bombPos) < 9) {
                particleType = ParticleTypes.WAX_ON; // Near particle
                break;
            }

        Random random = clientWorld.getRandom();

        //TODO: fix bomb particle rendering in other gravities
        if (bombData.isEntity)
            for (int h = 0; h < 16; ++h)
                clientWorld.addParticle(particleType,
                        bombPos.x + random.nextTriangular(0, 1) * xLength,
                        bombPos.y + random.nextTriangular(0, 1) * yLength,
                        bombPos.z + random.nextTriangular(0, 1) * zLength,
                        0, 0, 0);

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

            Vector3f auraColor = stand.getAuraColor();

            if ( (!isOwnerAndFP || stand.isFree())
                    && !(stand.isRemoteAndControllable() && isFP)
                            && random.nextBoolean() )
                displayAuraParticles(clientWorld, random, stand, RotationUtil.vecPlayerToWorld(stand.getWidth(), stand.getHeight(), stand.getWidth(), gravity), gravity, auraColor);
            if ( !isOwnerAndFP && random.nextBoolean() && !JClientUtils.shouldNotRender(user) )
                displayAuraParticles(clientWorld, random, user, RotationUtil.vecPlayerToWorld(user.getWidth(), user.getHeight(), user.getWidth(), gravity), gravity, auraColor);
        }
    }

    private static final double metersPerTickSquared = 9.81 / 400;
    private void displayAuraParticles(ClientWorld clientWorld, Random random, Entity entity, Vector3f maxBox, Direction gravity, Vector3f color) {
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
                pos.x + maxBox.x() * random.nextTriangular(0, 1),
                pos.y + maxBox.y() * random.nextTriangular(0.5, 0.5),
                pos.z + maxBox.z() * random.nextTriangular(0, 1),
                vel.x, vel.y, vel.z);

        clientWorld.addParticle(JParticleTypeRegistry.AURA_BLOB, false,
                pos.x + maxBox.x() * random.nextTriangular(0, 1),
                pos.y + maxBox.y() * random.nextTriangular(0.5, 0.5),
                pos.z + maxBox.z() * random.nextTriangular(0, 1),
                vel.x, vel.y, vel.z);
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

    @Override
    public void purpleHazeRemoteClientTick(@NotNull AbstractPurpleHazeEntity<?, ?> purpleHazeEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (JUtils.getStand(client.player) != purpleHazeEntity) return;

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

        //JCraft.LOGGER.info("Handling remote movement for: " + purpleHazeEntity + " with " + f + " " + s + " " + jump);
        purpleHazeEntity.tickRemoteMovement(f, s, jump);
    }

    @Override
    public void hierophantGreenRemoteClientTick(@NotNull HGEntity hgEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (JUtils.getStand(client.player) != hgEntity) return;

        GameOptions options = client.options;
        float f = 0, s = 0;
        boolean jump = options.jumpKey.isPressed();
        boolean sneak = options.sneakKey.isPressed();
        if (options.forwardKey.isPressed())
            f += 1.0f;
        if (options.backKey.isPressed())
            f += 1.0f;
        if (options.leftKey.isPressed())
            s -= 1.0f;
        if (options.rightKey.isPressed())
            s += 1.0f;

        hgEntity.tickRemoteMovement(f, s, jump, sneak);
    }

    @Override
    public void sheerHeartAttackEntityTick(SheerHeartAttackEntity sHAEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID ownerId = sHAEntity.getOwnerId();
        if (ownerId == null) return;
        if (ownerId.equals(client.player.getUuid()) && sHAEntity.age <= 300)
            sHAEntity.setCustomName( Text.literal(15 - sHAEntity.age / 20 + "s") );
    }
}
