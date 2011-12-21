package pro.reznick.flywheel.hashing;

/**
 * @author alex
 * @since 11/23/11 4:44 PM
 */

public interface HashingStrategy
{
    byte[] hash(byte[] data);
    String getStrategyName();
}
