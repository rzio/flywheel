package pro.reznick.flywheel.service;

import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.OperationFailedException;

/**
 * @author alex
 * @since 11/24/11 3:27 PM
 */

public interface DataService
{
    OperationStatus put(Key key, Entity entity) throws OperationFailedException;

    Entity get(Key key);

    OperationStatus delete(Key key);

    boolean containsKey(Key key);

    void duplicate(Key sourceKey, Key targetKey);
}
