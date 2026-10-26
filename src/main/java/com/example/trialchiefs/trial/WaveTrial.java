package com.example.trialchiefs.trial;

import com.example.trialchiefs.entity.ChiefEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Sobrevive a 3 oleadas de enemigos. */
public class WaveTrial extends Trial {
	private static final int WAVES = 3;
	private static final int MAX_TICKS = 20 * 60 * 8;

	private int wave = -1;
	private int pause = 100;
	private int waveSize = 1;
	private double mult = 1.0;
	private final List<UUID> mobs = new ArrayList<>();

	public WaveTrial(ChiefEntity boss, ServerWorld world, List<ServerPlayerEntity> players) {
		super(boss, world, players);
		say(Text.translatable("message.trialchiefs.waves.start", WAVES));
	}

	@Override
	protected Status run() {
		if (age > MAX_TICKS) {
			return Status.LOST;
		}
		if (pause > 0) {
			pause--;
			if (pause == 0) {
				spawnWave();
			}
			return Status.RUNNING;
		}
		mobs.removeIf(id -> {
			Entity e = world.getEntity(id);
			return e == null || !e.isAlive();
		});
		if (mobs.isEmpty()) {
			if (wave >= WAVES - 1) {
				return Status.WON;
			}
			pause = 100;
			say(Text.translatable("message.trialchiefs.waves.next"));
		}
		return Status.RUNNING;
	}

	private void spawnWave() {
		wave++;
		mobs.clear();
		mult = 1.0 + 0.6 * (Math.max(1, activePlayers().size()) - 1);
		switch (wave) {
			case 0 -> {
				spawn(EntityType.ZOMBIE, 6);
				spawn(EntityType.SKELETON, 3);
			}
			case 1 -> {
				spawn(EntityType.ZOMBIE, 6);
				spawn(EntityType.SKELETON, 4);
				spawn(EntityType.SPIDER, 3);
				spawn(EntityType.VINDICATOR, 2);
			}
			default -> {
				spawn(EntityType.VINDICATOR, 5);
				spawn(EntityType.PILLAGER, 4);
				spawn(EntityType.ZOMBIE, 6);
				spawn(EntityType.WITCH, 2);
			}
		}
		waveSize = Math.max(1, mobs.size());
	}

	private void spawn(EntityType<? extends MobEntity> type, int base) {
		int count = (int) Math.round(base * mult);
		List<ServerPlayerEntity> targets = activePlayers();
		for (int i = 0; i < count; i++) {
			double ang = world.random.nextDouble() * Math.PI * 2;
			double dist = 9 + world.random.nextDouble() * 6;
			int x = MathHelper.floor(boss.getX() + Math.cos(ang) * dist);
			int z = MathHelper.floor(boss.getZ() + Math.sin(ang) * dist);
			int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
			MobEntity mob = type.spawn(world, new BlockPos(x, y, z), SpawnReason.EVENT);
			if (mob == null) {
				continue;
			}
			mob.setPersistent();
			mob.setGlowing(true);
			// Que no se quemen al sol y arruinen la prueba.
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 60 * 10, 0, false, false));
			if (!targets.isEmpty()) {
				mob.setTarget(targets.get(world.random.nextInt(targets.size())));
			}
			world.spawnParticles(ParticleTypes.POOF, x + 0.5, y + 0.5, z + 0.5, 12, 0.3, 0.5, 0.3, 0.02);
			mobs.add(mob.getUuid());
		}
	}

	@Override
	public float progress() {
		if (wave < 0) {
			return 0f;
		}
		float cleared = 1.0f - (float) mobs.size() / waveSize;
		return (wave + cleared) / WAVES;
	}

	@Override
	public Text title() {
		return Text.translatable("bossbar.trialchiefs.waves", Math.max(wave + 1, 1), WAVES);
	}

	@Override
	public void cleanup() {
		for (UUID id : mobs) {
			Entity e = world.getEntity(id);
			if (e != null) {
				e.discard();
			}
		}
		mobs.clear();
	}
}
