package com.acrous.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/**
 * 复仇之刃工具材质 - 比下界合金更强
 */
public class ModToolMaterials {

    public static final ToolMaterial REVENGE = new ToolMaterial() {
        public int getDurability() { return 2500; }
        public float getMiningSpeedMultiplier() { return 10.0f; }
        public float getAttackDamage() { return 9.0f; }
        public int getMiningLevel() { return 5; }
        public int getEnchantability() { return 22; }
        public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.DIAMOND); }
        public TagKey<Block> getInverseTag() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    };
}
