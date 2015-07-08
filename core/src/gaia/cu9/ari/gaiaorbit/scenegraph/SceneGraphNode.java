package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.IPosition;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * A scene graph entity.
 * @author Toni Sagrista
 *
 * @param <T>
 */
public class SceneGraphNode implements ISceneGraphNode, IPosition {
    public static final String ROOT_NAME = "Universe";

    /** Static Thread local auxiliary Vector3d **/
    protected static ThreadLocal<Vector3d> auxVector3d = new ThreadLocal<Vector3d>() {
        @Override
        protected Vector3d initialValue() {
            return new Vector3d();
        }
    };

    /** Static Thread local auxiliary Vector3f **/
    protected static ThreadLocal<Vector3> auxVector3f = new ThreadLocal<Vector3>() {
        @Override
        protected Vector3 initialValue() {
            return new Vector3();
        }
    };

    /** Static Thread local auxiliary Vector3f **/
    protected static ThreadLocal<Vector3> aux2Vector3f = new ThreadLocal<Vector3>() {
        @Override
        protected Vector3 initialValue() {
            return new Vector3();
        }
    };

    /** Static Thread local auxiliary Vector2d **/
    protected static ThreadLocal<Vector2d> auxVector2d = new ThreadLocal<Vector2d>() {
        @Override
        protected Vector2d initialValue() {
            return new Vector2d();
        }
    };

    /**
     * Describes to which render group this node belongs at a particular time step.
     */
    public static enum RenderGroup {
        /** Foreground models **/
        MODEL_F(0),
        /** Background models **/
        MODEL_B(1),
        /** Shader - back (stars) **/
        SHADER(2),
        /** Shader - front (planets, satellites...) **/
        SHADER_F(3),
        /** Single pixel **/
        POINT(4),
        /** Line **/
        LINE(5),
        /** Annotations of background models (grids) **/
        MODEL_B_ANNOT(6),
        /** Atmospheres of foreground models (planets) **/
        MODEL_F_ATM(7),
        /** Label **/
        LABEL(8),
        /** Model star **/
        MODEL_S(9),

        /** None **/
        NONE(-1);

        private int index;

        private RenderGroup(int index) {
            this.index = index;
        }

        public boolean is(Bits rgmask) {
            return (index < 0 && rgmask.isEmpty()) || rgmask.get(index);
        }

        /**
         * Adds the given render groups to the given Bits mask.
         * @param rgmask
         * @param rgs
         * @return
         */
        public static Bits add(Bits rgmask, RenderGroup... rgs) {
            for (RenderGroup rg : rgs) {
                rgmask.set(rg.index);
            }
            return rgmask;
        }

        /**
         * Sets the given Bits mask to the given render groups.
         * @param rgmask
         * @param rgs
         * @return
         */
        public static Bits set(Bits rgmask, RenderGroup... rgs) {
            rgmask.clear();
            return add(rgmask, rgs);
        }

    }

    /** Reference to scene graph **/
    public static ISceneGraph sg;

    /** The identifier **/
    public Long id;

    /**
     * The parent entity.
     */
    public SceneGraphNode parent;
    /**
     * The parent name
     */
    public String parentName = null;

    /**
     * List of children entities.
     */
    public List<SceneGraphNode> children;

    /**
     * Transform object. Contains the transformations that will be applied to the children.
     */
    public Transform transform;

    /**
     * Local transform matrix. Contains the transform matrix and the transformations
     * that will be applied to this object and not to its children.
     */
    public Matrix4 localTransform;

    /**
     * This transform stores only the orientation of the object. For example in planets, it stores
     * their orientation with respect to their equatorial plane, but not other transformations applied
     * to the object such as the size or the rotation angle at the time.
     */
    public Matrix4d orientation;

    /**
     * The name of the node, if any.
     */
    public String name;

    /**
     * The total number of descendants under this node.
     */
    public int numChildren;

    /**
     * Flag indicating whether we compute the nested elements or not.
     */
    public boolean expandedFlag = true;

    /**
     * Flag indicating whether the object has been computed in this step.
     */
    public boolean computed = true;

    /** 
     * The transparency value (alpha)
     */
    public float opacity = 1f;

    /**
     * For visibility toggles
     */
    public ComponentType ct;

    public SceneGraphNode() {
        // Identity
        this.transform = new Transform();
        this.transform.position = new Vector3d();
    }

    public SceneGraphNode(long id) {
        this();
        this.id = id;
    }

    public SceneGraphNode(ComponentType ct) {
        super();
        this.ct = ct;
    }

