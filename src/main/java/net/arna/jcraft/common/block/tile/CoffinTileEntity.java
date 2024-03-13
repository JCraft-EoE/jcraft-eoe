package net.arna.jcraft.common.block.tile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class CoffinTileEntity extends BlockEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public CoffinTileEntity(BlockPos pos, BlockState state) {
        super(JBlockEntityTypeRegistry.COFFIN_TILE, pos, state);
    }

    private <E extends BlockEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (!world.getBlockState(getPos()).isOf(JObjectRegistry.COFFIN_BLOCK))
            return PlayState.STOP;

        boolean occupied = getCachedState().get(CoffinBlock.OCCUPIED);
        event.getController().setAnimation(new AnimationBuilder().addAnimation(
                occupied ? "animation.coffin.closed" : "animation.coffin.open"
                , ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 30, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
