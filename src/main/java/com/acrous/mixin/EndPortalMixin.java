package com.acrous.mixin;

import com.acrous.data.RevengeManager;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 末地传送门混合器 - 检测玩家击败末影龙后进入传送门
 */
@Mixin(EndPortalBlock.class)
public class EndPortalMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"))
    private void onEntityCollision(net.minecraft.block.BlockState state, World world, net.minecraft.util.math.BlockPos pos, Entity entity, CallbackInfo ci) {
        if (world.isClient) return;

        if (entity instanceof PlayerEntity player) {
            // 检查是否在末地（击败末影龙后的传送门）
            if (world.getRegistryKey() == World.END) {
                // 玩家进入末地传送门，标记为已击败末影龙
                RevengeManager.onDragonDefeated(player.getUuid());
            }
        }
    }
}
