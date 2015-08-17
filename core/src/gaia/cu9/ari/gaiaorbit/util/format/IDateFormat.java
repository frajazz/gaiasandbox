package gaia.cu9.ari.gaiaorbit.util.format;

import java.util.Date;

public interface IDateFormat {
    public String format(Date date);

    public Date parse(String date);
}
