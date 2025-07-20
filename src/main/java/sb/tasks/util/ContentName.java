package sb.tasks.util;

import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class ContentName {

    private static final Pattern PATTERN = Pattern.compile("attachment; filename=\"(.*)\"", Pattern.CASE_INSENSITIVE);

    private final String contentDisposition;

    public String get() {
        Matcher m = PATTERN.matcher(contentDisposition);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

}
