package com.example.trialchiefs;

import com.example.trialchiefs.item.HeroCallItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * El enfriamiento REAL vive en el ItemStack (componente LAST_USE) y por eso sobrevive a muerte, dimension,
 * reconexion y reinicios. Aqui solo se restaura el indicador visual (el barrido del icono), que vanilla pierde al reconectar.
 */
public final class CooldownSync {
	private CooldownSync() {}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 != 0) {
				return;
			}
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				for (int i = 0; i < p.getInventory().size(); i++) {
					ItemStack stack = p.getInventory().getStack(i);
					if (stack.isOf(ModItems.HERO_CALL)) {
						long rem = HeroCallItem.remainingCooldown(stack, server.getOverworld().getTime());
						if (rem > 0 && !p.getItemCooldownManager().isCoolingDown(stack)) {
							p.getItemCooldownManager().set(stack, (int) rem);
						}
					}
				}
			}
		});
	}
}
