package net.yezon.theabyss.possession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import org.jspecify.annotations.Nullable;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class PossessionManager {
    public static final double RANGE = 20.0;
    private static final double START_COST = 20.0;
    private static final double DRAIN_PER_SECOND = 2.0;
    private static final double ABILITY_RANGE = 32.0;
    private static final String BODY_TAG = "theabyss.possession_body";

    private record Session(UUID mob, ResourceKey<Level> bodyLevel, Vec3 bodyPos, float bodyYRot, float bodyXRot, @Nullable UUID body) {
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, UUID> POSSESSED = new HashMap<>();

    private PossessionManager() {
    }

    public static boolean isPossessing(Player player) {
        return PossessionData.of(player).active();
    }

    public static @Nullable Mob findTarget(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
        BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        AABB box = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box, e -> e instanceof Mob && e.isPickable() && e.isAlive(), eye.distanceToSqr(end));
        return hit != null && hit.getEntity() instanceof Mob mob ? mob : null;
    }

    public static boolean tryPossess(ServerPlayer player, Entity target) {
        if (isPossessing(player)) {
            return false;
        }
        if (AstralData.isAstral(player)) {
            player.sendOverlayMessage(Component.translatable("possession.theabyss.cannot_astral"));
            return false;
        }
        if (!(target instanceof Mob mob) || !mob.isAlive() || mob.is(Tags.EntityTypes.BOSSES) || mob.isVehicle()
            || POSSESSED.containsKey(mob.getUUID())) {
            player.sendOverlayMessage(Component.translatable("possession.theabyss.cannot"));
            return false;
        }
        if (player.isPassenger() || player.distanceTo(mob) > RANGE + mob.getBbWidth()) {
            player.sendOverlayMessage(Component.translatable("possession.theabyss.cannot"));
            return false;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        double cost = START_COST * SomniumUpgrades.costFactor(vars);
        if (vars.Mana < cost) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return false;
        }

        ServerLevel level = player.level();
        Mannequin body = createBody(player);
        Session session = new Session(
            mob.getUUID(), level.dimension(), player.position(), player.getYRot(), player.getXRot(), body != null ? body.getUUID() : null
        );
        if (!player.startRiding(mob, true, false)) {
            player.sendOverlayMessage(Component.translatable("possession.theabyss.cannot"));
            return false;
        }
        SESSIONS.put(player.getUUID(), session);
        if (body != null) {
            level.addFreshEntity(body);
        }
        POSSESSED.put(mob.getUUID(), player.getUUID());
        TheabyssModVariables.updatePlayerVariables(player, capability -> {
            capability.Mana = capability.Mana - cost;
            capability.syncPlayerVariables(player);
        });
        hideBody(player);
        player.setData(PossessionData.TYPE, new PossessionData(mob.getId()));
        freezeMind(mob);
        level.playSound(null, mob.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.7F);
        net.yezon.theabyss.ring.RingVisuals.possessionEnter(level, player, mob);
        player.sendOverlayMessage(Component.translatable("possession.theabyss.start", mob.getDisplayName()));
        return true;
    }

    private static void hideBody(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false, false));
    }

    public static void release(ServerPlayer player, String messageKey) {
        end(player, messageKey, true);
    }

    private static void end(ServerPlayer player, String messageKey, boolean returnToBody) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) {
            return;
        }
        POSSESSED.remove(session.mob());
        MinecraftServer server = player.level().getServer();
        Entity mobEntity = player.getVehicle();
        if (!(mobEntity instanceof Mob)) {
            mobEntity = player.level().getEntity(session.mob());
        }
        player.setData(PossessionData.TYPE, PossessionData.NONE);
        player.stopRiding();
        player.removeEffect(MobEffects.INVISIBILITY);
        if (mobEntity instanceof Mob mob && mob.isAlive()) {
            restoreMind(mob);
            if (mob.level() instanceof ServerLevel mobLevel) {
                net.yezon.theabyss.ring.RingVisuals.possessionRelease(mobLevel, mob);
            }
        }
        ServerLevel bodyLevel = server.getLevel(session.bodyLevel());
        if (bodyLevel == null) {
            bodyLevel = player.level();
        }
        removeBody(server, session);
        if (!returnToBody) {
            return;
        }
        player.getCooldowns().addCooldown(new ItemStack(TheabyssModItems.RING_OF_POSSESSION.get()), 40);
        player.teleportTo(bodyLevel, session.bodyPos().x, session.bodyPos().y, session.bodyPos().z, Set.of(), session.bodyYRot(), session.bodyXRot(), true);
        player.resetFallDistance();
        bodyLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
        player.sendOverlayMessage(Component.translatable(messageKey));
    }

    private static @Nullable Mannequin createBody(ServerPlayer player) {
        Mannequin body = EntityTypes.MANNEQUIN.create(player.level(), EntitySpawnReason.COMMAND);
        if (body == null) {
            return null;
        }
        body.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.level().registryAccess());
        body.saveWithoutId(out);
        var tag = out.buildResult();
        tag.putBoolean("immovable", true);
        tag.putBoolean("hide_description", true);
        tag.putBoolean("Invulnerable", true);
        body.load(TagValueInput.create(ProblemReporter.DISCARDING, player.level().registryAccess(), tag));
        body.setComponent(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
        body.setCustomName(player.getDisplayName());
        body.setCustomNameVisible(false);
        body.addTag(BODY_TAG);
        return body;
    }

    private static void removeBody(MinecraftServer server, Session session) {
        if (session.body() == null) {
            return;
        }
        ServerLevel level = server.getLevel(session.bodyLevel());
        Entity body = level != null ? level.getEntity(session.body()) : null;
        if (body != null) {
            body.discard();
        }
    }

    private static void freezeMind(Mob mob) {
        mob.goalSelector.disableControlFlag(Goal.Flag.MOVE);
        mob.goalSelector.disableControlFlag(Goal.Flag.LOOK);
        mob.goalSelector.disableControlFlag(Goal.Flag.JUMP);
        mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
        mob.setTarget(null);
        mob.getNavigation().stop();
    }

    private static void restoreMind(Mob mob) {
        mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        mob.goalSelector.enableControlFlag(Goal.Flag.LOOK);
        mob.goalSelector.enableControlFlag(Goal.Flag.JUMP);
        mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
    }

    private static @Nullable Mob possessedMob(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return null;
        }
        return player.level().getEntity(session.mob()) instanceof Mob mob ? mob : null;
    }

    public static int useAbility(ServerPlayer player) {
        Mob mob = possessedMob(player);
        if (mob == null) {
            return 0;
        }
        ServerLevel level = player.level();
        Vec3 eye = mob.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(ABILITY_RANGE));
        BlockHitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        Vec3 aimPoint = block.getType() != HitResult.Type.MISS ? block.getLocation() : end;
        AABB box = mob.getBoundingBox().expandTowards(aimPoint.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(mob, eye, aimPoint, box,
            e -> e != player && e != mob && e.isPickable() && !e.isSpectator(), eye.distanceToSqr(aimPoint));
        LivingEntity target = hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;

        if (mob instanceof Creeper creeper) {
            creeper.ignite();
            return 30;
        }
        if (mob instanceof Blaze) {
            Vec3 direction = aimPoint.subtract(mob.getX(), mob.getY(0.5) + 0.5, mob.getZ()).normalize();
            level.levelEvent(null, 1018, mob.blockPosition(), 0);
            SmallFireball fireball = new SmallFireball(level, mob, direction);
            fireball.setPos(fireball.getX(), mob.getY(0.5) + 0.5, fireball.getZ());
            level.addFreshEntity(fireball);
            return 10;
        }
        if (mob instanceof Ghast ghast) {
            Vec3 view = look.normalize();
            level.levelEvent(null, 1016, mob.blockPosition(), 0);
            LargeFireball fireball = new LargeFireball(level, mob, view, ghast.getExplosionPower());
            fireball.setPos(mob.getX() + view.x * 4.0, mob.getY(0.5) + 0.5, mob.getZ() + view.z * 4.0);
            level.addFreshEntity(fireball);
            return 40;
        }
        if (mob instanceof RangedAttackMob ranged && canShoot(mob)) {
            LivingEntity aim = target;
            if (aim == null) {
                ArmorStand dummy = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
                if (dummy == null) {
                    return 0;
                }
                dummy.setPos(aimPoint.x, aimPoint.y - dummy.getBbHeight() / 3.0, aimPoint.z);
                aim = dummy;
            }
            mob.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
            ranged.performRangedAttack(aim, 1.0F);
            return 20;
        }
        if (target != null && mob.isWithinMeleeAttackRange(target)) {
            mob.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
            mob.doHurtTarget(level, target);
            return 10;
        }
        return 0;
    }

    private static boolean canShoot(Mob mob) {
        if (mob instanceof net.minecraft.world.entity.monster.skeleton.AbstractSkeleton) {
            return mob.isHolding(stack -> stack.getItem() instanceof net.minecraft.world.item.BowItem);
        }
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        Mob mob = possessedMob(player);
        if (mob == null || !mob.isAlive() || player.getVehicle() != mob) {
            release(player, mob != null && !mob.isAlive() ? "possession.theabyss.returned" : "possession.theabyss.left");
            return;
        }
        player.resetFallDistance();
        if (player.tickCount % 20 == 0) {
            hideBody(player);
            TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
            double drain = DRAIN_PER_SECOND * SomniumUpgrades.costFactor(vars);
            if (vars.Mana < drain) {
                release(player, "possession.theabyss.no_energy");
                return;
            }
            TheabyssModVariables.updatePlayerVariables(player, capability -> {
                capability.Mana = capability.Mana - drain;
                capability.syncPlayerVariables(player);
            });
        }
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide() && POSSESSED.containsKey(mob.getUUID())
            && mob.getControllingPassenger() == null && mob.getFirstPassenger() instanceof ServerPlayer player) {
            steer(mob, player);
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide() && POSSESSED.containsKey(mob.getUUID())
            && mob.getFirstPassenger() instanceof ServerPlayer player) {
            freezeMind(mob);
            net.yezon.theabyss.ring.RingVisuals.cursedTick((ServerLevel)mob.level(), mob);
            float yaw = player.getYRot();
            mob.setYRot(yaw);
            mob.setYHeadRot(yaw);
            mob.setYBodyRot(yaw);
            mob.setXRot(player.getXRot());
        }
    }

    private static void steer(Mob mob, ServerPlayer player) {
        Input input = player.getLastClientInput();
        double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        double strafe = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
        boolean free3d = mob.isNoGravity() || mob.getMoveControl() instanceof FlyingMoveControl || mob instanceof Ghast
            || mob.getNavigation() instanceof FlyingPathNavigation || mob.isInWater();
        double yaw = Math.toRadians(player.getYRot());
        double dx = -Math.sin(yaw) * forward + Math.cos(yaw) * strafe;
        double dz = Math.cos(yaw) * forward + Math.sin(yaw) * strafe;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) {
            dx /= len;
            dz /= len;
        }
        double dy = 0;
        if (free3d) {
            dy = -Math.sin(Math.toRadians(player.getXRot())) * forward + (input.jump() ? 1 : 0);
        }
        if (len == 0 && dy == 0) {
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
        } else {
            double reach = 3.0;
            mob.getMoveControl().setWantedPosition(mob.getX() + dx * reach, mob.getY() + dy * reach, mob.getZ() + dz * reach, input.sprint() ? 1.4 : 1.0);
        }
        if (input.jump() && !free3d && mob.onGround()) {
            mob.getJumpControl().jump();
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer dying && SESSIONS.containsKey(dying.getUUID())) {
            end(dying, "possession.theabyss.left", false);
            return;
        }
        UUID playerId = POSSESSED.get(event.getEntity().getUUID());
        if (playerId != null && event.getEntity().level().getServer() != null) {
            ServerPlayer player = event.getEntity().level().getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                release(player, "possession.theabyss.returned");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SESSIONS.containsKey(player.getUUID())
            && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SESSIONS.containsKey(player.getUUID())) {
            event.setCanceled(true);
            Mob mob = possessedMob(player);
            if (mob != null && event.getTarget() instanceof LivingEntity target && target != mob && mob.isWithinMeleeAttackRange(target)) {
                mob.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
                mob.doHurtTarget(player.level(), target);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (blockedWhilePossessing(event.getEntity(), event.getItemStack().is(TheabyssModItems.RING_OF_POSSESSION.get()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (blockedWhilePossessing(event.getEntity(), event.getItemStack().is(TheabyssModItems.RING_OF_POSSESSION.get()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (blockedWhilePossessing(event.getEntity(), event.getItemStack().is(TheabyssModItems.RING_OF_POSSESSION.get()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (isPossessing(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    private static boolean blockedWhilePossessing(Player player, boolean holdingRing) {
        return isPossessing(player) && !holdingRing;
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mannequin body && body.entityTags().contains(BODY_TAG)
            && SESSIONS.values().stream().noneMatch(session -> body.getUUID().equals(session.body()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            release(player, "possession.theabyss.left");
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (UUID id : List.copyOf(SESSIONS.keySet())) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(id);
            if (player != null) {
                release(player, "possession.theabyss.left");
            }
        }
        SESSIONS.clear();
        POSSESSED.clear();
    }
}
