package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.gui.swing.components.IconTreeNode;
import gaia.cu9.ari.gaiaorbit.gui.swing.components.OwnDateField;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.TwoWayHashmap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import com.alee.extended.button.WebSwitch;
import com.alee.extended.date.DateSelectionListener;
import com.alee.extended.image.WebImage;
import com.alee.extended.layout.ToolbarLayout;
import com.alee.extended.panel.WebOverlay;
import com.alee.extended.statusbar.WebMemoryBar;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;

public class Gui implements IObserver {

    LwjglCanvas canvas;
    JFrame frame;
    JLabel fpsLabel;
    JTree tree;
    DefaultMutableTreeNode top;
    DefaultTreeModel model;
    JTabbedPane tabbedPane;
    JPanel mainPanel;
    boolean fullscreen = false;
    ISceneGraph sg;

    /** ALL WIDGETS **/
    WebSwitch playPause;
    JSpinner pace, time;
    OwnDateField date;
    JComboBox<CameraMode> cameraMode;
    JSlider fovSlider;
    JCheckBox lockCameraCheckbox, gaiaScanToggle, colorScanToggle, onlyObservedToggle;
    Map<ComponentType, WebToggleButton> visibilityButtons;
    JSlider starBrightnessSlider, ambientSlider, bloomSlider;
    JScrollPane treeScrollPane;
    WebTextField searchInput;

    boolean[] updateTime;

