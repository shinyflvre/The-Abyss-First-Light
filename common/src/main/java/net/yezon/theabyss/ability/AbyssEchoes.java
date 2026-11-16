package net.yezon.theabyss.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.AbyssEchoEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class AbyssEchoes {
    public static final double SOMNIUM_COST = 40.0;
    public static final int COOLDOWN_TICKS = 300;
    private static final double RANGE = 24.0;
    private static final float DAMAGE = 10.0F;
    private static final int DASH_TICKS = 4;
    private static final int LUNGE_TICKS = 5;

    private static final Map<UUID, Long> READY_AT = new HashMap<>();
    private static final List<Echo> ECHOES = new ArrayList<>();

    private static final class Echo {
        final AbyssEchoEntity entity;
        final ServerPlayer player;
        final LivingEntity target;
        final int delay;
        final boolean aerial;
        final List<Vec3> offsets = new ArrayList<>();
        final Set<Integer> hit = new HashSet<>();
        int age;
        int segment = -1;
        Vec3 from;
        Vec3 to;

        Echo(AbyssEchoEntity entity, ServerPlayer player, LivingEntity target, int delay, boolean aerial, int side) {
            this.entity = entity;
            this.player = player;
            this.target = target;
            this.delay = delay;
            this.aerial = aerial;
            double s = side;
            this.offsets.add(new Vec3(4.0 * s, aerial ? 3.0 : 0.0, 0.5));
            this.offsets.add(new Vec3(-3.5 * s, aerial ? 2.0 : 0.0, -2.5));
            this.offsets.add(new Vec3(2.5 * s, aerial ? 4.0 : 0.0, -4.0));
        }
    }

    private AbyssEchoes() {
    }

    public static void activate(ServerPlayer player) {
        if (!player.getMainHandItem().is(TheabyssModItems.ABYSS_SWORD.get()) || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();
        Long ready = READY_AT.get(player.getUUID());
        if (ready != null && now < ready) {
            player.sendOverlayMessage(Component.translatable("message.theabyss.abyss_echoes.cooldown", (int)Math.ceil((ready - now) / 20.0)));
            return;
        }
        LivingEntity target = aimedAt(player);
        if (target == null) {
            player.sendOverlayMessage(Component.translatable("message.theabyss.abyss_echoes.no_target"));
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        double cost = SOMNIUM_COST * SomniumUpgrades.costFactor(vars);
        if (vars.Mana < cost) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return;
        }
        double remaining = vars.Mana - cost;
        TheabyssModVariables.updatePlayerVariables(player, variables -> {
            variables.Mana = remaining;
            variables.syncPlayerVariables(player);
        });
        READY_AT.put(player.getUUID(), now + COOLDOWN_TICKS);

        boolean aerial = !target.onGround();
        Vec3 forward = horizontal(target.position().subtract(player.position()));
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
        for (int i = 0; i < 2; i++) {
            int sign = i == 0 ? 1 : -1;
            Vec3 at = freeSpot(level, player.position().add(side.scale(2.0 * sign)), player);
            AbyssEchoEntity echo = TheabyssModEntities.ABYSS_ECHO.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (echo == null || at == null) {
                continue;
            }
            echo.setOwnerId(player.getUUID().toString());
            echo.snapTo(at.x, at.y, at.z, player.getYRot(), player.getXRot());
            level.addFreshEntity(echo);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 1.0, at.z, 12, 0.3, 0.7, 0.3, 0.02);
            ECHOES.add(new Echo(echo, player, target, 6 + i * 10, aerial, sign));
        }
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.2F, 0.8F);
    }

    private static LivingEntity aimedAt(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(RANGE));
        HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = block.getType() == HitResult.Type.MISS ? RANGE : block.getLocation().distanceTo(eye);
        AABB box = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, eye.add(look.scale(reach)), box,
            entity -> entity instanceof LivingEntity living && living.isAlive() && !(entity instanceof AbyssEchoEntity) && entity != player, reach * reach);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (Iterator<Echo> it = ECHOES.iterator(); it.hasNext();) {
            Echo echo = it.next();
            if (tick(echo)) {
                dissolve(echo.entity);
                it.remove();
            }
        }
    }

    private static boolean tick(Echo echo) {
        AbyssEchoEntity entity = echo.entity;
        if (!entity.isAlive() || !echo.player.isAlive() || entity.level() != echo.target.level()) {
            return true;
        }
        int t = echo.age++ - echo.delay;
        LivingEntity target = echo.target;
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        entity.setYHeadRot(entity.getYRot());
        entity.setYBodyRot(entity.getYRot());
        if (t < 0) {
            return false;
        }
        if (!target.isAlive()) {
            return true;
        }
        int blinks = echo.offsets.size() * DASH_TICKS;
        if (t < blinks) {
            int index = t / DASH_TICKS;
            if (index != echo.segment) {
                echo.segment = index;
                echo.from = entity.position();
                echo.to = around(entity, target, echo.offsets.get(index), echo.aerial);
                entity.setDashing(true);
                playAt(entity, modSound("dash"), 0.8F, 1.2F + entity.getRandom().nextFloat() * 0.4F);
            }
            move(entity, echo.from, echo.to, t % DASH_TICKS, DASH_TICKS);
            return false;
        }
        int step = t - blinks;
        if (step == 0) {
            echo.from = entity.position();
            Vec3 aim = target.getBoundingBox().getCenter().subtract(0.0, entity.getBbHeight() * 0.5, 0.0);
            echo.to = aim.add(aim.subtract(echo.from).normalize().scale(2.5));
            playAt(entity, modSound("dash"), 1.2F, 0.8F);
        }
        if (step < LUNGE_TICKS) {
            move(entity, echo.from, echo.to, step, LUNGE_TICKS);
            entity.swing(step % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, SwingAnimation.DEFAULT, true);
            Vec3 direction = echo.to.subtract(echo.from).normalize();
            for (LivingEntity victim : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0),
                v -> v.isAlive() && v != echo.player && !(v instanceof AbyssEchoEntity))) {
                if (echo.hit.add(victim.getId()) && entity.level() instanceof ServerLevel level) {
                    victim.hurtServer(level, echo.player.damageSources().playerAttack(echo.player), DAMAGE);
                    victim.knockback(0.8, -direction.x, -direction.z, echo.player.damageSources().playerAttack(echo.player), 0.0F);
                    victim.needsSync = true;
                    playAt(entity, modSound("nightblade_attack"), 1.0F, 1.2F);
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getY() + 1.0, victim.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            return false;
        }
        return step >= LUNGE_TICKS + 4;
    }

    private static void move(AbyssEchoEntity entity, Vec3 from, Vec3 to, int step, int steps) {
        Vec3 wanted = from.lerp(to, (step + 1) / (double)steps);
        Vec3 free = freeSpot((ServerLevel)entity.level(), wanted, entity);
        if (free != null) {
            entity.setPos(free.x, free.y, free.z);
        }
        entity.setDeltaMovement(to.subtract(from).scale(1.0 / steps));
        entity.needsSync = true;
    }

    private static Vec3 around(AbyssEchoEntity entity, LivingEntity target, Vec3 offset, boolean aerial) {
        Vec3 forward = horizontal(target.position().subtract(entity.position()));
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 point = target.position().add(side.scale(offset.x)).add(forward.scale(offset.z));
        if (aerial) {
            return point.add(0.0, offset.y, 0.0);
        }
        BlockPos.MutableBlockPos pos = BlockPos.containing(point.x, target.getY() + 3.0, point.z).mutable();
        for (int i = 0; i < 10; i++) {
            BlockPos below = pos.below();
            if (entity.level().getBlockState(pos).getCollisionShape(entity.level(), pos).isEmpty()
                && !entity.level().getBlockState(below).getCollisionShape(entity.level(), below).isEmpty()) {
                return new Vec3(point.x, pos.getY(), point.z);
            }
            pos.move(0, -1, 0);
        }
        return new Vec3(point.x, target.getY(), point.z);
    }

    private static Vec3 freeSpot(ServerLevel level, Vec3 wanted, net.minecraft.world.entity.Entity entity) {
        for (double up = 0.0; up <= 3.0; up += 0.5) {
            Vec3 at = wanted.add(0.0, up, 0.0);
            AABB box = entity.getType().getDimensions().makeBoundingBox(at);
            if (level.noCollision(entity, box)) {
                return at;
            }
        }
        return null;
    }

    private static void dissolve(AbyssEchoEntity entity) {
        if (entity.isAlive() && entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY() + 1.0, entity.getZ(), 12, 0.3, 0.7, 0.3, 0.02);
            entity.discard();
        }
    }

    private static Vec3 horizontal(Vec3 v) {
        Vec3 flat = new Vec3(v.x, 0.0, v.z);
        return flat.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
    }

    private static SoundEvent modSound(String name) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, name));
    }

    private static void playAt(AbyssEchoEntity entity, SoundEvent sound, float volume, float pitch) {
        if (sound != null) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
