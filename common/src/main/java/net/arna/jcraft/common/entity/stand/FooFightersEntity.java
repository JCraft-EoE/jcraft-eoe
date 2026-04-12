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
 * The {@link StandEntity} for <a href="https://jojowiki.com/Foo_Fighters_(Stand)">Foo Fighters</a>.
 * @see JStandTypeRegistry#FOO_FIGHTERS
 */
public class FooFightersEntity extends StandEntity<FooFightersEntity, FooFightersEntity.State> {
    public static final MoveSet<FooFightersEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.FOO_FIGHTERS,
            FooFightersEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.foo_fighters")))
            .withObtainable(false);

    public FooFightersEntity(Level world) {
        super(JStandTypeRegistry.FOO_FIGHTERS.get(), world);
    }

    private static void registerMoves(MoveMap<FooFightersEntity, State> moves) {
        // TODO Arna
    }

    public enum State implements StandAnimationState<FooFightersEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(FooFightersEntity attacker) {
            // TODO Arna
        }
    }

    @Override
    public @NonNull FooFightersEntity getThis() {
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
