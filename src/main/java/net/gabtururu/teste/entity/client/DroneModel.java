package net.gabtururu.teste.entity.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.gabtururu.teste.TutorialMod;
import net.gabtururu.teste.entity.custom.DroneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class DroneModel<T extends DroneEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "drone"), "main");
    private final ModelPart Body;
    private final ModelPart helix;
    private final ModelPart root;

    public DroneModel(ModelPart root) {
        this.root = root;
        this.Body = root.getChild("Body");
        this.helix = root.getChild("helix");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(28, 0).addBox(-3.0F, -1.75F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(36, 9).addBox(-2.5F, -2.05F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

        PartDefinition cube_r1 = Body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 38).addBox(-4.0F, -1.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.001F))
                .texOffs(-6, 13).addBox(-4.0F, 0.0F, -12.0F, 1.0F, 1.0F, 16.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(5.25F, -1.0F, 0.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r2 = Body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 38).addBox(-3.64F, -1.0F, 3.06F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(-6.0F, -1.0F, 0.75F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r3 = Body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 38).addBox(-3.82F, -1.0F, 3.025F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(-5.5F, -1.0F, -10.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r4 = Body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 38).addBox(-3.84F, -1.0F, 3.07F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(4.75F, -1.0F, -10.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition arm_r1 = Body.addOrReplaceChild("arm_r1", CubeListBuilder.create().texOffs(-6, 13).addBox(-4.0F, -1.0F, -12.0F, 1.0F, 1.0F, 16.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(-0.75F, 0.0F, 5.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition helix = partdefinition.addOrReplaceChild("helix", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition helix1 = helix.addOrReplaceChild("helix1", CubeListBuilder.create().texOffs(0, 30).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.25F, -3.0F, 5.25F));

        PartDefinition cube_r5 = helix1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 34).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition helix2 = helix.addOrReplaceChild("helix2", CubeListBuilder.create().texOffs(10, 30).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.5F, -3.0F, 5.5F));

        PartDefinition cube_r6 = helix2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(10, 34).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition helix3 = helix.addOrReplaceChild("helix3", CubeListBuilder.create().texOffs(30, 30).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.25F, -3.0F, -5.25F));

        PartDefinition cube_r7 = helix3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(30, 34).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition helix4 = helix.addOrReplaceChild("helix4", CubeListBuilder.create().texOffs(20, 30).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.75F, -3.0F, -5.0F));

        PartDefinition cube_r8 = helix4.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(20, 34).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(DroneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        entity.flyAnimationState.ifStarted(state -> {
            this.animate(entity.flyAnimationState, DroneAnimations.FLY_DRONE, ageInTicks);
        });    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        helix.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

    }

    @Override
    public ModelPart root() {
        return root;
    }
}