/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.internal.ble.eddystone;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EddystoneURLEncoding {

    private static Map<String, String> urlEncodings;

    private EddystoneURLEncoding() {
        // Not used
    }

    static {
        urlEncodings = new HashMap<>();
        urlEncodings.put("00", String.format("%x", new BigInteger(1, ".com/".getBytes())));
        urlEncodings.put("01", String.format("%x", new BigInteger(1, ".org/".getBytes())));
        urlEncodings.put("02", String.format("%x", new BigInteger(1, ".edu/".getBytes())));
        urlEncodings.put("03", String.format("%x", new BigInteger(1, ".net/".getBytes())));
        urlEncodings.put("04", String.format("%x", new BigInteger(1, ".info/".getBytes())));
        urlEncodings.put("05", String.format("%x", new BigInteger(1, ".biz/".getBytes())));
        urlEncodings.put("06", String.format("%x", new BigInteger(1, ".gov/".getBytes())));
        urlEncodings.put("07", String.format("%x", new BigInteger(1, ".com".getBytes())));
        urlEncodings.put("08", String.format("%x", new BigInteger(1, ".org".getBytes())));
        urlEncodings.put("09", String.format("%x", new BigInteger(1, ".edu".getBytes())));
        urlEncodings.put("0A", String.format("%x", new BigInteger(1, ".net".getBytes())));
        urlEncodings.put("0B", String.format("%x", new BigInteger(1, ".info".getBytes())));
        urlEncodings.put("0C", String.format("%x", new BigInteger(1, ".biz".getBytes())));
        urlEncodings.put("0D", String.format("%x", new BigInteger(1, ".gov".getBytes())));
    }

    public static String encodeURL(String url) {
        String hexUrl = String.format("%x", new BigInteger(1, url.getBytes()));
        for (Entry<String, String> entry : urlEncodings.entrySet()) {
            hexUrl = hexUrl.replaceAll(entry.getValue(), entry.getKey());
        }
        return hexUrl;
    }

    public static String decodeURL(String url) {
        String[] hexUrl = urlEncodings.get(url).split("(?<=\\G.{2})");
        StringBuilder decodedURL = new StringBuilder();
        for (String hex : hexUrl) {
            decodedURL.append((char) Integer.parseInt(hex, 16));
        }
        return decodedURL.toString();
    }

}
