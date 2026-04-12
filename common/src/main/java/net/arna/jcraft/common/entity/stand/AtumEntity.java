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
 * The {@link StandEntity} for <a href="https://jojowiki.com/Atum">Atum</a>.
 * @see JStandTypeRegistry#ATUM
 * @see net.arna.jcraft.common.entity.npc.DarbyYoungerEntity DarbyYoungerEntity
 */
public class AtumEntity extends StandEntity<AtumEntity, AtumEntity.State> {
    public static final MoveSet<AtumEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.ATUM, AtumEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.atum")))
            .withObtainable(false);

    public AtumEntity(Level world) {
        super(JStandTypeRegistry.ATUM.get(), world);
    }

    private static void registerMoves(MoveMap<AtumEntity, State> moves) {
        // TODO Arna
    }

    @Override
    public @NonNull AtumEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<AtumEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(AtumEntity attacker) {
            // TODO Arna
        }
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
