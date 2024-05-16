package com.example.brailleradar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.example.brailleradar.models.TagInfo;
import com.example.brailleradar.services.ServiceCallback;
import com.example.brailleradar.services.TagService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Scanner implements Runnable {
    //TODO: Turn off google ble cache, figure out if scanner as runnable is necessary
    //not important: figure out better distance
    static final int TIMEOUT = 10000;

    private final BluetoothAdapter bluetoothAdapter;

    private final List<LogEntry> logContents = new CopyOnWriteArrayList<>();

    private final TagService tagService = new TagService();

    private int currentFloor = -1;

    public Scanner(Activity activity) {
        bluetoothAdapter = activity.getSystemService(BluetoothManager.class).getAdapter();
    }

    @SuppressLint("MissingPermission")
    public void run() {
        if (!bluetoothAdapter.isEnabled()) {
            Log.d("INFO", "Bluetooth not enabled");
            while (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
                Log.d("INFO", "Bluetooth enabled, beginning scan");
            }
        }
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanCallback leScanCallback =
                new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        //String mac_address = result.getDevice().getAddress();
                        String name = result.getDevice().getName();
                        System.out.println("Device discovered: " + name);
                        if (name != null && name.startsWith("BR")) {
                            //double distance = calculateDistance(result.getTxPower(), result.getRssi());
                            long time = System.currentTimeMillis();
                            updateLog(name, result.getRssi(), time);
                            determineCurrentFloor();
                        }
                    }
                };
        bluetoothLeScanner.startScan(leScanCallback);
        long prev_time = System.currentTimeMillis();
        while (true) {
            long cur_time = System.currentTimeMillis();
            if (cur_time - prev_time > TIMEOUT) {
                cleanLog(cur_time);
                prev_time = cur_time;
            }
            if (Thread.currentThread().isInterrupted()) {
                bluetoothLeScanner.stopScan(leScanCallback);
                break;
            }
        }
    }

    private void updateLog(String name, int rssi, long time) {
        Log.d("INFO", "Updating log entry " + name + ": " + time);
        for (LogEntry entry : logContents) {
            if (entry.name.equals(name)) {
                entry.time = time;
                entry.calculateDistance(rssi);
                Log.d("INFO", "Updated log entry " + name + " with: " + entry.filteredDistance + ", " + time);
                return;
            }
        }

        String location = null;
        List <TagInfo> tagList = TagListSingleton.getTagList();
        for (TagInfo tag : tagList) {
            if (tag.getDeviceName().equals(name)) {
                location = tag.getLocation();
            }
        }
        LogEntry log_entry = new LogEntry(name, time, location);
        log_entry.calculateDistance(rssi);
        logContents.add(log_entry);
        Log.d("INFO", "Added log entry " + name + " with: " + log_entry.filteredDistance + ", " + time);

        // Increment the tag usage data
        tagService.incrementTagUsageDataByDeviceName(name, new ServiceCallback() {
            @Override
            public void onSuccess(JsonNode tag) {
                Log.d("INFO", "Incremented " + name + " tag usage data. todaysPings: " + tag.get("todaysPings").asInt() + ", lastIntervalPings: " + tag.get("lastIntervalPings").asInt() + ", totalPings: " + tag.get("totalPings").asInt());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("INFO", "Failed to increment tag usage data for " + name + ". " + t.toString());
            }
        });
    }

    private void cleanLog(long cur_time) {
        logContents.removeIf(entry -> cur_time - entry.time > TIMEOUT);
        //updateTextViewLog();
        Log.d("INFO", "Removed stale entries at time: " + cur_time);
    }

    public List<String> getFilteredDetectedTags() {
        Map<Integer, String> filterList = FilterListSingleton.getInstance();
        if (filterList.size() != 0) {
            List<String> ret = new ArrayList<>();
            for (LogEntry log_entry : logContents) {
                for (Map.Entry<Integer, String> filter_entry : filterList.entrySet()) {
                    if (log_entry.name.startsWith(filter_entry.getValue())) {
                        ret.add(log_entry.name);
                    }
                }
            }
            return ret;
        } else {
            return getAllDetectedTags();
        }
    }

    public List<String> getAllDetectedTags() {
        List<String> ret = new ArrayList<>();
        for (LogEntry log_entry : logContents) {
            ret.add(log_entry.name);
        }
        return ret;
    }

    public float getTagDistanceIfDetected(String tagName) {
        for (LogEntry log_entry : logContents) {
            if (log_entry.name.equals(tagName)) {
                return log_entry.filteredDistance;
            }
        }
        return 0;
    }

