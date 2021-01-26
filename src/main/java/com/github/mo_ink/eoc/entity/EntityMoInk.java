package com.github.mo_ink.eoc.entity;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityMoInk extends EntityNPCBase {
    public EntityMoInk(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        ItemStack egg = new ItemStack(Items.SPAWN_EGG, 1);
        ItemStack stick = new ItemStack(Items.STICK, (int) (Math.random() * 5));
        ItemStack diamond = new ItemStack(Items.DIAMOND, (int) (Math.random() * 3));
        ItemMonsterPlacer.applyEntityIdToItemStack(egg, new ResourceLocation("eoc:entity.moink"));
        ItemMonsterPlacer.applyEntityIdToItemStack(stick, new ResourceLocation("eoc:entity.moink"));
        ItemMonsterPlacer.applyEntityIdToItemStack(diamond, new ResourceLocation("eoc:entity.moink"));
        this.entityDropItem(egg, 0.3F);
        this.entityDropItem(stick, 0.3F);
        this.entityDropItem(diamond, 0.3F);
        super.dropFewItems(wasRecentlyHit, lootingModifier);
    }

    @Override
    protected void setEquipmentBasedOnDifficulty() {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
    }
}