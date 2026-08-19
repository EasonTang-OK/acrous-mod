package com.acrous.registry;

import com.acrous.AcrousMod;
import com.acrous.entity.RevengeMonster;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 注册所有自定义实体
 */
public class ModEntities {

    public static final EntityType<RevengeMonster> REVENGE_MONSTER = register(
            "revenge_monster",
            EntityType.Builder.create(RevengeMonster::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f)
                    .maxTrackingRange(64)
                    .trackingTickInterval(1)
    );

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = Identifier.of(AcrousMod.MOD_ID, name);
        return Registry.register(Registries.ENTITY_TYPE, id, builder.build(id.toString()));
    }

    public static void registerEntities() {
        AcrousMod.LOGGER.info("Registering mod entities for " + AcrousMod.MOD_ID);
    }
}
