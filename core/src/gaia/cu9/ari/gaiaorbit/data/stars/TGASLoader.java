package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.data.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

/**
 * Loads TGAS stars in the original ASCII format:
 *  
 *  # 0    1         2      3               4      5           6      7           8            9                 10       11            12       13    14           15    16     17 18
 *  # cat  sourceId  alpha  alphaStarError  delta  deltaError  varpi  varpiError  muAlphaStar  muAlphaStarError  muDelta  muDeltaError  nObsAl   nOut  excessNoise  gMag  nuEff  C  M 
 *  
 * Source position and corresponding errors are in radian, parallax in mas and propermotion in mas/yr
 * Color and magnitude are based on 2Mass catalogue C = Jmag-Kmag and M = (VTmag+5*(1+log10(varPi/1000))) set to NaN if some data is not available
 * 
 * @author Toni Sagrista
 *
 */
public class TGASLoader extends AbstractCatalogLoader implements ISceneGraphLoader {

    private static final String separator = "\\s+";
    private static final String comma = ",";
    private static final String comment = "#";

    /** Whether to load the sourceId->HIP correspondences file **/
    private static final boolean useHIP = true;
    /** Gaia sourceId to HIP numbers csv file **/
    private static final String idCorrespondences = "data/tgas_201507/hip-sourceid-correspondences.csv";
    /** Map of Gaia sourceId to HIP id **/
    private Map<Long, Integer> sidHIPMap;

    private int sidhipfound = 0;

    @Override
    public List<Particle> loadData() throws FileNotFoundException {
        if (useHIP) {
            sidHIPMap = loadSourceidHipCorrespondences(idCorrespondences);
        }

        List<Particle> stars = new ArrayList<Particle>();
        for (String file : files) {
            FileHandle f = Gdx.files.internal(file);
            InputStream data = f.read();
            BufferedReader br = new BufferedReader(new InputStreamReader(data));

            try {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith(comment))
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
        }

        Logger.info(this.getClass().getSimpleName(), "SourceId matched to HIP in " + sidhipfound + " stars");
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", stars.size()));
        return stars;
    }

    private void addStar(String line, List<Particle> stars) {
        String[] st = line.split(separator);

        String catalog = st[0];
        int hip = -1;
        long sourceid = Parser.parseLong(st[1]);

        if (sidHIPMap != null && sidHIPMap.containsKey(sourceid)) {
            hip = sidHIPMap.get(sourceid);
            sidhipfound++;
        }
        float appmag = new Double(Parser.parseDouble(st[15].trim())).floatValue();

        if (appmag < GlobalConf.data.LIMIT_MAG_LOAD) {
            double ra = AstroUtils.TO_DEG * Parser.parseDouble(st[2].trim());
            double dec = AstroUtils.TO_DEG * Parser.parseDouble(st[4].trim());
            double pllx = Parser.parseDouble(st[6].trim());
            double dist = (1000d / pllx) * Constants.PC_TO_U;
            Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());

            // Mu_alpha Mu_delta in mas/yr
            double mualpha = Parser.parseDouble(st[8].trim()) * AstroUtils.MILLARCSEC_TO_DEG;
            double mudelta = Parser.parseDouble(st[10].trim()) * AstroUtils.MILLARCSEC_TO_DEG;

            // Proper motion vector = (pos+dx) - pos
            Vector3d pm = Coordinates.sphericalToCartesian(Math.toRadians(ra + mualpha), Math.toRadians(dec + mudelta), dist, new Vector3d());
            pm.sub(pos);

            Vector3 pmfloat = pm.toVector3();

            float colorbv = new Double(Parser.parseDouble(st[17].trim())).floatValue();

            float absmag = appmag;
            String name = Long.toString(sourceid);

            Star star = new Star(pos, appmag, absmag, colorbv, name, (float) ra, (float) dec, sourceid, hip, (byte) 1);
            if (runFiltersAnd(star))
                stars.add(star);

        }
    }

    private Map<Long, Integer> loadSourceidHipCorrespondences(String file) {
        Map<Long, Integer> result = new HashMap<Long, Integer>();

        FileHandle f = Gdx.files.internal(file);
        InputStream data = f.read();
        BufferedReader br = new BufferedReader(new InputStreamReader(data));
        try {
            // skip first line with headers
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(comment))
                    //Add correspondence
                    addCorrespondence(line, result);
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

        return result;
    }

    private void addCorrespondence(String line, Map<Long, Integer> map) {
        String[] st = line.split(comma);
        int hip = Parser.parseInt(st[2].trim());
        long sourceId = Parser.parseLong(st[3].trim());

        map.put(sourceId, hip);
    }

}
