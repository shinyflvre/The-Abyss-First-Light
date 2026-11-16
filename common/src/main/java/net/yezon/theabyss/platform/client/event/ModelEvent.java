package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class ModelEvent extends Event {

    public static class ModifyBakingResult extends ModelEvent {
        private final net.minecraft.client.resources.model.ModelBakery.BakingResult bakingResult;

        public ModifyBakingResult(net.minecraft.client.resources.model.ModelBakery.BakingResult bakingResult) {
            this.bakingResult = bakingResult;
        }

        public net.minecraft.client.resources.model.ModelBakery.BakingResult getBakingResult() {
            return this.bakingResult;
        }
    }
}
