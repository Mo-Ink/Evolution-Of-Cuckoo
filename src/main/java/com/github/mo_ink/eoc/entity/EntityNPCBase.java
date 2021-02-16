package com.github.mo_ink.eoc.entity;

import com.github.mo_ink.eoc.entity.ai.EntityAIAttackWithBow;
import com.github.mo_ink.eoc.handler.ItemHandler;
import com.github.mo_ink.eoc.items.ItemFunnyApple;
import com.github.mo_ink.eoc.utils.EnumNPCLevel;
import com.github.mo_ink.eoc.utils.RandomCreator;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityNPCBase extends EntityTameable implements IRangedAttackMob {
    private static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.<Boolean>createKey(EntityNPCBase.class, DataSerializers.BOOLEAN);
    private final EntityAIAttackWithBow aiArrowAttack = new EntityAIAttackWithBow(this, 0.12D, 16, 16.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 0.62D, true) {
        public void resetTask() {
            super.resetTask();
            EntityNPCBase.this.setSwingingArms(false);
        }

        public void startExecuting() {
            super.startExecuting();
            EntityNPCBase.this.setSwingingArms(true);
        }
    };
    EntityAINearestAttackableTarget aiNearestAttackableTarget = new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
        public boolean apply(@Nullable Entity entity) {
            return entity instanceof IMob && !entity.isInvisible();
        }
    });
    EntityAINearestAttackableTarget aiNearestAttackableTargetKillHorse = new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
        public boolean apply(@Nullable Entity entity) {
            return (entity instanceof IMob || entity instanceof EntityHorse) && !entity.isInvisible();
        }
    });
    EntityAINearestAttackableTarget aiNearestAttackableTargetWhitoutEnderman = new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
        public boolean apply(@Nullable Entity entity) {
            return entity instanceof IMob && !entity.isInvisible() && !(entity instanceof EntityEnderman);
        }
    });
    EntityAINearestAttackableTarget aiNearestAttackableTargetWhitoutEndermanKillHorse = new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
        public boolean apply(@Nullable Entity entity) {
            return (entity instanceof IMob || entity instanceof EntityHorse) && !entity.isInvisible() && !(entity instanceof EntityEnderman);
        }
    });
    private EnumNPCLevel enumNPCLevel;
    private Item mainHandItem;
    private boolean killHorse;

    public EntityNPCBase(World worldIn, Item itemIn, EnumNPCLevel levelIn, boolean killHorseIn) {
        super(worldIn);
        this.setTamed(false);
        this.setSize(0.6F, 1.8F);
        this.setEnumNPCLevel(levelIn);
        this.changeEntityAttributes();
        this.setItem(itemIn);
        this.setKillHorse(killHorseIn);
        this.setEquipmentBased();
        this.setCombatTask();
    }

    public EntityNPCBase(World worldIn) {
        this(worldIn, null, EnumNPCLevel.C, false);
    }

    public EntityNPCBase(World worldIn, Item itemIn, EnumNPCLevel levelIn) {
        this(worldIn, itemIn, levelIn, false);
    }

    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SWINGING_ARMS, Boolean.valueOf(false));
    }

    public void onLivingUpdate() {
        this.updateArmSwingProgress();
        EnumParticleTypes particleTypes = this.getEnumNPCLevel().getParticleType();
        int particleTimes = this.getEnumNPCLevel().getParticleTimes();
        if (particleTypes != null) {
            this.playEffect(particleTypes, this.posX, this.posY - 1.3F, this.posZ, particleTimes);
        }
        super.onLivingUpdate();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1024.0D);
    }

    protected void changeEntityAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getEnumNPCLevel().getMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.getEnumNPCLevel().getAttackDamage());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getEnumNPCLevel().getMovementSpeed());
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIFollowOwner(this, 0.525D, 10.5F, 5.5F));
        this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 0.34D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityLiving.class, 10.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
        super.initEntityAI();
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) {
            this.applyEnchantments(this, entityIn);
        }
        return flag;
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!itemStack.isEmpty() && itemStack.getItem().equals(ItemHandler.ITEM_FUNNY_APPLE)) {
            ItemFunnyApple itemFunnyApple = (ItemFunnyApple) itemStack.getItem();
            if (this.isTamed()) {
                if (this.getHealth() < this.getMaxHealth()) {
                    if (!player.capabilities.isCreativeMode) {
                        itemStack.shrink(1);
                    }
                    int heal = itemFunnyApple.getHealAmount(itemStack);
                    this.heal(heal);
                    this.playEffect(EnumParticleTypes.HEART, this.posX, this.posY + 0.05F, this.posZ, heal);
                }
            } else {
                if (!player.capabilities.isCreativeMode) {
                    itemStack.shrink(1);
                }
                if (!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    if (!this.world.isRemote) {
                        this.setTamedBy(player);
                        this.setAttackTarget(null);
                    } else {
                        this.playEffect(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + 0.08F, this.posZ, 10);
                    }
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityArrow entityarrow = this.getArrow(distanceFactor);
        if (this.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
            entityarrow = ((net.minecraft.item.ItemBow) this.getHeldItemMainhand().getItem()).customizeArrow(entityarrow);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 6);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(float f) {
        ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

        if (itemstack.getItem() == Items.SPECTRAL_ARROW) {
            EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(this.world, this);
            entityspectralarrow.setEnchantmentEffectsFromEntity(this, f);
            return entityspectralarrow;
        } else {
            EntityArrow entityarrow = new EntityTippedArrow(this.world, this);
            entityarrow.setEnchantmentEffectsFromEntity(this, f);

            if (itemstack.getItem() == Items.TIPPED_ARROW && entityarrow instanceof EntityTippedArrow) {
                ((EntityTippedArrow) entityarrow).setPotionEffect(itemstack);
            }
            return entityarrow;
        }
    }

    public void setCombatTask() {
        if (this.world != null && !this.world.isRemote) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            this.targetTasks.removeTask(this.aiNearestAttackableTarget);
            this.targetTasks.removeTask(this.aiNearestAttackableTargetKillHorse);
            this.targetTasks.removeTask(this.aiNearestAttackableTargetWhitoutEnderman);
            this.targetTasks.removeTask(this.aiNearestAttackableTargetWhitoutEndermanKillHorse);
            ItemStack itemstack = this.getHeldItemMainhand();
            if (itemstack.getItem() instanceof net.minecraft.item.ItemBow) {
                this.tasks.addTask(3, this.aiArrowAttack);
                if (getKillhorse()) {
                    this.targetTasks.addTask(4, aiNearestAttackableTargetWhitoutEndermanKillHorse);
                } else {
                    this.targetTasks.addTask(4, aiNearestAttackableTargetWhitoutEnderman);
                }
            } else {
                this.tasks.addTask(3, this.aiAttackOnCollide);
                if (getKillhorse()) {
                    this.targetTasks.addTask(4, aiNearestAttackableTargetKillHorse);
                } else {
                    this.targetTasks.addTask(4, aiNearestAttackableTarget);
                }
            }
        }
    }

    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        super.setItemStackToSlot(slotIn, stack);

        if (!this.world.isRemote && slotIn == EntityEquipmentSlot.MAINHAND) {
            this.setCombatTask();
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isSwingingArms() {
        return ((Boolean) this.dataManager.get(SWINGING_ARMS)).booleanValue();
    }

    public void setSwingingArms(boolean swingingArms) {
        this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(swingingArms));
    }

    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * difficulty.getClampedAdditionalDifficulty());
        return livingdata;
    }

    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setCombatTask();
    }

    protected void playEffect(EnumParticleTypes particleTypes, Double posX, Double posY, Double posZ, int times) {
        for (int i = 1; i <= times; ++i) {
            double d0 = this.rand.nextGaussian() * 0.013D;
            double d1 = this.rand.nextGaussian() * 0.013D;
            double d2 = this.rand.nextGaussian() * 0.013D;
            this.world.spawnParticle(particleTypes, posX + (double) (this.rand.nextFloat() * this.width * 1.5F) - (double) this.width, posY + 0.5D + (double) (this.rand.nextFloat() * this.height), posZ + (double) (this.rand.nextFloat() * this.width * 1.5F) - (double) this.width, d0, d1, d2);
        }
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return this.experienceValue + RandomCreator.randomTenth(5);
    }

    public EnumNPCLevel getEnumNPCLevel() {
        return this.enumNPCLevel;
    }

    protected void setEnumNPCLevel(EnumNPCLevel enumNPCLevel) {
        this.enumNPCLevel = enumNPCLevel;
        this.experienceValue = enumNPCLevel.getExperienceValue();
    }

    protected void setEquipmentBased() {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(mainHandItem));
    }

    public Item getItem() {
        return mainHandItem;
    }

    protected void setItem(Item itemIn) {
        this.mainHandItem = itemIn;
    }

    protected void setKillHorse(boolean killHorse) {
        this.killHorse = killHorse;
    }

    public boolean getKillhorse() {
        return this.killHorse;
    }

    public void dropNPCItem(ItemStack itemStack) {
        this.entityDropItem(itemStack, 0.3F);
    }
}