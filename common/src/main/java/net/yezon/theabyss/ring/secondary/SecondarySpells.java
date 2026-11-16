package net.yezon.theabyss.ring.secondary;

import java.util.List;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.SomniumUpgrades.Kind;
import net.yezon.theabyss.ring.secondary.Secondary.Spec;

final class SecondarySpells {
    private SecondarySpells() {
    }

    static List<Spec> all() {
        return List.of(
            new Spec(TheabyssModItems.RING_OF_FIRESTORM::get, "tooltip.theabyss.ring_of_firestorm.secondary.ability", "tooltip.theabyss.ring_of_firestorm.secondary.text",
                "tooltip.theabyss.ring_of_firestorm.secondary.action", Kind.DAMAGE, 3, 45.0, 400, Meteor::damage, "", false, Meteor::cast),
            new Spec(TheabyssModItems.RING_OF_BLACKSTRIKE::get, "tooltip.theabyss.ring_of_blackstrike.secondary.ability", "tooltip.theabyss.ring_of_blackstrike.secondary.text",
                "tooltip.theabyss.ring_of_blackstrike.secondary.action", Kind.DAMAGE, 2, 35.0, 300, VoidRift::damage, "", false, VoidRift::cast),
            new Spec(TheabyssModItems.RING_OF_FIRESTRIKE::get, "tooltip.theabyss.ring_of_firestrike.secondary.ability", "tooltip.theabyss.ring_of_firestrike.secondary.text",
                "tooltip.theabyss.ring_of_firestrike.secondary.action", Kind.DAMAGE, 2, 20.0, 40, ScatterShot::damage, "tooltip.theabyss.times_5", false, ScatterShot::cast),
            new Spec(TheabyssModItems.RING_OF_TELEPORT::get, "tooltip.theabyss.ring_of_teleport.secondary.ability", "tooltip.theabyss.ring_of_teleport.secondary.text",
                "tooltip.theabyss.ring_of_teleport.secondary.action", Kind.USAGE, 2, 25.0, 120, null, "", false, Swap::cast),
            new Spec(TheabyssModItems.RING_OF_TIME::get, "tooltip.theabyss.ring_of_time.secondary.ability", "tooltip.theabyss.ring_of_time.secondary.text",
                "tooltip.theabyss.ring_of_time.secondary.action", Kind.USAGE, 4, 40.0, 600, null, "", false, Rewind::cast),
            new Spec(TheabyssModItems.RING_OF_POCKET_BOX::get, "tooltip.theabyss.ring_of_pocket_box.secondary.ability", "tooltip.theabyss.ring_of_pocket_box.secondary.text",
                "tooltip.theabyss.ring_of_pocket_box.secondary.action", Kind.USAGE, 2, 50.0, 500, null, "", true, BringAlong::cast),
            new Spec(TheabyssModItems.RING_OF_OCEAN::get, "tooltip.theabyss.ring_of_ocean.secondary.ability", "tooltip.theabyss.ring_of_ocean.secondary.text",
                "tooltip.theabyss.ring_of_ocean.secondary.action", Kind.USAGE, 2, 30.0, 300, null, "", false, TidalWave::cast),
            new Spec(TheabyssModItems.RING_OF_REG::get, "tooltip.theabyss.ring_of_reg.secondary.ability", "tooltip.theabyss.ring_of_reg.secondary.text",
                "tooltip.theabyss.ring_of_reg.secondary.action", Kind.USAGE, 2, 25.0, 300, null, "", false, HealingPulse::cast),
            new Spec(TheabyssModItems.RING_OF_FANGS::get, "tooltip.theabyss.ring_of_fangs.secondary.ability", "tooltip.theabyss.ring_of_fangs.secondary.text",
                "tooltip.theabyss.ring_of_fangs.secondary.action", Kind.USAGE, 2, 30.0, 300, CrystalWall::damage, "", false, CrystalWall::cast),
            new Spec(TheabyssModItems.RING_OF_CURSE::get, "tooltip.theabyss.ring_of_curse.secondary.ability", "tooltip.theabyss.ring_of_curse.secondary.text",
                "tooltip.theabyss.ring_of_curse.secondary.action", Kind.DAMAGE, 2, 25.0, 200, null, "", false, HexMark::cast),
            new Spec(TheabyssModItems.RING_OF_POSSESSION::get, "tooltip.theabyss.ring_of_possession.secondary.ability", "tooltip.theabyss.ring_of_possession.secondary.text",
                "tooltip.theabyss.ring_of_possession.secondary.action", Kind.DAMAGE, 3, 30.0, 400, null, "", false, Puppeteer::cast),
            new Spec(TheabyssModItems.RING_OF_THUNDER::get, "tooltip.theabyss.ring_of_thunder.secondary.ability", "tooltip.theabyss.ring_of_thunder.secondary.text",
                "tooltip.theabyss.ring_of_thunder.secondary.action", Kind.DAMAGE, 2, 30.0, 300, Overcharge::damage, "tooltip.theabyss.times_3", false, Overcharge::cast),
            new Spec(TheabyssModItems.RING_OF_FIRE::get, "tooltip.theabyss.ring_of_fire.secondary.ability", "tooltip.theabyss.ring_of_fire.secondary.text",
                "tooltip.theabyss.ring_of_fire.secondary.action", Kind.USAGE, 2, 20.0, 80, FlameDash::damage, "", false, FlameDash::cast),
            new Spec(TheabyssModItems.RING_OF_FREEZE::get, "tooltip.theabyss.ring_of_freeze.secondary.ability", "tooltip.theabyss.ring_of_freeze.secondary.text",
                "tooltip.theabyss.ring_of_freeze.secondary.action", Kind.DAMAGE, 2, 30.0, 300, IcePrison::damage, "", false, IcePrison::cast),
            new Spec(TheabyssModItems.RING_OF_HOME::get, "tooltip.theabyss.ring_of_home.secondary.ability", "tooltip.theabyss.ring_of_home.secondary.text",
                "tooltip.theabyss.ring_of_home.secondary.action", Kind.USAGE, 2, RecallPoint.COST, RecallPoint.COOLDOWN, null, "", true, RecallPoint::cast),
            new Spec(TheabyssModItems.RING_OF_NIGHTBLADE::get, "tooltip.theabyss.ring_of_nightblade.secondary.ability", "tooltip.theabyss.ring_of_nightblade.secondary.text",
                "tooltip.theabyss.ring_of_nightblade.secondary.action", Kind.DAMAGE, 3, 30.0, 160, BladeThrow::damage, "tooltip.theabyss.times_2", false, BladeThrow::cast),
            new Spec(TheabyssModItems.RING_OF_SPECTRAL::get, "tooltip.theabyss.ring_of_spectral.secondary.ability", "tooltip.theabyss.ring_of_spectral.secondary.text",
                "tooltip.theabyss.ring_of_spectral.secondary.action", Kind.USAGE, 3, 15.0, 60, null, "", false, SoulStep::cast),
            new Spec(TheabyssModItems.RING_OF_SPEED::get, "tooltip.theabyss.ring_of_speed.secondary.ability", "tooltip.theabyss.ring_of_speed.secondary.text",
                "tooltip.theabyss.ring_of_speed.secondary.action", Kind.USAGE, 1, 10.0, 40, null, "", false, SpeedDash::cast),
            new Spec(TheabyssModItems.RING_OF_SILENT_ECHOES::get, "tooltip.theabyss.ring_of_silent_echoes.secondary.ability", "tooltip.theabyss.ring_of_silent_echoes.secondary.text",
                "tooltip.theabyss.ring_of_silent_echoes.secondary.action", Kind.USAGE, 0, net.yezon.theabyss.ring.echo.EchoVision.COST,
                net.yezon.theabyss.ring.echo.EchoVision.COOLDOWN, null, "", true, net.yezon.theabyss.ring.echo.EchoVision::cast)
        );
    }
}
