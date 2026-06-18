package net.arna.jcraft.common.attack.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

// Extra properties for AbstractMove.
@Data
@NoArgsConstructor
public class BaseMoveExtras {
    public static final Supplier<Codec<BaseMoveExtras>> CODEC = () -> RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.COMPONENT.optionalFieldOf("name", Component.empty()).forGetter(BaseMoveExtras::getName),

            ExtraCodecs.COMPONENT.optionalFieldOf("description", Component.empty()).forGetter(BaseMoveExtras::getDescription),

            JRegistries.MOVE_CONDITION_CODEC.listOf().optionalFieldOf("conditions", new ArrayList<>()).forGetter(BaseMoveExtras::getConditions),

            JRegistries.MOVE_ACTION_CODEC.listOf().optionalFieldOf("actions", new ArrayList<>()).forGetter(BaseMoveExtras::getActions),

            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("armor", 0).forGetter(BaseMoveExtras::getArmor),

            MobilityType.CODEC.optionalFieldOf("mobility_type").forGetter(BaseMoveExtras::getMobilityType),


            Codec.BOOL.optionalFieldOf("is_holdable").forGetter(BaseMoveExtras::getIsHoldable),

            Codec.BOOL.optionalFieldOf("ranged", false).forGetter(BaseMoveExtras::isRanged),

            Codec.BOOL.optionalFieldOf("may_hit_user", false).forGetter(BaseMoveExtras::isMayHitUser),

            RecordCodecBuilder.<IntObjectPair<AbstractMove<?, ?>>>create(i1 -> i1.group(
                    Codec.INT.fieldOf("tick").forGetter(IntObjectPair::leftInt),
                    JRegistries.MOVE_CODEC.fieldOf("move").forGetter(IntObjectPair::right)
            ).apply(i1, IntObjectPair::of)).optionalFieldOf("finisher").forGetter(BaseMoveExtras::getFinisher),

            ExtraCodecs.POSITIVE_INT.optionalFieldOf("followup_frame")
                    .xmap(i -> i.map(OptionalInt::of).orElseGet(OptionalInt::empty),
                            i -> i.isEmpty() ? Optional.empty() : Optional.of(i.getAsInt()))
                    .forGetter(BaseMoveExtras::getFollowupFrame),

            Codec.BOOL.optionalFieldOf("loop_prevention", true).forGetter(BaseMoveExtras::isLoopPrevention),

            Codec.BOOL.optionalFieldOf("lingering_sounds", false).forGetter(BaseMoveExtras::isLingeringSounds),

            Codec.BOOL.optionalFieldOf("look_tracking", false).forGetter(BaseMoveExtras::isLookTracking),

            Codec.FLOAT.optionalFieldOf("look_tracking_reach", 3.5f).forGetter(BaseMoveExtras::getLookTrackingReach)
    ).apply(instance, BaseMoveExtras::new));
    private Component name = Component.empty();
    private Component description = Component.empty();
    private final List<MoveCondition<?, ?>> conditions = new ArrayList<>();
    private final List<MoveAction<?, ?>> actions = new ArrayList<>(), initActions = new ArrayList<>();
    private int armor;
    protected Optional<MobilityType> mobilityType = Optional.empty();
    private Optional<Boolean> isHoldable = Optional.empty();
    protected boolean ranged;
    protected boolean mayHitUser;
    private Optional<IntObjectPair<AbstractMove<?, ?>>> finisher = Optional.empty();
    private OptionalInt followupFrame = OptionalInt.empty();
    private boolean loopPrevention = true;
    private boolean lingeringSounds = false;
    private boolean lookTracking = false;
    private float lookTrackingReach = 3.5f;

    private BaseMoveExtras(final Component name, final Component description, final List<MoveCondition<?, ?>> conditions,
                           final List<MoveAction<?, ?>> actions,
                           final int armor, Optional<MobilityType> mobilityType, final Optional<Boolean> isHoldable, final boolean ranged,
                           final boolean mayHitUser, final Optional<IntObjectPair<AbstractMove<?, ?>>> finisher,
                           final OptionalInt followupFrame, final boolean loopPrevention, final boolean lingeringSounds,
                           final boolean lookTracking, final float lookTrackingReach) {
        this.name = name;
        this.description = description;
        this.conditions.addAll(conditions);
        this.actions.addAll(actions);
        this.armor = armor;
        this.mobilityType = mobilityType;
        this.isHoldable = isHoldable;
        this.ranged = ranged;
        this.mayHitUser = mayHitUser;
        this.finisher = finisher;
        this.followupFrame = followupFrame;
        this.loopPrevention = loopPrevention;
        this.lingeringSounds = lingeringSounds;
        this.lookTracking = lookTracking;
        this.lookTrackingReach = lookTrackingReach;
    }

    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    public static <A extends IAttacker<? extends A, ?>> BaseMoveExtras fromMove(final AbstractMove<?, A> move) {
        return new BaseMoveExtras(move.getName(), move.getDescription(), (List<MoveCondition<?, ?>>) (List) move.getConditions(),
                (List<MoveAction<?, ?>>) (List) move.getActions(),
                move.getArmor(), Optional.ofNullable(move.getMobilityType()), Optional.ofNullable(move.getIsHoldable()),
                move.isRanged(), move.isMayHitUser(), Optional.ofNullable((IntObjectPair<AbstractMove<?, ?>>) (IntObjectPair) move.getFinisher()),
                move.getFollowupFrame(), move.isLoopPrevention(), move.isLingeringSounds(),
                move.isLookTracking(), move.getLookTrackingReach());
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // generic types and (de)serialization don't mix well
    public <M extends AbstractMove<? extends M, ?>> M apply(final M move) {
        move
                .withInfo(name, description)
                .withArmor(armor)
                .withMobilityType(mobilityType.orElse(null))
                .withHoldable(isHoldable.orElse(null))
                .withConditionsRaw(conditions)
                .withActionsRaw(actions)
                .withInitActionsRaw(initActions)
                .withFollowupFrame(followupFrame)
                .withLookTrackingReach(lookTrackingReach);
        if (ranged) move.markRanged();
        if (mayHitUser) move.allowHitUser();
        if (!loopPrevention) move.noLoopPrevention();
        if (lookTracking) move.withLookTracking();
        finisher.ifPresent(f -> ((AbstractMove) move).withFinisher(f.leftInt(), f.right()));

        return move;
    }
}
