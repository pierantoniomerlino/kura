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
package org.eclipse.kura.linux.bluetooth;

import org.eclipse.kura.bluetooth.BluetoothConnector;
import org.eclipse.kura.bluetooth.BluetoothDevice;
import org.eclipse.kura.bluetooth.BluetoothGatt;
import org.eclipse.kura.linux.bluetooth.le.BluetoothGattImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BluetoothDeviceImpl implements BluetoothDevice {

    private final tinyb.BluetoothDevice device;

    public BluetoothDeviceImpl(tinyb.BluetoothDevice device) {
        this.device = device;
    }

    // --------------------------------------------------------------------
    //
    // BluetoothDevice API
    //
    // --------------------------------------------------------------------
    @Override
    public String getName() {
        return this.device.getName();
    }

    @Override
    public String getAdress() {
        return getAddress();
    }

    @Override
    public String getAddress() {
        return this.device.getAddress();
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public BluetoothGatt getBluetoothGatt() {
        return getBluetoothGattClient();
    }

    @Override
    public int getReceivedSignalStrength() {
        return this.device.getRSSI();
    }

    @Override
    public int getTransmittedSignalStrength() {
        return this.device.getTxPower();
    }

    @Override
    public BluetoothGatt getBluetoothGattClient() {
        return new BluetoothGattImpl(this.device);
    }

    @Override
    public BluetoothConnector getBluetoothConnector() {
        BluetoothConnector bluetoothConnector = null;
        BundleContext bundleContext = BluetoothServiceImpl.getBundleContext();
        if (bundleContext != null) {
            ServiceReference<BluetoothConnector> sr = bundleContext.getServiceReference(BluetoothConnector.class);
            if (sr != null) {
                bluetoothConnector = bundleContext.getService(sr);
            }
        }
        return bluetoothConnector;
    }

}
