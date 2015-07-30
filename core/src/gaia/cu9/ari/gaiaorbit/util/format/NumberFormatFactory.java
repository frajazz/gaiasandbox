package gaia.cu9.ari.gaiaorbit.util.format;

public abstract class NumberFormatFactory {
    private static NumberFormatFactory instance;

    public static void initialize(NumberFormatFactory inst) {
        instance = inst;
    }

    public static INumberFormat getFormatter(String pattern) {
        return instance.getNumberFormatter(pattern);
    }

    protected abstract INumberFormat getNumberFormatter(String pattern);
}
