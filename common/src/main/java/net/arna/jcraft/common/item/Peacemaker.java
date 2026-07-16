package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.projectile.BulletProjectile;
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
    // Animation lengths out of peacemaker.animation.json at 20 ticks per second: cock 0.5s,
    // fire 0.375s. Each click is locked out for as long as the step it started takes.
    private static final int COCK_TICKS = 10;
    private static final int FIRE_TICKS = 8;

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

    // Handle right-click - reload only
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.hasEffect(JStatusRegistry.DAZED.get()) || user.isSpectator()) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Creative mode players don't need to reload
        // But they are allowed to!
//        if (user.isCreative()) {
//            return InteractionResultHolder.fail(itemStack);
//        }

        CompoundTag data = itemStack.getOrCreateTag();

        // Check if already at max capacity
        if (data.getInt(SHOTS_ID) >= MAX_ROUNDS) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check if already reloading
        if (data.getBoolean(RELOADING_ID)) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check if player has bullets
        if (!canLoadMore(itemStack, user)) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!world.isClientSide) {
            startReload(itemStack, world, user, hand);
        }

        return InteractionResultHolder.success(itemStack);
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
        final ItemStack mainHand = player.getMainHandItem();
        final ItemStack offHand = player.getOffhandItem();

        // Check if player is holding a peacemaker in either hand
        ItemStack peacemakerStack = null;
        if (mainHand.getItem() instanceof Peacemaker) {
            peacemakerStack = mainHand;
        } else if (offHand.getItem() instanceof Peacemaker) {
            peacemakerStack = offHand;
        }

        if (peacemakerStack == null) {
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
        int shots = data.getInt(SHOTS_ID);

        // Check if reloading
        if (data.getBoolean(RELOADING_ID)) {
            return true; // We handled it (by doing nothing)
        }

        // Creative mode has infinite bullets
        if (shots < 1 && !player.isCreative()) {
            return true; // We handled it (by doing nothing)
        }

        if (!world.isClientSide) {
            // Single action: the hammer has to be thumbed back on its own click before the next
            // one can drop it. Each click is one step, and the gun rests cocked in between.
            if (data.getBoolean(COCKED_ID)) {
                data.putBoolean(COCKED_ID, false);
                player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), FIRE_TICKS);
                markAnimation(peacemakerStack, "fire");
                world.playSound(null, player.getX(), player.getY(), player.getZ(), JSoundRegistry.PEACEMAKER_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);
                shoot(peacemakerStack, world, player);
            } else {
                data.putBoolean(COCKED_ID, true);
                player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), COCK_TICKS);
                markAnimation(peacemakerStack, "cock");
                world.playSound(null, player.getX(), player.getY(), player.getZ(), JSoundRegistry.GUN_COCK.get(), SoundSource.PLAYERS, 1f, 1f);
            }
        }

        return true; // Successfully handled the input
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
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        // Creative mode players have infinite bullets
        if (!(user instanceof Player player && player.isCreative())) {
            if (shots < 1) {
                return;
            }
            data.putInt(SHOTS_ID, shots - 1);
        }

        BulletProjectile bullet = new BulletProjectile(world, user, 9f, 10f, 2, 7f);
        bullet.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, 10, 0f);
        final Vec3 forward = Vec3.directionFromRotation(0f, user.getYRot());
        final boolean offhand = user.getOffhandItem() == itemStack;
        final boolean rightArm = user.getMainArm() == HumanoidArm.RIGHT;
        final double side = rightArm != offhand ? 0.35 : -0.35;
        final Vec3 armPosition = user.getEyePosition().add(forward.scale(0.25))
                .add(forward.cross(new Vec3(0, 1, 0)).scale(side))
                .add(0, -0.35, 0);
        bullet.setPos(armPosition);

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
