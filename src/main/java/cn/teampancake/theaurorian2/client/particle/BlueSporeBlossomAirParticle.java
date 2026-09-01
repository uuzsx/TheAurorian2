package cn.teampancake.theaurorian2.client.particle;

import java.util.Optional;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class BlueSporeBlossomAirParticle extends SingleQuadParticle {

    private BlueSporeBlossomAirParticle(
            ClientLevel level, double x, double y, double z, SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, 0.0, -0.8, 0.0, sprites.get(random));
        this.setSize(0.01F, 0.01F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.6F;
        this.lifetime = Mth.randomBetweenInclusive(random, 500, 1000);
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.01F;
        this.setColor(0.20F, 0.55F, 1.0F);
    }

    @Override
    public Optional<ParticleLimit> getParticleLimit() {
        return Optional.of(ParticleLimit.SPORE_BLOSSOM);
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource random) {
            return new BlueSporeBlossomAirParticle(level, x, y, z, this.sprites, random);
        }
    }
}