    public SceneGraphNode(String name, SceneGraphNode parent) {
        this();
        this.name = name;
        this.parent = parent;
    }

    public SceneGraphNode(String name) {
        this(name, null);
    }

    public SceneGraphNode(SceneGraphNode parent) {
        this(null, parent);
    }

    /**
     * Adds the given SceneGraphNode list as children to this node.
     * @param children
     */
    @SafeVarargs
    public final void add(SceneGraphNode... children) {
        if (this.children == null) {
            initChildren(this.parent == null || this instanceof AbstractOctreeWrapper ? 300000 : children.length * 5, this.parent == null ? 1000 : children.length);
        }
        for (int i = 0; i < children.length; i++) {
            SceneGraphNode child = children[i];
            this.children.add(child);
            child.parent = this;
        }
        numChildren += children.length;
    }

    /**
     * Adds a child to the given node and updates the number of children in this node
     * and in all ancestors.
     * @param child The child node to add.
     * @param updateAncestorCount Whether to update the ancestors number of children.
     */
    public final void addChild(SceneGraphNode child, boolean updateAncestorCount) {
        if (this.children == null) {
            initChildren(this.parent == null ? 200 : 5, this.parent == null ? 100 : 1);
        }
        this.children.add(child);
        child.parent = this;
        numChildren++;

        if (updateAncestorCount) {
            // Update num children in ancestors
            SceneGraphNode ancestor = this.parent;
            while (ancestor != null) {
                ancestor.numChildren++;
                ancestor = ancestor.parent;
            }
        }
    }

    /**
     * Removes the given child from this node, if it exists.
     * @param child
     * @param updateAncestorCount
     */
    public final void removeChild(SceneGraphNode child, boolean updateAncestorCount) {
        if (this.children.contains(child)) {
            this.children.remove(child);
            child.parent = null;
            numChildren--;
            if (updateAncestorCount) {
                // Update num children in ancestors
                SceneGraphNode ancestor = this.parent;
                while (ancestor != null) {
                    ancestor.numChildren--;
                    ancestor = ancestor.parent;
                }
            }
        }
    }

    /**
     * Adds a child to the given node and updates the number of children in this node
     * and in all ancestors.
     * @param child The child node to add.
     * @param updateAncestorCount Whether to update the ancestors number of children.
     * @param numChildren The number of children this will hold.
     */
    public final void addChild(SceneGraphNode child, boolean updateAncestorCount, int numChildren) {
        if (this.children == null) {
            initChildren(numChildren, 1);
        }
        this.children.add(child);
        child.parent = this;
        numChildren++;

        if (updateAncestorCount) {
            // Update num children in ancestors
            SceneGraphNode ancestor = this.parent;
            while (ancestor != null) {
                ancestor.numChildren++;
                ancestor = ancestor.parent;
            }
        }
    }

    /**
     * Adds the given list of children as child nodes.
     * @param children
     */
    public void add(List<? extends SceneGraphNode> children) {
        add(children.toArray(new SceneGraphNode[children.size()]));
    }

    /**
     * Inserts the list of nodes under the parents that match each node's name.
     * @param nodes
     */
    public final void insert(List<? extends SceneGraphNode> nodes) {
        Iterator<? extends SceneGraphNode> it = nodes.iterator();

        // Insert top level
        while (it.hasNext()) {
            SceneGraphNode node = it.next();
            if ((this.name == null && node.parentName == null) || (this.name != null && this.name.equals(node.parentName))) {
                // Match, add and remove from list
                addChild(node, false);
                node.setUp();
                it.remove();
            }
        }

        // Add to children
        if (children != null) {
            for (SceneGraphNode child : children) {
                child.insert(nodes);
            }
        }

    }

    private void initChildren(int size, int grow) {
        children = new Vector<SceneGraphNode>(size, grow);
    }

