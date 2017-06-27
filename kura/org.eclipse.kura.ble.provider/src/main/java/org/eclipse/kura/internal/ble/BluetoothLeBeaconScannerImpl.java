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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothAdvertisementData;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeacon;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconFactory;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconListener;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconScanner;
import org.eclipse.kura.internal.ble.util.BTSnoopListener;
import org.eclipse.kura.internal.ble.util.BluetoothLeUtil;
import org.eclipse.kura.internal.ble.util.BluetoothProcess;
import org.eclipse.kura.internal.ble.util.BluetoothProcessListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothLeBeaconScannerImpl<T extends BluetoothLeBeacon>
        implements BluetoothLeBeaconScanner<T>, BTSnoopListener, BluetoothProcessListener {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeBeaconScannerImpl.class);

    private BluetoothLeAdapter adapter;
    private BluetoothLeBeaconFactory<T> factory;
    private List<BluetoothLeBeaconListener<T>> listeners;
    private BluetoothProcess dumpProc;
    private BluetoothProcess hcitoolProc;
    private boolean isScanning;

    public BluetoothLeBeaconScannerImpl(BluetoothLeAdapter adapter, BluetoothLeBeaconFactory<T> factory) {
        this.adapter = adapter;
        this.factory = factory;
        this.listeners = new ArrayList<>();
        this.isScanning = false;
    }

    @Override
    public void startBeaconScan(long timeout) throws KuraException {
        logger.info("Starting bluetooth beacon scan...");
        this.hcitoolProc = BluetoothLeUtil.hcitoolCmd(this.adapter.getInterfaceName(),
                new String[] { "lescan-passive", "--duplicates" }, this);
        this.dumpProc = BluetoothLeUtil.btdumpCmd(this.adapter.getInterfaceName(), this);
        this.isScanning = true;
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < timeout) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted Exception", e);
            }
        }
        stopBeaconScan();
    }

    @Override
    public void stopBeaconScan() throws KuraException {
        logger.info("Stopping bluetooth beacon scan...");
        if (this.hcitoolProc != null) {
            this.hcitoolProc.destroy();
        }
        if (this.dumpProc != null) {
            this.dumpProc.destroyBTSnoop();
        }
        this.isScanning = false;
    }

    @Override
    public boolean isScanning() {
        return this.isScanning;
    }

    @Override
    public void addBeaconListener(BluetoothLeBeaconListener<T> listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        } else {
            logger.warn("The listener has been already registered");
        }
    }

    @Override
    public void removeBeaconListener(BluetoothLeBeaconListener<T> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void processBTSnoopRecord(byte[] record) {
        // Extract raw advertisement data
        BluetoothAdvertisementData bAdData = BluetoothLeUtil.parseLEAdvertisement(record);
        if (bAdData != null) {
            List<T> beacons = this.factory.createBeacons(bAdData);

            // Notify advertisement listeners
            if (!beacons.isEmpty() && !this.listeners.isEmpty()) {
                for (BluetoothLeBeaconListener<T> l : this.listeners) {
                    l.onBeaconsReceived(beacons);
                }
            }
        }
    }

    @Override
    public void processErrorStream(String string) {
        // Not used
    }

    @Override
    public void processInputStream(String string) throws KuraException {
        // Not used
    }

    @Override
    public void processInputStream(int ch) throws KuraException {
        // Not used
    }
}
