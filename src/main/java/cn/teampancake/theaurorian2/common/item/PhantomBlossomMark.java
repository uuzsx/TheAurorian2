package cn.teampancake.theaurorian2.common.item;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record PhantomBlossomMark(@Nullable UUID owner, long expiresAt) {

    public static final PhantomBlossomMark EMPTY = new PhantomBlossomMark(null, 0L);

    public boolean isActive(long gameTime) {
        return this.owner != null && gameTime <= this.expiresAt;
    }
}
