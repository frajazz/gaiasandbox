package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.gui.swing.components.LinkLabel;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.GuiUtility;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;

public class HelpDialog extends JFrame {

    JFrame frame;

    public HelpDialog() {
	super("Help - Gaia Sandbox v" + GlobalConf.instance.VERSION.version);
	initialize();
	frame.pack();
	frame.setResizable(false);
	GuiUtility.centerOnScreen(frame);
	frame.setVisible(true);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path,
	    String description) {
	java.net.URL imgURL = getClass().getResource(path);
	if (imgURL != null) {
	    return new ImageIcon(imgURL, description);
	} else {
	    System.err.println("Couldn't find file: " + path);
	    return null;
	}
    }

    private void initialize() {
	frame = this;
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	// Build content
	frame.setLayout(new BorderLayout(0, 0));

	/** BODY **/
	JPanel body = new JPanel(new MigLayout("", "[grow,fill][]", ""));

	/** TABBED PANEL **/

	JTabbedPane tabbedPane = new JTabbedPane();

	/**
	 * ====== HELP TAB =======
	 */

	/** HELP **/
	JPanel help = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
	help.setBorder(new EmptyBorder(10, 10, 10, 10));

	ImageIcon icon = createImageIcon("/img/gaiasandboxlogo.png",
		"Gaia Sandbox logo");
	JLabel label1 = new JLabel(icon, JLabel.CENTER);

	help.add(label1, "span,wrap");

	JLabel aux = new JLabel("User manual");
	Font boldFont = new Font(aux.getFont().getFontName(), Font.BOLD, aux.getFont().getSize());
	aux.setFont(boldFont);
	help.add(aux);

	JTextArea help1 = new JTextArea("If you need help please visit the help section in our website where you can get the user manual");
	help1.setEditable(false);
	help1.setBackground(null);
	help1.setLineWrap(true);
	help1.setWrapStyleWord(true);
	help.add(help1, "wrap");

	LinkLabel helpWebsite = new LinkLabel("http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasandbox");
	help.add(helpWebsite, "span,wrap");

	aux = new JLabel("Wiki");
	aux.setFont(boldFont);
	help.add(aux);

	JTextArea help2 = new JTextArea("You can also visit our wiki, which contains the most up to date information");
	help2.setEditable(false);
	help2.setBackground(null);
	help2.setLineWrap(true);
	help2.setWrapStyleWord(true);
	help.add(help2, "wrap");

	LinkLabel wikiWebsite = new LinkLabel("https://github.com/ari-zah/gaiasandbox/wiki");

	help.add(wikiWebsite, "span,wrap");

	JPanel readmepanel = new JPanel(new MigLayout("fillx", "[grow,fill]", ""));
	readmepanel.setBorder(new TitledBorder("Readme file"));
	FileHandle readmefile = Gdx.files.internal("README.md");
	if (!readmefile.exists()) {
	    readmefile = Gdx.files.internal("../README.md");
	}
	JTextArea readme = new JTextArea(readmefile.readString(), 15, 35);
	readme.setEditable(false);
	readme.setLineWrap(true);
	readme.setWrapStyleWord(true);
	JScrollPane readmescroll = new JScrollPane(readme);
	readmescroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	readmepanel.add(readmescroll, "span");

	help.add(readmepanel, "span, wrap");

	JPanel helpPanel = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
	helpPanel.add(help);

	/** SYSTEM **/
	JPanel build = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
	build.setBorder(new TitledBorder("Build information"));
	aux = new JLabel("Gaia Sandbox version");
	aux.setFont(boldFont);
	build.add(aux);
	build.add(new JLabel(GlobalConf.instance.VERSION.version), "wrap");
	aux = new JLabel("Build number");
	aux.setFont(boldFont);
	build.add(aux);
	build.add(new JLabel(GlobalConf.instance.VERSION.build), "wrap");
	aux = new JLabel("Build time");
	aux.setFont(boldFont);
	build.add(aux);
	build.add(new JLabel(GlobalConf.instance.VERSION.buildtime), "wrap");
	aux = new JLabel("Build system");
	aux.setFont(boldFont);
	build.add(aux);

	JTextArea versionsystem = new JTextArea(GlobalConf.instance.VERSION.system);
	versionsystem.setEditable(false);
	versionsystem.setBackground(null);
	versionsystem.setLineWrap(true);
	versionsystem.setWrapStyleWord(true);
	build.add(versionsystem, "wrap");
	aux = new JLabel("Builder");
	aux.setFont(boldFont);
	build.add(aux);
	build.add(new JLabel(GlobalConf.instance.VERSION.builder), "wrap");

	JPanel java = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
	java.setBorder(new TitledBorder("Java information"));
	aux = new JLabel("Java version");
	aux.setFont(boldFont);
	java.add(aux);
	java.add(new JLabel(System.getProperty("java.version")), "wrap");
	aux = new JLabel("Java runtime name");
	aux.setFont(boldFont);
	java.add(aux);
	java.add(new JLabel(System.getProperty("java.runtime.name")), "wrap");
	aux = new JLabel("Java VM name");
	aux.setFont(boldFont);
	java.add(aux);
	java.add(new JLabel(System.getProperty("java.vm.name")), "wrap");
	aux = new JLabel("Java VM version");
	aux.setFont(boldFont);
	java.add(aux);
	java.add(new JLabel(System.getProperty("java.vm.version")), "wrap");
	aux = new JLabel("Java VM vendor");
	aux.setFont(boldFont);
	java.add(aux);
	java.add(new JLabel(System.getProperty("java.vm.vendor")), "wrap");

	String meminfostr = "";
	for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
	    meminfostr += "Name: " + mpBean.getName() + ": " + mpBean.getUsage() + "\n";
	}
	JTextArea meminfo = new JTextArea(meminfostr);
	meminfo.setEditable(false);
	JScrollPane memscroll = new JScrollPane(meminfo);
	memscroll.setPreferredSize(new Dimension(300, 80));

