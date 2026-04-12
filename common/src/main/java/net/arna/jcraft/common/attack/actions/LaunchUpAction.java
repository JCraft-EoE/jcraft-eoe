package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
@RequiredArgsConstructor(staticName = "launchUp")
public class LaunchUpAction extends MoveAction<LaunchUpAction, IAttacker<?, ?>> {
    private final float strength;

    @Override
    public void perform(IAttacker<?, ?> attacker, LivingEntity user, Set<LivingEntity> targets) {
        final Vec3 upDir = new Vec3(GravityChangerAPI.getGravityDirection(user).step()).scale(-strength);

        for (LivingEntity target : targets) {
            JUtils.addVelocity(target, upDir);
        }
    }

    @Override
    public @NonNull MoveActionType<LaunchUpAction> getType() {
        return Type.INSTANCE;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Type extends MoveActionType<LaunchUpAction> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<LaunchUpAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(),
                    Codec.FLOAT.fieldOf("strength").forGetter(LaunchUpAction::getStrength)
            ).apply(instance, apply(LaunchUpAction::launchUp)));
        }
    }
}
