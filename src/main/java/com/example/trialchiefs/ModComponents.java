package com.example.trialchiefs;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

/** Componentes de datos persistentes (se guardan con el ItemStack: mundo, inventario, cofres, muerte...). */
public final class ModComponents {
	/** Usos restantes (nunca negativo). */
	public static final ComponentType<Integer> USES = Registry.register(
			Registries.DATA_COMPONENT_TYPE, Identifier.of(TrialChiefs.MOD_ID, "uses"),
			ComponentType.<Integer>builder().codec(Codecs.NON_NEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build());

	/** Tick de juego (global, compartido por dimensiones) de la ultima activacion con exito. */
	public static final ComponentType<Long> LAST_USE = Registry.register(
			Registries.DATA_COMPONENT_TYPE, Identifier.of(TrialChiefs.MOD_ID, "last_use"),
			ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build());

	private ModComponents() {}

	public static void init() {}
}
