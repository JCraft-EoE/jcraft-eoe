package net.arna.jcraft.common.attack.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.core.HitBoxData;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// Extra properties for AbstractSimpleAttack
@With
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AttackMoveExtras {
    public static final Codec<AttackMoveExtras> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StunType.CODEC.optionalFieldOf("stun_type", StunType.BURSTABLE).forGetter(AttackMoveExtras::getStunType),
            Codec.BOOL.optionalFieldOf("override_stun", false).forGetter(AttackMoveExtras::isOverrideStun),
            Codec.BOOL.optionalFieldOf("lift", true).forGetter(AttackMoveExtras::isLift),
            Codec.BOOL.optionalFieldOf("backstab", false).forGetter(AttackMoveExtras::isBackstab),
            Codec.INT.optionalFieldOf("block_stun", -1).forGetter(AttackMoveExtras::getBlockStun),
            Codec.BOOL.optionalFieldOf("static_height", false).forGetter(AttackMoveExtras::isStaticHeight),
            Codec.BOOL.optionalFieldOf("shockwaves", false).forGetter(AttackMoveExtras::isShockwaves),
            Codec.INT.optionalFieldOf("ipsId", 0).forGetter(AttackMoveExtras::getIpsId),
            BlockableType.CODEC.optionalFieldOf("blockable_type", BlockableType.BLOCKABLE).forGetter(AttackMoveExtras::getBlockableType),
            CommonHitPropertyComponent.HitAnimation.CODEC.optionalFieldOf("hit_animation").forGetter(AttackMoveExtras::getHitAnimation),
            JParticleType.CODEC.optionalFieldOf("hit_spark").forGetter(AttackMoveExtras::getHitSpark),
            HitBoxData.CODEC.listOf().<Set<HitBoxData>>xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("extra_hit_boxes", new HashSet<>()).forGetter(AttackMoveExtras::getExtraHitBoxes)
    ).apply(instance, AttackMoveExtras::new));
    private StunType stunType = StunType.BURSTABLE;
    private boolean overrideStun = false;
    private boolean lift = true;
    private boolean backstab = false;
    private int blockStun = -1;
    private boolean staticHeight = false;
    private boolean shockwaves = false;
    private int ipsId = 0;
    private @NonNull BlockableType blockableType = BlockableType.BLOCKABLE;
    private Optional<CommonHitPropertyComponent.HitAnimation> hitAnimation = Optional.of(CommonHitPropertyComponent.HitAnimation.MID);
    private Optional<JParticleType> hitSpark = Optional.of(JParticleType.HIT_SPARK_1);
    private Set<HitBoxData> extraHitBoxes = new HashSet<>();

    public static AttackMoveExtras fromMove(AbstractSimpleAttack<?, ?> move) {
        return new AttackMoveExtras(move.getStunType(), move.isOverrideStun(), move.isLift(), move.isCanBackstab(),
                move.getBlockStun(), move.isStaticY(), move.isDoShockwaves(), move.getIpsId(), move.getBlockableType(),
                Optional.ofNullable(move.getHitAnimation()), Optional.ofNullable(move.getHitSpark()),
                new HashSet<>(move.getExtraHitBoxes()));
    }

    public <M extends AbstractSimpleAttack<? extends M, ?>> M apply(M move) {
            move
                .withStunType(stunType)
                .withOverrideStun(overrideStun)
                .withLift(lift)
                .withBackstab(backstab)
                .withBlockStun(blockStun)
                .withBlockableType(blockableType)
                .withHitAnimation(hitAnimation.orElse(null))
                .withHitSpark(hitSpark.orElse(null))
                .withShockwaves(shockwaves)
                .withIpsId(ipsId);

        extraHitBoxes.forEach(move::withExtraHitBox);

        return move;
    }
}
