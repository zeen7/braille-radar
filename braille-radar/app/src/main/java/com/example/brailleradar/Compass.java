package com.example.brailleradar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Compass {
    // TODO: Figure out correct azimuth calculation, calibrate speed of azimuth update
    // not important: figure out why mainlooper works but not handlerthread???
//    private HandlerThread handlerThread;
    private final SensorManager sensorManager;
    private final SensorEventListener listener;
    public float azimuth;

    public Compass(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        listener = new SensorEventListener() {
            final float[] accelerometerReading = new float[3];
            private final float[] magnetometerReading = new float[3];
            private final float[] rotationMatrix = new float[9];

            private final float[] rotationMatrix2 = new float[9];
            private final float[] orientationAngles2 = new float[3];
            private float[] lastAccelerometer = new float[3];
            private float[] lastMagnetometer = new float[3];
            private boolean isLastAccelerometerArrayCopied = false;
            private boolean isLastMagnetometerArrayCopied = false;


            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, accelerometerReading, 0,
                            accelerometerReading.length);
                    lastAccelerometer = lowPass(event.values.clone(), lastAccelerometer);
                    isLastAccelerometerArrayCopied = true;
                } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, magnetometerReading, 0,
                            magnetometerReading.length);
                    lastMagnetometer = lowPass(event.values.clone(), lastMagnetometer);
                    isLastMagnetometerArrayCopied = true;
                }
                if (isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied) {
                    SensorManager.getRotationMatrix(rotationMatrix2, null, lastAccelerometer, lastMagnetometer);
                    SensorManager.getOrientation(rotationMatrix2, orientationAngles2);
                    float azimuthInRadians = orientationAngles2[0];
                    azimuth = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                    if (!compassListeners.isEmpty()) {
                        for(CompassUpdateListener listener : compassListeners) {
                            listener.onCompassUpdated();
                        }
                    }
                }
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading,
                        magnetometerReading);
//                SensorManager.getOrientation(rotationMatrix, orientationAngles);
//                double azimuth = (Math.toDegrees(Math.atan2((rotationMatrix[1] - rotationMatrix[3]),
//                        (rotationMatrix[0] + rotationMatrix[4]))) + 360) % 360;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            private float[] lowPass(float[] input, float[] output) {
                if (output == null) return input;
                for (int i = 0; i < input.length; i++) {
                    float ALPHA = 0.1f;
                    output[i] = output[i] + ALPHA * (input[i] - output[i]);
                }
                return output;
            }
        };
    }

    public void start() {
//        handlerThread = new HandlerThread("CompassListenerThread");
//        handlerThread.start();
//        Handler handler = new Handler(handlerThread.getLooper());
        Handler handler = new Handler(Looper.getMainLooper());

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI, handler);
        }

        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(listener, magneticField,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI, handler);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(listener);
//        if (handlerThread.isAlive()) {
//            handlerThread.quit();
//        }
    }

    public interface CompassUpdateListener {
        void onCompassUpdated();
    }
    private final List<CompassUpdateListener> compassListeners = new CopyOnWriteArrayList<>();

    public void setCompassUpdateListener(CompassUpdateListener listener) {
        this.compassListeners.add(listener);
    }

    public void removeCompassUpdateListener(CompassUpdateListener listener) {
        if (!compassListeners.isEmpty()) {
            compassListeners.remove(listener);
        }
    }
}
