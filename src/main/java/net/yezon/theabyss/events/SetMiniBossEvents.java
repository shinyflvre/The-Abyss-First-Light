package net.yezon.theabyss.events;

import net.yezon.theabyss.util.CommandUtil;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;

public class SetMiniBossEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level) {
            _level.getServer()
                .getCommands()
                .performPrefixedCommand(
                    CommandUtil.source(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null)
                        .withSuppressedOutput(),
                    "team add AbyssMiniBosses"
                );
        }

        TheabyssMod.queueServerWork(
            20,
            () -> {
                if (world instanceof ServerLevel _levelx) {
                    _levelx.getServer()
                        .getCommands()
                        .performPrefixedCommand(
                            CommandUtil.source(
                                    CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _levelx, 4, "", Component.literal(""), _levelx.getServer(), null
                                )
                                .withSuppressedOutput(),
                            "team modify AbyssMiniBosses color dark_aqua"
                        );
                }
            }
        );
        TheabyssMod.queueServerWork(
            40,
            () -> {
                if (world instanceof ServerLevel _levelxxx) {
                    _levelxxx.getServer()
                        .getCommands()
                        .performPrefixedCommand(
                            CommandUtil.source(
                                    CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _levelxxx, 4, "", Component.literal(""), _levelxxx.getServer(), null
                                )
                                .withSuppressedOutput(),
                            "/team join AbyssMiniBosses @e[type=theabyss:the_roka]"
                        );
                }

                if (world instanceof ServerLevel _levelxx) {
                    _levelxx.getServer()
                        .getCommands()
                        .performPrefixedCommand(
                            CommandUtil.source(
                                    CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _levelxx, 4, "", Component.literal(""), _levelxx.getServer(), null
                                )
                                .withSuppressedOutput(),
                            "/team join AbyssMiniBosses @e[type=theabyss:crystal_golem]"
                        );
                }

                if (world instanceof ServerLevel _levelx) {
                    _levelx.getServer()
                        .getCommands()
                        .performPrefixedCommand(
                            CommandUtil.source(
                                    CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _levelx, 4, "", Component.literal(""), _levelx.getServer(), null
                                )
                                .withSuppressedOutput(),
                            "/team join AbyssMiniBosses @e[type=theabyss:elder_lvl_x]"
                        );
                }
            }
        );
    }
}
