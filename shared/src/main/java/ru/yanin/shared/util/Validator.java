package ru.yanin.shared.util;

import java.util.Objects;

/**
 * @author Vyacheslav Yanin
 */
public final class Validator {

    public static void validateNonNull(Object... fields) {
        for (Object field : fields) {
            Objects.requireNonNull(field);
        }
    }
}
