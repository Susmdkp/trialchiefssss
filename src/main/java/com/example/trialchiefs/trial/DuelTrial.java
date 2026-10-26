package com.example.trialchiefs.trial;

import com.example.trialchiefs.entity.ChiefEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

/** Duelo contra el propio jefe, que ademas golpea el suelo con una onda expansiva. */
public class DuelTrial extends Trial {
	private static final int MAX_TICKS = 20 * 60 * 8;

	public DuelTrial(ChiefEntity boss, ServerWorld world, List<ServerPlayerEntity> players) {
		super(boss, world, players);
		boss.setDuelDefeated(false);
		boss.setDuelActive(true);
		boss.setHealth(boss.getMaxHealth());
		say(Text.translatable("message.trialchiefs.duel.start"));
	}

	@Override
	protected Status run() {
		if (age > MAX_TICKS) {
			return Status.LOST;
		}
		if (boss.isDuelDefeated()) {
			return Status.WON;
		}
		LivingEntity target = boss.getTarget();
		if (target == null || !target.isAlive()) {
			ServerPlayerEntity nearest = null;
			double best = Double.MAX_VALUE;
			for (ServerPlayerEntity p : activePlayers()) {
				double d = p.squaredDistanceTo(boss);
				if (d < best) {
					best = d;
					nearest = p;
				}
			}
			boss.setTarget(nearest);
		}
		int phase = age % 140;
		if (phase == 100) { // aviso
			world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, boss.getX(), boss.getY() + 3.2, boss.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
			world.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 2.0f, 1.3f);
		} else if (phase == 0) {
			slam();
		}
		return Status.RUNNING;
	}

	private void slam() {
		for (ServerPlayerEntity p : activePlayers()) {
			if (p.squaredDistanceTo(boss) < 6 * 6) {
				p.damage(world, world.getDamageSources().mobAttack(boss), 8.0f);
				p.takeKnockback(1.3, boss.getX() - p.getX(), boss.getZ() - p.getZ());
				p.scheduleVelocityUpdate();
			}
		}
		world.spawnParticles(ParticleTypes.EXPLOSION, boss.getX(), boss.getY() + 0.3, boss.getZ(), 8, 2.5, 0.2, 2.5, 0.0);
		world.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.HOSTILE, 3.0f, 0.6f);
	}

	@Override
	public float progress() {
		return boss.getHealth() / boss.getMaxHealth();
	}

	@Override
	public Text title() {
		return Text.translatable("bossbar.trialchiefs.duel");
	}

	@Override
	public void cleanup() {
		boss.setDuelActive(false);
		boss.setDuelDefeated(false);
		boss.setTarget(null);
		boss.setHealth(boss.getMaxHealth());
	}
}
