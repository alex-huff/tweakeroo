package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.Tweakeroo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    private MixinLivingEntity(EntityType<?> type, World worldIn)
    {
        super(type, worldIn);
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", ordinal = 1,
            target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean tweakeroo$fixElytraLanding(World world)
    {
        return world.isClient &&
                (Configs.Fixes.ELYTRA_FIX.getBooleanValue() == false ||
                ((Object) this instanceof ClientPlayerEntity) == false);
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;"),
            cancellable = true)
    private void tweakeroo$removeOwnPotionEffects(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_FP_EFFECT_PARTICLES.getBooleanValue())
        {
            Tweakeroo.debugLog("tweakeroo$removeOwnPotionEffects(): pre");

            // TODO I don't know why IntelliJ is greying this out. --> TEST
            if (((Object) this) instanceof ClientPlayerEntity)
            {
                Tweakeroo.debugLog("tweakeroo$removeOwnPotionEffects(): post");
                if (MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON)
                {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;tickFallFlying()V"))
    private void tweakeroo$applyCustomDeceleration(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_CUSTOM_FLY_DECELERATION.getBooleanValue())
        {
            Tweakeroo.debugLog("tweakeroo$applyCustomDeceleration(): pre");

            // TODO I don't know why IntelliJ is greying this out. --> TEST
            if (((Object) this) instanceof ClientPlayerEntity)
            {
                Tweakeroo.debugLog("tweakeroo$applyCustomDeceleration(): post");
                MiscUtils.handlePlayerDeceleration();
            }
        }
    }
}
