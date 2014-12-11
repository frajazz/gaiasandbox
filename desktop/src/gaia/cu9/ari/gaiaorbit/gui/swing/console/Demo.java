package gaia.cu9.ari.gaiaorbit.gui.swing.console;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Demo {
    public static void main(String... args) {
	Demo demo = new Demo();
	demo.createUI();
    }

    private void performDemo(ConsoleAPI consoleAPI) {
	consoleAPI.clear();

	consoleAPI.setStringAt(4, 57, "Demon", Color.BLUE, Color.GREEN);
	consoleAPI.setStringAt(19, 75, "'Stargate SG1' will live forever!", Color.CYAN, Color.DARK_GRAY);
	consoleAPI.setCharAt(3, 6, 'D');
	consoleAPI.setCharAt(3, 7, 'r', Color.YELLOW, Color.GRAY);
	consoleAPI.setCharAt(3, 8, 'a', Color.BLACK, Color.WHITE);
	consoleAPI.setCharAt(3, 9, 'g');
	consoleAPI.setStringAt(1, 79, "Test1");
	consoleAPI.setStringAt(5, 1, "Hello");
	consoleAPI.setCharAt(0, 0, 'S');
	consoleAPI.setCharAt(0, 1, 'T');
	consoleAPI.setCharAt(0, 2, 'U');
	consoleAPI.setStringAt(7, 17, "Demon", Color.BLUE, Color.YELLOW);
	consoleAPI.setCharAt(24, 77, 'U');
	consoleAPI.setCharAt(24, 78, 'V');
	consoleAPI.setCharAt(24, 79, 'W', Color.BLACK, Color.WHITE);

	consoleAPI.refresh();
    }

    private void createUI() {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		JFrame frame = new JFrame("Swing Text Console");

		TextConsole console = new TextConsole();

		frame.setLayout(new BorderLayout());
		frame.add(console, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);

		performDemo(console.getConsoleAPI());
	    }
	});
    }
}
