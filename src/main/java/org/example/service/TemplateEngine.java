package org.example.service;

import lombok.Getter;
import org.example.dto.exception.ValidationException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.example.service.TemplateEngine.TemplateKey.BY_KEY;

public class TemplateEngine {

    @Getter
    public enum TemplateKey {
        USERNAME("username"),
        BANK_NAME("banking_details_bank_name"),
        ROUTING_NUMBER("banking_details_routing_number"),
        ACCOUNT_NUMBER("banking_details_account_number"),
        PAYMENT_FIRST_NAME("payment_first_name"),
        PAYMENT_LAST_NAME("payment_last_name"),
        PAYMENT_VALUE("payment_value"),
        PAYMENT_STATUS_STATUS("payment_status_status"),
        PAYMENT_STATUS_HISTORY("payment_status_history"),
        PAYMENT_STATUS_UPDATED_AT("payment_status_updated_at");

        private final String key;

        TemplateKey(String key) {
            this.key = key;
        }

        static final Map<String, TemplateKey> BY_KEY = Arrays.stream(values())
                .collect(Collectors.toMap(
                        TemplateKey::getKey,
                        identity(),
                        (existing, replacement) -> {
                            throw new IllegalStateException("duplicate key: " + existing.getKey());
                        },
                        LinkedHashMap::new
                ));
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\[\\[([a-zA-Z_]+)]]");

    public static void validateTemplate(String template) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (!BY_KEY.containsKey(key))
                throw new ValidationException("invalid key in template: " + key + ", allowed keys: " + BY_KEY.keySet());
        }
    }

    public static String render(String template, Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        Matcher m = PLACEHOLDER.matcher(template);
        while (m.find()) {
            String key = m.group(1);
            if (!BY_KEY.containsKey(key)) throw new IllegalArgumentException("invalid key " + key);
            String replacement = values.get(key);
            if (replacement == null) throw new IllegalArgumentException("missing value for key " + key);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
