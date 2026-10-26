package com.example.trialchiefs.trial;

import com.example.trialchiefs.entity.ChiefEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Base de todas las pruebas. */
public abstract class Trial {
	public enum Status { RUNNING, WON, LOST }

	protected final ChiefEntity boss;
	protected final ServerWorld world;
	protected final List<UUID> participants = new ArrayList<>();
	protected int age = 0;

	protected Trial(ChiefEntity boss, ServerWorld world, List<ServerPlayerEntity> players) {
		this.boss = boss;
		this.world = world;
		for (ServerPlayerEntity p : players) {
			participants.add(p.getUuid());
		}
	}

	/** Participantes vivos y cerca del jefe. */
	public List<ServerPlayerEntity> activePlayers() {
		List<ServerPlayerEntity> list = new ArrayList<>();
		for (UUID id : participants) {
			PlayerEntity p = world.getPlayerByUuid(id);
			if (p instanceof ServerPlayerEntity sp && sp.isAlive() && !sp.isSpectator()
					&& sp.squaredDistanceTo(boss) < 80 * 80) {
				list.add(sp);
			}
		}
		return list;
	}

	public final Status tick() {
		age++;
		if (age > 60 && activePlayers().isEmpty()) {
			return Status.LOST;
		}
		return run();
	}

	protected abstract Status run();

	/** 0..1 para la barra de jefe. */
	public abstract float progress();

	public abstract Text title();

	/** Se llama siempre al terminar (ganar, perder o desaparecer el jefe). */
	public void cleanup() {}

	protected void say(Text text) {
		for (ServerPlayerEntity p : activePlayers()) {
			p.sendMessage(text, false);
		}
	}
}
