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
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
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
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

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
    JButton cancelButton, okButton;

    public ConfigDialog(final GaiaSandboxDesktop gsd, boolean startup) {
	super(startup ? GlobalConf.instance.getFullApplicationName() : txt("gui.settings"));
	initialize(gsd, startup);

	if (startup) {
	    /** SPLASH IMAGE **/
	    URL url = this.getClass().getResource("/img/splash/splash-s.jpg");
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

	// ESC closes the frame
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		KeyStroke.getKeyStroke("ESCAPE"), "closeTheDialog");
	getRootPane().getActionMap().put("closeTheDialog",
		new AbstractAction() {

		    private static final long serialVersionUID = 8360999630557775801L;

		    @Override
		    public void actionPerformed(ActionEvent e) {
			//This should be replaced by the action you want to perform
			cancelButton.doClick();
		    }
		});

	// Request focus
	frame.getRootPane().setDefaultButton(okButton);
	okButton.requestFocus();
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

	//	JTabbedPane tabbedPane = new JTabbedPane();
	//	tabbedPane.setTabPlacement(WebTabbedPane.LEFT);

	JXTabbedPane tabbedPane = new JXTabbedPane(JTabbedPane.LEFT);
	AbstractTabRenderer renderer = (AbstractTabRenderer) tabbedPane.getTabRenderer();
	renderer.setPrototypeText("123456789012345678");
	renderer.setHorizontalTextAlignment(SwingConstants.LEADING);

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
	final JSpinner widthField = new JSpinner(new SpinnerNumberModel(MathUtils.clamp(GlobalConf.instance.SCREEN_WIDTH, 100, nativeMode.width), 100, nativeMode.width, 1));
	final JSpinner heightField = new JSpinner(new SpinnerNumberModel(MathUtils.clamp(GlobalConf.instance.SCREEN_HEIGHT, 100, nativeMode.height), 100, nativeMode.height, 1));
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

	ThreadComboBoxBean[] msaas = new ThreadComboBoxBean[] { new ThreadComboBoxBean(txt("gui.aa.no"), 0), new ThreadComboBoxBean(txt("gui.aa.fxaa"), -1), new ThreadComboBoxBean(txt("gui.aa.nfaa"), -2), new ThreadComboBoxBean(txt("gui.aa.msaa", 2), 2), new ThreadComboBoxBean(txt("gui.aa.msaa", 4), 4), new ThreadComboBoxBean(txt("gui.aa.msaa", 8), 8), new ThreadComboBoxBean(txt("gui.aa.msaa", 16), 16) };
	final JComboBox<ThreadComboBoxBean> msaa = new JComboBox<ThreadComboBoxBean>(msaas);
	msaa.setSelectedItem(msaas[idxAa(2, GlobalConf.instance.POSTPROCESS_ANTIALIAS)]);

	// Vsync
	final JCheckBox vsync = new JCheckBox(txt("gui.vsync"), GlobalConf.instance.VSYNC);

	graphics.add(msaaInfo, "span,wrap");
	graphics.add(new JLabel(txt("gui.aa")));
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

	tabbedPane.addTab(txt("gui.graphics"), IconManager.get("config/graphics"), graphicsPanel);
	tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

	/**
	 * ====== USER INTERFACE TAB =======
	 */
	JPanel ui = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	ui.setBorder(new TitledBorder(txt("gui.ui.interfacesettings")));

	File i18nfolder = new File("./data/i18n/");
	if (!i18nfolder.exists()) {
	    i18nfolder = new File("../android/assets/i18n/");
	}
	String i18nname = "gsbundle";
	String[] files = i18nfolder.list();
	LangComboBoxBean[] langs = new LangComboBoxBean[files.length];
	int i = 0;
	for (String file : files) {
	    if (file.startsWith("gsbundle") && file.endsWith(".properties")) {
		String locale = file.substring(i18nname.length(), file.length() - ".properties".length());
		if (locale.length() != 0) {
		    // Remove underscore _
		    locale = locale.substring(1).replace("_", "-");
		    Locale loc = Locale.forLanguageTag(locale);
		    langs[i] = new LangComboBoxBean(loc);
		} else {
		    langs[i] = new LangComboBoxBean(I18n.bundle.getLocale());
		}
	    }
	    i++;
	}
	Arrays.sort(langs);
	final JComboBox<LangComboBoxBean> lang = new JComboBox<LangComboBoxBean>(langs);
	lang.setSelectedItem(langs[idxLang(GlobalConf.instance.LOCALE, langs)]);

	String[] themes = new String[] { "dark", "bright", "dark-big" };
	final JComboBox<String> theme = new JComboBox<String>(themes);
	theme.setSelectedItem(GlobalConf.instance.UI_THEME);

	ui.add(new JLabel(txt("gui.ui.language")));
	ui.add(lang, "wrap");
	ui.add(new JLabel(txt("gui.ui.theme")));
	ui.add(theme);

	/** NOTICE **/
	JPanel uiNotice = new JPanel(new MigLayout("", "[]", ""));
	JLabel uinoticeText = new JLabel(txt("gui.ui.info"));
	uinoticeText.setForeground(darkgreen);
	uiNotice.add(uinoticeText);

	JPanel uiPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	uiPanel.add(ui, "wrap");
	if (!startup) {
	    uiPanel.add(uiNotice, "wrap");
	}

	tabbedPane.addTab(txt("gui.ui.interface"), IconManager.get("config/interface"), uiPanel);
	tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

	/**
	 * ====== PERFORMANCE TAB =======
	 */

	/** MULTITHREAD **/
	JPanel multithread = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	multithread.setBorder(new TitledBorder(txt("gui.multithreading")));

	int maxthreads = Runtime.getRuntime().availableProcessors();
	ThreadComboBoxBean[] cbs = new ThreadComboBoxBean[maxthreads + 1];
	cbs[0] = new ThreadComboBoxBean(txt("gui.letdecide"), 0);
	for (i = 1; i <= maxthreads; i++) {
	    cbs[i] = new ThreadComboBoxBean(txt("gui.thread", i), i);
	}
	final JComboBox<ThreadComboBoxBean> numThreads = new JComboBox<ThreadComboBoxBean>(cbs);

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

	tabbedPane.addTab(txt("gui.performance"), IconManager.get("config/performance"), performancePanel);
	tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

	/**
	 * ====== CONTROLS TAB =======
	 */
	JPanel controls = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	controls.setBorder(new TitledBorder(txt("gui.keymappings")));

	Map<TreeSet<Integer>, ProgramAction> maps = KeyMappings.instance.mappings;
	Set<TreeSet<Integer>> keymaps = maps.keySet();

	String[] headers = new String[] { txt("gui.keymappings.action"), txt("gui.keymappings.keys") };
	String[][] data = new String[maps.size()][2];
	i = 0;
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
	controls.add(lab, "wrap");
	controls.add(controlsScrollPane, "wrap");

	JPanel controlsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	controlsPanel.add(controls, "wrap");

	tabbedPane.addTab(txt("gui.controls"), IconManager.get("config/controls"), controlsPanel);
	tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

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

	JPanel screenshotsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	screenshotsPanel.add(screenshots, "wrap");

	tabbedPane.addTab(txt("gui.screenshots"), IconManager.get("config/screenshots"), screenshotsPanel);
	tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

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

	JPanel imageOutputPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
	imageOutputPanel.add(imageOutput, "wrap");

	tabbedPane.addTab(txt("gui.frameoutput.title"), IconManager.get("config/frameoutput"), imageOutputPanel);
	tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);

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

	okButton = new JButton(startup ? txt("gui.launchapp") : txt("gui.saveprefs"));
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
		ThreadComboBoxBean bean = (ThreadComboBoxBean) msaa.getSelectedItem();
		GlobalConf.instance.POSTPROCESS_ANTIALIAS = bean.value;
		GlobalConf.instance.VSYNC = vsync.isSelected();

		// Interface
		LangComboBoxBean lbean = (LangComboBoxBean) lang.getSelectedItem();
		GlobalConf.instance.LOCALE = lbean.locale.toLanguageTag();
		if (!I18n.forceinit("./data/i18n/gsbundle"))
		    I18n.forceinit("../android/assets/i18n/gsbundle");
		GlobalConf.instance.UI_THEME = (String) theme.getSelectedItem();

		// Performance
		bean = (ThreadComboBoxBean) numThreads.getSelectedItem();
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

	cancelButton = new JButton(txt("gui.cancel"));
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

    private int idxAa(int base, int x) {
	if (x == -1)
	    return 1;
	if (x == -2)
	    return 2;
	if (x == 0)
	    return 0;
	return (int) (Math.log(x) / Math.log(2) + 1e-10) + 2;
    }

    private int idxLang(String code, LangComboBoxBean[] langs) {
	if (code.isEmpty()) {
	    code = I18n.bundle.getLocale().toLanguageTag();
	}
	for (int i = 0; i < langs.length; i++) {
	    if (langs[i].locale.toLanguageTag().equals(code)) {
		return i;
	    }
	}
	return -1;
    }

    private class ThreadComboBoxBean {
	public String name;
	public int value;

	public ThreadComboBoxBean(String name, int samples) {
	    super();
	    this.name = name;
	    this.value = samples;
	}

	@Override
	public String toString() {
	    return name;
	}

    }

    private class LangComboBoxBean implements Comparable<LangComboBoxBean> {
	public Locale locale;
	public String name;

	public LangComboBoxBean(Locale locale) {
	    super();
	    this.locale = locale;
	    this.name = GlobalResources.capitalise(locale.getDisplayName(locale));
	}

	@Override
	public String toString() {
	    return name;
	}

	@Override
	public int compareTo(LangComboBoxBean o) {
	    return this.name.compareTo(o.name);
	}

    }

    private Thread getCheckVersionThread() {
	return new Thread(new CallbackTask(new VersionChecker(GlobalConf.instance.VERSION_CHECK_URL), new Callback() {
	    @Override
	    public void complete(Object result) {
		checkPanel.removeAll();
		checkPanel.add(checkLabel);
		if (result instanceof String) {
		    // Error
		    checkLabel.setText("Error checking version: " + (String) result);
		    checkLabel.setForeground(Color.RED);
		} else if (result instanceof JsonValue) {
		    // Ok!
		    JsonValue json = (JsonValue) result;

		    JsonValue last = json.get(json.size - 1);
		    String version = last.getString("name");
		    if (version.matches("^(\\D{1})?\\d+.\\d+(\\D{1})?$")) {
			GlobalConf.instance.LAST_VERSION = new String(version);
			GlobalConf.instance.LAST_CHECKED = new Date();
			newVersionCheck(version);
		    }
		    checkPanel.validate();
		}

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

    class JXTabbedPane extends JTabbedPane {

	private ITabRenderer tabRenderer = new DefaultTabRenderer();

	public JXTabbedPane() {
	    super();
	}

	public JXTabbedPane(int tabPlacement) {
	    super(tabPlacement);
	}

	public JXTabbedPane(int tabPlacement, int tabLayoutPolicy) {
	    super(tabPlacement, tabLayoutPolicy);
	}

	public ITabRenderer getTabRenderer() {
	    return tabRenderer;
	}

	public void setTabRenderer(ITabRenderer tabRenderer) {
	    this.tabRenderer = tabRenderer;
	}

	@Override
	public void addTab(String title, Component component) {
	    this.addTab(title, null, component, null);
	}

	@Override
	public void addTab(String title, Icon icon, Component component) {
	    this.addTab(title, icon, component, null);
	}

	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {
	    super.addTab(title, icon, component, tip);
	    int tabIndex = getTabCount() - 1;
	    Component tab = tabRenderer.getTabRendererComponent(this, title, icon, tabIndex);
	    super.setTabComponentAt(tabIndex, tab);
	}
    }

    interface ITabRenderer {

	public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex);

    }

    abstract class AbstractTabRenderer implements ITabRenderer {

	private String prototypeText = "";
	private Icon prototypeIcon = UIManager.getIcon("OptionPane.informationIcon");
	private int horizontalTextAlignment = SwingConstants.CENTER;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public AbstractTabRenderer() {
	    super();
	}

	public void setPrototypeText(String text) {
	    String oldText = this.prototypeText;
	    this.prototypeText = text;
	    firePropertyChange("prototypeText", oldText, text);
	}

	public String getPrototypeText() {
	    return prototypeText;
	}

	public Icon getPrototypeIcon() {
	    return prototypeIcon;
	}

	public void setPrototypeIcon(Icon icon) {
	    Icon oldIcon = this.prototypeIcon;
	    this.prototypeIcon = icon;
	    firePropertyChange("prototypeIcon", oldIcon, icon);
	}

	public int getHorizontalTextAlignment() {
	    return horizontalTextAlignment;
	}

	public void setHorizontalTextAlignment(int horizontalTextAlignment) {
	    this.horizontalTextAlignment = horizontalTextAlignment;
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return propertyChangeSupport.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
	    return propertyChangeSupport.getPropertyChangeListeners(propertyName);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
	    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	    PropertyChangeListener[] listeners = getPropertyChangeListeners();
	    for (int i = listeners.length - 1; i >= 0; i--) {
		listeners[i].propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	    }
	}
    }

    class DefaultTabRenderer extends AbstractTabRenderer implements PropertyChangeListener {

	private Component prototypeComponent;

	public DefaultTabRenderer() {
	    super();
	    prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
	    addPropertyChangeListener(this);
	}

	private Component generateRendererComponent(String text, Icon icon, int horizontalTabTextAlignmen) {
	    JPanel rendererComponent = new JPanel(new GridBagLayout());
	    rendererComponent.setOpaque(false);

	    GridBagConstraints c = new GridBagConstraints();
	    c.insets = new Insets(2, 4, 2, 4);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    rendererComponent.add(new JLabel(icon), c);

	    c.gridx = 1;
	    c.weightx = 1;
	    rendererComponent.add(new JLabel(text, horizontalTabTextAlignmen), c);

	    return rendererComponent;
	}

	@Override
	public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex) {
	    Component rendererComponent = generateRendererComponent(text, icon, getHorizontalTextAlignment());
	    int prototypeWidth = prototypeComponent.getPreferredSize().width;
	    int prototypeHeight = prototypeComponent.getPreferredSize().height;
	    rendererComponent.setPreferredSize(new Dimension(prototypeWidth, prototypeHeight));
	    return rendererComponent;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    String propertyName = evt.getPropertyName();
	    if ("prototypeText".equals(propertyName) || "prototypeIcon".equals(propertyName)) {
		this.prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
	    }
	}
    }

}
