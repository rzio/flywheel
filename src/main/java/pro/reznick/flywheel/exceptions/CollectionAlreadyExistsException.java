package pro.reznick.flywheel.exceptions;

/**
 * @author alex
 * @since 11/25/11 7:54 PM
 */

public class CollectionAlreadyExistsException extends Exception
{
    public CollectionAlreadyExistsException(String collectionName)
    {
        super(String.format("CollectionConfiguration %s allready exists",collectionName));
    }
}
