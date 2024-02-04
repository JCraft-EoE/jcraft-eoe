package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface BombTrackerComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    class BombData {
        public boolean dirty = false;
        public boolean isEntity = false, isBlock = false, isItem = false;
        public Entity bombEntity;
        public BlockPos bombBlock;
        public ItemStack bombItem;

        public void reset() {
            isEntity = isBlock = isItem = false;
            bombEntity = null;
            bombBlock = null;
            bombItem = null;
            dirty = true;
        }

        public @Nullable Vec3d getBombPos() {
            // Failsafe due to clientside bullshit
            if (bombEntity == null) isEntity = false;

            if (isEntity)
                return bombEntity.getPos();
            if (isBlock)
                return Vec3d.of(bombBlock);
            if (isItem && bombItem.getHolder() != null)
                return bombItem.getHolder().getPos();
            return null;
        }
        public void setBomb(@Nullable Entity entity) {
            isEntity = isBlock = isItem = false;
            if (entity != null) {
                isEntity = true;
                bombEntity = entity;
            }

            dirty = true;
        }
        public void setBomb(BlockPos blockPos) {
            isEntity = isItem = false;
            isBlock = true;
            bombBlock = blockPos;

            dirty = true;
        }
        public void setBomb(@Nullable ItemStack itemStack) {
            isEntity = isBlock = isItem = false;
            if (itemStack != null) {
                isItem = true;
                bombItem = itemStack;
            }

            dirty = true;
        }
    }

    BombData getMainBomb();
    BombData getBTD();
}
