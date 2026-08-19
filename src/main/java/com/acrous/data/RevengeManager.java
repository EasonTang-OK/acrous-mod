package com.acrous.data;

import com.acrous.AcrousMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.registry.Registries;

import java.util.*;

/**
 * 复仇管理器 - 管理被杀死的驯服生物的复仇逻辑
 */
public class RevengeManager {
    // 玩家UUID -> 复仇目标列表
    private static final Map<UUID, List<RevengeTarget>> REVENGE_TARGETS = new HashMap<>();

    // 玩家UUID -> 已生成的复仇实体UUID列表
    private static final Map<UUID, List<UUID>> ACTIVE_REVENGE_ENTITIES = new HashMap<>();

    // 玩家UUID -> 是否已击败末影龙
    private static final Set<UUID> DEFEATED_DRAGON = new HashSet<>();

    // 一天的tick数 (24000)
    private static final long DAY_TICKS = 24000;
    // 正午时间
    private static final long NOON_TICKS = 6000;

    public static void init() {
        AcrousMod.LOGGER.info("Revenge Manager initialized");
    }

    /**
     * 添加复仇目标
     */
    public static void addRevengeTarget(UUID playerId, String entityType, long deathTime) {
        RevengeTarget target = new RevengeTarget(entityType, deathTime);
        REVENGE_TARGETS.computeIfAbsent(playerId, k -> new ArrayList<>()).add(target);
        AcrousMod.LOGGER.info("Added revenge target for player {}: {}, deathTime: {}", playerId, entityType, deathTime);
    }

    /**
     * 服务器tick事件
     */
    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerId = player.getUuid();

            // 如果玩家已击败末影龙，跳过
            if (DEFEATED_DRAGON.contains(playerId)) {
                continue;
            }

            ServerWorld world = player.getServerWorld();
            long currentTime = world.getTimeOfDay();

            // 检查是否有需要生成的复仇目标
            checkAndSpawnRevenge(player, world, currentTime);

            // 检查活跃的复仇实体
            checkActiveRevengeEntities(player, world);

