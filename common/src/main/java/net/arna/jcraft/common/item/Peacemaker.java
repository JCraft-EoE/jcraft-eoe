package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.projectile.BulletProjectile;
import net.arna.jcraft.common.tickable.PeacemakerReload;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Peacemaker extends Item {

    public static final String SHOTS_ID = "Shots";
    public static final String RELOADING_ID = "Reloading";
    public static final int MAX_ROUNDS = 6;

    public Peacemaker(Properties settings) {
        super(settings);
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
        int shots = data.getInt(SHOTS_ID);

        // Check if already at max capacity
        if (shots >= MAX_ROUNDS) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check if already reloading
        if (data.getBoolean(RELOADING_ID) && user.getCooldowns().isOnCooldown(JItemRegistry.PEACEMAKER.get())) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check if player has bullets
        if (!user.isCreative() && !hasBulletInInventory(user)) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!world.isClientSide) {
            // Start reload process
            data.putBoolean(RELOADING_ID, true);
            // Put shooting on cooldown during reload (3 seconds = 60 ticks)
            user.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), 60); // 3 second cooldown during reload
            PeacemakerReload.enqueue(new DimensionData(user, world.dimension(), 10)); // 10 ticks between bullets
            world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegistry.LOAD.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
        }

        return InteractionResultHolder.success(itemStack);
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

        // Check if player has an active stand with moveStun > 0
        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null && stand.getMoveStun() > 0) {
            return true; // We handled it (by doing nothing) - stand is busy
        }

        // Check if player has an active spec with moveStun > 0
        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec != null && spec.getMoveStun() > 0) {
            return true; // We handled it (by doing nothing) - spec is busy
        }

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
            // Set immediate cooldown to prevent multiple inputs
            player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), 10); // 10 tick cooldown
            Peacemaker.fireStatic(peacemakerStack, world, player);
        }

        return true; // Successfully handled the input
    }

    // Remove the old onEntitySwing method completely

    // Static fire method for RevolverFire to call
    public static void fireStatic(ItemStack itemStack, Level world, LivingEntity user) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        // Creative mode players have infinite bullets
        if (!(user instanceof Player player && player.isCreative())) {
            if (shots < 1) {
                return;
            }
            data.putInt(SHOTS_ID, shots - 1);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegistry.REVOLVER_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);

        BulletProjectile bullet = new BulletProjectile(world, user, 9f, 10f, 2, 5);
        bullet.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, 10, 0f);

        // Add campfire smoke particles at bullet spawn position
        if (world instanceof ServerLevel serverLevel) {
            // Get the bullet's initial position (slightly in front of the player)
            double bulletX = bullet.getX();
            double bulletY = bullet.getY();
            double bulletZ = bullet.getZ();

            // Spawn small campfire smoke particles
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    bulletX, bulletY, bulletZ,
                    5, // particle count
                    0.1, 0.1, 0.1, // spread
                    0.02 // speed
            );
        }

        world.addFreshEntity(bullet);

        if (user instanceof Player player) {
            // Set refire cooldown - longer than the initial input cooldown
            player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), 20); // 20 tick refire time
            player.awardStat(Stats.ITEM_USED.get(JItemRegistry.PEACEMAKER.get()));
        }
    }

    public static void finishReload(ItemStack itemStack, Level world, LivingEntity user) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt(SHOTS_ID);

        if (shots >= MAX_ROUNDS) {
            data.putBoolean(RELOADING_ID, false);
            return;
        }

        if (user instanceof Player player) {
            // Consume one bullet from inventory
            if (consumeBulletFromInventory(player)) {
                data.putInt(SHOTS_ID, shots + 1);
                // Play reload sound from JSoundRegistry
                world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegistry.LOAD.get(), SoundSource.PLAYERS, 0.7f, 1.0f);

                // Check if we need to reload more bullets
                if (data.getInt(SHOTS_ID) < MAX_ROUNDS && hasBulletInInventory(player)) {
                    // Queue next bullet reload (10 ticks between each bullet)
                    PeacemakerReload.enqueue(new DimensionData(user, world.dimension(), 10));
                    // Keep cooldown active during reload (3 seconds total)
                    player.getCooldowns().addCooldown(JItemRegistry.PEACEMAKER.get(), 60);
                } else {
                    // Finished reloading - remove cooldown
                    data.putBoolean(RELOADING_ID, false);
                    player.getCooldowns().removeCooldown(JItemRegistry.PEACEMAKER.get());
                }
            } else {
                // No more bullets available - remove cooldown
                data.putBoolean(RELOADING_ID, false);
                player.getCooldowns().removeCooldown(JItemRegistry.PEACEMAKER.get());
            }
        }
    }

    private static boolean hasBulletInInventory(Player player) {
        return player.getInventory().contains(new ItemStack(JItemRegistry.BULLET.get()));
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