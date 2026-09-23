package cn.teampancake.theaurorian2.common.entity;

/** Continuous, critically damped pose weights; changing gait preserves position and velocity. */
public final class BlueTailWolfBlend {
    private static final double RATE = 12;
    private final double[] start = new double[3], velocity = new double[3];
    private int gait;
    private double changedAt;

    public void setGait(int next, double time) {
        if (next == gait) return;
        for (int i = 0; i < 3; i++) {
            double elapsed = Math.max(0, time - changedAt), offset = start[i] - target(i);
            double slope = velocity[i] + RATE * offset;
            double decay = Math.exp(-RATE * elapsed);
            start[i] = target(i) + (offset + slope * elapsed) * decay;
            velocity[i] = (velocity[i] - RATE * slope * elapsed) * decay;
        }
        gait = next;
        changedAt = time;
    }

    public double value(int channel, double time) {
        double elapsed = Math.max(0, time - changedAt), offset = start[channel] - target(channel);
        return Math.clamp(target(channel) + (offset + (velocity[channel] + RATE * offset) * elapsed)
                * Math.exp(-RATE * elapsed), 0, 1);
    }

    private double target(int channel) { return gait == channel + 1 ? 1 : 0; }
}
