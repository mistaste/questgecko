package eu.mistaste.questgecko.client.model;

import eu.mistaste.questgecko.QuestGecko;
import eu.mistaste.questgecko.entity.QuestNPC;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class QuestNPCModel extends GeoModel<QuestNPC> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(QuestGecko.MOD_ID, "geo/test.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(QuestGecko.MOD_ID, "textures/entity/test.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(QuestGecko.MOD_ID, "animations/test.animation.json");

    @Override
    public ResourceLocation getModelResource(QuestNPC object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(QuestNPC object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(QuestNPC object) {
        return ANIMATION;
    }
}
