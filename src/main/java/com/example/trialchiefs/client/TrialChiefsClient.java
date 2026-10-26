package com.example.trialchiefs.client;

import com.example.trialchiefs.ModComponents;
import com.example.trialchiefs.ModEntities;
import com.example.trialchiefs.ModItems;
import com.example.trialchiefs.item.HeroCallItem;
import com.example.trialchiefs.item.TransmuterItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class TrialChiefsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.SUN_CHIEF, ctx -> new ChiefRenderer(ctx, "sun_chief"));
		EntityRendererRegistry.register(ModEntities.PILLAR_MASTER, ctx -> new ChiefRenderer(ctx, "pillar_master"));
		EntityRendererRegistry.register(ModEntities.STONE_GUARDIAN, ctx -> new ChiefRenderer(ctx, "stone_guardian"));

		// Usos restantes y enfriamiento en la descripcion (se calcula en el cliente a partir de los datos del item).
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> addTooltip(stack, lines));
	}

	private static void addTooltip(ItemStack stack, List<Text> lines) {
		if (stack.isOf(ModItems.HERO_CALL)) {
			int uses = stack.getOrDefault(ModComponents.USES, HeroCallItem.MAX_USES);
			lines.add(Text.translatable("tooltip.trialchiefs.uses", uses, HeroCallItem.MAX_USES).formatted(Formatting.GRAY));
			long now = MinecraftClient.getInstance().world != null ? MinecraftClient.getInstance().world.getTime() : 0;
			long rem = HeroCallItem.remainingCooldown(stack, now);
			if (rem > 0) {
				lines.add(Text.translatable("tooltip.trialchiefs.cooldown", rem / 1200, (rem / 20) % 60).formatted(Formatting.RED));
			} else {
				lines.add(Text.translatable("tooltip.trialchiefs.ready").formatted(Formatting.GREEN));
			}
		} else if (stack.isOf(ModItems.TRANSMUTER)) {
			int uses = stack.getOrDefault(ModComponents.USES, TransmuterItem.MAX_USES);
			lines.add(Text.translatable("tooltip.trialchiefs.uses", uses, TransmuterItem.MAX_USES).formatted(Formatting.GRAY));
			lines.add(Text.translatable("tooltip.trialchiefs.mirror_hint").formatted(Formatting.DARK_GRAY));
		} else if (stack.isOf(ModItems.ROYAL_ASCENT)) {
			int max = stack.getMaxDamage();
			lines.add(Text.translatable("tooltip.trialchiefs.uses", max - stack.getDamage(), max).formatted(Formatting.GRAY));
		}
	}
}
