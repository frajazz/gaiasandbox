package gaia.cu9.ari.gaiaorbit.data.stars;

import com.badlogic.gdx.Gdx;
import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DigitalUniverseCatalogLoader extends AbstractCatalogLoader implements ICatalogLoader {
    private static final String separator = "\\s+";

    private static final float factor = 10000;
    private static final float magcut = 12f;
    private static final float distcut = 1000000f;

    @Override
    public List<CelestialBody> loadCatalog() throws FileNotFoundException {
        List<CelestialBody> stars = new ArrayList<CelestialBody>();
        InputStream data = null;
        try {
            data = FileLocator.getStream(file);
        } catch (FileNotFoundException e) {
            Logger.error(e);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(data));

        try {
            //Skip first line
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                //Add star
                if (line.startsWith("#") || line.startsWith("datavar") || line.startsWith("texture") || line.startsWith("\n") || line.isEmpty()) {
                    // Skipping line
                } else {
                    addStar(line, stars);
                }
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
        Gdx.app.log(this.getClass().getCanonicalName(), "Catalog initialized, " + stars.size() + " stars.");
        return stars;
    }

    private void addStar(String line, List<CelestialBody> stars) {
        String[] st = line.split(separator);
        float x = Parser.parseFloat(st[1]) * factor;
        float y = Parser.parseFloat(st[2]) * factor;
        float z = Parser.parseFloat(st[3]) * factor;
        float absmag = Parser.parseFloat(st[6]);
        float appmag = Parser.parseFloat(st[7]);
        float colorbv = Parser.parseFloat(st[4]);
        String name = st[19];
        Vector3d pos = new Vector3d(x, y, z);
        float dist = (float) pos.len();
        if (appmag < magcut && dist < distcut) {
            Star star = new Star(pos, absmag, appmag, colorbv, name, 0l);
            stars.add(star);
        }
    }

    @Override
    public void initialize(Properties p) {
        super.initialize(p);

    }
}
