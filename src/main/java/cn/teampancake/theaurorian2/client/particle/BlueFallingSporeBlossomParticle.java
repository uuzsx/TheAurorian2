package cn.teampancake.theaurorian2.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class BlueFallingSporeBlossomParticle extends SingleQuadParticle {

    private BlueFallingSporeBlossomParticle(
            ClientLevel level, double x, double y, double z, SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, sprites.get(random));
        this.setSize(0.01F, 0.01F);
        this.lifetime = (int) (64.0F / Mth.randomBetween(random, 0.1F, 0.9F));
        this.gravity = 0.005F;
        this.setColor(0.20F, 0.55F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (--this.lifetime <= 0) {
            this.remove();
            return;
        }

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround) {
            this.remove();
            return;
        }

        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;
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
            return new BlueFallingSporeBlossomParticle(level, x, y, z, this.sprites, random);
        }
    }
}
