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
package org.eclipse.kura.linux.bluetooth;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.BluetoothAdapter;
import org.eclipse.kura.bluetooth.BluetoothBeaconCommandListener;
import org.eclipse.kura.bluetooth.BluetoothService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothManager;

public class BluetoothServiceImpl implements BluetoothService {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothServiceImpl.class);

    private static ComponentContext componentContext;
    private static BluetoothManager bluetoothManager;

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    protected void activate(ComponentContext context) {
        logger.info("Activating Bluetooth Service...");
        componentContext = context;
        bluetoothManager = BluetoothManager.getBluetoothManager();
    }

    protected void deactivate() {
        logger.debug("Deactivating Bluetooth Service...");
    }

    // --------------------------------------------------------------------
    //
    // Service APIs
    //
    // --------------------------------------------------------------------
    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return getBluetoothAdapter("hci0");
    }

    @Override
    public BluetoothAdapter getBluetoothAdapter(String name) {
        BluetoothAdapterImpl adapter = null;
        try {
            for (tinyb.BluetoothAdapter ba : bluetoothManager.getAdapters()) {
                if (ba.getInterfaceName().equals(name)) {
                    adapter = new BluetoothAdapterImpl(ba);
                    break;
                }
            }
            return adapter;
        } catch (KuraException e) {
            logger.error("Could not get bluetooth adapter", e);
            return null;
        }
    }

    @Override
    public BluetoothAdapter getBluetoothAdapter(String name, BluetoothBeaconCommandListener bbcl) {
        BluetoothAdapterImpl adapter = null;
        try {
            for (tinyb.BluetoothAdapter ba : bluetoothManager.getAdapters()) {
                if (ba.getInterfaceName().equals(name)) {
                    adapter = new BluetoothAdapterImpl(ba, bbcl);
                    break;
                }
            }
            return adapter;
        } catch (KuraException e) {
            logger.error("Could not get bluetooth beacon service", e);
            return null;
        }
    }

    // --------------------------------------------------------------------
    //
    // Local methods
    //
    // --------------------------------------------------------------------
    static BundleContext getBundleContext() {
        return componentContext.getBundleContext();
    }
}
