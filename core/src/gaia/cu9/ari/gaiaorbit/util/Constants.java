package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;

public class Constants {

    /**
     * Scale factor that applies to all distances.
     */
    public static final double SCALE_FACTOR = 5e-6;

    /**
     * Speed of light in m/s
     */
    public static final double C = 299792458;

    /**
     * Speed of light in km/h
     */
    public static final double C_KMH = 1.079253e9;

    /**
     * Solar radius in Km
     */
    public static final double R_o = .6957964e6;

    /** 
     * Metre to local unit conversion. Multiply this by all values in m.
     */
    public static final double M_TO_U = 1e-9;
    /**
     * Local unit to m conversion.
     */
    public static final double U_TO_M = 1 / M_TO_U;

    /** 
     * Kilometre to local unit conversion. Multiply this by all values in Km.
     */
    public static final double KM_TO_U = M_TO_U * 1000;
    /**
     * Local unit to km conversion.
     */
    public static final double U_TO_KM = 1 / KM_TO_U;

    /**
     * AU to local units conversion.
     */
    public static final double AU_TO_U = AstroUtils.AU_TO_KM * KM_TO_U;

    /**
     * Local unit to AU conversion.
     */
    public static final double U_TO_AU = 1 / AU_TO_U;

    /** 
     * Parsec to local unit conversion. Multiply this by all values in pc.
     */
    public static final double PC_TO_U = AstroUtils.PC_TO_KM * KM_TO_U;
    /**
     * Local unit to pc conversion.
     */
    public static final double U_TO_PC = 1 / PC_TO_U;

    /** Hours to seconds **/
    public static final double H_TO_S = 3600;

    /** Seconds to hours **/
    public static final double S_TO_H = 1 / H_TO_S;

    /** Hours to milliseconds **/
    public static final double H_TO_MS = H_TO_S * 1000;

    /** Milliseconds to hours **/
    public static final double MS_TO_H = 1 / H_TO_MS;

    /** Multiplier for all KM values in the application **/
    public static final double KM_MULTIPLIER = AstroUtils.KM_TO_PC * 1e9 * SCALE_FACTOR;

    /** Distance from Sun that marks the end of the solar system **/
    public static final double SOLAR_SYSTEM_THRESHOLD = 5e9 * KM_MULTIPLIER;

    /** Factor we need to use to get the real size of the star given its quad *texture* size **/
    public static final double STAR_SIZE_FACTOR = 1.31526e-6;
    public static final double STAR_SIZE_FACTOR_INV = 1d / STAR_SIZE_FACTOR;

    /** Threshold angle where star size remains constant, in radians. **/
    public static final double TH_ANGLE_DOWN = Math.toRadians(2.6e-5);
    public static final double TAN_TH_ANGLE_DOWN = Math.tan(Constants.TH_ANGLE_DOWN);
    public static final double TAN_TH_ANGLE_DOWN_FAC = TAN_TH_ANGLE_DOWN * STAR_SIZE_FACTOR_INV;
    public static final double TH_ANGLE_UP = Math.toRadians(3);
    public static final double TAN_TH_ANGLE_UP = Math.tan(Constants.TH_ANGLE_UP);
    public static final double TAN_TH_ANGLE_UP_FAC = TAN_TH_ANGLE_UP * STAR_SIZE_FACTOR_INV;

    /**
     * 
     * MAXIMUM AND MINIMUM VALUES FOR SEVERAL PARAMETERS - THESE SHOULD BE ENFORCED
     *
     */

    /** Minimum generic slider value **/
    public static final float MIN_SLIDER = 0;
    /** Maximum generic slider value **/
    public static final float MAX_SLIDER = 100;

    /** Maximum fov value, in degrees **/
    public static final int MAX_FOV = 145;
    /** Minimum fov value, in degrees **/
    public static final int MIN_FOV = 20;

    /** Maximum rotation speed **/
    public static final float MAX_ROT_SPEED = 3e4f;
    /** Minimum rotation speed **/
    public static final float MIN_ROT_SPEED = 2e2f;

    /** Maximum turning speed **/
    public static final float MAX_TURN_SPEED = 10e3f;
    /** Minimum turning speed **/
    public static final float MIN_TURN_SPEED = 2e2f;

    /** Minimum star brightness **/
    public static final float MIN_STAR_BRIGHT = 0f;
    /** Maximum star brightness **/
    public static final float MAX_STAR_BRIGHT = 12f;

    /**
     * 
     * SYSTEM DEPENDANT STUFF
     * 
     */
    public static boolean mobile;

    static {
	if (Gdx.app != null) {
	    mobile = Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS;
	} else {
	    mobile = false;
	}
    }

}