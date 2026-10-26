package com.example.trialchiefs.client;

import com.example.trialchiefs.TrialChiefs;
import com.example.trialchiefs.entity.ChiefEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

/** Reutiliza el modelo humanoide vanilla (escalado x1.6 por atributo) con una textura propia. */
public class ChiefRenderer extends BipedEntityRenderer<ChiefEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {
	private final Identifier texture;

	public ChiefRenderer(EntityRendererFactory.Context ctx, String name) {
		super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.6f);
		this.texture = Identifier.of(TrialChiefs.MOD_ID, "textures/entity/" + name + ".png");
	}

	@Override
	public BipedEntityRenderState createRenderState() {
		return new BipedEntityRenderState();
	}

	@Override
	public Identifier getTexture(BipedEntityRenderState state) {
		return texture;
	}
}
