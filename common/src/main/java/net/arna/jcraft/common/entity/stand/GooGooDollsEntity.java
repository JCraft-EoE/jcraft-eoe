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
 * The {@link StandEntity} for <a href="https://jojowiki.com/Goo_Goo_Dolls">Goo Goo Dolls</a>.
 * @see JStandTypeRegistry#GOO_GOO_DOLLS
 */
public class GooGooDollsEntity extends StandEntity<GooGooDollsEntity, GooGooDollsEntity.State> {
    public static final MoveSet<GooGooDollsEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.GOO_GOO_DOLLS,
            GooGooDollsEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.goo_goo_dolls")))
            .withObtainable(false);

    public GooGooDollsEntity(Level world) {
        super(JStandTypeRegistry.GOO_GOO_DOLLS.get(), world);
    }

    private static void registerMoves(MoveMap<GooGooDollsEntity, State> moves) {
        // TODO Arna
    }

    public enum State implements StandAnimationState<GooGooDollsEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(GooGooDollsEntity attacker) {
            // TODO Arna
        }
    }

    @Override
    public @NonNull GooGooDollsEntity getThis() {
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
