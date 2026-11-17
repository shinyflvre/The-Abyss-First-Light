package net.yezon.theabyss.client.renderer;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public record EchoSkins(ResourceLocation texture, boolean slim) {
    private static final EchoSkins DEFAULT = new EchoSkins(DefaultPlayerSkin.getDefaultTexture(), true);

    public static EchoSkins defaultSkin() {
        return DEFAULT;
    }

    public static EchoSkins of(@Nullable UUID owner) {
        if (owner == null) {
            return DEFAULT;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection != null ? connection.getPlayerInfo(owner) : null;
        return of(info != null ? info.getSkin() : DefaultPlayerSkin.get(owner));
    }

    public static EchoSkins of(String ownerId) {
        try {
            return of(UUID.fromString(ownerId));
        } catch (IllegalArgumentException noOwnerYet) {
            return DEFAULT;
        }
    }

    public static EchoSkins local() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? DEFAULT : of(mc.player.getUUID());
    }

    private static EchoSkins of(PlayerSkin skin) {
        return new EchoSkins(skin.texture(), skin.model() == PlayerSkin.Model.SLIM);
    }
}
