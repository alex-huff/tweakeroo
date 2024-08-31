package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.explosion.Explosion;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @Shadow @Final private ParticleEffect emitterParticle;

    @Redirect(method = "affectWorld",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/world/explosion/Explosion;emitterParticle:Lnet/minecraft/particle/ParticleEffect;"))
    private ParticleEffect tweakeroo$reducedParticles$redirectEmitterParticle(Explosion instance)
    {
        if (FeatureToggle.TWEAK_EXPLOSION_REDUCED_PARTICLES.getBooleanValue())
        {
            return ParticleTypes.EXPLOSION;
        }

        return this.emitterParticle;
    }
}
