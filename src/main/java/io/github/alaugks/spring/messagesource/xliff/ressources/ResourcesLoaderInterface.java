package io.github.alaugks.spring.messagesource.xliff.ressources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public interface ResourcesLoaderInterface {
    ArrayList<ResourcesLoader.Dto> getResourcesInputStream() throws IOException;

    ResourcesLoader setDefaultLocale(Locale locale);

    Locale getDefaultLocale();

    ResourcesLoader setBasenamePattern(String basename);

    ResourcesLoader setBasenamesPattern(Iterable<String> basenames);
}
