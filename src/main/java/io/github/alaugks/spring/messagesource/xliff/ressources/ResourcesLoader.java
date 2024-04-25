package io.github.alaugks.spring.messagesource.xliff.ressources;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.records.Filename;
import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import java.io.IOException;
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
    private final List<String> fileExtensions;


    public ResourcesLoader(Locale defaultLocale, Set<String> basenames, List<String> fileExtensions) {
        this.defaultLocale = defaultLocale;
        this.basenames = basenames;
        this.fileExtensions = fileExtensions;
    }

    public List<TranslationFile> getTranslationFiles() {
        try {
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
        } catch (IOException e) {
            throw new XliffMessageSourceRuntimeException(e);
        }
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
