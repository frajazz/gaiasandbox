package gaia.cu9.ari.gaiaorbit.util.g3d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Helper generic class to create icospheres.
 * @author Toni Sagrista
 *
 */
public class IcoSphereCreator extends ModelCreator
{

    private Map<Long, Integer> middlePointIndexCache;

    /**
     * Adds a vertex and its UV mapping.
     * @param p The point.
     * @param radius The radius.
     * @return
     */
    protected int addVertex(Vector3 p, float radius)
    {
        p.nor();

        addUV(p);
        // Vertex is p times the radius
        vertices.add(p.scl(radius));

        return index++;
    }

    /**
     * Implements the spherical UV mapping
     * @param p The normalized point
     */
    protected void addUV(final Vector3 p) {
        // UV
        float u = (float) (Math.atan2(p.z, p.y) / (Math.PI * 2.0) + 0.5);
        float v = (float) (Math.asin(-p.x) / Math.PI + 0.5);

        if (p.equals(new Vector3(1, 0, 0))) {
            u = 0.5f;
            v = 1f;
        }
        if (p.equals(new Vector3(-1, 0, 0))) {
            u = 0.5f;
            v = 0f;
        }

        uv.add(new Vector2(u, v));
    }

    private void addNormals() {
        // Each face has only one normal

        for (IFace face : faces) {
            // Calculate normals
            if (hardEdges) {
                // Calculate face normal, shared amongst all vertices
                Vector3 a = vertices.get(face.v()[1] - 1).cpy().sub(vertices.get(face.v()[0] - 1));
                Vector3 b = vertices.get(face.v()[2] - 1).cpy().sub(vertices.get(face.v()[1] - 1));
                normals.add(a.crs(b).nor());

                // Add index to face
                int idx = normals.size();
                face.setNormals(idx, idx, idx);

            } else {
                // Just add the vertex normal
                normals.add(vertices.get(face.v()[0] - 1).cpy().nor());
                normals.add(vertices.get(face.v()[1] - 1).cpy().nor());
                normals.add(vertices.get(face.v()[2] - 1).cpy().nor());

                // Add indeces to face
                int idx = normals.size();
                face.setNormals(idx - 2, idx - 1, idx);
            }
        }
    }

    // return index of point in the middle of p1 and p2
    private int getMiddlePoint(int p1, int p2, float radius)
    {
        // first check if we have it already
        boolean firstIsSmaller = p1 < p2;
        Long smallerIndex = (long) (firstIsSmaller ? p1 : p2);
        Long greaterIndex = (long) (firstIsSmaller ? p2 : p1);
        Long key = (smallerIndex << 32) + greaterIndex;

        if (this.middlePointIndexCache.containsKey(key))
        {
            return middlePointIndexCache.get(key);
        }

        // not in cache, calculate it
        Vector3 point1 = this.vertices.get(p1 - 1);
        Vector3 point2 = this.vertices.get(p2 - 1);
        Vector3 middle = new Vector3(
                (point1.x + point2.x) / 2.0f,
                (point1.y + point2.y) / 2.0f,
                (point1.z + point2.z) / 2.0f);

        middle.nor();
        // add vertex makes sure point is on unit sphere
        int i = addVertex(middle, radius);

        // store it, return index
        this.middlePointIndexCache.put(key, i);
        return i;
    }

    private void repairTextureWrapSeam()
    {
        int corrections = 0;

        /// whenever a vertex is split, add its original and new indices to the dictionary to avoid
        /// creating duplicates.
        Map<Integer, Integer> correctionList = new HashMap<Integer, Integer>();

        for (int i = faces.size() - 1; i >= 0; i--)
        {
            IFace face = faces.get(i);
            /// see if the texture coordinates appear in counter-clockwise order.
            /// If so, the triangle needs to be rectified.
            Vector3 v0 = new Vector3(uv.get(face.v()[0] - 1), 0);
            Vector3 v1 = new Vector3(uv.get(face.v()[1] - 1), 0);
            Vector3 v2 = new Vector3(uv.get(face.v()[2] - 1), 0);

            Vector3 a = v0.cpy().sub(v1);
            Vector3 b = v2.cpy().sub(v1);
            Vector3 cross = a.crs(b);

            if (cross.z <= 0)
            {
                /// this should only happen if the face crosses a texture boundary

                boolean corrected = false;

                // Iterate over each vertex of the face
                for (int j = 0; j < 3; j++) {
                    int index = face.v()[j] - 1;
                    Vector3 vertex = vertices.get(index);
                    Vector2 texCoord = uv.get(index);
                    if (texCoord.x <= 0.1f) {
                        if (correctionList.containsKey(index)) {
                            // Use the existing index
                            face.v()[j] = correctionList.get(index) + 1;
                        } else {
                            texCoord.x = 1;
                            corrected = true;

                            // Add corrected shit
                            uv.add(texCoord);
                            vertices.add(vertex);

                            // Modify face
                            int correctedVertexIndex = vertices.size() - 1;
                            face.v()[j] = correctedVertexIndex + 1;

                            // This has been corrected already
                            correctionList.put(index, correctedVertexIndex);

                        }
                    }

                    if (corrected)
                        corrections++;
                }
            }
        }
        System.out.println("Corrected " + corrections + " vertices");
    }

