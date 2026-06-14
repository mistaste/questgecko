package eu.mistaste.questgecko;

import eu.mistaste.questgecko.client.CinematicCamera;
import eu.mistaste.questgecko.client.KeyMappings;
import eu.mistaste.questgecko.client.renderer.QuestNPCRenderer;
import eu.mistaste.questgecko.network.ModMessages;
import eu.mistaste.questgecko.network.StoryAdvanceC2SPacket;
import eu.mistaste.questgecko.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuestGecko.MOD_ID)
public class QuestGecko {
    public static final String MOD_ID = "questgecko";

    public QuestGecko() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITIES.register(modBus);
        modBus.addListener(this::setupNetwork);
    }

    private void setupNetwork(FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            AttributeSupplier attributes = PathfinderMob.createMobAttributes().build();
            event.put(ModEntities.QUEST_NPC.get(), attributes);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.QUEST_NPC.get(), QuestNPCRenderer::new);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KeyMappings.ADVANCE_STORY);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            while (KeyMappings.ADVANCE_STORY.consumeClick()) {
                if (mc.player == null) {
                    continue;
                }

                ModMessages.sendToServer(new StoryAdvanceC2SPacket());
                CinematicCamera.advanceStage();
            }
        }
    }
}
