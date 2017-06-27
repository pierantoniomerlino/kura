
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
package org.eclipse.kura.example.eddystone;

import java.util.Map;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystone;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystoneService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconAdvertising;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.internal.ble.eddystone.EddystoneFrameType;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EddystoneExample implements ConfigurableComponent {

    private static final Logger logger = LoggerFactory.getLogger(EddystoneExample.class);

    private static final String PROPERTY_ENABLE = "enableAdvertising";
    private static final String PROPERTY_MIN_INTERVAL = "minBeaconInterval";
    private static final String PROPERTY_MAX_INTERVAL = "maxBeaconInterval";
    private static final String PROPERTY_TYPE = "eddystoneType";
    private static final String PROPERTY_NAMESPACE = "eddystoneUidNamespace";
    private static final String PROPERTY_INSTANCE = "eddystoneUidInstance";
    private static final String PROPERTY_URL = "eddystoneUrl";
    private static final String PROPERTY_TX_POWER = "txPower";
    private static final String PROPERTY_INAME = "iname";

    private BluetoothLeService bluetoothLeService;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private BluetoothLeEddystoneService bluetoothLeEddystoneService;
    private BluetoothLeBeaconAdvertising<BluetoothLeEddystone> advertising;

    private boolean enable;
    private Integer minInterval;
    private Integer maxInterval;
    private EddystoneFrameType eddystoneFrametype;
    private String uidNamespace;
    private String uidInstance;
    private String urlUrl;
    private Integer txPower;
    private String iname = "hci0";

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    public void unsetBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = null;
    }

    public void setBluetoothLeEddystoneService(BluetoothLeEddystoneService bluetoothLeEddystoneService) {
        this.bluetoothLeEddystoneService = bluetoothLeEddystoneService;
    }

    public void unsetBluetoothLeEddystoneService(BluetoothLeEddystoneService bluetoothLeEddystoneService) {
        this.bluetoothLeEddystoneService = null;
    }

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    protected void activate(ComponentContext context, Map<String, Object> properties) {
        logger.info("Activating Bluetooth Eddystone example...");

        update(properties);

        logger.debug("Activating Eddystone Example... Done.");

    }

    protected void deactivate(ComponentContext context) {

        logger.debug("Deactivating Eddystone Example...");

        // Stop the advertising
        try {
            this.advertising.stopBeaconAdvertising();
        } catch (KuraException e) {
            logger.error("Stop Eddystone advertising failed", e);
        }

        // cancel bluetoothAdapter
        this.bluetoothLeAdapter = null;

        logger.debug("Deactivating Eddystone Example... Done.");
    }

    protected void updated(Map<String, Object> properties) {

        // Stop the advertising
        try {
            this.advertising.stopBeaconAdvertising();
        } catch (KuraException e) {
            logger.error("Stop Beacon advertising failed", e);
        }

        update(properties);

        logger.debug("Updating Eddystone Example... Done.");
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
            logger.info("Bluetooth adapter interface => " + this.iname);
            logger.info("Bluetooth adapter address => " + this.bluetoothLeAdapter.getAddress());

            if (!this.bluetoothLeAdapter.isPowered()) {
                logger.info("Enabling bluetooth adapter...");
                this.bluetoothLeAdapter.setPowered(true);
            }

            this.advertising = this.bluetoothLeEddystoneService.getBeaconAdvertising(this.bluetoothLeAdapter);
            configureBeacon();

        } else {
            logger.warn("No Bluetooth adapter found ...");
        }
    }

    private void configureBeacon() {

        try {
            if (this.enable) {
                BluetoothLeEddystone eddystone = new BluetoothLeEddystone();
                if (this.eddystoneFrametype.equals(EddystoneFrameType.UID)) {
                    eddystone.configureEddystoneUIDFrame(this.uidNamespace, this.uidInstance, this.txPower);
                } else if (this.eddystoneFrametype.equals(EddystoneFrameType.URL)) {
                    eddystone.configureEddystoneURLFrame(this.urlUrl, this.txPower);
                }
                this.advertising.updateBeaconAdvertisingData(eddystone);
                this.advertising.updateBeaconAdvertisingInterval(this.minInterval, this.maxInterval);

                this.advertising.startBeaconAdvertising();
            } else {
                this.advertising.stopBeaconAdvertising();
            }
        } catch (KuraException e) {
            logger.error("IBeacon configuration failed", e);
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
            if (properties.get(PROPERTY_TX_POWER) != null) {
                this.txPower = (Integer) properties.get(PROPERTY_TX_POWER);
            }
            if (properties.get(PROPERTY_INAME) != null) {
                this.iname = (String) properties.get(PROPERTY_INAME);
            }
            if (properties.get(PROPERTY_TYPE) != null) {
                if ("UID".equals((String) properties.get(PROPERTY_TYPE))) {
                    this.eddystoneFrametype = EddystoneFrameType.UID;
                } else if ("URL".equals((String) properties.get(PROPERTY_TYPE))) {
                    this.eddystoneFrametype = EddystoneFrameType.URL;
                }
            }
            if (this.eddystoneFrametype.equals(EddystoneFrameType.UID)) {
                if (properties.get(PROPERTY_NAMESPACE) != null
                        && ((String) properties.get(PROPERTY_NAMESPACE)).length() == 20) {
                    this.uidNamespace = (String) properties.get(PROPERTY_NAMESPACE);
                } else {
                    logger.warn("Invalid UID namespace.");
                }
                if (properties.get(PROPERTY_INSTANCE) != null
                        && ((String) properties.get(PROPERTY_INSTANCE)).length() == 12) {
                    this.uidInstance = (String) properties.get(PROPERTY_INSTANCE);
                } else {
                    logger.warn("Invalid UID instance.");
                }
            }
            if (this.eddystoneFrametype.equals(EddystoneFrameType.URL) && properties.get(PROPERTY_URL) != null) {
                this.urlUrl = (String) properties.get(PROPERTY_URL);
            }
        }
    }
}
