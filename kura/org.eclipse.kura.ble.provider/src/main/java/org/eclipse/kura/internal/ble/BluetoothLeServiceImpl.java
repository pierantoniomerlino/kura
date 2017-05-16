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
package org.eclipse.kura.internal.ble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.BluetoothLeService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothManager;

public class BluetoothLeServiceImpl implements BluetoothLeService {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeServiceImpl.class);

    private BluetoothManager bluetoothManager;

    protected void activate(ComponentContext context) {
        logger.info("Activating Bluetooth Le Service...");
        if (!startBluetoothSystemd()) {
            startBluetoothDaemon();
        }
        try {
            this.bluetoothManager = BluetoothManager.getBluetoothManager();
        } catch (Exception e) {
            logger.error("Failed to start bluetooth service", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        logger.debug("Deactivating Bluetooth Service...");
        this.bluetoothManager = null;
    }

    @Override
    public List<BluetoothLeAdapter> getAdapters() {
        List<BluetoothLeAdapter> adapters = new ArrayList<>();
        for (tinyb.BluetoothAdapter adapter : this.bluetoothManager.getAdapters()) {
            adapters.add(new BluetoothLeAdapterImpl(adapter));
        }
        return adapters;
    }

    @Override
    public BluetoothLeAdapter getAdapter(String interfaceName) {
        BluetoothLeAdapterImpl adapter = null;
        for (tinyb.BluetoothAdapter ba : this.bluetoothManager.getAdapters()) {
            if (ba.getInterfaceName().equals(interfaceName)) {
                adapter = new BluetoothLeAdapterImpl(ba);
                break;
            }
        }
        return adapter;
    }

    private boolean startBluetoothSystemd() {
        String systemdCommand = "systemctl start bluetooth";
        boolean started = false;
        Process process;
        try {
            process = Runtime.getRuntime().exec(systemdCommand);
            started = process.waitFor() == 0 ? true : false;
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to start linux systemd bluetooth", e); // debug???
        }
        return started;
    }

    private void startBluetoothDaemon() {
        String daemonCommand = "bluetoothd -E";
        try {
            Runtime.getRuntime().exec(daemonCommand);
        } catch (IOException e) {
            logger.error("Failed to start linux bluetooth service", e);
        }
    }
}
