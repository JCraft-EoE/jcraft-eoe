package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KnuckledusterItem extends JCraftWeaponItem {

    public int charges = 0;

    public KnuckledusterItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        JCraft.LOGGER.info("Clicked");
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.of("§9wanna frow hands"));
        tooltip.add(Text.of("§aStanding LMB - "));
        tooltip.add(Text.of("§cCrouching LMB - "));
        tooltip.add(Text.of("§bStanding RMB - "));
        tooltip.add(Text.of("§dCrouching RMB - "));

        super.appendTooltip(stack, world, tooltip, context);
    }

    /*
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
       return TypedActionResult.consume(itemStack);
    }
     */

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        /*
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity player) {
                if (state != 0) {
                    PacketByteBuf buf = PacketByteBufs.create();

                    buf.writeShort(7);

                    buf.writeInt(state);
                    buf.writeInt(player.getId());

                    for (PlayerEntity sendPlayer : world.getPlayers()) {
                        ServerPlayNetworking.send((ServerPlayerEntity) sendPlayer, JCraft.serverFeedbackChannel, buf);
                    }
                    state = 0;
                }
            }
        }
         */
    }
}
