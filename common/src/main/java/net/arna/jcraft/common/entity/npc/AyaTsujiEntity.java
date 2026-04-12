package net.arna.jcraft.common.entity.npc;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.common.tickable.JEnemies;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaTsujiEntity extends PathfinderMob implements Merchant {

    private Player tradingPlayer;
    private final MerchantOffers merchantOffers = new MerchantOffers();

    public AyaTsujiEntity(Level world) {
        super(JEntityTypeRegistry.AYA_TSUJI.get(), world);
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(this);
        standData.setType(JStandTypeRegistry.CINDERELLA.get());
        standData.setSkin(0);

        if (world.isClientSide()) return;
        JEnemies.add(this);

        final ItemStack[] masks = new ItemStack[4];
        for (int i = 0; i <= 3; i++) {
            masks[i] = new ItemStack(JItemRegistry.CINDERELLA_MASK.get());
            if (i > 0) {
                final CompoundTag nbt = masks[i].getOrCreateTag();
                final ListTag enchantments = new ListTag();
                final CompoundTag enchantment = new CompoundTag();
                enchantment.putString("id", "jcraft:cinderellas_kiss");
                enchantment.putShort("lvl", (short) i);
                enchantments.add(enchantment);
                nbt.put("Enchantments", enchantments);
            }
        }
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 15), masks[0], 4, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 25), masks[1], 3, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 35), masks[2], 2, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.ENDER_PEARL, 16), masks[0], 4, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.ENDER_EYE, 8), masks[1], 3, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.ENDER_EYE, 10), masks[2], 2, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(Items.ENDER_EYE, 12), masks[3], 1, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(JItemRegistry.STELLAR_IRON_INGOT.get()), masks[1], 3, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(JItemRegistry.STAND_ARROW.get(), 1), masks[2], 2, 0, 1f));
        merchantOffers.add(new MerchantOffer(new ItemStack(JItemRegistry.STAND_ARROW.get(), 2), masks[3], 1, 0, 1f));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(10, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1.0, 32f));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(IronGolem.class));
    }

    public static AttributeSupplier.Builder createAyaTsujiAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected @NonNull InteractionResult mobInteract(@NonNull final Player player, @NonNull final InteractionHand hand) {
        if (!this.getOffers().isEmpty()) {
            if (getTarget() == player) return InteractionResult.FAIL;
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
        }
        return InteractionResult.sidedSuccess(isClientSide());
    }

    @Override
    public void setTradingPlayer(@Nullable Player tradingPlayer) {
        this.tradingPlayer = tradingPlayer;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public @NotNull MerchantOffers getOffers() {
        return merchantOffers;
    }

    @Override
    public void overrideOffers(@NonNull MerchantOffers offers) {
        // intentionally left empty
    }

    @Override
    public void notifyTrade(@NonNull MerchantOffer offer) {
        // intentionally left empty
    }

    @Override
    public void notifyTradeUpdated(@NonNull ItemStack stack) {
        // intentionally left empty
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
        // intentionally left empty
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public @Nullable SoundEvent getNotifyTradeSound() {
        return null;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide();
    }

    @Override
    public void tick() {
        if (tradingPlayer != null) {
            if (navigation.isInProgress()) {
                navigation.stop();
            }
            lookAt(EntityAnchorArgument.Anchor.EYES, tradingPlayer.getEyePosition());
        }
        super.tick();
    }

    // Animations
    /*
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle_controller", 10, this::idlePredicate));
        controllers.add(new AnimationController<>(this, "walk_controller", 10, this::walkPredicate));
    }

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.aya_tsuji.walk");
    private PlayState walkPredicate(AnimationState<AyaTsujiEntity> state) {
        final float velocityLengthSqr = (float) getDeltaMovement().length();
        if (velocityLengthSqr < 0.05f) {
            return state.setAndContinue(IDLE);
        } else {
            state.setControllerSpeed(1.0f + velocityLengthSqr / 2.5f);
        }
        return state.setAndContinue(WALK);
    }

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.aya_tsuji.idle");
    private PlayState idlePredicate(AnimationState<AyaTsujiEntity> state) {
        if (getDeltaMovement().lengthSqr() > 0.01 && getTarget() == null) {
            return state.setAndContinue(IDLE);
        }

        return PlayState.STOP;
    }*/
}
