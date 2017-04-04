/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.BluetoothDevice;
import org.eclipse.kura.bluetooth.BluetoothGatt;
import org.eclipse.kura.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.kura.bluetooth.BluetoothGattSecurityLevel;
import org.eclipse.kura.bluetooth.BluetoothGattService;
import org.eclipse.kura.bluetooth.BluetoothLeNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// More documentation can be found in http://processors.wiki.ti.com/index.php/SensorTag_User_Guide for the CC2541
// and http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide for the CC2650

public class TiSensorTag implements BluetoothLeNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(TiSensorTag.class);

    private static final String TEMPERATURE = "temperature";
    private static final String HUMIDITY = "humidity";
    private static final String PRESSURE = "pressure";
    private static final String MOVEMENT = "movement";
    private static final String ACCELEROMETER = "accelerometer";
    private static final String MAGNETOMETER = "magnetometer";
    private static final String GYROSCOPE = "gyroscope";
    private static final String OPTO = "opto";
    private static final String KEYS = "keys";
    private static final String IO = "io";

    private BluetoothGatt bluetoothGattClient;
    private BluetoothDevice device;
    private boolean connected;
    private String pressureCalibration;
    private boolean isCC2650;
    private String firmwareRevision;
    private Map<String, BluetoothGattService> services;

    public TiSensorTag(BluetoothDevice bluetoothDevice) {
        this.device = bluetoothDevice;
        this.connected = false;
        this.services = new HashMap<String, BluetoothGattService>();
        if (this.device.getName().contains("CC2650 SensorTag")) {
            this.isCC2650 = true;
        } else {
            this.isCC2650 = false;
        }

    }

    public BluetoothDevice getBluetoothDevice() {
        return this.device;
    }

    public void setBluetoothDevice(BluetoothDevice device) {
        this.device = device;
    }

    public boolean isConnected() {
        this.connected = checkConnection();
        return this.connected;
    }

    public boolean connect() {
        this.bluetoothGattClient = this.device.getBluetoothGattClient();
        try {
            this.connected = this.bluetoothGattClient.connect();
        } catch (KuraException e) {
            logger.error("Failed to connect to the device", e);
        }
        getServices();
        return this.connected;
    }

    public void disconnect() {
        if (this.bluetoothGattClient != null) {
            this.bluetoothGattClient.disconnect();
            this.connected = false;
        }
    }

    public boolean checkConnection() {
        if (this.bluetoothGattClient != null) {
            try {
                this.connected = this.bluetoothGattClient.checkConnection();
            } catch (KuraException e) {
                logger.error("Failed to connect to the device", e);
            }
        } else {
            this.connected = false;
        }
        return this.connected;
    }

    public void setSecurityLevel(BluetoothGattSecurityLevel level) {
        if (this.bluetoothGatt != null) {
            this.bluetoothGatt.setSecurityLevel(level);
        }
    }

    public BluetoothGattSecurityLevel getSecurityLevel() {
        BluetoothGattSecurityLevel level = BluetoothGattSecurityLevel.UNKNOWN;
        try {
            if (this.bluetoothGatt != null) {
                level = this.bluetoothGatt.getSecurityLevel();
            }
        } catch (KuraException e) {
            logger.error("Get security level failed", e);
        }

        return level;
    }

    public boolean getCC2650() {
        return this.isCC2650;
    }

    public void setCC2650(boolean cc2650) {
        this.isCC2650 = cc2650;
    }

    public String getFirmareRevision() {
        return this.firmwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    /*
     * Discover services
     */
    public List<BluetoothGattService> discoverServices() {
        return this.bluetoothGattClient.getServices();
    }

    public List<BluetoothGattCharacteristic> getCharacteristics(String startHandle, String endHandle) {
        logger.info("List<BluetoothGattCharacteristic> getCharacteristics");
        return this.bluetoothGattClient.getCharacteristics(startHandle, endHandle);
    }

    public String firmwareRevision() {
        String firmwareVersion = "";
        try {
            if (this.isCC2650) {
                firmwareVersion = hexAsciiToString(this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_FIRMWARE_REVISION_2650));
            } else {
                String firmware = this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_FIRMWARE_REVISION_2541);
                firmwareVersion = hexAsciiToString(firmware.substring(0, firmware.length() - 3));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return firmwareVersion;
    }

    // ----------------------------------------------------------------------------------------------------------
    //
    // Temperature Sensor, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // ----------------------------------------------------------------------------------------------------------
    /*
     * Enable temperature sensor
     */
    public void enableTermometer() {
        byte[] value = { 0x01 };
        this.services.get(TEMPERATURE).getCharacteristic(TiSensorTagGatt.UUID_TEMP_SENSOR_ENABLE).writeValue(value);
    }

    /*
     * Disable temperature sensor
     */
    public void disableTermometer() {
        byte[] value = { 0x00 };
        this.services.get(TEMPERATURE).getCharacteristic(TiSensorTagGatt.UUID_TEMP_SENSOR_ENABLE).writeValue(value);
    }

    /*
     * Read temperature sensor
     */
    public double[] readTemperature() {
        double[] temperatures = new double[2];
        // Read value
        try {
            if (this.isCC2650) {
                temperatures = calculateTemperature(this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_TEMP_SENSOR_VALUE_2650));
            } else {
                temperatures = calculateTemperature(this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_TEMP_SENSOR_VALUE_2541));
            }
        } catch (KuraException e) {
            logger.error("Failed to read temperature", e);
        }
        return temperatures;
    }

    /*
     * Read temperature sensor by UUID
     */
    public double[] readTemperatureByUuid() {
        byte[] temperatures = this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_TEMP_SENSOR_SERVICE)
                .getCharacteristic(TiSensorTagGatt.UUID_TEMP_SENSOR_VALUE).readValue();
        return calculateTemperature(toHexString(temperatures));
    }

    /*
     * Enable temperature notifications
     */
    public void enableTemperatureNotifications(BluetoothLeNotificationListener listener) {
        this.services.get(TEMPERATURE).getCharacteristic(TiSensorTagGatt.UUID_TEMP_SENSOR_VALUE)
                .setBluetoothLeNotificationListener(listener);
    }

    /*
     * Disable temperature notifications
     */
    public void disableTemperatureNotifications() {
        this.services.get(TEMPERATURE).getCharacteristic(TiSensorTagGatt.UUID_TEMP_SENSOR_VALUE)
                .unsetBluetoothLeNotificationListener();
    }

    /*
     * Set sampling period (only for CC2650)
     */
    public void setTermometerPeriod(int period) {
        byte[] periodBytes = ByteBuffer.allocate(4).putInt(period).array();
        this.services.get(TiSensorTagGatt.UUID_TEMP_SENSOR_SERVICE)
                .getCharacteristic(TiSensorTagGatt.UUID_ACC_SENSOR_PERIOD).writeValue(periodBytes);
    }

    /*
     * Calculate temperature
     */
    private double[] calculateTemperature(String value) {

        logger.info("Received temperature value: " + value);

        double[] temperatures = new double[2];

        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));

        if (this.isCC2650) {
            int ambT = shortUnsignedAtOffset(valueByte, 2);
            int objT = shortUnsignedAtOffset(valueByte, 0);
            temperatures[0] = (ambT >> 2) * 0.03125;
            temperatures[1] = (objT >> 2) * 0.03125;
        } else {

            int ambT = shortUnsignedAtOffset(valueByte, 2);
            int objT = shortSignedAtOffset(valueByte, 0);
            temperatures[0] = ambT / 128.0;

            double Vobj2 = objT;
            Vobj2 *= 0.00000015625;

            double Tdie = ambT / 128.0 + 273.15;

            double S0 = 5.593E-14;	// Calibration factor
            double a1 = 1.75E-3;
            double a2 = -1.678E-5;
            double b0 = -2.94E-5;
            double b1 = -5.7E-7;
            double b2 = 4.63E-9;
            double c2 = 13.4;
            double Tref = 298.15;
            double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * Math.pow(Tdie - Tref, 2));
            double Vos = b0 + b1 * (Tdie - Tref) + b2 * Math.pow(Tdie - Tref, 2);
            double fObj = Vobj2 - Vos + c2 * Math.pow(Vobj2 - Vos, 2);
            double tObj = Math.pow(Math.pow(Tdie, 4) + fObj / S, .25);

            temperatures[1] = tObj - 273.15;
        }

        return temperatures;
    }

    // ------------------------------------------------------------------------------------------------------------
    //
    // Accelerometer Sensor, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // ------------------------------------------------------------------------------------------------------------
    /*
     * Enable accelerometer sensor
     */
    public void enableAccelerometer(String config) {

        if (this.isCC2650) {
            // 0: gyro X, 1: gyro Y, 2: gyro Z
            // 3: acc X, 4: acc Y, 5: acc Z
            // 6: mag
            // 7: wake-on-motion
            // 8-9: acc range (0 : 2g, 1 : 4g, 2 : 8g, 3 : 16g)
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_ENABLE_2650, config);
        } else {
            // Write "01" in order to enable the sensor in 2g range
            // Write "01" in order to select 2g range, "02" for 4g, "03" for 8g (only for firmware > 1.5)
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_ACC_SENSOR_ENABLE_2541, config);
        }
    }

    /*
     * Disable accelerometer sensor
     */
    public void disableAccelerometer() {
        if (this.isCC2650) {
            // Write "0000" to disable accelerometer sensor
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_ENABLE_2650, "0000");
        } else {
            // Write "00" to disable accelerometer sensor
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_ACC_SENSOR_ENABLE_2541, "00");
        }
    }

    /*
     * Read accelerometer sensor
     */
    public double[] readAcceleration() {
        double[] acceleration = new double[3];
        // Read value
        try {
            if (this.isCC2650) {
                acceleration = calculateAcceleration(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_VALUE_2650));
            } else {
                acceleration = calculateAcceleration(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_ACC_SENSOR_VALUE_2541));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return acceleration;
    }

    /*
     * Read accelerometer sensor by UUID
     */
    public double[] readAccelerationByUuid() {
        double[] acceleration = new double[3];
        try {
            if (this.isCC2650) {
                return calculateAcceleration(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_MOV_SENSOR_VALUE));
            } else {
                return calculateAcceleration(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_ACC_SENSOR_VALUE));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return acceleration;
    }

    /*
     * Enable accelerometer notifications
     */
    public void enableAccelerationNotifications() {
        // Write "01:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_NOTIFICATION_2650,
                    "01:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_ACC_SENSOR_NOTIFICATION_2541,
                    "01:00");
        }
    }

    /*
     * Disable accelerometer notifications
     */
    public void disableAccelerationNotifications() {
        // Write "00:00 to disable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_NOTIFICATION_2650,
                    "00:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_ACC_SENSOR_NOTIFICATION_2541,
                    "00:00");
        }
    }

    /*
     * Set sampling period
     */
    public void setAccelerometerPeriod(String period) {
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_PERIOD_2650, period);
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_ACC_SENSOR_PERIOD_2541, period);
        }
    }

    /*
     * Calculate acceleration
     */
    private double[] calculateAcceleration(String value) {

        logger.info("Received accelerometer value: " + value);

        double[] acceleration = new double[3];
        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));

        if (this.isCC2650) {
            final float SCALE = (float) 4096.0;

            int x = shortSignedAtOffset(valueByte, 6);
            int y = shortSignedAtOffset(valueByte, 8);
            int z = shortSignedAtOffset(valueByte, 10);

            acceleration[0] = x / SCALE * -1;
            acceleration[1] = y / SCALE;
            acceleration[2] = z / SCALE * -1;
        } else {
            String[] tmp = value.split("\\s");
            int x = unsignedToSigned(Integer.parseInt(tmp[0], 16), 8);
            int y = unsignedToSigned(Integer.parseInt(tmp[1], 16), 8);
            int z = unsignedToSigned(Integer.parseInt(tmp[2], 16), 8) * -1;

            acceleration[0] = x / 64.0;
            acceleration[1] = y / 64.0;
            acceleration[2] = z / 64.0;
        }

        return acceleration;
    }

    // -------------------------------------------------------------------------------------------------------
    //
    // Humidity Sensor, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // -------------------------------------------------------------------------------------------------------
    /*
     * Enable humidity sensor
     */
    public void enableHygrometer() {
        // Write "01" to enable humidity sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_ENABLE_2650, "01");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_ENABLE_2541, "01");
        }
    }

    /*
     * Disable humidity sensor
     */
    public void disableHygrometer() {
        // Write "00" to disable humidity sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_ENABLE_2650, "00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_ENABLE_2541, "00");
        }
    }

    /*
     * Read humidity sensor
     */
    public float readHumidity() {
        float humidity = 0F;
        // Read value
        try {
            if (this.isCC2650) {
                humidity = calculateHumidity(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_VALUE_2650));
            } else {
                humidity = calculateHumidity(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_VALUE_2541));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return humidity;
    }

    /*
     * Read humidity sensor by UUID
     */
    public float readHumidityByUuid() {
        float humidity = 0F;
        try {
            humidity = calculateHumidity(
                    this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_HUM_SENSOR_VALUE));
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return humidity;
    }

    /*
     * Enable humidity notifications
     */
    public void enableHumidityNotifications() {
        // Write "01:00 to 0x39 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_NOTIFICATION_2650,
                    "01:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_NOTIFICATION_2541,
                    "01:00");
        }
    }

    /*
     * Disable humidity notifications
     */
    public void disableHumidityNotifications() {
        // Write "00:00 to 0x39 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_NOTIFICATION_2650,
                    "00:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_NOTIFICATION_2541,
                    "00:00");
        }
    }

    /*
     * Set sampling period (for CC2650 only)
     */
    public void setHygrometerPeriod(String period) {
        this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_HUM_SENSOR_PERIOD_2650, period);
    }

    /*
     * Calculate Humidity
     */
    private float calculateHumidity(String value) {

        logger.info("Received barometer value: " + value);

        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));

        int hum = shortUnsignedAtOffset(valueByte, 2);

        float humf = 0f;

        if (this.isCC2650) {
            humf = hum / 65536f * 100f;
        } else {
            hum = hum - hum % 4;
            humf = -6f + 125f * (hum / 65535f);
        }
        return humf;
    }

    // -----------------------------------------------------------------------------------------------------------
    //
    // Magnetometer Sensor, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // -----------------------------------------------------------------------------------------------------------
    /*
     * Enable magnetometer sensor
     */
    public void enableMagnetometer(String config) {
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_ENABLE_2650, config);
        } else {
            // Write "01" enable magnetometer sensor
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MAG_SENSOR_ENABLE_2541, "01");
        }
    }

    /*
     * Disable magnetometer sensor
     */
    public void disableMagnetometer() {
        if (this.isCC2650) {
            // Write "0000" to disable magnetometer sensor
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_ENABLE_2650, "0000");
        } else {
            // Write "00" to disable magnetometer sensor
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MAG_SENSOR_ENABLE_2541, "00");
        }
    }

    /*
     * Read magnetometer sensor
     */
    public float[] readMagneticField() {
        float[] magnetic = new float[3];
        // Read value
        try {
            if (this.isCC2650) {
                magnetic = calculateMagneticField(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_VALUE_2650));
            } else {
                magnetic = calculateMagneticField(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_MAG_SENSOR_VALUE_2541));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return magnetic;
    }

    /*
     * Read magnetometer sensor by UUID
     */
    public float[] readMagneticFieldByUuid() {
        float[] magnetic = new float[3];
        try {
            if (this.isCC2650) {
                magnetic = calculateMagneticField(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_MOV_SENSOR_VALUE));
            } else {
                magnetic = calculateMagneticField(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_MAG_SENSOR_VALUE));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return magnetic;
    }

    /*
     * Enable magnetometer notifications
     */
    public void enableMagneticFieldNotifications() {
        // Write "01:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_NOTIFICATION_2650,
                    "01:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MAG_SENSOR_NOTIFICATION_2541,
                    "01:00");
        }
    }

    /*
     * Disable magnetometer notifications
     */
    public void disableMagneticFieldNotifications() {
        // Write "00:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_NOTIFICATION_2650,
                    "00:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MAG_SENSOR_NOTIFICATION_2541,
                    "00:00");
        }
    }

    /*
     * Set sampling period
     */
    public void setMagnetometerPeriod(String period) {
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_PERIOD_2650, period);
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MAG_SENSOR_PERIOD_2541, period);
        }
    }

    /*
     * Calculate Magnetic Field
     */
    private float[] calculateMagneticField(String value) {

        logger.info("Received magnetometer value: " + value);

        float[] magneticField = new float[3];

        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));

        if (this.isCC2650) {

            final float SCALE = 32768 / 4912;

            int x = shortSignedAtOffset(valueByte, 12);
            int y = shortSignedAtOffset(valueByte, 14);
            int z = shortSignedAtOffset(valueByte, 16);

            magneticField[0] = x / SCALE;
            magneticField[1] = y / SCALE;
            magneticField[2] = z / SCALE;
        } else {

            int x = shortSignedAtOffset(valueByte, 0);
            int y = shortSignedAtOffset(valueByte, 2);
            int z = shortSignedAtOffset(valueByte, 4);

            magneticField[0] = x * (2000f / 65536f) * -1;
            magneticField[1] = y * (2000f / 65536f) * -1;
            magneticField[2] = z * (2000f / 65536f);
        }

        return magneticField;
    }

    // ------------------------------------------------------------------------------------------------------------------
    //
    // Barometric Pressure Sensor, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // ------------------------------------------------------------------------------------------------------------------
    /*
     * Enable pressure sensor
     */
    public void enableBarometer() {
        // Write "01" enable pressure sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2650, "01");
        } else {
            if (this.firmwareRevision.contains("1.4")) {
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2541_1_4,
                        "01");
            } else {
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2541_1_5,
                        "01");
            }
        }
    }

    /*
     * Disable pressure sensor
     */
    public void disableBarometer() {
        // Write "00" to disable pressure sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2650, "00");
        } else {
            if (this.firmwareRevision.contains("1.4")) {
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2541_1_4,
                        "00");
            } else {
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2541_1_5,
                        "00");
            }
        }
    }

    /*
     * Calibrate pressure sensor
     */
    public void calibrateBarometer() {
        // Write "02" to calibrate pressure sensor
        if (!this.isCC2650) {
            if (this.firmwareRevision.contains("1.4")) {
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2541_1_4,
                        "02");
            } else {
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_ENABLE_2541_1_5,
                        "02");
            }
        }
    }

    /*
     * Read calibration pressure sensor
     */
    public String readCalibrationBarometer() {
        this.pressureCalibration = "";
        // Read value
        try {
            if (!this.isCC2650) {
                if (this.firmwareRevision.contains("1.4")) {
                    this.pressureCalibration = this.bluetoothGattClient
                            .readCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_CALIBRATION_2541_1_4);
                } else {
                    this.pressureCalibration = this.bluetoothGattClient
                            .readCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_CALIBRATION_2541_1_5);
                }
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return this.pressureCalibration;
    }

    /*
     * Read pressure sensor
     */
    public double readPressure() {
        double pressure = 0.0;
        // Read value
        try {
            if (this.isCC2650) {
                pressure = calculatePressure(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_VALUE_2650));
            } else if (this.firmwareRevision.contains("1.4")) {
                pressure = calculatePressure(this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_VALUE_2541_1_4));
            } else {
                pressure = calculatePressure(this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_VALUE_2541_1_5));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return pressure;
    }

    /*
     * Read pressure sensor by UUID
     */
    public double readPressureByUuid() {
        double pressure = 0.0;
        try {
            pressure = calculatePressure(
                    this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_PRE_SENSOR_VALUE));
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return pressure;
    }

    /*
     * Enable pressure notifications
     */
    public void enablePressureNotifications() {
        // Write "01:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_NOTIFICATION_2650,
                    "01:00");
        } else if (this.firmwareRevision.contains("1.4")) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_NOTIFICATION_2541_1_4,
                    "01:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_NOTIFICATION_2541_1_5,
                    "01:00");
        }
    }

    /*
     * Disable pressure notifications
     */
    public void disablePressureNotifications() {
        // Write "00:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_NOTIFICATION_2650,
                    "00:00");
        } else if (this.firmwareRevision.contains("1.4")) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_NOTIFICATION_2541_1_4,
                    "00:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_NOTIFICATION_2541_1_5,
                    "00:00");
        }
    }

    /*
     * Set sampling period (only for CC2650)
     */
    public void setBarometerPeriod(String period) {
        this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_PRE_SENSOR_PERIOD_2650, period);
    }

    /*
     * Calculate pressure
     */
    private double calculatePressure(String value) {

        logger.info("Received pressure value: " + value);

        double p_a = 0.0;
        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));

        if (this.isCC2650) {

            if (valueByte.length > 4) {
                Integer val = twentyFourBitUnsignedAtOffset(valueByte, 3);
                p_a = val / 100.0;
            } else {
                int mantissa;
                int exponent;
                Integer pre = shortUnsignedAtOffset(valueByte, 2);

                mantissa = pre & 0x0FFF;
                exponent = pre >> 12 & 0xFF;

                double output;
                double magnitude = Math.pow(2.0, exponent);
                output = mantissa * magnitude;
                p_a = output / 100.0;
            }

        } else {

            int t_r = shortSignedAtOffset(valueByte, 0);
            int p_r = shortUnsignedAtOffset(valueByte, 2);

            byte[] pressureCalibrationByte = hexStringToByteArray(this.pressureCalibration.replace(" ", ""));
            int c[] = new int[8];
            c[0] = shortUnsignedAtOffset(pressureCalibrationByte, 0);
            c[1] = shortUnsignedAtOffset(pressureCalibrationByte, 2);
            c[2] = shortUnsignedAtOffset(pressureCalibrationByte, 4);
            c[3] = shortUnsignedAtOffset(pressureCalibrationByte, 6);
            c[4] = shortSignedAtOffset(pressureCalibrationByte, 8);
            c[5] = shortSignedAtOffset(pressureCalibrationByte, 10);
            c[6] = shortSignedAtOffset(pressureCalibrationByte, 12);
            c[7] = shortSignedAtOffset(pressureCalibrationByte, 14);

            // Ignore temperature from pressure sensor
            // double t_a = (100 * (c[0] * t_r / Math.pow(2,8) + c[1] * Math.pow(2,6))) / Math.pow(2,16);
            double S = c[2] + c[3] * t_r / Math.pow(2, 17) + c[4] * t_r / Math.pow(2, 15) * t_r / Math.pow(2, 19);
            double O = c[5] * Math.pow(2, 14) + c[6] * t_r / Math.pow(2, 3)
                    + c[7] * t_r / Math.pow(2, 15) * t_r / Math.pow(2, 4);
            p_a = (S * p_r + O) / Math.pow(2, 14) / 100.0;

        }

        return p_a;
    }

    // --------------------------------------------------------------------------------------------------------
    //
    // Gyroscope Sensor, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // --------------------------------------------------------------------------------------------------------
    /*
     * Enable gyroscope sensor
     */
    public void enableGyroscope(String enable) {
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_ENABLE_2650, enable);
        } else {
            // Write "00" to turn off gyroscope, "01" to enable X axis only, "02" to enable Y axis only,
            // "03" = X and Y, "04" = Z only, "05" = X and Z, "06" = Y and Z and "07" = X, Y and Z.
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_GYR_SENSOR_ENABLE_2541, enable);
        }
    }

    /*
     * Disable gyroscope sensor
     */
    public void disableGyroscope() {
        // Write "00" to disable gyroscope sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_ENABLE_2650, "0000");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_GYR_SENSOR_ENABLE_2541, "00");
        }
    }

    /*
     * Read gyroscope sensor
     */
    public float[] readGyroscope() {
        float[] gyroscope = new float[3];
        // Read value
        try {
            if (this.isCC2650) {
                gyroscope = calculateGyroscope(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_VALUE_2650));
            } else {
                gyroscope = calculateGyroscope(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_GYR_SENSOR_VALUE_2541));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return gyroscope;
    }

    /*
     * Read gyroscope sensor by UUID
     */
    public float[] readGyroscopeByUuid() {
        float[] gyroscope = new float[3];
        try {
            if (this.isCC2650) {
                gyroscope = calculateGyroscope(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_MOV_SENSOR_VALUE));
            } else {
                gyroscope = calculateGyroscope(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_GYR_SENSOR_VALUE));
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return gyroscope;
    }

    /*
     * Enable gyroscope notifications
     */
    public void enableGyroscopeNotifications() {
        // Write "01:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_NOTIFICATION_2650,
                    "01:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_GYR_SENSOR_NOTIFICATION_2541,
                    "01:00");
        }
    }

    /*
     * Disable gyroscope notifications
     */
    public void disableGyroscopeNotifications() {
        // Write "00:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_NOTIFICATION_2650,
                    "00:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_GYR_SENSOR_NOTIFICATION_2541,
                    "00:00");
        }
    }

    /*
     * Set sampling period (only for CC2650)
     */
    public void setGyroscopePeriod(String period) {
        this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_MOV_SENSOR_PERIOD_2650, period);
    }

    /*
     * Calculate gyroscope
     */
    private float[] calculateGyroscope(String value) {

        logger.info("Received gyro value: " + value);

        float[] gyroscope = new float[3];
        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));

        int y = shortSignedAtOffset(valueByte, 0);
        int x = shortSignedAtOffset(valueByte, 2);
        int z = shortSignedAtOffset(valueByte, 4);

        if (this.isCC2650) {

            final float SCALE = 65535 / 500;

            gyroscope[0] = x / SCALE;
            gyroscope[1] = y / SCALE;
            gyroscope[2] = z / SCALE;
        } else {
            gyroscope[0] = x * (500f / 65536f);
            gyroscope[1] = y * (500f / 65536f) * -1;
            gyroscope[2] = z * (500f / 65536f);
        }

        return gyroscope;
    }

    // -------------------------------------------------------------------------------------------------------
    //
    // Optical Sensor
    //
    // -------------------------------------------------------------------------------------------------------
    /*
     * Enable optical sensor
     */
    public void enableLuxometer() {
        // Write "01" to enable light sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_OPTO_SENSOR_ENABLE_2650, "01");
        } else {
            logger.info("Not optical sensor on CC2541.");
        }

    }

    /*
     * Disable optical sensor
     */
    public void disableLuxometer() {
        // Write "00" to disable light sensor
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_OPTO_SENSOR_ENABLE_2650, "00");
        } else {
            logger.info("Not optical sensor on CC2541.");
        }
    }

    /*
     * Read optical sensor
     */
    public double readLight() {
        double light = 0.0;
        // Read value
        try {
            if (this.isCC2650) {
                light = calculateLight(this.bluetoothGattClient
                        .readCharacteristicValue(TiSensorTagGatt.HANDLE_OPTO_SENSOR_VALUE_2650));
            } else {
                logger.info("Not optical sensor on CC2541.");
                light = 0.0;
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return light;
    }

    /*
     * Read optical sensor by UUID
     */
    public double readLightByUuid() {
        double light = 0.0;
        try {
            if (this.isCC2650) {
                light = calculateLight(
                        this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_OPTO_SENSOR_VALUE));
            } else {
                logger.info("Not optical sensor on CC2541.");
                light = 0.0;
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return light;
    }

    /*
     * Enable optical notifications
     */
    public void enableLightNotifications() {
        // Write "01:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_OPTO_SENSOR_NOTIFICATION_2650,
                    "01:00");
        } else {
            logger.info("Not optical sensor on CC2541.");
        }
    }

    /*
     * Disable optical notifications
     */
    public void disableLightNotifications() {
        // Write "00:00 to enable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_OPTO_SENSOR_NOTIFICATION_2650,
                    "00:00");
        } else {
            logger.info("Not optical sensor on CC2541.");
        }
    }

    /*
     * Set sampling period (only for CC2650)
     */
    public void setLuxometerPeriod(String period) {
        this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_OPTO_SENSOR_PERIOD_2650, period);
    }

    /*
     * Calculate light
     */
    private double calculateLight(String value) {

        logger.info("Received luxometer value: " + value);

        byte[] valueByte = hexStringToByteArray(value.replace(" ", ""));
        int sfloat = shortUnsignedAtOffset(valueByte, 0);

        int mantissa;
        int exponent;

        mantissa = sfloat & 0x0FFF;
        exponent = (sfloat & 0xF000) >> 12;

        return mantissa * (0.01 * Math.pow(2.0, exponent));

    }

    // --------------------------------------------------------------------------------------------
    //
    // Keys, reference: http://processors.wiki.ti.com/index.php/SensorTag_User_Guide (for CC2541)
    //
    // --------------------------------------------------------------------------------------------
    /*
     * Read keys status
     */
    public String readKeysStatus() {
        String key = "";
        // Read value
        try {
            if (this.isCC2650) {
                key = this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_KEYS_STATUS_2650);
            } else {
                key = this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_KEYS_STATUS_2541);
            }
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return key;
    }

    /*
     * Read keys status by UUID
     */
    public String readKeysStatusByUuid() {
        String key = "";
        try {
            key = this.bluetoothGattClient.readCharacteristicValueByUuid(TiSensorTagGatt.UUID_KEYS_STATUS);
        } catch (KuraException e) {
            logger.error(e.toString());
        }
        return key;
    }

    /*
     * Enable keys notification
     */
    public void enableKeysNotification() {
        // Write "01:00 to enable keys
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_KEYS_NOTIFICATION_2650, "01:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_KEYS_NOTIFICATION_2541, "01:00");
        }
    }

    /*
     * Disable keys notifications
     */
    public void disableKeysNotifications() {
        // Write "00:00 to disable notifications
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_KEYS_NOTIFICATION_2650, "00:00");
        } else {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_KEYS_NOTIFICATION_2541, "00:00");
        }
    }

    // -------------------------------------------------------------------------------------------------------
    //
    // IO Service
    //
    // -------------------------------------------------------------------------------------------------------
    /*
     * Enable IO Service
     */
    public void enableIOService() {
        // Write "01" to enable IO Service
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_ENABLE_2650, "01");
        } else {
            logger.info("Not IO Service on CC2541.");
        }

    }

    /*
     * Disable IO Service
     */
    public void disableIOService() {
        // Write "00" to disable IO Service
        if (this.isCC2650) {
            this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_ENABLE_2650, "00");
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    /*
     * Switch on red led
     */
    public void switchOnRedLed() {
        // Write "01" to switch on red led
        if (this.isCC2650) {
            int value;
            String hexValue;
            try {
                value = Integer.parseInt(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650),
                        16) | 0x01;
                hexValue = Integer.toHexString(value);
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650,
                        hexValue.length() < 2 ? "0" + hexValue : hexValue);
            } catch (KuraException e) {
                logger.error("Unable to read characteristic", e);
            }
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    /*
     * Switch off red led
     */
    public void switchOffRedLed() {
        // Write "00" to switch off red led
        if (this.isCC2650) {
            int value;
            String hexValue;
            try {
                value = Integer.parseInt(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650),
                        16) & 0xFE;
                hexValue = Integer.toHexString(value);
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650,
                        hexValue.length() < 2 ? "0" + hexValue : hexValue);
            } catch (KuraException e) {
                logger.error("Unable to read characteristic", e);
            }
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    /*
     * Switch on green led
     */
    public void switchOnGreenLed() {
        // Write "02" to switch on green led
        if (this.isCC2650) {
            int value;
            String hexValue;
            try {
                value = Integer.parseInt(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650),
                        16) | 0x02;
                hexValue = Integer.toHexString(value);
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650,
                        hexValue.length() < 2 ? "0" + hexValue : hexValue);
            } catch (KuraException e) {
                logger.error("Unable to read characteristic", e);
            }
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    /*
     * Switch off green led
     */
    public void switchOffGreenLed() {
        // Write "00" to switch off green led
        if (this.isCC2650) {
            int value;
            String hexValue;
            try {
                value = Integer.parseInt(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650),
                        16) & 0xFD;
                hexValue = Integer.toHexString(value);
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650,
                        hexValue.length() < 2 ? "0" + hexValue : hexValue);
            } catch (KuraException e) {
                logger.error("Unable to read characteristic", e);
            }
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    /*
     * Switch on buzzer
     */
    public void switchOnBuzzer() {
        // Write "04" to switch on buzzer
        if (this.isCC2650) {
            int value;
            String hexValue;
            try {
                value = Integer.parseInt(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650),
                        16) | 0x04;
                hexValue = Integer.toHexString(value);
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650,
                        hexValue.length() < 2 ? "0" + hexValue : hexValue);
            } catch (KuraException e) {
                logger.error("Unable to read characteristic", e);
            }
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    /*
     * Switch off buzzer
     */
    public void switchOffBuzzer() {
        // Write "00" to switch off buzzer
        if (this.isCC2650) {
            int value;
            String hexValue;
            try {
                value = Integer.parseInt(
                        this.bluetoothGattClient.readCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650),
                        16) & 0xFB;
                hexValue = Integer.toHexString(value);
                this.bluetoothGattClient.writeCharacteristicValue(TiSensorTagGatt.HANDLE_IO_SENSOR_VALUE_2650,
                        hexValue.length() < 2 ? "0" + hexValue : hexValue);
            } catch (KuraException e) {
                logger.error("Unable to read characteristic", e);
            }
        } else {
            logger.info("Not IO Service on CC2541.");
        }
    }

    // ---------------------------------------------------------------------------------------------
    //
    // BluetoothLeNotificationListener API
    //
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onDataReceived(String handle, String value) {

        if (handle.equals(TiSensorTagGatt.HANDLE_KEYS_STATUS_2541)
                || handle.equals(TiSensorTagGatt.HANDLE_KEYS_STATUS_2650)) {
            logger.info("Received keys value: " + value);
            if (!value.equals("00")) {
                BluetoothLe.doPublishKeys(this.device.getAddress(), Integer.parseInt(value));
            }
        }

    }

    // ---------------------------------------------------------------------------------------------
    //
    // Auxiliary methods
    //
    // ---------------------------------------------------------------------------------------------
    private int unsignedToSigned(int unsigned, int bitLength) {
        if ((unsigned & 1 << bitLength - 1) != 0) {
            unsigned = -1 * ((1 << bitLength - 1) - (unsigned & (1 << bitLength - 1) - 1));
        }
        return unsigned;
    }

    private String hexAsciiToString(String hex) {
        hex = hex.replaceAll(" ", "");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1]; // Interpret MSB as signedan
        return (upperByte << 8) + lowerByte;
    }

    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = c[offset] & 0xFF;
        Integer upperByte = c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

    private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = c[offset] & 0xFF;
        Integer mediumByte = c[offset + 1] & 0xFF;
        Integer upperByte = c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }

    private String toHexString(byte[] hexValue) {
        StringBuilder data = new StringBuilder();
        for (byte b : hexValue) {
            data.append(String.format("%02x", b));
        }
        return data.toString();
    }

    private void getServices() {
        if (services != null) {
            services.put("TEMPERATURE", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_TEMP_SENSOR_SERVICE));
            services.put("HUMIDITY", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_HUM_SENSOR_SERVICE));
            services.put("PRESSURE", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_PRE_SENSOR_SERVICE));
            services.put("KEYS", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_KEYS_SERVICE));
            if (isCC2650) {
                services.put("OPTO", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_OPTO_SENSOR_SERVICE));
                services.put("MOVEMENT", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_MOV_SENSOR_SERVICE));
                services.put("IO", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_IO_SENSOR_SERVICE));
            } else {
                services.put("ACCELEROMETER",
                        this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_ACC_SENSOR_SERVICE));
                services.put("MAGNETOMETER",
                        this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_MAG_SENSOR_SERVICE));
                services.put("GYROSCOPE", this.bluetoothGattClient.getService(TiSensorTagGatt.UUID_GYR_SENSOR_SERVICE));
            }
        }
    }
}
