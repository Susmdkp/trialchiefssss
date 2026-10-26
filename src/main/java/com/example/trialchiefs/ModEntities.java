package com.example.trialchiefs;

import com.example.trialchiefs.entity.ChiefEntity;
import com.example.trialchiefs.entity.TrialKind;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public class ModEntities {
	public static final EntityType<ChiefEntity> SUN_CHIEF =
			register("sun_chief", (t, w) -> new ChiefEntity(t, w, TrialKind.WAVES));
	public static final EntityType<ChiefEntity> PILLAR_MASTER =
			register("pillar_master", (t, w) -> new ChiefEntity(t, w, TrialKind.PILLAR));
	public static final EntityType<ChiefEntity> STONE_GUARDIAN =
			register("stone_guardian", (t, w) -> new ChiefEntity(t, w, TrialKind.DUEL));

	private static <T extends Entity> EntityType<T> register(String id, EntityType.EntityFactory<T> factory) {
		RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(TrialChiefs.MOD_ID, id));
		EntityType<T> type = EntityType.Builder.create(factory, SpawnGroup.CREATURE)
				.dimensions(0.7f, 2.0f)
				.maxTrackingRange(10)
				.build(key);
		return Registry.register(Registries.ENTITY_TYPE, key, type);
	}

	public static void init() {
		for (EntityType<ChiefEntity> type : new EntityType[]{SUN_CHIEF, PILLAR_MASTER, STONE_GUARDIAN}) {
			FabricDefaultAttributeRegistry.register(type, ChiefEntity.createAttributes());
			SpawnRestriction.register(type, SpawnLocationTypes.ON_GROUND,
					Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
			// Aparicion natural MUY rara (peso 1). Sube el peso si quieres verlos mas.
			BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE, type, 1, 1, 1);
		}
	}
}
