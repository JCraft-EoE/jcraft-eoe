package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public final class KillerQueenEntity extends AbstractKillerQueenEntity<KillerQueenEntity, KillerQueenEntity.State> {

    public KillerQueenEntity(World worldIn) {
        super(StandType.KILLER_QUEEN, State.class, worldIn);
    }

    // Move-set
    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;

        if (handleAttack(heavy, JCraft.standHeavyCD, State.HEAVY)) {
            playSound(JSoundRegister.KQ_UPPERCUT, 1, 1);
            playSound(JSoundRegister.KQ_HEAVY, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegister.KQ_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack() || !hasUser()) return;

        LivingEntity user = getUserOrThrow();
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();
        if (user.isInSneakingPose() && userData.getInt(JCraft.standS1CD) < 1) {
            BlockPos downBlock = user.getBlockPos().down();
            boolean notAir = (world.getBlockState(downBlock).getBlock() != Blocks.AIR && world.getBlockState(downBlock).getBlock() != Blocks.CAVE_AIR &&
                    world.getBlockState(downBlock).getBlock() != Blocks.VOID_AIR);
            if (notAir) {
                bombEntity = null;
                bombBlock = user.getPos().add(0, -0.5, 0);
                userData.putInt(JCraft.standS1CD, bombplantCD);
            }
        } else {
            handleAttack(bombplant, JCraft.standS1CD, State.BOMB_PLANT);
            bombBlock = null;
        }

        if (this.coin != null)
            this.coin.discard();
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        handleAttack(sha, JCraft.standS2CD, State.SHA);
        //playSound(ModSoundRegister.KQ_SHA,1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack() || !hasUser()) return;

        LivingEntity user = getUserOrThrow();
        NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
        if (playerData.getInt(JCraft.standS3CD) > 0) return;

        Vec3d lookVec = user.getRotationVector().multiply(0.75);
        if (this.coin != null) this.coin.discard();
        this.coin = new ItemEntity(world, user.getX(), user.getY() + user.getHeight() * 2 / 3, user.getZ(), new ItemStack(JObjectRegistry.KQCOIN, 1), lookVec.x, lookVec.y, lookVec.z);
        this.coin.setPickupDelayInfinite();

        world.spawnEntity(this.coin);

        this.bombEntity = this.coin;
        this.bombBlock = null;

        playSound(JSoundRegister.COIN_TOSS, 1, 1);
        playerData.putInt(JCraft.standS3CD, 500); // 25s coin toss cd
        playerData.putInt(JCraft.standUltCD, 20); // 1s detonate cd (prevents IUB)
    }

    @Override
    public void initUlt() {
        //todo: KQ ULT
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        switch (attack.id) {
            case (4) -> {
                if (!entities.isEmpty()) { // Living entities take priority
                    bombEntity = entities.get(0);
                    bombBlock = null;
                } else { // If none are found, re-do an optimized hitbox check for any entity type
                    Vec3d rotVec = getRotationVector();
                    Vec3d boxCenter = getPos().add(0, user.getHeight() / 2, 0).add(rotVec);
                    Vec3d halfBox = new Vec3d(0.5, 0.5, 0.5);
                    List<Entity> hit = world.getEntitiesByClass(Entity.class,
                            new Box(boxCenter.subtract(halfBox), boxCenter.add(halfBox)),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != user));

                    if (!hit.isEmpty()) {
                        bombEntity = hit.get(0);
                        bombBlock = null;
                    }
                }
            }
            case (5) -> {
                SheerHeartAttackEntity sha = new SheerHeartAttackEntity(JEntityTypeRegister.SHEER_HEART_ATTACK, world);
                sha.setMaster(user);
                sha.refreshPositionAndAngles(getX(), getY() + 0.5, getZ(), getYaw(), getPitch());

                world.spawnEntity(sha);
            }
            case (6) -> {
                if (bombEntity instanceof LivingEntity livingEntity) {

                    JUtils.explode(
                            world, user,
                            livingEntity.getX(),
                            livingEntity.getY() + livingEntity.getHeight() / 2,
                            livingEntity.getZ(),
                            2f,
                            JExplosionModifier.builder()
                                    .particle(JParticleTypeRegistry.BOOM_1)
                                    .destructionType(Explosion.DestructionType.NONE)
                                    .particleVelocity(Vec3d.ZERO)
                                    .build()
                    );

                    livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));
                } else {
                    Vec3d bombPos = null;

                    if (bombEntity != null) {
                        bombPos = bombEntity.getPos();
                        if (bombEntity instanceof ItemEntity)
                            bombEntity.kill();
                    }
                    if (bombBlock != null) bombPos = bombBlock;

                    if (bombPos != null) {
                        JUtils.explode(
                                world, user,
                                bombPos.getX(),
                                bombPos.getY(),
                                bombPos.getZ(),
                                2f,
                                JExplosionModifier.builder()
                                        .particle(JParticleTypeRegistry.BOOM_1)
                                        .destructionType(Explosion.DestructionType.NONE)
                                        .particleVelocity(Vec3d.ZERO)
                                        .build()
                        );

                        List<LivingEntity> toKD = world.getEntitiesByClass(
                                LivingEntity.class,
                                new Box(bombPos.add(2.2, 2.2, 2.2), bombPos.add(-2.2, -2.2, -2.2)),
                                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
                        );

                        for (LivingEntity livingEntity : toKD)
                            livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));
                    }
                }

                bombEntity = null;
                bombBlock = null;
            }
        }
    }

    // Animations
    public enum State implements StandAnimationState<KillerQueenEntity> {
        IDLE(builder -> builder.loop("animation.killerqueen.idle")),
        LIGHT(builder -> builder.playAndHold("animation.killerqueen.light")),
        BLOCK(builder -> builder.loop("animation.killerqueen.block")),
        HEAVY(builder -> builder.playAndHold("animation.killerqueen.heavy")),
        BARRAGE(builder -> builder.loop("animation.killerqueen.barrage")),
        DETONATE(builder -> builder.playAndHold("animation.killerqueen.detonate")),
        BOMB_PLANT(builder -> builder.playAndHold("animation.killerqueen.bombplant")),
        SHA(builder -> builder.playAndHold("animation.killerqueen.sha")),
        LOW(builder -> builder.playAndHold("animation.killerqueen.low"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KillerQueenEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.killerqueen.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }

    @Override
    protected State getLightState() {
        return State.LIGHT;
    }

    @Override
    protected State getLowState() {
        return State.LOW;
    }

    @Override
    protected State getDetonateState() {
        return State.DETONATE;
    }
}
