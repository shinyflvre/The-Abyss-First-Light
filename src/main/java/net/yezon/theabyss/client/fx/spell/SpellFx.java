package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class SpellFx {
    private static final ContextKey<Map<RenderType, List<BiConsumer<PoseStack.Pose, VertexConsumer>>>> DRAWS =
        new ContextKey<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "spell_fx"));
    private static final List<Effect> EFFECTS = new ArrayList<>();
    private static Level level;

    private SpellFx() {
    }

    public static void add(Effect effect) {
        EFFECTS.add(effect);
    }

    public static List<Effect> effects() {
        return EFFECTS;
    }

    public static void handle(SpellFxMessage m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        prune(mc);
        Effect effect = switch (m.kind()) {
            case SpellFxMessage.METEOR -> new MeteorFx(m);
            case SpellFxMessage.VOID -> new VoidFx(m);
            case SpellFxMessage.TRAIL -> new TrailFx(m);
            case SpellFxMessage.SWAP -> new SwapFx(m);
            case SpellFxMessage.REWIND -> new RewindFx(m);
            case SpellFxMessage.TETHER -> new TetherFx(m);
            case SpellFxMessage.TIDAL -> new TidalFx(m);
            case SpellFxMessage.HEAL -> new HealFx(m);
            case SpellFxMessage.WALL -> new WallFx(m);
            case SpellFxMessage.HEX -> new HexFx(m);
            case SpellFxMessage.HEX_END -> new HexFx.Shatter(m);
            case SpellFxMessage.PUPPET -> new PuppetFx(m);
            case SpellFxMessage.PUPPET_TARGET -> {
                PuppetFx.retarget(m);
                yield null;
            }
            case SpellFxMessage.OVERCHARGE -> OverchargeFx.update(m);
            case SpellFxMessage.OVERCHARGE_FIRE -> new OverchargeFx.Shot(m);
            case SpellFxMessage.DASH -> new DashFx(m);
            case SpellFxMessage.GHOSTS -> new GhostFx(m);
            case SpellFxMessage.FLAME_TRAIL -> new FlameTrailFx(m);
            case SpellFxMessage.ICE -> new IceFx(m);
            case SpellFxMessage.ICE_BURST -> new IceFx.Burst(m);
            case SpellFxMessage.RECALL_MARK -> new RecallFx.Mark(m);
            case SpellFxMessage.RECALL -> new RecallFx(m);
            case SpellFxMessage.SOUL -> new SoulFx(m);
            case SpellFxMessage.PHANTOM_CHARGE -> new PhantomFx.Charge(m);
            case SpellFxMessage.PHANTOM_BOLT -> new PhantomFx.Bolt(m);
            case SpellFxMessage.PHANTOM_SPLASH -> PhantomFx.splash(m);
            case SpellFxMessage.PHANTOM_GATHER -> new PhantomFx.Gather(m);
            case SpellFxMessage.PHANTOM_MIASMA -> new PhantomFx.Miasma(m);
            case SpellFxMessage.PHANTOM_REND -> new PhantomFx.Rend(m);
            case SpellFxMessage.ECHO_SUMMON -> new EchoFx.Summon(m);
            case SpellFxMessage.ECHO_DISSOLVE -> new EchoFx.Dissolve(m);
            case SpellFxMessage.ECHO_LINK -> new EchoFx.Link(m);
            case SpellFxMessage.ENCHANT_SMELT -> new EnchantFx.Smelt(m);
            case SpellFxMessage.ENCHANT_ECHO -> new EnchantFx.Echo(m);
            case SpellFxMessage.ENCHANT_SHATTER -> new EnchantFx.Shatter(m);
            case SpellFxMessage.ENCHANT_SHOT -> new EnchantFx.Shot(m);
            case SpellFxMessage.ENCHANT_HIT -> new EnchantFx.Hit(m);
            case SpellFxMessage.ENCHANT_LOCK -> new EnchantFx.Lock(m);
            case SpellFxMessage.ENCHANT_BEAM -> new EnchantFx.Beam(m);
            case SpellFxMessage.ACCENT -> AccentFx.ofMessage(m);
            case SpellFxMessage.FREEZE_BURST -> new FreezeBurstFx(m);
            case SpellFxMessage.CIRCLE -> new SpellCircleFx(m);
            default -> null;
        };
        if (effect != null) {
            EFFECTS.add(effect);
        }
        Effect accent = AccentFx.forSpell(m);
        if (accent != null) {
            EFFECTS.add(accent);
        }
        EFFECTS.addAll(SpellCircleFx.forSpell(m));
        if (m.kind() == SpellFxMessage.TRAIL && m.data().get(0).intValue() == net.yezon.theabyss.ring.RingVisuals.NIGHTBLADE) {
            for (int id : m.ids()) {
                TrailStreaks.follow(id, net.yezon.theabyss.ring.RingVisuals.NIGHTBLADE, m.ticks(), mc.level.getGameTime());
            }
        }
    }

    private static void prune(Minecraft mc) {
        if (mc.level != level) {
            level = mc.level;
            EFFECTS.clear();
            TrailStreaks.clear();
        }
        EFFECTS.removeIf(Effect::done);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        prune(mc);
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        for (Effect effect : List.copyOf(EFFECTS)) {
            effect.tick(mc.level);
        }
        TrailStreaks.tick(mc);
    }

    @SubscribeEvent
    public static void onExtract(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        prune(mc);
        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        net.yezon.theabyss.client.fx.MagicLights.begin(event.getCamera().position());
        if ((EFFECTS.isEmpty() && TrailStreaks.isEmpty()) || mc.level == null) {
            net.yezon.theabyss.client.fx.MagicLights.finish(mc, partialTick);
            return;
        }
        Frame frame = new Frame(mc, mc.level, event.getCamera().position(), partialTick);
        for (Effect effect : EFFECTS) {
            try {
                effect.draw(frame);
            } catch (RuntimeException e) {
                TheabyssMod.LOGGER.warn("Spell effect {} failed to draw", effect.getClass().getSimpleName(), e);
                effect.kill();
            }
        }
        TrailStreaks.draw(frame);
        net.yezon.theabyss.client.fx.MagicLights.finish(mc, partialTick);
        event.getRenderState().setRenderData(DRAWS, frame.draws);
    }

    @SubscribeEvent
    public static void onSubmit(SubmitCustomGeometryEvent event) {
        Map<RenderType, List<BiConsumer<PoseStack.Pose, VertexConsumer>>> draws = event.getLevelRenderState().getRenderData(DRAWS);
        if (draws == null || draws.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        for (Map.Entry<RenderType, List<BiConsumer<PoseStack.Pose, VertexConsumer>>> entry : draws.entrySet()) {
            List<BiConsumer<PoseStack.Pose, VertexConsumer>> list = entry.getValue();
            event.getSubmitNodeCollector().submitCustomGeometry(poseStack, entry.getKey(), (pose, buffer) -> {
                for (BiConsumer<PoseStack.Pose, VertexConsumer> draw : list) {
                    draw.accept(pose, buffer);
                }
            });
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class<EntityRenderer<Entity, EntityRenderState>>)(Class<?>)EntityRenderer.class, (entity, state) -> {
            if (EFFECTS.isEmpty()) {
                return;
            }
            for (Effect effect : EFFECTS) {
                int colour = effect.outline(entity);
                if (colour != 0) {
                    state.outlineColor = colour;
                    return;
                }
            }
        });
    }

    static Vec3 vec(SpellFxMessage m, int i) {
        return new Vec3(m.data().get(i), m.data().get(i + 1), m.data().get(i + 2));
    }
}
