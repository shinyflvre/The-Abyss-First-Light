package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class InfectedHand {
    private static final ContextKey<Boolean> INFECTED = new ContextKey<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "infected_hand"));
    private static final float INFLATE = 0.27F;
    private static final int FULL_BRIGHT = 15728880;
    private static final Map<PlayerModelType, Model<Void>> ARM_BOX = new EnumMap<>(PlayerModelType.class);
    private static @Nullable Method renderPlayerArm;
    private static boolean vanillaArmMissing;

    private InfectedHand() {
    }

    private static Identifier texture(PlayerModelType type, String suffix) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID,
            "textures/entity/infected_hand_" + (type == PlayerModelType.SLIM ? "slim" : "wide") + suffix + ".png");
    }

    private static Model<Void> armBox(PlayerModelType type) {
        return ARM_BOX.computeIfAbsent(type, t -> {
            boolean slim = t == PlayerModelType.SLIM;
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("arm", CubeListBuilder.create().texOffs(0, 0)
                .addBox(slim ? -2.0F : -3.0F, -2.0F, -2.0F, slim ? 3.0F : 4.0F, 12.0F, 4.0F, new CubeDeformation(INFLATE)), PartPose.ZERO);
            return new Model<Void>(LayerDefinition.create(mesh, 16, 16).bakeRoot(), RenderTypes::entityTranslucent) {
            };
        });
    }

    public static boolean holds(Player player) {
        return player.getMainHandItem().is(TheabyssModItems.INFECTED_GAUNTLET.get()) || player.getOffhandItem().is(TheabyssModItems.INFECTED_GAUNTLET.get())
            || net.yezon.theabyss.client.fx.OvergrowthClient.overgrown(player.getId());
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType type : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(type);
            if (renderer != null) {
                renderer.addLayer(new Layer(renderer, type));
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class<EntityRenderer<Entity, EntityRenderState>>)(Class<?>)EntityRenderer.class, (entity, state) -> {
            if (state instanceof AvatarRenderState) {
                state.setRenderData(INFECTED, entity instanceof Player player && holds(player));
            }
        });
    }

    private static final class Layer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private final PlayerModelType type;

        Layer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, PlayerModelType type) {
            super(renderer);
            this.type = type;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
            Boolean infected = state.getRenderData(INFECTED);
            if (infected == null || !infected || state.isInvisible) {
                return;
            }
            PlayerModel parent = this.getParentModel();
            Model<Void> box = armBox(this.type);
            poseStack.pushPose();
            parent.root().translateAndRotate(poseStack);
            parent.rightArm.translateAndRotate(poseStack);
            collector.order(1).submitModel(box, null, poseStack, RenderTypes.entityTranslucent(texture(this.type, "")), light, OverlayTexture.NO_OVERLAY,
                state.outlineColor);
            collector.order(2).submitModel(box, null, poseStack, RenderTypes.eyes(texture(this.type, "_glow")), light, OverlayTexture.NO_OVERLAY,
                state.outlineColor);
            poseStack.popPose();
        }
    }

    private static void infectedHand(AvatarRenderer<?> renderer, PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier skin,
                                     boolean sleeve, PlayerModelType type) {
        renderer.renderRightHand(poseStack, collector, light, skin, sleeve);
        renderer.renderRightHand(poseStack, collector, light, texture(type, "_skin"), sleeve);
        renderer.renderRightHand(poseStack, collector, FULL_BRIGHT, texture(type, "_skin_glow"), sleeve);
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Player player = Minecraft.getInstance().player;
        AvatarRenderState avatar = event.getPlayerRenderState().avatarRenderState;
        if (event.getArm() != HumanoidArm.RIGHT || player == null || avatar == null || !holds(player)) {
            return;
        }
        event.setCanceled(true);
        AvatarRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(avatar);
        infectedHand(renderer, event.getPoseStack(), event.getSubmitNodeCollector(), event.getLightCoords(), event.getSkinTexture(), event.hasSleeve(),
            avatar.skin.model());
    }

    private static boolean vanillaArm(PoseStack poseStack, SubmitNodeCollector collector, int light, float equip, float swing, PlayerRenderState state) {
        if (vanillaArmMissing) {
            return false;
        }
        try {
            if (renderPlayerArm == null) {
                Method method = FirstPersonHandsAndItemsRenderer.class.getDeclaredMethod("renderPlayerArm", PoseStack.class, SubmitNodeCollector.class, int.class,
                    float.class, float.class, HumanoidArm.class, PlayerRenderState.class);
                method.setAccessible(true);
                renderPlayerArm = method;
            }
            renderPlayerArm.invoke(Minecraft.getInstance().gameRenderer.firstPersonHandsAndItemsRenderer, poseStack, collector, light, equip, swing,
                HumanoidArm.RIGHT, state);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            vanillaArmMissing = true;
            TheabyssMod.LOGGER.warn("Infected hand: vanilla first person arm not reachable, using the own placement", e);
            return false;
        }
    }

    private static void ownArm(PoseStack poseStack, SubmitNodeCollector collector, int light, float equip, float swing, AvatarRenderState avatar) {
        float sqrtAttack = Mth.sqrt(swing);
        float xSwing = -0.3F * Mth.sin(sqrtAttack * (float)Math.PI);
        float ySwing = 0.4F * Mth.sin(sqrtAttack * (float)(Math.PI * 2));
        float zSwing = -0.4F * Mth.sin(swing * (float)Math.PI);
        poseStack.translate(xSwing + 0.64000005F, ySwing - 0.6F + equip * -0.6F, zSwing - 0.71999997F);
        poseStack.rotateDegrees(Axis.YP, 45.0F);
        poseStack.rotateDegrees(Axis.YP, Mth.sin(sqrtAttack * (float)Math.PI) * 70.0F);
        poseStack.rotateDegrees(Axis.ZP, Mth.sin(swing * swing * (float)Math.PI) * -20.0F);
        poseStack.translate(-1.0F, 3.6F, 3.5F);
        poseStack.rotateDegrees(Axis.ZP, 120.0F);
        poseStack.rotateDegrees(Axis.XP, 200.0F);
        poseStack.rotateDegrees(Axis.YP, -135.0F);
        poseStack.translate(5.6F, 0.0F, 0.0F);
        AvatarRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(avatar);
        infectedHand(renderer, poseStack, collector, light, avatar.skin.body().texturePath(), avatar.showRightSleeve, avatar.skin.model());
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getItemStack().is(TheabyssModItems.INFECTED_GAUNTLET.get())) {
            return;
        }
        PlayerRenderState playerState = event.getPlayerState();
        AvatarRenderState avatar = playerState.avatarRenderState;
        if (avatar == null || avatar.mainArm != HumanoidArm.RIGHT) {
            return;
        }
        event.setCanceled(true);
        if (avatar.isInvisible) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        if (!vanillaArm(poseStack, event.getSubmitNodeCollector(), event.getPackedLight(), event.getEquipProgress(), event.getSwingProgress(), playerState)) {
            ownArm(poseStack, event.getSubmitNodeCollector(), event.getPackedLight(), event.getEquipProgress(), event.getSwingProgress(), avatar);
        }
        poseStack.popPose();
    }
}
