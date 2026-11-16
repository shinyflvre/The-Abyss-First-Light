package net.yezon.theabyss.neoforge.event;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.EventPriority;

public final class NeoForgeEventBridgeGenerated {
    private NeoForgeEventBridgeGenerated() {
    }

    static net.neoforged.bus.api.EventPriority priority(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> net.neoforged.bus.api.EventPriority.HIGHEST;
            case HIGH -> net.neoforged.bus.api.EventPriority.HIGH;
            case NORMAL -> net.neoforged.bus.api.EventPriority.NORMAL;
            case LOW -> net.neoforged.bus.api.EventPriority.LOW;
            case LOWEST -> net.neoforged.bus.api.EventPriority.LOWEST;
        };
    }

    public static void register(IEventBus bus) {
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.event.BlockEntityTypeAddBlocksEvent.class)) {
            bus.addListener(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.event.BlockEntityTypeAddBlocksEvent() {
                @Override
                public void modify(net.minecraft.world.level.block.entity.BlockEntityType<?> a0, net.minecraft.world.level.block.Block... a1) {
                    e.modify(a0, a1);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.event.OnDatapackSyncEvent.class)) {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.OnDatapackSyncEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.event.OnDatapackSyncEvent() {
                @Override
                public void sendRecipes(net.minecraft.world.item.crafting.RecipeType<?>... a0) {
                    e.sendRecipes(a0);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.event.RegisterCommandsEvent.class)) {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.event.RegisterCommandsEvent() {
                @Override
                public com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() {
                    return e.getDispatcher();
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.TagsUpdatedEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.TagsUpdatedEvent.class, e -> {
                net.yezon.theabyss.platform.event.TagsUpdatedEvent c = new net.yezon.theabyss.platform.event.TagsUpdatedEvent();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.VanillaGameEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.VanillaGameEvent.class, e -> {
                net.yezon.theabyss.platform.event.VanillaGameEvent c = new net.yezon.theabyss.platform.event.VanillaGameEvent(e.getCause());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.event.entity.EntityAttributeCreationEvent.class)) {
            bus.addListener(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.event.entity.EntityAttributeCreationEvent() {
                @Override
                public void put(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> a0, net.minecraft.world.entity.ai.attributes.AttributeSupplier a1) {
                    e.put(a0, a1);
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.EntityJoinLevelEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent c = new net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent(e.getEntity(), e.getLevel(), e.loadedFromDisk());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.ProjectileImpactEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.ProjectileImpactEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.ProjectileImpactEvent c = new net.yezon.theabyss.platform.event.entity.ProjectileImpactEvent(e.getProjectile(), e.getRayTraceResult());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.FinalizeSpawnEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.FinalizeSpawnEvent c = new net.yezon.theabyss.platform.event.entity.living.FinalizeSpawnEvent(e.getEntity(), e.getSpawnType(), e.isSpawnCancelled());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setSpawnCancelled(c.getSpawnCancelled());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent(e.getEntity(), e.getNewAboutToBeSetTarget());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent.Post c = new net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent.Post(e.getBlockedDamage(), e.getEntity(), e.getInflictedDamage(), e.getOriginalDamage(), e.getSource());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent.Pre c = new net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent.Pre(e.getEntity(), e.getNewDamage(), e.getSource());
                AbyssEventBus.post(p, c);
                e.setNewDamage(c.getNewDamage());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingDeathEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent(e.getEntity(), e.getSource());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingDropsEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingDropsEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingDropsEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingDropsEvent(e.getDrops(), e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent.Finish.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent.Finish c = new net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent.Finish(e.getEntity(), e.getItem());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent.Start.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent.Start c = new net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent.Start(e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.event.entity.living.LivingEvent.LivingVisibilityEvent.class)) {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingVisibilityEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.event.entity.living.LivingEvent.LivingVisibilityEvent() {
                @Override
                public net.minecraft.world.entity.Entity getEntity() {
                    return e.getEntity();
                }
                @Override
                public void modifyVisibility(double a0) {
                    e.modifyVisibility(a0);
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingExperienceDropEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingExperienceDropEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingExperienceDropEvent(e.getAttackingPlayer(), e.getDroppedExperience(), e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setDroppedExperience(c.getDroppedExperience());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingFallEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingFallEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingFallEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingFallEvent(e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingHealEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingHealEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingHealEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingHealEvent(e.getAmount(), e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setAmount(c.getAmount());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent c = new net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent(e.getAmount(), e.getEntity(), e.getSource());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setAmount(c.getAmount());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Added.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Added c = new net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Added(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Expired.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Expired c = new net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Expired(e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Remove.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove.class, e -> {
                net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Remove c = new net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Remove(e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.AttackEntityEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent c = new net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent(e.getEntity(), e.getTarget());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.ItemTooltipEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.ItemTooltipEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.ItemTooltipEvent c = new net.yezon.theabyss.platform.event.entity.player.ItemTooltipEvent(e.getEntity(), e.getFlags(), e.getItemStack(), e.getToolTip());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerContainerEvent.Open.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Open.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerContainerEvent.Open c = new net.yezon.theabyss.platform.event.entity.player.PlayerContainerEvent.Open(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerEvent.Clone.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerEvent.Clone c = new net.yezon.theabyss.platform.event.entity.player.PlayerEvent.Clone(e.getEntity(), e.getOriginal());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent c = new net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(e.getEntity(), e.getFrom(), e.getTo());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerLoggedInEvent c = new net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerLoggedInEvent(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerLoggedOutEvent c = new net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerRespawnEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerRespawnEvent c = new net.yezon.theabyss.platform.event.entity.player.PlayerEvent.PlayerRespawnEvent(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerEvent.StartTracking.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerEvent.StartTracking c = new net.yezon.theabyss.platform.event.entity.player.PlayerEvent.StartTracking(e.getEntity(), e.getTarget());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.EntityInteract.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.EntityInteract c = new net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.EntityInteract(e.getEntity(), e.getItemStack(), e.getTarget());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.LeftClickBlock.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.LeftClickBlock c = new net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.LeftClickBlock(e.getEntity(), e.getLevel(), e.getPos());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.RightClickBlock.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.RightClickBlock c = new net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.RightClickBlock(e.getEntity(), e.getHand(), e.getItemStack(), e.getLevel(), e.getPos(), e.getUseItem());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setUseItem(c.getUseItem());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.RightClickItem.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.RightClickItem c = new net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent.RightClickItem(e.getEntity(), e.getItemStack());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.entity.player.PlayerWakeUpEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent.class, e -> {
                net.yezon.theabyss.platform.event.entity.player.PlayerWakeUpEvent c = new net.yezon.theabyss.platform.event.entity.player.PlayerWakeUpEvent(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.level.BlockDropsEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.level.BlockDropsEvent.class, e -> {
                net.yezon.theabyss.platform.event.level.BlockDropsEvent c = new net.yezon.theabyss.platform.event.level.BlockDropsEvent(e.getDrops(), e.getLevel(), e.getPos(), e.getState(), e.getTool());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.level.BlockEvent.EntityPlaceEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent.class, e -> {
                net.yezon.theabyss.platform.event.level.BlockEvent.EntityPlaceEvent c = new net.yezon.theabyss.platform.event.level.BlockEvent.EntityPlaceEvent(e.getEntity(), e.getLevel(), e.getPos());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.level.ChunkEvent.Unload.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.level.ChunkEvent.Unload.class, e -> {
                net.yezon.theabyss.platform.event.level.ChunkEvent.Unload c = new net.yezon.theabyss.platform.event.level.ChunkEvent.Unload(e.getChunk(), e.getLevel());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.level.ExplosionEvent.Detonate.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.level.ExplosionEvent.Detonate.class, e -> {
                net.yezon.theabyss.platform.event.level.ExplosionEvent.Detonate c = new net.yezon.theabyss.platform.event.level.ExplosionEvent.Detonate(e.getAffectedBlocks(), e.getLevel());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.level.block.BreakBlockEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.level.block.BreakBlockEvent.class, e -> {
                net.yezon.theabyss.platform.event.level.block.BreakBlockEvent c = new net.yezon.theabyss.platform.event.level.block.BreakBlockEvent(e.getLevel(), e.getPlayer(), e.getPos());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.server.ServerStoppedEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.server.ServerStoppedEvent.class, e -> {
                net.yezon.theabyss.platform.event.server.ServerStoppedEvent c = new net.yezon.theabyss.platform.event.server.ServerStoppedEvent();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.server.ServerStoppingEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.server.ServerStoppingEvent.class, e -> {
                net.yezon.theabyss.platform.event.server.ServerStoppingEvent c = new net.yezon.theabyss.platform.event.server.ServerStoppingEvent(e.getServer());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.tick.EntityTickEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.tick.EntityTickEvent.Post.class, e -> {
                net.yezon.theabyss.platform.event.tick.EntityTickEvent.Post c = new net.yezon.theabyss.platform.event.tick.EntityTickEvent.Post(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.tick.EntityTickEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.tick.EntityTickEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.event.tick.EntityTickEvent.Pre c = new net.yezon.theabyss.platform.event.tick.EntityTickEvent.Pre(e.getEntity());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.tick.LevelTickEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.tick.LevelTickEvent.Post.class, e -> {
                net.yezon.theabyss.platform.event.tick.LevelTickEvent.Post c = new net.yezon.theabyss.platform.event.tick.LevelTickEvent.Post(e.getLevel());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.tick.PlayerTickEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.tick.PlayerTickEvent.Post.class, e -> {
                net.yezon.theabyss.platform.event.tick.PlayerTickEvent.Post c = new net.yezon.theabyss.platform.event.tick.PlayerTickEvent.Post(e.getEntity());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.tick.ServerTickEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, e -> {
                net.yezon.theabyss.platform.event.tick.ServerTickEvent.Post c = new net.yezon.theabyss.platform.event.tick.ServerTickEvent.Post(e.getServer());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.event.tick.ServerTickEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.tick.ServerTickEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.event.tick.ServerTickEvent.Pre c = new net.yezon.theabyss.platform.event.tick.ServerTickEvent.Pre();
                AbyssEventBus.post(p, c);
            });
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.registries.RegisterEvent.class)) {
            bus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.registries.RegisterEvent() {
                @Override
                public <T> void register(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> a0, net.minecraft.resources.Identifier a1, java.util.function.Supplier<T> a2) {
                    e.register(a0, a1, a2);
                }
            }));
        }
    }
}
