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
package org.eclipse.kura.internal.ble.eddystone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.kura.ble.eddystone.BluetoothLeEddystone;
import org.eclipse.kura.bluetooth.le.beacon.AdvertisingReportRecord;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothAdvertisementData;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconFactory;

public class BluetoothLeEddystoneFactory implements BluetoothLeBeaconFactory<BluetoothLeEddystone> {

    private static final char[] hexChars = "0123456789ABCDEF".toCharArray();

    @Override
    public List<BluetoothLeEddystone> createBeacons(BluetoothAdvertisementData data) {
        List<BluetoothLeEddystone> eddystones = new ArrayList<>();

        for (AdvertisingReportRecord record : data.getReportRecords()) {
            BluetoothLeEddystone beacon = parseEIRData(record.getReportData());
            if (beacon != null) {
                beacon.setAddress(record.getAddress());
                beacon.setRssi(record.getRssi());
                eddystones.add(beacon);
            }
        }
        return eddystones;
    }

    /**
     * Parse EIR data from a BLE advertising report, extracting UUID, major and minor number.
     *
     * See Bluetooth Core 4.0; 8 EXTENDED INQUIRY RESPONSE DATA FORMAT
     *
     * @param b
     *            Array containing EIR data
     * @return BluetoothLeEddystone or null if no beacon data present
     */
    private static BluetoothLeEddystone parseEIRData(byte[] b) {

        int ptr = 0;
        while (ptr < b.length) {

            int structSize = b[ptr];
            if (structSize == 0) {
                break;
            }

            if (b[ptr + 1] == Integer.decode("0x" + BluetoothLeEddystone.UUID_LIST).byteValue()
                    && b[ptr + 2] == Integer.decode("0x" + BluetoothLeEddystone.EDDYSTONE_UUID.substring(2, 4))
                            .byteValue()
                    && b[ptr + 3] == Integer.decode("0x" + BluetoothLeEddystone.EDDYSTONE_UUID.substring(0, 2))
                            .byteValue()) {

                BluetoothLeEddystone eddystone = new BluetoothLeEddystone();
                int txPower = (int) b[ptr + 9];

                EddystoneFrameType frameType = EddystoneFrameType
                        .getFrameTypeCode(BluetoothLeEddystone.toByteHex((int) b[ptr + 8]));
                if (frameType.equals(EddystoneFrameType.UID)) {
                    String namespace = bytesToHexString(Arrays.copyOfRange(b, ptr + 10, ptr + 20));
                    String instance = bytesToHexString(Arrays.copyOfRange(b, ptr + 20, ptr + 26));
                    eddystone.configureEddystoneUIDFrame(namespace, instance, txPower);
                } else if (frameType.equals(EddystoneFrameType.URL)) {
                    String urlHex = bytesToHexString(Arrays.copyOfRange(b, ptr + 10, b.length));
                    eddystone.configureEddystoneURLFrame(decodeURL(urlHex), txPower);
                }

                return eddystone;
            }

            ptr += structSize + 1;
        }

        return null;
    }

    private static String decodeURL(String urlHex) {
        String[] urlHexArray = urlHex.split("(?<=\\G.{2})");
        StringBuilder url = new StringBuilder(EddystoneURLScheme.decodeURLScheme(urlHexArray[0]));
        for (String hex : Arrays.copyOfRange(urlHexArray, 1, urlHexArray.length)) {
            if (Integer.parseInt(hex, 16) <= 13) {
                url.append(EddystoneURLEncoding.decodeURL(hex));
            } else {
                url.append((char) Integer.parseInt(hex, 16));
            }
        }

        return url.toString();
    }

    private static String bytesToHexString(byte[] bytes) {
        char[] hexCharArray = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int hex = bytes[i] & 0xFF;
            hexCharArray[i * 2] = hexChars[hex >>> 4];
            hexCharArray[i * 2 + 1] = hexChars[hex & 0x0F];
        }
        return new String(hexCharArray);
    }

}
