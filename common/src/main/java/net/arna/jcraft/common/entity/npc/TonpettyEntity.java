package net.arna.jcraft.common.entity.npc;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.arna.jcraft.api.spec.SpecTypeUtil;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.spec.HamonSpecUser;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class TonpettyEntity extends HamonSpecUser {

    public static final ResourceKey<DamageType> HAMON_INITIATION = JDamageSources.createDamageType("hamoninit");

    protected Set<Player> greeted = Collections.newSetFromMap(new WeakHashMap<>());
    protected Set<Player> warned = Collections.newSetFromMap(new WeakHashMap<>());

    public TonpettyEntity(final Level level) {
        super(JEntityTypeRegistry.TONPETTY.get(), level);
        setPersistenceRequired();
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
                if (JUtils.hasAdvancement(sp, JCraft.id("hamon6"))) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.master"));
                }
                else if (JUtils.hasAdvancement(sp, JCraft.id("hamon5"))) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon6"));
                }
                else if (JUtils.hasAdvancement(sp, JCraft.id("hamon4"))) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon5"));
                }
                else if (JUtils.hasAdvancement(sp, JCraft.id("hamon3"))) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon4"));
                }
                else if (JUtils.hasAdvancement(sp, JCraft.id("hamon2"))) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon3"));
                }
                else if (JUtils.hasAdvancement(sp, JCraft.id("hamon1"))) {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon2"));
                }
                else {
                    player.sendSystemMessage(Component.translatable("dialogue.tonpetty.hamon1"));
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

    public static AttributeSupplier.Builder createTonpettiAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

}
