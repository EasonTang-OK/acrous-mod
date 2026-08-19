package com.acrous.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LivingEntity混合器 - 处理复仇实体的特殊行为
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        World world = entity.getWorld();

        if (world.isClient) return;

        // 检查是否是复仇实体（通过NBT标记）
        NbtCompound nbt = new NbtCompound();
        entity.writeNbt(nbt);

        if (nbt.getBoolean("AcrousRevenge")) {
            // 复仇实体不能到虚空下面去
            if (entity.getY() < -10) {
                entity.discard();
            }
            // 免疫火焰
            entity.setFireTicks(0);
        }
    }
}
