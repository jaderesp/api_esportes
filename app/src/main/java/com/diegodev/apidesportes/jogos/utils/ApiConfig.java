package com.diegodev.apidesportes.jogos.utils;

import android.text.TextUtils;

import com.diegodev.apidesportes.BuildConfig;
import com.diegodev.apidesportes.jogos.callback.dja;
import com.diegodev.apidesportes.jogos.callback.na;

import java.net.URI;

public final class ApiConfig {

    private static final String DEFAULT_BASE_URL = "https://api.futebols.com.br/api/";

    private ApiConfig() {
    }

    public static String getBaseUrl() {
        String configured = normalizeBaseUrl(BuildConfig.API_BASE_URL);
        if (!TextUtils.isEmpty(configured)) {
            return configured;
        }

        String nativeEncrypted = na.ae();
        String nativeDecrypted = dja.dpt(nativeEncrypted);
        String nativeBase = normalizeBaseUrl(nativeDecrypted);
        if (!TextUtils.isEmpty(nativeBase)) {
            return nativeBase;
        }

        return DEFAULT_BASE_URL;
    }

    public static boolean validateRequestedUrl(String fullUrl) {
        if (isUsingCustomBaseUrl()) {
            String expectedHost = extractHost(getBaseUrl());
            String currentHost = extractHost(fullUrl);
            return !TextUtils.isEmpty(expectedHost)
                    && !TextUtils.isEmpty(currentHost)
                    && expectedHost.equalsIgnoreCase(currentHost);
        }
        return na.verificarUrlNativa(fullUrl);
    }

    public static boolean isUsingCustomBaseUrl() {
        return !TextUtils.isEmpty(normalizeBaseUrl(BuildConfig.API_BASE_URL));
    }

    private static String normalizeBaseUrl(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return "";
        }
        String trimmed = rawUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return "";
        }
        if (!trimmed.endsWith("/")) {
            trimmed = trimmed + "/";
        }
        return trimmed;
    }

    private static String extractHost(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost();
        } catch (Exception ignored) {
            return "";
        }
    }
}
