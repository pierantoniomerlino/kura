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

public enum EddystoneFrameType {

    UID("00"),
    URL("10"),
    TLM("20"),
    EID("30"),
    RESERVED("40");

    private final String frameType;

    private EddystoneFrameType(String frameType) {
        this.frameType = frameType;
    }

    public String getCode() {
        return this.frameType;
    }

    public static EddystoneFrameType getFrameTypeCode(String frameType) {
        EddystoneFrameType type = null;
        if (frameType.equalsIgnoreCase(UID.getCode())) {
            type = UID;
        } else if (frameType.equalsIgnoreCase(URL.getCode())) {
            type = URL;
        } else if (frameType.equalsIgnoreCase(TLM.getCode())) {
            type = TLM;
        } else if (frameType.equalsIgnoreCase(EID.getCode())) {
            type = EID;
        } else if (frameType.equalsIgnoreCase(RESERVED.getCode())) {
            type = RESERVED;
        }
        return type;
    }

    public static EddystoneFrameType getFrameType(String frameType) {
        EddystoneFrameType type = null;
        if (frameType.equalsIgnoreCase(UID.toString())) {
            type = UID;
        } else if (frameType.equalsIgnoreCase(URL.toString())) {
            type = URL;
        } else if (frameType.equalsIgnoreCase(TLM.toString())) {
            type = TLM;
        } else if (frameType.equalsIgnoreCase(EID.toString())) {
            type = EID;
        } else if (frameType.equalsIgnoreCase(RESERVED.toString())) {
            type = RESERVED;
        }
        return type;
    }

}
