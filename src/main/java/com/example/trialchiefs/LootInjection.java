package com.example.trialchiefs;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Anade UN pool extra a las tablas vanilla de zombi, bruja y saqueador (no toca las recompensas vanilla).
 * El azar lo resuelve el servidor al generar el botin. random_chance NO usa Looting.
 */
public final class LootInjection {
	public static final float DROP_CHANCE = 0.05f;

	private static final Set<Identifier> ELIGIBLE = Set.of(
			Identifier.of("minecraft", "entities/zombie"),
			Identifier.of("minecraft", "entities/witch"),
			Identifier.of("minecraft", "entities/pillager"));

	private LootInjection() {}

	public static void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && ELIGIBLE.contains(key.getValue())) {
				tableBuilder.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.conditionally(RandomChanceLootCondition.builder(DROP_CHANCE))
						.with(ItemEntry.builder(ModItems.MYSTERY_SACK)));
			}
		});
	}
}
