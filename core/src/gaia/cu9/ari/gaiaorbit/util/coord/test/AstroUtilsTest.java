package gaia.cu9.ari.gaiaorbit.util.coord.test;

import static org.junit.Assert.assertEquals;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;

/**
 * Created by tsagrista on 01/06/15.
 */
public class AstroUtilsTest {

    @org.junit.Test
    /**
     * JD2456536.5 TCB = 2013-09-01 00:00:00
     */
    public void testGetJulianDate() throws Exception {
        double jd1 = AstroUtils.getJulianDate(2013, 9, 1, 0, 0, 0, 0, true);

        assertEquals(2456536.5, jd1, 0);
    }
}