package net.arna.jcraft.common.argumenttype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(staticName = "stand")
public class StandArgumentType implements ArgumentType<StandType> {
    private static final SimpleCommandExceptionType NOT_FOUND = new SimpleCommandExceptionType(Text.literal("That stand was not found"));
    private static final Map<String, StandType> suggestions = StandType.getAllStandTypes().stream()
            .collect(ImmutableMap.toImmutableMap(type -> type.name().toLowerCase().replaceAll("_", ""), type -> type));
    @Getter // implements ArgumentType#getExamples()
    private final Collection<String> examples = ImmutableList.of("MADE_IN_HEAVEN", "C_MOON", "GER");

    @Override
    public StandType parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        try {
            return StandType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw NOT_FOUND.createWithContext(reader);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase().replaceAll("_", "");
        suggestions.entrySet().stream()
                .filter(e -> e.getKey().startsWith(input))
                .map(Map.Entry::getValue)
                .map(StandType::name)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
