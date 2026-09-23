package cn.teampancake.theaurorian2.common.entity;

/** Continuous, bounded pose weights. Reversing direction preserves both value and velocity. */
public final class SpiritGlideBlend {
    public static final int HEAD = 0, BODY = 1, ARMS = 2, FINGERS = 3;
    private static final double[] RESPONSE = {9, 7, 6, 5.5};
    private final double[] start = new double[4], velocity = new double[4];
    private double changedAt;
    private boolean moving;

    public void setMoving(boolean moving, double seconds) {
        if (this.moving == moving) return;
        double elapsed = Math.max(0, seconds - changedAt);
        for (int i = 0; i < start.length; i++) {
            double rate = RESPONSE[i], offset = start[i] - (this.moving ? 1 : 0);
            double slope = velocity[i] + rate * offset, decay = Math.exp(-rate * elapsed);
            start[i] = (this.moving ? 1 : 0) + (offset + slope * elapsed) * decay;
            velocity[i] = (velocity[i] - rate * slope * elapsed) * decay;
        }
        this.moving = moving;
        changedAt = seconds;
    }

    public double value(int group, double seconds) {
        double elapsed = Math.max(0, seconds - changedAt), target = moving ? 1 : 0;
        double offset = start[group] - target, rate = RESPONSE[group];
        return target + (offset + (velocity[group] + rate * offset) * elapsed) * Math.exp(-rate * elapsed);
    }
}
