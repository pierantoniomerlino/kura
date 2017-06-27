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

public enum EddystoneURLScheme {

    HTTPWWW("http://www.", "00"),
    HTTPSWWW("https://www.", "01"),
    HTTP("http://", "02"),
    HTTPS("https://", "03"),
    UKNOWN("", "");

    private final String urlScheme;
    private final String urlSchemeCode;
    private final int urlSchemeLength;

    private EddystoneURLScheme(String urlScheme, String urlSchemeCode) {
        this.urlScheme = urlScheme;
        this.urlSchemeCode = urlSchemeCode;
        this.urlSchemeLength = urlScheme.length();
    }

    public String getUrlSchemeCode() {
        return this.urlSchemeCode;
    }

    public String getUrlScheme() {
        return this.urlScheme;
    }

    public int getLength() {
        return this.urlSchemeLength;
    }

    public static EddystoneURLScheme encodeURLScheme(String url) {
        EddystoneURLScheme scheme;
        if (url.startsWith(HTTPWWW.urlScheme)) {
            scheme = HTTPWWW;
        } else if (url.startsWith(HTTPSWWW.urlScheme)) {
            scheme = HTTPSWWW;
        } else if (url.startsWith(HTTP.urlScheme)) {
            scheme = HTTP;
        } else if (url.startsWith(HTTPS.urlScheme)) {
            scheme = HTTPS;
        } else {
            scheme = UKNOWN;
        }
        return scheme;
    }

    public static String decodeURLScheme(String scheme) {
        String prefix;
        if (scheme.equals(HTTPWWW.urlSchemeCode)) {
            prefix = HTTPWWW.urlScheme;
        } else if (scheme.equals(HTTPSWWW.urlSchemeCode)) {
            prefix = HTTPSWWW.urlScheme;
        } else if (scheme.equals(HTTP.urlSchemeCode)) {
            prefix = HTTP.urlScheme;
        } else if (scheme.equals(HTTPS.urlSchemeCode)) {
            prefix = HTTPS.urlScheme;
        } else {
            prefix = UKNOWN.urlScheme;
        }
        return prefix;
    }
}
