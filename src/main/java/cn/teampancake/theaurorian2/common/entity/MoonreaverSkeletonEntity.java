package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModEntities;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/** Vanilla skeleton combat and equipment persistence, with variant-specific initial loadouts. */
public final class MoonreaverSkeletonEntity extends AbstractSkeleton implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final double CAPTAIN_GREATSWORD_REACH = 1.75D;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public MoonreaverSkeletonEntity(EntityType<? extends MoonreaverSkeletonEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createCaptainAttributes() {
        return createAttributes().add(Attributes.MAX_HEALTH, 100.0D);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        // Preserve each variant's equipment and the exposed skull, including on holidays.
        this.setCanPickUpLoot(false);
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        return result;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // The variants start with their own weapon, without vanilla random armor hiding this model.
        if (this.getType() == ModEntities.MOONREAVER_SKELETON_CAPTAIN.get()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STARFORGED_KNIGHT_GREATSWORD.get()));
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.STARFORGED_KNIGHT_CHESTPLATE.get()));
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.STARFORGED_KNIGHT_LEGGINGS.get()));
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.STARFORGED_KNIGHT_BOOTS.get()));
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(
                    this.getType() == ModEntities.MOONREAVER_SKELETON_SWORDSMAN.get()
                            ? ModItems.AURORIAN_STONE_SWORD.get() : ModLegacyItems.SILENT_WOOD_BOW.get()));
        }
        // Vanilla equipment drops randomize damage and apply Looting; do not duplicate them in the loot table.
        this.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
    }

    @Override
    public boolean canUseNonMeleeWeapon(ItemStack stack) {
        return stack.is(ModLegacyItems.SILENT_WOOD_BOW.get()) || super.canUseNonMeleeWeapon(stack);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        if (this.getType() == ModEntities.MOONREAVER_SKELETON_CAPTAIN.get()
                && this.getMainHandItem().is(ModItems.STARFORGED_KNIGHT_GREATSWORD.get())) {
            // Expand from the body like vanilla, matching the greatsword instead of the skeleton's short reach.
            // The vanilla melee goal still controls line of sight, cooldown and the synchronized swing.
            return this.getAttackBoundingBox(CAPTAIN_GREATSWORD_REACH).intersects(target.getHitbox());
        }
        return super.isWithinMeleeAttackRange(target);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.SKELETON_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.SKELETON_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.SKELETON_DEATH; }

    @Override
    protected SoundEvent getStepSound() { return SoundEvents.SKELETON_STEP; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<MoonreaverSkeletonEntity>("idle", 0,
                state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.animationCache; }
}
