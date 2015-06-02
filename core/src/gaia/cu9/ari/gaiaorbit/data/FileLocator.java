package gaia.cu9.ari.gaiaorbit.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileLocator {

    private static String ASSETS_FOLDER;

    public static void initialize() {
        ASSETS_FOLDER = System.getProperty("assets.location");
        if (ASSETS_FOLDER == null) {
            ASSETS_FOLDER = "";
        }
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

    public static File getFile(String file){
        return new File(ASSETS_FOLDER, file);
    }

    public static FileHandle internal(String file){
        return Gdx.files.internal(ASSETS_FOLDER + file);
    }

    public static FileHandle external(String file){
        return Gdx.files.external(ASSETS_FOLDER + file);
    }

}
