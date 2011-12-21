package pro.reznick.flywheel.exceptions;

/**
 * @author alex
 * @since 12/17/11 11:13 AM
 */

public class MissingCollectionException extends Exception
{
    String collectionName;

    public MissingCollectionException(String collectionName)
    {
        super(String.format("Missing collection: %s", collectionName));
        this.collectionName = collectionName;
    }

    public MissingCollectionException(String collectionName, Throwable throwable)
    {
        super(String.format("Missing collection: %s", collectionName), throwable);
        this.collectionName = collectionName;
    }

    public String getCollectionName()
    {
        return collectionName;
    }
}
