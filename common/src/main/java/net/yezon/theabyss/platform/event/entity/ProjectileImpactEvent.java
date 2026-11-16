package net.yezon.theabyss.platform.event.entity;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class ProjectileImpactEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.projectile.Projectile projectile;
    private final net.minecraft.world.phys.HitResult rayTraceResult;

    public ProjectileImpactEvent(net.minecraft.world.entity.projectile.Projectile projectile, net.minecraft.world.phys.HitResult rayTraceResult) {
        this.projectile = projectile;
        this.rayTraceResult = rayTraceResult;
    }

    public net.minecraft.world.entity.projectile.Projectile getProjectile() {
        return this.projectile;
    }

    public net.minecraft.world.phys.HitResult getRayTraceResult() {
        return this.rayTraceResult;
    }
}
