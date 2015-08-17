package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/** 
 * Contains the logic to record the camera state at each frame. The format is as follows:
 * > time[s](float) cam_pos(double x3) cam_dir(double x3) cam_up(double x3)
 * 
 * The time is the time in seconds since the start of the recording, to synchronize with the current FPS in playing mode.
 * @author Toni Sagrista
 *
 */
public class CamRecorder implements IObserver {
    /** Singleton **/
    public static CamRecorder instance;
    private static final String sep = " ";

    public enum RecorderMode {
        RECORDING, PLAYING, IDLE
    }

    private RecorderMode mode;
    private BufferedWriter os;
    private BufferedReader is;
    private File f;
    private long startMs;
    float time;
    private IDateFormat df;

    public static void initialize() {
        instance = new CamRecorder();

    }

    public CamRecorder() {
        this.mode = RecorderMode.IDLE;
        df = DateFormatFactory.getFormatter("yyyy-MM-dd_HH:mm:ss");
        EventManager.instance.subscribe(this, Events.RECORD_CAMERA_CMD, Events.PLAY_CAMERA_CMD);
    }

    public void update(float dt, Vector3d position, Vector3d direction, Vector3d up) {
        switch (mode) {
        case RECORDING:
            if (os != null) {
                try {
                    time += dt;
                    os.append(Float.toString(time)).append(sep);
                    os.append(Double.toString(position.x)).append(sep).append(Double.toString(position.y)).append(sep).append(Double.toString(position.z));
                    os.append(sep).append(Double.toString(direction.x)).append(sep).append(Double.toString(direction.y)).append(sep).append(Double.toString(direction.z));
                    os.append(sep).append(Double.toString(up.x)).append(sep).append(Double.toString(up.y)).append(sep).append(Double.toString(up.z));
                    os.append("\n");
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
            break;
        case PLAYING:
            if (is != null) {
                try {
                    String line;
                    if ((line = is.readLine()) != null) {
                        String[] tokens = line.split("\\s+");
                        if (tokens.length != 0 && tokens[0].equals("settime")) {
                            EventManager.instance.post(Events.TIME_CHANGE_CMD, df.parse(tokens[1]));

                        } else {
                            // TODO use time to adapt FPS
                            float time = Parser.parseFloat(tokens[0]);
                            position.set(Parser.parseDouble(tokens[1]), Parser.parseDouble(tokens[2]), Parser.parseDouble(tokens[3]));
                            direction.set(Parser.parseDouble(tokens[4]), Parser.parseDouble(tokens[5]), Parser.parseDouble(tokens[6]));
                            up.set(Parser.parseDouble(tokens[7]), Parser.parseDouble(tokens[8]), Parser.parseDouble(tokens[9]));
                        }
                    } else {
                        // Finish off
                        is.close();
                        is = null;
                        mode = RecorderMode.IDLE;
                        // Stop camera
                        EventManager.instance.post(Events.CAMERA_STOP);
                        // Post notification
                        Logger.info(I18n.bundle.get("notif.cameraplay.done"));
                    }
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
            break;
        }

    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case RECORD_CAMERA_CMD:
            RecorderMode m = null;
            if (data[0] != null) {
                if ((Boolean) data[0]) {
                    m = RecorderMode.RECORDING;
                } else {
                    m = RecorderMode.IDLE;
                }
            } else {
                m = (mode == RecorderMode.RECORDING) ? RecorderMode.IDLE : RecorderMode.RECORDING;
            }

            if (m == RecorderMode.RECORDING) {
                // We start recording, prepare buffer!
                if (mode == RecorderMode.RECORDING) {
                    Logger.info(I18n.bundle.get("error.camerarecord.already"));
                    return;
                }
                f = new File(SysUtils.getGSHomeDir(), System.currentTimeMillis() + "_gscamera.dat");
                if (f.exists()) {
                    f.delete();
                }
                try {
                    f.createNewFile();
                    os = new BufferedWriter(new FileWriter(f));
                } catch (IOException e) {
                    Logger.error(e);
                    return;
                }
                try {
                    // Write time command
                    os.append("settime").append(sep).append(df.format(GlobalClock.clock.getTime())).append("\n");
                } catch (Exception e) {
                    Logger.error(e);
                }
                Logger.info(I18n.bundle.get("notif.camerarecord.start"));
                startMs = System.currentTimeMillis();
                time = 0;
                mode = RecorderMode.RECORDING;

            } else if (m == RecorderMode.IDLE) {
                // Flush and close
                if (mode == RecorderMode.IDLE) {
                    // No recording to cancel
                    return;
                }
                try {
                    os.close();
                } catch (IOException e) {
                    Logger.error(e);
                }
                os = null;
                long elapsed = System.currentTimeMillis() - startMs;
                startMs = 0;
                float secs = elapsed / 1000f;
                Logger.info(I18n.bundle.format("notif.camerarecord.done", f.getAbsolutePath(), secs));
                f = null;
                mode = RecorderMode.IDLE;
            }
            break;
        case PLAY_CAMERA_CMD:
            if (is != null) {
                throw new RuntimeException("Hey, we are already playing another movie!");
            }
            if (mode != RecorderMode.IDLE) {
                throw new RuntimeException("The recorder is busy! The current mode is " + mode);
            }
            String filepath = (String) data[0];
            f = new File(filepath);
            try {
                is = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                Logger.error(e);
                return;
            }
            Logger.info(I18n.bundle.format("notif.cameraplay.start", filepath));
            mode = RecorderMode.PLAYING;

            break;
        }

    }

}
