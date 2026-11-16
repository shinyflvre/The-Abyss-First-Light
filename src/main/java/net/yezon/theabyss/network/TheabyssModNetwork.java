package net.yezon.theabyss.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.yezon.theabyss.TheabyssMod;

public final class TheabyssModNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private TheabyssModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TheabyssMod.MODID).versioned(PROTOCOL_VERSION);

        registrar.playToClient(TheabyssModVariables.SavedDataSyncMessage.TYPE, TheabyssModVariables.SavedDataSyncMessage.STREAM_CODEC,
            TheabyssModVariables.SavedDataSyncMessage::handleData);
        registrar.playToClient(TotemActivationMessage.TYPE, TotemActivationMessage.STREAM_CODEC, TotemActivationMessage::handleData);
        registrar.playToClient(DreamShiftMessage.TYPE, DreamShiftMessage.STREAM_CODEC, DreamShiftMessage::handleData);
        registrar.playToClient(AfterLifeFxMessage.TYPE, AfterLifeFxMessage.STREAM_CODEC, AfterLifeFxMessage::handleData);
        registrar.playToClient(ClockRewindMessage.TYPE, ClockRewindMessage.STREAM_CODEC, ClockRewindMessage::handleData);
        registrar.playToClient(PocketFoldMessage.TYPE, PocketFoldMessage.STREAM_CODEC, PocketFoldMessage::handleData);
        registrar.playToClient(RiftShiftMessage.TYPE, RiftShiftMessage.STREAM_CODEC, RiftShiftMessage::handleData);
        registrar.playToClient(DialogueMessage.TYPE, DialogueMessage.STREAM_CODEC, DialogueMessage::handleData);
        registrar.playToClient(NightGraceMessage.TYPE, NightGraceMessage.STREAM_CODEC, NightGraceMessage::handleData);
        registrar.playToServer(DialogueReplyMessage.TYPE, DialogueReplyMessage.STREAM_CODEC, DialogueReplyMessage::handleData);
        registrar.playToServer(RiftOfSinsMessage.TYPE, RiftOfSinsMessage.STREAM_CODEC, RiftOfSinsMessage::handleData);
        registrar.playToClient(NightbladeImpactMessage.TYPE, NightbladeImpactMessage.STREAM_CODEC, NightbladeImpactMessage::handleData);
        registrar.playToClient(NightbladeShakeMessage.TYPE, NightbladeShakeMessage.STREAM_CODEC, NightbladeShakeMessage::handleData);
        registrar.playToClient(NightbladeStormMessage.TYPE, NightbladeStormMessage.STREAM_CODEC, NightbladeStormMessage::handleData);
        registrar.playToClient(TelekinesisMessage.TYPE, TelekinesisMessage.STREAM_CODEC, TelekinesisMessage::handleData);
        registrar.playToClient(SpellFxMessage.TYPE, SpellFxMessage.STREAM_CODEC, SpellFxMessage::handleData);
        registrar.playToClient(ChronicleMessage.TYPE, ChronicleMessage.STREAM_CODEC, ChronicleMessage::handleData);
        registrar.playToServer(NightbladeParryMessage.TYPE, NightbladeParryMessage.STREAM_CODEC, NightbladeParryMessage::handleData);
        registrar.playToServer(FearCaughtMessage.TYPE, FearCaughtMessage.STREAM_CODEC, FearCaughtMessage::handleData);
        registrar.playToClient(NightbladeParryCueMessage.TYPE, NightbladeParryCueMessage.STREAM_CODEC, NightbladeParryCueMessage::handleData);
        registrar.playToClient(NightbladeArenaMessage.TYPE, NightbladeArenaMessage.STREAM_CODEC, NightbladeArenaMessage::handleData);
        registrar.playToClient(RingFxMessage.TYPE, RingFxMessage.STREAM_CODEC, RingFxMessage::handleData);
        registrar.playToClient(TrueInvisibilityMessage.TYPE, TrueInvisibilityMessage.STREAM_CODEC, TrueInvisibilityMessage::handleData);
        registrar.playToClient(ShadowVeilMessage.TYPE, ShadowVeilMessage.STREAM_CODEC, ShadowVeilMessage::handleData);
        registrar.playToServer(CrystalNeedlesMessage.TYPE, CrystalNeedlesMessage.STREAM_CODEC, CrystalNeedlesMessage::handleData);
        registrar.playToServer(GauntletMessage.TYPE, GauntletMessage.STREAM_CODEC, GauntletMessage::handleData);
        registrar.playToClient(EagleBossesMessage.TYPE, EagleBossesMessage.STREAM_CODEC, EagleBossesMessage::handleData);
        registrar.playToClient(TeleportDashMessage.TYPE, TeleportDashMessage.STREAM_CODEC, TeleportDashMessage::handleData);
        registrar.playToClient(EchoVisionMessage.TYPE, EchoVisionMessage.STREAM_CODEC, EchoVisionMessage::handleData);
        registrar.playBidirectional(GravityMessage.TYPE, GravityMessage.STREAM_CODEC, GravityMessage::handleServer, GravityMessage::handleClient);
        registrar.playToClient(RingFinisherMessage.TYPE, RingFinisherMessage.STREAM_CODEC, RingFinisherMessage::handleData);
        registrar.playToClient(SkillSyncMessage.TYPE, SkillSyncMessage.STREAM_CODEC, SkillSyncMessage::handleData);
        registrar.playToServer(SkillRequestMessage.TYPE, SkillRequestMessage.STREAM_CODEC, SkillRequestMessage::handleData);
        registrar.playToServer(SkillActionMessage.TYPE, SkillActionMessage.STREAM_CODEC, SkillActionMessage::handleData);
        registrar.playToClient(WraithMessage.TYPE, WraithMessage.STREAM_CODEC, WraithMessage::handleData);
        registrar.playToClient(OvergrowthMessage.TYPE, OvergrowthMessage.STREAM_CODEC, OvergrowthMessage::handleData);

        registrar.playToServer(ResearchButtonMessage.TYPE, ResearchButtonMessage.STREAM_CODEC, ResearchButtonMessage::handleData);

        registrar.playToServer(AbilDebugMessage.TYPE, AbilDebugMessage.STREAM_CODEC, AbilDebugMessage::handleData);
        registrar.playToServer(FriendleyFireAMessage.TYPE, FriendleyFireAMessage.STREAM_CODEC, FriendleyFireAMessage::handleData);
        registrar.playToServer(AbyssEchoesMessage.TYPE, AbyssEchoesMessage.STREAM_CODEC, AbyssEchoesMessage::handleData);
        registrar.playToServer(FriendleyFireBMessage.TYPE, FriendleyFireBMessage.STREAM_CODEC, FriendleyFireBMessage::handleData);
        registrar.playToServer(GhostAttackMessage.TYPE, GhostAttackMessage.STREAM_CODEC, GhostAttackMessage::handleData);
        registrar.playToServer(GhostDeactivateMessage.TYPE, GhostDeactivateMessage.STREAM_CODEC, GhostDeactivateMessage::handleData);
        registrar.playToServer(GhostFireAttackMessage.TYPE, GhostFireAttackMessage.STREAM_CODEC, GhostFireAttackMessage::handleData);
    }
}
