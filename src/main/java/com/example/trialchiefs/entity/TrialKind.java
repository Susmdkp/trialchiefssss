package com.example.trialchiefs.entity;

import net.minecraft.entity.boss.BossBar;

public enum TrialKind {
	WAVES(BossBar.Color.YELLOW),
	PILLAR(BossBar.Color.BLUE),
	DUEL(BossBar.Color.RED);

	public final BossBar.Color color;

	TrialKind(BossBar.Color color) {
		this.color = color;
	}
}
