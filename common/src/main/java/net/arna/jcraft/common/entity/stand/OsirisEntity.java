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
 * The {@link StandEntity} for <a href="https://jojowiki.com/Osiris">Osiris</a>.
 * @see JStandTypeRegistry#OSIRIS
 * @see net.arna.jcraft.common.entity.npc.DarbyOlderEntity DarbyOlderEntity
 */
public class OsirisEntity extends StandEntity<OsirisEntity, OsirisEntity.State> {
    public static final MoveSet<OsirisEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.OSIRIS,
            OsirisEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.osiris")))
            .withObtainable(false);

    public OsirisEntity(Level world) {
        super(JStandTypeRegistry.OSIRIS.get(), world);
    }

    private static void registerMoves(MoveMap<OsirisEntity, State> moves) {
        // TODO Arna
    }

    @Override
    public @NonNull OsirisEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<OsirisEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(OsirisEntity attacker) {
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
