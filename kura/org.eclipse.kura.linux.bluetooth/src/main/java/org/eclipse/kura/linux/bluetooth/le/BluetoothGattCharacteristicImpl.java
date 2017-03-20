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
package org.eclipse.kura.linux.bluetooth.le;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.kura.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.kura.bluetooth.BluetoothGattCharacteristicProperties;
import org.eclipse.kura.bluetooth.BluetoothGattDescriptor;
import org.eclipse.kura.bluetooth.BluetoothLeNotificationListener;

import tinyb.BluetoothNotification;

public class BluetoothGattCharacteristicImpl implements BluetoothGattCharacteristic {

    // If we want to implement a GATT server, we should remove the finals... and implement the setters.
    private final UUID uuid;
    private final String handle;
    private final List<BluetoothGattCharacteristicProperties> properties;
    private final String valueHandle;
    private final List<BluetoothGattDescriptor> descriptors;
    private final tinyb.BluetoothGattCharacteristic characteristic;
    private BluetoothLeNotificationListener listener;

    public BluetoothGattCharacteristicImpl(tinyb.BluetoothGattCharacteristic characteristic) {
        this.uuid = UUID.fromString(characteristic.getUUID());
        this.handle = characteristic.getCharacteristicHandle();
        this.properties = new ArrayList<>();
        for (String property : characteristic.getFlags()) {
            this.properties.add(BluetoothGattCharacteristicProperties.getProperty(property));
        }
        this.valueHandle = "0000";
        this.descriptors = convertBluetoothGattDescriptors(characteristic.getDescriptors());
        this.characteristic = characteristic;
    }

    // --------------------------------------------------------------------
    //
    // BluetoothGattCharacteristic API
    //
    // --------------------------------------------------------------------
    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public Object getValue() {
        return this.characteristic.readValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof byte[]) {
            writeValue((byte[]) value);
        }
    }

    @Override
    public byte[] readValue() {
        return this.characteristic.readValue();
    }

    @Override
    public void writeValue(byte[] value) {
        this.characteristic.writeValue(value);
    }

    @Override
    public int getPermissions() {
        return 0;
    }

    @Override
    public String getHandle() {
        return this.handle;
    }

    @Override
    public String getValueHandle() {
        return this.valueHandle;
    }

    @Override
    public int getProperties() {
        return convertProperties(this.properties);
    }

    @Override
    public List<BluetoothGattCharacteristicProperties> getPropertyList() {
        return this.properties;
    }

    @Override
    public List<BluetoothGattDescriptor> getDescriptors() {
        return this.descriptors;
    }

    @Override
    public BluetoothGattDescriptor getDescriptor(UUID uuid) {
        BluetoothGattDescriptor descriptor = null;
        for (BluetoothGattDescriptor d : this.descriptors) {
            if (d.getUuid().equals(uuid)) {
                descriptor = d;
                break;
            }
        }
        return descriptor;
    }

    @Override
    public void setBluetoothLeNotificationListener(BluetoothLeNotificationListener listener) {
        this.listener = listener;
        BluetoothNotification<byte[]> notification = value -> BluetoothGattCharacteristicImpl.this.listener
                .onDataReceived(BluetoothGattCharacteristicImpl.this.handle, toHexString(value));
        this.characteristic.enableValueNotifications(notification);
    }

    @Override
    public void unsetBluetoothLeNotificationListener() {
        this.listener = null;
        this.characteristic.disableValueNotifications();
    }

    @Override
    public BluetoothLeNotificationListener getBluetoothLeNotificationListener() {
        return this.listener;
    }

    public tinyb.BluetoothGattCharacteristic getCharacteristic() {
        return this.characteristic;
    }

    // --------------------------------------------------------------------
    //
    // Private Methods
    //
    // --------------------------------------------------------------------
    private int convertProperties(List<BluetoothGattCharacteristicProperties> properties) {
        int props = 0x00;
        for (BluetoothGattCharacteristicProperties property : properties) {
            props |= property.getCode();
        }
        return props;
    }

    private List<BluetoothGattDescriptor> convertBluetoothGattDescriptors(
            List<tinyb.BluetoothGattDescriptor> descriptors) {
        ArrayList<BluetoothGattDescriptor> list = new ArrayList<>();
        for (tinyb.BluetoothGattDescriptor descriptor : descriptors) {
            list.add(new BluetoothGattDescriptorImpl(descriptor));
        }
        return list;
    }

    private String toHexString(byte[] hexValue) {
        StringBuilder data = new StringBuilder();
        for (byte b : hexValue) {
            data.append(String.format("%02x", b));
        }
        return data.toString();
    }
}
