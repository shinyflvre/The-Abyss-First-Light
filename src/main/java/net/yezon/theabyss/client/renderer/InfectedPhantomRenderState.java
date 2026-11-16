package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class InfectedPhantomRenderState extends LivingEntityRenderState {
    public byte attack;
    public float attackAge;
    public float fade = 1.0F;
    public float lean;
}
