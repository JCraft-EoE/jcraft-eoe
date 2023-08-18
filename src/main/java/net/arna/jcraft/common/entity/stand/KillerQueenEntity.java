package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class KillerQueenEntity extends AbstractKillerQueenEntity<KillerQueenEntity, KillerQueenEntity.State> {
    public static final Attack heavy = new Attack(2, 12, 0.75f, 24, 16, 2, 9f, 1.75f, AttackType.BOX, 0.5f, 0, 0, JSoundRegistry.IMPACT_4)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Haymaker", "slow, uninterruptable launcher");
    public static final Attack sha = new Attack(5, 50, 20, 16, 0, AttackType.BOX)
            .setRanged(true)
            .setInfo("Sheer Heart Attack", "creates an automatic, heat-seeking sub-stand that explodes on contact, reflects 25% damage back to owner");
    public static final Attack grab = new Attack(7, 22, 0.75f, 20, 12, 1.75, 0, 0.1f, AttackType.BOX, 1f)
            .setGrab()
            .setInfo("Grab", "grabs opponent by the face, then detonates them, launching them upwards");
    public static final Attack grabhit = new Attack(8, 0, 20, 13, 0.75f, AttackType.BOX)
            .setGrab()
            .setInfo("Grab (Hit)", "");

    public KillerQueenEntity(World worldIn) {
        super(StandType.KILLER_QUEEN, worldIn, null);

        moves = List.of(light, heavy, barrage, bombplant, sha, grab,
                new Attack().setRanged(true).setInfo("Coin Toss", "overrides current bomb with an aimable coin"),
                new Attack().setMobility(MobilityType.DASH).setInfo("Explosive Dash", "slight aoe damage, 3D movement tool"));

        super.initialize();
    }

    // Move-set
    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;

        if (handleAttack(heavy, CooldownType.STAND_HEAVY, State.HEAVY)) {
            playSound(JSoundRegistry.KQ_UPPERCUT, 1, 1);
            playSound(JSoundRegistry.KQ_HEAVY, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE))
            playSound(JSoundRegistry.KQ_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack() || !hasUser()) return;

        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        if (user.isInSneakingPose() && cooldowns.getCooldown(CooldownType.STAND_SP1) < 1) {
            Block downBlock = world.getBlockState(user.getBlockPos().down()).getBlock();
            boolean notAir = downBlock != Blocks.AIR && downBlock != Blocks.CAVE_AIR && downBlock != Blocks.VOID_AIR;
            if (notAir) {
                bombEntity = null;
                bombBlock = user.getPos().add(0, -0.5, 0);
                cooldowns.setCooldown(CooldownType.STAND_SP1, bombplantCD);
            }
        } else {
            handleAttack(bombplant, CooldownType.STAND_SP1, State.BOMB_PLANT);
            bombBlock = null;
        }

        if (this.coin != null)
            this.coin.discard();
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        handleAttack(grab, CooldownType.STAND_SP2, State.GRAB);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack() || !hasUser()) return;

        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.STAND_SP3) > 0) return;
        cooldowns.setCooldown(CooldownType.STAND_SP3, 500); // 25s coin toss cd

        Vec3d lookVec = user.getRotationVector().multiply(0.75);
        if (this.coin != null) this.coin.discard();
        this.coin = new ItemEntity(world, user.getX(), user.getY() + user.getHeight() * 2 / 3, user.getZ(), new ItemStack(JObjectRegistry.KQCOIN, 1), lookVec.x, lookVec.y, lookVec.z);
        this.coin.setPickupDelayInfinite();

        world.spawnEntity(this.coin);

        this.bombEntity = this.coin;
        this.bombBlock = null;

        playSound(JSoundRegistry.COIN_TOSS, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        handleAttack(sha, CooldownType.STAND_ULT, State.SHA);
        //playSound(ModSoundRegister.KQ_SHA,1, 1);
    }

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        switch (attack.id) {
            case (4) -> {
                Entity target = entities.stream()
                        .findFirst()
                        .<Entity>map(JUtils::getUserIfStand)
                        .or(() -> {
                            // If none are found, re-do an optimized hitbox check for any entity type
                            Vec3d rotVec = getRotationVector();
                            Vec3d boxCenter = getPos().add(0, user.getHeight() / 2, 0).add(rotVec);
                            Vec3d halfBox = new Vec3d(0.5, 0.5, 0.5);
                            List<Entity> hit = world.getEntitiesByClass(Entity.class,
                                    new Box(boxCenter.subtract(halfBox), boxCenter.add(halfBox)),
                                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != user));
                            return !hit.isEmpty() ? Optional.of(hit.get(0)) : Optional.empty();
                        })
                        .orElse(null);

                if (target != null) {
                    bombEntity = target;
                    bombBlock = null;
                }
            }
            case (5) -> {
                SheerHeartAttackEntity sha = new SheerHeartAttackEntity(JEntityTypeRegistry.SHEER_HEART_ATTACK, world);
                sha.setMaster(user);
                sha.refreshPositionAndAngles(getX(), getY() + 0.5, getZ(), getYaw(), getPitch());

                world.spawnEntity(sha);
            }
            case (6) -> {
                if (bombEntity instanceof LivingEntity livingEntity) {
                    explode(user, livingEntity.getPos());
                } else {
                    Vec3d bombPos = null;

                    if (bombEntity != null) {
                        bombPos = bombEntity.getPos();
                        if (bombEntity instanceof ItemEntity)
                            bombEntity.kill();
                    }
                    if (bombBlock != null) bombPos = bombBlock;

                    if (bombPos != null)
                        explode(user, bombPos);
                }

                bombEntity = null;
                bombBlock = null;
            }
            case (7) -> {
                if (entities.isEmpty()) return;

                setAttack(grabhit, State.GRABHIT);
                bombEntity = entities.stream().findFirst().orElseThrow();
                bombBlock = null;
            }
            case (8) -> {
                playSound(JSoundRegistry.KQ_DETONATE, 1, 1);

                if (bombEntity instanceof LivingEntity livingEntity) {
                    ServerWorld serverWorld = (ServerWorld) world;

                    Vec3d pos = livingEntity.getPos();
                    JCraft.createParticle(serverWorld, pos.x, pos.y, pos.z,-5);
                    JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE, serverWorld, pos, 96);

                    DamageSource damageSource = JDamageSources.stand(this);

                    damageLogic(world, livingEntity, new Vec3d(0, 1, 0), 2, 3, true, 11f, false, 4, damageSource, user);
                    livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false));
                }

                bombEntity = null;
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
        LOW(builder -> builder.playAndHold("animation.killerqueen.low")),
        GRAB(builder -> builder.playAndHold("animation.killerqueen.grab")),
        GRABHIT(builder -> builder.playAndHold("animation.killerqueen.grab_hit"));


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
