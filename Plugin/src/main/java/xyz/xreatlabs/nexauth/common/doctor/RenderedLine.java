package xyz.xreatlabs.nexauth.common.doctor;

import java.util.List;
import java.util.Objects;

public record RenderedLine(String messageKey, List<String> replacements) {

    public RenderedLine {
        Objects.requireNonNull(messageKey, "messageKey");
        Objects.requireNonNull(replacements, "replacements");
    }

    public RenderedLine(String messageKey, String... replacements) {
        this(messageKey, List.of(replacements));
    }
}
