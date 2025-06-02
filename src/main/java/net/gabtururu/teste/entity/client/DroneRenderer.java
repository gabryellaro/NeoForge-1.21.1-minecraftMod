package net.gabtururu.teste.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.gabtururu.teste.TutorialMod;
import net.gabtururu.teste.entity.custom.DroneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DroneRenderer extends MobRenderer<DroneEntity, DroneModel<DroneEntity>> {
    public DroneRenderer(EntityRendererProvider.Context context) {
        super(context, new DroneModel<>(context.bakeLayer(DroneModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(DroneEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/entity/drone/drone.png");
    }

    @Override
    public void render(DroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.45f, 0.45f, 0.45f);
        } else {
            poseStack.scale(3f, 3f, 3f);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

}
