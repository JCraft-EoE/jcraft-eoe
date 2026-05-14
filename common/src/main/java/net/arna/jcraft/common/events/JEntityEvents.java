package net.arna.jcraft.common.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.Entity;

public interface JEntityEvents {
    /**
     * Invoked directly after an entity has been successfully added.
     */
    Event<PostAdd> POST_ADD = EventFactory.createEventResult();

    /**
     * Invoked when an entity was removed.
     */
    Event<Remove> REMOVE = EventFactory.createEventResult();

    interface PostAdd {
        EventResult add(Entity entity, boolean worldGenSpawned);
    }

    interface Remove {
        void remove(Entity entity, Entity.RemovalReason reason);
    }
}
