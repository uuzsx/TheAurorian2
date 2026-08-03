package cn.teampancake.theaurorian2.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class PhantomPetalParticle extends SingleQuadParticle {

    private final float spinSpeed;

    private PhantomPetalParticle(
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
        this.hasPhysics = false;
        this.lifetime = 26 + random.nextInt(21);
        this.quadSize = 0.09F + random.nextFloat() * 0.06F;
        this.xd = xSpeed + (random.nextDouble() - 0.5) * 0.025;
        this.yd = ySpeed + random.nextDouble() * 0.025;
        this.zd = zSpeed + (random.nextDouble() - 0.5) * 0.025;
        this.roll = random.nextFloat() * (float) (Math.PI * 2.0);
        this.oRoll = this.roll;
        this.spinSpeed = (random.nextFloat() - 0.5F) * 0.22F;
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

        this.roll += this.spinSpeed;
        this.xd += Math.sin(this.age * 0.31 + this.spinSpeed) * 0.0008;
        this.zd += Math.cos(this.age * 0.27 + this.spinSpeed) * 0.0008;
        this.yd -= 0.0018;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.97;
        this.yd *= 0.98;
        this.zd *= 0.97;
        if (this.age > this.lifetime - 10) {
            this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 10.0F);
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
            return new PhantomPetalParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, random);
        }
    }
}
