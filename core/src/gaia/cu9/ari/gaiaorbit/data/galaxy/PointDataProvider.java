package gaia.cu9.ari.gaiaorbit.data.galaxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

public class PointDataProvider {

    public List<Vector3> loadData(String file) {
        List<Vector3> pointData = new ArrayList<Vector3>();
        FileHandle f = Gdx.files.internal(file);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.read()));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    // Read line
                    String[] tokens = line.split("\\s+");
                    Vector3 point = new Vector3(Parser.parseFloat(tokens[0]), Parser.parseFloat(tokens[1]), Parser.parseFloat(tokens[2]));
                    pointData.add(point);
                }
            }

            br.close();

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size(), file));
        } catch (Exception e) {
            Logger.error(e, PointDataProvider.class.getName());
        }

        return pointData;
    }
}