            // 检查创造模式
            checkCreativeMode(player, server);
        }
    }

    /**
     * 检查并生成复仇实体
     */
    private static void checkAndSpawnRevenge(ServerPlayerEntity player, ServerWorld world, long currentTime) {
        UUID playerId = player.getUuid();
        List<RevengeTarget> targets = REVENGE_TARGETS.get(playerId);
        if (targets == null || targets.isEmpty()) return;

        Iterator<RevengeTarget> iterator = targets.iterator();
        while (iterator.hasNext()) {
            RevengeTarget target = iterator.next();

            // 计算第二天正午的时间
            long nextNoon = getNextNoon(target.deathTime);

            if (currentTime >= nextNoon && currentTime < nextNoon + 100) {
                // 生成复仇实体
                spawnRevengeEntity(player, world, target.entityType);
                iterator.remove();
            }
        }
    }

    /**
     * 获取下一个正午的时间
     */
    private static long getNextNoon(long deathTime) {
        long daysSinceDeath = (deathTime / DAY_TICKS) + 1;
        return daysSinceDeath * DAY_TICKS + NOON_TICKS;
    }

    /**
     * 生成复仇实体 - 使用自定义复仇亡灵
     */
    private static void spawnRevengeEntity(ServerPlayerEntity player, ServerWorld world, String entityTypeId) {
        try {
            // 在玩家附近生成
            Vec3d playerPos = player.getPos();
            double angle = Math.random() * Math.PI * 2;
            double distance = 8 + Math.random() * 4;
            double x = playerPos.x + Math.cos(angle) * distance;
            double z = playerPos.z + Math.sin(angle) * distance;
            double y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);

            // 使用自定义复仇亡灵实体
            com.acrous.entity.RevengeMonster revengeMonster = com.acrous.registry.ModEntities.REVENGE_MONSTER.create(world);
            if (revengeMonster == null) {
                AcrousMod.LOGGER.error("Failed to create revenge monster");
                return;
            }

            revengeMonster.setPosition(x, y + 1, z);
            revengeMonster.setHealth(3000);
            revengeMonster.setTarget(player);
            revengeMonster.setPersistent();

            // 装备复仇之刃
            revengeMonster.equipStack(EquipmentSlot.MAINHAND, new ItemStack(com.acrous.registry.ModItems.REVENGE_BLADE));

            // 标记NBT
            NbtCompound nbt = new NbtCompound();
            revengeMonster.writeNbt(nbt);
            nbt.putBoolean("AcrousRevenge", true);
            nbt.putUuid("AcrousTarget", player.getUuid());
            revengeMonster.readNbt(nbt);

            world.spawnEntity(revengeMonster);

            // 记录活跃的复仇实体
            ACTIVE_REVENGE_ENTITIES.computeIfAbsent(player.getUuid(), k -> new ArrayList<>()).add(revengeMonster.getUuid());

            // 播放诡异音效
            try {
                ServerPlayNetworking.send(player, new AcrousMod.VoicePacket("__CREEPY_SOUND__"));
            } catch (Exception e) {
                AcrousMod.LOGGER.error("Failed to send creepy sound packet", e);
            }

            player.sendMessage(Text.literal("§c你听到了诡异的声音...你杀死的生物回来了！"), true);
            AcrousMod.LOGGER.info("Spawned revenge monster for player {} (original type: {})", player.getUuid(), entityTypeId);

        } catch (Exception e) {
            AcrousMod.LOGGER.error("Failed to spawn revenge entity", e);
        }
    }

    /**
     * 检查活跃的复仇实体
     */
    private static void checkActiveRevengeEntities(ServerPlayerEntity player, ServerWorld world) {
        UUID playerId = player.getUuid();
        List<UUID> entityIds = ACTIVE_REVENGE_ENTITIES.get(playerId);
        if (entityIds == null || entityIds.isEmpty()) return;

        // 只能在主世界或下界追逐
        boolean canChase = world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;

        Iterator<UUID> iterator = entityIds.iterator();
        while (iterator.hasNext()) {
            UUID entityId = iterator.next();
            Entity entity = world.getEntity(entityId);

            if (entity == null || !entity.isAlive()) {
                iterator.remove();
                continue;
            }

            if (entity instanceof LivingEntity livingEntity) {
                // 不能到虚空上面去 (Y < 0)
                if (entity.getY() < 0) {
                    // 传送到玩家附近的安全位置
                    Vec3d playerPos = player.getPos();
                    entity.setPosition(playerPos.x, playerPos.y + 2, playerPos.z);
                }

                // 如果不能追逐（末地等），传送到玩家所在的可追逐维度
                if (!canChase) {
                    // 实体留在当前位置，不追逐
                    continue;
                }

                // 持续设置目标为玩家
                if (livingEntity instanceof MobEntity mobEntity) {
                    if (mobEntity.getTarget() == null || !mobEntity.getTarget().isAlive()) {
                        mobEntity.setTarget(player);
                    }
                }
            }
        }
    }

    /**
     * 检查创造模式 - 如果玩家在被追逐时开创造，移除创造
     */
    private static void checkCreativeMode(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();

        // 检查是否有活跃的复仇实体
        List<UUID> activeEntities = ACTIVE_REVENGE_ENTITIES.get(playerId);
        boolean hasActiveRevenge = activeEntities != null && !activeEntities.isEmpty();

        // 检查是否有待生成的复仇目标
        List<RevengeTarget> pendingTargets = REVENGE_TARGETS.get(playerId);
        boolean hasPendingRevenge = pendingTargets != null && !pendingTargets.isEmpty();

        if (!hasActiveRevenge && !hasPendingRevenge) return;

        // 检查是否是局域网世界 - 局域网世界不移除创造
        if (server.isRemote()) {
            // 是局域网世界，跳过
            return;
        }

        // 如果玩家是创造模式，改为生存模式
        if (player.interactionManager.getGameMode() == GameMode.CREATIVE) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.sendMessage(Text.literal("§c你无法在被追逐时使用创造模式！"), true);
            AcrousMod.LOGGER.info("Removed creative mode from player {}", playerId);
        }
    }

    /**
     * 玩家击败末影龙后调用
     */
    public static void onDragonDefeated(UUID playerId) {
        DEFEATED_DRAGON.add(playerId);

        // 清除所有活跃的复仇实体
        List<UUID> entityIds = ACTIVE_REVENGE_ENTITIES.remove(playerId);
        if (entityIds != null) {
            // 实体将在下次tick时被清理
        }

        // 清除待生成的复仇目标
        REVENGE_TARGETS.remove(playerId);

        AcrousMod.LOGGER.info("Player {} defeated the dragon, revenge ended", playerId);
    }

    /**
     * 检查玩家是否已击败末影龙
     */
    public static boolean hasDefeatedDragon(UUID playerId) {
        return DEFEATED_DRAGON.contains(playerId);
    }

    /**
     * 复仇目标数据类
     */
    private static class RevengeTarget {
        String entityType;
        long deathTime;

        RevengeTarget(String entityType, long deathTime) {
            this.entityType = entityType;
            this.deathTime = deathTime;
        }
    }
}
