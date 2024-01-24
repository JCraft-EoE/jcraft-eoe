package net.arna.jcraft.client.util;

import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.particle.AuraArcParticle;
import net.arna.jcraft.client.particle.AuraBlobParticle;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.IClientEntityHandler;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;

public class ClientEntityHandlerImpl implements IClientEntityHandler {
    public static final ClientEntityHandlerImpl INSTANCE = new ClientEntityHandlerImpl();

    private ClientEntityHandlerImpl() {}

    @Override
    public void standEntityClientTick(StandEntity<?, ?> stand) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        // Stand Auras
        if (JClientConfig.getInstance().isStandAuras()) {
            LivingEntity user = stand.getUser();
            if (user == null) return;

            boolean isOwnerAndFP = user == minecraftClient.player && minecraftClient.options.getPerspective().isFirstPerson();

            ClientWorld clientWorld = (ClientWorld) stand.getWorld();
            Random random = clientWorld.getRandom();

            Direction gravity = GravityChangerAPI.getGravityDirection(stand);

            Vec3f auraColor = stand.getAuraColor();

            if ( (!isOwnerAndFP || stand.isFree()) && random.nextBoolean() )
                displayAuraParticles(clientWorld, random, stand, RotationUtil.vecPlayerToWorld(stand.getWidth(), stand.getHeight(), stand.getWidth(), gravity), gravity, auraColor);
            if ( !isOwnerAndFP && random.nextBoolean() )
                displayAuraParticles(clientWorld, random, user, RotationUtil.vecPlayerToWorld(user.getWidth(), user.getHeight(), user.getWidth(), gravity), gravity, auraColor);
        }
    }

    private static final double metersPerTickSquared = 9.81 / 400;
    private void displayAuraParticles(ClientWorld clientWorld, Random random, Entity entity, Vec3f maxBox, Direction gravity, Vec3f color) {
        Vec3d pos = entity.getPos();
        Vec3d vel = entity.getVelocity();
        if (entity instanceof ClientPlayerEntity)
            vel = entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ);
        vel = vel.subtract(Vec3d.of(gravity.getVector()).multiply(metersPerTickSquared));

        // minecraft is single-threaded :)
        AuraArcParticle.Factory.color = color;
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
}
