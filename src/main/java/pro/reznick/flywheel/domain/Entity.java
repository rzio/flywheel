package pro.reznick.flywheel.domain;

/**
 * @author alex
 * @since 11/25/11 3:53 PM
 */

public class Entity
{
    final byte[] data;
    final String mediaType;

    public Entity(byte[] data, String mediaType)
    {
        this.data = data;
        this.mediaType = mediaType;
    }

    public byte[] getData()
    {
        return data;
    }

    public String getMediaType()
    {
        return mediaType;
    }
}
