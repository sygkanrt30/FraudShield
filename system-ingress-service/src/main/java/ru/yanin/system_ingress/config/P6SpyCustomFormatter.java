package ru.yanin.system_ingress.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;

import java.util.Objects;

/**
 * @author Vyacheslav Yanin
 */
public class P6SpyCustomFormatter implements MessageFormattingStrategy {

    private final Formatter formatter = new BasicFormatterImpl();

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {

        if (Objects.isNull(sql) || sql.trim().isEmpty()) {
            return "";
        }

        return String.format(
                """
                 
                 Execution time: %d ms | Category: %s | Connection ID: %d
                 %s
                 """,
                elapsed, category, connectionId, formatter.format(sql)
        );
    }
}
