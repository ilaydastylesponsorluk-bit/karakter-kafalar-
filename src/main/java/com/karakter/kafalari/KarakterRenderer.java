package com.karakter.kafalari;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Karakterleri oyuncu gövdesiyle çizer; skin: textures/entity/<isim>.png */
public class KarakterRenderer extends HumanoidMobRenderer<KarakterMob, HumanoidModel<KarakterMob>> {
    public KarakterRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(KarakterMob mob) {
        String isim = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(KarakterKafalari.MODID, "textures/entity/" + isim + ".png");
    }

    public static void kaydet(EntityRenderersEvent.RegisterRenderers event) {
        for (var tip : KarakterKafalari.MOBLAR) {
            event.registerEntityRenderer(tip.get(), KarakterRenderer::new);
        }
    }
}
