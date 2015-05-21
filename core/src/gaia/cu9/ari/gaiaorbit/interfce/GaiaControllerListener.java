package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;

public class GaiaControllerListener implements ControllerListener {

    CameraManager cam;
    IGui gui;

    private static final int ROLL_AXIS = 3;
    private static final int PITCH_AXIS = 1;
    private static final int YAW_AXIS = 0;
    private static final int SPEED_AXIS = 4;
    private static final int LEFT_TRIGGER_BUTTON = 4;
    private static final int RIGHT_TRIGGER_BUTTON = 5;

    public GaiaControllerListener(CameraManager cam, IGui gui) {
        this.cam = cam;
        this.gui = gui;
    }

    @Override
    public void connected(Controller controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnected(Controller controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        switch (buttonCode) {
        case LEFT_TRIGGER_BUTTON:
            cam.naturalCamera.setGamepadMultiplier(0.5);
            break;
        case RIGHT_TRIGGER_BUTTON:
            cam.naturalCamera.setGamepadMultiplier(0.1);
            break;
        }
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        switch (buttonCode) {
        case LEFT_TRIGGER_BUTTON:
        case RIGHT_TRIGGER_BUTTON:
            cam.naturalCamera.setGamepadMultiplier(1);
            break;
        }
        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        boolean treated = false;
        // y = x^4
        // http://www.wolframalpha.com/input/?i=y+%3D+sign%28x%29+*+x%5E2+%28x+from+-1+to+1%29}
        value = Math.signum(value) * value * value * value * value;
        switch (axisCode) {
        case ROLL_AXIS:

            if (cam.mode.equals(CameraMode.Focus)) {
                cam.naturalCamera.setRoll(value * 1e-2f);
            } else {
                // Use this for lateral movement
                cam.naturalCamera.setHorizontalRotation(value);
            }

            treated = true;
            break;
        case PITCH_AXIS:
            if (cam.mode.equals(CameraMode.Focus)) {
                cam.naturalCamera.setVerticalRotation(value * 0.1);
            } else {
                cam.naturalCamera.setPitch(value * 1.5e-2f);
            }

            treated = true;
            break;
        case YAW_AXIS:
            if (cam.mode.equals(CameraMode.Focus)) {
                cam.naturalCamera.setHorizontalRotation(value * 0.1);
            } else {
                cam.naturalCamera.setYaw(value * 1.5e-2f);
            }

            treated = true;
            break;
        case SPEED_AXIS:
            if (Math.abs(value) < 0.005)
                value = 0;
            cam.naturalCamera.setVelocity(-value);
            treated = true;
            break;
        }
        return treated;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        // TODO Auto-generated method stub
        return false;
    }

}
