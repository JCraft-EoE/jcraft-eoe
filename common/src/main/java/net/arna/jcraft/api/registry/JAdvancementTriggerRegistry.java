package net.arna.jcraft.api.registry;

import net.arna.jcraft.common.advancements.Hamon1Trigger;
import net.arna.jcraft.common.advancements.Hamon2Trigger;
import net.arna.jcraft.common.advancements.Hamon3Trigger;
import net.arna.jcraft.common.advancements.Hamon4Trigger;
import net.arna.jcraft.common.advancements.Hamon5Trigger;
import net.arna.jcraft.common.advancements.Hamon6Trigger;
import net.arna.jcraft.common.advancements.ObtainedSpecTrigger;
import net.arna.jcraft.common.advancements.ObtainedStandTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public interface JAdvancementTriggerRegistry {

    ObtainedStandTrigger OBTAINED_STAND = CriteriaTriggers.register(new ObtainedStandTrigger());
    ObtainedSpecTrigger OBTAINED_SPEC = CriteriaTriggers.register(new ObtainedSpecTrigger());
    Hamon1Trigger HAMON1 = CriteriaTriggers.register(new Hamon1Trigger());
    Hamon2Trigger HAMON2 = CriteriaTriggers.register(new Hamon2Trigger());
    Hamon3Trigger HAMON3 = CriteriaTriggers.register(new Hamon3Trigger());
    Hamon4Trigger HAMON4 = CriteriaTriggers.register(new Hamon4Trigger());
    Hamon5Trigger HAMON5 = CriteriaTriggers.register(new Hamon5Trigger());
    Hamon6Trigger HAMON6 = CriteriaTriggers.register(new Hamon6Trigger());

    static void init() {
        /* empty on purpose */
    }
}
