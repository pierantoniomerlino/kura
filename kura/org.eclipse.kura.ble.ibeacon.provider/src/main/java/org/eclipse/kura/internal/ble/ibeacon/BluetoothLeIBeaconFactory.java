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
package org.eclipse.kura.internal.ble.ibeacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.kura.ble.ibeacon.BluetoothLeIBeacon;
import org.eclipse.kura.bluetooth.le.beacon.AdvertisingReportRecord;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothAdvertisementData;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconFactory;

public class BluetoothLeIBeaconFactory implements BluetoothLeBeaconFactory<BluetoothLeIBeacon> {

    private static final byte[] IBEACON_PREFIX = {
            Integer.decode("0x" + BluetoothLeIBeacon.COMPANY_CODE.substring(2, 4)).byteValue(),
            Integer.decode("0x" + BluetoothLeIBeacon.COMPANY_CODE.substring(0, 2)).byteValue(),
            Integer.decode("0x" + BluetoothLeIBeacon.BEACON_ID.substring(0, 2)).byteValue(),
            Integer.decode("0x" + BluetoothLeIBeacon.BEACON_ID.substring(2, 4)).byteValue() };

    @Override
    public List<BluetoothLeIBeacon> createBeacons(BluetoothAdvertisementData data) {
        List<BluetoothLeIBeacon> iBeacons = new ArrayList<>();

        for (AdvertisingReportRecord record : data.getReportRecords()) {
            BluetoothLeIBeacon beacon = parseEIRData(record.getReportData());
            if (beacon != null) {
                beacon.setAddress(record.getAddress());
                beacon.setRssi(record.getRssi());
                iBeacons.add(beacon);
            }
        }
        return iBeacons;
    }

    /**
     * Parse EIR data from a BLE advertising report, extracting UUID, major and minor number.
     *
     * See Bluetooth Core 4.0; 8 EXTENDED INQUIRY RESPONSE DATA FORMAT
     *
     * @param b
     *            Array containing EIR data
     * @return BluetoothLeIBeacon or null if no beacon data present
     */
    private static BluetoothLeIBeacon parseEIRData(byte[] b) {

        int ptr = 0;
        while (ptr < b.length) {

            int structSize = b[ptr];
            if (structSize == 0) {
                break;
            }

            byte dataType = b[ptr + 1];

            if (dataType == (byte) 0xFF // Data-Type: Manufacturer-Specific
                    && Arrays.equals(IBEACON_PREFIX, Arrays.copyOfRange(b, ptr + 2, ptr + 2 + IBEACON_PREFIX.length))) {

                BluetoothLeIBeacon beacon = new BluetoothLeIBeacon();

                beacon.setLeLimited((b[ptr - 1] & 0x01) == 0x01);
                beacon.setLeGeneral((b[ptr - 1] & 0x02) == 0x02);
                beacon.setBrEdrSupported((b[ptr - 1] & 0x04) == 0x04);
                beacon.setLeBrController((b[ptr - 1] & 0x08) == 0x08);
                beacon.setLeBrHost((b[ptr - 1] & 0x10) == 0x10);

                int uuidPtr = ptr + 2 + IBEACON_PREFIX.length;
                int majorPtr = uuidPtr + 16;
                int minorPtr = uuidPtr + 18;

                StringBuilder uuid = new StringBuilder();
                for (byte ub : Arrays.copyOfRange(b, uuidPtr, majorPtr)) {
                    uuid.append(String.format("%02X", ub));
                }
                beacon.setUuid(UUID.fromString(uuid.toString().replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5")));

                int majorl = b[majorPtr + 1] & 0xFF;
                int majorh = b[majorPtr] & 0xFF;
                int minorl = b[minorPtr + 1] & 0xFF;
                int minorh = b[minorPtr] & 0xFF;
                beacon.setMajor(majorh << 8 | majorl);
                beacon.setMinor(minorh << 8 | minorl);
                beacon.setTxPower((int) b[minorPtr + 2]);
                return beacon;
            }

            ptr += structSize + 1;
        }

        return null;
    }

}
