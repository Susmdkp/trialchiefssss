package com.example.trialchiefs.entity;

import com.example.trialchiefs.Rewards;
import com.example.trialchiefs.trial.DuelTrial;
import com.example.trialchiefs.trial.PillarTrial;
import com.example.trialchiefs.trial.Trial;
import com.example.trialchiefs.trial.WaveTrial;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Jefe tribal. Segun su TrialKind propone una prueba distinta al hacerle clic derecho.
 */
public class ChiefEntity extends PathAwareEntity {
	/** Ticks de descanso tras superar la prueba (10 min). */
	private static final int COOLDOWN_WIN = 20 * 60 * 10;
	/** Ticks de descanso tras fallar (30 s). */
	private static final int COOLDOWN_LOSE = 20 * 30;

	private final TrialKind kind;
	private Trial trial;
	private ServerBossBar bar;
	private long cooldownUntil;
	private UUID pendingStarter;
	private boolean duelActive;
	private boolean duelDefeated;

	public ChiefEntity(EntityType<? extends PathAwareEntity> type, World world, TrialKind kind) {
		super(type, world);
		this.kind = kind;
		this.setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.MAX_HEALTH, 250.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.28)
				.add(EntityAttributes.ATTACK_DAMAGE, 12.0)
				.add(EntityAttributes.FOLLOW_RANGE, 40.0)
				.add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.SCALE, 1.6);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		// Solo ataca cuando hay objetivo (es decir, durante el duelo).
		this.goalSelector.add(1, new MeleeAttackGoal(this, 1.1, true));
		this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
		this.goalSelector.add(3, new LookAroundGoal(this));
	}

	// ---------- Interaccion ----------

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (hand != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}
		this.pendingStarter = player.getUuid(); // se procesa en mobTick (lado servidor)
		return ActionResult.SUCCESS;
	}

	@Override
	protected void mobTick(ServerWorld world) {
		super.mobTick(world);

		if (pendingStarter != null) {
			PlayerEntity p = world.getPlayerByUuid(pendingStarter);
			pendingStarter = null;
			if (p instanceof ServerPlayerEntity sp) {
				tryStart(world, sp);
			}
		}
		if (trial != null) {
			tickTrial(world);
		}
	}

	private void tryStart(ServerWorld world, ServerPlayerEntity starter) {
		if (trial != null) {
			starter.sendMessage(Text.translatable("message.trialchiefs.busy"), true);
			return;
		}
		long left = cooldownUntil - world.getTime();
		if (left > 0) {
			starter.sendMessage(Text.translatable("message.trialchiefs.cooldown", left / 1200 + 1), true);
			return;
		}
		List<ServerPlayerEntity> group = new ArrayList<>(world.getPlayers(
				p -> p.isAlive() && !p.isSpectator() && p.squaredDistanceTo(this) < 20 * 20));
		if (!group.contains(starter)) {
			group.add(starter);
		}
		trial = switch (kind) {
			case WAVES -> new WaveTrial(this, world, group);
			case PILLAR -> new PillarTrial(this, world, group);
			case DUEL -> new DuelTrial(this, world, group);
		};
		bar = new ServerBossBar(trial.title(), kind.color, BossBar.Style.PROGRESS);
		world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 3.0f, 0.8f);
	}

	private void tickTrial(ServerWorld world) {
		Trial.Status status = trial.tick();
		for (ServerPlayerEntity p : world.getPlayers(pl -> pl.squaredDistanceTo(this) < 64 * 64)) {
			bar.addPlayer(p);
		}
		for (ServerPlayerEntity p : new ArrayList<>(bar.getPlayers())) {
			if (p.squaredDistanceTo(this) > 70 * 70) {
				bar.removePlayer(p);
			}
		}
		bar.setName(trial.title());
		bar.setPercent(MathHelper.clamp(trial.progress(), 0.0f, 1.0f));

		if (status == Trial.Status.WON) {
			for (ServerPlayerEntity p : trial.activePlayers()) {
				Rewards.give(world, p, kind);
			}
			world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 2.5, getZ(), 120, 0.8, 1.2, 0.8, 0.6);
			world.playSound(null, getX(), getY(), getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.HOSTILE, 3.0f, 1.0f);
			finishTrial(world, COOLDOWN_WIN);
		} else if (status == Trial.Status.LOST) {
			for (ServerPlayerEntity p : world.getPlayers(pl -> pl.squaredDistanceTo(this) < 70 * 70)) {
				p.sendMessage(Text.translatable("message.trialchiefs.lose"), false);
			}
			finishTrial(world, COOLDOWN_LOSE);
		}
	}

	private void finishTrial(ServerWorld world, int cooldownTicks) {
		trial.cleanup();
		trial = null;
		if (bar != null) {
			bar.clearPlayers();
			bar = null;
		}
		cooldownUntil = world.getTime() + cooldownTicks;
	}

	@Override
	public void remove(RemovalReason reason) {
		// Si el jefe desaparece (o se descarga el chunk) a mitad de prueba, limpiamos todo.
		if (trial != null) {
			trial.cleanup();
			trial = null;
		}
		if (bar != null) {
			bar.clearPlayers();
			bar = null;
		}
		super.remove(reason);
	}

	// ---------- Dano: solo vulnerable durante su duelo ----------

	@Override
	public boolean damage(ServerWorld world, DamageSource source, float amount) {
		if (source.isOf(DamageTypes.GENERIC_KILL) || source.isOf(DamageTypes.OUT_OF_WORLD)) {
			return super.damage(world, source, amount);
		}
		if (!duelActive || duelDefeated) {
			return false;
		}
		// Nunca muere: al quedar casi sin vida, el duelo se da por ganado.
		if (getHealth() - amount <= getMaxHealth() * 0.04f) {
			duelDefeated = true;
			setHealth(Math.max(1.0f, getMaxHealth() * 0.04f));
			return false;
		}
		return super.damage(world, source, amount);
	}

	public boolean isDuelDefeated() { return duelDefeated; }
	public void setDuelDefeated(boolean v) { this.duelDefeated = v; }
	public void setDuelActive(boolean v) { this.duelActive = v; }

	// ---------- Sonidos ----------

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_PIGLIN_BRUTE_AMBIENT; }

	@Override
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_PIGLIN_BRUTE_HURT; }

	@Override
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_PIGLIN_BRUTE_DEATH; }
}
