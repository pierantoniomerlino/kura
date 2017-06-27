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
 * BluetoothLeBeaconAdvertising allows to manage the advertising mechanism for Bluetooth LE beacons.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@ProviderType
public interface BluetoothLeBeaconAdvertising<T extends BluetoothLeBeacon> {

    /**
     * Start Beacon advertising.
     *
     */
    public void startBeaconAdvertising() throws KuraException;

    /**
     * Stop Beacon advertising.
     *
     */
    public void stopBeaconAdvertising() throws KuraException;

    /**
     * Set the Beacon advertising interval.
     *
     * @param min
     *            Minimum time interval between advertises
     * @param max
     *            Maximum time interval between advertises
     *
     */
    public void updateBeaconAdvertisingInterval(Integer min, Integer max) throws KuraException;

    /**
     * Set the data in to the Beacon advertising packet.
     *
     * @param beacon
     *            An instance of BluetoothLeBeacon class
     *
     */
    public void updateBeaconAdvertisingData(T beacon) throws KuraException;

}
