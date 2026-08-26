package net.arna.jcraft.common.command.permissions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Every permission node the mod's commands check, one per thing a command can actually do.
 * <p>
 * Nodes ending in {@code .others} are additive: the base node is always required, and the
 * {@code .others} node is checked on top of it as soon as a command targets anything other than the
 * source itself. So {@code jcraft.stand.set} alone lets a player restand themselves, and only
 * {@code jcraft.stand.set} plus {@code jcraft.stand.set.others} lets them restand anybody else.
 */
public class JPerms {
    static final Map<String, JPerm> PERMS_MUT = new LinkedHashMap<>();
    public static final Map<String, JPerm> PERMS = Collections.unmodifiableMap(PERMS_MUT);

    // /jcraft
    public static final JPerm HELP = new JPerm("jcraft.help", JPermDefaults.ALL);
    public static final JPerm CHANGES = new JPerm("jcraft.changes", JPermDefaults.ALL);
    public static final JPerm CHANGES_STAND = new JPerm("jcraft.changes.stand", JPermDefaults.ALL);
    public static final JPerm CHANGES_SPEC = new JPerm("jcraft.changes.spec", JPermDefaults.ALL);

    // /jconfig
    public static final JPerm CONFIG = new JPerm("jcraft.config", JPermDefaults.ALL);
    /** Whether the config screen opens read-only or editable. */
    public static final JPerm CONFIG_EDIT = new JPerm("jcraft.config.edit", JPermDefaults.OP_OR_SINGLEPLAYER);

    // /stand
    public static final JPerm STAND_ABOUT = new JPerm("jcraft.stand.about", JPermDefaults.ALL);
    public static final JPerm STAND_SET = new JPerm("jcraft.stand.set");
    public static final JPerm STAND_SET_OTHERS = new JPerm("jcraft.stand.set.others");
    public static final JPerm STAND_SET_SKIN = new JPerm("jcraft.stand.set.skin");
    public static final JPerm STAND_SET_RANDOM = new JPerm("jcraft.stand.set.random");
    public static final JPerm STAND_CLEAR = new JPerm("jcraft.stand.clear", JPermDefaults.OP);
    public static final JPerm STAND_CLEAR_OTHERS = new JPerm("jcraft.stand.clear.others", JPermDefaults.OP);
    public static final JPerm STAND_SKIN = new JPerm("jcraft.stand.skin", JPermDefaults.OP);
    public static final JPerm STAND_SKIN_OTHERS = new JPerm("jcraft.stand.skin.others", JPermDefaults.OP);
    public static final JPerm STAND_BLOCK = new JPerm("jcraft.stand.block", JPermDefaults.OP);
    public static final JPerm STAND_BLOCK_OTHERS = new JPerm("jcraft.stand.block.others", JPermDefaults.OP);

    // /spec
    public static final JPerm SPEC_ABOUT = new JPerm("jcraft.spec.about", JPermDefaults.ALL);
    public static final JPerm SPEC_SET = new JPerm("jcraft.spec.set");
    public static final JPerm SPEC_SET_OTHERS = new JPerm("jcraft.spec.set.others");
    public static final JPerm SPEC_CLEAR = new JPerm("jcraft.spec.clear");
    public static final JPerm SPEC_CLEAR_OTHERS = new JPerm("jcraft.spec.clear.others");
    public static final JPerm SPEC_RESET = new JPerm("jcraft.spec.reset");
    public static final JPerm SPEC_RESET_OTHERS = new JPerm("jcraft.spec.reset.others");
    public static final JPerm SPEC_UNLOCK = new JPerm("jcraft.spec.unlock");
    public static final JPerm SPEC_UNLOCK_OTHERS = new JPerm("jcraft.spec.unlock.others");

    // /framedata
    public static final JPerm FRAMEDATA_STAND = new JPerm("jcraft.framedata.stand", JPermDefaults.ALL);
    public static final JPerm FRAMEDATA_SPEC = new JPerm("jcraft.framedata.spec", JPermDefaults.ALL);

    // /attack
    public static final JPerm ATTACK_STAND = new JPerm("jcraft.attack.stand", JPermDefaults.OP);
    public static final JPerm ATTACK_SPEC = new JPerm("jcraft.attack.spec", JPermDefaults.OP);
    public static final JPerm ATTACK_OTHERS = new JPerm("jcraft.attack.others", JPermDefaults.OP);

    // /cooldown, /cdc
    public static final JPerm COOLDOWN_CANCEL = new JPerm("jcraft.cooldown.cancel");
    public static final JPerm COOLDOWN_CANCEL_OTHERS = new JPerm("jcraft.cooldown.cancel.others");

    // /jgravity
    public static final JPerm GRAVITY_GET = new JPerm("jcraft.gravity.get");
    public static final JPerm GRAVITY_GET_OTHERS = new JPerm("jcraft.gravity.get.others");
    public static final JPerm GRAVITY_CLEAR = new JPerm("jcraft.gravity.clear");
    public static final JPerm GRAVITY_CLEAR_OTHERS = new JPerm("jcraft.gravity.clear.others");
    public static final JPerm GRAVITY_ADD = new JPerm("jcraft.gravity.add");
    public static final JPerm GRAVITY_ADD_OTHERS = new JPerm("jcraft.gravity.add.others");
    public static final JPerm GRAVITY_SET = new JPerm("jcraft.gravity.set");
    public static final JPerm GRAVITY_SET_OTHERS = new JPerm("jcraft.gravity.set.others");
    public static final JPerm GRAVITY_ROTATE = new JPerm("jcraft.gravity.rotate");
    public static final JPerm GRAVITY_ROTATE_OTHERS = new JPerm("jcraft.gravity.rotate.others");
    public static final JPerm GRAVITY_RANDOMISE = new JPerm("jcraft.gravity.randomise");
    public static final JPerm GRAVITY_RANDOMISE_OTHERS = new JPerm("jcraft.gravity.randomise.others");

    /**
     * Passes if the source holds any of the given nodes. Used on branch nodes that don't execute
     * anything themselves and whose children each carry their own node, so that the branch stays
     * usable by anybody who can run something under it. Never put this on a node that also has an
     * {@code .executes}, or holding any one child's node would let you run the branch itself.
     */
    public static Predicate<CommandSourceStack> any(JPerm... perms) {
        return source -> {
            for (final JPerm perm : perms) {
                if (perm.sourceHas(source)) return true;
            }
            return false;
        };
    }

    /**
     * Checks the {@code .others} half of a command's permission pair. Call this once the targets are
     * resolved; it only demands the node if something other than the source itself was selected.
     */
    public static void checkTargets(final CommandSourceStack source, final Collection<? extends Entity> targets,
                                    final JPerm othersPerm) throws CommandSyntaxException {
        final Entity self = source.getEntity();
        for (final Entity target : targets) {
            if (target != self) {
                othersPerm.check(source);
                return;
            }
        }
    }
}
