package gaia.cu9.ari.gaiaorbit.util;

import java.io.File;

/**
 * Wee utility class to check the operating system and the desktop environment.
 * It also offers retrieval of common system folders.
 * @author Toni Sagrista
 *
 */
public class SysUtils {

    public static boolean checkLinuxDesktop(String desktop) {
        try {
            String value = System.getenv("DESKTOP_SESSION");
            return value != null && !value.isEmpty() && value.equalsIgnoreCase(desktop);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return false;
    }

    public static boolean checkUnity() {
        return isLinux() && checkLinuxDesktop("ubuntu");
    }

    public static boolean checkGnome() {
        return isLinux() && checkLinuxDesktop("gnome");
    }

    public static boolean checkKDE() {
        return isLinux() && checkLinuxDesktop("kde");
    }

    public static boolean isLinux() {
        return getOSName().equalsIgnoreCase("linux");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static void main(String[] args) {
        System.out.println(getOSName());
        System.out.println("Unity: " + checkUnity());
        System.out.println("KDE: " + checkKDE());
        System.out.println("Gnome: " + checkGnome());
    }

    /**
     * Gets a file pointer to the home directory. It is $HOME/.gaiasandbox in Linux systems and C:\Users\$USERNAME\.gaiasandbox in Windows.
     * @return A pointer to the GaiaSandbox directory in the user's home.
     */
    public static File getGSHomeDir(){
        return new File(System.getProperty("user.home") + File.separator + ".gaiasandbox" + File.separator);
    }


}
