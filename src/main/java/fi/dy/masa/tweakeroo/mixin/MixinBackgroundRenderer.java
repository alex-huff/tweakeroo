package fi.dy.masa.tweakeroo.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;

@Mixin(BackgroundRenderer.class)
public abstract class MixinBackgroundRenderer
{
    @Unique
    private static boolean wasLava;

    @ModifyConstant(
            method = "applyFog",
            slice = @Slice(
                            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;FIRE_RESISTANCE:Lnet/minecraft/registry/entry/RegistryEntry;"),
                            to   = @At(value = "FIELD", target = "Lnet/minecraft/client/render/CameraSubmersionType;POWDER_SNOW:Lnet/minecraft/client/render/CameraSubmersionType;")),
            constant = @Constant(floatValue = 0.25f),
            require = 0)
    private static float tweakeroo$reduceLavaFogStart(float original)
    {
        wasLava = true;

        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            return 0.0f;
        }

        return original;
    }

    @ModifyConstant(
            method = "applyFog",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;FIRE_RESISTANCE:Lnet/minecraft/registry/entry/RegistryEntry;"),
                    to   = @At(value = "FIELD", target = "Lnet/minecraft/client/render/CameraSubmersionType;POWDER_SNOW:Lnet/minecraft/client/render/CameraSubmersionType;")),
            constant = { @Constant(floatValue = 1.0f), @Constant(floatValue = 5.0f)},
            require = 0)
    private static float tweakeroo$reduceLavaFogEnd(float original)
    {
        wasLava = true;

        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            return RenderUtils.getLavaFogDistance(MinecraftClient.getInstance().getCameraEntity(), original);
        }

        return original;
    }

    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/world/ClientWorld$Properties;getHorizonShadingRatio()F"))
    private static float tweakeroo$disableSkyDarkness(ClientWorld.Properties props)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : props.getHorizonShadingRatio();
    }

    @Inject(method = "applyFog",
            require = 0,
            at = @At(value = "INVOKE", remap = false,
                     target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V",
                     shift = At.Shift.AFTER))
    private static void tweakeroo$disableRenderDistanceFog(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            if (!thickFog && !wasLava)
            {
                float distance = Math.max(512, MinecraftClient.getInstance().gameRenderer.getViewDistance());
                RenderSystem.setShaderFogStart(distance * 1.6F);
                RenderSystem.setShaderFogEnd(distance * 2.0F);
            }

            wasLava = false;
        }
    }
}
