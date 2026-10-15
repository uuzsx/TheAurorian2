package cn.teampancake.theaurorian2.client.renderer;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.BoneSnapshots;

/** Per-queen pose history. Captures the real step and velocity before a state change. */
final class MoonQueenPoseBlend {
    private static final int CHANNELS = 9;
    private BakedGeoModel model;
    private GeoBone[] bones;
    private boolean[] blinkBones;
    private float[] lastPose, velocity, fromPose, fromVelocity;
    private long stamp = Long.MIN_VALUE;
    private double lastTime = Double.NaN, blendStart, duration;
    private boolean preview;

    void apply(BakedGeoModel nextModel, BoneSnapshots snapshots, long nextStamp, double time, double blendDuration,
               boolean nextPreview) {
        if (nextModel != model) {
            model = nextModel;
            bones = model.boneLookup().get().values().toArray(GeoBone[]::new);
            blinkBones = new boolean[bones.length];
            int size = bones.length * CHANNELS;
            lastPose = new float[size]; velocity = new float[size];
            fromPose = new float[size]; fromVelocity = new float[size];
            for (int i = 0; i < bones.length; i++) blinkBones[i] = switch (bones[i].name()) {
                case "LeftEyeball", "RightEyeball", "LeftEyebrow", "RightEyebrow" -> true;
                default -> false;
            };
            lastTime = Double.NaN;
            preview = false;
        }
        boolean initialized = Double.isFinite(lastTime);
        if (nextStamp != stamp || !initialized || time < lastTime) {
            if (initialized) {
                System.arraycopy(lastPose, 0, fromPose, 0, lastPose.length);
                if (nextPreview || preview) {
                    // A different authored action is not a continuation of the
                    // previous swing. Keep every channel between its two poses
                    // instead of extrapolating that swing into the body or ground.
                    java.util.Arrays.fill(fromVelocity, 0);
                } else System.arraycopy(velocity, 0, fromVelocity, 0, velocity.length);
            }
            stamp = nextStamp;
            // Advance from the last rendered instant even on the first new-state
            // frame, so entering a transition does not hold the old step once.
            blendStart = initialized && time > lastTime && time - lastTime < .25 ? lastTime : time;
            duration = initialized ? (nextPreview || preview ? .22 : blendDuration) : 0;
            preview = nextPreview;
        }
        double dt = time - lastTime;
        double u = duration > 0 ? Math.clamp((time - blendStart) / duration, 0, 1) : 1;
        double u2 = u*u, u3 = u2*u, u4 = u3*u, u5 = u4*u;
        double weight = 10*u3 - 15*u4 + 6*u5;
        double tangent = (u - 6*u3 + 8*u4 - 3*u5) * duration;
        for (int i = 0; i < bones.length; i++) {
            if (blinkBones[i]) continue; // The independent blink clock must never pause during a step.
            BoneSnapshot pose = snapshots.get(bones[i]);
            int offset = i * CHANNELS;
            for (int channel = 0; channel < CHANNELS; channel++) {
                int index = offset + channel;
                float target = read(pose, channel);
                float value;
                // Outline/shader passes at the same time reuse the already blended result.
                if (initialized && dt == 0) value = lastPose[index];
                else if (u < 1) {
                    double delta = target - fromPose[index];
                    if (channel < 3) delta = wrap(delta);
                    value = (float)(fromPose[index] + delta * weight + fromVelocity[index] * tangent);
                } else value = target;
                if (initialized && dt > 0 && dt < .5) {
                    double delta = value - lastPose[index];
                    if (channel < 3) delta = wrap(delta);
                    float limit = channel < 3 ? 16 : channel < 6 ? 200 : 20;
                    velocity[index] = (float)Math.clamp(delta / dt, -limit, limit);
                } else if (!initialized || dt >= .5) velocity[index] = 0;
                lastPose[index] = value;
            }
            pose.setRotation(lastPose[offset], lastPose[offset+1], lastPose[offset+2]);
            pose.setTranslation(lastPose[offset+3], lastPose[offset+4], lastPose[offset+5]);
            pose.setScale(lastPose[offset+6], lastPose[offset+7], lastPose[offset+8]);
        }
        lastTime = time;
    }

    private static double wrap(double angle) {
        double value = (angle + Math.PI) % (Math.PI * 2);
        return (value < 0 ? value + Math.PI * 2 : value) - Math.PI;
    }

    private static float read(BoneSnapshot pose, int channel) {
        return switch (channel) {
            case 0 -> pose.getRotX(); case 1 -> pose.getRotY(); case 2 -> pose.getRotZ();
            case 3 -> pose.getTranslateX(); case 4 -> pose.getTranslateY(); case 5 -> pose.getTranslateZ();
            case 6 -> pose.getScaleX(); case 7 -> pose.getScaleY(); default -> pose.getScaleZ();
        };
    }
}
