package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class StarPlatinumEntity extends AbstractStarPlatinumEntity<StarPlatinumEntity, StarPlatinumEntity.State> {
    // Inhale
    public static final Attack inhale = new Attack(6, 50, 5, 5, 4, AttackType.BOX)
            .setUB(true)
            .setInfo("Inhale", "vacuums nearby entities for 4 seconds");
    private static final TrackedData<Integer> INHALE_TIME;

    static {
        INHALE_TIME = DataTracker.registerData(StarPlatinumEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public StarPlatinumEntity(World worldIn) {
        super(StandType.STAR_PLATINUM, State.class, worldIn);

        moves = List.of(light, heavy, barrage, starfinger, inhale, lowkick, starfinger, jump);

        super.initialize();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(INHALE_TIME, 0);
    }

    private void setInhaleTime(int time) {
        dataTracker.set(INHALE_TIME, time);
    }

    private int getInhaleTime() {
        return dataTracker.get(INHALE_TIME);
    }

    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking())
            handleAttack(crm1, JCraft.standLightCD, State.UPPERCUT);
        else handleAttack(light, JCraft.standLightCD, State.PUNCH);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, State.HEAVY))
            playSound(JSoundRegistry.STAR_BREAKER, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegistry.STAR_PLATINUM_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(starfinger, JCraft.standS1CD, State.STAR_FINGER))
            playSound(JSoundRegistry.STAR_FINGER, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(inhale, JCraft.standUltCD, State.INHALE)) {
            //playSound(JSoundRegister.STAR_SUCK, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(lowkick, JCraft.standS2CD, State.LOW_KICK))
            playSound(JSoundRegistry.STAR_PLATINUM_KICK, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        // Uses a copy because otherwise the main one gets overwritten by specialAttack()
        if (handleAttack(Attack.copyOf(chargebarrage), JCraft.standS3CD, State.BARRAGE))
            playSound(JSoundRegistry.STAR_PLATINUM_ADVANCING_BARRAGE, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser() || !getUserOrThrow().isOnGround()) return;
        handleAttack(jump, JCraft.utilCD, State.JUMP);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> {
                if (!hasUser()) return;
                LivingEntity user = getUserOrThrow();
                if (!user.isOnGround()) return;

                Vec3d jumpVel = getRotationVector().multiply(1.5).add(0, 0.5, 0);

                user.addVelocity(jumpVel.x, jumpVel.y, jumpVel.z);
                user.velocityModified = true;

                if (user instanceof ServerPlayerEntity player)
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            }
            case (5) -> {
                if (curAttack == null || entities.isEmpty()) return;
                Vec3d avgPos = Vec3d.ZERO;
                float c = 0;
                for (LivingEntity ent : entities) {
                    if (ent instanceof StandEntity) continue;
                    avgPos = avgPos.add(ent.getPos());
                    c += 1f;
                }
                avgPos = avgPos.multiply(1f / c);
                lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, avgPos);
                curAttack.attackDist = (float) avgPos.distanceTo(getPos());
            }
            case (6) -> setInhaleTime((int) (inhale.stun * 20));
            case (7) -> {
                for (LivingEntity living : entities) {
                    living.addVelocity(0, 0.25, 0);
                    living.velocityModified = true;
                    if (living instanceof ServerPlayerEntity serverPlayerEntity)
                        serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        Vec3d rotVec = getRotationVector();

        Vec3d fPos = getEyePos().add(rotVec.multiply(1.75));
        Vec3d ffPos = getEyePos().add(rotVec.multiply(3));

        if (world.isClient) {
            setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);

            if (getInhaleTime() > 0) {
                // Display particles for the two hitboxes
                Vec3d addVel = rotVec.add(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
                Vec3d particlePos = fPos.add(addVel);

                world.addParticle(ParticleTypes.POOF,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);

                addVel = rotVec.add(random.nextDouble() * 1.5 - 0.75, random.nextDouble() * 1.5 - 0.75, random.nextDouble() * 1.5 - 0.75);
                particlePos = ffPos.add(addVel);

                world.addParticle(ParticleTypes.POOF,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);
            }
        } else if (getInhaleTime() > 0) {
            setInhaleTime(getInhaleTime() - 1);

            if (getInhaleTime() > 0)
                setRotationOffset(90);
            else setRotationOffset(225);

            if (age % 2 != 0) return;
            List<Entity> filter = new ArrayList<>(List.of(this, user));
            if (user.hasVehicle()) filter.add(user.getVehicle());

            List<Entity> toInhale = (List<Entity>) JUtils.generateHitbox(world, fPos, 2, Entity.class, filter);
            List<? extends Entity> inhaleTip = JUtils.generateHitbox(world, ffPos, 1.5, Entity.class, filter);
            toInhale.addAll(inhaleTip);

            for (Entity entity : toInhale) {
                entity.setVelocity(entity.getVelocity()
                        .subtract(rotVec.x, 0, rotVec.z)
                        .multiply(0.2 * entity.distanceTo(this)));

                entity.velocityModified = true;

                if (entity instanceof ServerPlayerEntity player)
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<StarPlatinumEntity> {
        IDLE((starPlatinum, builder) -> builder.loop("animation.starplatinum." +
                (starPlatinum.getInhaleTime() > 0 ? "inhaleidle" : "idle"))),
        PUNCH(builder -> builder.playAndHold("animation.starplatinum.punch")),
        BLOCK(builder -> builder.loop("animation.starplatinum.block")),
        HEAVY(builder -> builder.playAndHold("animation.starplatinum.heavy")),
        BARRAGE(builder -> builder.loop("animation.starplatinum.barrage")),
        STAR_FINGER(builder -> builder.playAndHold("animation.starplatinum.star_finger")),
        INHALE(builder -> builder.playAndHold("animation.starplatinum.inhale")),
        LOW_KICK(builder -> builder.playAndHold("animation.starplatinum.low_kick")),
        JUMP(builder -> builder.playAndHold("animation.starplatinum.jump")),
        UPPERCUT(builder -> builder.playAndHold("animation.starplatinum.uppercut"));

        private final BiConsumer<StarPlatinumEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<StarPlatinumEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(StarPlatinumEntity stand, AnimationBuilder builder) {
            animator.accept(stand, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.starplatinum.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
