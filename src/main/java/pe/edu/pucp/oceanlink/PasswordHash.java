package pe.edu.pucp.oceanlink;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHash {
    private PasswordHash() { }

    public static String sha256(String password) {
        if (password == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    public static boolean coincide(String password, String hashGuardado) {
        return hashGuardado != null && MessageDigest.isEqual(
            sha256(password).getBytes(StandardCharsets.US_ASCII),
            hashGuardado.toLowerCase().getBytes(StandardCharsets.US_ASCII)
        );
    }
}
