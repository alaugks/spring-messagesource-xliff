package io.github.alaugks.spring.messagesource.xliff.ressources;

import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesFileNameParser.Filename;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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

    public List<TranslationFile> getTranslationFiles() throws IOException {
        ArrayList<TranslationFile> translationTranslationFiles = new ArrayList<>();
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        for (String basename : getBasenameSet()) {
            Resource[] resources = resourceLoader.getResources(basename);
            for (Resource resource : resources) {
                if (this.isFileExtensionSupported(resource)) {
                    TranslationFile translationFile = this.parseFileName(resource);
                    if (translationFile != null) {
                        translationTranslationFiles.add(translationFile);
                    }
                }
            }
        }

        return translationTranslationFiles;
    }

    private TranslationFile parseFileName(Resource resource) throws IOException {
        Filename filename = new ResourcesFileNameParser(resource.getFilename()).parse();
        if (filename != null) {
            return new TranslationFile(
                filename.domain(),
                filename.hasLocale()
                    ? filename.locale()
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

    public record TranslationFile(String domain, Locale locale, InputStream inputStream) {
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(filename.lastIndexOf(".") + 1))
            .orElse(null);
    }

    private Set<String> getBasenameSet() {
        return this.basenames;
    }
}
