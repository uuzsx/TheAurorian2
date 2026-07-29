package cn.teampancake.theaurorian2.common.effect;

import java.util.function.Supplier;

public final class EffectRemovalContext {

    public enum Reason {
        DEFAULT,
        MILK,
        HOLINESS
    }

    private static final ThreadLocal<Reason> CURRENT = ThreadLocal.withInitial(() -> Reason.DEFAULT);

    private EffectRemovalContext() {
    }

    public static Reason current() {
        return CURRENT.get();
    }

    public static <T> T run(Reason reason, Supplier<T> action) {
        Reason previous = CURRENT.get();
        CURRENT.set(reason);
        try {
            return action.get();
        } finally {
            CURRENT.set(previous);
        }
    }
}
