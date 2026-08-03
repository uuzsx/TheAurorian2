package cn.teampancake.theaurorian2.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class PhantomButterflyParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
    private final float flutterPhase;

    private PhantomButterflyParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites,
            RandomSource random) {
        super(level, x, y, z, sprites.get(random));
        this.sprites = sprites;
        this.hasPhysics = false;
        this.lifetime = 24 + random.nextInt(17);
        this.quadSize = 0.16F + random.nextFloat() * 0.07F;
        this.flutterPhase = random.nextFloat() * (float) (Math.PI * 2.0);
        this.xd = xSpeed + (random.nextDouble() - 0.5) * 0.035;
        this.yd = ySpeed + 0.015 + random.nextDouble() * 0.025;
        this.zd = zSpeed + (random.nextDouble() - 0.5) * 0.035;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.setSpriteFromAge(this.sprites);
        double flutter = Math.sin(this.flutterPhase + this.age * 0.72) * 0.008;
        this.xd += flutter;
        this.zd -= flutter * 0.7;
        this.yd += Math.sin(this.flutterPhase + this.age * 0.42) * 0.0015;
        this.roll += (float) flutter * 0.8F;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.92;
        this.yd *= 0.94;
        this.zd *= 0.92;
        if (this.age > this.lifetime - 8) {
            this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 8.0F);
        }
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
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
            return new PhantomButterflyParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, random);
        }
    }
}
