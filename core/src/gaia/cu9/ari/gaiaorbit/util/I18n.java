package gaia.cu9.ari.gaiaorbit.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Manages the i18n system.
 * @author Toni Sagrista
 *
 */
public class I18n {

    public static I18NBundle bundle;
    public static Locale locale;

    /**
     * Initialises the i18n system.
     */
    public static void initialize() {
        if (bundle == null) {
            forceinit(Gdx.files.internal("i18n/gsbundle"));
        }
    }

    /**
     * Initialises the i18n system.
     * @param fileName The file name, without the '.properties' extension.
     */
    public static void initialize(String fileName) {
        try {
            if (bundle == null) {
                forceinit(fileName);
            }
        } catch (Exception e) {
        }

    }

    public static boolean forceinit(String fileName) {
        return forceinit(new FileHandle(fileName));
    }

    public static boolean forceinit(FileHandle baseFileHandle) {
        if (GlobalConf.program == null || GlobalConf.program.LOCALE.isEmpty()) {
            // Use system default
            locale = Locale.getDefault();
        } else {
            locale = Locale.forLanguageTag(GlobalConf.program.LOCALE);
        }
        try {
            bundle = I18NBundle.createBundle(baseFileHandle, locale);
            return true;
        } catch (MissingResourceException e) {
            // Use default locale - en_GB
            locale = Locale.forLanguageTag("en-GB");
            try {
                bundle = I18NBundle.createBundle(baseFileHandle, locale);
            } catch (Exception e2) {
            }
            return false;
        }

    }

}
