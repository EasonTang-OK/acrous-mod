package com.acrous.registry;

import com.acrous.AcrousMod;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * 暗影盔甲材质 - 比钻石更强
 */
public class ModArmorMaterials {

    public static final RegistryEntry<ArmorMaterial> SHADOW = register(
            "shadow",
            ImmutableMap.of(
                    ArmorItem.Type.HELMET, 4,
                    ArmorItem.Type.CHESTPLATE, 10,
                    ArmorItem.Type.LEGGINGS, 7,
                    ArmorItem.Type.BOOTS, 4
            ),
            20,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.ofItems(Items.DIAMOND),
            3.5f,
            0.2f
    );

    private static RegistryEntry<ArmorMaterial> register(String name,
                                                         Map<ArmorItem.Type, Integer> defense,
                                                         int enchantability,
                                                         RegistryEntry<SoundEvent> equipSound,
                                                         java.util.function.Supplier<Ingredient> repairIngredient,
                                                         float toughness,
                                                         float knockbackResistance) {
        Identifier id = Identifier.of(AcrousMod.MOD_ID, name);
        ArmorMaterial material = new ArmorMaterial(
                defense, enchantability, equipSound, repairIngredient,
                java.util.List.of(new ArmorMaterial.Layer(id)),
                toughness, knockbackResistance
        );
        return Registry.registerReference(Registries.ARMOR_MATERIAL, id, material);
    }

    public static void registerMaterials() {
        AcrousMod.LOGGER.info("Registering armor materials");
    }
}
