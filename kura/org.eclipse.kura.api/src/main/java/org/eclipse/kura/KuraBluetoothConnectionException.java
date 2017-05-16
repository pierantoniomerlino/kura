package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothDisconnectException is raised when an error is detected during the device disconnection.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothConnectionException extends KuraException {

    private static final long serialVersionUID = -376745878312274934L;

    public KuraBluetoothConnectionException(Object argument) {
        super(KuraErrorCode.BLE_CONNECTION_ERROR, null, argument);
    }

    public KuraBluetoothConnectionException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_CONNECTION_ERROR, cause, argument);
    }
}