package com.example.trialchiefs;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Gestiona los ascensos de "Ascenso Real" (solo servidor):
 *  - velocidad inicial calculada con la fisica vanilla para que el apice sea exactamente la altura pedida;
 *  - columna de viento = solo particulas (no toca bloques);
 *  - proteccion contra dano por CAIDA solo hasta aterrizar (o agua, lava, trepar, montura, desconexion o muerte).
 */
public final class AscentManager {
	private static final Map<UUID, State> ACTIVE = new HashMap<>();

	private static final class State {
		final ServerWorld world;
		final double startY;
		double lastY;
		int ticks;
		boolean rising = true;

		State(ServerWorld world, double startY) {
			this.world = world;
			this.startY = startY;
			this.lastY = startY;
		}
	}

	private AscentManager() {}

	public static boolean isActive(UUID id) {
		return ACTIVE.containsKey(id);
	}

	/** Velocidad vertical inicial para alcanzar `rise` bloques con gravedad 0.08 y rozamiento 0.98 (jugador). */
	static double launchVelocity(double rise) {
		double lo = 0.1, hi = 6.0;
		for (int i = 0; i < 50; i++) {
			double mid = (lo + hi) / 2;
			if (apex(mid) < rise) lo = mid; else hi = mid;
		}
		return (lo + hi) / 2;
	}

	private static double apex(double v0) {
		double pos = 0, v = v0, max = 0;
		for (int i = 0; i < 500; i++) {
			pos += v;
			max = Math.max(max, pos);
			v = (v - 0.08) * 0.98;
			if (v <= 0) break;
		}
		return max;
	}

	public static void start(ServerPlayerEntity p, ServerWorld world, double rise) {
		ACTIVE.put(p.getUuid(), new State(world, p.getY()));
		Vec3d v = p.getVelocity();
		p.setVelocity(v.x, launchVelocity(rise), v.z);
		p.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(p));
		world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.0f, 1.2f);
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, State>> it = ACTIVE.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, State> e = it.next();
			State s = e.getValue();
			ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
			if (p == null || !p.isAlive() || p.isSpectator()) {
				it.remove();
				continue;
			}
			s.ticks++;
			if (s.rising) {
				if (s.ticks % 2 == 0) {
					column(s, p);
				}
				double y = p.getY();
				boolean lifted = y > s.startY + 1.0;
				// Fin de la subida: apice alcanzado, techo, planeo o tiempo limite.
				if ((lifted && y <= s.lastY + 0.01) || p.isGliding() || s.ticks > 60) {
					s.rising = false;
				}
				s.lastY = y;
			} else if ((s.ticks > 5 && (p.isOnGround() || p.isTouchingWater() || p.isInLava() || p.hasVehicle() || p.isClimbing()))
					|| s.ticks > 6000) {
				it.remove(); // se acaba la proteccion: no es inmunidad permanente
			}
		}
	}

	/** Columna de viento: solo particulas, desde el punto de salida hasta la posicion actual. */
	private static void column(State s, ServerPlayerEntity p) {
		for (double y = s.startY; y <= p.getY(); y += 1.0) {
			s.world.spawnParticles(ParticleTypes.CLOUD, p.getX(), y, p.getZ(), 2, 0.3, 0.0, 0.3, 0.01);
			s.world.spawnParticles(ParticleTypes.END_ROD, p.getX(), y, p.getZ(), 1, 0.25, 0.0, 0.25, 0.0);
		}
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(AscentManager::tick);
		// Cancela SOLO el dano por caida mientras dura la maniobra.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
				!(entity instanceof ServerPlayerEntity p && ACTIVE.containsKey(p.getUuid()) && source.isOf(DamageTypes.FALL)));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ACTIVE.remove(handler.getPlayer().getUuid()));
	}
}
