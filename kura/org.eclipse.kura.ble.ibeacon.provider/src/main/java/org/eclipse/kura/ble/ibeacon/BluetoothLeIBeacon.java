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
package org.eclipse.kura.ble.ibeacon;

import java.util.UUID;

import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeacon;

public class BluetoothLeIBeacon extends BluetoothLeBeacon {

    public static final String PKT_BYTES_NUMBER = "1e";
    public static final String PAYLOAD_BYTES_NUMBER = "1a";
    public static final String MANUFACTURER_AD = "ff";
    public static final String BEACON_ID = "0215";
    public static final String COMPANY_CODE = "004c";

    private UUID uuid;
    private Integer major;
    private Integer minor;
    private Integer txPower;
    private Integer rssi;
    private String address;

    public BluetoothLeIBeacon() {
        super();
    }

    public BluetoothLeIBeacon(UUID uuid, Integer major, Integer minor, Integer txPower) {
        super();
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public Integer getTxPower() {
        return txPower;
    }

    public void setTxPower(Integer txPower) {
        this.txPower = txPower;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String encode() {
        String data = "";

        // Create flags
        String flags = "000";
        flags += Integer.toString(this.isLeBrHost() ? 1 : 0);
        flags += Integer.toString(this.isLeBrController() ? 1 : 0);
        flags += Integer.toString(this.isBrEdrSupported() ? 1 : 0);
        flags += Integer.toString(this.isLeGeneral() ? 1 : 0);
        flags += Integer.toString(this.isLeLimited() ? 1 : 0);
        String flagsString = Integer.toHexString(Integer.parseInt(flags, 2));
        if (flagsString.length() == 1) {
            flagsString = "0" + flagsString;
        }

        // Convert TxPower
        String txPowerString;
        if (txPower >= 0) {
            txPowerString = Integer.toHexString(txPower);
            if (txPowerString.length() == 1) {
                txPowerString = "0" + txPowerString;
            }
        } else {
            txPowerString = Integer.toHexString(txPower);
            txPowerString = txPowerString.substring(txPowerString.length() - 2, txPowerString.length());
        }

        // Create Advertising data
        data += PKT_BYTES_NUMBER;
        data += AD_BYTES_NUMBER;
        data += AD_FLAG;
        data += flagsString;
        data += PAYLOAD_BYTES_NUMBER;
        data += MANUFACTURER_AD;
        data += COMPANY_CODE.substring(2, 4);
        data += COMPANY_CODE.substring(0, 2);
        data += BEACON_ID;
        data += uuid.toString().replace("-", "");
        data += to2BytesHex(major);
        data += to2BytesHex(minor);
        data += txPowerString;
        data += "00";

        return data;
    }

    private static String to2BytesHex(Integer in) {
        String out = Integer.toHexString(in);
        if (out.length() == 1) {
            out = "000" + out;
        } else if (out.length() == 2) {
            out = "00" + out;
        } else if (out.length() == 3) {
            out = "0" + out;
        }
        return out;
    }
}
