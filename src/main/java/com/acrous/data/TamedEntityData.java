package com.acrous.data;

import com.acrous.AcrousMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 管理被苹果驯服的实体数据
 */
public class TamedEntityData {
    // 存储被驯服实体的主人UUID: 实体UUID -> 玩家UUID
    private static final Map<UUID, UUID> TAMED_ENTITIES = new HashMap<>();

    // 存储实体类型: 实体UUID -> 实体类型ID
    private static final Map<UUID, String> ENTITY_TYPES = new HashMap<>();

    /**
     * 玩家右键实体时触发 - 用苹果驯服
     */
    public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (world.isClient) return ActionResult.PASS;
        if (!(entity instanceof LivingEntity)) return ActionResult.PASS;

        // 检查是否拿着苹果
        if (player.getStackInHand(hand).getItem() == Items.APPLE) {
            UUID entityId = entity.getUuid();

            // 如果已经被驯服
            if (TAMED_ENTITIES.containsKey(entityId)) {
                UUID ownerId = TAMED_ENTITIES.get(entityId);
                if (ownerId.equals(player.getUuid())) {
                    player.sendMessage(Text.literal("这个生物已经是你的了！"), true);
                } else {
                    player.sendMessage(Text.literal("这个生物已经被别人驯服了！"), true);
                }
                return ActionResult.SUCCESS;
            }

            // 驯服成功
            TAMED_ENTITIES.put(entityId, player.getUuid());
            ENTITY_TYPES.put(entityId, entity.getType().getUntranslatedName());

            // 消耗一个苹果
            if (!player.getAbilities().creativeMode) {
                player.getStackInHand(hand).decrement(1);
            }

            // 保存到实体NBT
            NbtCompound nbt = new NbtCompound();
            entity.writeNbt(nbt);
            nbt.putUuid("AcrousOwner", player.getUuid());
            nbt.putBoolean("AcrousTamed", true);
            entity.readNbt(nbt);

            player.sendMessage(Text.literal("你成功驯服了这个生物！但要小心..."), true);
            AcrousMod.LOGGER.info("Entity {} tamed by player {}", entityId, player.getUuid());

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    /**
     * 实体死亡时触发
     */
    public static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
        World world = entity.getWorld();
        if (world.isClient) return;

        UUID entityId = entity.getUuid();

        // 检查是否是被驯服的实体
        if (TAMED_ENTITIES.containsKey(entityId)) {
            UUID ownerId = TAMED_ENTITIES.get(entityId);
            String entityType = ENTITY_TYPES.getOrDefault(entityId, entity.getType().getUntranslatedName());

            // 检查是否是被主人杀死的
            if (damageSource.getAttacker() instanceof PlayerEntity killer) {
                if (killer.getUuid().equals(ownerId)) {
                    // 主人杀死了驯服的生物
                    AcrousMod.LOGGER.info("Player {} killed their tamed entity {}", ownerId, entityId);

                    // 立即让实体消失
                    entity.discard();

                    // 播放语音
                    if (killer instanceof ServerPlayerEntity serverPlayer) {
                        try {
                            ServerPlayNetworking.send(serverPlayer, new AcrousMod.VoicePacket("i will kill you"));
                        } catch (Exception e) {
                            AcrousMod.LOGGER.error("Failed to send voice packet", e);
                        }
                    }

                    // 记录到复仇管理器
                    long deathTime = world.getTimeOfDay();
                    RevengeManager.addRevengeTarget(ownerId, entityType, deathTime);

                    // 移除驯服记录
                    TAMED_ENTITIES.remove(entityId);
                    ENTITY_TYPES.remove(entityId);
                }
            }
        }
    }

    /**
     * 检查实体是否被指定玩家驯服
     */
    public static boolean isTamedBy(UUID entityId, UUID playerId) {
        return playerId.equals(TAMED_ENTITIES.get(entityId));
    }

    /**
     * 检查实体是否被驯服
     */
    public static boolean isTamed(UUID entityId) {
        return TAMED_ENTITIES.containsKey(entityId);
    }

    /**
     * 获取驯服者
     */
    public static UUID getOwner(UUID entityId) {
        return TAMED_ENTITIES.get(entityId);
    }
}
