package net.arna.jcraft.common.attack.moves.thehand;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.common.entity.stand.TheHandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class EraseGroundAttack extends AbstractEraseAttack<EraseGroundAttack> {
    public EraseGroundAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<EraseGroundAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheHandEntity attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        final Level level = user.level();
        if (!mayBreak(user, null)) return targets;

        final Vec3 rotVec = attacker.getLookAngle();
        final Vec3i rotVecI = new Vec3i((int) Math.round(rotVec.x), (int) Math.round(rotVec.y), (int) Math.round(rotVec.z));

            /*
            PATTERN:
            [][][]
            [][]
            WHERE TOP LEFT IS ATTACKER STANDING BLOCK POSITION, AND RIGHT IS ATTACKER FORWARD
             */

        final Vec3i gravityNormal = GravityChangerAPI.getGravityDirection(user).getNormal();

        final BlockPos lowBlock1 = attacker.getOnPos();
        final BlockPos lowBlock2 = lowBlock1.offset(rotVecI);
        final BlockPos block1 = lowBlock1.subtract(gravityNormal);
        final BlockPos block2 = block1.offset(rotVecI);
        final BlockPos block3 = block2.offset(rotVecI);
        eraseBlock(user, level, lowBlock1);
        eraseBlock(user, level, lowBlock2);
        eraseBlock(user, level, block1);
        eraseBlock(user, level, block2);
        eraseBlock(user, level, block3);

        return targets;
    }

    private void eraseBlock(final LivingEntity user, final Level level, final BlockPos lookedBlock) {
        if (!mayBreak(user, lookedBlock)) return;
        level.setBlock(lookedBlock, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected @NonNull EraseGroundAttack getThis() {
        return this;
    }

    @Override
    public @NonNull EraseGroundAttack copy() {
        return copyExtras(new EraseGroundAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractEraseAttack.Type<EraseGroundAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<EraseGroundAttack>, EraseGroundAttack> buildCodec(RecordCodecBuilder.Instance<EraseGroundAttack> instance) {
            return attackDefault(instance, EraseGroundAttack::new);
        }
    }
}
