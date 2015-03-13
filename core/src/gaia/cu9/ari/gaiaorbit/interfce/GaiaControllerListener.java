package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class GaiaControllerListener implements ControllerListener {

    CameraManager cam;
    IGui gui;

    private static final int ROLL_AXIS = 3;
    private static final int PITCH_AXIS = 1;
    private static final int YAW_AXIS = 0;
    private static final int SPEED_AXIS = 4;

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
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
	boolean treated = false;
	// y = x^3
	// http://www.wolframalpha.com/input/?i=y+%3D+sign%28x%29+*+x%5E2+%28x+from+-1+to+1%29}
	value = value * value * value;
	switch (axisCode) {
	case ROLL_AXIS:
	    cam.naturalCamera.setRoll(value * 1e-2f);
	    treated = true;
	    break;
	case PITCH_AXIS:
	    cam.naturalCamera.setPitch(value * 1.5e-2f);
	    treated = true;
	    break;
	case YAW_AXIS:
	    cam.naturalCamera.setYaw(value * 1.5e-2f);
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
