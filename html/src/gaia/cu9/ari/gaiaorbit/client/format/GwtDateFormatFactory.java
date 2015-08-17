package gaia.cu9.ari.gaiaorbit.client.format;

import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;

import java.util.Locale;

public class GwtDateFormatFactory extends DateFormatFactory {

    @Override
    protected IDateFormat getDateFormatter(String pattern) {
        return new GwtDateFormat(pattern);
    }

    @Override
    protected IDateFormat getDateFormatter(Locale loc) {
        return new GwtDateFormat(loc, false);
    }

    @Override
    protected IDateFormat getTimeFormatter(Locale loc) {
        return new GwtDateFormat(loc, true);
    }

}
