package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
@RequiredArgsConstructor(staticName = "addNails")
public class TuskAddNailAction<T extends StandEntity<T, ?>> extends MoveAction<TuskAddNailAction<T>, T> {
    private final float nails;

    @Override
    public void perform(T attacker, LivingEntity user, Set<LivingEntity> targets) {
        if (attacker instanceof net.arna.jcraft.common.entity.stand.TuskAct1Entity tusk1) {
            tusk1.addNails(nails);
        } else if (attacker instanceof net.arna.jcraft.common.entity.stand.TuskAct2Entity tusk2) {
            tusk2.addNails(nails);
        } else if (attacker instanceof net.arna.jcraft.common.entity.stand.TuskAct3Entity tusk3) {
            tusk3.addNails(nails);
        }
    }

    @Override
    public @NonNull MoveActionType<TuskAddNailAction<T>> getType() {
        return (MoveActionType<TuskAddNailAction<T>>) (MoveActionType<?>) Type.INSTANCE;
    }

    public static class Type extends MoveActionType<TuskAddNailAction<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<TuskAddNailAction<?>> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(),
                    Codec.FLOAT.fieldOf("nails").forGetter(TuskAddNailAction::getNails)
            ).apply(instance, apply(TuskAddNailAction::addNails)));
        }
    }
}