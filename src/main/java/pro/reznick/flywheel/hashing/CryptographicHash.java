package pro.reznick.flywheel.hashing;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author alex
 * @since 11/23/11 4:44 PM
 */

public class CryptographicHash implements HashingStrategy
{
    final String algorithm;
    final int outputSize;

    private CryptographicHash(String algorithm, int outputSize)
    {
        this.algorithm = algorithm;
        this.outputSize = outputSize;
    }

    public byte[] hash(byte[] data)
    {
        return hash(ByteBuffer.wrap(data));
    }

    @Override
    public byte[] hash(ByteBuffer data)
    {
        MessageDigest md;
        byte[] hash = new byte[outputSize];

        try
        {
            md = MessageDigest.getInstance(algorithm);
            md.update(data);
            hash = md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            //TODO exception
        }
        return hash;
    }


    public String getStrategyName()
    {
        return algorithm;
    }


    public static HashingStrategy MD5 = new CryptographicHash("MD5", 16);
    public static HashingStrategy SHA1 = new CryptographicHash("SHA-1", 20);
    public static HashingStrategy SHA256 = new CryptographicHash("SHA-256", 32);
    public static HashingStrategy SHA512 = new CryptographicHash("SHA-512", 64);
}

