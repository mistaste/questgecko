package eu.mistaste.questgecko.client.renderer;

import eu.mistaste.questgecko.client.model.QuestNPCModel;
import eu.mistaste.questgecko.entity.QuestNPC;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class QuestNPCRenderer extends GeoEntityRenderer<QuestNPC> {
    public QuestNPCRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new QuestNPCModel());
    }
}