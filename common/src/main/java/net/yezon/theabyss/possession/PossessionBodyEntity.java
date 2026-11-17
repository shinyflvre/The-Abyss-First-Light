package net.yezon.theabyss.possession;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PossessionBodyEntity extends LivingEntity {
    public static final Vec3 DEFAULT_VEHICLE_ATTACHMENT = new Vec3(0.0, 0.6, 0.0);
    private static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F)
        .withEyeHeight(1.62F)
        .withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT));
    private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder()
        .put(Pose.STANDING, STANDING_DIMENSIONS)
        .put(Pose.SLEEPING, SLEEPING_DIMENSIONS)
        .put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
        .put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
        .put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
        .put(
            Pose.CROUCHING,
            EntityDimensions.scalable(0.6F, 1.5F)
                .withEyeHeight(1.27F)
                .withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT))
        )
        .put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(1.62F))
        .build();
    private static final EntityDataAccessor<Byte> DATA_MAIN_HAND = SynchedEntityData.defineId(PossessionBodyEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_MODEL_CUSTOMISATION = SynchedEntityData.defineId(PossessionBodyEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<CompoundTag> DATA_PROFILE = SynchedEntityData.defineId(PossessionBodyEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> DATA_IMMOVABLE = SynchedEntityData.defineId(PossessionBodyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final byte ALL_LAYERS = (byte)Arrays.stream(PlayerModelPart.values()).mapToInt(PlayerModelPart::getMask).reduce(0, (a, b) -> a | b);
    public static final ResolvableProfile DEFAULT_PROFILE = new ResolvableProfile(Optional.empty(), Optional.empty(), new PropertyMap());

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    private CompoundTag profileTag;
    private ResolvableProfile profile = DEFAULT_PROFILE;
    private Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    private double xCloak;
    private double yCloak;
    private double zCloak;
    private double xCloakO;
    private double yCloakO;
    private double zCloakO;

    public PossessionBodyEntity(EntityType<? extends PossessionBodyEntity> type, Level level) {
        super(type, level);
        this.entityData.set(DATA_MODEL_CUSTOMISATION, ALL_LAYERS);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MAIN_HAND, (byte)1);
        builder.define(DATA_MODEL_CUSTOMISATION, (byte)0);
        builder.define(DATA_PROFILE, new CompoundTag());
        builder.define(DATA_IMMOVABLE, false);
    }

    public ResolvableProfile getProfile() {
        CompoundTag tag = this.entityData.get(DATA_PROFILE);
        if (tag != this.profileTag) {
            this.profileTag = tag;
            this.profile = decodeProfile(tag);
        }
        return this.profile;
    }

    public void setProfile(ResolvableProfile profile) {
        CompoundTag tag = encodeProfile(profile);
        this.entityData.set(DATA_PROFILE, tag);
        this.profileTag = this.entityData.get(DATA_PROFILE);
        this.profile = decodeProfile(this.profileTag);
    }

    private static CompoundTag encodeProfile(ResolvableProfile profile) {
        Optional<CompoundTag> full = ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).result()
            .filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast);
        if (full.isPresent()) {
            return full.get();
        }
        return ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, new ResolvableProfile(Optional.empty(), profile.id(), profile.properties())).result()
            .filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).orElseGet(CompoundTag::new);
    }

    private static ResolvableProfile decodeProfile(CompoundTag tag) {
        if (tag.isEmpty()) {
            return DEFAULT_PROFILE;
        }
        return ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(DEFAULT_PROFILE);
    }

    public boolean getImmovable() {
        return this.entityData.get(DATA_IMMOVABLE);
    }

    public void setImmovable(boolean immovable) {
        this.entityData.set(DATA_IMMOVABLE, immovable);
    }

    public boolean isModelPartShown(PlayerModelPart part) {
        return (this.entityData.get(DATA_MODEL_CUSTOMISATION) & part.getMask()) == part.getMask();
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.entityData.get(DATA_MAIN_HAND) == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public void setMainArm(HumanoidArm arm) {
        this.entityData.set(DATA_MAIN_HAND, (byte)(arm == HumanoidArm.LEFT ? 0 : 1));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return POSES.getOrDefault(pose, STANDING_DIMENSIONS);
    }

    @Override
    protected boolean isImmobile() {
        return this.getImmovable() || super.isImmobile();
    }

    @Override
    public boolean isEffectiveAi() {
        return !this.getImmovable() && super.isEffectiveAi();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.deltaMovementOnPreviousTick = this.getDeltaMovement();
            this.moveCloak();
        }
    }

    private void moveCloak() {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double x = this.getX() - this.xCloak;
        double y = this.getY() - this.yCloak;
        double z = this.getZ() - this.zCloak;
        if (!(x > 10.0) && !(x < -10.0)) {
            this.xCloak += x * 0.25;
        } else {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }
        if (!(y > 10.0) && !(y < -10.0)) {
            this.yCloak += y * 0.25;
        } else {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }
        if (!(z > 10.0) && !(z < -10.0)) {
            this.zCloak += z * 0.25;
        } else {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }
    }

    public double getCloakX(float partialTick) {
        return Mth.lerp(partialTick, this.xCloakO, this.xCloak);
    }

    public double getCloakY(float partialTick) {
        return Mth.lerp(partialTick, this.yCloakO, this.yCloak);
    }

    public double getCloakZ(float partialTick) {
        return Mth.lerp(partialTick, this.zCloakO, this.zCloak);
    }

    public Vec3 getDeltaMovementLerped(float partialTick) {
        return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), partialTick);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> this.handItems.get(slot.getIndex());
            case HUMANOID_ARMOR -> this.armorItems.get(slot.getIndex());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.BODY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND -> this.onEquipItem(slot, this.handItems.set(slot.getIndex(), stack), stack);
            case HUMANOID_ARMOR -> this.onEquipItem(slot, this.armorItems.set(slot.getIndex(), stack), stack);
            default -> {
            }
        }
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        return this.getItemBySlot(this.getEquipmentSlotForItem(stack)).isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag armor = new ListTag();
        for (ItemStack stack : this.armorItems) {
            armor.add(stack.saveOptional(this.registryAccess()));
        }
        tag.put("ArmorItems", armor);
        ListTag hands = new ListTag();
        for (ItemStack stack : this.handItems) {
            hands.add(stack.saveOptional(this.registryAccess()));
        }
        tag.put("HandItems", hands);
        tag.put("profile", this.entityData.get(DATA_PROFILE).copy());
        ListTag hidden = new ListTag();
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (!this.isModelPartShown(part)) {
                hidden.add(StringTag.valueOf(part.getId()));
            }
        }
        tag.put("hidden_layers", hidden);
        tag.putString("main_hand", this.getMainArm().getSerializedName());
        tag.putBoolean("immovable", this.getImmovable());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ArmorItems", Tag.TAG_LIST)) {
            ListTag armor = tag.getList("ArmorItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < this.armorItems.size(); i++) {
                this.armorItems.set(i, ItemStack.parseOptional(this.registryAccess(), armor.getCompound(i)));
            }
        }
        if (tag.contains("HandItems", Tag.TAG_LIST)) {
            ListTag hands = tag.getList("HandItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < this.handItems.size(); i++) {
                this.handItems.set(i, ItemStack.parseOptional(this.registryAccess(), hands.getCompound(i)));
            }
        }
        if (tag.contains("profile", Tag.TAG_COMPOUND)) {
            this.setProfile(decodeProfile(tag.getCompound("profile")));
        }
        byte layers = ALL_LAYERS;
        if (tag.contains("hidden_layers", Tag.TAG_LIST)) {
            ListTag hidden = tag.getList("hidden_layers", Tag.TAG_STRING);
            for (int i = 0; i < hidden.size(); i++) {
                String id = hidden.getString(i);
                for (PlayerModelPart part : PlayerModelPart.values()) {
                    if (part.getId().equals(id)) {
                        layers = (byte)(layers & ~part.getMask());
                    }
                }
            }
        }
        this.entityData.set(DATA_MODEL_CUSTOMISATION, layers);
        this.setMainArm(HumanoidArm.LEFT.getSerializedName().equals(tag.getString("main_hand")) ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        this.setImmovable(tag.getBoolean("immovable"));
    }
}
