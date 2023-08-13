package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackQueue;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.entity.GEButterflyEntity;
import net.arna.jcraft.common.entity.GEFrogEntity;
import net.arna.jcraft.common.entity.GESnakeEntity;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GoldExperienceEntity extends StandEntity<GoldExperienceEntity, GoldExperienceEntity.State> {
    // JCraft.lightCooldown -> 0 | 0.5f -> 0.35f
    public static final Attack crm1 = new Attack(11, JCraft.lightCooldown * 4, 1.25f, 20, 16, 1.5, 4f, 0.75f, AttackType.BOX, 0.25f, 0.2f, 0, JSoundRegistry.IMPACT_4)
            .setInfo("Place Berry Bush", "places an almost-ripe berry bush on the ground, this move cannot be aimed up or down");
    public static final Attack light = new Attack(0, JCraft.lightCooldown / 2, 0.75f, 9, 6, 1.5, 5f, 0.75f, AttackType.BOX, 0.35f, -0.1f, 0, JSoundRegistry.IMPACT_1)
            .crouchingVariation(crm1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack heavy = new Attack(1, 14, 1f, 22, 13, 1.5, 9f, 1.5f, AttackType.BOX, 0.5f, 0, 0, JSoundRegistry.IMPACT_2)
            .setHitspark(2)
            .appendHitbox(new HitBoxData(0, 0, 1.25))
            .hyperArmor()
            .setLaunch()
            .setInfo("Shoulder Smash", "slow, uninterruptable combo finisher");
    public static final Attack barrage = Attack.barrageAttack(2, 14, 0.75f, 30, 0, 2, 1f, 0.25f, 1.5f, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack healself = new Attack(3, 26, 1f, 14, 10, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand", "standing: heals user for 2 hearts, crouching: heals others for 2 hearts, pacifies angered mobs");
    public static final Attack heal = new Attack(4, 26, 1f, 16, 10, 1.25, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand (Others)", "");
    public static final Attack tree = new Attack(5, 20, 1f, 24, 14, 1.75, 5f, 0.2f, AttackType.BOX, 0.75f, -0.1f, 0, JSoundRegistry.IMPACT_8)
            .setHitspark(2)
            .setInfo("Tree Summon", "two-hitting launch");
    public static final Attack lifegiver = new Attack(6, 36, 1f, 25, 16, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Life Giver",
                    """
                            STANDING: turns any stackable item into a snake, lasts for 25s and stuns for 0.5s on hit
                            CROUCHING: turns any stackable item into a frog, lasts for 15s and reflects damage, follows user
                            AERIAL: turns any item into a butterfly, lasts forever""");
    public static final Attack overclock = new Attack(10, 46, 1f, 31, 22, 2, 9f, 0.9f, AttackType.BOX, 3, 0, 0, JSoundRegistry.IMPACT_10)
            .setHitspark(2)
            .setUB(false)
            .setInfo("Overclock", "slow, unblockable, devastating stun");
    public static final Attack rekka3 = new Attack(9, 23, 1f, 24, 12, 2, 7f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegistry.TW_KICK_HIT)
            .setHitspark(2)
            .setInfo("Rekka (Final Hit)", "knockdown", AttackQueue.SPECIAL2);
    public static final Attack rekka2 = new Attack(8, 23, 1f, 18, 10, 1.75, 5f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegistry.IMPACT_2)
            .setHitspark(2)
            .setInfo("Rekka (2nd Hit)", "links into Light", AttackQueue.SPECIAL2);
    public static final Attack rekka1 = new Attack(7, 23, 1f, 20, 8, 1.5, 5f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegistry.IMPACT_2)
            .appendHitbox(new HitBoxData(1.25))
            .setFollowup(rekka2)
            .setInfo("Rekka Series", "a set of three attacks, which cancel into each other during recovery", AttackQueue.SPECIAL2);

    public GoldExperienceEntity(World worldIn) {
        super(StandType.GOLD_EXPERIENCE, worldIn, JSoundRegistry.GE_SUMMON);

        idleRotation = 0f;

        description = "Impenetrable Regenerative DEFENSE";

        pros = List.of(
                "good pressure",
                "above average speed",
                "excellent defense (tree, heal, snake, heavy)",
                "excellent setups"
        );

        cons = List.of(
                "low damage",
                "no horizontal movement tools",
                "snake is unreliable"
        );

        freespace = """
                BNBs:
                the giogio
                M1>Barrage>M1>Tree>Rekka 1~2~3
                the superprince of gaming
                Rekka 1~2>M1>Barrage>M1>Tree>Heavy""";

        moves = List.of(light, heavy, barrage, healself, overclock, rekka1, lifegiver, tree);

        super.initialize();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking())
            handleAttack(crm1, CooldownType.STAND_LIGHT, State.LIFEGIVER);
        else handleAttack(light, CooldownType.STAND_LIGHT, State.LIGHT);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        handleAttack(heavy, CooldownType.STAND_HEAVY, State.HEAVY);
        //this.playSound(ModSoundRegister.GE_HEAVY,1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE))
            playSound(JSoundRegistry.GE_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        CanAttackData data = this.canAttackWithData();
        if (!data.canAttack()) return;

        if (data.user().isSneaking()) {
            if (handleAttack(heal, CooldownType.STAND_SP1, State.HEAL)) playSound(JSoundRegistry.GE_HEAL, 1, 1);
        } else if (handleAttack(healself, CooldownType.STAND_SP1, State.HEAL_SELF)) playSound(JSoundRegistry.GE_HEAL, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        handleAttack(overclock, CooldownType.STAND_ULT, State.OVERCLOCK);
        //this.playSound(ModSoundRegister.GE_ULT, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (hasUser()) {
            LivingEntity user = getUserOrThrow();
            if (user.hasStatusEffect(JStatusRegistry.DAZED)) return;
            boolean idling = this.getMoveStun() < 1;

            if (curAttack != rekka1 && curAttack != rekka2 && curAttack != rekka3) {
                if (idling) {
                    if (handleAttack(rekka1, CooldownType.STAND_SP2, State.REKKA1))
                        playSound(JSoundRegistry.GE_REKKA1, 1, 1);
                    return;
                }
            }
            if (curAttack.id == rekka1.id && this.getMoveStun() < 12) {
                setAttack(rekka2, State.REKKA2);
                playSound(JSoundRegistry.GE_REKKA2, 1, 1);
            }
            if (curAttack.id == rekka2.id && this.getMoveStun() < 8) {
                setAttack(rekka3, State.REKKA3);
                playSound(JSoundRegistry.GE_REKKA3, 1, 1);
            }
        }
    }

    private enum LifeGiverType {
        SNAKE,
        FROG,
        BUTTERFLY
    }

    private LifeGiverType toSummon;

    @Override
    public void initSpecial3() {
        if (!canAttack() || !hasUser()) return;
        LivingEntity user = getUserOrThrow();

        toSummon = LifeGiverType.SNAKE;
        if (user.isOnGround()) {
            if (user.isSneaking()) toSummon = LifeGiverType.FROG;
        } else toSummon = LifeGiverType.BUTTERFLY;

        if (handleAttack(lifegiver, CooldownType.STAND_SP3, State.LIFEGIVER))
            playSound(JSoundRegistry.GE_HEAL, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;
        if (handleAttack(tree, CooldownType.UTIL, State.TREE))
            playSound(JSoundRegistry.GE_TREE, 1, 1);
    }

    /*
    @Override
    public boolean allowUtilityUse() { // Disables using the utility while sneaking, allowing menu control
        if (getUser().isSneaking()) return false;
        return super.allowUtilityUse();
    }
    @Environment(EnvType.CLIENT)
    boolean inMenu = false;
    @Override
    public void initClientUtility() {
        inMenu = true;
    }
     */

    private static final BlockState berryBush = Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 1);

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        switch (attack.id) {
            case (3) -> {
                if (user != null) user.heal(4f);
            }
            case (4) -> {
                for (LivingEntity ent : entities) {
                    ent.heal(4f);
                    ent.setAttacker(null);

                    if (ent instanceof MobEntity mob) {
                        stun(mob, 10, 0);
                        mob.setTarget(null);
                        mob.setAttacking(null);
                        if (mob instanceof Angerable angerable) angerable.stopAnger();
                    }
                }
            }
            case (5) -> {
                if (user == null) return;

                GETreeEntity tree = new GETreeEntity(JEntityTypeRegistry.GE_TREE, world);
                tree.setMaster(user);
                tree.copyPositionAndRotation(this);

                world.spawnEntity(tree);
            }
            case (6) -> {
                if (user == null) return;

                ItemStack item = user.getOffHandStack(); // Get offhand, or if unavailable main hand stack
                if (item.isEmpty()) item = user.getMainHandStack();
                if (item.isEmpty()) return;

                LivingEntity animal = null;
                ItemStack animalItem = item.copy();
                animalItem.setCount(1);
                switch (toSummon) {
                    case SNAKE -> {
                        if (item.getMaxCount() <= 1) return;

                        GESnakeEntity snake = new GESnakeEntity(JEntityTypeRegistry.GE_SNAKE, world);
                        //todo: fix snake not working for mobs
                        if (user instanceof PlayerEntity playerEntity) snake.setOwner(playerEntity);
                        animal = snake;
                    }
                    case FROG -> {
                        if (item.getMaxCount() <= 1) return;

                        GEFrogEntity frog = new GEFrogEntity(JEntityTypeRegistry.GE_FROG, world);
                        frog.setMaster(user);
                        animal = frog;
                    }
                    case BUTTERFLY -> {
                        GEButterflyEntity butterfly = new GEButterflyEntity(JEntityTypeRegistry.GE_BUTTERFLY, world);
                        butterfly.setMaster(user);
                        animal = butterfly;
                    }
                    default -> JCraft.LOGGER.error("Attempted to create Life Giver entity with invalid LifeGiverType: " + this);
                }

                if (animal == null) {
                    JCraft.LOGGER.error("Failed to create animal of type " + toSummon + " from item " + animalItem);
                    return;
                }
                item.decrement(1);
                animal.refreshPositionAndAngles(getX(), getY() + 0.5f, getZ(), getYaw(), getPitch());
                animal.setStackInHand(Hand.MAIN_HAND, animalItem);
                world.spawnEntity(animal);
            }
            case (9) -> {
                for (LivingEntity ent : entities) {
                    if (!JUtils.isBlocking(ent))
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 50, 0, true, false));
                }
            }
            case (10) -> {
                for (LivingEntity ent : entities) {
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 14, true, false));
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, 60, 1, true, false));
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.OUTOFBODY, 60, 0, false, true));
                }
            }
            case (11) -> {
                BlockPos blockPos = getBlockPos();
                if (world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.down()).isOpaque())
                    world.setBlockState(blockPos, berryBush);
            }
        }
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, Attack enemyAttack) {
        if (attack == lifegiver) {
            if (mob.getMainHandStack().isEmpty() && mob.getOffHandStack().isEmpty()) {
                return MoveSelectionResult.STOP;
            }
            return MoveSelectionResult.USE;
        }
        return MoveSelectionResult.PASS;
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.LIFEGIVER) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasUser()) return;

        if (!world.isClient && curAttack == rekka2 && queuedAttack == AttackQueue.SPECIAL2)
            queuedAttack = null;
    }

    // Animation code
    public enum State implements StandAnimationState<GoldExperienceEntity> {
        IDLE(builder -> builder.loop("animation.ge.idle")),
        LIGHT(builder -> builder.playAndHold("animation.ge.light")),
        BLOCK(builder -> builder.loop("animation.ge.block")),
        HEAVY(builder -> builder.playAndHold("animation.ge.heavy")),
        BARRAGE(builder -> builder.loop("animation.ge.barrage")),
        HEAL_SELF(builder -> builder.playAndHold("animation.ge.healself")),
        HEAL(builder -> builder.playAndHold("animation.ge.heal")),
        TREE(builder -> builder.playAndHold("animation.ge.tree")),
        LIFEGIVER(builder -> builder.playAndHold("animation.ge.lifegiver")),
        REKKA1(builder -> builder.playAndHold("animation.ge.rekka1")),
        REKKA2(builder -> builder.playAndHold("animation.ge.rekka2")),
        REKKA3(builder -> builder.playAndHold("animation.ge.rekka3")),
        OVERCLOCK(builder -> builder.playAndHold("animation.ge.overclock"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GoldExperienceEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Nullable
    @Override
    protected String getSummonAnimation() {
        return "animation.ge.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
