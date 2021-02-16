package com.github.mo_ink.eoc.utils;

import com.github.mo_ink.eoc.entity.EntityNPCBase;
import com.github.mo_ink.eoc.entity.ai.EntityAIAttackWithBow;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;

import javax.annotation.Nullable;

public class AttackTargetAI {
    public EntityAINearestAttackableTarget aiAttackAllLiving;
    public EntityAINearestAttackableTarget aiAttackMob;
    public EntityAINearestAttackableTarget aiAttackMobAndHorse;
    public EntityAINearestAttackableTarget aiAttackMobWhitoutEnderman;
    public EntityAINearestAttackableTarget aiAttackMobAndHorseWhitoutEnderman;
    public EntityAIAttackWithBow aiArrowAttack;
    public EntityAIAttackMelee aiAttackOnCollide;

    public AttackTargetAI(EntityNPCBase npcIn) {
        aiAttackAllLiving = new EntityAINearestAttackableTarget(npcIn, EntityLivingBase.class, 10, true, false, new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                return true;
            }
        });
        aiAttackMob = new EntityAINearestAttackableTarget(npcIn, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                return entity instanceof IMob && !entity.isInvisible();
            }
        });
        aiAttackMobAndHorse = new EntityAINearestAttackableTarget(npcIn, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                return (entity instanceof IMob || entity instanceof EntityHorse) && !entity.isInvisible();
            }
        });
        aiAttackMobWhitoutEnderman = new EntityAINearestAttackableTarget(npcIn, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                return entity instanceof IMob && !entity.isInvisible() && !(entity instanceof EntityEnderman);
            }
        });
        aiAttackMobAndHorseWhitoutEnderman = new EntityAINearestAttackableTarget(npcIn, EntityLiving.class, 10, true, false, new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                return (entity instanceof IMob || entity instanceof EntityHorse) && !entity.isInvisible() && !(entity instanceof EntityEnderman);
            }
        });
        aiArrowAttack = new EntityAIAttackWithBow(npcIn, 0.12D, 16, 16.0F);
        aiAttackOnCollide = new EntityAIAttackMelee(npcIn, 0.62D, true) {
            public void resetTask() {
                super.resetTask();
                npcIn.setSwingingArms(false);
            }

            public void startExecuting() {
                super.startExecuting();
                npcIn.setSwingingArms(true);
            }
        };
    }
}
