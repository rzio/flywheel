package pro.reznick.flywheel.domain;

import java.nio.ByteBuffer;

/**
 * @author alex
 * @since 12/10/11 10:06 AM
 */

public interface Key
{
    String getCollectionName();

    ByteBuffer getStorageKey();
}
