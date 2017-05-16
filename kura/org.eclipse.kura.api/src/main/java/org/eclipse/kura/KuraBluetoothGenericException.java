package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothDisconnectException is raised when an error is detected during the device disconnection.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothGenericException extends KuraException {

    private static final long serialVersionUID = -8293799213339317642L;

    public KuraBluetoothGenericException(Object argument) {
        super(KuraErrorCode.BLE_GENERIC_ERROR, null, argument);
    }

    public KuraBluetoothGenericException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_GENERIC_ERROR, cause, argument);
    }
}