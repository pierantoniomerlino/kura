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

import java.util.List;
import java.util.UUID;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothGattCharacteristic {

    /*
     * Get UUID of this characteristic
     */
    public UUID getUuid();

    /*
     * Get value of this characteristic
     */
    @Deprecated
    public Object getValue();

    /*
     * Set value of this characteristic
     */
    @Deprecated
    public void setValue(Object value);

    /*
     * Read the value of this characteristic
     */
    public byte[] readValue();

    /*
     * Write the value of this characteristic
     */
    public void writeValue(byte[] value);

    /*
     * Get permissions of this characteristic
     */
    public int getPermissions();

    /*
     * Get handle of this characteristic
     */
    public String getHandle();

    /*
     * Get handle of the characteristic value
     */
    public String getValueHandle();

    /*
     * Get characteristic properties
     */
    public int getProperties();

    /*
     * Get characteristic properties
     */
    public List<BluetoothGattCharacteristicProperties> getPropertyList();

    /*
     * Get characteristic descriptors
     */
    public List<BluetoothGattDescriptor> getDescriptors();

    /*
     * Get specific characteristic descriptor based on UUID
     */
    public BluetoothGattDescriptor getDescriptor(UUID uuid);

    // APIs for data notifications
}
