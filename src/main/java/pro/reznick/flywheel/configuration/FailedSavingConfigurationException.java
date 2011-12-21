package pro.reznick.flywheel.configuration;

/**
 * @author alex
 * @since 12/8/11 1:30 PM
 */

public class FailedSavingConfigurationException extends Exception
{
    public FailedSavingConfigurationException(String message)
    {
        super(message);
    }

    public FailedSavingConfigurationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}