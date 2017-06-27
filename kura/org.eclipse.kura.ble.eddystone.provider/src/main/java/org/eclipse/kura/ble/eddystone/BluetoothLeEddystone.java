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
package org.eclipse.kura.ble.eddystone;

import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeacon;
import org.eclipse.kura.internal.ble.eddystone.EddystoneFrameType;
import org.eclipse.kura.internal.ble.eddystone.EddystoneURLEncoding;
import org.eclipse.kura.internal.ble.eddystone.EddystoneURLScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothLeEddystone extends BluetoothLeBeacon {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeEddystone.class);

    // See https://github.com/google/eddystone/blob/master/protocol-specification.md
    public static final String PKT_BYTES_NUMBER = "1e";
    public static final String PAYLOAD_BYTES_NUMBER = "03";
    public static final String UUID_LIST = "03";
    public static final String EDDYSTONE_UUID = "FEAA";
    public static final String EDDYSTONE_UID_PAYLOAD_LENGTH = "17";
    public static final String SERVICE_DATA = "16";
    public static final Integer URL_MAX_LENGTH = 17;

    // Common fields
    private EddystoneFrameType frameType;
    private Integer txPower;
    private Integer rssi;
    private String address;
    // UID fields
    private String namespace;
    private String instance;
    // URL fields
    private EddystoneURLScheme urlScheme;
    private String url;

    public BluetoothLeEddystone() {
        super();
    }

    public void configureEddystoneUIDFrame(String namespace, String instance, Integer txPower) {
        this.frameType = EddystoneFrameType.UID;
        this.txPower = txPower;
        this.namespace = namespace;
        this.instance = instance;
    }

    public void configureEddystoneURLFrame(String url, Integer txPower) {
        this.frameType = EddystoneFrameType.URL;
        this.txPower = txPower;
        buildURL(url);
    }

    public EddystoneFrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(EddystoneFrameType frameType) {
        this.frameType = frameType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public EddystoneURLScheme getUrlScheme() {
        return urlScheme;
    }

    public void setUrlScheme(EddystoneURLScheme urlScheme) {
        this.urlScheme = urlScheme;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        // Create flags
        String flagsString = encodeFlags();

        // Convert TxPower
        String txPowerString = encodeTxPower();

        // Create Advertising data
        if (this.frameType.equals(EddystoneFrameType.UID)) {
            return encodeUID(flagsString, txPowerString);
        } else if (this.frameType.equals(EddystoneFrameType.URL)) {
            return encodeURL(flagsString, txPowerString);
        } else if (this.frameType.equals(EddystoneFrameType.TLM)) {
            return encodeTLM(flagsString, txPowerString);
        } else if (this.frameType.equals(EddystoneFrameType.EID)) {
            return encodeEID(flagsString, txPowerString);
        } else {
            return "";
        }
    }

    public static String toByteHex(Integer in) {
        String out = Integer.toHexString(in);
        if (out.length() == 1) {
            out = "0" + out;
        }
        return out;
    }

    private String encodeTxPower() {
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
        return txPowerString;
    }

    private String encodeFlags() {
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
        return flagsString;
    }

    private void buildURL(String url) {
        this.urlScheme = EddystoneURLScheme.encodeURLScheme(url);
        this.url = url.substring(this.urlScheme.getLength());
    }

    private String encodeUID(String flagsString, String txPowerString) {
        String data = "";

        data += PKT_BYTES_NUMBER;
        data += AD_BYTES_NUMBER;
        data += AD_FLAG;
        data += flagsString;
        data += PAYLOAD_BYTES_NUMBER;
        data += UUID_LIST;
        data += EDDYSTONE_UUID.substring(2, 4);
        data += EDDYSTONE_UUID.substring(0, 2);
        data += EDDYSTONE_UID_PAYLOAD_LENGTH;
        data += SERVICE_DATA;
        data += EDDYSTONE_UUID.substring(2, 4);
        data += EDDYSTONE_UUID.substring(0, 2);
        data += this.frameType.getCode();
        data += txPowerString;
        data += this.namespace;
        data += this.instance;
        data += "0000";

        return data;
    }

    private String encodeURL(String flagsString, String txPowerString) {
        StringBuilder data = new StringBuilder();
        String hexUrl = EddystoneURLEncoding.encodeURL(this.url);

        if (!this.urlScheme.equals(EddystoneURLScheme.UKNOWN) && hexUrl.length() <= URL_MAX_LENGTH * 2) {

            data.append(toByteHex(14 + hexUrl.length() / 2));
            data.append(AD_BYTES_NUMBER);
            data.append(AD_FLAG);
            data.append(flagsString);
            data.append(PAYLOAD_BYTES_NUMBER);
            data.append(UUID_LIST);
            data.append(EDDYSTONE_UUID.substring(2, 4));
            data.append(EDDYSTONE_UUID.substring(0, 2));
            data.append(toByteHex(6 + hexUrl.length() / 2));
            data.append(SERVICE_DATA);
            data.append(EDDYSTONE_UUID.substring(2, 4));
            data.append(EDDYSTONE_UUID.substring(0, 2));
            data.append(this.frameType.getCode());
            data.append(txPowerString);
            data.append(this.urlScheme.getUrlSchemeCode());
            data.append(hexUrl);
            for (int i = hexUrl.length() / 2; i < URL_MAX_LENGTH; i++) {
                data.append("00");
            }

        } else {
            logger.warn("Invalid Eddystone URL frame or url too long.");
        }
        return data.toString();
    }

    private String encodeTLM(String flagsString, String txPowerString) {
        // Not implemented yet
        return "";
    }

    private String encodeEID(String flagsString, String txPowerString) {
        // Not implemented yet
        return "";
    }

}
