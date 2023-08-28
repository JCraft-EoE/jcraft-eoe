package net.arna.jcraft.common.argumenttype;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.arna.jcraft.common.attack.core.MoveType;
import net.minecraft.text.Text;

import java.util.Collection;

@Getter
@RequiredArgsConstructor(staticName = "attack")
public class AttackArgumentType implements ArgumentType<MoveType> {
    private static final SimpleCommandExceptionType NOT_FOUND = new SimpleCommandExceptionType(Text.literal("That attack type does not exist"));
    // implements ArgumentType#getExamples()
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
}
