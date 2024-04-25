package io.github.alaugks.spring.messagesource.xliff.ressources;

import io.github.alaugks.spring.messagesource.xliff.records.Filename;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ResourcesFileNameParser {

    private final String filename;

    public ResourcesFileNameParser(String filename) {
        this.filename = filename;
    }

    public Filename parse() {
        String regexp = "^(?<domain>[a-z0-9]+)(?:([_-](?<language>[a-z]+))(?:[_-](?<region>[a-z]+))?)?";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(this.filename);

        if (matcher.find()) {
            return new Filename(
                this.getGroup(matcher, "domain"),
                this.getGroup(matcher, "language"),
                this.getGroup(matcher, "region")
            );
        }
        return null;
    }

    private String getGroup(Matcher matcher, String groupName) {
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }
    }
}
