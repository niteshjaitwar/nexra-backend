package com.nexra.hrms.nexra.modules.crm.support;

import com.nexra.hrms.nexra.common.exception.NexraValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CrmWebhookSignatureCodec {

    public static final String SIGNATURE_ALGORITHM = "NEXRA-HMAC-SHA256";

    private CrmWebhookSignatureCodec() {
    }

    public static String secretHash(final String secret) {
        return sha256Hex(required(secret));
    }

    public static String buildSignature(
        final String secretHash,
        final String payloadJson,
        final String idempotencyKey,
        final String timestamp
    ) {
        return sha256Hex(
            required(secretHash) + "." + required(payloadJson) + "." + required(idempotencyKey) + "." + required(timestamp)
        );
    }

    public static boolean matches(
        final String expectedSignature,
        final String actualSignature
    ) {
        final byte[] left = required(expectedSignature).getBytes(StandardCharsets.UTF_8);
        final byte[] right = required(actualSignature).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    private static String sha256Hex(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", ex);
        }
    }

    private static String required(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }
}
