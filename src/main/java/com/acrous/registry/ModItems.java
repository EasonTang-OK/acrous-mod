package com.acrous.registry;

import com.acrous.AcrousMod;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 注册所有自定义物品
 */
public class ModItems {

    // 复仇之刃 - 强力武器
    public static final Item REVENGE_BLADE = register("revenge_blade",
            new SwordItem(ModToolMaterials.REVENGE,
                    new Item.Settings().fireproof().rarity(Rarity.EPIC).attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.REVENGE, 5, -2.0f))));

    // 暗影盔甲套装
    public static final Item SHADOW_HELMET = register("shadow_helmet",
            new ArmorItem(ModArmorMaterials.SHADOW, ArmorItem.Type.HELMET,
                    new Item.Settings().fireproof().rarity(Rarity.EPIC)));

    public static final Item SHADOW_CHESTPLATE = register("shadow_chestplate",
            new ArmorItem(ModArmorMaterials.SHADOW, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings().fireproof().rarity(Rarity.EPIC)));

    public static final Item SHADOW_LEGGINGS = register("shadow_leggings",
            new ArmorItem(ModArmorMaterials.SHADOW, ArmorItem.Type.LEGGINGS,
                    new Item.Settings().fireproof().rarity(Rarity.EPIC)));

    public static final Item SHADOW_BOOTS = register("shadow_boots",
            new ArmorItem(ModArmorMaterials.SHADOW, ArmorItem.Type.BOOTS,
                    new Item.Settings().fireproof().rarity(Rarity.EPIC)));

    private static Item register(String name, Item item) {
        Identifier id = Identifier.of(AcrousMod.MOD_ID, name);
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void registerItems() {
        ModArmorMaterials.registerMaterials();
        AcrousMod.LOGGER.info("Registering mod items for " + AcrousMod.MOD_ID);
    }
}
