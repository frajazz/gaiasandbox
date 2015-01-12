package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.GaiaSandboxDesktop;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.gui.swing.callback.Callback;
import gaia.cu9.ari.gaiaorbit.gui.swing.callback.CallbackTask;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.GuiUtility;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.JSplashLabel;
import gaia.cu9.ari.gaiaorbit.gui.swing.version.VersionChecker;
import gaia.cu9.ari.gaiaorbit.interfce.KeyMappings;
import gaia.cu9.ari.gaiaorbit.interfce.KeyMappings.ProgramAction;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import com.alee.extended.filechooser.WebDirectoryChooser;
import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import com.alee.utils.FileUtils;
import com.alee.utils.swing.DialogOptions;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * The configuration dialog to set the resolution, the screen mode, etc.
 * @author Toni Sagrista
 *
 */
public class ConfigDialog extends I18nJFrame {
    private static long fiveDaysMs = 5 * 24 * 60 * 60 * 1000;

    JFrame frame;
    JLabel checkLabel;
    JPanel checkPanel;
    Color darkgreen, darkred;

    public ConfigDialog(final GaiaSandboxDesktop gsd, boolean startup) {
	super(startup ? GlobalConf.instance.getFullApplicationName() : txt("gui.settings"));
	initialize(gsd, startup);

	if (startup) {
	    /** SPLASH IMAGE **/
	    URL url = this.getClass().getResource("/img/splash/splash1-s.png");
	    JSplashLabel label = new JSplashLabel(url, txt("gui.build", GlobalConf.instance.VERSION.build) + " - " + txt("gui.version", GlobalConf.instance.VERSION.version), null, Color.lightGray);
	    JPanel imagePanel = new JPanel(new GridLayout(1, 1, 0, 0));
	    imagePanel.add(label);
	    imagePanel.setBackground(Color.black);
	    frame.add(imagePanel, BorderLayout.NORTH);
	}

	frame.pack();
	GuiUtility.centerOnScreen(frame);
	frame.setVisible(true);
	frame.setEnabled(true);
	frame.setAutoRequestFocus(true);
    }

