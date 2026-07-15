# Parent MessageSource

A `MessageSource` can delegate to a **parent** `MessageSource`. When a code cannot be resolved in the primary source, the lookup falls back to the parent. This combines the XLIFF translations with another source, for example a `ResourceBundleMessageSource`. You decide which source is asked first.

This works in **either order**:

1. [XLIFF first, other source as fallback](#1-xliff-first-other-source-as-fallback). The XLIFF `MessageSource` is primary and delegates to the other source.
2. [Other source first, XLIFF as fallback](#2-other-source-first-xliff-as-fallback). The other source is primary and delegates to the XLIFF `MessageSource`.

See the main [README](../README.md) for keys, filenames and the full `MessageSource` configuration.

## How resolution works

Spring resolves a code against a source and only asks the parent if the code is **not found**:

```
getMessage(code)
    │
    ▼
primary source ──found──► return message
    │
    │ not found
    ▼
parent source ──found──► return message
    │
    │ not found
    ▼
NoSuchMessageException
```

The source that is asked **first wins** for codes that exist in both. Pick the order based on which set of translations should take precedence.

## Table of Contents

- [1. XLIFF first, other source as fallback](#1-xliff-first-other-source-as-fallback)
- [2. Other source first, XLIFF as fallback](#2-other-source-first-xliff-as-fallback)
- [Which order should I use?](#which-order-should-i-use)

## 1. XLIFF first, other source as fallback

The XLIFF `MessageSource` is the primary source. A code is looked up in the XLIFF translations first; if it is missing, the lookup falls back to the parent (here a `ResourceBundleMessageSource`).

Use `parentMessageSource(...)` on the builder:

```java
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.xliff.XliffResourceMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource parent = new ResourceBundleMessageSource();
        parent.setBasename("messages/messages");
        parent.setDefaultEncoding(StandardCharsets.UTF_8.name());
        parent.setFallbackToSystemLocale(false);

        return XliffResourceMessageSource
            .builder(
                Locale.forLanguageTag("en"),
                new LocationPattern("translations/*")
            )
            .parentMessageSource(parent)
            .build();
    }
}
```

Lookup order: **XLIFF translations → `messages` ResourceBundle**.

## 2. Other source first, XLIFF as fallback

The other `MessageSource` is the primary source. A code is looked up there first; if it is missing, the lookup falls back to the XLIFF translations.

Build the XLIFF `MessageSource` and set it as the parent of the primary source via `setParentMessageSource(...)`:

```java
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.xliff.XliffResourceMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        MessageSource parent = XliffResourceMessageSource
            .builder(
                Locale.forLanguageTag("en"),
                new LocationPattern("translations/*")
            )
            .build();

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setParentMessageSource(parent);

        return messageSource;
    }
}
```

Lookup order: **`messages` ResourceBundle → XLIFF translations**.

## Which order should I use?

| Goal                                                                                              | Order                                                |
|---------------------------------------------------------------------------------------------------|------------------------------------------------------|
| XLIFF holds the translations; the other source only provides a few extra/legacy codes.            | [1. XLIFF first](#1-xliff-first-other-source-as-fallback) |
| An existing `ResourceBundle` setup stays authoritative; XLIFF adds or gradually replaces codes.   | [2. Other source first](#2-other-source-first-xliff-as-fallback) |

> [!NOTE]
> The parent chain is not limited to two sources. A parent can itself have a parent, so several `MessageSource`s can be chained; each level is asked only when the code was not resolved at the level before it.
