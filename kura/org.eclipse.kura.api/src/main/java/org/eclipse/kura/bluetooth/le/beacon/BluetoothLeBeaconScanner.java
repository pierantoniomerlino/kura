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

import org.eclipse.kura.KuraException;
import org.osgi.annotation.versioning.ProviderType;

/**
 * BluetoothLeBeaconScanner allows to manage the scanner mechanism for Bluetooth LE beacons.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothLeBeaconScanner<T extends BluetoothLeBeacon> {

    public void startBeaconScan(long timeout) throws KuraException;

    public void stopBeaconScan() throws KuraException;

    public boolean isScanning();

    public void addBeaconListener(BluetoothLeBeaconListener<T> listener);

    public void removeBeaconListener(BluetoothLeBeaconListener<T> listener);
}
