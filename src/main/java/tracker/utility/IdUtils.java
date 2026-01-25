package tracker.utility;

import java.util.UUID;

public final class IdUtils {
    public static long getIdByString(String name){
        return UUID.nameUUIDFromBytes(name.getBytes()).getMostSignificantBits() & Long.MAX_VALUE;
    }
}
