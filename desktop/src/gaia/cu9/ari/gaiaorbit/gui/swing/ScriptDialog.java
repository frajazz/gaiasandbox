package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.GuiUtility;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.python.core.PyCode;
import org.python.core.PySyntaxError;

import sandbox.script.JythonFactory;

import com.alee.extended.filechooser.FilesSelectionListener;
import com.alee.extended.filechooser.WebFileChooserField;
import com.alee.laf.filechooser.WebFileChooserPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;

public class ScriptDialog extends I18nJFrame {

    JFrame frame;
    PyCode code;
    Color darkgreen, darkred;
    JButton okButton, cancelButton;

    public ScriptDialog() {
        super(txt("gui.script.title"));
        initialize();
        frame.setPreferredSize(new Dimension(300, 200));
        frame.pack();
        GuiUtility.centerOnScreen(frame);
        frame.setVisible(true);
    }

    private void initialize() {
        frame = this;
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        darkgreen = new Color(0f, .7f, 0f);
        darkred = new Color(.7f, 0f, 0f);

        // Build content
        frame.setLayout(new MigLayout("", "", ""));

        /** BODY **/
        JPanel body = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        body.setToolTipText(txt("gui.script.choose"));

        final JTextArea outConsole = new JTextArea(
                txt("gui.script.console")
                );
        outConsole.setLineWrap(true);
        outConsole.setWrapStyleWord(true);
        outConsole.setEditable(false);
        outConsole.setForeground(Color.gray);
        outConsole.setPreferredSize(new Dimension(300, 80));

        // Single file chooser field with custom root
        WebFileChooserField scriptChooser = new WebFileChooserField(frame);

        scriptChooser.setPreferredWidth(200);
        scriptChooser.setMultiSelectionEnabled(false);
        scriptChooser.setShowFileShortName(true);
        scriptChooser.setShowRemoveButton(true);
        scriptChooser.setShowFileExtensions(true);
        FileFilter pyff = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".py");
            }

            @Override
            public String getDescription() {
                return ".py Python scripts";
            }

        };
        // Increase scroll bar speed
        WebSplitPane wsp = ((WebSplitPane) ((WebFileChooserPanel) scriptChooser.getWebFileChooser().getComponents()[0]).getComponent(1));
        ((WebScrollPane) wsp.getComponent(1)).getVerticalScrollBar().setUnitIncrement(50);
        ((WebScrollPane) wsp.getComponent(2)).getVerticalScrollBar().setUnitIncrement(50);

        //	scriptChooser.getWebFileChooser().setCurrentDirectory(new File(System.getProperty("user.dir")));
        scriptChooser.getWebFileChooser().setCurrentDirectory(new File(GlobalConf.program.SCRIPT_LOCATION));
        scriptChooser.getWebFileChooser().addChoosableFileFilter(pyff);
        scriptChooser.getWebFileChooser().setFileFilter(pyff);
        scriptChooser.addSelectedFilesListener(new FilesSelectionListener() {
            @Override
            public void selectionChanged(List<File> files) {
                if (files.size() == 1) {
                    File file = files.get(0);
                    GlobalConf.program.SCRIPT_LOCATION = file.getParent();
                    try {
                        code = JythonFactory.getInstance().compileJythonScript(file);
                        outConsole.setText(txt("gui.script.ready"));
                        outConsole.setForeground(darkgreen);
                        okButton.setEnabled(true);
                    } catch (PySyntaxError e) {
                        outConsole.setText(txt("gui.script.error", e.type, e.value));
                        outConsole.setForeground(darkred);
                        okButton.setEnabled(false);
                    } catch (Exception e) {
                        outConsole.setText(txt("gui.script.error2", e.getMessage()));
                        outConsole.setForeground(darkred);
                        okButton.setEnabled(false);
                    }
                }

            }
        });

        final JCheckBox asyncCheckbox = new JCheckBox(txt("gui.script.runasync"), true);
        asyncCheckbox.setEnabled(false);

        body.add(new JLabel(txt("gui.script.choose")));
        body.add(scriptChooser, "wrap");
        body.add(outConsole, "span, wrap");
        body.add(asyncCheckbox, "span");

        /** BUTTONS **/
        JPanel buttons = new JPanel(new MigLayout("", "push[][]", ""));

        okButton = new JButton(txt("gui.script.run"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean async = asyncCheckbox.isSelected();
                if (frame.isDisplayable()) {
                    frame.dispose();
                }
                if (code != null) {
                    EventManager.instance.post(Events.RUN_SCRIPT_PYCODE, code, GlobalConf.program.SCRIPT_LOCATION, async);
                }
            }
        });
        okButton.setMinimumSize(new Dimension(100, 20));
        okButton.setEnabled(false);
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

        /** ADD TO FRAME **/
        frame.add(body, "wrap");
        frame.add(buttons, "grow");

    }
}
