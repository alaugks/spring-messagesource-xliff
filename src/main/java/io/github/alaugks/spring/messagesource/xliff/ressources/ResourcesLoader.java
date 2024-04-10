package io.github.alaugks.spring.messagesource.xliff.ressources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

;

public final class ResourcesLoader {

    private final Locale defaultLocale;
    private final Set<String> basenames;
    private final List<String> fileExtensions = List.of("xlf", "xliff");

    private ResourcesLoader(ResourcesLoader.Builder builder) {
        this.defaultLocale = builder.defaultLocale;
        this.basenames = builder.basenames;
    }

    public static ResourcesLoader.Builder builder() {
        return new ResourcesLoader.Builder();
    }

    public static final class Builder {

        private Locale defaultLocale;
        private Set<String> basenames;

        public ResourcesLoader.Builder defaultLocale(Locale locale) {
            this.defaultLocale = locale;
            return this;
        }

        public ResourcesLoader.Builder basenamesPattern(Set<String> basenames) {
            this.basenames = basenames;
            return this;
        }

        public ResourcesLoader build() {
            return new ResourcesLoader(this);
        }

    }

    public List<Dto> getTranslationFiles() throws IOException {
        ArrayList<Dto> translationFiles = new ArrayList<>();
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        for (String basename : getBasenameSet()) {
            Resource[] resources = resourceLoader.getResources(basename);
            for (Resource resource : resources) {
                if (this.isFileExtensionSupported(resource)) {
                    Dto dto = this.parseFileName(resource);
                    if(dto != null) {
                        translationFiles.add(dto);
                    }
                }
            }
        }

        return translationFiles;
    }

    private Dto parseFileName(Resource resource) throws IOException {
        ResourcesFileNameParser.Dto dto = new ResourcesFileNameParser(resource.getFilename()).parse();
        if (dto != null) {
            return new Dto(
                    dto.getDomain(),
                    dto.hasLocale()
                            ? dto.getLocale()
                            : this.defaultLocale,
                    resource.getInputStream()
            );
        }
        return null;
    }

    private boolean isFileExtensionSupported(Resource resource) {
        String fileExtension = this.getFileExtension(resource.getFilename());
        return fileExtension != null && this.fileExtensions.contains(fileExtension.toLowerCase());
    }

    public static class Dto {
        private final String domain;
        private final Locale locale;
        private final InputStream inputStream;

        public Dto(String domain, Locale locale, InputStream inputStream) {
            this.domain = domain;
            this.locale = locale;
            this.inputStream = inputStream;
        }

        public String getDomain() {
            return domain;
        }

        public Locale getLocale() {
            return locale;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1)).orElse(null);
    }

    private Set<String> getBasenameSet() {
        return this.basenames;
    }
}
