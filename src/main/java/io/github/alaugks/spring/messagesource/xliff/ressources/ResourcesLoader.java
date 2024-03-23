package io.github.alaugks.spring.messagesource.xliff.ressources;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class ResourcesLoader {

    private final Locale defaultLocale;
    private final Set<String> basenameSet;
    private final List<String> fileExtensions = List.of("xlf", "xliff");

    private ResourcesLoader(ResourcesLoader.Builder builder) {
        this.defaultLocale = builder.defaultLocale;
        this.basenameSet = builder.basenameSet;
    }

    public static ResourcesLoader.Builder builder() {
        return new ResourcesLoader.Builder();
    }

    public static final class Builder {
        Locale defaultLocale;
        String defaultDomain;
        private final Set<String> basenameSet = new LinkedHashSet<>();

        public ResourcesLoader.Builder setDefaultLocale(Locale locale) {
            this.defaultLocale = locale;
            return this;
        }

        public ResourcesLoader.Builder setDefaultDomain(String defaultDomain) {
            this.defaultDomain = defaultDomain;
            return this;
        }

        public ResourcesLoader.Builder setBasenamePattern(String basename) {
            this.setBasenamesPattern(List.of(basename));
            return this;
        }

        public ResourcesLoader.Builder setBasenamesPattern(Iterable<String> basenames) {
            if (basenames != null) {
                this.basenameSet.clear();
                this.addBasenames(basenames);
            }
            return this;
        }

        private void addBasenames(Iterable<String> basenames) {
            for (String basename : basenames) {
                Assert.hasText(basename, "Basename must not be empty");
                this.basenameSet.add(basename.trim());
            }
        }

        public ResourcesLoader build() {
            return new ResourcesLoader(this);
        }

    }

    public List<Dto> getTranslationFiles() throws IOException {
        if (this.defaultLocale == null || this.defaultLocale.toString().isEmpty()) {
            throw new XliffMessageSourceRuntimeException("Default language is not set or empty.");
        }
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
        return this.basenameSet;
    }
}
