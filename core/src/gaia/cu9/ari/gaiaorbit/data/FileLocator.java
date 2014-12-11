package gaia.cu9.ari.gaiaorbit.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;

public class FileLocator {

    private static String ASSETS_FOLDER;

    public static void initialize(String assetsFolder) {
	ASSETS_FOLDER = assetsFolder;
    }

    public static InputStream getStream(String file) throws FileNotFoundException {
	if (Gdx.files != null) {
	    return Gdx.files.internal(file).read();
	} else {
	    return new FileInputStream(new File(ASSETS_FOLDER + file));
	}
    }

    public static boolean exists(String file) {
	if (Gdx.files != null) {
	    return Gdx.files.internal(file).exists();
	} else {
	    return (new File(ASSETS_FOLDER + file)).exists();
	}
    }

}
