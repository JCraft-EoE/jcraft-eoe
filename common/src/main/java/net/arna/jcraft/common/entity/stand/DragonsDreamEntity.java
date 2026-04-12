package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Dragon%27s_Dream">Dragon's Dream</a>.
 * @see JStandTypeRegistry#DRAGONS_DREAM
 */
public class DragonsDreamEntity extends StandEntity<DragonsDreamEntity, DragonsDreamEntity.State> {
    public static final MoveSet<DragonsDreamEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.DRAGONS_DREAM,
            DragonsDreamEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.dragons_dream")))
            .withObtainable(false);

    public DragonsDreamEntity(Level world) {
        super(JStandTypeRegistry.DRAGONS_DREAM.get(), world);
    }

    private static void registerMoves(MoveMap<DragonsDreamEntity, State> moves) {
        // TODO Arna
    }

    public enum State implements StandAnimationState<DragonsDreamEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(DragonsDreamEntity attacker) {
            // TODO Arna
        }
    }

    @Override
    public @NonNull DragonsDreamEntity getThis() {
        return this;
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
