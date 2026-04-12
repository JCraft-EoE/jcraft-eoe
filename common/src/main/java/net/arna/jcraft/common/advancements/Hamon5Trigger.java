package net.arna.jcraft.common.advancements;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class Hamon5Trigger extends SimpleCriterionTrigger<Hamon5Trigger.TriggerInstance> {

    public final static ResourceLocation ID = JCraft.id("hamon5");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(final JsonObject json, final ContextAwarePredicate predicate, final DeserializationContext deserializationContext) {
        final int amount;
        if (json.has("amount")) {
            amount = GsonHelper.getAsInt(json, "amount");
        }
        else {
            throw new JsonSyntaxException("Unknown amount of enemies to hit at least!");
        }
        return new TriggerInstance(predicate, amount);
    }

    public void trigger(final ServerPlayer player, final int amount) {
        this.trigger(player, trigger -> JUtils.hasAdvancement(player, Hamon4Trigger.ID) && trigger.matches(amount));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final int amount;

        public TriggerInstance(final ContextAwarePredicate player, final int amount) {
            super(Hamon5Trigger.ID, player);
            this.amount = amount;
        }

        public static TriggerInstance hitAtLeastEnemies(final int amount) {
            return new TriggerInstance(ContextAwarePredicate.ANY, amount);
        }

        public boolean matches(final int amount) {
            return amount >= this.amount;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(final @NotNull SerializationContext context) {
            final JsonObject jsonObject = super.serializeToJson(context);
            jsonObject.addProperty("amount", amount);
            return jsonObject;
        }
    }
}
