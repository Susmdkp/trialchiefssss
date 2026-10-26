package com.example.trialchiefs.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** Lista EXPLICITA de conversiones validas. Todo lo que no este aqui no se transmuta. */
public final class Transmutation {
	private static final Map<Item, Item> NEXT = new HashMap<>();

	static {
		// lingote / gema
		chain(Items.COPPER_INGOT, Items.IRON_INGOT, Items.GOLD_INGOT, Items.EMERALD, Items.DIAMOND);
		// bloques
		chain(Items.COPPER_BLOCK, Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.EMERALD_BLOCK, Items.DIAMOND_BLOCK);
		// minerales
		chain(Items.COPPER_ORE, Items.IRON_ORE, Items.GOLD_ORE, Items.EMERALD_ORE, Items.DIAMOND_ORE);
		chain(Items.DEEPSLATE_COPPER_ORE, Items.DEEPSLATE_IRON_ORE, Items.DEEPSLATE_GOLD_ORE,
				Items.DEEPSLATE_EMERALD_ORE, Items.DEEPSLATE_DIAMOND_ORE);
		// en bruto: solo existen cobre, hierro y oro (el oro bruto no tiene siguiente nivel)
		chain(Items.RAW_COPPER, Items.RAW_IRON, Items.RAW_GOLD);
		chain(Items.RAW_COPPER_BLOCK, Items.RAW_IRON_BLOCK, Items.RAW_GOLD_BLOCK);
	}

	private Transmutation() {}

	private static void chain(Item... items) {
		for (int i = 0; i < items.length - 1; i++) {
			NEXT.put(items[i], items[i + 1]);
		}
	}

	/** Resultado de transmutar UN item, o null si no hay equivalencia (o el stack tiene datos personalizados). */
	@Nullable
	public static Item next(ItemStack stack) {
		if (stack.isEmpty() || !stack.getComponentChanges().isEmpty()) {
			return null; // nombres, encantamientos, etc.: no se tocan para no perder datos
		}
		return NEXT.get(stack.getItem());
	}
}
