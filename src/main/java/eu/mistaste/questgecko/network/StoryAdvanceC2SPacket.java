package eu.mistaste.questgecko.network;

import eu.mistaste.questgecko.story.StoryScene;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StoryAdvanceC2SPacket {
    public StoryAdvanceC2SPacket() {
    }

    public StoryAdvanceC2SPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                StoryScene.advance(player);
            }
        });

        context.setPacketHandled(true);
        return true;
    }
}
