package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackQueue;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.JCraftUtils;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class GoldenExperienceEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(2, 0.75f, 9, 6, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static Attack heavy = new Attack(17, 1f, 22, 13, 1.5, 9f, 1.5f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.IMPACT_2).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Shoulder Smash", "slow, uninterruptable combo finisher");
    public static Attack barrage = new Attack(14, 0.75f, 30, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static Attack healself = new Attack(26, 1f, 14, 10, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand", "standing: heals user for 2 hearts, crouching: heals others for 2 hearts, pacifies angered mobs");
    public static Attack heal = new Attack(26, 1f, 16, 10, 0, 0f, 0f, AttackType.BOX);
    public static Attack tree = new Attack(20, 1f, 24, 14, 1.25, 5f, 0.2f, AttackType.BOX, 0.75f, -0.1f, 0, JSoundRegister.IMPACT_2).setHitspark(2)
            .setInfo("Tree Summon", "two-hitting launch");
    public static Attack snake = new Attack(36, 1f, 25, 16, 0, 0f, 0f, AttackType.BOX).setRanged(true)
            .setInfo("Snake Summon", "turns a held item into a snake which lasts for 25s and stuns for 0.5s on hit");
    public static Attack overclock = new Attack(46, 1f, 31, 22, 2, 9f, 0.9f, AttackType.BOX, 3, 0, 0, JSoundRegister.IMPACT_5).setHitspark(2)
            .setUB(false)
            .setInfo("Overclock", "slow, unblockable, devastating stun");
    public static Attack rekka3 = new Attack(23, 1f, 24, 12, 2, 7f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2);
    public static Attack rekka2 = new Attack(23, 1f, 18, 10, 1.75, 5f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_2)
            .setInfo("how did you", "get here", AttackQueue.SPECIAL2).setHitspark(2).setFollowup(rekka3);
    public static Attack rekka1 = new Attack(23, 1f, 20, 8, 1.5, 5f, 0.5f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_2)
            .setInfo("Rekka Series", "a set of three attacks, which cancel into each other during recovery", AttackQueue.SPECIAL2).setFollowup(rekka2);
    //todo: sounds on MR
    //todo: add skin select

    public GoldenExperienceEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
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

        freespace = "BNBs:\n" +
                "the giogio\n" +
                "M1>Barrage>M1>Tree>Rekka 1~2~3\n" +
                "the superprince of gaming\n" +
                "Rekka 1~2>M1>Barrage>M1>Tree>Heavy";

        moves = List.of(light, heavy, barrage, healself, overclock, rekka1, snake, tree);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) {
            return;
        }
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(heavy, JCraft.standHeavyCD, 4)) {
            //this.playSound(ModSoundRegister.STAR_BREAKER,1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.GE_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        CanAttackData data = this.canAttackWithData();
        if (!data.canAttack) {
            return;
        }
        if (data.user.isSneaking()) {
            if (handleAttack(heal, JCraft.standS1CD, 7)) {
                this.playSound(JSoundRegister.GE_HEAL, 1, 1);
            }
        } else if (handleAttack(healself, JCraft.standS1CD, 6)) {
            this.playSound(JSoundRegister.GE_HEAL, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(overclock, JCraft.standUltCD, 13)) {
            //this.playSound(ModSoundRegister.STAR_PLATINUM_THE_WORLD, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (user.hasStatusEffect(JStatusRegister.Dazed)) {
                return;
            }
            boolean idling = this.getMoveStun() < 1;

            if (this.curAttack != rekka1 && this.curAttack != rekka2 && this.curAttack != rekka3) {
                if (idling) {
                    handleAttack(rekka1, JCraft.standS2CD, 10);
                    return;
                }
            }
            if (this.curAttack == rekka1 && this.getMoveStun() < 12) {
                setAttack(rekka2, 11);
            }
            if (this.curAttack == rekka2 && this.getMoveStun() < 8) {
                setAttack(rekka3, 12);
            }
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(snake, JCraft.standS3CD, 9)) {
            this.playSound(JSoundRegister.GE_HEAL, 1, 1);
        }
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(tree, JCraft.standMMBCD, 8)) {
            this.playSound(JSoundRegister.GE_TREE, 1, 1);
        }
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (hasUser()) {
            LivingEntity user = this.getUser();
            if (attack == healself) {
                user.heal(4f);
            } else if (attack == heal) {
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
            } else if (attack == tree) {
                GETreeEntity tree = new GETreeEntity(JEntityTypeRegister.GE_TREE, world);
                tree.owner = user;
                tree.copyPositionAndRotation(this);
                this.world.spawnEntity(tree);
            } else if (attack == snake) {
                ItemStack item = user.getOffHandStack(); // Get offhand, or if unavailable main hand stack
                if (item.isEmpty()) {
                    item = user.getMainHandStack();
                }
                if (item.isEmpty()) {
                    return;
                }

                //todo: fix snake not working for mobs
                GESnakeEntity snake = new GESnakeEntity(JEntityTypeRegister.GE_SNAKE, world);
                if (user instanceof PlayerEntity playerEntity) {
                    snake.setOwner(playerEntity);
                }

                ItemStack snakeItem = item.copy();
                snakeItem.setCount(1);
                snake.refreshPositionAndAngles(this.getX(), this.getY() + 0.5f, this.getZ(), this.getYaw(), this.getPitch());
                snake.setStackInHand(Hand.MAIN_HAND, snakeItem);
                item.decrement(1);
                this.world.spawnEntity(snake);
            } else if (attack == rekka3) {
                for (LivingEntity ent :
                        entities) {
                    if (!JCraftUtils.isBlocking(ent)) {
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.Knockdown, 50, 0, true, false));
                    }
                }
            } else if (attack == overclock) {
                for (LivingEntity ent :
                        entities) {
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 14, true, false));
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.Dazed, 60, 1, true, false));
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.OutOfBody, 60, 0, false, true));
                }
            }
        }
    }

    @Override
    public MoveSelectionResult SpecificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity enemyStand, Attack enemyAttack) {
        if (attack == snake) {
            if (mob.getMainHandStack().isEmpty() && mob.getOffHandStack().isEmpty()) {
                return MoveSelectionResult.STOP;
            }
            return MoveSelectionResult.USE;
        }
        return MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.GE_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();

        if (hasUser()) {
            if (world.isClient) {
                this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
            } else if (this.curAttack == rekka2 && this.queuedAttack == AttackQueue.SPECIAL2) {
                this.queuedAttack = null;
            }
        }
    }

    // Animation code
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
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
