package net.yezon.theabyss.world.inventory;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class CraftingStationMenu extends AbstractBlockMenu {
    protected final ResultContainer result = new ResultContainer();

    protected CraftingStationMenu(MenuType<?> type, int id, Inventory inv, @Nullable BlockPos pos, int containerSize, int oldResultIndex) {
        super(type, id, inv, pos, containerSize);
        if (inv.player instanceof ServerPlayer player && this.boundBlockEntity != null) {
            ItemStack leftover = this.internal.removeItemNoUpdate(oldResultIndex);
            if (!leftover.isEmpty()) {
                player.getInventory().placeItemBackInInventory(leftover, net.minecraft.util.Prediction.SERVER_ONLY);
                this.internal.setChanged();
            }
        }
    }

    protected void addResultSlot(int x, int y) {
        this.addStationSlot(new Slot(this.result, 0, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
                int all = this.getItem().getCount();
                return decrement < all ? Optional.empty() : super.tryRemove(all, all, player);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                CraftingStationMenu.this.crafted(player, stack);
                super.onTake(player, stack);
            }
        });
    }

    protected abstract ItemStack computeResult(ServerLevel level);

    protected abstract void consume(Player player);

    protected @Nullable SoundEvent craftSound() {
        return null;
    }

    private void crafted(Player player, ItemStack stack) {
        stack.onCraftedBy(player, stack.getCount());
        if (this instanceof ArcaneMenu && player.level() instanceof ServerLevel chronicleLevel
            && net.yezon.theabyss.ring.RingWear.isRing(stack.isEmpty() ? this.computeResult(chronicleLevel) : stack)) {
            net.yezon.theabyss.lore.Chronicles.note(player, net.yezon.theabyss.lore.Chronicles.SOMNIUM);
        }
        this.consume(player);
        this.internal.setChanged();
        if (player.level() instanceof ServerLevel level) {
            SoundEvent sound = this.craftSound();
            if (sound != null) {
                level.playSound(null, this.x + 0.5, this.y + 0.5, this.z + 0.5, sound, SoundSource.BLOCKS, 0.8F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            }
            this.updateResult(level);
        } else {
            this.result.setItem(0, ItemStack.EMPTY);
        }
    }

    protected void useOne(int index) {
        ItemStack stack = this.internal.getItem(index);
        if (stack.isEmpty()) {
            return;
        }
        net.minecraft.world.item.ItemStackTemplate template = net.yezon.theabyss.platform.services.ServerHooks.get().getCraftingRemainder(stack);
        ItemStack remainder = template == null ? ItemStack.EMPTY : template.create();
        stack.shrink(1);
        if (stack.isEmpty()) {
            this.internal.setItem(index, remainder);
        } else {
            this.internal.setItem(index, stack);
            if (!remainder.isEmpty()) {
                this.entity.getInventory().placeItemBackInInventory(remainder, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
    }

    protected ItemStack item(int index) {
        return this.internal.getItem(index);
    }

    private void updateResult(ServerLevel level) {
        ItemStack now = this.computeResult(level);
        if (!ItemStack.matches(now, this.result.getItem(0))) {
            this.result.setItem(0, now);
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.entity.level() instanceof ServerLevel level) {
            this.updateResult(level);
        }
        super.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.result.setItem(0, ItemStack.EMPTY);
    }
}
