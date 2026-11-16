package net.yezon.theabyss.astral;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.VanillaGameEvent;
import net.yezon.theabyss.platform.event.entity.ProjectileImpactEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerContainerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent;
import net.yezon.theabyss.platform.event.level.BlockEvent;
import net.yezon.theabyss.platform.event.level.block.BreakBlockEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.possession.PossessionManager;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class AstralPlane {
    static final double ENTER_COST = 15.0;
    static final double DRAIN_PER_SECOND = 1.0;
    static final double BREAK_COST = 2.0;
    static final double PLACE_COST = 2.0;
    static final double MENU_COST = 3.0;
    private static final double FORGET_RADIUS = 48.0;

    private AstralPlane() {
    }

    public static boolean isAstral(Entity entity) {
        return AstralData.isAstral(entity);
    }

    public static boolean toggle(ServerPlayer player) {
        if (isAstral(player)) {
            leave(player, "astral.theabyss.leave");
            return true;
        }
        if (PossessionManager.isPossessing(player)) {
            player.sendOverlayMessage(Component.translatable("astral.theabyss.cannot_now"));
            return false;
        }
        if (!pay(player, ENTER_COST)) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return false;
        }
        net.yezon.theabyss.platform.attachment.Attachments.setData(player, AstralData.TYPE, new AstralData(true));
        player.setSilent(true);
        forgetMe(player);
        ServerLevel level = player.level();
        net.yezon.theabyss.ring.fx.RingAccent.send(level, net.yezon.theabyss.ring.fx.RingAccent.SPECTRAL, player.position(), player);
        net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.RUNES, player, 0xE8E0FF, 2.0F, 30);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.35, 0.8, 0.35, 0.05);
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.8, player.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.7F, 0.6F);
        player.sendOverlayMessage(Component.translatable("astral.theabyss.enter"));
        return true;
    }

    public static void leave(ServerPlayer player, String messageKey) {
        if (!isAstral(player)) {
            return;
        }
        net.yezon.theabyss.platform.attachment.Attachments.setData(player, AstralData.TYPE, AstralData.NONE);
        player.setSilent(false);
        ServerLevel level = player.level();
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.3, 0.7, 0.3, 0.04);
        level.sendParticles(ParticleTypes.WHITE_ASH, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.8, 0.5, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.7F, 1.4F);
        if (messageKey != null) {
            player.sendOverlayMessage(Component.translatable(messageKey));
        }
    }

    static boolean pay(Player player, double amount) {
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        double cost = amount * SomniumUpgrades.costFactor(vars);
        if (vars.Mana < cost) {
            return false;
        }
        TheabyssModVariables.updatePlayerVariables(player, capability -> {
            capability.Mana = capability.Mana - cost;
            capability.syncPlayerVariables(player);
        });
        return true;
    }

    private static void forgetMe(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(FORGET_RADIUS))) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
            var brain = mob.getBrain();
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.getMemory(MemoryModuleType.ATTACK_TARGET).filter(target -> target == player).isPresent()) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)
                && brain.getMemory(MemoryModuleType.ANGRY_AT).filter(id -> id.equals(player.getUUID())).isPresent()) {
                brain.eraseMemory(MemoryModuleType.ANGRY_AT);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isAstral(player)) {
            return;
        }
        if (player.tickCount % 20 == 0 && !pay(player, DRAIN_PER_SECOND)) {
            leave(player, "astral.theabyss.no_energy");
            return;
        }
        if (player.tickCount % 10 == 0) {
            forgetMe(player);
            player.setSilent(true);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (isAstral(event.getNewAboutToBeSetTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (isAstral(event.getEntity())) {
            event.modifyVisibility(0.0);
        }
    }

    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        if (isAstral(event.getCause())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (isAstral(victim) && (attacker != null && !isAstral(attacker) || direct != null && !isAstral(direct))) {
            event.setCanceled(true);
        } else if (attacker != victim && (isAstral(attacker) || isAstral(direct))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            boolean astralShooter = isAstral(event.getProjectile().getOwner());
            if (isAstral(target) != astralShooter) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (isAstral(event.getEntity())) {
            event.setCanceled(true);
            event.getEntity().sendOverlayMessage(Component.translatable("astral.theabyss.cannot_touch"));
        } else if (isAstral(event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isAstral(event.getEntity()) || isAstral(event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (isAstral(event.getPlayer()) && !pay(event.getPlayer(), BREAK_COST)) {
            event.setCanceled(true);
            event.getPlayer().sendOverlayMessage(Component.translatable("astral.theabyss.no_energy_block"));
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && isAstral(player) && !pay(player, PLACE_COST)) {
            event.setCanceled(true);
            player.sendOverlayMessage(Component.translatable("astral.theabyss.no_energy_block"));
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !isAstral(player)) {
            return;
        }
        boolean hasMenu = event.getLevel().getBlockState(event.getPos()).getMenuProvider(event.getLevel(), event.getPos()) != null;
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (hasMenu && !player.isSecondaryUseActive() && vars.Mana < MENU_COST * SomniumUpgrades.costFactor(vars)) {
            event.setCanceled(true);
            player.sendOverlayMessage(Component.translatable("astral.theabyss.no_energy_block"));
        }
    }

    @SubscribeEvent
    public static void onMenuOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && isAstral(player) && !pay(player, MENU_COST)) {
            player.closeContainer();
            player.sendOverlayMessage(Component.translatable("astral.theabyss.no_energy_block"));
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isAstral(player)) {
            leave(player, null);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isAstral(player)) {
            player.setSilent(true);
        }
    }
}