    private void initialize(final GaiaSandboxDesktop gsd, final boolean startup) {
	frame = this;
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.setResizable(false);

	darkgreen = new Color(0, .5f, 0);
	darkred = new Color(.7f, 0, 0);

	// Build content
	frame.setLayout(new BorderLayout(0, 0));

	/** BODY **/
	JPanel body = new JPanel(new MigLayout("", "[grow,fill][]", ""));

	/** VERSION CHECK **/
	checkPanel = new JPanel(new MigLayout("", "[][]", "[]4[]"));
	checkLabel = new JLabel("");
	checkPanel.add(checkLabel);
	if (GlobalConf.instance.LAST_CHECKED == null || GlobalConf.instance.LAST_VERSION.isEmpty() || new Date().getTime() - GlobalConf.instance.LAST_CHECKED.getTime() > fiveDaysMs) {
	    // Check!
	    checkLabel.setText(txt("gui.newversion.checking"));
	    getCheckVersionThread().start();
	} else {
	    // Inform latest
	    newVersionCheck(GlobalConf.instance.LAST_VERSION);

	}

	/** TABBED PANEL **/

	JTabbedPane tabbedPane = new JTabbedPane();

	/**
	 * ====== GRAPHICS TAB =======
	 */

	/** RESOLUTION **/
	JPanel mode = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
	mode.setBorder(new TitledBorder(txt("gui.resolutionmode")));

	// Full screen mode resolutions
	DisplayMode[] modes = LwjglApplicationConfiguration.getDisplayModes();
	final JComboBox<DisplayMode> fullScreenResolutions = new JComboBox<DisplayMode>(modes);

	DisplayMode selectedMode = null;
	for (DisplayMode dm : modes) {
	    if (dm.width == GlobalConf.instance.FULLSCREEN_WIDTH && dm.height == GlobalConf.instance.FULLSCREEN_HEIGHT) {
		selectedMode = dm;
		break;
	    }
	}
	if (selectedMode != null)
	    fullScreenResolutions.setSelectedItem(selectedMode);

	// Get native resolution
	int maxw = 0, maxh = 0;
	DisplayMode nativeMode = null;
	for (DisplayMode dm : LwjglApplicationConfiguration.getDisplayModes()) {
	    if (dm.width > maxw) {
		nativeMode = dm;
		maxw = dm.width;
	    }
	    if (dm.height > maxh) {
		nativeMode = dm;
		maxh = dm.height;
	    }
	}

	// Windowed mode resolutions
	JPanel windowedResolutions = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
	final JSpinner widthField = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.SCREEN_WIDTH, 100, nativeMode.width, 1));
	final JSpinner heightField = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.SCREEN_HEIGHT, 100, nativeMode.height, 1));
	final JCheckBox resizable = new JCheckBox("Resizable", GlobalConf.instance.RESIZABLE);

	windowedResolutions.add(new JLabel(txt("gui.width") + ":"));
	windowedResolutions.add(widthField);
	windowedResolutions.add(new JLabel(txt("gui.height") + ":"));
	windowedResolutions.add(heightField, "wrap");
	windowedResolutions.add(resizable, "span");

	// Radio buttons 
	final JRadioButton fullscreen = new JRadioButton(txt("gui.fullscreen"));
	fullscreen.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		GlobalConf.instance.FULLSCREEN = fullscreen.isSelected();
		selectFullscreen(fullscreen.isSelected(), widthField, heightField, fullScreenResolutions, resizable);
	    }
	});
	fullscreen.setSelected(GlobalConf.instance.FULLSCREEN);

	final JRadioButton windowed = new JRadioButton(txt("gui.windowed"));
	windowed.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		GlobalConf.instance.FULLSCREEN = !windowed.isSelected();
		selectFullscreen(!windowed.isSelected(), widthField, heightField, fullScreenResolutions, resizable);
	    }
	});
	windowed.setSelected(!GlobalConf.instance.FULLSCREEN);
	selectFullscreen(GlobalConf.instance.FULLSCREEN, widthField, heightField, fullScreenResolutions, resizable);

	ButtonGroup modeButtons = new ButtonGroup();
	modeButtons.add(fullscreen);
	modeButtons.add(windowed);

	mode.add(fullscreen);
	mode.add(fullScreenResolutions, "wrap");
	mode.add(windowed);
	mode.add(windowedResolutions);

	/** GRAPHICS **/
	JPanel graphics = new JPanel(new MigLayout("", "[][]", ""));
	graphics.setBorder(new TitledBorder(txt("gui.graphicssettings")));

	// MSAA
	JTextArea msaaInfo = new JTextArea(txt("gui.aa.info")) {
	    @Override
	    public void setBorder(Border border) {
		// No!
	    }
	};
	msaaInfo.setBackground(new Color(1, 1, 1, 0));
	msaaInfo.setForeground(darkgreen);
	msaaInfo.setEditable(false);

	JLabel msaaLabel = new JLabel(txt("gui.aa"));
	ComboBoxBean[] msaas = new ComboBoxBean[] { new ComboBoxBean(txt("gui.aa.no"), 0), new ComboBoxBean(txt("gui.aa.fxaa"), -1), new ComboBoxBean(txt("gui.aa.nfaa"), -2), new ComboBoxBean(txt("gui.aa.msaa", 2), 2), new ComboBoxBean(txt("gui.aa.msaa", 4), 4), new ComboBoxBean(txt("gui.aa.msaa", 8), 8), new ComboBoxBean(txt("gui.aa.msaa", 16), 16) };
	final JComboBox<ComboBoxBean> msaa = new JComboBox<ComboBoxBean>(msaas);
	msaa.setSelectedItem(msaas[idx(2, GlobalConf.instance.POSTPROCESS_ANTIALIAS)]);

	// Vsync
	final JCheckBox vsync = new JCheckBox(txt("gui.vsync"), GlobalConf.instance.VSYNC);

	graphics.add(msaaInfo, "span,wrap");
	graphics.add(msaaLabel);
	graphics.add(msaa);
	graphics.add(vsync, "span");

	/** NOTICE **/
	JPanel notice = new JPanel(new MigLayout("", "[]", ""));
	JLabel noticeText = new JLabel(txt("gui.graphics.info"));
	noticeText.setForeground(darkgreen);
	notice.add(noticeText);

	JPanel graphicsPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	graphicsPanel.add(mode, "wrap");
	graphicsPanel.add(graphics, "wrap");
	if (!startup) {
	    graphicsPanel.add(notice, "wrap");
	}

	tabbedPane.addTab(txt("gui.graphics"), graphicsPanel);

	/**
	 * ====== PERFORMANCE TAB =======
	 */

	/** MULTITHREAD **/
	JPanel multithread = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	multithread.setBorder(new TitledBorder(txt("gui.multithreading")));

	int maxthreads = Runtime.getRuntime().availableProcessors();
	ComboBoxBean[] cbs = new ComboBoxBean[maxthreads + 1];
	cbs[0] = new ComboBoxBean(txt("gui.letdecide"), 0);
	for (int i = 1; i <= maxthreads; i++) {
	    cbs[i] = new ComboBoxBean(txt("gui.thread", i), i);
	}
	final JComboBox<ComboBoxBean> numThreads = new JComboBox<ComboBoxBean>(cbs);

	final JCheckBox multithreadCb = new JCheckBox(txt("gui.thread.enable"));
	multithreadCb.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		numThreads.setEnabled(multithreadCb.isSelected());
	    }
	});
	multithreadCb.setSelected(GlobalConf.instance.MULTITHREADING);
	numThreads.setEnabled(multithreadCb.isSelected());

	multithread.add(multithreadCb, "span");
	multithread.add(new JLabel(txt("gui.thread.number") + ":"));
	multithread.add(numThreads);

	JPanel performancePanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	performancePanel.add(multithread, "wrap");

	tabbedPane.addTab(txt("gui.performance"), performancePanel);

	/**
	 * ====== CONTROLS TAB =======
	 */
	JPanel controls = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	controls.setBorder(new TitledBorder(txt("gui.keymappings")));

	Map<TreeSet<Integer>, ProgramAction> maps = KeyMappings.instance.mappings;
	Set<TreeSet<Integer>> keymaps = maps.keySet();

	String[] headers = new String[] { txt("gui.keymappings.action"), txt("gui.keymappings.keys") };
	String[][] data = new String[maps.size()][2];
	int i = 0;
	for (TreeSet<Integer> keys : keymaps) {
	    ProgramAction action = maps.get(keys);
	    data[i][0] = action.actionName;
	    data[i][1] = keysToString(keys);
	    i++;
	}

	WebTable table = new WebTable(data, headers);
	table.setEditable(false);
	table.setAutoResizeMode(WebTable.AUTO_RESIZE_ALL_COLUMNS);
	table.setRowSelectionAllowed(true);
	table.setColumnSelectionAllowed(false);

	JScrollPane controlsScrollPane = new JScrollPane(table);
	controlsScrollPane.setPreferredSize(new Dimension(0, 180));

	JLabel lab = new JLabel(txt("gui.noteditable"));
	lab.setForeground(darkred);
	controls.add(lab, "span");
	controls.add(controlsScrollPane, "span");

	tabbedPane.addTab(txt("gui.controls"), controls);

	/**
	 * ====== SCREENSHOTS TAB =======
	 */

	/** SCREENSHOTS CONFIG **/
	JPanel screenshots = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	screenshots.setBorder(new TitledBorder(txt("gui.screencapture")));

	JTextArea screenshotsInfo = new JTextArea(txt("gui.screencapture.info")) {
	    @Override
	    public void setBorder(Border border) {
		// No!
	    }
	};
	screenshotsInfo.setEditable(false);
	screenshotsInfo.setBackground(new Color(1, 1, 1, 0));
	screenshotsInfo.setForeground(darkgreen);

	// SCREENSHOTS LOCATION
	JLabel screenshotsLocationLabel = new JLabel(txt("gui.screenshots.save") + ":");
	File currentLocation = new File(GlobalConf.instance.SCREENSHOT_FOLDER);
	String dirText = txt("gui.screenshots.directory.choose");
	if (currentLocation.exists() && currentLocation.isDirectory()) {
	    dirText = GlobalConf.instance.SCREENSHOT_FOLDER;
	}
	final WebButton screenshotsLocation = new WebButton(dirText);
	screenshotsLocation.addActionListener(new ActionListener()
	{
	    private WebDirectoryChooser directoryChooser = null;

	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		if (directoryChooser == null)
		{
		    directoryChooser = new WebDirectoryChooser(frame, txt("gui.directory.chooseany"));
		    // Increase scrollbar speed
		    WebScrollPane wsp = (WebScrollPane) ((Container) ((Container) ((Container) ((Container) ((Container) directoryChooser.getComponents()[0]).getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[1];
		    wsp.getVerticalScrollBar().setUnitIncrement(50);
		    File currentLocation = new File(GlobalConf.instance.SCREENSHOT_FOLDER);
		    if (currentLocation.exists() && currentLocation.isDirectory()) {
			directoryChooser.setSelectedDirectory(new File(GlobalConf.instance.SCREENSHOT_FOLDER));
		    }
		}
		directoryChooser.setVisible(true);

		if (directoryChooser.getResult() == DialogOptions.OK_OPTION)
		{
		    File file = directoryChooser.getSelectedDirectory();
		    screenshotsLocation.setIcon(FileUtils.getFileIcon(file));
		    screenshotsLocation.setText(file.getAbsolutePath());
		}
	    }
	});

	// SCREENSHOT WIDTH AND HEIGHT
	final JSpinner sswidthField = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.SCREENSHOT_WIDTH, 100, 5000, 1));
	final JSpinner ssheightField = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.SCREENSHOT_HEIGHT, 100, 5000, 1));

	JPanel screenshotSize = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
	screenshotSize.add(new JLabel(txt("gui.width") + ":"));
	screenshotSize.add(sswidthField);
	screenshotSize.add(new JLabel(txt("gui.height") + ":"));
	screenshotSize.add(ssheightField);

	screenshots.add(screenshotsInfo, "span,wrap");
	screenshots.add(screenshotsLocationLabel);
	screenshots.add(screenshotsLocation, "wrap");
	screenshots.add(screenshotSize, "span");

	tabbedPane.addTab(txt("gui.screenshots"), screenshots);

	/**
	 * ====== FRAME OUTPUT TAB =======
	 */

	/** IMAGE OUTPUT CONFIG **/
	JPanel imageOutput = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	imageOutput.setBorder(new TitledBorder(txt("gui.frameoutput")));

	JTextArea frameInfo = new JTextArea(txt("gui.frameoutput.info")) {
	    @Override
	    public void setBorder(Border border) {
		// No!
	    }
	};
	frameInfo.setEditable(false);
	frameInfo.setBackground(new Color(1, 1, 1, 0));
	frameInfo.setForeground(darkgreen);

	// SAVE LOCATION
	File currentFrameLocation = new File(GlobalConf.instance.SCREENSHOT_FOLDER);
	String dirFrameText = txt("gui.frameoutput.directory.choose");
	if (currentFrameLocation.exists() && currentFrameLocation.isDirectory()) {
	    dirFrameText = GlobalConf.instance.RENDER_FOLDER;
	}
	final WebButton frameLocation = new WebButton(dirFrameText);
	frameLocation.addActionListener(new ActionListener()
	{
	    private WebDirectoryChooser directoryChooser = null;

	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		if (directoryChooser == null)
		{
		    directoryChooser = new WebDirectoryChooser(frame, txt("gui.directory.chooseany"));
		    // Increase scrollbar speed
		    WebScrollPane wsp = (WebScrollPane) ((Container) ((Container) ((Container) ((Container) ((Container) directoryChooser.getComponents()[0]).getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[1];
		    wsp.getVerticalScrollBar().setUnitIncrement(50);
		    File currentLocation = new File(GlobalConf.instance.RENDER_FOLDER);
		    if (currentLocation.exists() && currentLocation.isDirectory()) {
			directoryChooser.setSelectedDirectory(new File(GlobalConf.instance.RENDER_FOLDER));
		    }
		}
		directoryChooser.setVisible(true);

		if (directoryChooser.getResult() == DialogOptions.OK_OPTION)
		{
		    File file = directoryChooser.getSelectedDirectory();
		    frameLocation.setIcon(FileUtils.getFileIcon(file));
		    frameLocation.setText(file.getAbsolutePath());
		}
	    }
	});

	// NAME
	final JTextField frameFileName = new JTextField();
	frameFileName.getDocument().addDocumentListener(new DocumentListener() {
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
		String text = frameFileName.getText();
		// Only word characters
		if (!text.matches("^\\w+$")) {
		    frameFileName.setForeground(Color.red);
		} else {
		    frameFileName.setForeground(Color.black);
		}
	    }
	});
	frameFileName.setText(GlobalConf.instance.RENDER_FILE_NAME);

	// FRAME OUTPUT WIDTH AND HEIGHT
	final JSpinner frameWidthField = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.RENDER_WIDTH, 100, 5000, 1));
	final JSpinner frameHeightField = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.RENDER_HEIGHT, 100, 5000, 1));

	JPanel renderSize = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
	renderSize.add(new JLabel(txt("gui.width") + ":"));
	renderSize.add(frameWidthField);
	renderSize.add(new JLabel(txt("gui.height") + ":"));
	renderSize.add(frameHeightField);

	// TARGET FPS
	final JSpinner targetFPS = new JSpinner(new SpinnerNumberModel(GlobalConf.instance.RENDER_TARGET_FPS, 1, 60, 1));

	// FRAME OUTPUT CHECKBOX
	final JCheckBox frameCb = new JCheckBox(txt("gui.frameoutput.enable"));
	frameCb.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		JCheckBox cb = (JCheckBox) e.getSource();
		boolean selected = cb.isSelected();
		setEnabledFrameOutput(selected, frameLocation, frameWidthField, frameHeightField, targetFPS, frameFileName);
	    }
	});
	frameCb.setSelected(GlobalConf.instance.RENDER_OUTPUT);
	setEnabledFrameOutput(frameCb.isSelected(), frameLocation, frameWidthField, frameHeightField, targetFPS, frameFileName);

	imageOutput.add(frameInfo, "span");
	imageOutput.add(frameCb, "span");
	imageOutput.add(new JLabel(txt("gui.frameoutput.location") + ":"));
	imageOutput.add(frameLocation, "wrap");
	imageOutput.add(new JLabel(txt("gui.frameoutput.prefix") + ":"));
	imageOutput.add(frameFileName, "wrap");
	imageOutput.add(renderSize, "span");
	imageOutput.add(new JLabel(txt("gui.frameoutput.fps") + ":"));
	imageOutput.add(targetFPS);

	tabbedPane.addTab(txt("gui.frameoutput.title"), imageOutput);

	// Do not show again
	final JCheckBox showAgain = new JCheckBox(txt("gui.notagain"));
	showAgain.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		GlobalConf.instance.SHOW_CONFIG_DIALOG = !showAgain.isSelected();
	    }
	});

	body.add(tabbedPane, "wrap");
	body.add(checkPanel, "wrap");
	if (startup) {
	    body.add(showAgain);
	}

	/** BUTTONS **/
	JPanel buttons = new JPanel(new MigLayout("", "push[][]", ""));

	JButton okButton = new JButton(startup ? txt("gui.launchapp") : txt("gui.saveprefs"));
	okButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		// Add all properties to GlobalConf.instance
		GlobalConf.instance.FULLSCREEN = fullscreen.isSelected();

		// Fullscreen options
		GlobalConf.instance.FULLSCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelectedItem()).width;
		GlobalConf.instance.FULLSCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelectedItem()).height;

		// Windowed options
		GlobalConf.instance.SCREEN_WIDTH = ((Integer) widthField.getValue());
		GlobalConf.instance.SCREEN_HEIGHT = ((Integer) heightField.getValue());
		GlobalConf.instance.RESIZABLE = resizable.isSelected();

		// Graphics
		ComboBoxBean bean = (ComboBoxBean) msaa.getSelectedItem();
		GlobalConf.instance.POSTPROCESS_ANTIALIAS = bean.value;
		GlobalConf.instance.VSYNC = vsync.isSelected();

		// Performance
		bean = (ComboBoxBean) numThreads.getSelectedItem();
		GlobalConf.instance.NUMBER_THREADS = bean.value;
		GlobalConf.instance.MULTITHREADING = multithreadCb.isSelected();

		// Screenshots
		File ssfile = new File(screenshotsLocation.getText());
		if (ssfile.exists() && ssfile.isDirectory())
		    GlobalConf.instance.SCREENSHOT_FOLDER = ssfile.getAbsolutePath();
		GlobalConf.instance.SCREENSHOT_WIDTH = ((Integer) sswidthField.getValue());
		GlobalConf.instance.SCREENSHOT_HEIGHT = ((Integer) ssheightField.getValue());

		// Frame output
		File fofile = new File(frameLocation.getText());
		if (fofile.exists() && fofile.isDirectory())
		    GlobalConf.instance.RENDER_FOLDER = fofile.getAbsolutePath();
		String text = frameFileName.getText();
		if (text.matches("^\\w+$")) {
		    GlobalConf.instance.RENDER_FILE_NAME = text;
		}
		GlobalConf.instance.RENDER_WIDTH = ((Integer) frameWidthField.getValue());
		GlobalConf.instance.RENDER_HEIGHT = ((Integer) frameHeightField.getValue());
		GlobalConf.instance.RENDER_OUTPUT = frameCb.isSelected();
		GlobalConf.instance.RENDER_TARGET_FPS = ((Integer) targetFPS.getValue());

		// Save configuration
		try {
		    GlobalConf.instance.saveProperties(new File(System.getProperty("properties.file")).toURI().toURL());
		} catch (MalformedURLException e1) {
		    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
		}

		EventManager.getInstance().post(Events.PROPERTIES_WRITTEN);

		if (startup) {
		    gsd.launchMainApp();
		}
		frame.setVisible(false);
	    }

	});
	okButton.setMinimumSize(new Dimension(100, 20));

	JButton cancelButton = new JButton(txt("gui.cancel"));
	cancelButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (frame.isDisplayable()) {
		    frame.dispose();
		}
	    }
	});
	cancelButton.setMinimumSize(new Dimension(100, 20));

	buttons.add(okButton);
	buttons.add(cancelButton);

	frame.add(body, BorderLayout.CENTER);
	frame.add(buttons, BorderLayout.SOUTH);

    }

    private void setEnabledFrameOutput(boolean enabled, JComponent... components) {
	for (JComponent c : components)
	    c.setEnabled(enabled);
    }

    private void selectFullscreen(boolean fullscreen, JSpinner widthField, JSpinner heightField, JComboBox<DisplayMode> fullScreenResolutions, JCheckBox resizable) {
	if (fullscreen) {
	    GlobalConf.instance.SCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelectedItem()).width;
	    GlobalConf.instance.SCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelectedItem()).height;
	} else {
	    GlobalConf.instance.SCREEN_WIDTH = (Integer) widthField.getValue();
	    GlobalConf.instance.SCREEN_HEIGHT = (Integer) heightField.getValue();
	}
	widthField.setEnabled(!fullscreen);
	heightField.setEnabled(!fullscreen);
	resizable.setEnabled(!fullscreen);
	fullScreenResolutions.setEnabled(fullscreen);

    }

    private int idx(int base, int x) {
	if (x == -1)
	    return 1;
	if (x == -2)
	    return 2;
	if (x == 0)
	    return 0;
	return (int) (Math.log(x) / Math.log(2) + 1e-10) + 2;
    }

    private class ComboBoxBean {
	public String name;
	public int value;

	public ComboBoxBean(String name, int samples) {
	    super();
	    this.name = name;
	    this.value = samples;
	}

	@Override
	public String toString() {
	    return name;
	}

    }

    private Thread getCheckVersionThread() {
	return new Thread(new CallbackTask(new VersionChecker(GlobalConf.instance.VERSION_CHECK_URL), new Callback() {
	    @Override
	    public void complete(Object result) {
		String res = ((String) result).trim();
		checkPanel.removeAll();
		checkPanel.add(checkLabel);
		if (res.matches("^\\d+.\\d+\\D{1}$")) {
		    String version = res;
		    GlobalConf.instance.LAST_VERSION = new String(version);
		    GlobalConf.instance.LAST_CHECKED = new Date();
		    newVersionCheck(version);
		} else {
		    // Error!
		    checkLabel.setText("Error checking version :(");
		    checkLabel.setForeground(Color.RED);
		}
		checkPanel.validate();
	    }
	}));
    }

    /**
     * Checks the given version against the current version and:
     * <ul><li>
     * Displays a "new version available" message if the given version is newer than the current.
     * </li><li>
     * Display a "you have the latest version" message and a "check now" button if the given version is older.
     * </li></ul>
     * @param version The version to check.
     */
    private void newVersionCheck(String version) {
	int[] majmin = GlobalConf.VersionInfo.getMajorMinorFromString(version);

	if (majmin[0] > GlobalConf.instance.VERSION.major || (majmin[0] == GlobalConf.instance.VERSION.major && majmin[1] > GlobalConf.instance.VERSION.minor)) {
	    // There's a new version!
	    checkLabel.setText(txt("gui.newversion.available", GlobalConf.instance.VERSION, version));
	    try {
		final URI uri = new URI(GlobalConf.instance.WEBPAGE);

		JButton button = new JButton();
		button.setText(txt("gui.newversion.getit"));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setBackground(Color.WHITE);
		button.setToolTipText(uri.toString());
		button.addActionListener(new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			if (Desktop.isDesktopSupported()) {
			    try {
				Desktop.getDesktop().browse(uri);
			    } catch (IOException ex) {
			    }
			} else {
			}
		    }

		});
		checkPanel.add(button);
	    } catch (URISyntaxException e1) {
	    }
	} else {
	    checkLabel.setText(txt("gui.newversion.nonew", GlobalConf.instance.getLastCheckedString()));
	    // Add check now button
	    JButton button = new JButton();
	    button.setText(txt("gui.newversion.checknow"));
	    button.setHorizontalAlignment(SwingConstants.LEFT);
	    button.setBorderPainted(false);
	    button.setOpaque(false);
	    button.setBackground(Color.WHITE);
	    button.setToolTipText(txt("gui.newversion.checknow.tooltip"));
	    button.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    getCheckVersionThread().start();
		}

	    });
	    checkPanel.add(button);
	}
	checkLabel.setForeground(darkgreen);
    }

    private String keysToString(TreeSet<Integer> keys) {
	String s = "";

	int i = 0;
	int n = keys.size();
	for (Integer key : keys) {
	    s += Keys.toString(key).toUpperCase();
	    if (i < n - 1) {
		s += " + ";
	    }

	    i++;
	}

	return s;
    }

}
