package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class StarPlatinumEntity extends AbstractStarPlatinumEntity<StarPlatinumEntity, StarPlatinumEntity.State> {

    public StarPlatinumEntity(World worldIn) {
        super(StandType.STAR_PLATINUM, State.class, worldIn);
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
