/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *     Red Hat Inc - minor clean ups
 *******************************************************************************/
package org.eclipse.kura.linux.bluetooth.le;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.kura.bluetooth.BluetoothBeaconData;
import org.eclipse.kura.bluetooth.BluetoothBeaconScanListener;
import org.eclipse.kura.bluetooth.BluetoothDevice;
import org.eclipse.kura.bluetooth.BluetoothLeScanListener;
import org.eclipse.kura.bluetooth.listener.BluetoothAdvertisementData;
import org.eclipse.kura.bluetooth.listener.BluetoothAdvertisementScanListener;
import org.eclipse.kura.linux.bluetooth.BluetoothDeviceImpl;
import org.eclipse.kura.linux.bluetooth.util.BTSnoopListener;
import org.eclipse.kura.linux.bluetooth.util.BluetoothProcess;
import org.eclipse.kura.linux.bluetooth.util.BluetoothProcessListener;
import org.eclipse.kura.linux.bluetooth.util.BluetoothUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothException;

public class BluetoothLeScanner implements BluetoothProcessListener, BTSnoopListener {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeScanner.class);
    private static final String SIGINT = "2";

    private BluetoothProcess proc = null;
    private BluetoothProcess dumpProc = null;
    private BluetoothLeScanListener listener = null;
    private BluetoothBeaconScanListener beaconListener = null;
    private BluetoothAdvertisementScanListener advertisementListener = null;
    private boolean scanRunning = false;
    private String companyName;

    public BluetoothLeScanner() {
    }

    public void startScan(tinyb.BluetoothAdapter adapter, BluetoothLeScanListener listener) {
        this.listener = listener;
        logger.info("Starting bluetooth le scan...");

        // Start scan process
        try {
            adapter.startDiscovery();
        } catch (BluetoothException e) {
            logger.error("Failed to start discovering.", e);
        } finally {
            this.scanRunning = adapter.getDiscovering();
        }
    }

    public void startAdvertisementScan(String name, String companyName, BluetoothAdvertisementScanListener listener) {
        this.advertisementListener = listener;
        this.companyName = companyName;

        logger.info("Starting bluetooth le advertisement scan...");

        // Start scan process
        this.proc = BluetoothUtil.hcitoolCmd(name, new String[] { "lescan-passive", "--duplicates" }, this);

        // Start dump process
        this.dumpProc = BluetoothUtil.btdumpCmd(name, this);

        this.scanRunning = true;
    }

    public void startBeaconScan(String name, String companyName, BluetoothBeaconScanListener listener) {
        this.beaconListener = listener;
        this.companyName = companyName;

        logger.info("Starting bluetooth le beacon scan...");

        // Start scan process
        this.proc = BluetoothUtil.hcitoolCmd(name, new String[] { "lescan-passive", "--duplicates" }, this);

        // Start dump process
        this.dumpProc = BluetoothUtil.btdumpCmd(name, this);

        this.scanRunning = true;
    }

    public void killScan(tinyb.BluetoothAdapter adapter, boolean getDevices) {
        try {
            adapter.stopDiscovery();
        } catch (BluetoothException e) {
            logger.error("Failed to stop discovering.", e);
        } finally {
            this.scanRunning = adapter.getDiscovering();
        }
        if (getDevices) {
            this.listener.onScanResults(convertToBluetoothDevices(adapter.getDevices()));
        }

        // SIGINT must be sent to the hcitool process. Otherwise the adapter must be toggled (down/up).
        if (this.proc != null) {
            logger.info("Killing hcitool...");
            BluetoothUtil.killCmd(BluetoothUtil.HCITOOL, SIGINT);
            this.proc = null;
        } else {
            logger.info("Cannot Kill hcitool, proc = null ...");
        }

        // Shut down btdump process
        if (this.dumpProc != null) {
            logger.info("Killing btdump...");
            this.dumpProc.destroyBTSnoop();
            this.dumpProc = null;
        } else {
            logger.info("Cannot Kill btdump, m_dump_proc = null ...");
        }
    }

    // --------------------------------------------------------------------
    //
    // BluetoothProcessListener API
    //
    // --------------------------------------------------------------------
    @Override
    public void processInputStream(String string) {
    }

    @Override
    public void processInputStream(int ch) {
    }

    @Override
    public void processBTSnoopRecord(byte[] record) {

        try {

            // Extract raw advertisement data
            BluetoothAdvertisementData bAdData = BluetoothUtil.parseLEAdvertisement(record);

            // Notify advertisement listeners
            if (bAdData != null && this.advertisementListener != null) {
                try {
                    this.advertisementListener.onAdvertisementDataReceived(bAdData);
                } catch (Exception e) {
                    logger.error("Scan listener threw exception", e);
                }
            }

            // Extract beacon advertisements
            List<BluetoothBeaconData> beaconDatas = BluetoothUtil.parseLEAdvertisingReport(record, this.companyName);

            // Extract beacon data
            for (BluetoothBeaconData beaconData : beaconDatas) {

                // Notify the listener
                try {

                    if (this.beaconListener != null) {
                        this.beaconListener.onBeaconDataReceived(beaconData);
                    }

                } catch (Exception e) {
                    logger.error("Scan listener threw exception", e);
                }
            }

        } catch (Exception e) {
            logger.error("Error processing advertising report", e);
        }

    }

    @Override
    public void processErrorStream(String string) {
    }

    public boolean isScanRunning() {
        return this.scanRunning;
    }

    @Deprecated
    public boolean is_scanRunning() {
        return this.scanRunning;
    }

    @Deprecated
    public void set_scanRunning(boolean scanRunning) {
    }

    private List<BluetoothDevice> convertToBluetoothDevices(List<tinyb.BluetoothDevice> devices) {
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        for (tinyb.BluetoothDevice device : devices) {
            bluetoothDevices.add(new BluetoothDeviceImpl(device));
        }
        return bluetoothDevices;
    }
}
