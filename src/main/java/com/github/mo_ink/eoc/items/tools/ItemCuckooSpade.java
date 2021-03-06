package com.github.mo_ink.eoc.items.tools;

import com.github.mo_ink.eoc.EOC;
import com.github.mo_ink.eoc.EOCTab;
import com.github.mo_ink.eoc.utils.ToolMaterials;
import net.minecraft.item.ItemSpade;

public class ItemCuckooSpade extends ItemSpade implements ICuckooTools {
    public ItemCuckooSpade() {
        super(ToolMaterials.CUCKOO);
        String name = "cuckoo_shovel";
        this.setUnlocalizedName(EOC.MODID + "." + name);
        this.setRegistryName(name);
        this.setCreativeTab(EOCTab.EOC_TAB);
        this.setMaxStackSize(1);
    }
}
