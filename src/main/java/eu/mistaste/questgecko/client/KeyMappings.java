package eu.mistaste.questgecko.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {
    private static final String CATEGORY = "key.category.questgecko.story";
    private static final String ADVANCE_STORY_KEY = "key.questgecko.advance_story";

    public static final KeyMapping ADVANCE_STORY = new KeyMapping(
            ADVANCE_STORY_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    private KeyMappings() {
    }
}
