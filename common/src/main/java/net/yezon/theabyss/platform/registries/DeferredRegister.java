package net.yezon.theabyss.platform.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeferredRegister<T> {
    private static final List<DeferredRegister<?>> ALL = Collections.synchronizedList(new ArrayList<>());

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Function<Identifier, ? extends T>> entries = new LinkedHashMap<>();
    private final Map<Identifier, Identifier> aliases = new LinkedHashMap<>();
    private boolean registered;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
        ALL.add(this);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    public static <T> DeferredRegister<T> create(Identifier registryName, String namespace) {
        return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), namespace);
    }

    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    public static Entities createEntities(String namespace) {
        return new Entities(namespace);
    }

    public static List<DeferredRegister<?>> all() {
        synchronized (ALL) {
            return List.copyOf(ALL);
        }
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        return this.register(name, key -> sup.get());
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> func) {
        if (this.registered) {
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after it was registered");
        }
        Identifier key = Identifier.fromNamespaceAndPath(this.namespace, name);
        DeferredHolder<T, I> holder = NeoForgeRegistries.Keys.PLATFORM_MANAGED.contains(this.registryKey)
            ? new ValueHolder<>(ResourceKey.create(this.registryKey, key), func)
            : this.createHolder(this.registryKey, key);
        if (this.entries.putIfAbsent(holder, func) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }
        return holder;
    }

    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, Identifier key) {
        return DeferredHolder.create(registryKey, key);
    }

    public TagKey<T> createTagKey(String path) {
        return this.createTagKey(Identifier.fromNamespaceAndPath(this.namespace, path));
    }

    public TagKey<T> createTagKey(Identifier location) {
        return TagKey.create(this.registryKey, location);
    }

    public void addAlias(Identifier from, Identifier to) {
        this.aliases.put(from, to);
    }

    public Map<Identifier, Identifier> getAliases() {
        return Collections.unmodifiableMap(this.aliases);
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    public Map<DeferredHolder<T, ? extends T>, Function<Identifier, ? extends T>> getFactories() {
        return Collections.unmodifiableMap(this.entries);
    }

    public boolean isPlatformManaged() {
        return NeoForgeRegistries.Keys.PLATFORM_MANAGED.contains(this.registryKey);
    }

    public void markRegistered() {
        this.registered = true;
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    public Identifier getRegistryName() {
        return this.registryKey.identifier();
    }

    public String getNamespace() {
        return this.namespace;
    }

    public static class Blocks extends DeferredRegister<Block> {
        protected Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Function<Identifier, ? extends B> func) {
            return (DeferredBlock<B>)super.register(name, func);
        }

        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> sup) {
            return this.register(name, key -> sup.get());
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func, Supplier<BlockBehaviour.Properties> properties) {
            return this.register(name, key -> func.apply(properties.get().setId(ResourceKey.create(Registries.BLOCK, key))));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func, UnaryOperator<BlockBehaviour.Properties> properties) {
            return this.registerBlock(name, func, () -> properties.apply(BlockBehaviour.Properties.of()));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func) {
            return this.registerBlock(name, func, BlockBehaviour.Properties::of);
        }

        public DeferredBlock<Block> registerSimpleBlock(String name, Supplier<BlockBehaviour.Properties> properties) {
            return this.registerBlock(name, Block::new, properties);
        }

        public DeferredBlock<Block> registerSimpleBlock(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
            return this.registerBlock(name, Block::new, properties);
        }

        public DeferredBlock<Block> registerSimpleBlock(String name) {
            return this.registerSimpleBlock(name, BlockBehaviour.Properties::of);
        }

        @Override
        protected <I extends Block> DeferredBlock<I> createHolder(ResourceKey<? extends Registry<Block>> registryKey, Identifier key) {
            return DeferredBlock.createBlock(ResourceKey.create(registryKey, key));
        }
    }

    public static class Items extends DeferredRegister<Item> {
        protected Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends Item> DeferredItem<I> register(String name, Function<Identifier, ? extends I> func) {
            return (DeferredItem<I>)super.register(name, func);
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            return this.register(name, key -> sup.get());
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block, Supplier<Item.Properties> properties) {
            return this.registerItem(name, props -> new BlockItem(block.get(), props), () -> properties.get().useBlockDescriptionPrefix());
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block, UnaryOperator<Item.Properties> properties) {
            return this.registerSimpleBlockItem(name, block, () -> properties.apply(new Item.Properties()));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block) {
            return this.registerSimpleBlockItem(name, block, Item.Properties::new);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Supplier<Item.Properties> properties) {
            return this.registerSimpleBlockItem(block.getId().getPath(), block, properties);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, UnaryOperator<Item.Properties> properties) {
            return this.registerSimpleBlockItem(block.getId().getPath(), block, properties);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block) {
            return this.registerSimpleBlockItem(block.getId().getPath(), block);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block, Supplier<Item.Properties> properties) {
            return this.registerSimpleBlockItem(block.unwrapKey().orElseThrow().identifier().getPath(), block::value, properties);
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func, Supplier<Item.Properties> properties) {
            return this.register(name, key -> func.apply(properties.get().setId(ResourceKey.create(Registries.ITEM, key))));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func, UnaryOperator<Item.Properties> properties) {
            return this.registerItem(name, func, () -> properties.apply(new Item.Properties()));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func) {
            return this.registerItem(name, func, Item.Properties::new);
        }

        public DeferredItem<Item> registerSimpleItem(String name, Supplier<Item.Properties> properties) {
            return this.registerItem(name, Item::new, properties);
        }

        public DeferredItem<Item> registerSimpleItem(String name, UnaryOperator<Item.Properties> properties) {
            return this.registerItem(name, Item::new, properties);
        }

        public DeferredItem<Item> registerSimpleItem(String name) {
            return this.registerItem(name, Item::new);
        }

        @Override
        protected <I extends Item> DeferredItem<I> createHolder(ResourceKey<? extends Registry<Item>> registryKey, Identifier key) {
            return DeferredItem.createItem(ResourceKey.create(registryKey, key));
        }
    }

    public static class Entities extends DeferredRegister<EntityType<?>> {
        protected Entities(String namespace) {
            super(Registries.ENTITY_TYPE, namespace);
        }

        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(String name, EntityType.EntityFactory<E> factory, MobCategory category) {
            return this.registerEntityType(name, factory, category, UnaryOperator.identity());
        }

        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(String name, EntityType.EntityFactory<E> factory, MobCategory category,
            BiFunction<EntityType.EntityFactory<E>, MobCategory, EntityType.Builder<E>> builder) {
            return this.register(name, key -> builder.apply(factory, category).build(ResourceKey.create(Registries.ENTITY_TYPE, key)));
        }

        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(String name, EntityType.EntityFactory<E> factory, MobCategory category,
            UnaryOperator<EntityType.Builder<E>> builder) {
            return this.registerEntityType(name, factory, category, (f, c) -> builder.apply(EntityType.Builder.of(f, c)));
        }
    }
}
