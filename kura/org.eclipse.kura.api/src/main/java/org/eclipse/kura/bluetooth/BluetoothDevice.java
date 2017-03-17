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
package org.eclipse.kura.bluetooth;

import org.osgi.annotation.versioning.ProviderType;

/**
 * BluetoothDevice represents a Bluetooth device to which connections
 * may be made. The type of Bluetooth device will determine the
 * communications mechanism. Standard Bluetooth devices will use
 * the {@link BluetoothConnector} and Bluetooth LE devices will use
 * {@link BluetoothGatt}.
 * <br>
 * When using {@link BluetoothConnector}, A default connector is not provided
 * and will need to be implemented.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothDevice {

    /**
     * Returns the the name of the Bluetooth device.
     *
     * @return The devices name
     */
    public String getName();

    /**
     * Returns the physical address of the device.
     *
     * @return The physical address of the device
     */
    @Deprecated
    public String getAdress();

    /**
     * Returns the physical address of the device.
     *
     * @return The physical address of the device
     */
    public String getAddress();

    /**
     * Returns the Received Signal Strength Indicator of the device.
     *
     * @return The Received Signal Strength Indicator of the device
     */
    public int getReceivedSignalStrength();

    /**
     * Returns the Transmitted Signal Strength Indicator of the device.
     *
     * @return The Transmitted Signal Strength Indicator of the device
     */
    public int getTransmittedSignalStrength();

    /**
     * The type of devices, name whether the device supports
     * Bluetooth LE or not.
     *
     * @return The device type
     */
    @Deprecated
    public int getType();

    /**
     * Return a connector for communicating with a standard
     * Bluetooth device. A default connector is not provided
     * and will need to be implemented.
     *
     * @return Standard Bluetooth connector
     */
    public BluetoothConnector getBluetoothConnector();

    /**
     * Return an instance of a Bluetooth GATT server to be
     * used in communicating with Bluetooth LE devices.
     *
     * @return BluetoothGatt
     */
    @Deprecated
    public BluetoothGatt getBluetoothGatt();

    /**
     * Return an instance of a Bluetooth GATT server to be
     * used in communicating with Bluetooth LE devices.
     *
     * @return BluetoothGatt
     */
    public BluetoothGatt getBluetoothGattClient();

}
