package com.acrous.client.render;

import com.acrous.AcrousMod;
import com.acrous.entity.RevengeMonster;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * 复仇亡灵渲染器 - 复用僵尸模型，使用自定义贴图
 */
public class RevengeMonsterRenderer extends ZombieEntityRenderer {

    private static final Identifier TEXTURE = Identifier.of(AcrousMod.MOD_ID, "textures/entity/revenge_monster.png");

    public RevengeMonsterRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public Identifier getTexture(RevengeMonster entity) {
        return TEXTURE;
    }
}
