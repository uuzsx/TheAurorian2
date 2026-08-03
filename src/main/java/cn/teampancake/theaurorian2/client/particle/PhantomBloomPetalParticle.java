package cn.teampancake.theaurorian2.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class PhantomBloomPetalParticle extends SingleQuadParticle {

    private static final FacingCameraMode HORIZONTAL =
            (rotation, camera, partialTick) -> rotation.rotationX(-(float) Math.PI * 0.5F);
    private final int revealDelay;
    private final float spinSpeed;

    private PhantomBloomPetalParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double encodedDelay,
            double zSpeed,
            SpriteSet sprites,
            RandomSource random) {
        super(level, x, y, z, sprites.get(random));
        this.revealDelay = Mth.clamp((int) Math.round(Math.abs(encodedDelay) * 100.0), 0, 12);
        this.hasPhysics = false;
        this.lifetime = this.revealDelay + 28 + random.nextInt(7);
        this.quadSize = 0.11F + random.nextFloat() * 0.055F;
        this.xd = xSpeed;
        this.yd = 0.0035 + random.nextDouble() * 0.0025;
        this.zd = zSpeed;
        this.roll = random.nextFloat() * (float) (Math.PI * 2.0);
        this.oRoll = this.roll;
        this.spinSpeed = (random.nextFloat() - 0.5F) * 0.085F;
        this.alpha = 0.0F;
        if (this.revealDelay == 0) {
            this.setColor(0.70F, 0.90F, 1.0F);
        } else if (this.revealDelay <= 4) {
            this.setColor(1.0F, 0.88F, 0.96F);
        } else {
            this.setColor(0.86F, 0.76F, 1.0F);
        }
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return HORIZONTAL;
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

        int activeAge = this.age - this.revealDelay;
        if (activeAge <= 0) {
            this.alpha = 0.0F;
            return;
        }

        this.alpha = Math.min(1.0F, activeAge / 3.0F);
        this.roll += this.spinSpeed;
        this.yd += activeAge < 10 ? 0.0006 : -0.0011;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.96;
        this.yd *= 0.97;
        this.zd *= 0.96;
        int remaining = this.lifetime - this.age;
        if (remaining < 9) {
            this.alpha = Math.max(0.0F, remaining / 9.0F);
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
            return new PhantomBloomPetalParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, random);
        }
    }
}
