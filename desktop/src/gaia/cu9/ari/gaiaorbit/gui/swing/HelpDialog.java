package gaia.cu9.ari.gaiaorbit.gui.swing;

import com.badlogic.gdx.utils.BufferUtils;
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
import java.nio.IntBuffer;

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

public class HelpDialog extends I18nJFrame {

    JFrame frame;
    JButton okButton;

    public HelpDialog() {
        super(txt("gui.help.help") + " - " + GlobalConf.APPLICATION_NAME + " v" + GlobalConf.version.version);
        initialize();
        frame.pack();
        frame.setResizable(false);
        GuiUtility.centerOnScreen(frame);
        frame.setVisible(true);

        // Request focus
        frame.getRootPane().setDefaultButton(okButton);
        okButton.requestFocus();
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path,
            String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println(txt("gui.help.nofile", path));
            return null;
        }
    }

    private void initialize() {
        frame = this;
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Build content
        frame.setLayout(new BorderLayout());

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
                txt("gui.help.logo", GlobalConf.APPLICATION_NAME));
        JLabel label1 = new JLabel(icon, JLabel.CENTER);

        help.add(label1, "span,wrap");

        JLabel aux = new JLabel(txt("gui.help.usermanual"));
        Font boldFont = new Font(aux.getFont().getFontName(), Font.BOLD, aux.getFont().getSize());
        aux.setFont(boldFont);
        help.add(aux);

        JTextArea help1 = new JTextArea(txt("gui.help.help1"));
        help1.setEditable(false);
        help1.setBackground(null);
        help1.setLineWrap(true);
        help1.setWrapStyleWord(true);
        help.add(help1, "wrap");

        LinkLabel helpWebsite = new LinkLabel(GlobalConf.WEBPAGE);
        help.add(helpWebsite, "span,wrap");

        aux = new JLabel("Wiki");
        aux.setFont(boldFont);
        help.add(aux);

        JTextArea help2 = new JTextArea(txt("gui.help.help2"));
        help2.setEditable(false);
        help2.setBackground(null);
        help2.setLineWrap(true);
        help2.setWrapStyleWord(true);
        help.add(help2, "wrap");

        LinkLabel wikiWebsite = new LinkLabel(GlobalConf.WIKI);

        help.add(wikiWebsite, "span,wrap");

        JPanel readmepanel = new JPanel(new MigLayout("fillx", "[grow,fill]", ""));
        readmepanel.setBorder(new TitledBorder(txt("gui.help.readme")));
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
        build.setBorder(new TitledBorder(txt("gui.help.buildinfo")));
        aux = new JLabel(txt("gui.help.version", GlobalConf.APPLICATION_NAME));
        aux.setFont(boldFont);
        build.add(aux);
        build.add(new JLabel(GlobalConf.version.version), "wrap");
        aux = new JLabel(txt("gui.help.buildnumber"));
        aux.setFont(boldFont);
        build.add(aux);
        build.add(new JLabel(GlobalConf.version.build), "wrap");
        aux = new JLabel(txt("gui.help.buildtime"));
        aux.setFont(boldFont);
        build.add(aux);
        build.add(new JLabel(GlobalConf.version.buildtime), "wrap");
        aux = new JLabel(txt("gui.help.buildsys"));
        aux.setFont(boldFont);
        build.add(aux);

        JTextArea versionsystem = new JTextArea(GlobalConf.version.system);
        versionsystem.setEditable(false);
        versionsystem.setBackground(null);
        versionsystem.setLineWrap(true);
        versionsystem.setWrapStyleWord(true);
        build.add(versionsystem, "wrap");
        aux = new JLabel(txt("gui.help.builder"));
        aux.setFont(boldFont);
        build.add(aux);
        build.add(new JLabel(GlobalConf.version.builder), "wrap");

        JPanel java = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
        java.setBorder(new TitledBorder(txt("gui.help.javainfo")));
        aux = new JLabel(txt("gui.help.javaversion"));
        aux.setFont(boldFont);
        java.add(aux);
        java.add(new JLabel(System.getProperty("java.version")), "wrap");
        aux = new JLabel(txt("gui.help.javaname"));
        aux.setFont(boldFont);
        java.add(aux);
        java.add(new JLabel(System.getProperty("java.runtime.name")), "wrap");
        aux = new JLabel(txt("gui.help.javavmname"));
        aux.setFont(boldFont);
        java.add(aux);
        java.add(new JLabel(System.getProperty("java.vm.name")), "wrap");
        aux = new JLabel(txt("gui.help.javavmversion"));
        aux.setFont(boldFont);
        java.add(aux);
        java.add(new JLabel(System.getProperty("java.vm.version")), "wrap");
        aux = new JLabel(txt("gui.help.javavmvendor"));
        aux.setFont(boldFont);
        java.add(aux);
        java.add(new JLabel(System.getProperty("java.vm.vendor")), "wrap");

        String meminfostr = "";
        for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
            meminfostr += txt("gui.help.name") + ": " + mpBean.getName() + ": " + mpBean.getUsage() + "\n";
        }
        JTextArea meminfo = new JTextArea(meminfostr);
        meminfo.setEditable(false);
        JScrollPane memscroll = new JScrollPane(meminfo);
        memscroll.setPreferredSize(new Dimension(300, 80));

        aux = new JLabel(txt("gui.help.meminfo"));
        aux.setFont(boldFont);
        java.add(aux);
        java.add(memscroll, "wrap");

        JPanel opengl = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
        opengl.setBorder(new TitledBorder(txt("gui.help.openglinfo")));

        aux = new JLabel(txt("gui.help.openglversion"));
        aux.setFont(boldFont);
        opengl.add(aux);
        opengl.add(new JLabel(Gdx.gl.glGetString(GL20.GL_VERSION)), "wrap");
        aux = new JLabel(txt("gui.help.glslversion"));
        aux.setFont(boldFont);
        opengl.add(aux);
        opengl.add(new JLabel(Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)), "wrap");
        aux = new JLabel(txt("gui.help.glextensions"));
        aux.setFont(boldFont);
        opengl.add(aux);
        String glExtensionsString = Gdx.gl.glGetString(GL20.GL_EXTENSIONS).replace(' ', '\n');
        IntBuffer buf = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(Gdx.graphics.getGL20().GL_MAX_TEXTURE_SIZE, buf);
        int maxSize = buf.get(0);
        JTextArea glExtensions = new JTextArea("Max texture size: " + maxSize + "\n" + glExtensionsString);
        JScrollPane glExtensionsScroll = new JScrollPane(glExtensions);
        glExtensionsScroll.setPreferredSize(new Dimension(300, 80));
        glExtensions.setEditable(false);
        opengl.add(glExtensionsScroll, "wrap");

        JPanel systemPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        systemPanel.add(build, "wrap");
        systemPanel.add(java, "wrap");
        systemPanel.add(opengl, "wrap");

        /** ABOUT **/

        JPanel about = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
        about.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea intro = new JTextArea(txt("gui.help.gscredits", GlobalConf.version.version));
        intro.setEditable(false);
        intro.setBackground(null);
        intro.setLineWrap(true);
        intro.setWrapStyleWord(true);
        about.add(intro, "span,wrap");

        aux = new JLabel(txt("gui.help.homepage"));
        aux.setFont(boldFont);
        about.add(aux);
        about.add(new LinkLabel(GlobalConf.WEBPAGE), "span,wrap");
        about.add(new JLabel(" "), "span,wrap");

        JPanel author = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
        author.setBorder(new LineBorder(Color.LIGHT_GRAY));

        aux = new JLabel(txt("gui.help.author"));
        aux.setFont(boldFont);
        author.add(aux);
        author.add(new JLabel("Toni Sagristà Sellés"));
        author.add(new LinkLabel("tsagrista@ari.uni-heidelberg.de", "mailto:tsagrista@ari.uni-heidelberg.de"), "wrap");

        author.add(new JLabel(txt("gui.help.personalweb")));
        author.add(new LinkLabel("www.tonisagrista.com", "http://tonisagrista.com"), "span,wrap");

        JPanel contrib = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
        contrib.setBorder(new LineBorder(Color.LIGHT_GRAY));

        aux = new JLabel(txt("gui.help.contributors"));
        aux.setFont(boldFont);
        contrib.add(aux);
        contrib.add(new JLabel("Apl. Prof. Dr. Stefan Jordan"));
        contrib.add(new LinkLabel("jordan@ari.uni-heidelberg.de", "mailto:jordan@ari.uni-heidelberg.de"), "wrap");

        JPanel license = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill][]", ""));
        license.setBorder(new LineBorder(Color.LIGHT_GRAY));
        ImageIcon lgpl = createImageIcon("/img/license.png",
                txt("gui.help.logo", GlobalConf.APPLICATION_NAME));
        license.add(new JLabel(lgpl, JLabel.CENTER));

        JTextArea licensetext = new JTextArea(txt("gui.help.license"));
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
        tabbedPane.addTab(txt("gui.help.help"), helpPanel);
        tabbedPane.addTab(txt("gui.help.about"), aboutPanel);
        tabbedPane.addTab(txt("gui.help.system"), systemPanel);

        body.add(tabbedPane, "wrap");

        /** BUTTONS **/
        JPanel buttons = new JPanel(new MigLayout("", "push[]", ""));

        okButton = new JButton("Ok");
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

        frame.add(body, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);

        frame.pack();

    }

}
