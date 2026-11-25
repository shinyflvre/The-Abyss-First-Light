package net.yezon.theabyss.devtest;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(TheabyssDevTest.MODID)
public class TheabyssDevTest {
    public static final String MODID = "theabyss_devtest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheabyssDevTest() {
        NeoForge.EVENT_BUS.register(ServerSelfTest.class);
        NeoForge.EVENT_BUS.register(MixinAuditCheck.class);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(ClientAutotest.class);
            NeoForge.EVENT_BUS.register(MeteoroidAutotest.class);
        }
    }
}
