package gaia.cu9.ari.gaiaorbit.data.constel;

import gaia.cu9.ari.gaiaorbit.data.ISceneGraphNodeProvider;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.Constellation;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;

public class ConstellationsLoader<T extends SceneGraphNode> implements ISceneGraphNodeProvider {
    private static final String separator = "\\t|,";
    FileHandle file;

    public void initialize(FileHandle file) {
        this.file = file;
    }

    @Override
    public List<Constellation> loadObjects() {
        List<Constellation> constellations = new ArrayList<Constellation>();
        try {
            // load constellations
            BufferedReader br = new BufferedReader(new InputStreamReader(file.read()));

            try {
                //Skip first line
                String lastName = "";
                List<long[]> partial = null;
                long lastid = -1;
                String line;
                String name = null;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        String[] tokens = line.split(separator);
                        name = tokens[0].trim();

                        if (!lastName.isEmpty() && !name.equals("JUMP") && !name.equals(lastName)) {
                            // We finished a constellation object
                            Constellation cons = new Constellation(lastName, SceneGraphNode.ROOT_NAME);
                            cons.ct = ComponentType.Constellations;
                            cons.ids = partial;
                            constellations.add(cons);
                            partial = null;
                            lastid = -1;
                        }

                        if (partial == null) {
                            partial = new ArrayList<long[]>();
                        }

                        // Break point sequence
                        if (name.equals("JUMP") && tokens[1].trim().equals("JUMP")) {
                            lastid = -1;
                        } else {

                            long newid = Parser.parseLong(tokens[1].trim());
                            if (lastid > 0) {
                                partial.add(new long[] { lastid, newid });
                            }
                            lastid = newid;

                            lastName = name;
                        }
                    }
                }
                // Add last
                if (!lastName.isEmpty() && !name.equals("JUMP")) {
                    // We finished a constellation object
                    Constellation cons = new Constellation(lastName, SceneGraphNode.ROOT_NAME);
                    cons.ct = ComponentType.Constellations;
                    cons.ids = partial;
                    constellations.add(cons);
                    partial = null;
                    lastid = -1;
                }
            } catch (IOException e) {
                Logger.error(e);
            }

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.constellations.init", constellations.size()));

        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
            Logger.error(e);
        }
        return constellations;
    }
}
