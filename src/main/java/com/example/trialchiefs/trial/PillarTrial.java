package com.example.trialchiefs.trial;

import com.example.trialchiefs.entity.ChiefEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El jefe levanta un pilar gigante con una escalera en espiral (con saltos y bloques de hielo azul).
 * Hay que llegar a la cima antes de que se acabe el tiempo. Al terminar, el terreno se restaura.
 */
public class PillarTrial extends Trial {
	private static final int HEIGHT = 36;
	private static final int TIME = 20 * 60 * 5;

	/** Anillo de 16 celdas alrededor del nucleo 3x3, en orden (cada celda es adyacente a la siguiente). */
	private static final int[][] RING = new int[16][];
	static {
		int i = 0;
		for (int dx = -2; dx <= 2; dx++) RING[i++] = new int[]{dx, -2};
		for (int dz = -1; dz <= 2; dz++) RING[i++] = new int[]{2, dz};
		for (int dx = 1; dx >= -2; dx--) RING[i++] = new int[]{dx, 2};
		for (int dz = 1; dz >= -1; dz--) RING[i++] = new int[]{-2, dz};
	}

	private final Map<BlockPos, BlockState> backup = new HashMap<>();
	private final int cx;
	private final int cz;
	private final int ground; // y del bloque del suelo
	private int timeLeft = TIME;

	public PillarTrial(ChiefEntity boss, ServerWorld world, List<ServerPlayerEntity> players) {
		super(boss, world, players);
		this.cx = MathHelper.floor(boss.getX()) + 8;
		this.cz = MathHelper.floor(boss.getZ());
		this.ground = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx, cz) - 1;
		build();
		say(Text.translatable("message.trialchiefs.pillar.start"));
	}

	private void place(BlockPos pos, BlockState state) {
		backup.putIfAbsent(pos.toImmutable(), world.getBlockState(pos));
		world.setBlockState(pos, state, Block.NOTIFY_ALL);
	}

	private void build() {
		BlockState brick = Blocks.STONE_BRICKS.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();
		int top = ground + HEIGHT + 1;

		// Despeja el anillo para que no estorbe el terreno.
		for (int[] c : RING) {
			for (int y = ground + 1; y <= top + 1; y++) {
				BlockPos p = new BlockPos(cx + c[0], y, cz + c[1]);
				if (!world.getBlockState(p).isAir()) {
					place(p, air);
				}
			}
		}
		// Nucleo 3x3.
		for (int y = ground + 1; y <= top; y++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					place(new BlockPos(cx + dx, y, cz + dz), brick);
				}
			}
		}
		place(new BlockPos(cx, top, cz), Blocks.GOLD_BLOCK.getDefaultState());

		// Escalera en espiral: +1 de altura por escalon; cada 8 escalones hay un hueco que saltar;
		// cada 6 escalones es hielo azul (resbala).
		int idx = 0;
		for (int i = 1; i <= HEIGHT; i++) {
			idx += (i % 8 == 0) ? 2 : 1;
			int[] c = RING[idx % 16];
			BlockState state = (i % 6 == 0) ? Blocks.BLUE_ICE.getDefaultState() : brick;
			place(new BlockPos(cx + c[0], ground + i, cz + c[1]), state);
		}
	}

	@Override
	protected Status run() {
		timeLeft--;
		if (timeLeft <= 0) {
			return Status.LOST;
		}
		List<ServerPlayerEntity> players = activePlayers();
		if (age % 20 == 0) {
			// Caida lenta: la prueba va de escalar, no de morir por fall damage.
			for (ServerPlayerEntity p : players) {
				p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 0, false, false, true));
			}
		}
		double goalY = ground + HEIGHT + 2 - 0.1;
		for (ServerPlayerEntity p : players) {
			if (p.getY() >= goalY && Math.abs(p.getX() - (cx + 0.5)) <= 1.6 && Math.abs(p.getZ() - (cz + 0.5)) <= 1.6) {
				return Status.WON;
			}
		}
		return Status.RUNNING;
	}

	@Override
	public float progress() {
		return (float) timeLeft / TIME;
	}

	@Override
	public Text title() {
		return Text.translatable("bossbar.trialchiefs.pillar", timeLeft / 20);
	}

	@Override
	public void cleanup() {
		for (Map.Entry<BlockPos, BlockState> e : backup.entrySet()) {
			world.setBlockState(e.getKey(), e.getValue(), Block.NOTIFY_ALL);
		}
		backup.clear();
	}
}
