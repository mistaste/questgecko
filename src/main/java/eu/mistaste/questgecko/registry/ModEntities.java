package eu.mistaste.questgecko.registry;

import eu.mistaste.questgecko.QuestGecko;
import eu.mistaste.questgecko.entity.QuestNPC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    private static final String QUEST_NPC_ID = "quest_npc";
    private static final float NPC_WIDTH = 0.6F;
    private static final float NPC_HEIGHT = 1.95F;

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, QuestGecko.MOD_ID);

    public static final RegistryObject<EntityType<QuestNPC>> QUEST_NPC = ENTITIES.register(QUEST_NPC_ID,
            () -> EntityType.Builder.of(QuestNPC::new, MobCategory.CREATURE)
                    .sized(NPC_WIDTH, NPC_HEIGHT)
                    .build(QUEST_NPC_ID));
}
