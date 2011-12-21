package pro.reznick.flywheel.configuration;

/**
 * @author alex
 * @since 12/2/11 11:00 AM
 */

public class InvalidConfigurationException extends RuntimeException
{
    public InvalidConfigurationException(String message){
        super(message);
    }

    public InvalidConfigurationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
