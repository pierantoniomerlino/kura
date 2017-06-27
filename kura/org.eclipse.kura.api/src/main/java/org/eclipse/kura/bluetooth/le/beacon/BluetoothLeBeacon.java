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

/**
 * BluetoothLeBeacon is a representation of a generic Beacon advertise packet.
 * It offers methods to serialize the Beacon object.
 *
 */
public abstract class BluetoothLeBeacon {

    public static final String AD_BYTES_NUMBER = "02";
    public static final String AD_FLAG = "01";

    private boolean leBrHost;
    private boolean leBrController;
    private boolean brEdrSupported;
    private boolean leGeneral;
    private boolean leLimited;

    public BluetoothLeBeacon() {
        this.leBrHost = true;
        this.leBrController = true;
        this.brEdrSupported = false;
        this.leGeneral = true;
        this.leLimited = false;
    }

    public boolean isLeBrHost() {
        return leBrHost;
    }

    public void setLeBrHost(boolean leBrHost) {
        this.leBrHost = leBrHost;
    }

    public boolean isLeBrController() {
        return leBrController;
    }

    public void setLeBrController(boolean leBrController) {
        this.leBrController = leBrController;
    }

    public boolean isBrEdrSupported() {
        return brEdrSupported;
    }

    public void setBrEdrSupported(boolean brEdrSupported) {
        this.brEdrSupported = brEdrSupported;
    }

    public boolean isLeGeneral() {
        return leGeneral;
    }

    public void setLeGeneral(boolean leGeneral) {
        this.leGeneral = leGeneral;
    }

    public boolean isLeLimited() {
        return leLimited;
    }

    public void setLeLimited(boolean leLimited) {
        this.leLimited = leLimited;
    }

    /**
     * Serialize the Beacon object to a hexadecimal string
     * 
     * @return the serialized hexadecimal string
     */
    public abstract String encode();

}
