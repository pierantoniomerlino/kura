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

import java.util.UUID;

import org.eclipse.kura.KuraException;
import org.osgi.annotation.versioning.ProviderType;

/**
 * BluetoothLeGattDescriptor represents a GATT descriptor.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothLeGattDescriptor {

    /**
     * Reads the value of this descriptor
     * 
     * @return A byte[] containing data from this descriptor
     * @throws KuraException
     */
    public byte[] readValue() throws KuraException;;

    /**
     * Writes the value of this descriptor.
     * 
     * @param value
     *            The data as byte[] to be written
     * @throws KuraException
     */
    public void writeValue(byte[] value) throws KuraException;

    /**
     * Get the UUID of this descriptor.
     * 
     * @return The 128 byte UUID of this descriptor, NULL if an error occurred
     */
    public UUID getUUID();

    /**
     * Returns the characteristic to which this descriptor belongs to.
     * 
     * @return The characteristic.
     */
    public BluetoothLeGattCharacteristic getCharacteristic();

    /**
     * Returns the cached value of this descriptor, if any.
     * 
     * @return The cached value of this descriptor.
     */
    public byte[] getValue();

}
