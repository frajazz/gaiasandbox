package gaia.cu9.ari.gaiaorbit.util;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

/**
 * Manages the internationalisation system.
 * @author Toni Sagrista
 *
 */
public class I18n {

    public static I18NBundle bundle;

    /**
     * Initialises the i18n system.
     */
    public static void initialize() {
	if (bundle == null) {
	    FileHandle baseFileHandle = Gdx.files.internal("i18n/gsbundle");
	    Locale locale = null;
	    if (GlobalConf.instance.LOCALE.equals("default")) {
		locale = Locale.getDefault();
	    } else {
		locale = Locale.forLanguageTag(GlobalConf.instance.LOCALE);
	    }
	    bundle = I18NBundle.createBundle(baseFileHandle, locale);
	}
    }

    /**
     * Initialises the i18n system.
     * @param fileName The file name, without the '.properties' extension.
     */
    public static void initialize(String fileName) {
	try {
	    if (bundle == null) {
		FileHandle baseFileHandle = new FileHandle(fileName);
		Locale locale = null;
		if (GlobalConf.instance.LOCALE.equals("default")) {
		    locale = Locale.getDefault();
		} else {
		    locale = Locale.forLanguageTag(GlobalConf.instance.LOCALE);
		}
		bundle = I18NBundle.createBundle(baseFileHandle, locale);
	    }
	} catch (Exception e) {
	}

    }

}
