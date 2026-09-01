package cn.teampancake.theaurorian2.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class AurorianFireflyParticle extends FireflyParticle {

    private AurorianFireflyParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xa,
            double ya,
            double za,
            TextureAtlasSprite sprite) {
        super(level, x, y, z, xa, ya, za, sprite);
        this.setColor(0.25F, 0.72F, 1.0F);
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
            AurorianFireflyParticle particle = new AurorianFireflyParticle(
                    level,
                    x,
                    y,
                    z,
                    0.5 - random.nextDouble(),
                    random.nextBoolean() ? ySpeed : -ySpeed,
                    0.5 - random.nextDouble(),
                    this.sprites.get(random));
            particle.setLifetime(random.nextIntBetweenInclusive(200, 300));
            particle.scale(1.5F);
            particle.setAlpha(0.0F);
            return particle;
        }
    }
}
