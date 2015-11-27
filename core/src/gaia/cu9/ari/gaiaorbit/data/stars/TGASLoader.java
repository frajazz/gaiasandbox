package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
    private static final String comment = "#";

    @Override
    public List<Particle> loadData() throws FileNotFoundException {
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

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", stars.size()));
        return stars;
    }

    private void addStar(String line, List<Particle> stars) {
        String[] st = line.split(separator);

        String catalog = st[0];
        long sourceid = Parser.parseLong(st[1]);

        double ra = AstroUtils.TO_DEG * Parser.parseDouble(st[2].trim());
        double dec = AstroUtils.TO_DEG * Parser.parseDouble(st[4].trim());
        double pllx = Parser.parseDouble(st[6].trim());
        double dist = (1000d / pllx) * Constants.PC_TO_U;
        Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());

        float appmag = new Double(Parser.parseDouble(st[15].trim())).floatValue();
        float colorbv = new Double(Parser.parseDouble(st[17].trim())).floatValue();

        if (appmag < GlobalConf.data.LIMIT_MAG_LOAD) {
            float absmag = appmag;
            String name = catalog + sourceid;

            Star star = new Star(pos, appmag, absmag, colorbv, name, (float) ra, (float) dec, sourceid);
            stars.add(star);
        }
    }

}
