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
package org.eclipse.kura.internal.ble;

import java.util.Arrays;

import org.eclipse.kura.KuraBluetoothCommandException;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.le.BluetoothLeAdapter;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeacon;
import org.eclipse.kura.bluetooth.le.beacon.BluetoothLeBeaconAdvertising;
import org.eclipse.kura.internal.ble.util.BluetoothLeUtil;
import org.eclipse.kura.internal.ble.util.BluetoothProcessListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothLeBeaconAdvertisingImpl<T extends BluetoothLeBeacon>
        implements BluetoothLeBeaconAdvertising<T>, BluetoothProcessListener

{

    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeBeaconAdvertisingImpl.class);

    // See Bluetooth 4.0 Core specifications (https://www.bluetooth.org/docman/handlers/downloaddoc.ashx?doc_id=229737)
    private static final String OGF_CONTROLLER_CMD = "0x08";
    private static final String OCF_ADVERTISING_PARAM_CMD = "0x0006";
    private static final String OCF_ADVERTISING_DATA_CMD = "0x0008";
    private static final String OCF_ADVERTISING_ENABLE_CMD = "0x000a";
    private static final String CMD = "cmd";

    private BluetoothLeAdapter adapter;

    public BluetoothLeBeaconAdvertisingImpl(BluetoothLeAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void startBeaconAdvertising() throws KuraException {
        logger.debug("Start Advertising : hcitool -i " + this.adapter.getInterfaceName() + CMD + " "
                + OGF_CONTROLLER_CMD + " " + OCF_ADVERTISING_ENABLE_CMD + " 01");
        logger.info("Start Advertising on interface " + this.adapter.getInterfaceName());
        String[] cmd = { CMD, OGF_CONTROLLER_CMD, OCF_ADVERTISING_ENABLE_CMD, "01" };
        BluetoothLeUtil.hcitoolCmd(this.adapter.getInterfaceName(), cmd, this);
    }

    @Override
    public void stopBeaconAdvertising() throws KuraException {
        logger.debug("Stop Advertising : hcitool -i " + this.adapter.getInterfaceName() + CMD + " " + OGF_CONTROLLER_CMD
                + " " + OCF_ADVERTISING_ENABLE_CMD + " 00");
        logger.info("Stop Advertising on interface " + this.adapter.getInterfaceName());
        String[] cmd = { CMD, OGF_CONTROLLER_CMD, OCF_ADVERTISING_ENABLE_CMD, "00" };
        BluetoothLeUtil.hcitoolCmd(this.adapter.getInterfaceName(), cmd, this);

    }

    @Override
    public void updateBeaconAdvertisingInterval(Integer min, Integer max) throws KuraException {
        // See
        // http://stackoverflow.com/questions/21124993/is-there-a-way-to-increase-ble-advertisement-frequency-in-bluez
        String[] minHex = toStringArray(to2BytesHex(min));
        String[] maxHex = toStringArray(to2BytesHex(max));

        logger.debug("Set Advertising Parameters : hcitool -i " + this.adapter.getInterfaceName() + CMD + " "
                + OGF_CONTROLLER_CMD + " " + OCF_ADVERTISING_PARAM_CMD + " " + minHex[1] + " " + minHex[0] + " "
                + maxHex[1] + " " + maxHex[0] + " 03 00 00 00 00 00 00 00 00 07 00");
        logger.info("Set Advertising Parameters on interface " + this.adapter.getInterfaceName());
        String[] cmd = { CMD, OGF_CONTROLLER_CMD, OCF_ADVERTISING_PARAM_CMD, minHex[1], minHex[0], maxHex[1], maxHex[0],
                "03", "00", "00", "00", "00", "00", "00", "00", "00", "07", "00" };
        BluetoothLeUtil.hcitoolCmd(this.adapter.getInterfaceName(), cmd, this);
    }

    @Override
    public void updateBeaconAdvertisingData(T beacon) throws KuraException {
        String[] data = toStringArray(beacon.encode());
        String[] cmd = new String[3 + data.length];
        cmd[0] = CMD;
        cmd[1] = OGF_CONTROLLER_CMD;
        cmd[2] = OCF_ADVERTISING_DATA_CMD;
        for (int i = 0; i < data.length; i++) {
            cmd[i + 3] = data[i];
        }

        logger.debug("Set Advertising Data : hcitool -i " + this.adapter.getInterfaceName() + CMD + " "
                + OGF_CONTROLLER_CMD + " " + OCF_ADVERTISING_DATA_CMD + " " + Arrays.toString(data));
        logger.info("Set Advertising Data on interface " + this.adapter.getInterfaceName());
        BluetoothLeUtil.hcitoolCmd(this.adapter.getInterfaceName(), cmd, this);
    }

    @Override
    public void processInputStream(String string) throws KuraException {
        // Check if the command succedeed and return the last line
        logger.debug("Command response : {}", string);
        String[] lines = string.split("\n");
        if (lines[0].toLowerCase().contains("unknown") || lines[1].toLowerCase().contains("usage")) {
            logger.debug("Command failed. Error in command syntax.");
            throw new KuraBluetoothCommandException("Command failed. Error in command syntax.");
        } else {
            String lastLine = lines[lines.length - 1];

            // The last line of hcitool cmd return contains:
            // the numbers of packets sent (1 byte)
            // the opcode (2 bytes)
            // the exit code (1 byte)
            // the returned data if any
            String exitCode = lastLine.substring(11, 13);

            switch (exitCode.toLowerCase()) {
            case "00":
                logger.debug("Command " + lines[0].substring(15, 35) + " Succeeded.");
                break;
            case "01":
                // The Unknown HCI Command error code indicates that the Controller does not understand the HCI Command
                // Packet OpCode that the Host sent.
                logger.debug("Command " + lines[0].substring(15, 35) + " failed. Error: Unknown HCI Command (01)");
                throw new KuraBluetoothCommandException(
                        "Command " + lines[0].substring(15, 35) + " failed. Error: Unknown HCI Command (01)");
            case "03":
                // The Hardware Failure error code indicates to the Host that something in the Controller has failed in
                // a manner that cannot be described with any other error code.
                logger.debug("Command " + lines[0].substring(15, 35) + " failed. Error: Hardware Failure (03)");
                throw new KuraBluetoothCommandException(
                        "Command " + lines[0].substring(15, 35) + " failed. Error: Hardware Failure (03)");
            case "0c":
                // The Command Disallowed error code indicates that the command requested cannot be executed because the
                // Controller is in a state where it cannot process this command at this time.
                logger.debug("Command " + lines[0].substring(15, 35) + " failed. Error: Command Disallowed (0C)");
                break;
            case "11":
                // The Unsupported Feature Or Parameter Value error code indicates that a feature or parameter value
                // in the HCI command is not supported.
                logger.debug("Command " + lines[0].substring(15, 35)
                        + " failed. Error: Unsupported Feature or Parameter Value (11)");
                throw new KuraBluetoothCommandException("Command " + lines[0].substring(15, 35)
                        + " failed. Unsupported Feature or Parameter Value (11)");
            case "12":
                // The Invalid HCI Command Parameters error code indicates that at least one of the HCI command
                // parameters is invalid.
                logger.debug("Command " + lines[0].substring(15, 35)
                        + " failed. Error: Invalid HCI Command Parameters (12)");
                throw new KuraBluetoothCommandException("Command " + lines[0].substring(15, 35)
                        + " failed. Error: Invalid HCI Command Parameters (12)");
            default:
                logger.debug("Command " + lines[0].substring(15, 35) + " failed. Error " + exitCode);
                throw new KuraBluetoothCommandException(
                        "Command " + lines[0].substring(15, 35) + " failed. Error " + exitCode);
            }
        }
    }

    @Override
    public void processInputStream(int ch) throws KuraException {
        // Not used
    }

    @Override
    public void processErrorStream(String string) throws KuraException {
        // Not used
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

    private String[] toStringArray(String string) {
        // Regex to split a string every 2 characters
        return string.split("(?<=\\G..)");
    }

}
