package com.example.trialchiefs;

import com.example.trialchiefs.item.HeroCallItem;
import com.example.trialchiefs.item.MysterySackItem;
import com.example.trialchiefs.item.RoyalAscentItem;
import com.example.trialchiefs.item.TransmuterItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {
	public static final Item HERO_CALL = register("hero_call", HeroCallItem::new,
			new Item.Settings().maxCount(1).component(ModComponents.USES, HeroCallItem.MAX_USES));

	public static final Item TRANSMUTER = register("transmuter", TransmuterItem::new,
			new Item.Settings().maxCount(1).component(ModComponents.USES, TransmuterItem.MAX_USES));

	/** Durabilidad real = usos: Irrompibilidad, Reparacion y yunque funcionan con las reglas vanilla. */
	public static final Item ROYAL_ASCENT = register("royal_ascent", RoyalAscentItem::new,
			new Item.Settings().maxDamage(RoyalAscentItem.MAX_USES).enchantable(15));

	public static final Item MYSTERY_SACK = register("mystery_sack", MysterySackItem::new,
			new Item.Settings().maxCount(16));

	private ModItems() {}

	private static Item register(String id, Function<Item.Settings, Item> factory, Item.Settings settings) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(TrialChiefs.MOD_ID, id));
		return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(HERO_CALL);
			entries.add(TRANSMUTER);
			entries.add(ROYAL_ASCENT);
			entries.add(MYSTERY_SACK);
		});
	}
}
