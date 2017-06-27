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

import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * BluetoothLeBeaconFactory allows to create a list of specific BluetoothLeBeacon objects from a
 * BluetoothAdvertisementData
 *
 */
@FunctionalInterface
@ConsumerType
public interface BluetoothLeBeaconFactory<T extends BluetoothLeBeacon> {

    public List<T> createBeacons(BluetoothAdvertisementData data);
}
