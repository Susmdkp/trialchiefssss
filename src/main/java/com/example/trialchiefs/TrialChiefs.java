package com.example.trialchiefs;

import net.fabricmc.api.ModInitializer;

public class TrialChiefs implements ModInitializer {
	public static final String MOD_ID = "trialchiefs";

	@Override
	public void onInitialize() {
		ModEntities.init();      // jefes (existente)
		ModComponents.init();
		ModItems.init();
		LootInjection.init();
		AscentManager.init();
		CooldownSync.init();
	}
}
