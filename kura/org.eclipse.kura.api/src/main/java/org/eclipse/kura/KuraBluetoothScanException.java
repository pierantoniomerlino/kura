package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothScanAlreadyStartedException is raised when a notification has been already enabled.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothScanException extends KuraException {

    private static final long serialVersionUID = -1376339369893317371L;

    public KuraBluetoothScanException(Object argument) {
        super(KuraErrorCode.BLE_SCAN_ERROR, null, argument);
    }

    public KuraBluetoothScanException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_SCAN_ERROR, cause, argument);
    }
}
