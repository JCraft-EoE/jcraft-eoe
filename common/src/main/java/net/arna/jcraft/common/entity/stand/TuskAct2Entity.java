package net.arna.jcraft.common.entity.stand;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.conditions.TuskNailCondition;
import net.arna.jcraft.common.attack.moves.tusk.GoldenRectangleNailAttack;
import net.arna.jcraft.common.attack.moves.tusk.PerfectGoldenRotationAttack;
import net.arna.jcraft.common.attack.moves.tusk.TuskActCycleMove;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;

public class TuskAct2Entity extends StandEntity<TuskAct2Entity, TuskAct2Entity.State> {
    public static final MoveSet<TuskAct2Entity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.TUSK_ACT_2,
            TuskAct2Entity::registerMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleRotation(315f)
            .blockDistance(0f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.tusk_act2"))
                    .proCount(3)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                            Tusk Act 2 is an evolution stand.
                            Contains up to 10 nails.
                            Direct upgrade in nail shots
                            Layerable with specs.
                            
                            Utility: Cycle acts
                            - Click: Next act (1→2→3→1)
                            - Shift+Click: Previous act (1→3→2→1)"""))
                    .build())
            .summonData(SummonData.of(() -> null))
            .build();

    public static final EntityDataAccessor<Float> NAILS = SynchedEntityData.defineId(TuskAct2Entity.class, EntityDataSerializers.FLOAT);
    public static final float NAILS_MAX = 10.0f;

    public static final GoldenRectangleNailAttack GOLDEN_RECTANGLE_NAIL = new GoldenRectangleNailAttack(0, 10, 15, 0.75f, 2.5f, 15.0f, 8.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInfo(
                    Component.literal("Golden Rectangle Nail"),
                    Component.literal("Fires a spinning nail (15 blocks). On hit, creeps 8 blocks through terrain towards nearby enemies."));

    public static final PerfectGoldenRotationAttack PERFECT_GOLDEN_ROTATION = new PerfectGoldenRotationAttack(500, 10, 100, 0.75f, 2.0f, 20.0f)
            .withInfo(
                    Component.literal("Perfect Golden Rotation"),
                    Component.literal("Hold to charge. Fires a drilling nail that hits once per tick. Damage (6-12) and speed (1x-2x) increase with charge."));

    public static final TuskActCycleMove<TuskAct2Entity> ACT_CYCLE = new TuskActCycleMove<TuskAct2Entity>(
            0, 0, 1, 0f, 2, false)
            .withInfo(
                    Component.literal("Cycle Act"),
                    Component.literal("Cycle through unlocked Tusk acts.\n" +
                            "Utility → Cycle forward (Act 1→2→3→1...)\n" +
                            "Shift + Utility → Cycle backward (Act 3→2→1→3...)\n" +
                            "Only cycles through unlocked acts.\n" +
                            "Nail count is preserved when switching.")
            );

    @Getter
    @Setter
    private CommonMiscComponent miscComponent;

    public TuskAct2Entity(Level worldIn) {
        super(JStandTypeRegistry.TUSK_ACT_2.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.4f, 0.8f),
                new Vector3f(0.8f, 0.2f, 0.6f),
                new Vector3f(0.9f, 0.3f, 0.7f),
                new Vector3f(1.0f, 0.5f, 0.9f)
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(NAILS, NAILS_MAX);
    }

    @Override
    public void setUser(@Nullable LivingEntity user) {
        super.setUser(user);
        if (user == null) return;
        miscComponent = JComponentPlatformUtils.getMiscData(getUser());
        if (miscComponent == null) return;
        setNails(miscComponent.getTuskNails());
    }

    private static void registerMoves(MoveMap<TuskAct2Entity, State> moves) {
        moves.register(MoveClass.LIGHT, GOLDEN_RECTANGLE_NAIL, State.GOLDEN_RECTANGLE_NAIL);
        moves.register(MoveClass.ULTIMATE, PERFECT_GOLDEN_ROTATION, State.PERFECT_GOLDEN_ROTATION);
        moves.register(MoveClass.UTILITY, ACT_CYCLE, State.ACT_CYCLE);
    }

    public float getNails() {
        return entityData.get(NAILS);
    }

    public void setNails(float nails) {
        entityData.set(NAILS, nails);
        if (miscComponent == null) return;
        miscComponent.setTuskNails(nails);
    }

    public void addNails(float add) {
        setNails(Mth.clamp(entityData.get(NAILS) + add, 0f, NAILS_MAX));
    }

    public boolean drainNails(float amount) {
        float nails = getNails();
        if (nails < amount) return false;
        setNails(nails - amount);
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && hasUser()) {
            if (getNails() < NAILS_MAX && tickCount % 30 == 0) {
                LivingEntity user = getUser();
                if (user instanceof Player player) {
                    int currentHunger = player.getFoodData().getFoodLevel();
                    if (currentHunger > 0) {
                        addNails(1.0f);
                        player.getFoodData().setFoodLevel(currentHunger - 1);
                    }
                }
            }
        }
    }

    @Override
    public void onUserMoveInput(AbstractMove<?, ? super TuskAct2Entity> currentMove, MoveInputType type, boolean pressed, boolean moveInitiated) {
        if (!pressed) return;

        MoveClass moveClass = type.getMoveClass(standby);
        if (moveClass == null) return;

        if (moveClass != MoveClass.LIGHT && moveClass != MoveClass.ULTIMATE && moveClass != MoveClass.UTILITY) {
            if (hasUser() && getUser() instanceof Player player) {
                JSpec<?, ?> spec = JUtils.getSpec(player);
                if (spec != null && spec.canAttack()) {
                    if (spec.initMove(moveClass)) {
                        // Move was successful
                    } else if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
                        spec.queuedMove = type;
                    }
                }
            }
            return;
        }

        if (canAttack()) {
            initMove(moveClass);
        } else if (getMoveStun() > 0 && getMoveStun() < QUEUE_MOVESTUN_LIMIT) {
            queueMove(type);
        }
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.UTILITY) {
            MoveMap.Entry<TuskAct2Entity, State> entry = getMoveMap().getFirstValidEntry(
                    MoveClass.UTILITY, this, false, false
            );

            if (entry != null && entry.getMove() instanceof TuskActCycleMove<?>) {
                TuskActCycleMove<?> originalMove = (TuskActCycleMove<?>) entry.getMove();

                boolean shifting = getUser() != null && getUser().isShiftKeyDown();

                TuskActCycleMove<TuskAct2Entity> directedMove = new TuskActCycleMove<>(
                        originalMove.getCooldown(),
                        originalMove.getWindup(),
                        originalMove.getDuration(),
                        originalMove.getMoveDistance(),
                        2,
                        shifting
                );

                return handleMove(directedMove, CooldownType.UTILITY, State.ACT_CYCLE);
            }
        }

        return super.initMove(moveClass);
    }

    @Override
    public boolean allowMoveHandling() {
        return true;
    }

    @Override
    public void tryBlock() {
    }

    @Override
    @NonNull
    public TuskAct2Entity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<TuskAct2Entity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.tusk_act_2.idle", AzPlayBehaviors.LOOP)),
        GOLDEN_RECTANGLE_NAIL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_2.golden_rectangle_nail", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PERFECT_GOLDEN_ROTATION(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_2.perfect_golden_rotation", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ACT_CYCLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.tusk_act_2.idle", AzPlayBehaviors.LOOP));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TuskAct2Entity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.IDLE;
    }
}