	aux = new JLabel("Memory info");
	aux.setFont(boldFont);
	java.add(aux);
	java.add(memscroll, "wrap");

	JPanel opengl = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
	opengl.setBorder(new TitledBorder("OpenGL information"));

	aux = new JLabel("OpenGL version");
	aux.setFont(boldFont);
	opengl.add(aux);
	opengl.add(new JLabel(Gdx.gl.glGetString(GL20.GL_VERSION)), "wrap");
	aux = new JLabel("GLSL version");
	aux.setFont(boldFont);
	opengl.add(aux);
	opengl.add(new JLabel(Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)), "wrap");

	JPanel systemPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	systemPanel.add(build, "wrap");
	systemPanel.add(java, "wrap");
	systemPanel.add(opengl, "wrap");

	/** ABOUT **/

	JPanel about = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
	about.setBorder(new EmptyBorder(10, 10, 10, 10));

	JTextArea intro = new JTextArea("The Gaia Sandbox (" + GlobalConf.instance.VERSION.version + ") has"
		+ " been developed in the Astronomisches Rechen-Institut"
		+ " - ZAH - Heidelberg Universität.");
	intro.setEditable(false);
	intro.setBackground(null);
	intro.setLineWrap(true);
	intro.setWrapStyleWord(true);
	about.add(intro, "span,wrap");

	aux = new JLabel("Home page");
	aux.setFont(boldFont);
	about.add(aux);
	about.add(new LinkLabel("http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasandbox/"), "span,wrap");
	about.add(new JLabel(" "), "span,wrap");

	JPanel author = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
	author.setBorder(new LineBorder(Color.LIGHT_GRAY));

	aux = new JLabel("Author");
	aux.setFont(boldFont);
	author.add(aux);
	author.add(new JLabel("Toni Sagristà Sellés"));
	author.add(new LinkLabel("tsagrista@ari.uni-heidelberg.de", "mailto:tsagrista@ari.uni-heidelberg.de"), "wrap");

	author.add(new JLabel("Personal webpage"));
	author.add(new LinkLabel("www.tonisagrista.com", "http://tonisagrista.com"), "span,wrap");

	JPanel contrib = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
	contrib.setBorder(new LineBorder(Color.LIGHT_GRAY));

	aux = new JLabel("Contributors");
	aux.setFont(boldFont);
	contrib.add(aux);
	contrib.add(new JLabel("Apl. Prof. Dr. Stefan Jordan"));
	contrib.add(new LinkLabel("jordan@ari.uni-heidelberg.de", "mailto:jordan@ari.uni-heidelberg.de"), "wrap");

	JPanel license = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
	license.setBorder(new LineBorder(Color.LIGHT_GRAY));
	ImageIcon lgpl = createImageIcon("/img/license.png",
		"Gaia Sandbox logo");
	license.add(new JLabel(lgpl, JLabel.CENTER));

	JTextArea licensetext = new JTextArea("This software is published under the LGPL (Lesser General Public License) license.");
	licensetext.setEditable(false);
	licensetext.setBackground(null);
	licensetext.setLineWrap(true);
	licensetext.setWrapStyleWord(true);
	license.add(licensetext, "wrap");

	license.add(new LinkLabel("https://www.gnu.org/licenses/lgpl.html"), "skip,wrap");

	JPanel supporting = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][grow,fill]", ""));
	supporting.setBorder(new LineBorder(Color.LIGHT_GRAY));

	icon = createImageIcon("/img/zah.png",
		"ZAH");
	label1 = new JLabel(icon, JLabel.CENTER);
	supporting.add(label1);
	icon = createImageIcon("/img/dlr.png",
		"DLR");
	label1 = new JLabel(icon, JLabel.CENTER);
	supporting.add(label1);
	icon = createImageIcon("/img/bwt.png",
		"BWT");
	label1 = new JLabel(icon, JLabel.CENTER);
	supporting.add(label1);

	about.add(author, "span,wrap");
	about.add(contrib, "span,wrap");
	about.add(license, "span,wrap");
	about.add(supporting, "span,wrap");

	JPanel aboutPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
	aboutPanel.add(about);

	/**
	 * ADD PANELS
	 */
	tabbedPane.addTab("Help", helpPanel);
	tabbedPane.addTab("About", aboutPanel);
	tabbedPane.addTab("System", systemPanel);

	body.add(tabbedPane);

	/** BUTTONS **/
	JPanel buttons = new JPanel(new MigLayout("", "push[][]", ""));

	JButton okButton = new JButton("Ok");
	okButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (frame.isDisplayable()) {
		    frame.dispose();
		}
	    }

	});
	okButton.setMinimumSize(new Dimension(100, 20));

	buttons.add(okButton);

	frame.add(body, BorderLayout.NORTH);
	frame.add(buttons, BorderLayout.SOUTH);

    }
}
