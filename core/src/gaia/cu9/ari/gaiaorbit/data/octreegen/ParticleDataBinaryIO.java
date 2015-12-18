package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * Loads and writes particle data to/from our own binary format. The format is defined as follows
 * 
 * - 32 bits (int) with the number of stars, starNum
 * repeat the following starNum times (for each star)
 * - 32 bits (int) - The the length of the name, or nameLength
 * - 16 bits * nameLength (chars) - The name of the star
 * - 32 bits (float) - appmag
 * - 32 bits (float) - absmag
 * - 32 bits (float) - colorbv
 * - 32 bits (float) - x
 * - 32 bits (float) - y
 * - 32 bits (float) - z
 * - 64 bits (long)  - id
 * - 32 bits (int)   - HIP
 * - 32 bits (int)   - TYC
 * - 8 bits (byte)   - catalogSource
 * - 64 bits (long)  - pageId
 * - 32 bits (int)   - particleType
 * 
 * @author Toni Sagrista
 *
 */
public class ParticleDataBinaryIO {

    public void writeParticles(List<Particle> particles, OutputStream out) {

        try {
            // Wrap the FileOutputStream with a DataOutputStream
            DataOutputStream data_out = new DataOutputStream(out);

            // Size of stars
            data_out.writeInt(particles.size());
            for (Particle s : particles) {
                // name_length, name, appmag, absmag, colorbv, x, y, z, pmx, pmy, pmz, id, hip, tycho, sourceCatalog, pageid, type
                data_out.writeInt(s.name.length());
                data_out.writeChars(s.name);
                data_out.writeFloat(s.appmag);
                data_out.writeFloat(s.absmag);
                data_out.writeFloat(s.colorbv);
                data_out.writeFloat((float) s.pos.x);
                data_out.writeFloat((float) s.pos.y);
                data_out.writeFloat((float) s.pos.z);
                data_out.writeFloat(s.pm != null ? s.pm.x : 0f);
                data_out.writeFloat(s.pm != null ? s.pm.y : 0f);
                data_out.writeFloat(s.pm != null ? s.pm.z : 0f);
                data_out.writeLong(s.id);
                data_out.writeInt(s instanceof Star ? ((Star) s).hip : -1);
                data_out.writeInt(s instanceof Star ? ((Star) s).tycho : -1);
                data_out.writeByte(s.catalogSource);
                data_out.writeInt((int) s.octantId);
                data_out.writeInt(s.type);
            }
            data_out.close();
            out.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public List<CelestialBody> readParticles(InputStream in) throws FileNotFoundException {
        List<CelestialBody> stars = new ArrayList<CelestialBody>();
        DataInputStream data_in = new DataInputStream(in);

        try {
            // Read size of stars
            int size = data_in.readInt();

            for (int idx = 0; idx < size; idx++) {
                try {
                    // name_length, name, appmag, absmag, colorbv, x, y, z, vx, vy, vz, id, hip, tycho, catalogSource, pageid, type
                    int nameLength = data_in.readInt();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < nameLength; i++) {
                        sb.append(data_in.readChar());
                    }
                    String name = sb.toString();
                    float appmag = data_in.readFloat();
                    float absmag = data_in.readFloat();
                    float colorbv = data_in.readFloat();
                    float x = data_in.readFloat();
                    float y = data_in.readFloat();
                    float z = data_in.readFloat();
                    float vx = data_in.readFloat();
                    float vy = data_in.readFloat();
                    float vz = data_in.readFloat();
                    long id = data_in.readLong();
                    int hip = data_in.readInt();
                    int tycho = data_in.readInt();
                    byte source = data_in.readByte();
                    long pageId = data_in.readInt();
                    int type = data_in.readInt();
                    if (appmag < GlobalConf.data.LIMIT_MAG_LOAD) {
                        Vector3d pos = new Vector3d(x, y, z);
                        Vector3 vel = new Vector3(vx, vy, vz);
                        Vector3d sph = Coordinates.cartesianToSpherical(pos, new Vector3d());
                        Star s = new Star(pos, vel, appmag, absmag, colorbv, name, (float) Math.toDegrees(sph.x), (float) Math.toDegrees(sph.y), id, hip, tycho, source);
                        s.octantId = pageId;
                        s.type = type;
                        s.initialize();
                        stars.add(s);
                    }
                } catch (EOFException eof) {
                    Logger.error(eof);
                }
            }

        } catch (IOException e) {
            Logger.error(e);
        }

        return stars;
    }
}
