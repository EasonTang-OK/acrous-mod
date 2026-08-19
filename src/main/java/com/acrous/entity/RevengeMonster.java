package com.acrous.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

/**
 * 复仇亡灵 - 被玩家杀死的驯服生物复活后的形态
 * 继承ZombieEntity以复用僵尸模型和AI，使用自定义贴图
 */
public class RevengeMonster extends ZombieEntity {

    public RevengeMonster(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.setPersistent();
    }

    /**
     * 创建复仇亡灵的属性 - 3000血量，高攻击，高速度
     */
    public static DefaultAttributeContainer.Builder createRevengeAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 3000.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0);
    }

    @Override
    public void tick() {
        super.tick();
        // 免疫阳光燃烧
        this.setFireTicks(0);
        // 确保不会被清除
        this.setPersistent();
    }

    public boolean canBurn() {
        return false;
    }

    public boolean isUndead() {
        return true;
    }
}
