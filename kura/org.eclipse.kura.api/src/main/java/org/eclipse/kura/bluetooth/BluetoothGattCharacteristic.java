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

<<<<<<< HEAD
    /*
     * Set the notification listener for this characteristic
     */
    public void setBluetoothLeNotificationListener(BluetoothLeNotificationListener listener);

    /*
     * Unset the notification listener for this characteristic
     */
    public void unsetBluetoothLeNotificationListener();

    /*
     * Get the notification listener for this characteristic
     */
    public BluetoothLeNotificationListener getBluetoothLeNotificationListener();
=======
    // APIs for data notifications
>>>>>>> 71c632e9b191017bc2d6a0b0bf9c1e79552455c1
}
