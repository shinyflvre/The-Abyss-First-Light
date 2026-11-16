package net.yezon.theabyss.client.model;

import java.util.Set;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.yezon.theabyss.TheabyssMod;

public class AbyssChestModel extends Model<Float> {
    public static final ModelLayerLocation SINGLE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_chest"), "single");
    public static final ModelLayerLocation LEFT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_chest"), "left");
    public static final ModelLayerLocation RIGHT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_chest"), "right");
    private static final Set<Direction> EXCEPT_EAST = Util.allOfEnumExcept(Direction.EAST);
    private static final Set<Direction> EXCEPT_WEST = Util.allOfEnumExcept(Direction.WEST);
    private final ModelPart lid;

    public AbyssChestModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutCull);
        this.lid = root.getChild("lid");
    }

    public static ModelLayerLocation layer(ChestType type) {
        return switch (type) {
            case SINGLE -> SINGLE;
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
        };
    }

    @Override
    public void setupAnim(Float open) {
        super.setupAnim(open);
        this.lid.xRot = -(open * (float)(Math.PI / 2));
    }

    public static LayerDefinition createSingleLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(0, 0).addBox(1.0F, 1.0F, 1.0F, 14.0F, 9.0F, 14.0F)
            .texOffs(52, 46).addBox(1.0F, 0.0F, 1.0F, 3.0F, 1.0F, 3.0F)
            .texOffs(52, 50).addBox(1.0F, 0.0F, 12.0F, 3.0F, 1.0F, 3.0F)
            .texOffs(44, 54).addBox(12.0F, 0.0F, 1.0F, 3.0F, 1.0F, 3.0F)
            .texOffs(44, 58).addBox(12.0F, 0.0F, 12.0F, 3.0F, 1.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("lid", CubeListBuilder.create()
            .texOffs(0, 23).addBox(1.0F, 0.0F, 0.0F, 14.0F, 3.0F, 14.0F)
            .texOffs(0, 40).addBox(1.0F, 3.0F, 1.0F, 14.0F, 1.0F, 12.0F)
            .texOffs(0, 53).addBox(1.0F, 4.0F, 3.0F, 14.0F, 1.0F, 8.0F)
            .texOffs(52, 40).addBox(6.0F, -3.0F, 14.0F, 4.0F, 5.0F, 1.0F), PartPose.offset(0.0F, 10.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createLeftLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(0, 0).addBox(0.0F, 1.0F, 1.0F, 15.0F, 9.0F, 14.0F, EXCEPT_WEST)
            .texOffs(46, 53).addBox(12.0F, 0.0F, 1.0F, 3.0F, 1.0F, 3.0F)
            .texOffs(46, 57).addBox(12.0F, 0.0F, 12.0F, 3.0F, 1.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("lid", CubeListBuilder.create()
            .texOffs(0, 23).addBox(0.0F, 0.0F, 0.0F, 15.0F, 3.0F, 14.0F, EXCEPT_WEST)
            .texOffs(0, 40).addBox(0.0F, 3.0F, 1.0F, 15.0F, 1.0F, 12.0F, EXCEPT_WEST)
            .texOffs(0, 53).addBox(0.0F, 4.0F, 3.0F, 15.0F, 1.0F, 8.0F, EXCEPT_WEST)
            .texOffs(58, 0).addBox(0.0F, -3.0F, 14.0F, 2.0F, 5.0F, 1.0F, EXCEPT_WEST), PartPose.offset(0.0F, 10.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createRightLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(0, 0).addBox(1.0F, 1.0F, 1.0F, 15.0F, 9.0F, 14.0F, EXCEPT_EAST)
            .texOffs(46, 53).addBox(1.0F, 0.0F, 1.0F, 3.0F, 1.0F, 3.0F)
            .texOffs(46, 57).addBox(1.0F, 0.0F, 12.0F, 3.0F, 1.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("lid", CubeListBuilder.create()
            .texOffs(0, 23).addBox(1.0F, 0.0F, 0.0F, 15.0F, 3.0F, 14.0F, EXCEPT_EAST)
            .texOffs(0, 40).addBox(1.0F, 3.0F, 1.0F, 15.0F, 1.0F, 12.0F, EXCEPT_EAST)
            .texOffs(0, 53).addBox(1.0F, 4.0F, 3.0F, 15.0F, 1.0F, 8.0F, EXCEPT_EAST)
            .texOffs(58, 0).addBox(14.0F, -3.0F, 14.0F, 2.0F, 5.0F, 1.0F, EXCEPT_EAST), PartPose.offset(0.0F, 10.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
