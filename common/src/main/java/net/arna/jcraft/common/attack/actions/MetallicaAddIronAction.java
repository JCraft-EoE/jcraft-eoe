package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
@RequiredArgsConstructor(staticName = "addIron")
public class MetallicaAddIronAction extends MoveAction<MetallicaAddIronAction, MetallicaEntity> {
    private final float iron;

    @Override
    public void perform(MetallicaEntity attacker, LivingEntity user, Set<LivingEntity> targets) {
        boolean foundIronEntity = false;
        for (final LivingEntity target : targets) {
            if (!target.getType().is(JTagRegistry.IRONLESS_ENTITIES)) {
                foundIronEntity = true;
                break;
            }
        }
        if (foundIronEntity) {
            attacker.addIron(iron);
        }
    }

    @Override
    public @NonNull MoveActionType<MetallicaAddIronAction> getType() {
        return Type.INSTANCE;
    }

    public static class Type extends MoveActionType<MetallicaAddIronAction> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<MetallicaAddIronAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(),
                    Codec.FLOAT.fieldOf("iron").forGetter(MetallicaAddIronAction::getIron)
            ).apply(instance, apply(MetallicaAddIronAction::addIron)));
        }
    }
}
