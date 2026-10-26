package com.example.trialchiefs.item;

import com.example.trialchiefs.ModComponents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Llamado del Heroe: Heroe de la Aldea III (6000 ticks), 10 usos, 36000 ticks de enfriamiento. */
public class HeroCallItem extends Item {
	public static final int MAX_USES = 10;
	public static final int COOLDOWN_TICKS = 36000;
	public static final int EFFECT_TICKS = 6000;
	public static final int AMPLIFIER = 2; // nivel III

	public HeroCallItem(Settings settings) {
		super(settings);
	}

	/** Ticks de enfriamiento restantes segun los datos guardados en el item (0 = listo). */
	public static long remainingCooldown(ItemStack stack, long now) {
		Long last = stack.get(ModComponents.LAST_USE);
		if (last == null) {
			return 0;
		}
		return MathHelper.clamp(last + COOLDOWN_TICKS - now, 0, COOLDOWN_TICKS);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (world.isClient()) {
			return stack.getOrDefault(ModComponents.USES, MAX_USES) > 0 ? ActionResult.SUCCESS : ActionResult.PASS;
		}
		int uses = stack.getOrDefault(ModComponents.USES, MAX_USES);
		if (uses <= 0) {
			player.sendMessage(Text.translatable("message.trialchiefs.hero.no_uses"), true);
			return ActionResult.FAIL;
		}
		long now = world.getTime();
		long remaining = remainingCooldown(stack, now);
		if (remaining > 0) {
			player.sendMessage(Text.translatable("message.trialchiefs.hero.cooldown", remaining / 1200, (remaining / 20) % 60), true);
			return ActionResult.FAIL;
		}
		// addStatusEffect devuelve false si ya hay un efecto igual o mejor: en ese caso no se gasta el uso.
		boolean applied = player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, EFFECT_TICKS, AMPLIFIER, false, true, true));
		if (!applied) {
			player.sendMessage(Text.translatable("message.trialchiefs.hero.already"), true);
			return ActionResult.FAIL;
		}
		stack.set(ModComponents.USES, uses - 1);
		stack.set(ModComponents.LAST_USE, now);
		player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);
		player.sendMessage(Text.translatable("message.trialchiefs.hero.used", uses - 1), true);
		return ActionResult.SUCCESS;
	}
}
