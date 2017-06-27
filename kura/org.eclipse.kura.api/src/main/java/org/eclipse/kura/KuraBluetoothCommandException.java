package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothDisconnectException is raised when an error is detected during the device disconnection.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothCommandException extends KuraException {

    private static final long serialVersionUID = -5848254103027432830L;

    public KuraBluetoothCommandException(Object argument) {
        super(KuraErrorCode.BLE_COMMAND_ERROR, null, argument);
    }

    public KuraBluetoothCommandException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_COMMAND_ERROR, cause, argument);
    }
}