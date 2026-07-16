package net.arna.jcraft.common.tickable;

import dev.architectury.registry.registries.RegistrySupplier;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives the peacemaker's reload one round at a time.
 * <p>
 * The gate opens once, then every round ejects the spent case, feeds a fresh one and cycles the
 * cylinder, and the gate closes once the cylinder is full or the shooter is out of bullets.
 */
@UtilityClass
public class PeacemakerReload {
    private final List<Reload> reloads = new ArrayList<>();
    private final List<Reload> toAdd = new ArrayList<>();

    public void start(LivingEntity user, Level world, InteractionHand hand, ItemStack stack) {
        toAdd.add(new Reload(user, world.dimension(), hand, stack));
        // The opening stage is entered here rather than on the first tick, so the gate swings and
        // sounds the moment the reload is asked for.
        Peacemaker.markAnimation(stack, Stage.START.animation);
        playStageSound(Stage.START, world, user);
    }

    private void playStageSound(Stage stage, Level world, LivingEntity user) {
        if (stage.sound == null) {
            return;
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), stage.sound.get(), SoundSource.PLAYERS, 0.7f, 1.0f);
    }

    /** Whether this exact gun has a reload running or queued to start. */
    public boolean isReloading(ItemStack stack) {
        return holds(reloads, stack) || holds(toAdd, stack);
    }

    private boolean holds(List<Reload> list, ItemStack stack) {
        for (Reload reload : list) {
            if (reload.stack == stack) {
                return true;
            }
        }
        return false;
    }

    /** Length of a whole reload that chambers {@code rounds} bullets, in ticks. */
    public int totalTicks(int rounds) {
        final int perRound = Stage.EJECT.ticks + Stage.FEED.ticks + Stage.CYCLE.ticks;
        return Stage.START.ticks + Math.max(rounds, 0) * perRound + Stage.END.ticks;
    }

    public void tick(MinecraftServer server) {
        if (!toAdd.isEmpty()) {
            reloads.addAll(toAdd);
            toAdd.clear();
        }

        if (reloads.isEmpty()) {
            return;
        }

        final List<Reload> active = new ArrayList<>();
        for (Reload reload : reloads) {
            if (reload.tick(server)) {
                active.add(reload);
            }
        }

        reloads.clear();
        reloads.addAll(active);
    }

    /**
     * Each stage runs for as long as the animation it plays, so the round lands on the frame that
     * shows it landing. Tick counts are the animation lengths out of peacemaker.animation.json at
     * 20 ticks per second, and have to be updated alongside them: start 0.5s, eject 0.41667s,
     * feed and cycle 0.125s, end 0.91667s. That puts the first round in at exactly one second.
     */
    private enum Stage {
        // The gate swings open to start the reload and shut again to end it, so it books both.
        START("reload_start", 10, JSoundRegistry.GUN_GATE),
        EJECT("reload_eject", 8, JSoundRegistry.GUN_EJECT),
        FEED("reload_feed", 2, JSoundRegistry.GUN_LOAD),
        CYCLE("reload_cycle", 2, JSoundRegistry.GUN_CYCLE),
        END("reload_end", 18, JSoundRegistry.GUN_GATE);

        private final String animation;
        private final int ticks;
        private final @Nullable RegistrySupplier<SoundEvent> sound;

        Stage(String animation, int ticks, @Nullable RegistrySupplier<SoundEvent> sound) {
            this.animation = animation;
            this.ticks = ticks;
            this.sound = sound;
        }
    }

    private static final class Reload {
        private final LivingEntity user;
        private final ResourceKey<Level> worldKey;
        private final InteractionHand hand;
        private final ItemStack stack;
        private Stage stage = Stage.START;
        private int timer = Stage.START.ticks;

        private Reload(LivingEntity user, ResourceKey<Level> worldKey, InteractionHand hand, ItemStack stack) {
            this.user = user;
            this.worldKey = worldKey;
            this.hand = hand;
            this.stack = stack;
        }

        /** @return whether this reload is still running. */
        private boolean tick(MinecraftServer server) {
            final ServerLevel world = server.getLevel(worldKey);
            if (world == null || user == null || !user.isAlive() || interrupted()) {
                Peacemaker.abortReload(stack, user);
                return false;
            }

            if (--timer > 0) {
                return true;
            }

            return advance(world);
        }

        /**
         * The gun has to stay in the hand it started in. Holding the stack itself rather than
         * looking it up each tick is what lets a swapped-away reload still be cleaned up.
         */
        private boolean interrupted() {
            return user.getItemInHand(hand) != stack;
        }

        private boolean advance(ServerLevel world) {
            switch (stage) {
                case START -> enter(Stage.EJECT, world);
                case EJECT -> enter(Stage.FEED, world);
                // The round is only counted once the feed animation has played it into the gate.
                case FEED -> {
                    Peacemaker.loadRound(stack, user);
                    enter(Stage.CYCLE, world);
                }
                case CYCLE -> enter(Peacemaker.canLoadMore(stack, user) ? Stage.EJECT : Stage.END, world);
                case END -> {
                    Peacemaker.finishReload(stack, user);
                    return false;
                }
            }
            return true;
        }

        private void enter(Stage next, ServerLevel world) {
            stage = next;
            timer = next.ticks;
            Peacemaker.markAnimation(stack, next.animation);
            // Sounds fire as the stage opens so they run under its animation rather than after it.
            playStageSound(next, world, user);
        }
    }
}
