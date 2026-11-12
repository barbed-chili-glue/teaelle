package com.teaelle.app;

/**
 * Central definition of the local-assets origin used by the WebView.
 *
 * The query string does two jobs:
 *  - android=1 tells the web app it is inside the native shell, so it does
 *    not register the service worker (the APK assets are the source of truth).
 *  - v=<version> busts the WebView HTTP cache when the APK updates.
 */
public final class AssetPaths {
    public static final String ORIGIN = "https://appassets.androidplatform.net";
    public static final String ASSET_PREFIX = "/assets/www/";
    public static final String START_PATH = ASSET_PREFIX + "index.html";

    private AssetPaths() {
    }

    public static String startUrl() {
        return ORIGIN + START_PATH + "?android=1&v=" + BuildConfig.VERSION_NAME;
    }

    public static boolean isInternalUrl(String url) {
        return url != null && url.startsWith(ORIGIN + ASSET_PREFIX);
    }
}
