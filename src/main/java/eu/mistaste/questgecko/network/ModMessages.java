package eu.mistaste.questgecko.network;

import eu.mistaste.questgecko.QuestGecko;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel INSTANCE;
    private static int nextId;

    private static int id() {
        return nextId++;
    }

    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(QuestGecko.MOD_ID, "main"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        INSTANCE.messageBuilder(StoryAdvanceC2SPacket.class, id())
                .decoder(StoryAdvanceC2SPacket::new)
                .encoder(StoryAdvanceC2SPacket::toBytes)
                .consumerMainThread(StoryAdvanceC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
