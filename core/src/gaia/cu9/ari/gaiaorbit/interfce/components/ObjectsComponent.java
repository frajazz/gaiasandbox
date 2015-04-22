package gaia.cu9.ari.gaiaorbit.interfce.components;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.GaiaInputController;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.TwoWayHashmap;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;

public class ObjectsComponent extends GuiComponent implements IObserver {
    boolean tree = false;
    boolean list = true;

    protected ISceneGraph sg;

    protected Actor objectsList;
    protected TextField searchBox;
    protected OwnScrollPane focusListScrollPane;

    /**
     * Tree to model equivalences
     */
    private TwoWayHashmap<SceneGraphNode, Node> treeToModel;

    public ObjectsComponent(Skin skin, Stage stage) {
	super(skin, stage);
	EventManager.instance.subscribe(this, Events.FOCUS_CHANGED);
    }

    @Override
    public void initialize() {
	searchBox = new TextField("", skin);
	searchBox.setName("search box");
	searchBox.setMessageText(txt("gui.objects.search"));
	searchBox.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof InputEvent) {
		    InputEvent ie = (InputEvent) event;
		    if (ie.getType() == Type.keyUp) {
			String text = searchBox.getText();
			if (sg.containsNode(text.toLowerCase())) {
			    SceneGraphNode node = sg.getNode(text.toLowerCase());
			    if (node instanceof CelestialBody) {
				EventManager.instance.post(Events.FOCUS_CHANGE_CMD, node, true);
			    }
			}
			GaiaInputController.pressedKeys.remove(ie.getKeyCode());
		    }
		    return true;
		}
		return false;
	    }
	});

	treeToModel = new TwoWayHashmap<SceneGraphNode, Node>();

	EventManager.instance.post(Events.POST_NOTIFICATION, txt("notif.sgtree.init"));

	if (tree) {
	    final Tree objectsTree = new Tree(skin, "bright");
	    objectsTree.setName("objects list");
	    objectsTree.setPadding(1);
	    objectsTree.setIconSpacing(1, 1);
	    objectsTree.setYSpacing(0);
	    Array<Node> nodes = createTree(sg.getRoot().children);
	    for (Node node : nodes) {
		objectsTree.add(node);
	    }
	    objectsTree.expandAll();
	    objectsTree.addListener(new EventListener() {
		@Override
		public boolean handle(Event event) {
		    if (event instanceof ChangeEvent) {
			if (objectsTree.getSelection().hasItems()) {
			    if (objectsTree.getSelection().hasItems()) {
				Node n = objectsTree.getSelection().first();
				SceneGraphNode sgn = treeToModel.getBackward(n);
				EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
				EventManager.instance.post(Events.FOCUS_CHANGE_CMD, sgn, false);
			    }

			}
			return true;
		    }
		    return false;
		}

	    });
	    objectsList = objectsTree;
	} else if (list) {
	    final com.badlogic.gdx.scenes.scene2d.ui.List<String> focusList = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin, "light");
	    focusList.setName("objects list");
	    List<CelestialBody> focusableObjects = sg.getFocusableObjects();
	    Array<String> names = new Array<String>(focusableObjects.size());
	    for (CelestialBody cb : focusableObjects) {
		// Omit stars with no proper names
		if (!cb.name.startsWith("star_") && !cb.name.startsWith("Hip ") && !cb.name.startsWith("dummy")) {
		    names.add(cb.name);
		}
	    }
	    focusList.setItems(names);
	    focusList.pack();//
	    focusList.addListener(new EventListener() {
		@Override
		public boolean handle(Event event) {
		    if (event instanceof ChangeEvent) {
			ChangeEvent ce = (ChangeEvent) event;
			Actor actor = ce.getTarget();
			String name = ((com.badlogic.gdx.scenes.scene2d.ui.List<String>) actor).getSelected();
			if (name != null) {
			    // Change focus
			    EventManager.instance.post(Events.FOCUS_CHANGE_CMD, sg.getNode(name), false);
			    // Change camera mode to focus
			    EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
			}
			return true;
		    }
		    return false;
		}
	    });
	    objectsList = focusList;
	}
	EventManager.instance.post(Events.POST_NOTIFICATION, txt("notif.sgtree.initialised"));

	if (tree || list) {
	    focusListScrollPane = new OwnScrollPane(objectsList, skin, "minimalist");
	    focusListScrollPane.setName("objects list scroll");
	    focusListScrollPane.setFadeScrollBars(false);
	    focusListScrollPane.setScrollingDisabled(true, false);

	    focusListScrollPane.setHeight(tree ? 200 : 100);
	    focusListScrollPane.setWidth(160);
	}

	VerticalGroup objectsGroup = new VerticalGroup().align(Align.left).space(3);
	objectsGroup.addActor(searchBox);
	if (focusListScrollPane != null) {
	    objectsGroup.addActor(focusListScrollPane);
	}

	component = objectsGroup;

    }

    private Array<Node> createTree(List<SceneGraphNode> nodes) {
	Array<Node> treeNodes = new Array<Node>(nodes.size());
	for (SceneGraphNode node : nodes) {
	    Label l = new Label(node.name, skin, "ui-10");
	    l.setColor(Color.BLACK);
	    Node treeNode = new Node(l);

	    if (node.children != null && !node.children.isEmpty()) {
		treeNode.addAll(createTree(node.children));
	    }

	    treeNodes.add(treeNode);
	    treeToModel.add(node, treeNode);
	}

	return treeNodes;
    }

    public void setSceneGraph(ISceneGraph sg) {
	this.sg = sg;
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case FOCUS_CHANGED:
	    // Update focus selection in focus list
	    SceneGraphNode sgn = null;
	    if (data[0] instanceof String) {
		sgn = sg.getNode((String) data[0]);
	    } else {
		sgn = (SceneGraphNode) data[0];
	    }
	    // Select only if data[1] is true
	    if (sgn != null) {
		if (tree) {
		    Tree objList = ((Tree) objectsList);
		    Node node = treeToModel.getForward(sgn);
		    objList.getSelection().set(node);
		    node.expandTo();

		    focusListScrollPane.setScrollY(focusListScrollPane.getMaxY() - node.getActor().getY());
		} else if (list) {
		    // Update focus selection in focus list
		    com.badlogic.gdx.scenes.scene2d.ui.List<String> objList = (com.badlogic.gdx.scenes.scene2d.ui.List<String>) objectsList;
		    Array<String> items = objList.getItems();
		    SceneGraphNode node = (SceneGraphNode) data[0];

		    // Select without firing events, do not use set()
		    objList.getSelection().items().clear();
		    objList.getSelection().items().add(node.name);

		    int itemIdx = items.indexOf(node.name, false);
		    if (itemIdx >= 0) {
			objList.getSelection().setProgrammaticChangeEvents(false);
			objList.setSelectedIndex(itemIdx);
			objList.getSelection().setProgrammaticChangeEvents(true);
			float itemHeight = objList.getItemHeight();
			focusListScrollPane.setScrollY(itemIdx * itemHeight);
		    }
		}
	    }
	    break;
	}

    }

}
