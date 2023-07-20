package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackQueue;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class GoldenExperienceEntity extends StandEntity {
    public static final Attack light = new Attack(0, 2, 0.75f, 9, 6, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static final Attack heavy = new Attack(1, 17, 1f, 22, 13, 1.5, 9f, 1.5f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .appendHitbox(new Attack.HitboxData(0, 0, 1.25))
            .hyperArmor()
            .setLaunch()
            .setInfo("Shoulder Smash", "slow, uninterruptable combo finisher");
    public static final Attack barrage = new Attack(2, 14, 0.75f, 30, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack healself = new Attack(3, 26, 1f, 14, 10, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand", "standing: heals user for 2 hearts, crouching: heals others for 2 hearts, pacifies angered mobs");
    public static final Attack heal = new Attack(4, 26, 1f, 16, 10, 1.25, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand (Others)", "");
    public static final Attack tree = new Attack(5, 20, 1f, 24, 14, 1.25, 5f, 0.2f, AttackType.BOX, 0.75f, -0.1f, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .setInfo("Tree Summon", "two-hitting launch");

    //todo: convert lifegiver to move with followup
    public static final Attack lifegiver = new Attack(6, 36, 1f, 25, 16, 0, 0f, 0f, AttackType.BOX).setRanged(true)
            .setInfo("Life Giver",
                    """
                            turns a held item into a:
                            STANDING: snake which lasts for 25s and stuns for 0.5s on hit
                            CROUCHING: frog which lasts for 15s and reflects damage while following you""");
    public static final Attack overclock = new Attack(10, 46, 1f, 31, 22, 2, 9f, 0.9f, AttackType.BOX, 3, 0, 0, JSoundRegister.IMPACT_5)
            .setHitspark(2)
            .setUB(false)
            .setInfo("Overclock", "slow, unblockable, devastating stun");
    public static final Attack rekka3 = new Attack(9, 23, 1f, 24, 12, 2, 7f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.TW_KICK_HIT)
            .setHitspark(2)
            .setInfo("Rekka (Final Hit)", "knockdown", AttackQueue.SPECIAL2);
    public static final Attack rekka2 = new Attack(8, 23, 1f, 18, 10, 1.75, 5f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .setFollowup(rekka3)
            .setInfo("Rekka (2nd Hit)", "links into Light", AttackQueue.SPECIAL2);
    public static final Attack rekka1 = new Attack(7, 23, 1f, 20, 8, 1.5, 5f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_2)
            .appendHitbox(new Attack.HitboxData(1.25))
            .setInfo("Rekka Series", "a set of three attacks, which cancel into each other during recovery", AttackQueue.SPECIAL2)
            .setFollowup(rekka2);

    public GoldenExperienceEntity(World worldIn) {
        super(StandType.GOLD_EXPERIENCE, worldIn);
        super.initialize();
        idleRotation = 0f;

        description = "Impenetrable Regenerative DEFENSE";

        pros = List.of(
                "amazing pressure",
                "above average speed",
                "excellent defense (tree, heal, snake, heavy)"
        );

        cons = List.of(
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
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        handleAttack(heavy, JCraft.standHeavyCD, 4);
        //this.playSound(ModSoundRegister.GE_HEAVY,1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            this.playSound(JSoundRegister.GE_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        CanAttackData data = this.canAttackWithData();
        if (!data.canAttack) return;
        if (data.user.isSneaking()) {
            if (handleAttack(heal, JCraft.standS1CD, 7)) {
                playSound(JSoundRegister.GE_HEAL, 1, 1);
            }
        } else if (handleAttack(healself, JCraft.standS1CD, 6)) {
            playSound(JSoundRegister.GE_HEAL, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        handleAttack(overclock, JCraft.standUltCD, 13);
        //this.playSound(ModSoundRegister.GE_ULT, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (user.hasStatusEffect(JStatusRegister.DAZED)) return;
            boolean idling = this.getMoveStun() < 1;

            if (curAttack != rekka1 && curAttack != rekka2 && curAttack != rekka3) {
                if (idling) {
                    if (handleAttack(rekka1, JCraft.standS2CD, 10)) {
                        playSound(JSoundRegister.GE_REKKA1, 1, 1);
                    }
                    return;
                }
            }
            if (curAttack.id == rekka1.id && this.getMoveStun() < 12) {
                setAttack(rekka2, 11);
                playSound(JSoundRegister.GE_REKKA2, 1, 1);
            }
            if (curAttack.id == rekka2.id && this.getMoveStun() < 8) {
                setAttack(rekka3, 12);
                playSound(JSoundRegister.GE_REKKA3, 1, 1);
            }
        }
    }

    private enum LifeGiverType {
        SNAKE,
        FROG,
        FISH
    }

    private LifeGiverType toSummon;

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        toSummon = getUser().isSneaking() ? LifeGiverType.FROG : LifeGiverType.SNAKE;
        if (handleAttack(lifegiver, JCraft.standS3CD, 9))
            this.playSound(JSoundRegister.GE_HEAL, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;
        if (handleAttack(tree, JCraft.utilCD, 8))
            this.playSound(JSoundRegister.GE_TREE, 1, 1);
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

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        switch (attack.id) {
            case (3) -> user.heal(4f);
            case (4) -> {
                for (LivingEntity ent :
                        entities) {
                    ent.heal(4f);
                    ent.setAttacker(null);

                    if (ent instanceof MobEntity mob) {
                        stun(mob, 10, 0);
                        mob.setTarget(null);
                        mob.setAttacking(null);
                        if (mob instanceof Angerable angerable) {
                            angerable.stopAnger();
                        }
                    }
                }
            }
            case (5) -> {
                GETreeEntity tree = new GETreeEntity(JEntityTypeRegister.GE_TREE, world);
                tree.owner = user;
                tree.refreshPositionAndAngles(getX(), getY(), getZ(), getYaw(), 0);
                this.world.spawnEntity(tree);
            }
            case (6) -> {
                ItemStack item = user.getOffHandStack(); // Get offhand, or if unavailable main hand stack
                if (item.isEmpty()) item = user.getMainHandStack();
                if (item.isEmpty()) return;

                LivingEntity animal = null;
                ItemStack animalItem = item.copy();
                animalItem.setCount(1);
                if (toSummon == LifeGiverType.SNAKE) {
                    GESnakeEntity snake = new GESnakeEntity(JEntityTypeRegister.GE_SNAKE, world);
                    //todo: fix snake not working for mobs
                    if (user instanceof PlayerEntity playerEntity) snake.setOwner(playerEntity);
                    animal = snake;
                }
                if (toSummon == LifeGiverType.FROG) {
                    GEFrogEntity frog = new GEFrogEntity(JEntityTypeRegister.GE_FROG, world);
                    frog.setMaster(user);
                    animal = frog;
                }

                if (animal == null) {
                    JCraft.LOGGER.error("Failed to create animal of type " + toSummon + " from item " + animalItem);
                    return;
                }
                item.decrement(1);
                animal.refreshPositionAndAngles(this.getX(), this.getY() + 0.5f, this.getZ(), this.getYaw(), this.getPitch());
                animal.setStackInHand(Hand.MAIN_HAND, animalItem);
                world.spawnEntity(animal);
            }
            case (9) -> {
                for (LivingEntity ent :
                        entities) {
                    if (!JUtils.isBlocking(ent)) {
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 50, 0, true, false));
                    }
                }
            }
            case (10) -> {
                for (LivingEntity ent :
                        entities) {
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 14, true, false));
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.DAZED, 60, 1, true, false));
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.OUTOFBODY, 60, 0, false, true));
                }
            }
        }
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        if (attack == lifegiver) {
            if (mob.getMainHandStack().isEmpty() && mob.getOffHandStack().isEmpty()) {
                return MoveSelectionResult.STOP;
            }
            return MoveSelectionResult.USE;
        }
        return MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.GE_SUMMON, 1f, 1f);

        super.tick();
        if (!hasUser()) return;

        if (world.isClient)
            setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
        else if (curAttack == rekka2 && queuedAttack == AttackQueue.SPECIAL2)
            queuedAttack = null;
    }

    // Animation code
    final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (this.getSameState()) controller.markNeedsReload();
        switch (this.getState()) {

            default -> controller.setAnimation(builder.loop("animation.ge.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.ge.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.ge.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.ge.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.ge.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.ge.healself"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.ge.heal"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.ge.tree"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.ge.snake"));

            case 10 -> controller.setAnimation(builder.playAndHold("animation.ge.rekka1"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.ge.rekka2"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.ge.rekka3"));

            case 13 -> controller.setAnimation(builder.playAndHold("animation.ge.overclock"));

            //default -> throw new IllegalStateException("Unexpected value: " + this.getState());
        }

        return PlayState.CONTINUE;
    }
}
