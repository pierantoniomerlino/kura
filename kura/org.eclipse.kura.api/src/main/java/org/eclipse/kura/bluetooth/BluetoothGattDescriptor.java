package org.eclipse.kura.bluetooth;

import java.util.UUID;

public interface BluetoothGattDescriptor {

    /*
     * Get UUID of this descriptor
     */
    public UUID getUuid();

    /*
     * Read the value of this descriptor
     */
    public byte[] readValue();

    /*
     * Write the value of this descriptor
     */
    public void writeValue(byte[] value);

    /*
     * Get handle of this descriptor
     */
    public String getHandle();

}
