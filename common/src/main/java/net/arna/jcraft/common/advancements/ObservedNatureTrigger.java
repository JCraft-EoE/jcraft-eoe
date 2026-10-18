package net.arna.jcraft.common.advancements;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ObservedNatureTrigger extends SimpleCriterionTrigger<ObservedNatureTrigger.TriggerInstance> {

    public final static ResourceLocation ID = JCraft.id("observed_nature");

    @Override
    @NonNull
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    @NonNull
    protected TriggerInstance createInstance(final @NonNull JsonObject json, final @NonNull ContextAwarePredicate predicate, final @NonNull DeserializationContext deserializationContext) {
        return new TriggerInstance(predicate);
    }

    public void trigger(final ServerPlayer player) {
        this.trigger(player, ti -> JComponentPlatformUtils.getSpin(player).natureObservationAmount() >= 20);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(final ContextAwarePredicate player) {
            super(ObservedNatureTrigger.ID, player);
        }

        public static TriggerInstance trigger() {
            return new TriggerInstance(ContextAwarePredicate.ANY);
        }

    }

}
