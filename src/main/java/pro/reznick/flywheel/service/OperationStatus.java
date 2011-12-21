package pro.reznick.flywheel.service;

/**
 * @author alex
 * @since 12/17/11 10:37 AM
 */

public enum OperationStatus
{
    /**
     * return this if the operation failed
     */
    FAILED,
    /**
     * Return this if the operation resulted in entity creation
     */
    CREATED_ENTITY,
    /**
     * Return this if the operation resulted in entity replacement
     */
    REPLACED_ENTITY,
    /**
     * Return this if the operation wasn't yet executed, but it was accepted and it's guaranteed
     * to be executed
     */
    ACCEPTED_REQUEST,
    /**
     * Return this if the operation was executed (for delete operation for example)
     */
    OK,
}
