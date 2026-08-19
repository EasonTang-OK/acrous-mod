package com.acrous;

import com.acrous.client.render.RevengeMonsterRenderer;
import com.acrous.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import java.awt.*;
import javax.sound.sampled.*;

public class AcrousClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册复仇亡灵渲染器
        EntityRendererRegistry.register(ModEntities.REVENGE_MONSTER, RevengeMonsterRenderer::new);

        // 注册语音包接收
        ClientPlayNetworking.registerGlobalReceiver(AcrousMod.VOICE_PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                String message = payload.message();
                if ("__CREEPY_SOUND__".equals(message)) {
                    playCreepySound();
                } else {
                    playSystemVoice(message);
                }
            });
        });

        AcrousMod.LOGGER.info("Acrous Client initialized!");
    }

    /**
     * 播放系统语音 - 使用Windows SAPI语音合成
     */
    public static void playSystemVoice(String text) {
        try {
            String psScript = String.format(
                    "Add-Type -AssemblyName System.Speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; $synth.Speak('%s');",
                    text.replace("'", "''")
            );
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psScript);
            pb.redirectErrorStream(false);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            AcrousMod.LOGGER.error("Failed to play voice", e);
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                AcrousMod.LOGGER.error("Failed to beep", ex);
            }
        }
    }

    /**
     * 播放诡异音效 - 使用代码合成
     */
    public static void playCreepySound() {
        try {
            new Thread(() -> {
                try {
                    AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();

                    int duration = 3000;
                    byte[] buffer = new byte[44100 * 2];

                    for (int i = 0; i < duration / 100; i++) {
                        for (int j = 0; j < 4410; j++) {
                            double time = (i * 4410 + j) / 44100.0;
                            double freq = 80 + Math.sin(time * 3) * 40 + Math.sin(time * 7) * 20;
                            double amplitude = 0.3 * (0.5 + 0.5 * Math.sin(time * 2));
                            short sample = (short) (Math.sin(2 * Math.PI * freq * time) * amplitude * 32767);
                            buffer[j * 2] = (byte) (sample & 0xFF);
                            buffer[j * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
                        }
                        line.write(buffer, 0, 4410 * 2);
                    }

                    line.drain();
                    line.close();
                } catch (Exception e) {
                    AcrousMod.LOGGER.error("Failed to play creepy sound", e);
                }
            }).start();
        } catch (Exception e) {
            AcrousMod.LOGGER.error("Failed to start creepy sound thread", e);
        }
    }
}
