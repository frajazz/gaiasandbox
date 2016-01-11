package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.data.constel.ConstellationsLoader;
import gaia.cu9.ari.gaiaorbit.data.octreegen.BrightestStars;
import gaia.cu9.ari.gaiaorbit.data.octreegen.IAggregationAlgorithm;
import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.Constellation;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class ConstellationHIPUpdater implements IObserver {

    public static void main(String[] args) {
        try {
            Gdx.files = new Lwjgl3Files();

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            ConfInit.initialize(new DesktopConfInit(new FileInputStream(new File("../android/assets/conf/global.properties")), new FileInputStream(new File("../android/assets/data/dummyversion"))));

            I18n.initialize(new FileHandle("/home/tsagrista/git/gaiasandbox/android/assets/i18n/gsbundle"));

            // Add notif watch
            EventManager.instance.subscribe(new ConstellationHIPUpdater(), Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            updateConstellations();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateConstellations() throws IOException {
        IAggregationAlgorithm<Particle> aggr;
        try {
            aggr = ClassReflection.newInstance(BrightestStars.class);
        } catch (ReflectionException e) {
            e.printStackTrace(System.err);
            return;
        }

        /** LOAD CONSTEL **/
        ConstellationsLoader<Constellation> constel = new ConstellationsLoader<Constellation>();
        constel.initialize(new String[] { "data/constel.csv" });

        /** LOAD HIP **/
        HYGBinaryLoader hyg = new HYGBinaryLoader();
        hyg.initialize(new String[] { "data/hygxyz.bin" });

        List<Particle> catalog = hyg.loadData();
        List<Star> stars = new ArrayList<Star>(catalog.size());
        for (Particle p : catalog)
            if (p instanceof Star)
                stars.add((Star) p);

        List<Constellation> cons = (List<Constellation>) constel.loadData();

        Map<Integer, Star> idmap = new HashMap<Integer, Star>();
        for (Star s : stars) {
            idmap.put((int) s.id, s);
        }

        for (Constellation constellation : cons) {

            List<int[]> oldids = constellation.ids;
            List<int[]> newids = new ArrayList<int[]>(oldids.size());

            for (int[] oids : oldids) {
                int[] nids = new int[oids.length];
                for (int i = 0; i < oids.length; i++) {
                    int oldid = oids[i];
                    Star s = idmap.get(oldid);
                    if (s != null)
                        nids[i] = s.hip;
                    else
                        nids[i] = oldid;

                    Logger.info("id/hip: " + oldid + "/" + nids[i]);
                }
                newids.add(nids);
            }

            // replace reference
            constellation.ids = newids;
        }

        Logger.info(cons.size() + " constellations processed");

        String temp = System.getProperty("java.io.tmpdir");

        long tstamp = System.currentTimeMillis();

        /** WRITE METADATA **/
        File constelfile = new File(temp, "constel_" + tstamp + ".csv");
        if (constelfile.exists()) {
            constelfile.delete();
        }
        constelfile.createNewFile();

        BufferedWriter bw = new BufferedWriter(new FileWriter(constelfile));

        bw.write("#constelname,HIP");
        bw.newLine();

        int lastend = -1;

        for (Constellation constellation : cons) {

            List<int[]> ids = constellation.ids;
            for (int[] idlist : ids) {
                if (lastend >= 0 && lastend != idlist[0]) {
                    bw.write("JUMP,JUMP");
                    bw.newLine();
                }
                if (lastend != idlist[0]) {
                    bw.write(constellation.name + "," + idlist[0]);
                    bw.newLine();
                }

                bw.write(constellation.name + "," + idlist[1]);
                bw.newLine();

                lastend = idlist[1];

            }
        }
        bw.close();

        Logger.info("Constellations written to " + constelfile.getAbsolutePath());

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
        }

    }

}
