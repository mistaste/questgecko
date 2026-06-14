package eu.mistaste.questgecko.mixin;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureManager.class)
public abstract class TextureManagerBridgeMixin {
    @Unique
    public AbstractTexture m_118506_(ResourceLocation resourceLocation) {
        return ((TextureManager) (Object) this).getTexture(resourceLocation);
    }
}

