package com.example.trialchiefs.item;

import com.example.trialchiefs.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Saco misterioso: al abrirlo (servidor) da EXACTAMENTE un objeto, 1/3 cada uno. */
public class MysterySackItem extends Item {
	public MysterySackItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		ItemStack sack = player.getStackInHand(hand);
		Item reward = switch (world.random.nextInt(3)) {
			case 0 -> ModItems.HERO_CALL;
			case 1 -> ModItems.TRANSMUTER;
			default -> ModItems.ROYAL_ASCENT;
		};
		sack.decrementUnlessCreative(1, player);
		player.getInventory().offerOrDrop(new ItemStack(reward));
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0f, 0.7f);
		player.sendMessage(Text.translatable("message.trialchiefs.sack.opened", new ItemStack(reward).getName()), true);
		return ActionResult.SUCCESS;
	}
}
