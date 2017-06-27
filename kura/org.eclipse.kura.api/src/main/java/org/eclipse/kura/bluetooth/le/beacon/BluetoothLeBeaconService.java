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
package org.eclipse.kura.bluetooth.le.beacon;

import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.osgi.annotation.versioning.ProviderType;

/**
 * BluetoothLeBeaconService provides a mechanism for interfacing with specific Bluetooth LE Beacon devices.
 * It allows to advertise beacon packets and to scan for beacons of the given BluetoothLeBeacon type.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothLeBeaconService<T extends BluetoothLeBeacon> {

    public BluetoothLeBeaconScanner<T> getBeaconScanner(BluetoothLeAdapter adapter,
            BluetoothLeBeaconFactory<T> factory);

    public BluetoothLeBeaconAdvertising<T> getBeaconAdvertising(BluetoothLeAdapter adapter);

}
