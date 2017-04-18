/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *     Red Hat Inc - minor clean ups
 *******************************************************************************/
package org.eclipse.kura.linux.bluetooth.le;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.BluetoothGatt;
import org.eclipse.kura.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.kura.bluetooth.BluetoothGattSecurityLevel;
import org.eclipse.kura.bluetooth.BluetoothGattService;
import org.eclipse.kura.bluetooth.BluetoothLeNotificationListener;
import org.eclipse.kura.linux.bluetooth.util.BluetoothProcessListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothException;

public class BluetoothGattImpl implements BluetoothGatt, BluetoothProcessListener {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothGattImpl.class);

    // private static final long GATT_CONNECTION_TIMEOUT = 10000;
    // private static final long GATT_SERVICE_TIMEOUT = 6000;
    // private static final long GATT_COMMAND_TIMEOUT = 2000;
    //
    // private static final String[] NOT_CONNECTED = { "[ ]", "disconnected", "not connected", "error: connect" };
    // private static final String[] CONNECTED = { "[con]", "connection successful", "usage: mtu <value>" };
    // private static final String SERVICES = "attr handle:";
    // private static final String CHARACTERISTICS = "handle:";
    // private static final String READ_CHAR = "characteristic value/descriptor:";
    // private static final String REGEX_READ_CHAR_UUID = "handle\\:.*value\\:[\\s|0-9|a-f|A-F]*";
    // private static final String NOTIFICATION = "notification handle";
    // private static final String ERROR_HANDLE = "invalid handle";
    // private static final String[] ERROR_UUID = { "invalid uuid",
    // "read characteristics by uuid failed: attribute can't be read" };

    // private List<BluetoothGattService> m_bluetoothServices; // Serve che sia di classe???
    // private List<BluetoothGattCharacteristic> m_bluetoothGattCharacteristics;
    // private BluetoothLeNotificationListener m_listener;
    // private String m_charValue;
    // private String m_charValueUuid;

    // private BluetoothProcess m_proc;
    // private BufferedWriter m_bufferedWriter;
    // private boolean m_connected = false;
    // private boolean m_ready = false;
    // private StringBuilder m_stringBuilder = null;
    // private final String m_address;
    private final tinyb.BluetoothDevice device;
    private final List<BluetoothGattService> services;

    public BluetoothGattImpl(tinyb.BluetoothDevice device) {
        this.device = device;
        this.services = convertGattServices(device.getServices());
    }

    // --------------------------------------------------------------------
    //
    // BluetoothGatt API
    //
    // --------------------------------------------------------------------
    @Override
    public boolean connect() throws KuraException {
        boolean connected = false;
        try {
            connected = this.device.connect();
        } catch (BluetoothException e) {
            throw KuraException.internalError(e);
        }
        return connected;
    }

    @Override
    public boolean connect(String adapterName) throws KuraException {
        return connect();
    }

    @Override
    public void disconnect() { // Should we throw an exception? How to manage the APIs?
        try {
            this.device.disconnect();
        } catch (BluetoothException e) {
            logger.error("Failed to disconnect from {}.", this.device.getName(), e);
        }
    }

    @Override
    public boolean checkConnection() throws KuraException {
        return this.device.getConnected();
    }

    // we can subscribe on characteristic notifications, not device!
    @Override
    public void setBluetoothLeNotificationListener(BluetoothLeNotificationListener listener) {
        // this.m_listener = listener;
    }

    @Override
    public BluetoothGattService getService(UUID uuid) {
        // The BluetoothManager.find uses the default adapter to find an object
        // return new BluetoothGattServiceImpl(this.device.find(uuid.toString()));
        BluetoothGattService service = null;
        for (BluetoothGattService s : this.services) {
            if (s.getUuid().equals(uuid)) {
                service = s;
                break;
            }
        }
        return service;
    }

    @Override
    public List<BluetoothGattService> getServices() {
        return this.services;
    }

    @Override
    public List<BluetoothGattCharacteristic> getCharacteristics(String startHandle, String endHandle) {
        List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = new ArrayList<>();
        int startHandleInt = Integer.parseInt(startHandle, 16);
        int endHandleInt = Integer.parseInt(endHandle, 16);
        for (BluetoothGattService service : this.services) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (Integer.parseInt(characteristic.getHandle(), 16) >= startHandleInt
                        && Integer.parseInt(characteristic.getHandle(), 16) <= endHandleInt) {
                    bluetoothGattCharacteristics.add(characteristic);
                }
            }
        }

        return bluetoothGattCharacteristics;
    }

    // Non posso farlo
    @Override
    public String readCharacteristicValue(String handle) throws KuraException {
        // if (this.m_proc != null) {
        // this.m_charValue = "";
        // String command = "char-read-hnd " + handle + "\n";
        // sendCmd(command);
        //
        // // Wait until read is complete, error is received or timeout
        // long startTime = System.currentTimeMillis();
        // while ("".equals(this.m_charValue) && !this.m_charValue.startsWith("ERROR")
        // && System.currentTimeMillis() - startTime < GATT_COMMAND_TIMEOUT) {
        // try {
        // Thread.sleep(10);
        // } catch (InterruptedException e) {
        // s_logger.error("Exception waiting for characteristics", e);
        // }
        // }
        // if ("".equals(this.m_charValue)) {
        // throw new KuraTimeoutException("Gatttool read timeout.");
        // }
        // if (this.m_charValue.startsWith("ERROR")) {
        // throw KuraException.internalError("Gatttool read error.");
        // }
        //
        // }

        // return this.m_charValue;
        return "";
    }

    @Override
    public String readCharacteristicValueByUuid(UUID uuid) throws KuraException {
        String charValueUuid = "";
        for (BluetoothGattService service : this.services) {
            if (service.getCharacteristic(uuid) != null) {
                charValueUuid = toHexString(service.getCharacteristic(uuid).readValue());
                break;
            }
        }
        return charValueUuid;
    }

    @Override
    public void writeCharacteristicValue(String handle, String value) {
        // if (this.m_proc != null) {
        // this.m_charValueUuid = null;
        // // String command = "char-write-req " + handle + " " + value + "\n";
        // String command = "char-write-cmd " + handle + " " + value + "\n";
        // sendCmd(command);
        // }
    }

    @Override
    public void writeCharacteristicValueByUuid(UUID uuid, String value) {
        for (BluetoothGattService service : this.services) {
            if (service.getCharacteristic(uuid) != null) {
                service.getCharacteristic(uuid).writeValue(toByteArray(value));
                break;
            }
        }
    }

    @Override
    public void processInputStream(int ch) {
        // if (this.m_stringBuilder == null) {
        // this.m_stringBuilder = new StringBuilder();
        // }
        //
        // // Process stream once newline, carriage return, or > char is received.
        // // '>' indicates the gatttool prompt has returned.
        // if (ch == 0xA || ch == 0xD || ch == 0x1B || (char) ch == '>') {
        // this.m_stringBuilder.append((char) ch);
        // processLine(this.m_stringBuilder.toString());
        // this.m_stringBuilder.setLength(0);
        // } else {
        // this.m_stringBuilder.append((char) ch);
        // }
    }

    @Override
    public void processInputStream(String string) {
    }

    @Override
    public void processErrorStream(String string) {
    }

    @Override
    public BluetoothGattSecurityLevel getSecurityLevel() throws KuraException {

        BluetoothGattSecurityLevel level = BluetoothGattSecurityLevel.UNKNOWN;
        if (this.isConnected && this.proc != null) {
            this.securityLevel = "";
            this.bufferedWriter = this.proc.getWriter();
            logger.info("Get security level...");
            String command = "sec-level\n";
            sendCmd(command);

            // Wait until read is complete, error is received or timeout
            long startTime = System.currentTimeMillis();
            while ("".equals(this.securityLevel) && !this.securityLevel.startsWith("ERROR")
                    && System.currentTimeMillis() - startTime < GATT_COMMAND_TIMEOUT) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.error("Exception waiting for characteristics", e);
                }
            }
            if ("".equals(this.securityLevel)) {
                throw new KuraTimeoutException("Gatttool read timeout.");
            } else if (this.securityLevel.startsWith("ERROR")) {
                throw KuraException.internalError("Gatttool read error.");
            }

            level = BluetoothGattSecurityLevel.getBluetoothGattSecurityLevel(this.securityLevel);
        }

        return level;
    }

    @Override
    public void setSecurityLevel(BluetoothGattSecurityLevel level) {

        if (this.isConnected && this.proc != null) {
            this.bufferedWriter = this.proc.getWriter();
            logger.debug("Set security level to {}", level.toString());
            String command = "sec-level " + level.toString().toLowerCase() + "\n";
            sendCmd(command);
        }

    }

    // --------------------------------------------------------------------
    //
    // Private methods
    //
    // --------------------------------------------------------------------

    // private void sendCmd(String command) {
    // try {
    // logger.debug("send command = {}", command);
    // this.m_bufferedWriter.write(command);
    // this.m_bufferedWriter.flush();
    // } catch (IOException e) {
    // logger.error("Error writing command: " + command, e);
    // }
    // }

    // private void processLine(String line) {
    //
    // logger.debug("Processing line : " + line);
    //
    // // gatttool prompt indicates not connected, but session started
    // if (checkString(line.toLowerCase(), NOT_CONNECTED)) {
    // this.m_connected = false;
    // this.m_ready = false;
    // }
    // // gatttool prompt indicates connected
    // else if (checkString(line.toLowerCase(), CONNECTED)) {
    // this.m_connected = true;
    // this.m_ready = true;
    // }
    // // characteristic read by UUID returned
    // else if (line.matches(REGEX_READ_CHAR_UUID)) {
    // logger.debug("Characteristic value by UUID received: {}", line);
    // // Parse the characteristic line, line is expected to be:
    // // handle: 0xmmmm value: <value>
    // String[] attr = line.split(":");
    // this.m_charValueUuid = attr[2].trim();
    // logger.info("m_charValueUuid: " + this.m_charValueUuid);
    // }
    // // services are being returned
    // else if (line.toLowerCase().startsWith(SERVICES)) {
    // // s_logger.debug("Service : {}", line);
    // // // Parse the services line, line is expected to be:
    // // // attr handle: 0xnnnn, end grp handle: 0xmmmm uuid: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    // // String[] attr = line.split("\\s");
    // // String startHandle = attr[2].substring(0, attr[2].length() - 1);
    // // String endHandle = attr[6];
    // // String uuid = attr[8];
    // //
    // // if (this.m_bluetoothServices != null) {
    // // if (isNewService(uuid)) {
    // // s_logger.debug("Adding new GATT service: " + uuid + ":" + startHandle + ":" + endHandle);
    // // this.m_bluetoothServices.add(new BluetoothGattServiceImpl(uuid, startHandle, endHandle));
    // // }
    // // }
    // }
    // // characteristics are being returned
    // else if (line.toLowerCase().startsWith(CHARACTERISTICS)) {
    // // s_logger.debug("Characteristic : {}", line);
    // // // Parse the characteristic line, line is expected to be:
    // // // handle: 0xnnnn, char properties: 0xmm, char value handle: 0xpppp, uuid:
    // // // xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    // // String[] attr = line.split(" ");
    // // String handle = attr[1].substring(0, attr[1].length() - 1);
    // // String properties = attr[4].substring(0, attr[4].length() - 1);
    // // String valueHandle = attr[8].substring(0, attr[8].length() - 1);
    // // String uuid = attr[10].substring(0, attr[10].length() - 1);
    // // if (this.m_bluetoothGattCharacteristics != null) {
    // // if (isNewGattCharacteristic(uuid)) {
    // // s_logger.debug("Adding new GATT characteristic: {}", uuid);
    // // s_logger.debug(handle + " " + properties + " " + valueHandle);
    // // this.m_bluetoothGattCharacteristics
    // // .add(new BluetoothGattCharacteristicImpl(uuid, handle, properties, valueHandle));
    // // }
    // // }
    // }
    // // characteristic read by handle returned
    // else if (line.toLowerCase().contains(READ_CHAR)) {
    // logger.debug("Characteristic value by handle received: {}", line);
    // // Parse the characteristic line, line is expected to be:
    // // Characteristic value/descriptor: <value>
    // String[] attr = line.split(":");
    // this.m_charValue = attr[1].trim();
    //
    // }
    // // receiving notifications, need to notify listener
    // else if (line.toLowerCase().contains(NOTIFICATION)) {
    // logger.debug("Receiving notification: " + line);
    // // Parse the characteristic line, line is expected to be:
    // // Notification handle = 0xmmmm value: <value>
    // String x = "Notification hanlde = ";
    // String sub = line.substring(x.length()).trim();
    // String[] attr = sub.split(":");
    // String handle = attr[0].split("\\s")[0];
    // String value = attr[1].trim();
    // this.m_listener.onDataReceived(handle, value);
    // }
    // // error reading handle
    // else if (line.toLowerCase().contains(ERROR_HANDLE)) {
    // logger.info("ERROR_HANDLE");
    // this.m_charValue = "ERROR: Invalid handle!";
    // }
    // // error reading UUID
    // else if (checkString(line.toLowerCase(), ERROR_UUID)) {
    // logger.info("ERROR_UUID");
    // this.m_charValueUuid = "ERROR: Invalid UUID!";
    // }
    //
    // }
    //
    // private boolean checkString(String line, String[] lines) {
    //
    // for (String item : lines) {
    // if (line.contains(item)) {
    // return true;
    // }
    // }
    // return false;
    //
    // }

    // private boolean isNewService(String uuid) {
    //
    // for (BluetoothGattService service : this.m_bluetoothServices) {
    // if (service.getUuid().toString().equals(uuid)) {
    // return false;
    // }
    // }
    // return true;
    // }

    // private boolean isNewGattCharacteristic(String uuid) {
    //
    // for (BluetoothGattCharacteristic characteristic : this.m_bluetoothGattCharacteristics) {
    // if (characteristic.getUuid().toString().equals(uuid)) {
    // return false;
    // }
    // }
    // return true;
    //
    // }

    private List<BluetoothGattService> convertGattServices(List<tinyb.BluetoothGattService> services) {
        List<BluetoothGattService> bluetoothServices = new ArrayList<>();
        if (services != null) {
            for (tinyb.BluetoothGattService service : services) {
                bluetoothServices.add(new BluetoothGattServiceImpl(service));
            }
        }
        return bluetoothServices;
    }

    private String toHexString(byte[] hexValue) {
        StringBuilder data = new StringBuilder();
        for (byte b : hexValue) {
            data.append(String.format("%02x", b));
        }
        return data.toString();
    }

    private byte[] toByteArray(String stringValue) {
        byte[] data = new byte[stringValue.length() / 2];
        for (int i = 0; i < stringValue.length(); i += 2) {
            data[i / 2] = (byte) ((Character.digit(stringValue.charAt(i), 16) << 4)
                    + Character.digit(stringValue.charAt(i + 1), 16));
        }
        return data;
    }
}
