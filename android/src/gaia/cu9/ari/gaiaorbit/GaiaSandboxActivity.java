package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Matrix4;

public class GaiaSandboxActivity extends AndroidApplication {
    WakeLock mWakeLock;
    GSSensorListener lis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
	this.mWakeLock.acquire();

	try {
	    InputStream version = GaiaSandboxActivity.class.getResourceAsStream("/version");
	    if (version == null) {
		// In case of running in 'developer' mode
		version = this.getAssets().open("data/dummyversion");
	    }
	    GlobalConf.initialize(this.getAssets().open("conf/android/global.properties"), version);
	} catch (IOException e) {
	    Log.e(this.getApplicationInfo().name, "Error initializing global configuration");
	    this.finish();
	}

	AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
	cfg.numSamples = MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16);
	cfg.depth = 8;
	cfg.stencil = 8;

	// Init sensors
	sensorMan = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
	sensorAcce = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	sensorMagn = sensorMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

	initialize(new GaiaSandbox(true), cfg);
    }

    SensorManager sensorMan;
    Sensor sensorAcce, sensorMagn;

    @Override
    protected void onResume() {
	super.onResume();
	this.mWakeLock.acquire();
	registerSensorListener();
	hideSoftKeyboard();
    }

    @Override
    protected void onPause() {
	super.onPause();
	this.mWakeLock.release();
	unregisterSensorListeners();
    }

    private void registerSensorListener() {
	lis = new GSSensorListener();

	sensorMan.registerListener(lis, sensorAcce, SensorManager.SENSOR_DELAY_GAME);
	sensorMan.registerListener(lis, sensorMagn, SensorManager.SENSOR_DELAY_GAME);
    }

    private void unregisterSensorListeners() {
	sensorMan.unregisterListener(lis, sensorAcce);
	sensorMan.unregisterListener(lis, sensorMagn);
	lis = null;
    }

    private class GSSensorListener implements SensorEventListener {
	private float[] orientation;
	private float[] acceleration;

	float[] newLookAt, newUp;

	Matrix4 matT;

	private float Rtmp[] = new float[16];

	public GSSensorListener() {
	    orientation = new float[3];
	    acceleration = new float[3];

	    newLookAt = new float[] { 0, 0, -1, 1 };
	    newUp = new float[] { 0, 1, 0, 1 };

	    matT = new Matrix4();

	    // Init camera up and lookat links
	    NaturalCamera.upSensor = new float[4];
	    NaturalCamera.lookAtSensor = new float[4];

	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public void onSensorChanged(SensorEvent evt) {
	    int type = evt.sensor.getType();
	    //Smoothing the sensor
	    if (type == Sensor.TYPE_MAGNETIC_FIELD) {
		orientation = lowPass2(evt.values, orientation);
	    } else if (type == Sensor.TYPE_ACCELEROMETER) {
		acceleration = lowPass2(evt.values, acceleration);
	    }

	    if (acceleration != null && orientation != null) {
		boolean success = SensorManager.getRotationMatrix(Rtmp, null, acceleration, orientation);

		if (success) {
		    // THIS WORKS
		    SensorManager.remapCoordinateSystem(Rtmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, Rtmp);
		    matT.set(Rtmp).tra();

		    // Synchronize
		    synchronized (NaturalCamera.lookAtSensor) {
			System.arraycopy(newLookAt, 0, NaturalCamera.lookAtSensor, 0, 4);
			Matrix4.mulVec(matT.val, NaturalCamera.lookAtSensor);
			switchIndices(NaturalCamera.lookAtSensor);

			System.arraycopy(newUp, 0, NaturalCamera.upSensor, 0, 4);
			Matrix4.mulVec(matT.val, NaturalCamera.upSensor);
			switchIndices(NaturalCamera.upSensor);

		    }
		}
	    }
	}

	private void switchIndices(float[] vec) {
	    float x, y, z;
	    x = vec[0];
	    y = vec[1];
	    z = vec[2];
	    vec[0] = -x;
	    vec[1] = z;
	    vec[2] = y;
	}

	static final float ALPHA = 0.1f;

	/**
	 * Fast implementation of low-pass filter to smooth angle values coming from noisy sensor readings
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Simple_infinite_impulse_response_filter
	 */
	protected float[] lowPass(float[] input, float[] output) {
	    if (output == null)
		return input.clone();

	    for (int i = 0; i < input.length; i++) {
		output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}

	protected float[] lowPass2(float[] input, float[] output) {
	    if (output == null)
		return input.clone();

	    for (int i = 0; i < input.length; i++) {
		output[i] = (input[i] * ALPHA) + (output[i] * (1.0f - ALPHA));
	    }
	    return output;
	}

    }

    @Override
    public void onDestroy() {
	unregisterSensorListeners();
	this.mWakeLock.release();
	super.onDestroy();
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
	if (getCurrentFocus() != null) {
	    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}
    }
}