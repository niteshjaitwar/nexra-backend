package com.nexra.hrms.nexra.modules.auth.service.security;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * RFC 6238 TOTP generator and verifier compatible with standard authenticator apps.
 */
@Service
public class TotpService {

    private static final int SECRET_BYTES = 20;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int CLOCK_SKEW_STEPS = 1;
    private static final char[] BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        final byte[] buffer = new byte[SECRET_BYTES];
        secureRandom.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    public String buildOtpAuthUri(final String issuer, final String accountName, final String secret) {
        final String label = issuer + ":" + accountName;
        return "otpauth://totp/" + urlEncode(label)
            + "?secret=" + secret
            + "&issuer=" + urlEncode(issuer)
            + "&digits=" + CODE_DIGITS
            + "&period=" + TIME_STEP_SECONDS;
    }

    public boolean verify(final String code, final String secret) {
        if (code == null || secret == null || code.isBlank() || secret.isBlank()) {
            return false;
        }
        final String normalized = code.trim();
        if (!normalized.matches("\\d{6}")) {
            return false;
        }
        final long counter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int offset = -CLOCK_SKEW_STEPS; offset <= CLOCK_SKEW_STEPS; offset++) {
            if (normalized.equals(generateCode(secret, counter + offset))) {
                return true;
            }
        }
        return false;
    }

    /** Returns the TOTP code valid at the current time window (for tests and diagnostics). */
    public String currentCode(final String secret) {
        final long counter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        return generateCode(secret, counter);
    }

    private String generateCode(final String secret, final long counter) {
        try {
            final byte[] key = decodeBase32(secret);
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            final byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            final int offset = hash[hash.length - 1] & 0x0F;
            final int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
            final int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            return "";
        }
    }

    private static String encodeBase32(final byte[] data) {
        final StringBuilder encoded = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (final byte value : data) {
            buffer = (buffer << 8) | (value & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                encoded.append(BASE32_ALPHABET[(buffer >> (bitsLeft - 5)) & 0x1F]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            encoded.append(BASE32_ALPHABET[(buffer << (5 - bitsLeft)) & 0x1F]);
        }
        return encoded.toString();
    }

    private static byte[] decodeBase32(final String encoded) {
        final String normalized = encoded.trim().toUpperCase().replace("=", "");
        int buffer = 0;
        int bitsLeft = 0;
        final byte[] output = new byte[normalized.length() * 5 / 8];
        int index = 0;
        for (int i = 0; i < normalized.length(); i++) {
            final char ch = normalized.charAt(i);
            final int value = base32Value(ch);
            if (value < 0) {
                continue;
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        if (index == output.length) {
            return output;
        }
        final byte[] trimmed = new byte[index];
        System.arraycopy(output, 0, trimmed, 0, index);
        return trimmed;
    }

    private static int base32Value(final char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return ch - 'A';
        }
        if (ch >= '2' && ch <= '7') {
            return ch - '2' + 26;
        }
        return -1;
    }

    private static String urlEncode(final String value) {
        return value.replace(" ", "%20");
    }
}
