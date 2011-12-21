package pro.reznick.flywheel.exceptions;

/**
 * @author alex
 * @since 12/9/11 10:27 AM
 */

public class BadUriException extends Exception
{
    public BadUriException()
    {
        super("Bad request URI");
    }
}