//    private double calculateDistance(int txPower, int rssi) {
//        //return Math.pow(10d, (-54.606671799999994 - rssi)/20);
//        return Math.pow(10, ((txPower - rssi) * 1.0) / 20);
//    }

    private static class LogEntry {
        public String name, location;
        public long time;
        public float filteredDistance;

        private final KalmanFilter kalmanFilter;
        private static final double KALMAN_R = 0.125d;
        private static final double KALMAN_Q = 0.5d;

        public LogEntry(String name, long time, String location) {
            this.name = name;
            this.time = time;
            this.location = location;
            kalmanFilter = new KalmanFilter(KALMAN_R, KALMAN_Q);
        }

        private static final double txPower = -58;//-68.332; //-65.61;
        public void calculateDistance(int rssi) {
            //return Math.pow(10d, (-54.606671799999994 - rssi)/20);
            double filteredRssi = kalmanFilter.applyFilter(rssi);
            filteredDistance = (float)Math.pow(10, ((txPower - filteredRssi)) / 20);
        }
    }

    private static class KalmanFilter {
        private final double R;   //  Process Noise
        private final double Q;   //  Measurement Noise
        private final double A;   //  State Vector
        private final double B;   //  Control Vector
        private final double C;   //  Measurement Vector

        private Double x;   //  Filtered Measurement Value (No Noise)
        private double cov; //  Covariance

//        public KalmanFilter(double r, double q, double a, double b, double c) {
//            R = r;
//            Q = q;
//            A = a;
//            B = b;
//            C = c;
//        }

        public KalmanFilter(double r, double q){
            R = r;
            Q = q;
            A = 1;
            B = 0;
            C = 1;
        }

        public double applyFilter(double rssi){
            return applyFilter(rssi, 0.0d);
        }

        /**
         * Filters a measurement
         *
         * @param measurement The measurement value to be filtered
         * @param u The controlled input value
         * @return The filtered value
         */
        public double applyFilter(double measurement, double u) {
            double predX;           //  Predicted Measurement Value
            double K;               //  Kalman Gain
            double predCov;         //  Predicted Covariance
            if (x == null) {
                x = (1 / C) * measurement;
                cov = (1 / C) * Q * (1 / C);
            } else {
                predX = predictValue(u);
                predCov = getUncertainty();
                K = predCov * C * (1 / ((C * predCov * C) + Q));
                x = predX + K * (measurement - (C * predX));
                cov = predCov - (K * C * predCov);
            }
            return x;
        }

        private double predictValue(double control){
            return (A * x) + (B * control);
        }

        private double getUncertainty(){
            return ((A * cov) * A) + R;
        }
    }


    public int getCurrentFloor() {
        return currentFloor;
    }

    private void determineCurrentFloor() {
        List<TagInfo> tagList = TagListSingleton.getTagList();
        if(tagList.size() == 0) {
            return;
        }

//        int numLogs = logContents.size();
        HashMap<Integer, Double> floorWeight = new HashMap<Integer, Double>();
        for(LogEntry log : logContents) {
            // Check all tags that are being sensed, of all of the tags
            // The highest weighted average for the floors wins:  1/(distance_rssi)
            int floor = getFloor(log.name);
            if(floor != -1) {
                floorWeight.put(floor, floorWeight.getOrDefault(floor, 0.0) + (1/log.filteredDistance));
            }
        }

        int bestFloor = -1;
        double highestFloorWeight = 0.0;
        for(Map.Entry<Integer, Double> entry : floorWeight.entrySet()) {
            Integer floor = entry.getKey();
            Double weight = entry.getValue();

            if (weight > highestFloorWeight) {
                bestFloor = floor;
                highestFloorWeight = weight;
            }
        }

        currentFloor = bestFloor;
    }

    private int getFloor(String deviceName) {
        TagInfo tag = getTag(deviceName);
        if(tag == null) {
            return -1;
        }
        return tag.getFloor();
    }

    private TagInfo getTag(String deviceName) {
        TagInfo targetTag = null;
        List<TagInfo> tagList = TagListSingleton.getTagList();
        for(TagInfo tag : tagList) {
            if(tag.getDeviceName().equals(deviceName)) {
                targetTag = tag;
                break;
            }
        }
        return targetTag;
    }
}
