package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.pose.ModifierCondition;
import net.arna.jcraft.api.pose.PoseModifiers;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.pose.modifier.PoseModifierGroup;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.ExplosiveDashAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.KQDetonateAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

public abstract sealed class AbstractKillerQueenEntity<E extends AbstractKillerQueenEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits KillerQueenEntity, KQBTDEntity {
    public static final Supplier<IPoseModifier> POSE = () -> PoseModifierGroup.builder()
            .modifier(PoseModifierGroup.builder()
                    .condition(ModifierCondition.USER_NOT_MOVING)
                    .modifier(PoseModifiers.parse("""
                            leftArm.yRot += 15deg;
                            leftArm.xRot -= 15deg;
                            leftArm.zRot += 45deg;
                            """, ModifierCondition.LEFT_ARM_EMPTY))
                    .modifier(PoseModifiers.parse("""
                            rightArm.yRot -= 15deg;
                            rightArm.xRot -= 15deg;
                            rightArm.zRot -= 45deg;
                            """, ModifierCondition.RIGHT_ARM_EMPTY))
                    .build())
            .modifier(PoseModifiers.parse("""
                    body.xRot -= 5deg;
                    leftLeg.z -= 1;
                    rightLeg.z -= 1;
                    """))
            .build();

    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LOW = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            0, 8, 13, 0.85f, 4f, 10, 1.5f, 0.25f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Low Punch"),
                    Component.literal("frametrap tool, low stun")
            );
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT_FOLLOWUP = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            0, 6, 13, 0.8f, 3f, 20, 1.5f, 0.5f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            // implemented in class: .withFollowup(LOW)
            .withLookTracking()
            .withInfo(
                    Component.literal("Second Punch"),
                    Component.literal("frametrap tool")
            );
    public static final KQDetonateAttack<AbstractKillerQueenEntity<?, ?>> DETONATE = new KQDetonateAttack<AbstractKillerQueenEntity<?, ?>>(20, 5, 6, 1f)
            .withInfo(
                    Component.literal("Detonate"),
                    Component.literal("tiny windup, move queueing is disabled while Detonate is active")
            );
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            25, 6, 10, 0.75f, 3f, 10, 1.5f, 0.25f, 0.1f)
            .noLoopPrevention()
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withCrouchingVariant(DETONATE)
            // implemented in class: .withFollowup(LIGHT_FOLLOWUP)
            .withLookTracking()
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("combo starter, decent speed, has two followups")
            );
    public static final MainBarrageAttack<AbstractKillerQueenEntity<?, ?>> BARRAGE = new MainBarrageAttack<AbstractKillerQueenEntity<?, ?>>(
            240, 0, 40, 0.75f, 1f, 20, 2f, 0.1f, 0, 3, Blocks.DEEPSLATE.defaultDestroyTime())
            .withSound(JSoundRegistry.KQ_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, medium stun")
            );
    public static final BombPlantAttack<AbstractKillerQueenEntity<?, ?>> BOMB_PLANT = new BombPlantAttack<AbstractKillerQueenEntity<?, ?>>(140, 12, 20, 1f, 9, 1.5f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withBlockStun(8)
            .withInfo(
                    Component.literal("Bomb Plant"),
                    Component.literal("crouch to plant on the ground below you, stealthily")
            );
    public static final ExplosiveDashAttack<AbstractKillerQueenEntity<?, ?>> EXPLOSIVE_DASH = new ExplosiveDashAttack<AbstractKillerQueenEntity<?, ?>>(240)
            .withInfo(
                    Component.literal("Explosive Dash"),
                    Component.literal("instantly boosts the user in the aimed direction")
            );
    protected ItemEntity coin;

    protected AbstractKillerQueenEntity(StandType type, Level worldIn) {
        super(type, worldIn);
    }

    protected void detonate() {
        if (canAttack()) {
            setMove(DETONATE, getDetonateState());
            playSound(JSoundRegistry.KQ_DETONATE.get(), 1, 1);
        }
    }

    // Moveset
    @Override
    @SuppressWarnings("unchecked") // we checked :)
    public boolean initMove(MoveClass moveClass) {
        final LivingEntity user = getUserOrThrow();
        switch (moveClass) {
            case LIGHT -> {
                boolean idling = getMoveStun() <= 0;
                if (getCurrentMove() == null || getCurrentMove().getFollowup() == null) {
                    if (idling) {
                        if (user.isShiftKeyDown()) {
                            detonate();
                        } else {
                            return super.initMove(MoveClass.LIGHT);
                        }
                    }
                } else if (getMoveStun() < getCurrentMove().getWindupPoint()) {
                    if (user.isShiftKeyDown()) {
                        detonate();
                    } else {
                        final AbstractMove<?, ? super E> followup = getCurrentMove().getFollowup();
                        setMove(followup, (S) followup.getAnimation());
                    }
                }

                return true;
            }

            case SPECIAL1 -> {
                final CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);

                if (user.isCrouching() && cooldowns.getCooldown(CooldownType.STAND_SP1) <= 0) {
                    final BlockPos standingOn = user.blockPosition().relative(GravityChangerAPI.getGravityDirection(user));
                    if (!level().getBlockState(standingOn).isAir()) {
                        JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(standingOn);
                        cooldowns.setCooldown(CooldownType.STAND_SP1, BOMB_PLANT.getCooldown());
                    }

                    return true;
                } else {
                    return handleMove(MoveClass.SPECIAL1);
                }
            }

            default -> {
                return super.initMove(moveClass);
            }
        }
    }

    @Override
    public void desummon() {
        if (coin != null) {
            coin.discard();
        }
        super.desummon();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super E> attack, LivingEntity mob, LivingEntity target, int stunTicks,
                                                                                  int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        if (enemyStand != null && enemyStand.blocking) {
            return MoveSelectionResult.STOP;
        }
        return super.specificMoveSelectionCriterion(attack, mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
    }

    @Override
    public void tick() {
        super.tick();

        if (hasUser()) {
            if (getCurrentMove() instanceof KQDetonateAttack) {
                queuedMove = null;
            }
        }
    }

    // Animation code
    protected abstract S getDetonateState();
}
