package pro.reznick.flywheel.hashing;

import java.nio.ByteBuffer;

/**
 * @author alex
 * @since 11/23/11 4:44 PM
 */

public interface HashingStrategy
{
    byte[] hash(byte[] data);
    byte[] hash(ByteBuffer data);
    String getStrategyName();
}
