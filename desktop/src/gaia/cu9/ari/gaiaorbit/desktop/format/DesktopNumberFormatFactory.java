package gaia.cu9.ari.gaiaorbit.desktop.format;

import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

public class DesktopNumberFormatFactory extends NumberFormatFactory {

    @Override
    protected INumberFormat getNumberFormatter(String pattern) {
        return new DesktopNumberFormat(pattern);
    }

}
