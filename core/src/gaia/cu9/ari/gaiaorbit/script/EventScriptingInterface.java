package gaia.cu9.ari.gaiaorbit.script;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.EventManager.TimeFrame;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.IGui;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.LruCache;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Implementation of the scripting interface using the event system.
 * @author Toni Sagrista
 *
 */
public class EventScriptingInterface implements IScriptingInterface, IObserver {
    private EventManager em;
    private AssetManager manager;
    private LruCache<String, Texture> textures;

    public EventScriptingInterface() {
        em = EventManager.instance;
        manager = GaiaSandbox.instance.manager;
        em.subscribe(this, Events.INPUT_EVENT);
    }

    public void initializeTextures() {
        if (textures == null) {
            textures = new LruCache<String, Texture>(100);
        }
    }

    @Override
    public void activateRealTimeFrame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.EVENT_TIME_FRAME_CMD, TimeFrame.REAL_TIME);
            }
        });
    }

    @Override
    public void activateSimulationTimeFrame() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.EVENT_TIME_FRAME_CMD, TimeFrame.SIMULATION_TIME);
            }
        });
    }

    @Override
    public void setHeadlineMessage(final String headline) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.POST_HEADLINE_MESSAGE, headline);
            }
        });
    }

    @Override
    public void setSubheadMessage(final String subhead) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.POST_SUBHEAD_MESSAGE, subhead);
            }
        });
    }

    @Override
    public void clearHeadlineMessage() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CLEAR_HEADLINE_MESSAGE);
            }
        });
    }

    @Override
    public void clearSubheadMessage() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CLEAR_SUBHEAD_MESSAGE);
            }
        });
    }

    @Override
    public void clearAllMessages() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CLEAR_MESSAGES);
            }
        });
    }

    @Override
    public void disableInput() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.INPUT_ENABLED_CMD, false);
            }
        });
    }

    @Override
    public void enableInput() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.INPUT_ENABLED_CMD, true);
            }
        });
    }

    @Override
    public void setCameraFocus(final String focusName) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.FOCUS_CHANGE_CMD, focusName, true);
            }
        });
    }

    @Override
    public void setCameraLock(final boolean lock) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.FOCUS_LOCK_CMD, lock);
            }
        });
    }

    @Override
    public void setCameraFree() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_MODE_CMD, CameraMode.Free_Camera);
            }
        });
    }

    @Override
    public void setCameraFov1() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1);
            }
        });
    }

    @Override
    public void setCameraFov2() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV2);
            }
        });
    }

    @Override
    public void setCameraFov1and2() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1and2);
            }
        });
    }

    @Override
    public void setCameraPostion(final double[] vec) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_POS_CMD, vec);
            }
        });
    }

    @Override
    public void setCameraDirection(final double[] dir) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_DIR_CMD, dir);
            }
        });
    }

    @Override
    public void setCameraUp(final double[] up) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_UP_CMD, up);
            }
        });
    }

    @Override
    public void setCameraSpeed(final float speed) {
        assert speed >= Constants.MIN_SLIDER && speed <= Constants.MAX_SLIDER : "Speed must be between " + Constants.MIN_SLIDER + " and " + Constants.MAX_SLIDER;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_SPEED_CMD, speed / 10f, false);
            }
        });
    }

    @Override
    public void setRotationCameraSpeed(final float speed) {
        assert speed >= Constants.MIN_SLIDER && speed <= Constants.MAX_SLIDER : "Speed must be between " + Constants.MIN_SLIDER + " and " + Constants.MAX_SLIDER;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.ROTATION_SPEED_CMD, MathUtilsd.lint(speed, Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED), false);
            }
        });
    }

    @Override
    public void setTurningCameraSpeed(final float speed) {
        assert speed >= Constants.MIN_SLIDER && speed <= Constants.MAX_SLIDER : "Speed must be between " + Constants.MIN_SLIDER + " and " + Constants.MAX_SLIDER;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.TURNING_SPEED_CMD, MathUtilsd.lint(speed, Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED), false);

            }
        });
    }

    @Override
    public void cameraForward(final double value) {
        assert value >= 0d && value <= 1d : "Value must be between 0 and 1";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_FWD, value);
            }
        });

    }

    @Override
    public void cameraRotate(final double deltaX, final double deltaY) {
        assert deltaX >= 0d && deltaX <= 1d && deltaY >= 0d && deltaY <= 1d : "DeltaX and deltaY must be between 0 and 1";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_ROTATE, deltaX, deltaY);
            }
        });

    }

    @Override
    public void cameraRoll(final double roll) {
        assert roll >= 0d && roll <= 1d : "Roll must be between 0 and 1";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_ROLL, roll);
            }
        });
    }

    @Override
    public void cameraTurn(final double deltaX, final double deltaY) {
        assert deltaX >= 0d && deltaX <= 1d && deltaY >= 0d && deltaY <= 1d : "DeltaX and deltaY must be between 0 and 1";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_TURN, deltaX, deltaY);
            }
        });
    }

    @Override
    public void cameraStop() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_STOP);
            }
        });

    }

    @Override
    public void cameraCenter() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.CAMERA_CENTER);
            }
        });
    }

    @Override
    public void setFov(final float newFov) {
        assert newFov >= Constants.MIN_FOV && newFov <= Constants.MAX_FOV : "Fov value must be between " + Constants.MIN_FOV + " and " + Constants.MAX_FOV;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.FOV_CHANGED_CMD, newFov);
            }
        });
    }

    @Override
    public void setVisibility(final String name, final boolean visible) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.TOGGLE_VISIBILITY_CMD, name, visible);
            }
        });
    }

    @Override
    public void setAmbientLight(final float value) {
        assert value >= Constants.MIN_SLIDER && value <= Constants.MAX_SLIDER : "Value must be between 0 and 100";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.AMBIENT_LIGHT_CMD, value / 100f);
            }
        });
    }

    @Override
    public void setSimulationTime(final long time) {
        assert time > 0 : "Time can not be negative";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.TIME_CHANGE_CMD, new Date(time));
            }
        });
    }

    @Override
    public void startSimulationTime() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.TOGGLE_TIME_CMD, true, false);
            }
        });
    }

    @Override
    public void stopSimulationTime() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.TOGGLE_TIME_CMD, false, false);
            }
        });
    }

    @Override
    public void setSimulationPace(final double pace) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.PACE_CHANGE_CMD, pace);
            }
        });
    }

    @Override
    public void setStarBrightness(final float brightness) {
        assert brightness >= Constants.MIN_SLIDER && brightness <= Constants.MAX_SLIDER : "Brightness value must be between 0 and 100";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.AMBIENT_LIGHT_CMD, MathUtilsd.lint(brightness, Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT));
            }
        });
    }

    @Override
    public void configureRenderOutput(int width, int height, int fps, String folder, String namePrefix) {
        assert width > 0 : "Width must be positive";
        assert height > 0 : "Height must be positive";
        assert fps > 0 : "FPS must be positive";
        assert folder != null && namePrefix != null : "Folder and file name prefix must not be null";
        em.post(Events.CONFIG_PIXEL_RENDERER, width, height, fps, folder, namePrefix);
    }

    @Override
    public void setFrameOutput(boolean active) {
        em.post(Events.FRAME_OUTPUT_CMD, active);
    }

    @Override
    public void goToObject(String name) {
        goToObject(name, -1);
    }

    @Override
    public void goToObject(String name, double distance) {
        ISceneGraph sg = GaiaSandbox.instance.sg;
        if (sg.containsNode(name)) {
            CelestialBody focus = sg.findFocus(name);
            NaturalCamera cam = GaiaSandbox.instance.cam.naturalCamera;

            // Post focus change
            if (!cam.isFocus(focus)) {
                em.post(Events.FOCUS_CHANGE_CMD, name);

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }

            // Wait til camera is facing focus
            while (!cam.facingFocus) {
                // Wait
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }

            double radius = focus.getRadius();
            /* target distance from surface of object */
            double target = radius * 5;
            if (distance > 0) {
                target = distance * Constants.KM_TO_U;
            }

            // Add forward movement while distance > target distance
            while (!weAreThere(focus.distToCamera - radius, target, 0.2)) {
                em.post(Events.CAMERA_FWD, (focus.distToCamera - radius < target ? -1d : 1d) * 0.05d);
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }

            // We can stop now
            em.post(Events.CAMERA_STOP);

        }
    }

    private boolean weAreThere(double distance, double target, double accuracyPercent) {
        double down = target - target * accuracyPercent / 2d;
        double up = target + target * accuracyPercent / 2d;
        return distance >= down && distance <= up;
    }

    @Override
    public double getDistanceTo(String objectName) {
        ISceneGraph sg = GaiaSandbox.instance.sg;
        if (sg.containsNode(objectName)) {
            SceneGraphNode object = sg.getNode(objectName);
            if (object instanceof AbstractPositionEntity) {
                AbstractPositionEntity ape = (AbstractPositionEntity) object;
                return (ape.distToCamera - ape.getRadius()) * Constants.U_TO_KM;
            }
        }
        return -1;
    }

    @Override
    public void setGuiScrollPosition(final float pixelY) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.GUI_SCROLL_POSITION_CMD, pixelY);
            }
        });
    }

    @Override
    public void displayMessageObject(final int id, final String message, final float x, final float y, final float r, final float g, final float b, final float a, final float fontSize) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.ADD_CUSTOM_MESSAGE, id, message, x, y, r, g, b, a, fontSize);
            }
        });
    }

    @Override
    public void displayTextObject(final int id, final String text, final float x, final float y, final float maxWidth, final float maxHeight, final float r, final float g, final float b, final float a, final float fontSize) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.ADD_CUSTOM_TEXT, id, text, x, y, maxWidth, maxHeight, r, g, b, a, fontSize);
            }
        });

    }

    @Override
    public void displayImageObject(final int id, final String path, final float x, final float y, final float r, final float g, final float b, final float a) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Texture tex = getTexture(path);
                em.post(Events.ADD_CUSTOM_IMAGE, id, tex, x, y, r, g, b, a);
            }
        });

    }

    @Override
    public void displayImageObject(final int id, final String path, final float x, final float y) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Texture tex = getTexture(path);
                em.post(Events.ADD_CUSTOM_IMAGE, id, tex, x, y);
            }
        });
    }

    @Override
    public void removeAllObjects() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.REMOVE_ALL_OBJECTS);
            }
        });
    }

    @Override
    public void removeObject(final int id) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.REMOVE_OBJECTS, new int[] { id });
            }
        });
    }

    @Override
    public void removeObjects(final int[] ids) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.REMOVE_OBJECTS, ids);
            }
        });
    }

    @Override
    public void maximizeInterfaceWindow() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.GUI_FOLD_CMD, false);
            }
        });
    }

    @Override
    public void minimizeInterfaceWindow() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.GUI_FOLD_CMD, true);
            }
        });
    }

    @Override
    public void setGuiPosition(final float x, final float y) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                em.post(Events.GUI_MOVE_CMD, x, y);
            }
        });
    }

    @Override
    public void waitForInput() {
        while (inputCode < 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                em.post(Events.JAVA_EXCEPTION, e);
            }
        }
        // Consume
        inputCode = -1;

    }

    @Override
    public void waitForEnter() {
        while (inputCode != Keys.ENTER) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                em.post(Events.JAVA_EXCEPTION, e);
            }
        }
        // Consume
        inputCode = -1;
    }

    @Override
    public void waitForInput(int keyCode) {
        while (inputCode != keyCode) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                em.post(Events.JAVA_EXCEPTION, e);
            }
        }
        // Consume
        inputCode = -1;
    }

    int inputCode = -1;

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case INPUT_EVENT:
            inputCode = (Integer) data[0];
            break;
        }

    }

    @Override
    public int getScreenWidth() {
        return Gdx.graphics.getWidth();
    }

    @Override
    public int getScreenHeight() {
        return Gdx.graphics.getHeight();
    }

    @Override
    public float[] getPositionAndSizeGui(String name) {
        IGui gui = GaiaSandbox.instance.gui;
        Actor actor = gui.getGuiStage().getRoot().findActor(name);
        if (actor != null) {
            float x = actor.getX();
            float y = actor.getY();
            // x and y relative to parent, so we need to add coordinates of parents up to top
            Group parent = actor.getParent();
            while (parent != null) {
                x += parent.getX();
                y += parent.getY();
                parent = parent.getParent();
            }
            return new float[] { x, y, actor.getWidth(), actor.getHeight() };
        } else {
            return null;
        }

    }

    @Override
    public String getVersionNumber() {
        return GlobalConf.version.version;
    }

    @Override
    public boolean waitFocus(String name, long timeoutMs) {
        long iniTime = System.currentTimeMillis();
        NaturalCamera cam = GaiaSandbox.instance.cam.naturalCamera;
        while (cam.focus == null || !cam.focus.name.equalsIgnoreCase(name)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                em.post(Events.JAVA_EXCEPTION, e);
            }
            long spent = System.currentTimeMillis() - iniTime;
            if (timeoutMs > 0 && spent > timeoutMs) {
                // Timeout!
                return true;
            }
        }
        return false;
    }

    private Texture getTexture(String path) {
        if (textures == null || !textures.containsKey(path)) {
            preloadTextures(path);
        }
        return textures.get(path);
    }

    @Override
    public void preloadTextures(String... paths) {
        initializeTextures();
        for (final String path : paths) {
            // This only works in async mode!
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    manager.load(path, Texture.class);
                }
            });
            while (!manager.isLoaded(path)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    em.post(Events.JAVA_EXCEPTION, e);
                }
            }
            Texture tex = manager.get(path, Texture.class);
            textures.put(path, tex);
        }
    }

    @Override
    public void startRecordingCameraPath() {
        em.post(Events.RECORD_CAMERA_CMD, true);
    }

    @Override
    public void stopRecordingCameraPath() {
        em.post(Events.RECORD_CAMERA_CMD, false);
    }

    @Override
    public void runCameraRecording(String path) {
        em.post(Events.PLAY_CAMERA_CMD, path);
    }

}
