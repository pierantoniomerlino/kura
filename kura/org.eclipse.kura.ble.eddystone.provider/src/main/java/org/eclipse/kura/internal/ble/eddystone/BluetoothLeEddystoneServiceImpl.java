/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.internal.ble.eddystone;

import org.eclipse.kura.ble.eddystone.BluetoothLeEddystone;
import org.eclipse.kura.ble.eddystone.BluetoothLeEddystoneService;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconAdvertising;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconFactory;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconScanner;
import org.eclipse.kura.internal.ble.BluetoothLeBeaconAdvertisingImpl;
import org.eclipse.kura.internal.ble.BluetoothLeBeaconScannerImpl;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothLeEddystoneServiceImpl implements BluetoothLeEddystoneService {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeEddystoneServiceImpl.class);

    protected void activate(ComponentContext context) {
        logger.info("Activating Bluetooth Service...");
    }

    protected void deactivate(ComponentContext context) {
        logger.debug("Deactivating Bluetooth Service...");
    }

    @Override
    public BluetoothLeBeaconScanner<BluetoothLeEddystone> getBeaconScanner(BluetoothLeAdapter adapter,
            BluetoothLeBeaconFactory<BluetoothLeEddystone> factory) {
        return new BluetoothLeBeaconScannerImpl<>(adapter, factory);
    }

    @Override
    public BluetoothLeBeaconAdvertising<BluetoothLeEddystone> getBeaconAdvertising(BluetoothLeAdapter adapter) {
        return new BluetoothLeBeaconAdvertisingImpl<>(adapter);
    }

}
