package com.example.trialchiefs.item;

import com.example.trialchiefs.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Transmutador Metalurgico: espejo en la mano secundaria + mineral en la principal.
 * 70% de exito, 30 usos, transmuta 1 unidad por intento.
 */
public class TransmuterItem extends Item {
	public static final int MAX_USES = 30;
	public static final float SUCCESS_CHANCE = 0.70f;

	public TransmuterItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (hand != Hand.OFF_HAND) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.trialchiefs.transmute.offhand"), true);
			}
			return ActionResult.PASS;
		}
		ItemStack mirror = player.getStackInHand(Hand.OFF_HAND);
		ItemStack ore = player.getStackInHand(Hand.MAIN_HAND);
		Item result = Transmutation.next(ore);
		if (result == null) {
			return ActionResult.PASS; // intento invalido: no gasta usos
		}
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		int uses = mirror.getOrDefault(ModComponents.USES, MAX_USES);
		if (uses <= 0) {
			player.sendMessage(Text.translatable("message.trialchiefs.transmute.no_uses"), true);
			return ActionResult.FAIL;
		}
		mirror.set(ModComponents.USES, uses - 1); // todo intento valido gasta un uso

		ServerWorld sw = (ServerWorld) world;
		if (world.random.nextFloat() < SUCCESS_CHANCE) {
			ItemStack out = new ItemStack(result, 1);
			if (ore.getCount() == 1) {
				player.setStackInHand(Hand.MAIN_HAND, out);
			} else {
				ore.decrement(1);
				player.getInventory().offerOrDrop(out); // si no cabe, cae al suelo: nada se pierde
			}
			sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.2, player.getZ(), 12, 0.4, 0.4, 0.4, 0.0);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.2f);
			player.sendMessage(Text.translatable("message.trialchiefs.transmute.success", uses - 1), true);
		} else {
			sw.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.2, player.getZ(), 8, 0.3, 0.3, 0.3, 0.01);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.6f, 0.8f);
			player.sendMessage(Text.translatable("message.trialchiefs.transmute.fail", uses - 1), true);
		}
		return ActionResult.SUCCESS;
	}
}
