package gaia.cu9.ari.gaiaorbit.desktop.gui.swing;

import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.jsplash.GuiUtility;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.script.JythonFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

import org.python.core.PyCode;
import org.python.core.PySyntaxError;


public class ScriptDialog extends I18nJFrame {

    JFrame frame;
    PyCode code;
    Color darkgreen, darkred;
    JButton okButton, cancelButton;

    public ScriptDialog() {
        super(txt("gui.script.title"));
        initialize();
        //frame.setPreferredSize(new Dimension(400, 200));
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
        //outConsole.setPreferredSize(new Dimension(350, 80));

        // Single file chooser field with custom root
        final JButton scriptChooser = new JButton(GlobalConf.program.SCRIPT_LOCATION);
        scriptChooser.addActionListener(new ActionListener() {
            JFileChooser chooser = null;

            @Override public void actionPerformed(ActionEvent e) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                chooser = new JFileChooser();

                chooser.setFileHidingEnabled(false);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setCurrentDirectory(new File(GlobalConf.program.SCRIPT_LOCATION));

                // Filter
                FileFilter filter = new FileNameExtensionFilter("Python scripts", new String[] {"py", "pyc"});
                chooser.addChoosableFileFilter(filter);
                chooser.setFileFilter(filter);


                int v = chooser.showOpenDialog(null);

                switch (v) {
                case JFileChooser.APPROVE_OPTION:
                    File choice = null;
                    if (chooser.getSelectedFile() != null) {
                        choice = chooser.getSelectedFile();

                        GlobalConf.program.SCRIPT_LOCATION = choice.getParent();
                        try {
                            code = JythonFactory.getInstance().compileJythonScript(choice);
                            outConsole.setText(txt("gui.script.ready"));
                            outConsole.setForeground(darkgreen);
                            okButton.setEnabled(true);
                        } catch (PySyntaxError e1) {
                            outConsole.setText(txt("gui.script.error", e1.type, e1.value));
                            outConsole.setForeground(darkred);
                            okButton.setEnabled(false);
                        } catch (Exception e2) {
                            outConsole.setText(txt("gui.script.error2", e2.getMessage()));
                            outConsole.setForeground(darkred);
                            okButton.setEnabled(false);
                        }

                    }

                    break;
                case JFileChooser.CANCEL_OPTION:
                case JFileChooser.ERROR_OPTION:
                }
                chooser.removeAll();
                chooser = null;
                System.setSecurityManager(sm);
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
