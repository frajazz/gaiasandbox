package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the HYG catalog in CSV format
 * @author Toni Sagrista
 *
 */
public class HYGCSVLoader extends AbstractCatalogLoader implements ICatalogLoader {
    private static final String separator = "\t";

    @Override
    public List<Star> loadCatalog() throws FileNotFoundException {
        List<Star> stars = new ArrayList<Star>();
        InputStream data = file.read();
        BufferedReader br = new BufferedReader(new InputStreamReader(data));

        try {
            //Skip first line
            String[] header = br.readLine().split(separator);

            for (String head : header) {
                head = head.trim();
            }
            String line;
            while ((line = br.readLine()) != null) {
                //Add star
                addStar(line, stars);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Logger.error(e);
            }

        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", stars.size()));
        return stars;
    }

    private void addStar(String line, List<Star> stars) {
        String[] st = line.split(separator);
        double ra = MathUtilsd.lint(Parser.parseDouble(st[7].trim()), 0, 24, 0, 360);
        double dec = Parser.parseDouble(st[8].trim());
        double dist = Parser.parseDouble(st[9]) * Constants.PC_TO_U;
        Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());

        float appmag = Parser.parseFloat(st[10].trim());
        float colorbv = 0f;

        if (st.length >= 14 && !st[13].trim().isEmpty()) {
            colorbv = Parser.parseFloat(st[13].trim());
        } else {
            colorbv = 1;
        }

        if (appmag < GlobalConf.data.LIMIT_MAG_LOAD) {
            float absmag = Parser.parseFloat(st[11].trim());
            String name = null;
            if (!st[6].trim().isEmpty()) {
                name = st[6].trim().replaceAll("\\s+", " ");
            } else if (!st[5].trim().isEmpty()) {
                name = st[5].trim().replaceAll("\\s+", " ");
            } else if (!st[4].trim().isEmpty()) {
                name = st[4].trim().replaceAll("\\s+", " ");
            } else if (!st[2].trim().isEmpty()) {
                name = "Hip " + st[1].trim();
            }
            long starid = Parser.parseLong(st[0].trim());

            if (name != null && name.equalsIgnoreCase("Betelgeuse")) {
                int ad = 34;
                ad += 213;
            }

            Star star = new Star(pos, appmag, absmag, colorbv, name, (float) ra, (float) dec, (int) starid);
            stars.add(star);
        }
    }

}
