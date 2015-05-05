package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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

    public static void main(String[] args) {
        HYGToBinary hyg = new HYGToBinary();
        EventManager.instance.subscribe(hyg, Events.POST_NOTIFICATION);

        InputStream versionFile = HYGToBinary.class.getResourceAsStream("/version");
        Properties vprops = new Properties();
        try {
            vprops.load(versionFile);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        GlobalConf.version = new GlobalConf.VersionConf();
        GlobalConf.version.initialize(vprops);

        //hyg.compareCSVtoBinary("/home/tsagrista/Workspaces/workspace/GaiaOrbit-android/assets/data/hyg80.csv", "/home/tsagrista/Workspaces/workspace/GaiaOrbit-android/assets/data/hyg80.bin");

        hyg.convertToBinary();

    }

    public void convertToBinary() {
        String fileIn = "/home/tsagrista/Workspaces/workspace-luna/GaiaSandbox-android/assets-bak/data/hygxyz.csv";
        String fileOut = "/home/tsagrista/Workspaces/workspace-luna/GaiaSandbox-android/assets/data/android/hygxyz.bin";
        convertToBinary(fileIn, fileOut);
    }

    public void compareCSVtoBinary(String csv, String bin) {

        try {
            HYGCSVLoader csvLoader = new HYGCSVLoader();
            HYGBinaryLoader binLoader = new HYGBinaryLoader();

            List<? extends CelestialBody> csvStars = csvLoader.loadCatalog(new FileInputStream(new File(csv)));
            List<? extends CelestialBody> binStars = binLoader.loadCatalog(new FileInputStream(new File(bin)));

            if (csvStars.size() != binStars.size()) {
                System.err.println("Different sizes");
            }

            for (int i = 0; i < csvStars.size(); i++) {
                CelestialBody csvs = csvStars.get(i);
                CelestialBody bins = binStars.get(i);

                if (!csvs.equals(bins)) {
                    System.err.println("Different stars: idx: " + i + ", name: " + csvs.name);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void convertToBinary(String fileIn, String fileOut) {
        HYGCSVLoader cat = new HYGCSVLoader();
        try {
            GlobalConf.data.LIMIT_MAG_LOAD = 6.3f;
            List<? extends CelestialBody> stars = cat.loadCatalog(new FileInputStream(new File(fileIn)));

            // Write to binary
            File binFile = new File(fileOut);
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
                data_out.writeDouble(s.posSph.x);
                data_out.writeDouble(s.posSph.y);
                data_out.writeDouble(s.pos.len());
                data_out.writeLong(s.id);
            }
            file_output.close();
            System.out.println(stars.size() + " stars written to binary file " + fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBinaryFile(String fileIn) {
        HYGBinaryLoader cat = new HYGBinaryLoader();
        try {
            List<? extends CelestialBody> stars = cat.loadCatalog(new FileInputStream(new File(fileIn)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            System.out.println((String) data[0]);
        }

    }
}
