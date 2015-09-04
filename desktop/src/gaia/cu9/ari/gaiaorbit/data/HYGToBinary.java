package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.HYGCSVLoader;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.WebGLConfInit;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;

/**
 * Small utility to convert a the HYG CSV catalog to binary in the following format:
 * - 32 bits (int) with the number of stars, starNum
 * repeat the following starNum times (for each star)
 * - 32 bits (int) - The the length of the name, or nameLength
 * - 16 bits * nameLength (chars) - The name of the star
 * - 32 bits (float) - appmag
 * - 32 bits (float) - absmag
 * - 32 bits (float) - colorbv
 * - 32 bits (float) - ra
 * - 32 bits (float) - dec
 * - 32 bits (float) - distance
 * @author Toni Sagrista
 */
public class HYGToBinary implements IObserver {

    static String fileIn = "/home/tsagrista/git/gaiasandbox/android/assets-bak/data/hygxyz.csv";
    static String fileOut = "/home/tsagrista/git/gaiasandbox/android/assets-bak/data/hygxyz.bin";

    public static void main(String[] args) {
        HYGToBinary hyg = new HYGToBinary();
        EventManager.instance.subscribe(hyg, Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

        I18n.initialize(new FileHandle("/home/tsagrista/git/gaiasandbox/android/assets/i18n/gsbundle"));

        Gdx.files = new LwjglFiles();
        try {
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());
            DateFormatFactory.initialize(new DesktopDateFormatFactory());
            ConfInit.initialize(new WebGLConfInit());

            GlobalConf.data.LIMIT_MAG_LOAD = 20;
        } catch (IOException e) {
            Logger.error(e);
        } catch (Exception e) {
            Logger.error(e);
        }

        //hyg.compareCSVtoBinary(fileIn, fileOut);

        hyg.convertToBinary(fileIn, fileOut);

    }

    public void compareCSVtoBinary(String csv, String bin) {

        try {
            HYGCSVLoader csvLoader = new HYGCSVLoader();
            HYGBinaryLoader binLoader = new HYGBinaryLoader();

            csvLoader.files = new String[] { csv };
            List<? extends CelestialBody> csvStars = csvLoader.loadData();
            binLoader.files = new String[] { bin };
            List<? extends CelestialBody> binStars = binLoader.loadData();

            if (csvStars.size() != binStars.size()) {
                System.err.println("Different sizes");
            }

            int different = 0;
            for (int i = 0; i < csvStars.size(); i++) {
                CelestialBody csvs = csvStars.get(i);
                CelestialBody bins = binStars.get(i);

                if (!equals(csvs, bins) && csvs.name.equals("Betelgeuse")) {
                    Logger.info("Different stars: " + csvs + " // " + bins);
                    different++;
                }
            }

            Logger.info("Found " + different + " different stars");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void convertToBinary(String csv, String bin) {
        HYGCSVLoader cat = new HYGCSVLoader();
        try {
            cat.files = new String[] { csv };
            List<? extends CelestialBody> stars = cat.loadData();

            // Write to binary
            File binFile = new File(bin);
            binFile.mkdirs();
            if (binFile.exists()) {
                binFile.delete();
                binFile.createNewFile();
            }
            // Create an output stream to the file.
            FileOutputStream file_output = new FileOutputStream(binFile);
            // Wrap the FileOutputStream with a DataOutputStream
            DataOutputStream data_out = new DataOutputStream(file_output);

            // Size of stars
            data_out.writeInt(stars.size());
            for (CelestialBody s : stars) {
                // name_length, name, appmag, absmag, colorbv, ra, dec, dist
                data_out.writeInt(s.name.length());
                data_out.writeChars(s.name);
                data_out.writeFloat(s.appmag);
                data_out.writeFloat(s.absmag);
                data_out.writeFloat(s.colorbv);
                data_out.writeFloat(s.posSph.x);
                data_out.writeFloat(s.posSph.y);
                data_out.writeFloat((float) s.pos.len());
                data_out.writeInt(s.id);
            }
            data_out.close();
            file_output.close();
            System.out.println(stars.size() + " stars written to binary file " + bin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBinaryFile(String fileIn) {
        HYGBinaryLoader cat = new HYGBinaryLoader();
        try {
            cat.files = new String[] { fileIn };
            List<? extends CelestialBody> stars = cat.loadData();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Object ob : data) {
                sb.append(ob);
                if (i < data.length - 1) {
                    sb.append(" - ");
                }
                i++;
            }
            System.out.println(sb);
            break;
        case JAVA_EXCEPTION:
            ((Throwable) data[0]).printStackTrace(System.err);
        }

    }

    private boolean equals(CelestialBody s1, CelestialBody s2) {
        return s1.id == s2.id && s1.posSph.x == s2.posSph.x && s1.posSph.y == s2.posSph.y && s1.pos.x == s2.pos.x && s1.pos.y == s2.pos.y && s1.pos.z == s2.pos.z && s1.absmag == s2.absmag;
    }
}