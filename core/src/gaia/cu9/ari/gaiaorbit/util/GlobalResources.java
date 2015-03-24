package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Holds and initializes resources utilized globally.
 * @author Toni Sagrista
 *
 */
public class GlobalResources {

    /** Global all-purpose sprite batch **/
    public static SpriteBatch spriteBatch;
    /** Link cursor **/
    public static Pixmap linkCursor;
    /** The global skin **/
    public static Skin skin;
    /** Number formats **/
    public static NumberFormat oneDecimalFormat, twoDecimalsFormat;

    /**
     * Model for atmosphere scattering
     */
    public static final String atmModelLocation = "models/atm/atm-uv.g3db";

    public static void initialize(AssetManager manager) {

	// Sprite batch
	spriteBatch = new SpriteBatch();

	// Number formats
	oneDecimalFormat = new DecimalFormat("#######0.0");
	twoDecimalsFormat = new DecimalFormat("#######0.0#");

	// Create skin right now, it is needed.
	skin = new Skin(Gdx.files.internal("skins/" + GlobalConf.program.UI_THEME + ".json"));

	// Async load
	manager.load("img/cursor-link.png", Pixmap.class);
    }

    public static void doneLoading(AssetManager manager) {
	// Cursor for links
	linkCursor = manager.get("img/cursor-link.png", Pixmap.class);
    }

    /**
     * Converts from property name to method name by removing the 
     * separator dots and capitalising each chunk.
     * Example: model.texture.bump -> ModelTextureBump
     * @param property The property name.
     * @return
     */
    public static String propertyToMethodName(String property) {
	String[] parts = property.split("\\.");
	StringBuilder b = new StringBuilder();
	for (String part : parts) {
	    b.append(capitalise(part));
	}
	return b.toString();
    }

    /**
     * Returns the given string with the first letter capitalised
     * @param line
     * @return
     */
    public static String capitalise(String line) {
	return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * Returns the given string with the first letter capitalised and all the others in lower case.
     * @param line
     * @return
     */
    public static String trueCapitalise(String line) {
	return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }

    /**
     * Converts this float to the string representation of a distance.
     * @param d
     * @return An array containing the float number and the string units.
     */
    public static Object[] floatToDistanceString(float d) {
	d = d * (float) Constants.U_TO_KM;
	if (d >= 1f && d < AstroUtils.PC_TO_KM) {
	    // km
	    return new Object[] { d, "km" };
	} else if (d < 1f) {
	    // m
	    return new Object[] { (d * 1000), "m" };
	} else {
	    // pc
	    return new Object[] { (d * AstroUtils.KM_TO_PC), "pc" };
	}
    }

    /**
     * Transforms the given double array into a float array by casting
     * each of its numbers.
     * @param array
     * @return
     */
    public static float[] toFloatArray(double[] array) {
	float[] res = new float[array.length];
	for (int i = 0; i < array.length; i++)
	    res[i] = (float) array[i];
	return res;
    }

    /**
     * Computes whether a body with the given position is visible by a camera with the given direction
     * and angle. Coordinates are assumed to be in the camera-origin system.
     * @param point The position of the body in the reference system of the camera (i.e. camera is at origin).
     * @param coneAngle The cone angle of the camera.
     * @param dir The direction.
     * @return True if the body is visible.
     */
    public static boolean isInView(Vector3d point, float coneAngle, Vector3d dir) {
	return MathUtilsd.acos(point.dot(dir) / point.len()) < coneAngle;
    }

    /**
     * Computes whether any of the given points is visible by a camera with the given direction
     * and the given cone angle. Coordinates are assumed to be in the camera-origin system.
     * @param points The array of points to check.
     * @param coneAngle The cone angle of the camera (field of view).
     * @param dir The direction.
     * @return True if any of the points is in the camera view cone.
     */
    public static boolean isAnyInView(Vector3d[] points, float coneAngle, Vector3d dir) {
	boolean inview = false;
	int size = points.length;
	for (int i = 0; i < size; i++) {
	    inview = inview || MathUtilsd.acos(points[i].dot(dir) / points[i].len()) < coneAngle;
	}
	return inview;
    }

    /**
     * Compares a given buffer with another buffer.
     * @param buf Buffer to compare against
     * @param compareTo Buffer to compare to (content should be ASCII lowercase if possible)
     * @return True if the buffers compare favourably, false otherwise
     */
    public static boolean equal(String buf, char[] compareTo, boolean ignoreCase) {
	if (buf == null || compareTo == null || buf.length() == 0)
	    return false;
	char a, b;
	int len = Math.min(buf.length(), compareTo.length);
	if (ignoreCase) {
	    for (int i = 0; i < len; i++) {
		a = buf.charAt(i);
		b = compareTo[i];
		if (a == b || (a - 32) == b)
		    continue; // test a == a or A == a;
		return false;
	    }
	} else {
	    for (int i = 0; i < len; i++) {
		a = buf.charAt(i);
		b = compareTo[i];
		if (a == b)
		    continue; // test a == a
		return false;
	    }
	}
	return true;
    }

}
