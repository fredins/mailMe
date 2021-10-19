package org.group77.mailMe.model.exceptions;

/**
 * Exception for when a submitted account already exists in storage.
 *
 * @author Hampus Jernkrook
 */
public class AccountAlreadyExistsException extends StorageException {
    public AccountAlreadyExistsException(String msg) {
        super(msg);
    }
}