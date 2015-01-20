package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.io.IOException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Matrix4;

public class GaiaSandboxActivity extends AndroidApplication {
    WakeLock mWakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
	this.mWakeLock.acquire();

	try {
	    GlobalConf.initialize(this.getAssets().open("conf/android/global.properties"), GaiaSandboxActivity.class.getResourceAsStream("/version"));
	} catch (IOException e) {
	    Log.e(this.getApplicationInfo().name, "Error initializing global configuration");
	    this.finish();
	}

	AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
	cfg.numSamples = MathUtilsd.clamp(GlobalConf.instance.POSTPROCESS_ANTIALIAS, 0, 16);
	cfg.depth = 8;
	cfg.stencil = 8;

	initialize(new GaiaSandbox(true), cfg);
	registerSensorListener();
    }

    GSSensorListener lis;

    private void registerSensorListener() {
	lis = new GSSensorListener();

	SensorManager sensorMan = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
	Sensor sensorAcce = sensorMan.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
	Sensor sensorMagn = sensorMan.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
	Sensor sensorOrien = sensorMan.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
	sensorMan.registerListener(lis, sensorAcce, SensorManager.SENSOR_DELAY_FASTEST);
	sensorMan.registerListener(lis, sensorMagn, SensorManager.SENSOR_DELAY_FASTEST);
	sensorMan.registerListener(lis, sensorOrien, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensorListeners() {
	SensorManager sensorMan = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
	Sensor sensorAcce = sensorMan.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
	Sensor sensorMagn = sensorMan.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
	Sensor sensorOrien = sensorMan.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
	sensorMan.unregisterListener(lis, sensorAcce);
	sensorMan.unregisterListener(lis, sensorMagn);
	sensorMan.unregisterListener(lis, sensorOrien);
    }

    private class GSSensorListener implements SensorEventListener {
	private float[] orientation;
	private float[] acceleration;

	float[] newLookAt, newUp;

	public float[] lookAt, up;

	public GSSensorListener() {
	    orientation = new float[3];
	    acceleration = new float[3];

	    lookAt = new float[4];
	    up = new float[4];

	    newLookAt = new float[] { 0, 0, -1, 1 };
	    newUp = new float[] { 0, 1, 0, 1 };

	    // Link to natural camera
	    NaturalCamera.upSensor = up;
	    NaturalCamera.lookAtSensor = lookAt;
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public void onSensorChanged(SensorEvent evt) {
	    int type = evt.sensor.getType();
	    //Smoothing the sensor.
	    if (type == Sensor.TYPE_MAGNETIC_FIELD) {
		orientation = lowPass(evt.values, orientation, 0.1f);
	    } else if (type == Sensor.TYPE_ACCELEROMETER) {
		acceleration = lowPass(evt.values, acceleration, 0.05f);
	    }

	    if ((type == Sensor.TYPE_MAGNETIC_FIELD) || (type == Sensor.TYPE_ACCELEROMETER)) {
		float newMat[] = new float[16];
		SensorManager.getRotationMatrix(newMat, null, acceleration, orientation);
		SensorManager.remapCoordinateSystem(newMat,
			SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
			newMat);
		Matrix4 matT = new Matrix4(newMat).tra();
		System.arraycopy(newLookAt, 0, lookAt, 0, 4);
		System.arraycopy(newUp, 0, up, 0, 4);
		Matrix4.mulVec(matT.val, lookAt);
		Matrix4.mulVec(matT.val, up);
	    }
	}

	/**
	 * Fast implementation of low-pass filter to smooth angle values coming from noisy sensor readings
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Simple_infinite_impulse_response_filter
	 */
	protected float[] lowPass(float[] input, float[] output, float ALPHA) {
	    //float ALPHA = 0.4f;
	    if (output == null)
		return input.clone();

	    for (int i = 0; i < input.length; i++) {
		output[i] = output[i] + ALPHA * (input[i] - output[i]);
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
}