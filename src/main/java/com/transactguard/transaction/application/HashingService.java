package com.transactguard.transaction.application;

import com.transactguard.transaction.api.AuthorizeTransactionRequest;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class HashingService {

    public String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public String fingerprint(AuthorizeTransactionRequest request) {
        String canonicalAmount = request.amount()
                .setScale(2, RoundingMode.UNNECESSARY)
                .toPlainString();

        String canonicalRequest = String.join("|",
                request.accountId(),
                request.cardToken(),
                request.merchantId(),
                request.merchantCategoryCode(),
                request.merchantCountry(),
                request.currency(),
                canonicalAmount
        );
        return sha256(canonicalRequest);
    }
}
