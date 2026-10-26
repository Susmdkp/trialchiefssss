package com.example.trialchiefs;

import com.example.trialchiefs.entity.TrialKind;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Recompensas (todo vanilla) segun la prueba superada. Edita aqui para equilibrarlas. */
public final class Rewards {
	private Rewards() {}

	public static void give(ServerWorld w, ServerPlayerEntity player, TrialKind kind) {
		List<ItemStack> items = new ArrayList<>();
		int xp;
		switch (kind) {
			case WAVES -> {
				items.add(new ItemStack(Items.TOTEM_OF_UNDYING));
				items.add(new ItemStack(Items.GOLDEN_APPLE, 8));
				items.add(ench(w, ench(w, new ItemStack(Items.DIAMOND_SWORD), Enchantments.SHARPNESS, 4), Enchantments.UNBREAKING, 3));
				xp = 300;
			}
			case PILLAR -> {
				items.add(ench(w, ench(w, new ItemStack(Items.ELYTRA), Enchantments.UNBREAKING, 3), Enchantments.MENDING, 1));
				items.add(new ItemStack(Items.FIREWORK_ROCKET, 48));
				items.add(ench(w, ench(w, new ItemStack(Items.DIAMOND_BOOTS), Enchantments.FEATHER_FALLING, 4), Enchantments.UNBREAKING, 3));
				xp = 400;
			}
			default -> {
				items.add(new ItemStack(Items.NETHERITE_INGOT, 2));
				items.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
				items.add(new ItemStack(Items.TOTEM_OF_UNDYING));
				items.add(ench(w, ench(w, ench(w, new ItemStack(Items.DIAMOND_PICKAXE),
						Enchantments.EFFICIENCY, 5), Enchantments.UNBREAKING, 3), Enchantments.FORTUNE, 3));
				xp = 600;
			}
		}
		for (ItemStack s : items) {
			player.getInventory().offerOrDrop(s);
		}
		player.addExperience(xp);
		player.sendMessage(Text.translatable("message.trialchiefs.win"), false);
	}

	private static ItemStack ench(ServerWorld w, ItemStack stack, RegistryKey<Enchantment> key, int level) {
		stack.addEnchantment(w.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(key), level);
		return stack;
	}
}