    public Gui(JFrame fr, LwjglCanvas cv) {
	this.frame = fr;
	this.canvas = cv;
	this.frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		frame.remove(canvas.getCanvas());
		frame.dispose();
	    }
	});

	this.canvas.getCanvas().addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		if (tabbedPane != null) {
		    if (!canvas.getCanvas().hasFocus()) {
			canvas.getCanvas().requestFocus();
		    }
		}
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		if (tabbedPane != null) {
		    if (!tabbedPane.hasFocus()) {
			tabbedPane.requestFocus();
		    }
		}
	    }

	});

    }

    public void initialize(ISceneGraph sg) {
	updateTime = new boolean[] { true, true };

	final Container container = frame.getContentPane();
	container.setLayout(new BorderLayout());

	//Where the GUI is created:

	/** MENU BAR **/

	MainMenuBar menuBar = new MainMenuBar(this);
	frame.setJMenuBar(menuBar);

	/** STATUS BAR **/
	// Simple status bar
	WebStatusBar statusBar = new WebStatusBar();

	// Simple label
	fpsLabel = new JLabel();
	statusBar.add(fpsLabel);

	// Simple memory bar
	WebMemoryBar memoryBar = new WebMemoryBar();
	memoryBar.setPreferredWidth(memoryBar.getPreferredSize().width + 20);
	statusBar.add(memoryBar, ToolbarLayout.END);

	frame.add(statusBar, BorderLayout.SOUTH);

	/** LEFT PANE - TABS **/
	tabbedPane = new JTabbedPane()
	{
	    @Override
	    public Dimension getPreferredSize()
	    {
		final Dimension ps = super.getPreferredSize();
		ps.width = 150;
		return ps;
	    }
	};

	/** TREE **/
	top = new IconTreeNode("Scene graph", true, IconManager.get("tree"));
	model = new DefaultTreeModel(top);
	tree = new JTree(model);
	tree.setCellRenderer(new IconTreeCellRenderer());
	tree.setEditable(false);

	// Listener
	MouseListener ml = new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		if (selRow != -1) {
		    if (e.getClickCount() == 1) {
			mySingleClick(selRow, selPath);
		    }
		    else if (e.getClickCount() == 2) {
			myDoubleClick(selRow, selPath);
		    }
		}
	    }
	};
	tree.addMouseListener(ml);
	loadTree(sg);

	treeScrollPane = new JScrollPane(tree);
	treeScrollPane.setPreferredSize(new Dimension(300, 800));

	// Search input
	searchInput = new WebTextField(20);
	searchInput.setMargin(0, 0, 0, 2);
	searchInput.setInputPrompt("Search...");
	searchInput.setInputPromptFont(searchInput.getFont().deriveFont(Font.ITALIC));
	searchInput.setTrailingComponent(new WebImage(IconManager.get("search")));
	searchInput.getDocument().addDocumentListener(new DocumentListener() {
	    public void changedUpdate(DocumentEvent e) {
		warn();
	    }

	    public void removeUpdate(DocumentEvent e) {
		warn();
	    }

	    public void insertUpdate(DocumentEvent e) {
		warn();
	    }

	    public void warn() {
		String text = searchInput.getText();
		if (stringNode.containsKey(text.toLowerCase())) {
		    SceneGraphNode node = stringNode.get(text.toLowerCase());
		    if (node instanceof CelestialBody) {
			EventManager.instance.post(Events.FOCUS_CHANGE_CMD, node, false);
			selectNodeInTree(node);
		    }
		}
	    }
	});

	JPanel treePanel = new JPanel(new BorderLayout());
	treePanel.add(searchInput, BorderLayout.NORTH);
	treePanel.add(treeScrollPane, BorderLayout.CENTER);

	/** CONTROLS **/

	/* TIME */
	JPanel timePanel = new JPanel(new MigLayout("", "[pref!][grow,fill]", "[]2[]"));
	timePanel.setBorder(new TitledBorder("Time"));
	playPause = new WebSwitch(false);
	playPause.setToolTipText("Play and pause the time simulation");
	playPause.setMaximumSize(new Dimension(70, 50));
	playPause.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.instance.post(Events.TOGGLE_TIME_CMD, playPause.isSelected(), true);
	    }
	});

	pace = new JSpinner(new SpinnerNumberModel(0.01000d, -1000d, 1000d, 0.1000d));
	pace.setMinimumSize(new Dimension(100, 0));
	pace.setEditor(new JSpinner.NumberEditor(pace, "###0.0#####"));
	pace.setBorder(null);
	pace.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		EventManager.instance.post(Events.PACE_CHANGE_CMD, ((Double) pace.getValue()).floatValue());
	    }
	});

	date = new OwnDateField();
	date.addDateSelectionListener(new DateSelectionListener() {
	    @Override
	    public void dateSelected(Date date) {
		if (updateTime[0]) {
		    SpinnerDateModel sdm = (SpinnerDateModel) time.getModel();
		    Calendar hourCalendar = new GregorianCalendar();
		    hourCalendar.setTime(sdm.getDate());

		    Calendar dateCalendar = new GregorianCalendar();
		    dateCalendar.setTime(date);

		    dateCalendar.set(Calendar.HOUR_OF_DAY, hourCalendar.get(Calendar.HOUR_OF_DAY));
		    dateCalendar.set(Calendar.MINUTE, hourCalendar.get(Calendar.MINUTE));
		    dateCalendar.set(Calendar.SECOND, hourCalendar.get(Calendar.SECOND));

		    Date d = new Date(dateCalendar.getTimeInMillis());
		    EventManager.instance.post(Events.TIME_CHANGE_CMD, d);
		} else {
		    updateTime[0] = true;
		}
	    }
	});
	time = new JSpinner(new SpinnerDateModel());
	JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(time, "HH:mm:ss");
	time.setEditor(timeEditor);
	time.setBorder(null);
	time.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		if (updateTime[1]) {
		    // This is the date DD:MM:YY
		    Calendar dateCalendar = new GregorianCalendar();
		    dateCalendar.setTime(date.getDate());

		    // Output calendar, it has the correct HH:mm:ss
		    SpinnerDateModel sdm = (SpinnerDateModel) time.getModel();
		    Calendar hourCalendar = new GregorianCalendar();
		    hourCalendar.setTime(sdm.getDate());

		    hourCalendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
		    hourCalendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
		    hourCalendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));

		    Date d = new Date(hourCalendar.getTimeInMillis());
		    EventManager.instance.post(Events.TIME_CHANGE_CMD, d);
		} else {
		    updateTime[1] = true;
		}
	    }
	});

	JPanel dateTime = new JPanel(new MigLayout("fill", "[][grow,fill]", ""));
	dateTime.add(date);
	dateTime.add(time);

	WebLabel timeSpeedLabel = new WebLabel("Time speed");
	timeSpeedLabel.setToolTipText("Number of hours in the simulation per real time second");
	WebOverlay timeSpeedPanel = new WebOverlay();
	timeSpeedPanel.setComponent(timeSpeedLabel);
	timeSpeedPanel.setToolTipText("Number of hours in the simulation per real time second");

	timePanel.add(new JLabel("Time simulation"));
	timePanel.add(playPause, "wrap");
	timePanel.add(timeSpeedPanel);
	timePanel.add(pace, "wrap");
	timePanel.add(new JLabel("Current date and time"), "wrap");
	timePanel.add(dateTime, "span,wrap");

	/* CAMERA */
	JPanel cameraPanel = new JPanel(new MigLayout("", "[][grow,fill]", ""));
	cameraPanel.setBorder(new TitledBorder("Camera"));

	CameraMode[] cms = CameraMode.values();
	cameraMode = new JComboBox<CameraMode>(cms);
	cameraMode.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		CameraMode mode = (CameraMode) cameraMode.getSelectedItem();
		EventManager.instance.post(Events.CAMERA_MODE_CMD, mode);
	    }
	});

	fovSlider = new JSlider();
	fovSlider.setMinimum(Constants.MIN_FOV);
	fovSlider.setMaximum(Constants.MAX_FOV);
	fovSlider.setMinorTickSpacing(20);
	fovSlider.setMajorTickSpacing(70);
	fovSlider.setValue((int) GlobalConf.scene.CAMERA_FOV);
	fovSlider.setPaintTicks(true);
	fovSlider.setPaintLabels(true);
	fovSlider.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		EventManager.instance.post(Events.FOV_CHANGED_CMD, (float) fovSlider.getValue());
	    }
	});

	lockCameraCheckbox = new JCheckBox("Lock camera to focus");
	lockCameraCheckbox.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.instance.post(Events.FOCUS_LOCK_CMD, "Focus lock", lockCameraCheckbox.isSelected(), true);
	    }
	});

	cameraPanel.add(new JLabel("Camera mode"));
	cameraPanel.add(cameraMode, "wrap");
	cameraPanel.add(new JLabel("Field of view"), "span,wrap");
	cameraPanel.add(fovSlider, "span,growx,wrap");
	cameraPanel.add(lockCameraCheckbox, "span,wrap");

	/* VISIBILITY TOGGLES */
	JPanel visibilityPanel = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", "[]2[]"));
	visibilityPanel.setBorder(new TitledBorder("Object visibility"));
	visibilityButtons = new HashMap<ComponentType, WebToggleButton>();
	ComponentType[] cts = ComponentType.values();
	int idx = 1;
	for (final ComponentType ct : cts) {
	    final WebToggleButton wtb = new WebToggleButton(ct.name(), IconManager.get(ct));
	    wtb.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, new Object[] { ct.name(), wtb.isSelected() });
		}
	    });
	    visibilityPanel.add(wtb, idx % 2 == 0 ? "wrap" : "");
	    visibilityButtons.put(ct, wtb);
	    idx++;
	}

	/* GRAPHICS TOGGLES */
	JPanel graphicsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	graphicsPanel.setBorder(new TitledBorder("Lighting options"));

	starBrightnessSlider = new JSlider();
	starBrightnessSlider.setMinimum(0);
	starBrightnessSlider.setMaximum(150);
	starBrightnessSlider.setMinorTickSpacing(10);
	starBrightnessSlider.setMajorTickSpacing(50);
	starBrightnessSlider.setPaintTicks(true);
	starBrightnessSlider.setPaintLabels(true);
	starBrightnessSlider.setValue(50);
	starBrightnessSlider.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		float mult = starBrightnessSlider.getValue() / 50f;
		EventManager.instance.post(Events.STAR_BRIGHTNESS_CMD, mult);
	    }
	});

	ambientSlider = new JSlider();
	ambientSlider.setMinimum(0);
	ambientSlider.setMaximum(100);
	ambientSlider.setMinorTickSpacing(10);
	ambientSlider.setMajorTickSpacing(50);
	ambientSlider.setPaintTicks(true);
	ambientSlider.setPaintLabels(true);
	ambientSlider.setValue(0);
	ambientSlider.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		float mult = ambientSlider.getValue() / 100f;
		EventManager.instance.post(Events.AMBIENT_LIGHT_CMD, mult);
	    }
	});

	bloomSlider = new JSlider();
	bloomSlider.setMinimum(0);
	bloomSlider.setMaximum(100);
	bloomSlider.setMinorTickSpacing(10);
	bloomSlider.setMajorTickSpacing(50);
	bloomSlider.setPaintTicks(true);
	bloomSlider.setPaintLabels(true);
	bloomSlider.setValue(0);
	bloomSlider.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		float mult = bloomSlider.getValue() / 10f;
		EventManager.instance.post(Events.BLOOM_CMD, mult);
	    }
	});

	graphicsPanel.add(new WebLabel("Star brightness", IconManager.get("brightness")), "wrap");
	graphicsPanel.add(starBrightnessSlider, "wrap");
	graphicsPanel.add(new WebLabel("Ambient light", IconManager.get("bulb")), "wrap");
	graphicsPanel.add(ambientSlider, "wrap");
	graphicsPanel.add(new WebLabel("Bloom effect", IconManager.get("visibility")), "wrap");
	graphicsPanel.add(bloomSlider, "wrap");

	/* GAIA OBSERVATION */
	JPanel gaiaPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	gaiaPanel.setBorder(new TitledBorder("Gaia scan simulation"));

	gaiaScanToggle = new JCheckBox("Enable Gaia scan simulation");
	gaiaScanToggle.setToolTipText("Enable the Gaia scan mode to compute observed stars");
	gaiaScanToggle.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		EventManager.instance.post(Events.COMPUTE_GAIA_SCAN_CMD, "Gaia scan", gaiaScanToggle.isSelected(), true);
	    }
	});
	colorScanToggle = new JCheckBox("Colour Gaia observations");
	colorScanToggle.setToolTipText("Colour stars according to number of observations");
	colorScanToggle.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		EventManager.instance.post(Events.TRANSIT_COLOUR_CMD, "Transit colour", colorScanToggle.isSelected(), true);
	    }
	});
	onlyObservedToggle = new JCheckBox("Display only Gaia observations");
	onlyObservedToggle.setToolTipText("Only display stars that have been observed by Gaia");
	onlyObservedToggle.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		EventManager.instance.post(Events.ONLY_OBSERVED_STARS_CMD, "Only observed stars", onlyObservedToggle.isSelected(), true);
	    }
	});

	gaiaPanel.add(gaiaScanToggle, "wrap");
	gaiaPanel.add(colorScanToggle, "wrap");
	gaiaPanel.add(onlyObservedToggle, "wrap");

	/* FILL IN THE CONTROLS PANEL */
	JPanel controlsPanel = new JPanel();
	controlsPanel.setLayout(new MigLayout("fillx", "[grow,fill]", "[]"));
	controlsPanel.add(timePanel, "wrap");
	controlsPanel.add(cameraPanel, "wrap");
	controlsPanel.add(visibilityPanel, "wrap");
	controlsPanel.add(graphicsPanel, "wrap");
	controlsPanel.add(gaiaPanel);

	JScrollPane controlsScrollPanel = new JScrollPane(controlsPanel);
	controlsScrollPanel.setPreferredSize(new Dimension(300, 800));
	controlsScrollPanel.setAutoscrolls(true);

	tabbedPane.addTab("Controls", controlsScrollPanel);
	tabbedPane.addTab("Objects", treePanel);

	// Horizontal split pane
	mainPanel = new JPanel();
	mainPanel.setLayout(new MigLayout("fill"));

	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, canvas.getCanvas());
	splitPane.setOneTouchExpandable(false);
	splitPane.setDividerLocation(265);
	splitPane.setContinuousLayout(true);

	mainPanel.add(splitPane, "growx,growy");

	frame.add(mainPanel, BorderLayout.CENTER);
	frame.setMinimumSize(new Dimension(450, 300));

	EventManager.instance.subscribe(this, Events.FPS_INFO, Events.TOGGLE_TIME_CMD, Events.TIME_CHANGE_INFO, Events.CAMERA_MODE_CMD, Events.VISIBILITY_OF_COMPONENTS, Events.PACE_CHANGED_INFO, Events.FOCUS_LOCK_CMD, Events.FULLSCREEN_CMD, Events.FOCUS_CHANGED);
    }

    private void mySingleClick(int row, TreePath path) {
	SceneGraphNode node = treeToModel.getBackward((IconTreeNode) path.getLastPathComponent());
	EventManager.instance.post(Events.FOCUS_CHANGE_CMD, node, false);
    }

    private void myDoubleClick(int row, TreePath path) {
	mySingleClick(row, path);
    }

    /**
     * Holds the relation sceneGraphNode to tree node
     */
    public TwoWayHashmap<SceneGraphNode, DefaultMutableTreeNode> treeToModel;
    public HashMap<String, SceneGraphNode> stringNode;

    protected void loadTree(ISceneGraph sg) {
	this.sg = sg;
	treeToModel = new TwoWayHashmap<SceneGraphNode, DefaultMutableTreeNode>();
	stringNode = new HashMap<String, SceneGraphNode>();
	SceneGraphNode root = sg.getRoot();
	treeToModel.add(root, top);

	createNodes(top, root);
	tree.setSelectionModel(new DefaultTreeSelectionModel());
	tree.expandRow(0);
	tree.expandRow(1);
    }

    protected void createNodes(DefaultMutableTreeNode treeNode, SceneGraphNode node) {
	if (node.children != null) {
	    for (SceneGraphNode child : node.children) {
		boolean children = child.children != null;

		DefaultMutableTreeNode childNode = null;
		childNode = new IconTreeNode(child.name, children, IconManager.get(child.ct));
		treeToModel.add(child, childNode);
		if (child.name != null)
		    stringNode.put(child.name.toLowerCase(), child);
		treeNode.add(childNode);
		createNodes(childNode, child);
	    }
	}
    }

    protected JComponent makeTextPanel(String text) {
	JPanel panel = new JPanel(false);
	JLabel filler = new JLabel(text);
	filler.setHorizontalAlignment(JLabel.CENTER);
	panel.setLayout(new GridLayout(1, 1));
	panel.add(filler);
	return panel;
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case FPS_INFO:
	    fpsLabel.setText((Integer) data[0] + " FPS");
	    break;
	case TOGGLE_TIME_CMD:
	    if (!(Boolean) data[1]) {
		boolean timeOn = (Boolean) data[0];
		if (playPause.isSelected() != timeOn)
		    playPause.setSelected(timeOn, true);
	    }
	    break;
	case TIME_CHANGE_INFO:
	    // Set date to date and time
	    updateTime[0] = false;
	    updateTime[1] = false;
	    Date t = new Date(((Date) data[0]).getTime());
	    date.setDateSilent(t);
	    ((SpinnerDateModel) time.getModel()).setValue(t);
	    break;
	case CAMERA_MODE_CMD:
	    // Update camera mode selection
	    CameraMode mode = (CameraMode) data[0];
	    cameraMode.getModel().setSelectedItem(mode);
	    break;
	case VISIBILITY_OF_COMPONENTS:
	    boolean[] vis = (boolean[]) data[0];
	    int i = 0;
	    for (ComponentType ct : ComponentType.values()) {
		boolean visible = vis[i++];
		visibilityButtons.get(ct).setSelected(visible);
	    }
	    break;
	case FOCUS_CHANGED:
	    SceneGraphNode sgn = null;
	    if (data[0] instanceof String) {
		sgn = sg.getNode((String) data[0]);
	    } else {
		sgn = (SceneGraphNode) data[0];
	    }
	    // Select node only if data[1] is true
	    if (sgn != null) {
		selectNodeInTree(sgn);
	    }
	    break;
	case PACE_CHANGED_INFO:
	    double newpace = (double) data[0];
	    if (((Double) pace.getValue()).doubleValue() != newpace) {
		pace.setValue(newpace);
	    }
	    break;
	case FOCUS_LOCK_CMD:
	    boolean newlock = (boolean) data[1];
	    boolean iface = false;
	    if (data.length > 2) {
		iface = (boolean) data[2];
	    }
	    if (!iface && lockCameraCheckbox.isSelected() != newlock) {
		lockCameraCheckbox.setSelected(newlock);
	    }
	    break;
	case FULLSCREEN_CMD:

	    //	    boolean fs = data.length == 0 ? !fullscreen : (boolean) data[0];
	    //
	    //	    if (fullscreen != fs) {
	    //		if (fs) {
	    //		    // Enter fullscreen mode
	    //		    //		    frame.removeAll();
	    //		    //		    frame.add(canvas.getCanvas(), BorderLayout.CENTER);
	    //		    //		    frame.setUndecorated(true);
	    //		    frame.setResizable(false);
	    //		    frame.setAlwaysOnTop(true);
	    //		    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    //		    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
	    //		} else {
	    //		    // Exit fullscreen mode
	    //		    //		    frame.removeAll();
	    //		    //		    frame.add(mainPanel, BorderLayout.CENTER);
	    //		    //		    frame.setUndecorated(false);
	    //		    frame.setResizable(true);
	    //		    frame.setAlwaysOnTop(false);
	    //		    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
	    //		}
	    //		fullscreen = fs;
	    //	    }
	default:
	    break;
	}
    }

    /**
     * Selects the given node in the tree
     * @param node
     */
    public void selectNodeInTree(SceneGraphNode node) {
	List<DefaultMutableTreeNode> list = new ArrayList<DefaultMutableTreeNode>();
	SceneGraphNode aux = node;
	while (aux.parent != null) {
	    list.add(treeToModel.getForward(aux));
	    aux = aux.parent;
	}
	// Here node equals root
	list.add(treeToModel.getForward(aux));
	Collections.reverse(list);
	TreePath path = new TreePath(list.toArray());
	tree.getSelectionModel().setSelectionPath(path);

	double h = 16;
	double height = 0;
	// Work out pixels from top to selection
	height = h * (tree.getLeadSelectionRow() - 1);

	treeScrollPane.getVerticalScrollBar().setValue((int) height);
    }

    class IconTreeCellRenderer implements TreeCellRenderer {
	private JLabel label;

	IconTreeCellRenderer() {
	    label = new JLabel();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	    if (value instanceof IconTreeNode) {
		IconTreeNode node = (IconTreeNode) value;
		label.setIcon(node.getIcon());
		label.setText("" + node.getUserObject());
	    } else {
		label.setIcon(null);
		label.setText("" + value);
	    }
	    return label;
	}
    }

}
