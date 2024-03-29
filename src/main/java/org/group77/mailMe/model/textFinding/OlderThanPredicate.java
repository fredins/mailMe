package org.group77.mailMe.model.textFinding;

import org.group77.mailMe.model.Email;

import java.time.LocalDateTime;
import java.util.function.BiPredicate;

/**
 * BiPredicate subclass for checking if a given email has a date less than the given max date.
 *
 * @author Hampus Jernkrook
 */
class OlderThanPredicate implements BiPredicate<Email, LocalDateTime> {
    @Override
    public boolean test(Email email, LocalDateTime maxDate) {
        return email.date().isBefore(maxDate);
    }
}