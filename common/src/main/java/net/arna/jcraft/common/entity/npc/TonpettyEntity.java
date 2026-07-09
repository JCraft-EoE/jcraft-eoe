package net.arna.jcraft.common.entity.npc;

import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.arna.jcraft.api.spec.SpecTypeUtil;
import net.arna.jcraft.common.advancements.Hamon1Trigger;
import net.arna.jcraft.common.advancements.Hamon2Trigger;
import net.arna.jcraft.common.advancements.Hamon3Trigger;
import net.arna.jcraft.common.advancements.Hamon4Trigger;
import net.arna.jcraft.common.advancements.Hamon5Trigger;
import net.arna.jcraft.common.advancements.Hamon6Trigger;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.spec.HamonSpecUser;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class TonpettyEntity extends HamonSpecUser implements NeutralMob {
    public static final ResourceKey<DamageType> HAMON_INITIATION = JDamageSources.createDamageType("hamoninit");
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    protected Set<Player> greeted = Collections.newSetFromMap(new WeakHashMap<>());
    protected Set<Player> warned = Collections.newSetFromMap(new WeakHashMap<>());
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    public TonpettyEntity(final Level level) {
        super(JEntityTypeRegistry.TONPETTY.get(), level);
        // Only make visible when Tonpetty is aggravated.
        bossEvent.setVisible(false);
    }

    @Override
    protected InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        final CommonSpecComponent specData = JComponentPlatformUtils.getSpecData(player);

        if (specData.getType() != JSpecTypeRegistry.HAMON.get() && !greeted.contains(player) && !player.level().isClientSide()) {
            player.sendSystemMessage(Component.translatable("dialogue.tonpetty.greeting"));
            greeted.add(player);
            if (SpecTypeUtil.isNone(specData.getType())) {
                return InteractionResult.SUCCESS;
            }
        }
        if (!SpecTypeUtil.isNone(specData.getType()) && specData.getType() != JSpecTypeRegistry.HAMON.get()) {
            if (!warned.contains(player)) {
                if (!player.level().isClientSide()) {
                    player.sendSystemMessage(Component.translatable("warning.jcraft.spec.change.mob"));
                    warned.add(player);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Hamon lessons
        if (specData.getType() == JSpecTypeRegistry.HAMON.get()) {
            if (player instanceof ServerPlayer sp) {
                final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(player);
                if (JUtils.hasAdvancement(sp, Hamon6Trigger.ID)) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.master"));
                    hamon.setActiveLesson(0);
                }
                else if (JUtils.hasAdvancement(sp, Hamon5Trigger.ID)) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon6"));
                    hamon.setActiveLesson(6);
                    hamon.resetLessonTicks(6);
                }
                else if (JUtils.hasAdvancement(sp, Hamon4Trigger.ID)) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon5"));
                    hamon.setActiveLesson(5);
                    hamon.resetLessonTicks(5);
                }
                else if (JUtils.hasAdvancement(sp, Hamon3Trigger.ID)) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon4"));
                    hamon.setActiveLesson(4);
                    hamon.resetLessonTicks(4);
                }
                else if (JUtils.hasAdvancement(sp, Hamon2Trigger.ID)) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon3"));
                    hamon.setActiveLesson(3);
                    hamon.resetLessonTicks(3);
                }
                else if (JUtils.hasAdvancement(sp, Hamon1Trigger.ID)) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon2"));
                    hamon.setActiveLesson(2);
                    hamon.resetLessonTicks(2);
                }
                else {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon1"));
                    hamon.setActiveLesson(1);
                    hamon.resetLessonTicks(1);
                }
            }
        }
        // Hamon initiation
        else {
            int damage = specData.getType() == JSpecTypeRegistry.VAMPIRE ? 200 : 1;
            player.hurt(JDamageSources.create(player.level(), HAMON_INITIATION, player), damage);

            if (!player.level().isClientSide()) {
                warned.remove(player);
                specData.setType(JSpecTypeRegistry.HAMON.get());
                player.awardStat(JStatRegistry.SPECS_CHANGED.get());
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static AttributeSupplier.Builder createTonpettyAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;

        // Toggle boss bar based on whether we're angry.
        if (isAngry() != bossEvent.isVisible())
            bossEvent.setVisible(isAngry());
    }

    @Override
    public void customServerAiStep() {
        bossEvent.setProgress(getHealth() / getMaxHealth());
        updatePersistentAnger((ServerLevel)level(), true);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        addPersistentAngerSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readPersistentAngerSaveData(level(), compound);

        if (hasCustomName()) {
            bossEvent.setName(getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int remainingPersistentAngerTime) {
        this.remainingPersistentAngerTime = remainingPersistentAngerTime;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID persistentAngerTarget) {
        this.persistentAngerTarget = persistentAngerTarget;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return persistentAngerTarget;
    }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer player) {
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(@NotNull ServerPlayer player) {
        bossEvent.removePlayer(player);
    }
}
