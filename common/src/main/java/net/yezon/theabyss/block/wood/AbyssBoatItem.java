package net.yezon.theabyss.block.wood;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AbyssBoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final Supplier<? extends EntityType<? extends Boat>> entityType;

    public AbyssBoatItem(Supplier<? extends EntityType<? extends Boat>> entityType, Item.Properties properties) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> entities = level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
        if (!entities.isEmpty()) {
            Vec3 from = player.getEyePosition();
            for (Entity entity : entities) {
                AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (box.contains(from)) {
                    return InteractionResultHolder.pass(itemStack);
                }
            }
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        Boat boat = this.getBoat(level, hitResult, itemStack, player);
        if (boat == null) {
            return InteractionResultHolder.fail(itemStack);
        }
        boat.setYRot(player.getYRot());
        if (!level.noCollision(boat, boat.getBoundingBox())) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (!level.isClientSide()) {
            level.addFreshEntity(boat);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
            itemStack.consume(1, player);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private @Nullable Boat getBoat(Level level, HitResult hitResult, ItemStack itemStack, Player player) {
        Boat boat = this.entityType.get().create(level);
        if (boat != null) {
            Vec3 location = hitResult.getLocation();
            boat.setPos(location.x, location.y, location.z);
            boat.xo = location.x;
            boat.yo = location.y;
            boat.zo = location.z;
            if (level instanceof ServerLevel serverLevel) {
                EntityType.<Boat>createDefaultStackConfig(serverLevel, itemStack, player).accept(boat);
            }
        }
        return boat;
    }
}
