package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Diver_Down">Diver Down</a>.
 * @see JStandTypeRegistry#DIVER_DOWN
 */
public class DiverDownEntity extends StandEntity<DiverDownEntity, DiverDownEntity.State> {
    public static final MoveSet<DiverDownEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.DIVER_DOWN,
            DiverDownEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.diver_down")))
            .withObtainable(false);

    public DiverDownEntity(Level world) {
        super(JStandTypeRegistry.DIVER_DOWN.get(), world);
    }

    private static void registerMoves(MoveMap<DiverDownEntity, State> moves) {
        // TODO Arna
    }

    public enum State implements StandAnimationState<DiverDownEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(DiverDownEntity attacker) {
            // TODO Arna
        }
    }

    @Override
    public @NonNull DiverDownEntity getThis() {
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
