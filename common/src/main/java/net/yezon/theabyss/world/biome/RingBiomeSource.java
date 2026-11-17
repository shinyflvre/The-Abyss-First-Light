package net.yezon.theabyss.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public final class RingBiomeSource extends BiomeSource {
    public record Rule(Holder<Biome> biome, Holder<Biome> ring, int width, List<Holder<Biome>> ignore) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
            Biome.CODEC.fieldOf("biome").forGetter(Rule::biome),
            Biome.CODEC.fieldOf("ring").forGetter(Rule::ring),
            Codec.intRange(4, 512).fieldOf("width").forGetter(Rule::width),
            Biome.CODEC.listOf().optionalFieldOf("ignore", List.of()).forGetter(Rule::ignore)
        ).apply(i, Rule::new));
    }

    public static final MapCodec<RingBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BiomeSource.CODEC.fieldOf("base").forGetter(RingBiomeSource::base),
        Rule.CODEC.listOf().optionalFieldOf("rings", List.of()).forGetter(RingBiomeSource::rings)
    ).apply(i, RingBiomeSource::new));
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> REGISTRY = DeferredRegister.create(Registries.BIOME_SOURCE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<RingBiomeSource>> RINGS = REGISTRY.register("rings", () -> CODEC);
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private record Compiled(ResourceKey<Biome> ring, int far, int near, Set<ResourceKey<Biome>> ignore) {
    }

    private final BiomeSource base;
    private final List<Rule> rings;
    private final Map<ResourceKey<Biome>, Compiled> compiled = new HashMap<>();

    private RingBiomeSource(BiomeSource base, List<Rule> rings) {
        this.base = base;
        this.rings = rings;
        for (Rule rule : rings) {
            Optional<ResourceKey<Biome>> key = rule.biome().unwrapKey();
            Optional<ResourceKey<Biome>> ring = rule.ring().unwrapKey();
            if (key.isEmpty() || ring.isEmpty()) {
                continue;
            }
            Set<ResourceKey<Biome>> ignore = new HashSet<>();
            for (Holder<Biome> holder : rule.ignore()) {
                holder.unwrapKey().ifPresent(ignore::add);
            }
            int far = Math.max(1, Math.round(rule.width() / 4.0F));
            this.compiled.put(key.get(), new Compiled(ring.get(), far, Math.max(1, far / 2), ignore));
        }
    }

    public BiomeSource base() {
        return this.base;
    }

    public List<Rule> rings() {
        return this.rings;
    }

    @Override
    protected MapCodec<RingBiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(this.base.possibleBiomes().stream(), this.rings.stream().map(Rule::ring)).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        Holder<Biome> biome = this.base.getNoiseBiome(quartX, quartY, quartZ, sampler);
        if (this.compiled.isEmpty()) {
            return biome;
        }
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        Compiled rule = key.isPresent() ? this.compiled.get(key.get()) : null;
        if (rule == null) {
            return biome;
        }
        for (int step : new int[]{rule.near(), rule.far()}) {
            for (int[] d : DIRECTIONS) {
                Optional<ResourceKey<Biome>> other = this.base.getNoiseBiome(quartX + d[0] * step, quartY, quartZ + d[1] * step, sampler).unwrapKey();
                if (other.isPresent() && !other.get().equals(key.get()) && !other.get().equals(rule.ring()) && !rule.ignore().contains(other.get())) {
                    return this.ringHolder(rule.ring());
                }
            }
        }
        return biome;
    }

    private Holder<Biome> ringHolder(ResourceKey<Biome> key) {
        for (Rule rule : this.rings) {
            if (rule.ring().is(key)) {
                return rule.ring();
            }
        }
        throw new IllegalStateException("Ring biome missing: " + key);
    }
}
