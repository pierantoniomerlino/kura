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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.kura.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.kura.bluetooth.BluetoothGattService;

public class BluetoothGattServiceImpl implements BluetoothGattService {

    // If we want to implement a GATT server, we should remove the finals... and implement the setters.
    private final UUID uuid;
    private final String startHandle;
    private final String endHandle;
    private final tinyb.BluetoothGattService service;
    private final List<BluetoothGattCharacteristic> characteristics;

    public BluetoothGattServiceImpl(tinyb.BluetoothGattService service) {
        this.service = service;
        this.characteristics = convertBluetoothGattCharacteristics(service.getCharacteristics());
        this.uuid = UUID.fromString(service.getUUID());
        String[] handles = getBoundaryHandles();
        this.startHandle = handles[0];
        this.endHandle = handles[1];
    }

    // --------------------------------------------------------------------
    //
    // BluetoothGattService API
    //
    // --------------------------------------------------------------------

    @Override
    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        BluetoothGattCharacteristic characteristic = null;
        for (BluetoothGattCharacteristic c : this.characteristics) {
            if (c.getUuid().equals(uuid)) {
                characteristic = c;
            }
        }
        return characteristic;
    }

    @Override
    public List<BluetoothGattCharacteristic> getCharacterisitcs() {
        return getCharacteristics();
    }

    @Override
    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return this.characteristics;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getStartHandle() {
        return this.startHandle;
    }

    @Override
    public String getEndHandle() {
        return this.endHandle;
    }

    public tinyb.BluetoothGattService getService() {
        return this.service;
    }

    private List<BluetoothGattCharacteristic> convertBluetoothGattCharacteristics(
            List<tinyb.BluetoothGattCharacteristic> characteristics) {
        List<BluetoothGattCharacteristic> list = new ArrayList<>();
        for (tinyb.BluetoothGattCharacteristic characteristic : characteristics) {
            list.add(new BluetoothGattCharacteristicImpl(characteristic));
        }
        return list;
    }

    private String[] getBoundaryHandles() {
        String[] boundaryHandles = new String[2];
        List<Integer> handles = new ArrayList<>();
        for (BluetoothGattCharacteristic c : this.characteristics) {
            handles.add(Integer.valueOf(c.getHandle(), 16));
        }
<<<<<<< HEAD
        boundaryHandles[0] = String.format("%02x", Integer.toHexString(Collections.min(handles)));
        boundaryHandles[1] = String.format("%02x", Integer.toHexString(Collections.max(handles)));
=======
        boundaryHandles[0] = Integer.toHexString(Collections.min(handles));
        boundaryHandles[1] = Integer.toHexString(Collections.max(handles));
>>>>>>> 71c632e9b191017bc2d6a0b0bf9c1e79552455c1
        return boundaryHandles;
    }
}
