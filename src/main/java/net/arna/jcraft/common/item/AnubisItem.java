package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.arna.jcraft.common.entity.StandEntity.BaseDamageLogic;

public class AnubisItem extends Item {
    public int state = 0;

    public AnubisItem(Settings settings) {
        super(settings);
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            DamageSource damageSource = DamageSource.mob(attacker);
            Vec3d aPos = attacker.getPos();

            ItemCooldownManager cdManager = player.getItemCooldownManager();

            if (cdManager.isCoolingDown(this)) {
                return false;
            }

            IEntityDataSaver user = (IEntityDataSaver) player;
            NbtCompound pData = user.getPersistentData();

            // Sneaking / heavy, stunning attack
            // puts barrage and light on cooldown
            if (attacker.isSneaking()) {
                BaseDamageLogic(target, Vec3d.ZERO, 10, 1, false, 6.5f, false, damageSource, attacker);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 0, false, true));
                state = 3;

                pData.putInt(JCraft.standLightCD, 40);
                if (pData.getInt(JCraft.standBarrageCD) < 40) {
                    pData.putInt(JCraft.standBarrageCD, 40);
                }

                cdManager.set(this, 30);
                return true;
            }

            // Jumping / circular, knocking attack
            if (!attacker.isOnGround()) {
                List<LivingEntity> hit = attacker.world.getEntitiesByClass(LivingEntity.class, new Box(aPos.add(-2, -1, -2), aPos.add(2, 1, 2)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                hit.remove(attacker);

                for (LivingEntity ent : hit) {
                    BaseDamageLogic(ent, ent.getPos().subtract(aPos.x, ent.getY(), aPos.z).normalize().add(0, 0.2, 0), 5, 3, true, 3.5f, false, damageSource, attacker);
                }

                state = 2;
                cdManager.set(this, 10);
                return true;
            }

            // Standing / thrusting attack
            BaseDamageLogic(target, Vec3d.ZERO, 5, 1, false, 4f, true, damageSource, attacker);
            attacker.setVelocity(attacker.getVelocity().add(target.getPos().subtract(aPos.x, target.getY(), aPos.z).normalize().multiply(0.25)));
            attacker.velocityModified = true;
            state = 1;
            cdManager.set(this, 12);
            pData.putInt(JCraft.standLightCD, 40);
            return true;
        }

        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.of("§9The sword/stand named after the Egyptian god of death."));
        tooltip.add(Text.of("§eBloodthirsty. §9Fuels itself on any and all violence."));
        tooltip.add(Text.of("§9Operates on a charge system; §eglints §9when charged."));
        tooltip.add(Text.of("§9Can used to §eblock."));
        tooltip.add(Text.of("§eHard to get rid of."));
        tooltip.add(Text.of("§9Can §esheathed §9with §eCrouch + RMB."));
        tooltip.add(Text.of("§cCrouching attack - heavy damage, 1s stun"));
        tooltip.add(Text.of("§aStanding attack - medium damage, 0.25s stun, lunge"));
        tooltip.add(Text.of("§bJumping attack - medium damage, knockback"));

        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) {
            user.setCurrentHand(hand);
        } else {
            user.setStackInHand(hand, new ItemStack(JObjectRegistry.ANUBISSHEATHED));
        }
        ItemStack itemStack = user.getStackInHand(hand);

        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClient()) {
            if (entity instanceof PlayerEntity player) {
                // Glint logic
                boolean isCd = player.getItemCooldownManager().isCoolingDown(this);

                if (stack.getEnchantments().isEmpty()) {
                    if (!isCd) {
                        stack.addEnchantment(Enchantments.CHANNELING, 1);
                    }
                } else {
                    if (isCd) {
                        stack.getEnchantments().remove(0);
                    }
                }

                // Bloodlust
                int timeSinceAttack = player.getLastAttackTime() - player.age;

                // If was in battle within last 4 minutes, apply haste 1
                if (timeSinceAttack > -4800) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20, 0, true, false));
                }
                // If wasn't in battle for 4 minutes, apply mining fatigue 1
                if (timeSinceAttack < -4800) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 0, true, false));
                }
                // If wasn't in battle for 8 minutes, apply weakness 1
                if (timeSinceAttack < -9600) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 0, true, false));
                }
                // If wasn't in battle for 12 minutes, apply nausea 1
                if (timeSinceAttack < -14400) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20, 0, true, false));
                }
                // If wasn't in battle for 16 minutes, apply slowness 1
                if (timeSinceAttack < -19200) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 0, true, false));
                }

                if (state != 0) {
                    PacketByteBuf buf = PacketByteBufs.create();

                    buf.writeShort(7);

                    buf.writeInt(state);
                    buf.writeInt(player.getId());

                    for (PlayerEntity playerEntity : world.getPlayers()) {
                        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                            ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
                        }
                    }
                    state = 0;
                }
            }
        }
    }
}
