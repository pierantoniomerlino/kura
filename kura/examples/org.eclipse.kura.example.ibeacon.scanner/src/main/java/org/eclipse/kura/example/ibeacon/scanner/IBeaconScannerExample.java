package org.eclipse.kura.example.ibeacon.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeacon;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeaconService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconFactory;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconListener;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconScanner;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.internal.ble.ibeacon.BluetoothLeIBeaconFactory;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IBeaconScannerExample implements ConfigurableComponent, BluetoothLeBeaconListener<BluetoothLeIBeacon> {

    private static final Logger logger = LoggerFactory.getLogger(IBeaconScannerExample.class);

    private static final String PROPERTY_ENABLE = "enableScanning";
    private static final String PROPERTY_TOPIC_PREFIX = "topicPrefix";
    private static final String PROPERTY_INAME = "iname";
    private static final String PROPERTY_RATE_LIMIT = "rate_limit";
    private static final String PROPERTY_TIMEOUT = "timeout";

    private String adapterName;
    private String topicPrefix;
    private int rateLimit;
    private Boolean enableScanning;
    private int timeout;

    private ExecutorService worker;
    private Future<?> handle;

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private BluetoothLeIBeaconService bluetoothLeIBeaconService;
    private BluetoothLeBeaconScanner<BluetoothLeIBeacon> bluetoothLeIBeaconScanner;
    private BluetoothLeBeaconFactory<BluetoothLeIBeacon> bluetoothLeIBeaconFactory;
    private CloudService cloudService;
    private CloudClient cloudClient;
    private Map<String, Long> publishTimes;

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    public void unsetBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = null;
    }

    public void setBluetoothLeIBeaconService(BluetoothLeIBeaconService bluetoothLeIBeaconService) {
        this.bluetoothLeIBeaconService = bluetoothLeIBeaconService;
    }

    public void unsetBluetoothLeIBeaconService(BluetoothLeIBeaconService bluetoothLeIBeaconService) {
        this.bluetoothLeIBeaconService = null;
    }

    public void setCloudService(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    public void unsetCloudService(CloudService cloudService) {
        this.cloudService = null;
    }

    protected void activate(ComponentContext context, Map<String, Object> properties) {
        logger.info("Activating Bluetooth iBeacon Scanner example...");

        try {
            this.cloudClient = this.cloudService.newCloudClient("IBeaconScannerExample");
        } catch (KuraException e) {
            logger.error("Unable to get CloudClient", e);
        }

        this.enableScanning = false;
        this.publishTimes = new HashMap<>();
        doUpdate(properties);
        logger.info("Activating Bluetooth iBeacon Scanner example...Done");
    }

    protected void deactivate(ComponentContext context) {
        logger.debug("Deactivating iBeacon Scanner Example...");

        releaseResources();

        if (this.handle != null) {
            this.handle.cancel(true);
        }

        if (this.worker != null) {
            this.worker.shutdown();
        }

        this.enableScanning = false;

        if (this.cloudClient != null) {
            this.cloudClient.release();
        }

        logger.debug("Deactivating iBeacon Scanner Example... Done.");
    }

    protected void updated(Map<String, Object> properties) {
        logger.debug("Updating iBeacon Scanner Example...");

        releaseResources();

        if (this.handle != null) {
            this.handle.cancel(true);
        }

        if (this.worker != null) {
            this.worker.shutdown();
        }

        doUpdate(properties);

        logger.debug("Updating iBeacon Scanner Example... Done");
    }

    private void doUpdate(Map<String, Object> properties) {
        for (Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals(PROPERTY_INAME)) {
                this.adapterName = (String) value;
            } else if (key.equals(PROPERTY_TOPIC_PREFIX)) {
                this.topicPrefix = (String) value;
            } else if (key.equals(PROPERTY_RATE_LIMIT)) {
                this.rateLimit = (Integer) value;
            } else if (key.equals(PROPERTY_ENABLE)) {
                this.enableScanning = (Boolean) value;
            } else if (key.equals(PROPERTY_TIMEOUT)) {
                this.timeout = (Integer) value;
            }
        }

        if (this.enableScanning) {
            this.worker = Executors.newSingleThreadExecutor();
            this.handle = this.worker.submit(() -> setup());
        }
    }

    private void setup() {
        this.bluetoothLeIBeaconFactory = new BluetoothLeIBeaconFactory();
        this.bluetoothLeAdapter = this.bluetoothLeService.getAdapter(this.adapterName);
        if (this.bluetoothLeAdapter != null) {
            this.bluetoothLeIBeaconScanner = this.bluetoothLeIBeaconService.getBeaconScanner(bluetoothLeAdapter,
                    this.bluetoothLeIBeaconFactory);
            this.bluetoothLeIBeaconScanner.addBeaconListener(this);
            try {
                this.bluetoothLeIBeaconScanner.startBeaconScan(this.timeout * 1000L);
            } catch (KuraException e) {
                logger.error("iBeacon scanning failed", e);
            }
        }
    }

    private void releaseResources() {
        if (this.bluetoothLeIBeaconScanner != null && this.bluetoothLeIBeaconScanner.isScanning()) {
            try {
                this.bluetoothLeIBeaconScanner.stopBeaconScan();
            } catch (KuraException e) {
                logger.error("iBeacon scan stop failed", e);
            }
        }
    }

    private double calculateDistance(int rssi, int txpower) {

        double distance;

        int ratioDB = txpower - rssi;
        double ratioLinear = Math.pow(10, (double) ratioDB / 10);
        distance = Math.sqrt(ratioLinear);

        // See http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing/20434019#20434019
        // double ratio = rssi*1.0/txpower;
        // if (ratio < 1.0) {
        // distance = Math.pow(ratio,10);
        // }
        // else {
        // distance = (0.89976)*Math.pow(ratio,7.7095) + 0.111;
        // }

        return distance;
    }

    @Override
    public void onBeaconsReceived(List<BluetoothLeIBeacon> beacons) {
        for (BluetoothLeIBeacon iBeacon : beacons) {
            logger.info("iBeacon received from {}", iBeacon.getAddress());
            logger.info("UUID : {}", iBeacon.getUuid());
            logger.info("Major : {}", iBeacon.getMajor());
            logger.info("Minor : {}", iBeacon.getMinor());
            logger.info("TxPower : {}", iBeacon.getTxPower());
            logger.info("RSSI : {}", iBeacon.getRssi());
            long now = System.currentTimeMillis();

            Long lastPublishTime = this.publishTimes.get(iBeacon.getAddress());

            // If this beacon is new, or it last published more than 'rateLimit' seconds ago
            if (lastPublishTime == null || (now - lastPublishTime) > this.rateLimit * 1000L) {

                // Store the publish time against the address
                this.publishTimes.put(iBeacon.getAddress(), now);

                // Publish the beacon data to the beacon's topic
                KuraPayload kp = new KuraPayload();
                kp.addMetric("uuid", iBeacon.getUuid().toString());
                kp.addMetric("txpower", iBeacon.getTxPower());
                kp.addMetric("rssi", iBeacon.getRssi());
                kp.addMetric("major", iBeacon.getMajor());
                kp.addMetric("minor", iBeacon.getMinor());
                kp.addMetric("distance", calculateDistance(iBeacon.getRssi(), iBeacon.getTxPower()));
                try {
                    this.cloudClient.publish(this.topicPrefix + "/" + iBeacon.getAddress(), kp, 2, false);
                } catch (KuraException e) {
                    logger.error("Unable to publish", e);
                }
            }
        }
    }
}
