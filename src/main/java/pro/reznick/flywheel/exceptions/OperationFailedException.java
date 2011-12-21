package pro.reznick.flywheel.exceptions;

/**
 * @author alex
 * @since 12/17/11 10:31 AM
 */

public class OperationFailedException extends Exception
{
    public OperationFailedException(String s)
    {
        super(s);
    }

    public OperationFailedException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
