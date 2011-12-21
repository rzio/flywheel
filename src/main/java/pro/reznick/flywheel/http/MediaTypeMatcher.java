package pro.reznick.flywheel.http;

/**
 * @author alex
 * @since 12/17/11 2:08 PM
 */

public class MediaTypeMatcher
{
    public static boolean matchAcceptHeader(String mediaType, String acceptHeader)
    {
        // type/subtype
        String[] mediaTypeParts = mediaType.split("/");
        String[] acceptParts = acceptHeader.split(",");

        for (String acceptPart : acceptParts)
        {
            acceptPart = acceptPart.trim();
            if (acceptPart.equals(mediaType)) return true;
            if (acceptPart.startsWith("*/*")) return true;
            if (acceptPart.startsWith(mediaTypeParts[0]) && acceptPart.contains("/*")) return true;

        }
        return false;
    }
}
