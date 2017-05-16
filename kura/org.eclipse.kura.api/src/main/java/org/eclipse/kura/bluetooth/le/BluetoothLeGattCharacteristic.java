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
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.kura.KuraException;
import org.osgi.annotation.versioning.ProviderType;

/**
 * BluetoothLeGattCharacteristic represents a GATT characteristic.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothLeGattCharacteristic {

    /**
     * Find a BluetoothLeGattDescriptor specifying the UUID of the descriptor.
     * 
     * @param uuid
     *            The UUID of the GATT descriptor
     * @return The BluetoothLeGattDescriptor
     * @throws KuraException
     */
    public BluetoothLeGattDescriptor findDescriptor(UUID uuid) throws KuraException;

    /**
     * Returns a list of BluetoothLeGattDescriptors available on this characteristic.
     * 
     * @return A list of BluetoothLeGattDescriptor
     * @throws KuraException
     */
    public List<BluetoothLeGattDescriptor> findDescriptors() throws KuraException;

    /**
     * Reads the value of this characteristic.
     * 
     * @return A byte[] containing the value of this characteristic.
     */
    public byte[] readValue() throws KuraException;

    /**
     * Enables notifications for the value and calls accept function of the Consumer
     * object. It enables notifications for this characteristic at BLE level.
     * 
     * @param callback
     *            A Consumer<byte[]> object. Its accept function will be called
     *            when a notification is issued.
     * 
     * @throws KuraException
     */
    public void enableValueNotifications(Consumer<byte[]> callback) throws KuraException;

    /**
     * Disables notifications of the value and unregisters the consumer object
     * passed through the corresponding enable method. It disables notications
     * at BLE level for this characteristic.
     * 
     * @throws KuraException
     */
    public void disableValueNotifications() throws KuraException;

    /**
     * Writes the value of this characteristic.
     * 
     * @param value
     *            The data as byte[] to be written
     * 
     * @throws KuraException
     */
    public void writeValue(byte[] value) throws KuraException;

    /**
     * Get the UUID of this characteristic.
     * 
     * @return The 128 byte UUID of this characteristic, NULL if an error occurred
     */
    public UUID getUUID();

    /**
     * Returns the service to which this characteristic belongs to.
     * 
     * @return The BluetoothLeGattService.
     */
    public BluetoothLeGattService getService();

    /**
     * Returns the cached value of this characteristic, if any.
     * 
     * @return The cached value of this characteristic.
     */
    public byte[] getValue();

    /**
     * Returns true if notification for changes of this characteristic are
     * activated.
     * 
     * @return True if notificatios are activated.
     */
    public boolean isNotifying();

    /**
     * Returns the list of BluetoothLeGattCharacteristicProperties this characteristic has.
     * 
     * @return A list of BluetoothLeGattCharacteristicProperties for this characteristic.
     */
    public List<BluetoothLeGattCharacteristicProperties> getProperties();

}