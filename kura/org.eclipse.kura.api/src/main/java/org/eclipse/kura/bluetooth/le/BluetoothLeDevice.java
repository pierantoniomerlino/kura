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
package org.eclipse.kura.bluetooth.le;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.kura.KuraException;
import org.osgi.annotation.versioning.ProviderType;

/**
 * BluetoothLeDevice represents a Bluetooth LE device to which connections
 * may be made.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothLeDevice {

    /**
     * Find a BluetoothLeGattService specifying the UUID of the service.
     * 
     * @param uuid
     *            The UUID of the GATT service
     * @return The BluetoothLeGattService
     * @throws KuraException
     */
    public BluetoothLeGattService findService(UUID uuid) throws KuraException;

    /**
     * Returns a list of BluetoothGattServices available on this device.
     * 
     * @return A list of BluetoothLeGattService
     * @throws KuraException
     */
    public List<BluetoothLeGattService> findServices() throws KuraException;

    /**
     * Disconnect from this device, removing all connected profiles.
     * 
     */
    public void disconnect() throws KuraException;

    /**
     * A connection to this device is established, connecting each profile
     * flagged as auto-connectable.
     *
     * @throws KuraException
     */
    public void connect() throws KuraException;

    /**
     * Connects a specific profile available on the device, given by UUID
     * 
     * @param uuid
     *            The UUID of the profile to be connected
     * @throws KuraException
     */
    public void connectProfile(UUID uuid) throws KuraException;

    /**
     * Disconnects a specific profile available on the device, given by UUID
     * 
     * @param uuid
     *            The UUID of the profile to be disconnected
     * @throws KuraException
     */
    public void disconnectProfile(UUID uuid) throws KuraException;

    /**
     * A connection to this device is established, and the device is then
     * paired.
     * 
     * @throws KuraException
     */
    public void pair() throws KuraException;

    /**
     * Returns the hardware address of this device.
     * 
     * @return The hardware address of this device.
     */
    public String getAddress();

    /**
     * Returns the remote friendly name of this device.
     * 
     * @return The remote friendly name of this device, or NULL if not set.
     */
    public String getName();

    /**
     * Returns an alternative friendly name of this device.
     * 
     * @return The alternative friendly name of this device, or NULL if not set.
     */
    public String getAlias();

    /**
     * Sets an alternative friendly name of this device.
     */
    public void setAlias(String value);

    /**
     * Returns the Bluetooth class of the device.
     * 
     * @return The Bluetooth class of the device.
     */
    public int getBluetoothClass();
    // This is the class of the bluetooth device.
    // Too many codes. See http://www.ampedrftech.com/cod.htm and
    // http://ampedrftech.com/guides/cod_definition.pdf

    /**
     * Returns the appearance of the device, as found by GAP service.
     * 
     * @return The appearance of the device, as found by GAP service.
     */
    public short getAppearance();
    // This is the type of the device.
    // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.gap.appearance.xml&u=org.bluetooth.characteristic.gap.appearance.xml

    /**
     * Returns the proposed icon name of the device.
     * 
     * @return The proposed icon name, or NULL if not set.
     */
    public String getIcon();
    // Proposed icon name according to the freedesktop.org icon naming specification. See
    // https://standards.freedesktop.org/icon-naming-spec/icon-naming-spec-latest.html

    /**
     * Returns the paired state the device.
     * 
     * @return The paired state of the device.
     */
    public boolean isPaired();

    /**
     * Returns the trusted state the device.
     * 
     * @return The trusted state of the device.
     */
    public boolean isTrusted();

    /**
     * Sets the trusted state the device.
     */
    public void setTrusted(boolean value);

    /**
     * Returns the blocked state the device.
     * 
     * @return The blocked state of the device.
     */
    public boolean isBlocked();

    /**
     * Sets the blocked state the device.
     */
    public void setBlocked(boolean value);

    /**
     * Returns if device uses only pre-Bluetooth 2.1 pairing mechanism.
     * 
     * @return True if device uses only pre-Bluetooth 2.1 pairing mechanism.
     */
    public boolean isLegacyPairing();

    /**
     * Returns the Received Signal Strength Indicator of the device.
     * 
     * @return The Received Signal Strength Indicator of the device.
     */
    public short getRSSI();

    /**
     * Returns the connected state of the device.
     * 
     * @return The connected state of the device.
     */
    public boolean isConnected();

    /**
     * Returns the UUIDs of the device.
     * 
     * @return Array containing the UUIDs of the device, ends with NULL.
     */
    public UUID[] getUUIDs();

    /**
     * Returns the local ID of the adapter.
     * 
     * @return The local ID of the adapter.
     */
    public String getModalias();

    /**
     * Returns the adapter on which this device was discovered or
     * connected.
     * 
     * @return The adapter.
     */
    public BluetoothLeAdapter getAdapter();

    /**
     * Returns a map containing manufacturer specific advertisement data.
     * An entry has a short key and an array of bytes.
     * 
     * @return manufacturer specific advertisement data.
     */
    public Map<Short, byte[]> getManufacturerData();
    // ???

    /**
     * Returns a map containing service advertisement data.
     * An entry has a UUID key and an array of bytes.
     * 
     * @return service advertisement data.
     */
    public Map<UUID, byte[]> getServiceData();

    /**
     * Returns the transmission power level (0 means unknown).
     * 
     * @return the transmission power level (0 means unknown).
     */
    public short getTxPower();

}
