package net.yezon.theabyss.fabric.mixin.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.yezon.theabyss.fabric.server.FabricMonsterRoomMobs;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomFeatureMixin {
    @WrapMethod(method = "randomEntityId")
    private EntityType<?> theabyss$monsterRoomMobs(RandomSource random, Operation<EntityType<?>> original) {
        EntityType<?> mob = FabricMonsterRoomMobs.pick(random);
        return mob != null ? mob : original.call(random);
    }
}
