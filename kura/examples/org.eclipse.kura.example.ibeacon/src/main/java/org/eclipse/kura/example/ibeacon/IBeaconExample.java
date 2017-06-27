
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
package org.eclipse.kura.example.ibeacon;

import java.util.Map;
import java.util.UUID;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeacon;
import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeaconService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconAdvertising;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IBeaconExample implements ConfigurableComponent {

    private static final Logger s_logger = LoggerFactory.getLogger(IBeaconExample.class);

    private static final String PROPERTY_ENABLE = "enableAdvertising";
    private static final String PROPERTY_MIN_INTERVAL = "minBeaconInterval";
    private static final String PROPERTY_MAX_INTERVAL = "maxBeaconInterval";
    private static final String PROPERTY_UUID = "uuid";
    private static final String PROPERTY_MAJOR = "major";
    private static final String PROPERTY_MINOR = "minor";
    private static final String PROPERTY_TX_POWER = "txPower";
    private static final String PROPERTY_INAME = "iname";

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private BluetoothLeIBeaconService bluetoothLeIBeaconService;
    private BluetoothLeBeaconAdvertising<BluetoothLeIBeacon> advertising;

    private boolean enable;
    private Integer minInterval;
    private Integer maxInterval;
    private String uuid;
    private Integer major;
    private Integer minor;
    private Integer txPower;
    private String iname = "hci0";

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

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    protected void activate(ComponentContext context, Map<String, Object> properties) {
        s_logger.info("Activating Bluetooth Beacon example...");

        update(properties);

        s_logger.debug("Activating iBeacon Example... Done.");

    }

    protected void deactivate(ComponentContext context) {

        s_logger.debug("Deactivating iBeacon Example...");

        // Stop the advertising
        try {
            this.advertising.stopBeaconAdvertising();
        } catch (KuraException e) {
            s_logger.error("Stop iBeacon advertising failed", e);
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        s_logger.debug("Deactivating iBeacon Example... Done.");
    }

    protected void updated(Map<String, Object> properties) {

        // Stop the advertising
        try {
            this.advertising.stopBeaconAdvertising();
        } catch (KuraException e) {
            s_logger.error("Stop iBeacon advertising failed", e);
        }

        update(properties);

        s_logger.debug("Updating iBeacon Example... Done.");
    }

    // --------------------------------------------------------------------
    //
    // Private methods
    //
    // --------------------------------------------------------------------

    private void update(Map<String, Object> properties) {
        readProperties(properties);

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        // Get Bluetooth adapter with Beacon capabilities and ensure it is enabled
        this.bluetoothLeAdapter = this.bluetoothLeService.getAdapter(this.iname);
        if (this.bluetoothLeAdapter != null) {
            s_logger.info("Bluetooth adapter interface => " + this.iname);
            s_logger.info("Bluetooth adapter address => " + this.bluetoothLeAdapter.getAddress());

            if (!this.bluetoothLeAdapter.isPowered()) {
                s_logger.info("Enabling bluetooth adapter...");
                this.bluetoothLeAdapter.setPowered(true);
            }

            this.advertising = this.bluetoothLeIBeaconService.getBeaconAdvertising(this.bluetoothLeAdapter);
            configureBeacon();

        } else {
            s_logger.warn("No Bluetooth adapter found ...");
        }
    }

    private void configureBeacon() {

        try {
            if (this.enable) {
                BluetoothLeIBeacon iBeacon = new BluetoothLeIBeacon(UUID.fromString(this.uuid), this.major, this.minor,
                        this.txPower);
                this.advertising.updateBeaconAdvertisingData(iBeacon);
                this.advertising.updateBeaconAdvertisingInterval(this.minInterval, this.maxInterval);

                this.advertising.startBeaconAdvertising();
            } else {
                this.advertising.stopBeaconAdvertising();
            }
        } catch (KuraException e) {
            s_logger.error("IBeacon configuration failed", e);
        }
    }

    private void readProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get(PROPERTY_ENABLE) != null) {
                this.enable = (Boolean) properties.get(PROPERTY_ENABLE);
            }
            if (properties.get(PROPERTY_MIN_INTERVAL) != null) {
                this.minInterval = (int) ((Integer) properties.get(PROPERTY_MIN_INTERVAL) / 0.625);
            }
            if (properties.get(PROPERTY_MAX_INTERVAL) != null) {
                this.maxInterval = (int) ((Integer) properties.get(PROPERTY_MAX_INTERVAL) / 0.625);
            }
            if (properties.get(PROPERTY_UUID) != null) {
                if (((String) properties.get(PROPERTY_UUID)).trim().replace("-", "").length() != 32) {
                    s_logger.warn("Wrong UUID size!");
                } else {
                    this.uuid = ((String) properties.get(PROPERTY_UUID));
                }
            }
            if (properties.get(PROPERTY_MAJOR) != null) {
                this.major = (Integer) properties.get(PROPERTY_MAJOR);
            }
            if (properties.get(PROPERTY_MINOR) != null) {
                this.minor = (Integer) properties.get(PROPERTY_MINOR);
            }
            if (properties.get(PROPERTY_TX_POWER) != null) {
                this.txPower = (Integer) properties.get(PROPERTY_TX_POWER);
            }
            if (properties.get(PROPERTY_INAME) != null) {
                this.iname = (String) properties.get(PROPERTY_INAME);
            }
        }
    }
}
