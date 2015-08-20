package gaia.cu9.ari.gaiaorbit.data.bean;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBean {
    protected List<SceneGraphNode> list;

    public List<SceneGraphNode> list() {
        return list != null ? list : new ArrayList<SceneGraphNode>(1);
    }

    public int size() {
        return list != null ? list.size() : 0;
    }

    public void addAll(List<? extends SceneGraphNode> l) {
        if (list == null) {
            list = new ArrayList<SceneGraphNode>(l.size());
        }
        list.addAll(l);
    }
}
