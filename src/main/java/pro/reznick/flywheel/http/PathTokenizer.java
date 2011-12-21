package pro.reznick.flywheel.http;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @since 12/10/11 12:20 PM
 */

public class PathTokenizer
{
    public static List<String> tokenize(String path)
    {
        List<String> ret = new ArrayList<String>(2);// usually the path will contain 2 parts (collection, key);
        // searching and copying strings with minimal passes
        int currentStartOfset = 0;
        for (int i = 0; i < path.length(); i++)
        {
            if (path.charAt(i) == '/' && i - currentStartOfset > 0)
            {
                ret.add(path.substring(currentStartOfset, i));
            }
            if (path.charAt(i) == '/')
                currentStartOfset = i + 1;

        }
        if (currentStartOfset <= path.length() - 1)
            ret.add(path.substring(currentStartOfset, path.length()));
        return ret;
    }
}
