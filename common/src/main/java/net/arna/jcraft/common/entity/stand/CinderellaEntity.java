package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Cinderella">Cinderella</a>.
 * @see JStandTypeRegistry#CINDERELLA
 * @see net.arna.jcraft.common.entity.npc.AyaTsujiEntity AyaTsujiEntity
 */
public class CinderellaEntity extends StandEntity<CinderellaEntity, CinderellaEntity.State> {
    public static final MoveSet<CinderellaEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.CINDERELLA,
            CinderellaEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.of(StandInfo.of(Component.translatable("entity.jcraft.cinderella")))
            .withObtainable(false);

    public static final SimpleAttack<CinderellaEntity> LIGHT = SimpleAttack.<CinderellaEntity>lightAttack(
                    7, 11, 0.75f, 4f, 11, 0.15f, 0.2f)
            // .withFollowup(LIGHT_FOLLOWUP)
            // .withCrouchingVariant(CROUCHING_LIGHT)
            // .withAerialVariant(AIR_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );

    public CinderellaEntity(Level world) {
        super(JStandTypeRegistry.CINDERELLA.get(), world);
    }

    private static void registerMoves(MoveMap<CinderellaEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);
    }

    @Override
    public void tick() {
        super.tick();
        // still ghetto, makes a ghost entity where aya spawns (but only if she generates naturally)
        if (!hasUser() || JUtils.getStand(getUserOrThrow()) != this || getUserOrThrow().getFirstPassenger() != this) {
            discard();
        }
    }

    @Override
    public @NonNull CinderellaEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<CinderellaEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cinderella.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.cinderella.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.cinderella.block", AzPlayBehaviors.LOOP)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CinderellaEntity attacker) {
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
