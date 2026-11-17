package net.yezon.theabyss.platform.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import net.yezon.theabyss.platform.event.Event;

public class RenderLevelStageEvent extends Event {
    private final Stage stage;
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;

    public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum) {
        this.stage = stage;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
        this.modelViewMatrix = modelViewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
    }

    public Stage getStage() {
        return this.stage;
    }

    public LevelRenderer getLevelRenderer() {
        return this.levelRenderer;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public Matrix4f getModelViewMatrix() {
        return this.modelViewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public int getRenderTick() {
        return this.renderTick;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public float getPartialTickTime() {
        return this.partialTick.getGameTimeDeltaPartialTick(false);
    }

    public Camera getCamera() {
        return this.camera;
    }

    public Frustum getFrustum() {
        return this.frustum;
    }

    public MultiBufferSource.BufferSource getBufferSource() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public static final class Stage {
        private static final Map<String, Stage> BY_NAME = new HashMap<>();

        public static final Stage AFTER_SKY = new Stage("minecraft:after_sky");
        public static final Stage AFTER_SOLID_BLOCKS = new Stage("minecraft:after_solid_blocks");
        public static final Stage AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS = new Stage("minecraft:after_cutout_mipped_blocks");
        public static final Stage AFTER_CUTOUT_BLOCKS = new Stage("minecraft:after_cutout_blocks");
        public static final Stage AFTER_ENTITIES = new Stage("minecraft:after_entities");
        public static final Stage AFTER_BLOCK_ENTITIES = new Stage("minecraft:after_block_entities");
        public static final Stage AFTER_TRANSLUCENT_BLOCKS = new Stage("minecraft:after_translucent_blocks");
        public static final Stage AFTER_TRIPWIRE_BLOCKS = new Stage("minecraft:after_tripwire_blocks");
        public static final Stage AFTER_PARTICLES = new Stage("minecraft:after_particles");
        public static final Stage AFTER_WEATHER = new Stage("minecraft:after_weather");
        public static final Stage AFTER_LEVEL = new Stage("minecraft:after_level");

        private final String name;

        private Stage(String name) {
            this.name = name;
            BY_NAME.put(name, this);
        }

        public static Stage byName(String name) {
            return BY_NAME.get(name);
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