    public SceneGraphNode getNode(String name) {
        if (this.name != null && this.name.equals(name)) {
            return this;
        } else if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode child = children.get(i);
                SceneGraphNode n = child.getNode(name);
                if (n != null) {
                    return n;
                }
            }
        }
        return null;
    }

    public SceneGraphNode getNode(Long id) {
        if (this.id != null && this.id.equals(id)) {
            return this;
        } else if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode child = children.get(i);
                SceneGraphNode n = child.getNode(id);
                if (n != null) {
                    return n;
                }
            }
        }
        return null;
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        this.opacity = opacity;
        transform.set(parentTransform);

        // Update with translation/rotation/etc
        updateLocal(time, camera);

        if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                children.get(i).update(time, transform, camera);
            }
        }
    }

    /**
     * Updates the transform matrix with the transformations that will apply to the children and the
     * local transform matrix with the transformations that will apply only to this object.
     * @param time
     */
    protected void updateLocal(ITimeFrameProvider time, ICamera camera) {
    }

    public void initialize() {
        ct = ComponentType.Others;
    }

    public void doneLoading(AssetManager manager) {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParent(String parentName) {
        this.parentName = parentName;
    }

    public void dispose() {
        if (children != null)
            for (SceneGraphNode child : children) {
                child.dispose();
            }
    }

    /**
     * Adds all the children that are focusable objects to the list.
     * @param list
     */
    public void addFocusableObjects(List<CelestialBody> list) {
        if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode child = children.get(i);
                child.addFocusableObjects(list);
            }
        }
    }

    public void addNodes(List<SceneGraphNode> nodes) {
        nodes.add(this);
        if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode child = children.get(i);
                child.addNodes(nodes);
            }
        }
    }

    public void setUp() {

    }

    public void setCt(String ct) {
        this.ct = ComponentType.valueOf(ct);
    }

    public ComponentType getComponentType() {
        return ct;
    }

    /** 
     * Gets the number of nodes contained in this node, including itself.
     * @return
     */
    public int getAggregatedChildren() {
        return numChildren + 1;
    }

    /**
     * Returns the number of nodes of the specified class contained in this node.
     * @return
     */
    public int getNumNodes(Class<? extends SceneGraphNode> clazz) {
        int n = 0;
        if (clazz.isInstance(this)) {
            n = 1;
        }
        if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode child = children.get(i);
                n += child.getNumNodes(clazz);
            }
        }
        return n;
    }

    public <T extends SceneGraphNode> void getNodes(Class<T> clazz, List<T> l) {
        if (clazz.isInstance(this)) {
            l.add(clazz.cast(this));
        }
        if (children != null) {
            int size = children.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode child = children.get(i);
                child.getNodes(clazz, l);
            }
        }
    }

    public <T extends SceneGraphNode> T getLineCopy() {
        if (this.parent != null) {
            T parentCopy = parent.getLineCopy();
            T me = getSimpleCopy();
            parentCopy.addChild(me, false, 1);
            return me;
        } else {
            return getSimpleCopy();
        }

    }

    /**
     * Gets a copy of this object but does not copy its parent or children.
     * @return
     */
    public <T extends SceneGraphNode> T getSimpleCopy() {
        Pool<? extends SceneGraphNode> pool = Pools.get(this.getClass());
        T copy = (T) pool.obtain();
        copy.name = this.name;
        copy.parentName = this.parentName;
        return copy;
    }

    public SceneGraphNode getRoot() {
        if (this.parent == null) {
            return this;
        } else {
            return this.parent.getRoot();
        }
    }

    @Override
    public String toString() {
        if (name != null)
            return name;
        return super.toString();
    }

    public void returnToPool() {
        if (this.children != null)
            this.children.clear();
        Class clazz = this.getClass();
        Pools.get(clazz).free(this);
    }

    /**
     * Sets the computed flag of the list of nodes and their children to the given value.
     * @param nodes List of nodes to set the flag to. May be null.
     * @param computed The computed value.
     */
    public void setComputedFlag(List<SceneGraphNode> nodes, boolean computed) {
        if (nodes != null) {
            int size = nodes.size();
            for (int i = 0; i < size; i++) {
                SceneGraphNode node = nodes.get(i);
                node.computed = computed;
                setComputedFlag(node.children, computed);
            }
        }
    }

    protected void addToRender(IRenderable renderable, RenderGroup rg) {
        if (SceneGraphRenderer.visible[ct.ordinal()] || (!SceneGraphRenderer.visible[ct.ordinal()] && SceneGraphRenderer.alphas[ct.ordinal()] > 0)) {
            SceneGraphRenderer.render_lists.get(rg).add(renderable, ThreadIndexer.inst().i());
        }
    }

    protected boolean isInRender(IRenderable renderable, RenderGroup rg) {
        return SceneGraphRenderer.render_lists.get(rg).contains(renderable, ThreadIndexer.inst().i());
    }

    public SceneGraphNode getFirstAncestorOfType(Class<? extends SceneGraphNode> clazz) {
        if (this.getClass().isAssignableFrom(clazz))
            return this;
        else if (parent != null) {
            return parent.getFirstAncestorOfType(clazz);
        } else {
            return null;
        }
    }

    @Override
    public int getStarCount() {
        return 0;
    }

    @Override
    public Object getStars() {
        return null;
    }

    @Override
    public Vector3d getPosition() {
        return null;
    }
}
