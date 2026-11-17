package net.yezon.theabyss.platform.client.event;

import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.yezon.theabyss.platform.event.Event;

public abstract class ModelEvent extends Event {

    public static class ModifyBakingResult extends ModelEvent {
        private final Map<ModelResourceLocation, BakedModel> models;
        private final ModelBakery modelBakery;

        public ModifyBakingResult(Map<ModelResourceLocation, BakedModel> models, ModelBakery modelBakery) {
            this.models = models;
            this.modelBakery = modelBakery;
        }

        public Map<ModelResourceLocation, BakedModel> getModels() {
            return this.models;
        }

        public ModelBakery getModelBakery() {
            return this.modelBakery;
        }
    }
}
