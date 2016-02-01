package gaia.cu9.ari.gaiaorbit.data.galaxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.data.OctreeGeneratorTest;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopConfInit;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

public class GalaxyGenerator implements IObserver {

    /** Whether to write the results to disk **/
    private static final boolean writeFile = true;

    /** Number of spiral arms **/
    private static final int Narms = 5;

    /** Radius of the galaxy **/
    private static final float radius = 10f;

    /** Number of particles **/
    private static final int N = 5000;

    /** Ratio radius/armWidth **/
    private static final float armWidthRatio = 1f / 20f;

    /** Maximum spiral rotation (end of arm) in degrees **/
    private static final float maxRotation = 190f;

    public static void main(String[] args) {
        try {
            Gdx.files = new LwjglFiles();

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            ConfInit.initialize(new DesktopConfInit(new FileInputStream(new File("../android/assets/conf/global.properties")), new FileInputStream(new File("../android/assets/data/dummyversion"))));

            I18n.initialize(new FileHandle("/home/tsagrista/git/gaiasandbox/android/assets/i18n/gsbundle"));

            // Add notif watch
            EventManager.instance.subscribe(new OctreeGeneratorTest(), Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            List<Vector3> gal = generateGalaxy();

            if (writeFile) {
                writeToDisk(gal, "/home/tsagrista/Documents/");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a galaxy (particle positions) with spiral arms and so on.
     * The galactic plane is XZ and Y points to the galactic north pole.
     * @throws IOException
     */
    private static List<Vector3> generateGalaxy() throws IOException {
        Random rand = new Random();

        int NperArm = N / Narms;
        float armWidth = radius * armWidthRatio;

        List<Vector3> particles = new ArrayList<Vector3>(N);

        float stepAngle = 360f / Narms;
        float angle = 0;

        Vector3 center = new Vector3(0, 0, 0);
        Vector3 end = new Vector3();
        Vector3 rotAxis = new Vector3(0, 1, 0);
        for (int i = 0; i < Narms; i++) {
            Logger.info("Generating arm " + (i + 1));
            end.set(0, 0, radius);
            for (int j = 0; j < NperArm; j++) {
                float z = rand.nextFloat() * radius;
                float x = (float) (rand.nextGaussian() * armWidth);
                float y = (float) (rand.nextGaussian() * armWidth);

                Vector3 particle = new Vector3(x, y, z);
                particle.rotate(rotAxis, angle);

                // Rotate according to distance
                particle.rotate(rotAxis, maxRotation * particle.len() / radius);

                particles.add(particle);
                angle += stepAngle;
            }
        }

        return particles;
    }

    private static void writeToDisk(List<Vector3> gal, String dir) throws IOException {
        String filePath = dir + "galaxy_" + Narms + "_" + N + "_" + radius + "_" + armWidthRatio + "_" + maxRotation + ".txt";

        FileHandle fh = new FileHandle(filePath);
        File f = fh.file();
        if (fh.exists() && f.isFile()) {
            fh.delete();
        }

        if (fh.isDirectory()) {
            throw new RuntimeException("File is directory: " + filePath);
        }
        f.createNewFile();

        FileWriter fw = new FileWriter(filePath);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("#X Y Z");
        bw.newLine();

        for (Vector3 particle : gal) {
            bw.write(particle.x + " " + particle.y + " " + particle.z);
            bw.newLine();
        }

        bw.close();
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            String message = "";
            boolean perm = false;
            for (int i = 0; i < data.length; i++) {
                if (i == data.length - 1 && data[i] instanceof Boolean) {
                    perm = (Boolean) data[i];
                } else {
                    message += (String) data[i];
                    if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                        message += " - ";
                    }
                }
            }
            System.out.println(message);
            break;
        case JAVA_EXCEPTION:
            Exception e = (Exception) data[0];
            e.printStackTrace(System.err);
            break;
        default:
            break;
        }

    }

}
