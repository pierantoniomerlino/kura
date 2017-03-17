package org.eclipse.kura.bluetooth;

public enum BluetoothGattCharacteristicProperties {

    BROADCAST(0x01),
    READ(0x02),
    WRITE_WITHOUT_RESPONSE(0x04),
    WRITE(0x08),
    NOTIFY(0x10),
    INDICATE(0x20),
    AUTHENTICATE_SIGNED_WRITES(0x40),
    EXTENDED_PROPERTIES(0x80);

    private final int code;

    private BluetoothGattCharacteristicProperties(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static int getCode(String property) {
        int prop = 0x00;
        if (property.equalsIgnoreCase(BROADCAST.toString())) {
            prop = BROADCAST.code;
        } else if (property.equalsIgnoreCase(READ.toString())) {
            prop = READ.code;
        } else if (property.equalsIgnoreCase(WRITE_WITHOUT_RESPONSE.toString())) {
            prop = WRITE_WITHOUT_RESPONSE.code;
        } else if (property.equalsIgnoreCase(WRITE.toString())) {
            prop = WRITE.code;
        } else if (property.equalsIgnoreCase(NOTIFY.toString())) {
            prop = NOTIFY.code;
        } else if (property.equalsIgnoreCase(INDICATE.toString())) {
            prop = INDICATE.code;
        } else if (property.equalsIgnoreCase(AUTHENTICATE_SIGNED_WRITES.toString())) {
            prop = AUTHENTICATE_SIGNED_WRITES.code;
        } else if (property.equalsIgnoreCase(EXTENDED_PROPERTIES.toString())) {
            prop = EXTENDED_PROPERTIES.code;
        }

        return prop;
    }

    public static BluetoothGattCharacteristicProperties getProperty(String property) {
        BluetoothGattCharacteristicProperties prop = null;
        if (property.equalsIgnoreCase(BROADCAST.toString())) {
            prop = BROADCAST;
        } else if (property.equalsIgnoreCase(READ.toString())) {
            prop = READ;
        } else if (property.equalsIgnoreCase(WRITE_WITHOUT_RESPONSE.toString())) {
            prop = WRITE_WITHOUT_RESPONSE;
        } else if (property.equalsIgnoreCase(WRITE.toString())) {
            prop = WRITE;
        } else if (property.equalsIgnoreCase(NOTIFY.toString())) {
            prop = NOTIFY;
        } else if (property.equalsIgnoreCase(INDICATE.toString())) {
            prop = INDICATE;
        } else if (property.equalsIgnoreCase(AUTHENTICATE_SIGNED_WRITES.toString())) {
            prop = AUTHENTICATE_SIGNED_WRITES;
        } else if (property.equalsIgnoreCase(EXTENDED_PROPERTIES.toString())) {
            prop = EXTENDED_PROPERTIES;
        }

        return prop;
    }

}
