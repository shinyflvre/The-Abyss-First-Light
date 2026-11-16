package net.yezon.theabyss.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record SpellFxMessage(int kind, List<Integer> ids, List<Double> data, int ticks) implements CustomPacketPayload {
    public static final int SWAP = 1;
    public static final int TIDAL = 2;
    public static final int HEAL = 3;
    public static final int PUPPET = 4;
    public static final int PUPPET_TARGET = 5;
    public static final int REWIND = 6;
    public static final int TETHER = 7;
    public static final int HEX = 8;
    public static final int HEX_END = 9;
    public static final int WALL = 10;
    public static final int OVERCHARGE = 11;
    public static final int OVERCHARGE_FIRE = 12;
    public static final int METEOR = 13;
    public static final int TRAIL = 14;
    public static final int VOID = 15;
    public static final int DASH = 16;
    public static final int GHOSTS = 17;
    public static final int FLAME_TRAIL = 18;
    public static final int ICE = 19;
    public static final int ICE_BURST = 20;
    public static final int RECALL_MARK = 21;
    public static final int RECALL = 22;
    public static final int SOUL = 23;
    public static final int PHANTOM_CHARGE = 24;
    public static final int PHANTOM_BOLT = 25;
    public static final int PHANTOM_SPLASH = 26;
    public static final int PHANTOM_GATHER = 27;
    public static final int PHANTOM_MIASMA = 28;
    public static final int PHANTOM_REND = 29;
    public static final int ECHO_SUMMON = 30;
    public static final int ECHO_DISSOLVE = 31;
    public static final int ECHO_LINK = 32;
    public static final int ENCHANT_SMELT = 33;
    public static final int ENCHANT_ECHO = 34;
    public static final int ENCHANT_SHATTER = 35;
    public static final int ENCHANT_SHOT = 36;
    public static final int ENCHANT_HIT = 37;
    public static final int ENCHANT_LOCK = 38;
    public static final int ENCHANT_BEAM = 39;
    public static final int ACCENT = 40;
    public static final int FREEZE_BURST = 41;
    public static final int CIRCLE = 42;

    public static final Type<SpellFxMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "spell_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellFxMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SpellFxMessage::kind,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), SpellFxMessage::ids,
        ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()), SpellFxMessage::data,
        ByteBufCodecs.VAR_INT, SpellFxMessage::ticks,
        SpellFxMessage::new
    );

    @Override
    public Type<SpellFxMessage> type() {
        return TYPE;
    }

    public static void around(ServerLevel level, Vec3 at, SpellFxMessage message) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(at) <= 128.0 * 128.0 && player.connection != null && player.connection.hasChannel(TYPE)) {
                PacketDistributor.sendToPlayer(player, message);
            }
        }
    }

    public static void around(ServerLevel level, Vec3 a, Vec3 b, SpellFxMessage message) {
        for (ServerPlayer player : level.players()) {
            boolean near = player.position().distanceToSqr(a) <= 128.0 * 128.0 || player.position().distanceToSqr(b) <= 128.0 * 128.0;
            if (near && player.connection != null && player.connection.hasChannel(TYPE)) {
                PacketDistributor.sendToPlayer(player, message);
            }
        }
    }

    public static void around(ServerLevel level, Vec3 at, int kind, List<Integer> ids, List<Double> data, int ticks) {
        around(level, at, new SpellFxMessage(kind, ids, data, ticks));
    }

    public static List<Double> points(Vec3... points) {
        Double[] out = new Double[points.length * 3];
        for (int i = 0; i < points.length; i++) {
            out[i * 3] = points[i].x;
            out[i * 3 + 1] = points[i].y;
            out[i * 3 + 2] = points[i].z;
        }
        return List.of(out);
    }

    public static void handleData(SpellFxMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.handle(message));
    }

    private static final class Client {
        static void handle(SpellFxMessage message) {
            net.yezon.theabyss.client.fx.spell.SpellFx.handle(message);
        }
    }
}