    public IcoSphereCreator() {
        super();
        this.name = "Icosphere";
    }

    public IcoSphereCreator create(float radius, int recursionLevel) {
        return create(radius, recursionLevel, false);
    }

    /**
     * Creates an ico-sphere.
     * @param radius The radius of the sphere. 
     * @param divisions The number of divisions, it must be bigger than 0.
     * @param flipNormals Whether to flip normals or not.
     * @return
     */
    public IcoSphereCreator create(float radius, int divisions, boolean flipNormals)
    {
        return create(radius, divisions, flipNormals, false);
    }

    /**
     * Creates an ico-sphere.
     * @param radius The radius of the sphere. 
     * @param divisions The number of divisions, it must be bigger than 0.
     * @param flipNormals Whether to flip normals or not.
     * @param hardEdges Whether to use hard edges (all vertices in a face have the same normal) or not.
     * @return
     */
    public IcoSphereCreator create(float radius, int divisions, boolean flipNormals, boolean hardEdges)
    {
        assert divisions >= 1 : "Recursion level must be greater than 0";
        if (divisions < 1)
            throw new AssertionError("Recursion level must be greater than 0");
        this.flipNormals = flipNormals;
        this.hardEdges = hardEdges;
        this.middlePointIndexCache = new HashMap<Long, Integer>();

        // create 12 vertices of a icosahedron
        float t = (float) ((1.0 + Math.sqrt(5.0)) / 2.0);

        addVertex(new Vector3(-1, t, 0), radius);
        addVertex(new Vector3(1, t, 0), radius);
        addVertex(new Vector3(-1, -t, 0), radius);
        addVertex(new Vector3(1, -t, 0), radius);

        addVertex(new Vector3(0, -1, t), radius);
        addVertex(new Vector3(0, 1, t), radius);
        addVertex(new Vector3(0, -1, -t), radius);
        addVertex(new Vector3(0, 1, -t), radius);

        addVertex(new Vector3(t, 0, -1), radius);
        addVertex(new Vector3(t, 0, 1), radius);
        addVertex(new Vector3(-t, 0, -1), radius);
        addVertex(new Vector3(-t, 0, 1), radius);

        // create 20 triangles of the icosahedron
        List<IFace> faces = new ArrayList<IFace>();

        // 5 faces around point 0
        addFace(faces, flipNormals, 1, 12, 6);
        addFace(faces, flipNormals, 1, 6, 2);
        addFace(faces, flipNormals, 1, 2, 8);
        addFace(faces, flipNormals, 1, 8, 11);
        addFace(faces, flipNormals, 1, 11, 12);

        // 5 adjacent faces 
        addFace(faces, flipNormals, 2, 6, 10);
        addFace(faces, flipNormals, 6, 12, 5);
        addFace(faces, flipNormals, 12, 11, 3);
        addFace(faces, flipNormals, 11, 8, 7);
        addFace(faces, flipNormals, 8, 2, 9);

        // 5 faces around point 3
        addFace(faces, flipNormals, 4, 10, 5);
        addFace(faces, flipNormals, 4, 5, 3);
        addFace(faces, flipNormals, 4, 3, 7);
        addFace(faces, flipNormals, 4, 7, 9);
        addFace(faces, flipNormals, 4, 9, 10);

        // 5 adjacent faces 
        addFace(faces, flipNormals, 5, 10, 6);
        addFace(faces, flipNormals, 3, 5, 12);
        addFace(faces, flipNormals, 7, 3, 11);
        addFace(faces, flipNormals, 9, 7, 8);
        addFace(faces, flipNormals, 10, 9, 2);

        // refine triangles
        for (int i = 1; i < divisions; i++)
        {
            List<IFace> faces2 = new ArrayList<IFace>();
            for (IFace tri : faces)
            {
                // replace triangle by 4 triangles
                int a = getMiddlePoint(tri.v()[0], tri.v()[1], radius);
                int b = getMiddlePoint(tri.v()[1], tri.v()[2], radius);
                int c = getMiddlePoint(tri.v()[2], tri.v()[0], radius);

                addFace(faces2, flipNormals, tri.v()[0], a, c);
                addFace(faces2, flipNormals, tri.v()[1], b, a);
                addFace(faces2, flipNormals, tri.v()[2], c, b);
                addFace(faces2, flipNormals, a, b, c);
            }
            faces = faces2;
        }
        this.faces = faces;

        addNormals();

        // Repair seam
        //repairTextureWrapSeam();

        return this;
    }

    public static void main(String[] args) {
        boolean flipNormals = true;
        IcoSphereCreator isc = new IcoSphereCreator();
        int recursion = 2;
        isc.create(1, recursion, flipNormals);
        try {
            File file = File.createTempFile("icosphere_" + recursion + "_", ".obj");
            OutputStream os = new FileOutputStream(file);
            isc.dumpObj(os);
            os.flush();
            os.close();
            System.out.println("Vertices: " + isc.vertices.size());
            System.out.println("Normals: " + isc.normals.size());
            System.out.println("Faces: " + isc.faces.size());
            System.out.println("Model written in: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
