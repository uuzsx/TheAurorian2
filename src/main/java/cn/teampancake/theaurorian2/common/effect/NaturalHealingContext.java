package cn.teampancake.theaurorian2.common.effect;

public final class NaturalHealingContext {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private NaturalHealingContext() {
    }

    public static void enter() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void exit() {
        int next = DEPTH.get() - 1;
        if (next <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(next);
        }
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }
}
