package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothDisconnectException is raised when an error is detected during the device disconnection.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothResourceNotFoundException extends KuraException {

    private static final long serialVersionUID = -1142491109524317287L;

    public KuraBluetoothResourceNotFoundException(Object argument) {
        super(KuraErrorCode.BLE_RESOURCE_NOT_FOUND, null, argument);
    }

    public KuraBluetoothResourceNotFoundException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_RESOURCE_NOT_FOUND, cause, argument);
    }
}