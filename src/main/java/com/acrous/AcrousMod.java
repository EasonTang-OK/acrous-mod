package com.acrous;

import com.acrous.data.RevengeManager;
import com.acrous.data.TamedEntityData;
import com.acrous.registry.ModEntities;
import com.acrous.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcrousMod implements ModInitializer {
    public static final String MOD_ID = "acrous";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CustomPayload.Id<VoicePacket> VOICE_PACKET_ID =
            new CustomPayload.Id<>(Identifier.of(MOD_ID, "voice"));

    @Override
    public void onInitialize() {
        LOGGER.info("Acrous Mod initialized!");

        // 注册物品（静态初始化时已注册，这里确保类加载）
        ModItems.registerItems();

        // 注册实体属性
        FabricDefaultAttributeRegistry.register(ModEntities.REVENGE_MONSTER, com.acrous.entity.RevengeMonster.createRevengeAttributes());
        ModEntities.registerEntities();

        // 注册网络包
        PayloadTypeRegistry.playS2C().register(VOICE_PACKET_ID, VoicePacket.CODEC);

        // 注册事件
        UseEntityCallback.EVENT.register(TamedEntityData::onUseEntity);
        ServerLivingEntityEvents.AFTER_DEATH.register(TamedEntityData::onEntityDeath);
        ServerTickEvents.END_SERVER_TICK.register(RevengeManager::onServerTick);

        // 注册服务器端数据
        RevengeManager.init();

        LOGGER.info("Acrous Mod loading complete!");
    }

    // 语音播放网络包
    public record VoicePacket(String message) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, VoicePacket> CODEC = PacketCodec.of(
                (value, buf) -> buf.writeString(value.message),
                buf -> new VoicePacket(buf.readString())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return VOICE_PACKET_ID;
        }
    }
}
