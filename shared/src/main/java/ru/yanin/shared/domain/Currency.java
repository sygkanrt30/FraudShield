package ru.yanin.shared.domain;

/**
 * @author Vyacheslav Yanin
 */
public enum Currency {
    RUB,
    USD,
    EUR;

    public static Currency fromString(String currency) {
        for (Currency c : Currency.values()) {
            if (c.name().equalsIgnoreCase(currency)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown currency: " + currency);
    }
}
