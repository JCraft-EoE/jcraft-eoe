package net.arna.jcraft.common.item;

import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.projectile.BulletProjectile;
import net.arna.jcraft.common.system.GunAiming;
import net.arna.jcraft.common.tickable.PeacemakerReload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Peacemaker extends Item {

    public static final String SHOTS_ID = "Shots";
    public static final String RELOADING_ID = "Reloading";
    public static final String ANIMATION_ID = "Animation";
    public static final String ANIMATION_SEQUENCE_ID = "AnimationSequence";
    public static final String COCKED_ID = "Cocked";

    public static final int MAX_ROUNDS = 6;

    // Animation length of fire out of peacemaker.animation.json at 20 ticks per second.
    private static final int FIRE_TICKS = 8;

    // Cocking deliberately does not lock out for its animation's length; the hammer can be dropped
    // as soon as it is back, and the animation is cut off by the shot.
    private static final int COCK_INPUT_LOCKOUT = 2;

    public static final AzCommand FIRE = Attacks.createAnimationCommand(JCraft.FIRE_CONTROLLER, "fire", AzPlayBehaviors.PLAY_ONCE);
    public static final AzCommand COCK = Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "cock", AzPlayBehaviors.PLAY_ONCE);

    public Peacemaker(Properties settings) {
        super(settings);
    }

    /**
     * Names the animation the held model should play next.
     * <p>
     * The sequence counter is what the client actually watches; bumping it is how a repeat of
     * the same animation still reads as a new trigger rather than being skipped.
     */
    public static void markAnimation(ItemStack stack, String animation) {
        if (animation == null || animation.isEmpty()) {
            return;
        }

        CompoundTag data = stack.getOrCreateTag();
        data.putString(ANIMATION_ID, animation);
        data.putLong(ANIMATION_SEQUENCE_ID, data.getLong(ANIMATION_SEQUENCE_ID) + 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag itemData = stack.getTag();

        if (itemData != null && itemData.contains(SHOTS_ID)) {
            tooltip.add(Component.translatable("tooltip.jcraft.peacemaker.shots").append(" §e" + itemData.get(SHOTS_ID)));
        }

        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0.0f;
    }

    // Remove the old hurtEnemy method since we're not using it anymore
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false; // Don't actually hurt the entity
    }

    /**
     * Right click is left free for aiming. Reloading is on the pick block key, routed through
     * jcraft's own input rather than item use, which vanilla gates behind the item's cooldown.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return InteractionResultHolder.fail(user.getItemInHand(hand));
    }

    /**
     * Recovers a gun whose reload never finished. The reloading flag lives in NBT and saves to
     * disk, but the reload queue does not survive a logout or a server restart, so without this
     * the gun would come back flagged as reloading forever and never fire again.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClientSide) {
            return;
        }

        final CompoundTag data = stack.getTag();
        if (data != null && data.getBoolean(RELOADING_ID) && !PeacemakerReload.isReloading(stack)) {
            abortReload(stack, entity instanceof LivingEntity living ? living : null);
        }
    }

    private static void startReload(ItemStack itemStack, Level world, Player user, InteractionHand hand) {
        final CompoundTag data = itemStack.getOrCreateTag();
        data.putBoolean(RELOADING_ID, true);
        // Opening the gate lowers the hammer, so a gun left cocked does not stay that way.
        data.putBoolean(COCKED_ID, false);
        // Shooting stays locked out for exactly as long as the rounds actually being loaded take.
        user.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), PeacemakerReload.totalTicks(plannedRounds(itemStack, user)));
        // The reload owns its own animations and sounds, opening gate included.
        PeacemakerReload.start(user, world, hand, itemStack);
    }

    /** How many rounds this reload expects to chamber, used to size the cooldown up front. */
    private static int plannedRounds(ItemStack itemStack, Player user) {
        final int empty = MAX_ROUNDS - itemStack.getOrCreateTag().getInt(SHOTS_ID);
        return user.isCreative() ? empty : Math.min(empty, countBulletsInInventory(user));
    }

    /** Whether the cylinder has room for another round and the shooter has one to put in it. */
    public static boolean canLoadMore(ItemStack itemStack, LivingEntity user) {
        if (itemStack.getOrCreateTag().getInt(SHOTS_ID) >= MAX_ROUNDS) {
            return false;
        }
        if (!(user instanceof Player player)) {
            return false;
        }
        return player.isCreative() || hasBulletInInventory(player);
    }

    /** Chambers a single round. Called once per feed step of the reload, which owns the sound. */
    public static void loadRound(ItemStack itemStack, LivingEntity user) {
        if (!(user instanceof Player player)) {
            return;
        }

        final CompoundTag data = itemStack.getOrCreateTag();
        final int shots = data.getInt(SHOTS_ID);
        if (shots >= MAX_ROUNDS) {
            return;
        }

        // Creative keeps the animation and audio without eating the player's ammo.
        if (!player.isCreative() && !consumeBulletFromInventory(player)) {
            return;
        }

        data.putInt(SHOTS_ID, shots + 1);
    }

    /** Ends a reload that ran to completion. */
    public static void finishReload(ItemStack itemStack, LivingEntity user) {
        clearReloadState(itemStack, user);
    }

    /**
     * Ends a reload that was cut short, by swapping the gun away or dying mid-load. Without this
     * the reloading flag sticks and the gun can never fire again.
     */
    public static void abortReload(ItemStack itemStack, LivingEntity user) {
        clearReloadState(itemStack, user);
    }

    private static void clearReloadState(ItemStack itemStack, LivingEntity user) {
        itemStack.getOrCreateTag().putBoolean(RELOADING_ID, false);
        if (user instanceof Player player) {
            player.getCooldowns().removeCooldown(JItemRegistry.PEACEMAKER.get());
        }
    }

    // method to handle left-click firing via PlayerInputPacket
    public static boolean handleLeftClick(Player player) {
        return handleLeftClick(player, null);
    }

    public static boolean handleLeftClick(Player player, @Nullable Vec3 muzzle) {
        // Guns are main hand only, so the offhand is left to whatever else wants the input.
        final ItemStack peacemakerStack = player.getMainHandItem();
        if (!(peacemakerStack.getItem() instanceof Peacemaker)) {
            return false; // Not holding a peacemaker, let other systems handle it
        }

        Level world = player.level();

        // Check if on cooldown (prevents multiple inputs)
        if (player.getCooldowns().isOnCooldown(JItemRegistry.PEACEMAKER.get())) {
            return true; // We handled it (by doing nothing)
        }

        if (player.hasEffect(JStatusRegistry.DAZED.get())) {
            return true; // We handled it (by doing nothing), don't let other systems try
        }

/*        // Check if player has an active stand with moveStun > 0
        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null && stand.getMoveStun() > 0) {
            return true; // We handled it (by doing nothing) - stand is busy
        }

        // Check if player has an active spec with moveStun > 0
        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec != null && spec.getMoveStun() > 0) {
            return true; // We handled it (by doing nothing) - spec is busy
        }*/

        CompoundTag data = peacemakerStack.getOrCreateTag();

        // Working the trigger during a reload gives up on filling the cylinder and shuts the gate
        // now, keeping whatever is already chambered.
        if (data.getBoolean(RELOADING_ID)) {
            if (!world.isClientSide) {
                PeacemakerReload.finishEarly(peacemakerStack, world);
                player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), PeacemakerReload.endTicks());
            }
            return true; // We handled it
        }

        // Creative mode has infinite bullets
        if (data.getInt(SHOTS_ID) < 1 && !player.isCreative()) {
            return true; // We handled it (by doing nothing)
        }

        if (!world.isClientSide) {
            // Single action: the hammer has to be thumbed back on its own click before the next
            // one can drop it. Each click is one step, and the gun rests cocked in between.
            if (data.getBoolean(COCKED_ID)) {
                data.putBoolean(COCKED_ID, false);
                player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), FIRE_TICKS);

                FIRE.sendForItem(player, peacemakerStack);

                world.playSound(null, player.getX(), player.getY(), player.getZ(), JSoundRegistry.PEACEMAKER_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);
                shoot(peacemakerStack, world, player, muzzle);
            } else {
                data.putBoolean(COCKED_ID, true);
                // Only long enough to keep one click from reading as two. The hammer can be dropped
                // before the cock animation has played out, which is what keeps the gun quick.
                player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), COCK_INPUT_LOCKOUT);

                COCK.sendForItem(player, peacemakerStack);

                world.playSound(null, player.getX(), player.getY(), player.getZ(), JSoundRegistry.GUN_COCK.get(), SoundSource.PLAYERS, 1f, 1f);
            }
        }

        return true; // Successfully handled the input
    }

    /** Handles the reload input (pick block) routed through PlayerInputPacket. */
    public static boolean handleReloadInput(Player player) {
        final ItemStack peacemakerStack = player.getMainHandItem();
        if (!(peacemakerStack.getItem() instanceof Peacemaker)) {
            return false; // Not holding a peacemaker, let the toss move have the input
        }

        final Level world = player.level();

        if (player.hasEffect(JStatusRegistry.DAZED.get()) || player.isSpectator()) {
            return true;
        }

        final CompoundTag data = peacemakerStack.getOrCreateTag();
        if (data.getInt(SHOTS_ID) >= MAX_ROUNDS || data.getBoolean(RELOADING_ID) || !canLoadMore(peacemakerStack, player)) {
            return true; // We handled it (by doing nothing)
        }

        if (!world.isClientSide) {
            startReload(peacemakerStack, world, player, InteractionHand.MAIN_HAND);
        }

        return true;
    }

    public static boolean isReloading(ItemStack itemStack) {
        final CompoundTag data = itemStack.getTag();
        return data != null && data.getBoolean(RELOADING_ID);
    }

    /**
     * Spends a round and puts a bullet downrange. Called as the hammer falls, so the shot lands
     * with the fire animation rather than with the trigger input; the stage owns the sound.
     */
    public static void shoot(ItemStack itemStack, Level world, LivingEntity user) {
        shoot(itemStack, world, user, null);
    }

    public static void shoot(ItemStack itemStack, Level world, LivingEntity user, @Nullable Vec3 muzzle) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        // Creative mode players have infinite bullets
        if (!(user instanceof Player player && player.isCreative())) {
            if (shots < 1) {
                return;
            }
            data.putInt(SHOTS_ID, shots - 1);
        }

        final boolean aiming = GunAiming.isAiming(user);
        var inaccuracy = 0.1f;
        if (aiming) inaccuracy *= GunAiming.SPREAD_MULTIPLIER;

        BulletProjectile bullet = new BulletProjectile(world, user, 9f, 10f, 2, 7f);
        bullet.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, 10, inaccuracy);

        final Vec3 forward = Vec3.directionFromRotation(0f, user.getYRot());
        final boolean offhand = user.getOffhandItem() == itemStack;
        final boolean rightArm = user.getMainArm() == HumanoidArm.RIGHT;
        double side = rightArm != offhand ? 0.35 : -0.35;
        if (aiming) side = 0.0;

        final Vec3 eye = user.getEyePosition();
        Vec3 spawn;
        if (muzzle != null && muzzle.distanceToSqr(eye) < 6.25) {
            spawn = muzzle;
        } else {
            spawn = eye.add(forward.scale(0.25))
                    .add(forward.cross(new Vec3(0, 1, 0)).scale(side))
                    .add(0, -0.35, 0);
        }
        bullet.setPos(spawn);
        bullet.xo = spawn.x;
        bullet.yo = spawn.y;
        bullet.zo = spawn.z;

        world.addFreshEntity(bullet);

        // The refire cooldown covers the whole pull and is set when the trigger is worked, so
        // there is deliberately none added here; doing so would cut the fire animation short.
        if (user instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(JItemRegistry.PEACEMAKER.get()));
        }
    }

    private static boolean hasBulletInInventory(Player player) {
        return player.getInventory().contains(new ItemStack(JItemRegistry.BULLET.get()));
    }

    private static int countBulletsInInventory(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == JItemRegistry.BULLET.get()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean consumeBulletFromInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == JItemRegistry.BULLET.get()) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(SHOTS_ID, MAX_ROUNDS);
        nbt.putBoolean(RELOADING_ID, false);
        return stack;
    }
}
