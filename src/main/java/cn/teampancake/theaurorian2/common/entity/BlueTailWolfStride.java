package cn.teampancake.theaurorian2.common.entity;

/** Client visual odometer, in blocks at adult model scale. Never changes movement or sends packets. */
public final class BlueTailWolfStride {
    // Measured from the authored paw trajectories during ground contact, not from the AI speed attribute.
    public static final double WALK_STRIDE = 1.73;
    public static final double RUN_STRIDE = 4.10;
    private double distance, previousDistance;

    public void advance(double travelled, double modelScale, boolean grounded) {
        previousDistance = distance;
        // Teleport corrections must not spin the legs through the distance to the new position.
        if (grounded && Double.isFinite(travelled) && travelled >= 0 && travelled <= 1.5
                && Double.isFinite(modelScale) && modelScale > 0) distance += travelled / modelScale;
    }

    public double animationTime(boolean running, float partialTick) {
        double interpolated = previousDistance + (distance - previousDistance) * Math.clamp(partialTick, 0, 1);
        double cycles = interpolated / (running ? RUN_STRIDE : WALK_STRIDE);
        return (cycles - Math.floor(cycles)) * (running ? 0.5 : 1.16667);
    }
}
