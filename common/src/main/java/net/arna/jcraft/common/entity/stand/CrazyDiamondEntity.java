package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.common.attack.moves.shared.RestorationAttack;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class CrazyDiamondEntity extends StandEntity<CrazyDiamondEntity, CrazyDiamondEntity.State> {
    public static final MoveSet<CrazyDiamondEntity, CrazyDiamondEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.CRAZY_DIAMOND,
            CrazyDiamondEntity::registerDefaultMoves, CrazyDiamondEntity.State.class);

    // TODO Ayutac copied the values from CreamEntity
    public static final RestorationAttack<CrazyDiamondEntity> PUNCH = RestorationAttack.<CrazyDiamondEntity>lightAttack(
            6, 14, 0.75f, 5f, 20, 0.3f, -0.1f);

    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.crazydiamond"))
                    .build())
            .build()
            .withObtainable(false);

    public CrazyDiamondEntity(final Level world) {
        super(JStandTypeRegistry.CRAZY_DIAMOND.get(), world);
    }

    @Override
    public @NonNull CrazyDiamondEntity getThis() {
        return this;
    }

    private static void registerDefaultMoves(final @NonNull MoveMap<CrazyDiamondEntity, CrazyDiamondEntity.State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, PUNCH, CrazyDiamondEntity.State.LIGHT);
    }

    public enum State implements StandAnimationState<CrazyDiamondEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP)),
        LIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "block", AzPlayBehaviors.LOOP))
        ;

        private final AzCommand animator;

        State(final @NonNull AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(final @NonNull CrazyDiamondEntity attacker) {
            animator.sendForEntity(attacker);
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
