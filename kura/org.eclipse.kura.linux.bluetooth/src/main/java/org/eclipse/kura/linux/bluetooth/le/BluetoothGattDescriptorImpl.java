package org.eclipse.kura.linux.bluetooth.le;

import java.util.UUID;

import org.eclipse.kura.bluetooth.BluetoothGattDescriptor;

public class BluetoothGattDescriptorImpl implements BluetoothGattDescriptor {

    // If we want to implement a GATT server, we should remove the finals... and implement the setters.
    private final UUID uuid;
    private final String handle;
    private final tinyb.BluetoothGattDescriptor descriptor;

    public BluetoothGattDescriptorImpl(tinyb.BluetoothGattDescriptor descriptor) {
        this.uuid = UUID.fromString(descriptor.getUUID());
        this.handle = descriptor.getDescriptorHandle();
        this.descriptor = descriptor;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public byte[] readValue() {
        return this.descriptor.readValue();
    }

    @Override
    public void writeValue(byte[] value) {
        this.descriptor.writeValue(value);
    }

    @Override
    public String getHandle() {
        return this.handle;
    }

}
