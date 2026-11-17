package net.yezon.theabyss.platform.client.event;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Consumer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterShadersEvent extends Event {
    public abstract void registerShader(ResourceLocation id, VertexFormat format, Consumer<ShaderInstance> onLoad);
}
