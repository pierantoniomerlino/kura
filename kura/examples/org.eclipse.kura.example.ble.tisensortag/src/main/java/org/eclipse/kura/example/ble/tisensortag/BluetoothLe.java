/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.example.ble.tisensortag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeDevice;
import org.eclipse.kura.bluetooth.le.BluetoothLeGattCharacteristic;
import org.eclipse.kura.bluetooth.le.BluetoothLeGattService;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothLe implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLe.class);

    private static final String APP_ID = "BLE_APP_V2";
    private static final String PROPERTY_SCAN = "scan_enable";
    private static final String PROPERTY_SCANTIME = "scan_time";
    private static final String PROPERTY_PERIOD = "period";
    private static final String PROPERTY_TEMP = "enableTermometer";
    private static final String PROPERTY_ACC = "enableAccelerometer";
    private static final String PROPERTY_HUM = "enableHygrometer";
    private static final String PROPERTY_MAG = "enableMagnetometer";
    private static final String PROPERTY_PRES = "enableBarometer";
    private static final String PROPERTY_GYRO = "enableGyroscope";
    private static final String PROPERTY_OPTO = "enableLuxometer";
    private static final String PROPERTY_BUTTONS = "enableButtons";
    private static final String PROPERTY_REDLED = "switchOnRedLed";
    private static final String PROPERTY_GREENLED = "switchOnGreenLed";
    private static final String PROPERTY_BUZZER = "switchOnBuzzer";
    private static final String PROPERTY_TOPIC = "publishTopic";
    private static final String PROPERTY_INAME = "iname";

    private static final String INTERRUPTED_EX = "Interrupted Exception";
    private static final String DISCOVERY_STOP_EX = "Failed to stop discovery";

    private CloudService cloudService;
    private CloudClient cloudClient;
    private List<TiSensorTag> tiSensorTagList;
    private BluetoothLeService bluetoothLeService;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private ScheduledExecutorService worker;
    private ScheduledFuture<?> handle;

    private int period = 10;
    private int scantime = 5;
    private String topic = "data";
    private String iname = "hci0";
    private boolean enableScan = false;
    private boolean enableTemp = false;
    private boolean enableAcc = false;
    private boolean enableHum = false;
    private boolean enableMag = false;
    private boolean enablePres = false;
    private boolean enableGyro = false;
    private boolean enableOpto = false;
    private boolean enableButtons = false;
    private boolean enableRedLed = false;
    private boolean enableGreenLed = false;
    private boolean enableBuzzer = false;
    private Consumer<List<BluetoothLeDevice>> scanCallback;

    public void setCloudService(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    public void unsetCloudService(CloudService cloudService) {
        this.cloudService = null;
    }

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    public void unsetBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = null;
    }

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    protected void activate(ComponentContext context, Map<String, Object> properties) {
        logger.info("Activating BluetoothLe example...");

        try {
            this.cloudClient = this.cloudService.newCloudClient(APP_ID);
        } catch (KuraException e1) {
            logger.error("Error starting component", e1);
            throw new ComponentException(e1);
        }

        scanCallback = devices -> doScanCallback(devices);
        this.tiSensorTagList = new CopyOnWriteArrayList<>(new ArrayList<>());
        doUpdate(properties);
        logger.debug("Updating Bluetooth Service... Done.");
    }

    protected void deactivate(ComponentContext context) {

        logger.debug("Deactivating BluetoothLe...");
        if (this.bluetoothLeAdapter != null && this.bluetoothLeAdapter.isDiscovering()) {
            try {
                this.bluetoothLeAdapter.stopDiscovery();
            } catch (KuraException e) {
                logger.error(DISCOVERY_STOP_EX, e);
            }
        }

        // disconnect SensorTags
        for (TiSensorTag tiSensorTag : this.tiSensorTagList) {
            if (tiSensorTag != null && tiSensorTag.isConnected()) {
                if (this.enableButtons) {
                    tiSensorTag.disableKeysNotifications();
                }
                tiSensorTag.disconnect();
            }
        }
        this.tiSensorTagList.clear();

        // cancel a current worker handle if one if active
        if (this.handle != null) {
            this.handle.cancel(true);
        }

        // shutting down the worker and cleaning up the properties
        if (this.worker != null) {
            this.worker.shutdown();
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        // Releasing the CloudApplicationClient
        logger.info("Releasing CloudApplicationClient for {}...", APP_ID);
        if (cloudClient != null) {
            cloudClient.release();
        }

        logger.debug("Deactivating BluetoothLe... Done.");
    }

    protected void updated(Map<String, Object> properties) {

        logger.debug("Deactivating BluetoothLe...");
        if (this.bluetoothLeAdapter != null && this.bluetoothLeAdapter.isDiscovering()) {
            try {
                this.bluetoothLeAdapter.stopDiscovery();
            } catch (KuraException e) {
                logger.error(DISCOVERY_STOP_EX, e);
            }
        }

        // disconnect SensorTags
        for (TiSensorTag tiSensorTag : this.tiSensorTagList) {
            if (tiSensorTag != null && tiSensorTag.isConnected()) {
                if (this.enableButtons) {
                    tiSensorTag.disableKeysNotifications();
                }
                tiSensorTag.disconnect();
            }
        }
        this.tiSensorTagList.clear();

        // cancel a current worker handle if one is active
        if (this.handle != null) {
            this.handle.cancel(true);
        }

        // shutting down the worker and cleaning up the properties
        if (this.worker != null) {
            this.worker.shutdown();
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;
        doUpdate(properties);
        logger.debug("Updating Bluetooth Service... Done.");
    }

    private void doUpdate(Map<String, Object> properties) {

        readProperties(properties);
        if (this.enableScan) {
            // re-create the worker
            this.worker = Executors.newSingleThreadScheduledExecutor();

            // Get Bluetooth adapter and ensure it is enabled
            this.bluetoothLeAdapter = this.bluetoothLeService.getAdapter(this.iname);
            if (this.bluetoothLeAdapter != null) {
                logger.info("Bluetooth adapter interface => " + this.iname);
                if (!this.bluetoothLeAdapter.isPowered()) {
                    logger.info("Enabling bluetooth adapter...");
                    this.bluetoothLeAdapter.setPowered(true);
                }
                logger.info("Bluetooth adapter address => " + this.bluetoothLeAdapter.getAddress());

                this.handle = this.worker.scheduleAtFixedRate(() -> performScan(), 0, this.period, TimeUnit.SECONDS);
            } else {
                logger.info("Bluetooth adapter {} not found.", this.iname);
            }
        }
    }

    void performScan() {
        // Scan for devices
        if (this.bluetoothLeAdapter.isDiscovering()) {
            try {
                this.bluetoothLeAdapter.stopDiscovery();
            } catch (KuraException e) {
                logger.error(DISCOVERY_STOP_EX, e);
            }
        }
        this.bluetoothLeAdapter.findDevices(this.scantime, this.scanCallback);
    }

    // --------------------------------------------------------------------
    //
    // Private Methods
    //
    // --------------------------------------------------------------------

    protected void doPublishKeys(String address, Integer key) {
        KuraPayload payload = new KuraPayload();
        payload.setTimestamp(new Date());
        payload.addMetric("key", key);
        try {
            this.cloudClient.publish(topic + "/" + address + "/keys", payload, 0, false);
        } catch (Exception e) {
            logger.error("Can't publish message, " + "keys", e);
        }

    }

    private void doServicesDiscovery(TiSensorTag tiSensorTag) {
        logger.info("Starting services discovery...");
        for (Entry<String, BluetoothLeGattService> entry : tiSensorTag.discoverServices().entrySet()) {
            logger.info("Service {} {} ", entry.getKey(), entry.getValue().getUUID());
        }
    }

    private void doCharacteristicsDiscovery(TiSensorTag tiSensorTag) {
        for (BluetoothLeGattCharacteristic bgc : tiSensorTag.getCharacteristics()) {
            logger.info("Characteristics uuid : {}", bgc.getUUID());
        }
    }

    private boolean searchSensorTagList(String address) {
        boolean found = false;
        for (TiSensorTag tiSensorTag : this.tiSensorTagList) {
            if (tiSensorTag.getBluetoothLeDevice().getAddress().equals(address)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void readProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get(PROPERTY_SCAN) != null) {
                this.enableScan = (Boolean) properties.get(PROPERTY_SCAN);
            }
            if (properties.get(PROPERTY_SCANTIME) != null) {
                this.scantime = (Integer) properties.get(PROPERTY_SCANTIME);
            }
            if (properties.get(PROPERTY_PERIOD) != null) {
                this.period = (Integer) properties.get(PROPERTY_PERIOD);
            }
            if (properties.get(PROPERTY_TEMP) != null) {
                this.enableTemp = (Boolean) properties.get(PROPERTY_TEMP);
            }
            if (properties.get(PROPERTY_ACC) != null) {
                this.enableAcc = (Boolean) properties.get(PROPERTY_ACC);
            }
            if (properties.get(PROPERTY_HUM) != null) {
                this.enableHum = (Boolean) properties.get(PROPERTY_HUM);
            }
            if (properties.get(PROPERTY_MAG) != null) {
                this.enableMag = (Boolean) properties.get(PROPERTY_MAG);
            }
            if (properties.get(PROPERTY_PRES) != null) {
                this.enablePres = (Boolean) properties.get(PROPERTY_PRES);
            }
            if (properties.get(PROPERTY_GYRO) != null) {
                this.enableGyro = (Boolean) properties.get(PROPERTY_GYRO);
            }
            if (properties.get(PROPERTY_OPTO) != null) {
                this.enableOpto = (Boolean) properties.get(PROPERTY_OPTO);
            }
            if (properties.get(PROPERTY_BUTTONS) != null) {
                this.enableButtons = (Boolean) properties.get(PROPERTY_BUTTONS);
            }
            if (properties.get(PROPERTY_REDLED) != null) {
                this.enableRedLed = (Boolean) properties.get(PROPERTY_REDLED);
            }
            if (properties.get(PROPERTY_GREENLED) != null) {
                this.enableGreenLed = (Boolean) properties.get(PROPERTY_GREENLED);
            }
            if (properties.get(PROPERTY_BUZZER) != null) {
                this.enableBuzzer = (Boolean) properties.get(PROPERTY_BUZZER);
            }
            if (properties.get(PROPERTY_TOPIC) != null) {
                this.topic = (String) properties.get(PROPERTY_TOPIC);
            }
            if (properties.get(PROPERTY_INAME) != null) {
                this.iname = (String) properties.get(PROPERTY_INAME);
            }
        }
    }

    private void doScanCallback(List<BluetoothLeDevice> devices) {
        // Scan for TI SensorTag
        for (BluetoothLeDevice bluetoothLeDevice : devices) {
            logger.info("Address " + bluetoothLeDevice.getAddress() + " Name " + bluetoothLeDevice.getName());

            if (bluetoothLeDevice.getName().contains("SensorTag")
                    && !searchSensorTagList(bluetoothLeDevice.getAddress())) {
                this.tiSensorTagList.add(new TiSensorTag(bluetoothLeDevice));
            }
        }

        logger.debug("Found " + this.tiSensorTagList.size() + " SensorTags");

        // connect to TiSensorTags
        for (TiSensorTag myTiSensorTag : this.tiSensorTagList) {
            if (!myTiSensorTag.isConnected()) {
                logger.info("Connecting to TiSensorTag {}...", myTiSensorTag.getBluetoothLeDevice().getAddress());
                myTiSensorTag.connect();
            }

            if (myTiSensorTag.isConnected()) {
                KuraPayload payload = new KuraPayload();
                payload.setTimestamp(new Date());
                if (myTiSensorTag.isCC2650()) {
                    payload.addMetric("Type", "CC2650");
                } else {
                    payload.addMetric("Type", "CC2541");
                }

                doServicesDiscovery(myTiSensorTag);
                doCharacteristicsDiscovery(myTiSensorTag);

                payload.addMetric("Firmware", myTiSensorTag.getFirmareRevision());

                if (this.enableTemp) {
                    myTiSensorTag.enableTermometer();
                    waitFor(1000);
                    double[] temperatures = myTiSensorTag.readTemperature();

                    logger.info("Ambient: " + temperatures[0] + " Target: " + temperatures[1]);

                    payload.addMetric("Ambient", temperatures[0]);
                    payload.addMetric("Target", temperatures[1]);
                }

                if (this.enableAcc) {
                    // Reduce period to 500ms (for a bug on SensorTag firmware :-)) and enable accelerometer with
                    // range 8g
                    myTiSensorTag.setAccelerometerPeriod(50);
                    if (myTiSensorTag.isCC2650()) {
                        byte[] config = { 0x38, 0x02 };
                        myTiSensorTag.enableAccelerometer(config);
                    } else {
                        byte[] config = { 0x01 };
                        myTiSensorTag.enableAccelerometer(config);
                    }
                    waitFor(1000);
                    double[] acceleration = myTiSensorTag.readAcceleration();

                    logger.info(
                            "Acc X: " + acceleration[0] + " Acc Y: " + acceleration[1] + " Acc Z: " + acceleration[2]);

                    payload.addMetric("Acceleration X", acceleration[0]);
                    payload.addMetric("Acceleration Y", acceleration[1]);
                    payload.addMetric("Acceleration Z", acceleration[2]);
                }

                if (this.enableHum) {
                    myTiSensorTag.enableHygrometer();
                    waitFor(1000);

                    float humidity = myTiSensorTag.readHumidity();
                    logger.info("Humidity: " + humidity);

                    payload.addMetric("Humidity", humidity);
                }

                if (this.enableMag) {
                    // Reduce period to 500ms (for a bug on SensorTag firmware :-)) and enable magnetometer
                    myTiSensorTag.setMagnetometerPeriod(50);
                    if (myTiSensorTag.isCC2650()) {
                        byte[] config = { 0x40, 0x00 };
                        myTiSensorTag.enableMagnetometer(config);
                    } else {
                        byte[] config = { 0x01 };
                        myTiSensorTag.enableMagnetometer(config);
                    }
                    waitFor(1000);
                    float[] magneticField = myTiSensorTag.readMagneticField();

                    logger.info("Mag X: " + magneticField[0] + " Mag Y: " + magneticField[1] + " Mag Z: "
                            + magneticField[2]);

                    payload.addMetric("Magnetic X", magneticField[0]);
                    payload.addMetric("Magnetic Y", magneticField[1]);
                    payload.addMetric("Magnetic Z", magneticField[2]);

                }

                if (this.enablePres) {
                    // Calibrate pressure sensor
                    myTiSensorTag.calibrateBarometer();
                    waitFor(1000);

                    // Read pressure
                    myTiSensorTag.enableBarometer();
                    waitFor(1000);
                    double pressure = myTiSensorTag.readPressure();

                    logger.info("Pre : " + pressure);

                    payload.addMetric("Pressure", pressure);
                }

                if (this.enableGyro) {
                    if (myTiSensorTag.isCC2650()) {
                        // Reduce period to 500ms (for a bug on SensorTag firmware :-)) and enable gyroscope
                        myTiSensorTag.setGyroscopePeriod(50);
                        byte[] config = { 0x07, 0x00 };
                        myTiSensorTag.enableGyroscope(config);
                    } else {
                        byte[] config = { 0x07 };
                        myTiSensorTag.enableGyroscope(config);
                    }
                    waitFor(1000);
                    float[] gyroscope = myTiSensorTag.readGyroscope();

                    logger.info("Gyro X: " + gyroscope[0] + " Gyro Y: " + gyroscope[1] + " Gyro Z: " + gyroscope[2]);

                    payload.addMetric("Gyro X", gyroscope[0]);
                    payload.addMetric("Gyro Y", gyroscope[1]);
                    payload.addMetric("Gyro Z", gyroscope[2]);

                }

                if (this.enableOpto) {
                    myTiSensorTag.enableLuxometer();
                    waitFor(1000);

                    double light = myTiSensorTag.readLight();
                    logger.info("Light: " + light);

                    payload.addMetric("Light", light);
                }

                if (this.enableButtons && !myTiSensorTag.isKeysNotificationEnabled()) {
                    // For buttons only enable notifications
                    myTiSensorTag.enableKeysNotification(keys -> {
                        logger.info("Received key {}", keys);
                        doPublishKeys(myTiSensorTag.getBluetoothLeDevice().getAddress(), keys);
                    });
                }

                if (this.enableRedLed) {
                    myTiSensorTag.switchOnRedLed();
                } else {
                    myTiSensorTag.switchOffRedLed();
                }

                if (this.enableGreenLed) {
                    myTiSensorTag.switchOnGreenLed();
                } else {
                    myTiSensorTag.switchOffGreenLed();
                }

                if (this.enableBuzzer) {
                    myTiSensorTag.switchOnBuzzer();
                } else {
                    myTiSensorTag.switchOffBuzzer();
                }

                myTiSensorTag.enableIOService();

                // Publish only if there are metrics to be published!
                if (!payload.metricNames().isEmpty()) {
                    try {
                        cloudClient.publish(topic + "/" + myTiSensorTag.getBluetoothLeDevice().getAddress(), payload, 0,
                                false);
                    } catch (KuraException e) {
                        logger.error("Publish message failed", e);
                    }
                }

            } else {
                logger.info(
                        "Cannot connect to TI SensorTag " + myTiSensorTag.getBluetoothLeDevice().getAddress() + ".");
            }

        }
    }

    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EX, e);
        }
    }
}
