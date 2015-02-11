package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;

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

}
