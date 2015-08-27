package gaia.cu9.ari.gaiaorbit.data.constel;

import gaia.cu9.ari.gaiaorbit.data.stars.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ConstellationBoundaries;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class ConstelBoundariesLoader<T extends SceneGraphNode> implements ISceneGraphLoader {
    private static final String separator = "\\t";
    private static final boolean LOAD_INTERPOLATED = true;
    private static final int INTERPOLATED_MOD = 3;
    private String[] files;

    @Override
    public void initialize(String[] files) throws RuntimeException {
        this.files = files;
    }

    @Override
    public List<ConstellationBoundaries> loadData() {
        List<ConstellationBoundaries> boundaries = new ArrayList<ConstellationBoundaries>();
        try {
            int n = 0;
            for (String f : files) {
                Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.datafile", f));
                // load constellations
                FileHandle file = Gdx.files.internal(f);
                BufferedReader br = new BufferedReader(new InputStreamReader(file.read()));
                try {
                    //Skip first line
                    String line;
                    ConstellationBoundaries boundary = new ConstellationBoundaries();
                    boundary.ct = ComponentType.Boundaries;
                    List<List<Vector3d>> list = new ArrayList<List<Vector3d>>();
                    List<Vector3d> buffer = new ArrayList<Vector3d>(4);
                    String lastName = new String();
                    int interp = 0;
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("#")) {
                            String[] tokens = line.split(separator);

                            String name = tokens[2];
                            String type = tokens.length > 3 ? tokens[3] : "O";

                            if (!name.equals(lastName)) {
                                // New line
                                list.add(buffer);
                                buffer = new ArrayList<Vector3d>(20);
                                lastName = name;
                            }

                            if (type.equals("I")) {
                                interp++;
                            }

                            if ((type.equals("I") && LOAD_INTERPOLATED && interp % INTERPOLATED_MOD == 0) || type.equals("O")) {
                                // Load the data
                                double ra = Parser.parseDouble(tokens[0].trim()) * 15d;
                                double dec = Parser.parseDouble(tokens[1].trim());

                                double dist = 1 * Constants.AU_TO_U;

                                Vector3d point = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());
                                buffer.add(point);
                                n++;
                            }

                        }
                    }
                    list.add(buffer);
                    boundary.setBoundaries(list);
                    boundaries.add(boundary);
                } catch (IOException e) {
                    Logger.error(e, this.getClass().getSimpleName());
                }
            }

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.boundaries.init", n));

        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
        }
        return boundaries;
    }
}
