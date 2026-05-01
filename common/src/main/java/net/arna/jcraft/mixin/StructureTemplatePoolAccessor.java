package net.arna.jcraft.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the internal element lists of {@link StructureTemplatePool} so that
 * additional pieces (e.g. the boxing arena) can be appended at runtime, allowing
 * jcraft structures to be injected into vanilla template pools (such as villages)
 * without overriding or replacing them via datapacks.
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

    /**
     * The expanded list of pool elements - one entry per weight unit.
     * This is what the generator actually picks from.
     */
    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> jcraft$getTemplates();

    /**
     * The raw weighted list of pool elements ((element, weight) pairs).
     * Mostly used for serialization, but kept in sync for completeness.
     */
    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> jcraft$getRawTemplates();

    @Accessor("rawTemplates")
    void jcraft$setRawTemplates(List<Pair<StructurePoolElement, Integer>> rawTemplates);
}
