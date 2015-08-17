package gaia.cu9.ari.gaiaorbit.client.format;

import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

public class GwtNumberFormatFactory extends NumberFormatFactory {

    @Override
    protected INumberFormat getNumberFormatter(String pattern) {
        return new GwtNumberFormat(pattern);
    }

}
