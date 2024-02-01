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
import lombok.RequiredArgsConstructor;
import net.arna.jcraft.common.attack.core.MoveType;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor(staticName = "attack")
public class AttackArgumentType implements ArgumentType<MoveType> {
    private static final SimpleCommandExceptionType NOT_FOUND = new SimpleCommandExceptionType(Text.literal("That attack type does not exist"));
    private static final Map<String, MoveType> suggestions = Arrays.stream(MoveType.values()).collect(
            ImmutableMap.toImmutableMap(type -> type.name().toLowerCase(), type -> type));
    private final Collection<String> examples = ImmutableList.of("LIGHT", "BARRAGE", "UTILITY");

    @Override
    public MoveType parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        try {
            return MoveType.valueOf(name.toUpperCase());
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
                .map(MoveType::name)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
