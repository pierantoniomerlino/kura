package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothDisconnectException is raised when an error is detected during the device disconnection.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothIOException extends KuraException {

    private static final long serialVersionUID = -2183860317209493405L;

    public KuraBluetoothIOException(Object argument) {
        super(KuraErrorCode.BLE_IO_ERROR, null, argument);
    }

    public KuraBluetoothIOException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_IO_ERROR, cause, argument);
    }
}