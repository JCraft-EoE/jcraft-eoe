package net.arna.jcraft.client.util;

import lombok.NonNull;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.particle.AuraArcParticle;
import net.arna.jcraft.client.particle.AuraBlobParticle;
import net.arna.jcraft.client.particle.MoshParticle;
import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.*;
import net.arna.jcraft.common.entity.vehicle.AbstractGroundVehicleEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.IClientEntityHandler;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class ClientEntityHandlerImpl implements IClientEntityHandler {
    public static final ClientEntityHandlerImpl INSTANCE = new ClientEntityHandlerImpl();

    private ClientEntityHandlerImpl() {
    }

    @Override
    public void bombTrackerParticleTick(final Entity entity, final CommonBombTrackerComponent.BombData bombData) {
        final Vec3 bombPos = bombData.getBombPos();
        if (bombPos == null) {
            return;
        }
        final ClientLevel clientWorld = (ClientLevel) entity.level();

        SimpleParticleType particleType = ParticleTypes.WITCH; // Far particle
        final Vec3 v1 = bombPos.add(3, 3, 3);
        final Vec3 v2 = bombPos.add(-3, -3, -3);
        final List<LivingEntity> list = clientWorld.getEntitiesOfClass(LivingEntity.class, new AABB(v1, v2), EntitySelector.LIVING_ENTITY_STILL_ALIVE);

        double xLength = 0, yLength = 0, zLength = 0;
        if (!bombData.isBlock) {
            Entity bombEntity = bombData.bombEntity;
            if (bombEntity == null) {
                bombEntity = bombData.bombItem.getEntityRepresentation();
            }
            if (bombEntity == null) {
                return;
            }
            list.remove(bombEntity);
            xLength = bombEntity.getBoundingBox().getXsize();
            yLength = bombEntity.getBoundingBox().getYsize();
            zLength = bombEntity.getBoundingBox().getZsize();
        }

        for (LivingEntity l : list) {
            if (l.distanceToSqr(bombPos) < 9) {
                particleType = ParticleTypes.WAX_ON; // Near particle
                break;
            }
        }

        final RandomSource random = clientWorld.getRandom();

        //TODO: fix bomb particle rendering in other gravities
        if (bombData.isEntity) {
            for (int h = 0; h < 16; ++h) {
                clientWorld.addParticle(particleType,
                        bombPos.x + random.triangle(0, 1) * xLength,
                        bombPos.y + random.triangle(0, 1) * yLength,
                        bombPos.z + random.triangle(0, 1) * zLength,
                        0, 0, 0);
            }
        }

        if (bombData.isBlock) {
            for (int h = 0; h < 16; ++h) {
                clientWorld.addParticle(particleType,
                        bombPos.x + random.nextDouble(),
                        bombPos.y + random.nextDouble(),
                        bombPos.z + random.nextDouble(),
                        0, 0, 0);
            }
        }
    }

    @Override
    public void standEntityClientTick(final StandEntity<?, ?> stand) {
        // This won't be called if the stand has no user; see StandEntity#tick()
        final LivingEntity user = stand.getUserOrThrow();
        final Minecraft client = Minecraft.getInstance();

        // Stand Auras
        if (JClientConfig.getInstance().isStandAuras() && JClientUtils.shouldRenderStands()) {
            if (stand.distanceToSqr(client.player) > 6400) {
                return; // 5 chunk aura render distance
            }
            if (user.isInvisible() || stand.isInvisible()) {
                return;
            }

            final boolean isFP = client.options.getCameraType().isFirstPerson();
            final boolean isOwnerAndFP = user == client.player && isFP;

            final ClientLevel clientWorld = (ClientLevel) stand.level();
            final RandomSource random = clientWorld.getRandom();

            final Direction gravity = GravityChangerAPI.getGravityDirection(stand);

            final Vector3f auraColor = stand.getAuraColor();

            /*
            Basically,
            any stand you do not own should have an Aura drawn.
            Stands you do own should have an aura drawn EITHER if you are not in first person, or it is detached.
             */

            if ((!isOwnerAndFP || stand.isFree())
                    && !(stand.isRemoteAndControllable() && isFP)
                    && random.nextBoolean()) {
                displayAuraParticles(clientWorld,
                        random,
                        stand,
                        RotationUtil.vecPlayerToWorld(stand.getBbWidth(), stand.getBbHeight(), stand.getBbWidth(), gravity),
                        gravity,
                        auraColor
                );
            }
            if (!isOwnerAndFP && random.nextBoolean() && !JClientUtils.shouldNotRender(user)) {
                displayAuraParticles(clientWorld,
                        random,
                        user,
                        RotationUtil.vecPlayerToWorld(user.getBbWidth(), user.getBbHeight(), user.getBbWidth(), gravity),
                        gravity,
                        auraColor
                );
            }

            if (stand instanceof MetallicaEntity metallica && !metallica.isInvisible() && !isOwnerAndFP && !JClientUtils.shouldNotRender(user)) {
                final boolean doingMove = !metallica.isIdle() && !metallica.isBlocking();
                if (random.nextInt(doingMove ? 2 : 4) == 0) {
                    final int count = doingMove ? (1 + random.nextInt(2)) : 1;
                    final Vector3f userBox = RotationUtil.vecPlayerToWorld(user.getBbWidth(), user.getBbHeight(), user.getBbWidth(), gravity);
                    displayMoshParticles(clientWorld, random, user, userBox, count, metallica.getMoshColor());
                }
            }
        }
    }

    private static final double metersPerTickSquared = 9.81 / 400;

    private void displayAuraParticles(final ClientLevel clientWorld, final RandomSource random, final Entity entity,
                                      final Vector3f maxBox, final Direction gravity, final Vector3f color) {
        displayAuraParticles(clientWorld, random, entity, maxBox, gravity, color, false);
    }
    private void displayAuraParticles(final ClientLevel clientWorld, final RandomSource random, final Entity entity,
                                            final Vector3f maxBox, final Direction gravity, final Vector3f color, final boolean overrideNoRender) {
        if (!overrideNoRender && JClientUtils.shouldNotRender(entity)) {
            return;
        }

        final Vec3 pos = entity.position();
        final Vec3 vel = Vec3.atLowerCornerOf(gravity.getNormal()).scale(-metersPerTickSquared);
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

        clientWorld.addParticle(JParticleTypeRegistry.AURA_ARC.get(), false,
                pos.x + maxBox.x() * random.triangle(0, 1),
                pos.y + maxBox.y() * random.triangle(0.5, 0.5),
                pos.z + maxBox.z() * random.triangle(0, 1),
                vel.x, vel.y, vel.z);

        clientWorld.addParticle(JParticleTypeRegistry.AURA_BLOB.get(), false,
                pos.x + maxBox.x() * random.triangle(0, 1),
                pos.y + maxBox.y() * random.triangle(0.5, 0.5),
                pos.z + maxBox.z() * random.triangle(0, 1),
                vel.x, vel.y, vel.z);
    }

    @Override
    public void displayMetallicaAura(MetallicaEntity metallica) {
        final LivingEntity user = metallica.getUser();
        if (user == null || !JClientUtils.shouldRenderStands()) return;

        final Direction gravity = GravityChangerAPI.getGravityDirection(metallica);
        final Vector3f auraColor = metallica.getAuraColor();
        final ClientLevel clientWorld = (ClientLevel) metallica.level();
        final RandomSource random = metallica.getRandom();

        displayAuraParticles(
                clientWorld,
                random,
                metallica,
                RotationUtil.vecPlayerToWorld(user.getBbWidth(), user.getBbHeight(), user.getBbWidth(), gravity),
                gravity,
                auraColor,
                true
        );

        if (random.nextBoolean()) {
            displayMoshParticles(clientWorld, random, metallica,
                    RotationUtil.vecPlayerToWorld(user.getBbWidth(), user.getBbHeight(), user.getBbWidth(), gravity),
                    1 + random.nextInt(2), metallica.getMoshColor());
        }
    }

    private void displayMoshParticles(ClientLevel clientWorld, RandomSource random, Entity entity,
                                      Vector3f maxBox, int count, Vector3f color) {
        MoshParticle.Factory.parent = entity;
        MoshParticle.Factory.color = color;
        final Vec3 pos = entity.position();
        final int typeIndex = random.nextInt(JParticleTypeRegistry.MOSH_TYPES.size());
        for (int i = 0; i < count; i++) {
            clientWorld.addParticle(JParticleTypeRegistry.MOSH_TYPES.get(typeIndex).get(), false,
                    pos.x + maxBox.x() * random.triangle(0, 2),
                    pos.y + maxBox.y() * random.triangle(0.5, 0.5),
                    pos.z + maxBox.z() * random.triangle(0, 2),
                    0, 0, 0);
        } //TODO: make particle spawn faster relative to the iron bar (less iron less particles & vice versa)
    }

    @Override
    public void spawnGroundedMoshParticles(AbstractArrow projectile) {
        if (!JClientConfig.getInstance().isStandAuras()) {
            return;
        }
        if (!JUtils.shouldRenderStandsFor(Minecraft.getInstance().player)) {
            return;
        }

        final Entity owner = projectile.getOwner();
        if (!(owner instanceof LivingEntity living)) {
            return;
        }
        if (owner != Minecraft.getInstance().player) {
            return;
        }
        final var stand = JUtils.getStand(living);
        if (!(stand instanceof MetallicaEntity metallica)) {
            return;
        }

        final Vec3 pos = projectile.position();
        final Vector3f color = metallica.getMoshColor();
        MoshParticle.Factory.color = color;
        final ClientLevel clientWorld = (ClientLevel) projectile.level();
        final RandomSource random = clientWorld.getRandom();
        final int typeIndex = random.nextInt(JParticleTypeRegistry.MOSH_TYPES.size());
        clientWorld.addParticle(JParticleTypeRegistry.MOSH_TYPES.get(typeIndex).get(), false,
                pos.x + random.triangle(0, 0.2),
                pos.y + 0.5 + random.triangle(0, 0.2),
                pos.z + random.triangle(0, 0.2),
                0, 0, 0);
    }

    @Override
    public void whiteSnakeRemoteClientTick(final @NonNull WhiteSnakeEntity whiteSnakeEntity) {
        final Minecraft client = Minecraft.getInstance();
        if (JUtils.getStand(client.player) != whiteSnakeEntity) {
            return;
        }

        final Options options = client.options;
        float f = 0, s = 0;
        boolean jump = options.keyJump.isDown();
        if (options.keyUp.isDown()) {
            f += 1.0f;
        }
        if (options.keyDown.isDown()) {
            f += 1.0f;
        }
        if (options.keyLeft.isDown()) {
            s -= 1.0f;
        }
        if (options.keyRight.isDown()) {
            s += 1.0f;
        }

        //JCraft.LOGGER.info("Handling remote movement for: " + whiteSnakeEntity + " with " + f + " " + s + " " + jump);
        whiteSnakeEntity.tickRemoteMovement(f, s, jump);
    }

    @Override
    public void purpleHazeRemoteClientTick(final @NonNull AbstractPurpleHazeEntity<?, ?> purpleHazeEntity) {
        final Minecraft client = Minecraft.getInstance();
        if (JUtils.getStand(client.player) != purpleHazeEntity) {
            return;
        }

        final Options options = client.options;
        float f = 0, s = 0;
        boolean jump = options.keyJump.isDown();
        if (options.keyUp.isDown()) {
            f += 1.0f;
        }
        if (options.keyDown.isDown()) {
            f += 1.0f;
        }
        if (options.keyLeft.isDown()) {
            s -= 1.0f;
        }
        if (options.keyRight.isDown()) {
            s += 1.0f;
        }

        //JCraft.LOGGER.info("Handling remote movement for: " + purpleHazeEntity + " with " + f + " " + s + " " + jump);
        purpleHazeEntity.tickRemoteMovement(f, s, jump);
    }

    @Override
    public void hierophantGreenRemoteClientTick(final @NonNull HGEntity hgEntity) {
        final Minecraft client = Minecraft.getInstance();
        if (JUtils.getStand(client.player) != hgEntity) {
            return;
        }

        final Options options = client.options;
        float f = 0, s = 0;
        final boolean jump = options.keyJump.isDown();
        final boolean sneak = options.keyShift.isDown();
        if (options.keyUp.isDown()) {
            f += 1.0f;
        }
        if (options.keyDown.isDown()) {
            f += 1.0f;
        }
        if (options.keyLeft.isDown()) {
            s -= 1.0f;
        }
        if (options.keyRight.isDown()) {
            s += 1.0f;
        }

        hgEntity.tickRemoteMovement(f, s, jump, sneak);
    }

    @Override
    public void sheerHeartAttackEntityTick(final SheerHeartAttackEntity sHAEntity) {
        final Minecraft client = Minecraft.getInstance();
        UUID ownerId = sHAEntity.getOwnerId();
        if (ownerId == null) {
            return;
        }
        if (ownerId.equals(client.player.getUUID()) && sHAEntity.tickCount <= 300) {
            sHAEntity.setCustomName(Component.literal(15 - sHAEntity.tickCount / 20 + "s"));
        }
    }

    @Override
    public void vehicleMovementTick(final AbstractGroundVehicleEntity vehicle) {
        final Minecraft client = Minecraft.getInstance();
        if (client.player == vehicle.getFirstPassenger()) {
            final Options options = client.options;

            final boolean
                    w = options.keyUp.isDown(),
                    a = options.keyLeft.isDown(),
                    s = options.keyDown.isDown(),
                    d = options.keyRight.isDown(),
                    jump = options.keyJump.isDown(),
                    sneak = options.keyShift.isDown();

            vehicle.movementTick(w, a, s, d, jump, sneak);
        }
    }
}
