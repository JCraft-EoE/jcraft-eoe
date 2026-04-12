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
 * The {@link StandEntity} for <a href="https://jojowiki.com/Chariot_Requiem">Chariot Requiem</a>.
 * @see JStandTypeRegistry#CHARIOT_REQUIEM
 * @see net.arna.jcraft.client.renderer.entity.stands.ChariotRequiemRenderer ChariotRequiemRenderer
 */
public class ChariotRequiemEntity extends StandEntity<ChariotRequiemEntity, ChariotRequiemEntity.State> {
    public static final MoveSet<ChariotRequiemEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.CHARIOT_REQUIEM,
            ChariotRequiemEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.chariot_requiem")))
            .withObtainable(false);

    public ChariotRequiemEntity(Level world) {
        super(JStandTypeRegistry.CHARIOT_REQUIEM.get(), world);
    }

    private static void registerMoves(MoveMap<ChariotRequiemEntity, State> moves) {
        // TODO Arna
    }

    @Override
    public @NonNull ChariotRequiemEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<ChariotRequiemEntity> {
        IDLE,
        BLOCK;

        @Override
        public void playAnimation(ChariotRequiemEntity attacker) {
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
