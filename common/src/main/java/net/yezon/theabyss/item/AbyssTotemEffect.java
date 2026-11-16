package net.yezon.theabyss.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.timestop.TimeStop;

public record AbyssTotemEffect(Kind kind) implements ConsumeEffect {
    public enum Kind implements StringRepresentable {
        ABYSS("abyss"), TIME("time"), THUNDER("thunder");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String id;

        Kind(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final MapCodec<AbyssTotemEffect> CODEC = Kind.CODEC.fieldOf("kind").xmap(AbyssTotemEffect::new, AbyssTotemEffect::kind);
    public static final StreamCodec<RegistryFriendlyByteBuf, AbyssTotemEffect> STREAM_CODEC = ByteBufCodecs.VAR_INT
        .<RegistryFriendlyByteBuf>cast()
        .map(i -> new AbyssTotemEffect(Kind.values()[i]), effect -> effect.kind().ordinal());
    public static final ConsumeEffect.Type<AbyssTotemEffect> TYPE = new ConsumeEffect.Type<>(CODEC, STREAM_CODEC);
    public static final DeferredRegister<ConsumeEffect.Type<?>> REGISTRY = DeferredRegister.create(Registries.CONSUME_EFFECT_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<AbyssTotemEffect>> ABYSS_TOTEM = REGISTRY.register("abyss_totem", () -> TYPE);

    private static final double THUNDER_RADIUS = 10.0;
    private static final float THUNDER_DAMAGE = 10.0F;

    public static DeathProtection protection(Kind kind) {
        return new DeathProtection(List.of(new AbyssTotemEffect(kind)));
    }

    @Override
    public ConsumeEffect.Type<AbyssTotemEffect> getType() {
        return TYPE;
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity user) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        user.setHealth(user.getMaxHealth());
        server.sendParticles(TheabyssModParticleTypes.END_SWORD_PT.get(), user.getX(), user.getY(), user.getZ(), 10, 0.5, 0.5, 0.5, 1.0);
        server.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, user.getX(), user.getY(), user.getZ(), 1, 0.5, 0.5, 0.5, 1.0);
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, user.getX(), user.getY(), user.getZ(), 2, 0.5, 0.5, 0.5, 1.0);
        switch (this.kind) {
            case TIME -> TimeStop.totemActivated(user);
            case THUNDER -> thunder(server, user);
            case ABYSS -> {
            }
        }
        return true;
    }

    private static void thunder(ServerLevel level, LivingEntity user) {
        Vec3 centre = user.position();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(THUNDER_RADIUS),
            target -> target != user && target.isAlive())) {
            LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(target.position())));
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            target.hurtServer(level, level.damageSources().generic(), THUNDER_DAMAGE);
        }
    }
}
