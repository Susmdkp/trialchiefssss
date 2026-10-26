package com.example.trialchiefs.item;

import com.example.trialchiefs.AscentManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Ascenso Real: impulso vertical de 15 bloques. La durabilidad (50) ES el contador de usos, asi que
 * Irrompibilidad (probabilidad de no gastar) y Reparacion (XP) siguen las reglas vanilla, sin recargas "extra".
 */
public class RoyalAscentItem extends Item {
	public static final int MAX_USES = 50;
	public static final int RISE = 15;
	public static final int COOLDOWN_TICKS = 20; // evita spam / doble clic

	public RoyalAscentItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayerEntity sp)) {
			return ActionResult.PASS;
		}
		ItemStack stack = player.getStackInHand(hand);
		if (AscentManager.isActive(sp.getUuid()) || sp.hasVehicle() || sp.isGliding() || sp.isSpectator()) {
			sp.sendMessage(Text.translatable("message.trialchiefs.ascent.busy"), true);
			return ActionResult.FAIL;
		}
		// Altura permitida: limite del mundo y bloques solidos encima.
		int worldLimit = world.getTopYInclusive() - (int) Math.ceil(sp.getY()) - 2;
		int max = Math.min(RISE, worldLimit);
		int clear = 0;
		for (int h = 1; h <= max; h++) {
			if (!world.isSpaceEmpty(sp, sp.getBoundingBox().offset(0, h, 0))) {
				break;
			}
			clear = h;
		}
		if (clear < 2) {
			sp.sendMessage(Text.translatable("message.trialchiefs.ascent.no_space"), true);
			return ActionResult.FAIL; // no gasta durabilidad
		}
		AscentManager.start(sp, (ServerWorld) world, clear);

		// Durabilidad = usos. stack.damage aplica Irrompibilidad y rompe el item al llegar a 0.
		stack.damage(1, sp, LivingEntity.getSlotForHand(hand));
		player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);
		if (!stack.isEmpty()) {
			sp.sendMessage(Text.translatable("message.trialchiefs.ascent.used", stack.getMaxDamage() - stack.getDamage()), true);
		}
		return ActionResult.SUCCESS;
	}
}
