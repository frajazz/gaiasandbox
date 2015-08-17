package gaia.cu9.ari.gaiaorbit.util.format;

import java.util.Locale;

public abstract class DateFormatFactory {
    private static DateFormatFactory instance;

    public enum DateType {
        DATE, TIME
    }

    public static void initialize(DateFormatFactory inst) {
        instance = inst;
    }

    public static IDateFormat getFormatter(String pattern) {
        return instance.getDateFormatter(pattern);
    }

    public static IDateFormat getFormatter(Locale loc, DateType type) {
        switch (type) {
        case DATE:
            return instance.getDateFormatter(loc);
        case TIME:
            return instance.getTimeFormatter(loc);
        }
        return null;
    }

    protected abstract IDateFormat getDateFormatter(String pattern);

    protected abstract IDateFormat getDateFormatter(Locale loc);

    protected abstract IDateFormat getTimeFormatter(Locale loc);
}
