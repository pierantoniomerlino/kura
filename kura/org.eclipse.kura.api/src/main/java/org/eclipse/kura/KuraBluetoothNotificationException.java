package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothScanAlreadyStartedException is raised when a scan is started and a previous scan operation hasn't
 * already finished.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothNotificationException extends KuraException {

    private static final long serialVersionUID = -4188172396128459284L;

    public KuraBluetoothNotificationException(Object argument) {
        super(KuraErrorCode.BLE_NOTIFICATION_ERROR, null, argument);
    }

    public KuraBluetoothNotificationException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_NOTIFICATION_ERROR, cause, argument);
    }
}
