package com.github.mo_ink.eoc.handler;

import com.github.mo_ink.eoc.items.tools.ICuckooTools;
import com.github.mo_ink.eoc.potion.PotionFunny;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

@Mod.EventBusSubscriber
public class PotionHandler {
    public static final Potion POTION_FUNNY = new PotionFunny();

    @SubscribeEvent
    public static void onPotionRegistry(RegistryEvent.Register<Potion> event) {
        IForgeRegistry<Potion> registry = event.getRegistry();
        registry.register(POTION_FUNNY);
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.isPotionActive(POTION_FUNNY)) {
            Item heldItem = player.getHeldItemMainhand().getItem();
            int amplifier = player.getActivePotionEffect(POTION_FUNNY).getAmplifier() + 1;

            double damage = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            if (heldItem instanceof ItemTool) {
                damage = (Item.ToolMaterial.valueOf(((ItemTool) heldItem).getToolMaterialName())).getAttackDamage() + 2.8;
            } else if (heldItem instanceof ItemSword) {
                damage = ((ItemSword) heldItem).getAttackDamage() + 2.8;
            } else if (heldItem instanceof ItemHoe) {
                damage = (Item.ToolMaterial.valueOf(((ItemHoe) heldItem).getMaterialName())).getAttackDamage() + 2.8;
            }

            double amount = damage / 2.0 * amplifier;
            if (heldItem instanceof ICuckooTools) { //如果是不咕工具则减少扣血
                amount = amount / (new Random().nextInt(10) / 9.0 + 3);
            }
            player.attackEntityFrom(DamageSourceHandler.FUNNY_DIED, (float) amount);
        }
    }
}
