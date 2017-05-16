package org.eclipse.kura;

import org.osgi.annotation.versioning.ProviderType;

/**
 * KuraBluetoothPairException is raised when an error is detected during the device pairing.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class KuraBluetoothPairException extends KuraException {

    private static final long serialVersionUID = 4156356604467216236L;

    public KuraBluetoothPairException(Object argument) {
        super(KuraErrorCode.BLE_PAIR_ERROR, null, argument);
    }

    public KuraBluetoothPairException(Throwable cause, Object argument) {
        super(KuraErrorCode.BLE_PAIR_ERROR, cause, argument);
    }
}
