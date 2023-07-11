# XLIFF Translation Support for Spring Boot and Spring

This package provides a **MessageSource** for using translations from XLIFF files. The package support XLIFF versions 1.2, 2.0 and 2.1.

**Table of content**

1. [Version](#1-Versions)
2. [Dependency](#2-Dependency)
3. [MessageSource Configuration](#3-MessageSource-Configuration)
4. [Minimal CacheManager Configuration](#4-Minimal-CacheManager-Configuration)
5. [CacheManager with Supported Cache Providers](#5-CacheManager-with-Supported-Cache-Providers)
6. [Cache warming with an ApplicationRunner (recommended)](#6-Cache-warming-with-an-ApplicationRunner-recommended)
7. [Xliff Translations Files](#7-XLIFF-Translation-Files)
8. [Example with Translations Files](#8-Example-with-Translations-Files)
9. [Full Example](#9-Full-Example)
10. [Support](#10-Support)

## 1. Versions

| Version | Description          |
|:------- |:-------------------- |
| 1.0.0   | First public version |


[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-xliff-translation&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-xliff-translation) [![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-xliff.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-xliff/1.0.0)

## 2. Dependency

**Maven**
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-xliff</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**
```text
implementation group: 'io.github.alaugks', name: 'spring-messagesource-xliff', version: '1.0.0'
```

## 3. MessageSource Configuration

The class XliffTranslationMessageSource implements the [MessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) interface. An instance of the [CacheManager](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-caching.html#boot-features-caching-provider) is required for caching the translations.

### XliffTranslationMessageSource

`setBasenamePattern(String basename)` or `setBasenamesPattern(Iterable<String> basenames)` (*mandatory*)

* Defines the pattern used to select the XLIFF files.
* The package uses the [PathMatchingResourcePatternResolver](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html) to select the XLIFF files. So you can use the supported patterns.
* Files with the extension `xliff` and `xlf` are filtered from the result list.

`setDefaultLocale(Locale locale)` (*mandatory*)
* Defines the default language.

`setDefaultDomain(String defaultDomain)`
* Defines the default domain. Default is `messages`. For more information, see [Xliff Translations Files](#7-XLIFF-Translation-Files).

> Please note the [Minimal CacheManager Configuration](#Minimal-CacheManager-configuration).

```java
import de.alaugks.spring.XliffTranslationMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class MessageConfig {
    
    @Bean("messageSource")
    public MessageSource messageSource(CacheManager cacheManager) {
        XliffMessageSourcePatternResolver messageSource =  new XliffTranslationMessageSource(cacheManager);
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.setBasenamePattern("translations/*");
        return messageSource;
    }
    
}
```

## 4. Minimal CacheManager Configuration

You may already have an existing CacheManager configuration. If not, the following minimum CacheManager configuration is required.

The CacheName must be set with the constant `CatalogCache.CACHE_NAME`. The specific cache identifier is stored in the constant. Currently you cannot set a custom cache name.

[ConcurrentMapCacheManager](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/concurrent/ConcurrentMapCacheManager.html) is the default cache in Spring Boot and Spring.

### CacheConfig

```java
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(CatalogCache.CACHE_NAME));
        return cacheManager;
    }
}    
```

## 5. CacheManager with Supported Cache Providers

[Supported Cache Providers](https://docs.spring.io/spring-boot/docs/3.1.1/reference/html/io.html#io.caching.provider) can also be used. The following Example using [Caffeine](https://github.com/ben-manes/caffeine):

### CacheConfig with Caffeine

The CacheName must be set with the constant `CatalogCache.CACHE_NAME`. No ExpireDate should be set for the XLIFF Translations cache.

```java
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        Collection<String> cacheNames = List.of(CatalogCache.CACHE_NAME);
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        caffeineCacheManager.setCacheNames(cacheNames);
        return caffeineCacheManager;
    }
}
```

## 6. Cache warming with an ApplicationRunner (recommended)

In the following example, the cache of translations is warmed up after the application starts.

```java
import io.github.alaugks.spring.messagesource.xliff.XliffMessageSourcePatternResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

    @Autowired
    MessageSource messageSource;

    @Override
    public void run(ApplicationArguments args) {
        if (this.messageSource instanceof XliffTranslationMessageSource) {
            ((XliffTranslationMessageSource) this.messageSource).initCache();
        }
    }
}
```

## 7. XLIFF Translation Files

* Translations can be separated into different files (domains). The default domain is `messages`.
* The default domain can be defined.
* Translation files must be stored in the resource folder and have the extension `xliff` or `xlf`.
* In the XLIFF files, the `<target/>` is fetched in a `<trans-unit/>` (XLIFF 1.2) or `<segment/>` (XLIFF 2.*).
* For performance reasons, there is no validation of XLIFF files with an XMLSchema. If there is any broken XML in an XLIFF file, the SAX parser will throw a [Fatal Error].


### Structure of the Translation Filename

```
# Default language
<domain>.xlf

# Domain + Language
<domain>[-_]<language>.xlf

# Domain + Language + Region
<domain>[-_]<language>[-_]<region>.xlf
```

## 8. Example with Translations Files

* Default domain is `messages`.
* Default locale is `en` without region.
* Translations are provided for the locale `de` (without region) and `en-US`.

```
[resources]
     |-[translations]
             |-messages.xliff           // Default domain and default language
             |-messages_de.xliff
             |-messages_en-US.xliff
             |-payment.xliff            // Default language
             |-payment_de.xliff
             |-payment_en-US.xliff     
```  

### Translations files

Mixing XLIFF versions is possible. Here is an example using XLIFF 1.2 and XLIFF 2.1. 

#### messages.xliff
```xml
<?xml version="1.0" encoding="utf-8"?>
<xliff version="1.2"
       xmlns="urn:oasis:names:tc:xliff:document:1.2">
    <file source-language="en"
          target-language="en">
        <body>
            <trans-unit id="headline">
                <source>Headline</source>
                <target>Headline</target>
            </trans-unit>
            <trans-unit id="postcode">
                <source>Postcode</source>
                <target>Postcode</target>
            </trans-unit>
        </body>
    </file>
</xliff>
```

#### messages_de.xliff
```xml
<?xml version="1.0" encoding="utf-8"?>
<xliff version="1.2"
       xmlns="urn:oasis:names:tc:xliff:document:1.2">
    <file source-language="en"
          target-language="de">
        <body>
            <trans-unit id="headline">
                <source>Headline</source>
                <target>Überschrift</target>
            </trans-unit>
            <trans-unit id="postcode">
                <source>Postcode</source>
                <target>Postleitzahl</target>
            </trans-unit>
        </body>
    </file>
</xliff>
```

#### messages_en-US.xliff
```xml
<?xml version="1.0" encoding="utf-8"?>
<xliff version="1.2"
       xmlns="urn:oasis:names:tc:xliff:document:1.2">
    <file source-language="en"
          target-language="en-US">
        <body>
            <trans-unit id="postcode">
                <source>Postcode</source>
                <target>Zip code</target>
            </trans-unit>
        </body>
    </file>
</xliff>
```

#### payment.xliff
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<xliff xmlns="urn:oasis:names:tc:xliff:document:2.1" version="2.1"
       srcLang="en" trgLang="en">
    <file id="payment">
        <unit>
            <segment id="headline">
                <source>Payment</source>
                <target>Payment</target>
            </segment>
            <segment id="expiry_date">
                <source>Expiry date</source>
                <target>Expiry date</target>
            </segment>
        </unit>
    </file>
</xliff>
```

#### payment_de.xliff
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<xliff xmlns="urn:oasis:names:tc:xliff:document:2.1" version="2.1"
       srcLang="en" trgLang="de">
    <file id="payment_de">
        <unit>
            <segment id="headline">
                <source>Payment</source>
                <target>Zahlung</target>
            </segment>
            <segment id="expiry_date">
                <source>Expiry date</source>
                <target>Ablaufdatum</target>
            </segment>
        </unit>
    </file>
</xliff>
```

#### payment_en-US.xliff
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<xliff xmlns="urn:oasis:names:tc:xliff:document:2.1" version="2.1"
       srcLang="en" trgLang="en-US">
    <file id="payment_en-US">
        <unit>
            <segment id="headline">
                <source>Payment</source>
                <target>Payment</target>
            </segment>
            <segment id="expiry_date">
                <source>Expiry date</source>
                <target>Expiration date</target>
            </segment>
        </unit>
    </file>
</xliff>
```

#### Target value

| id                  | en          | de           | en-US           |
| ------------------- | ----------- | ------------ | --------------- |
| postcode*           | Postcode    | Postleitzahl | Zip code        |
| messages.postcode   | Postcode    | Postleitzahl | Zip code        |
| headline*           | Headline    | Überschrift  | Headline**      |
| messages.headline   | Headline    | Überschrift  | Headline**      |
| payment.headline    | Payment     | Zahlung      | Payment         |
| payment.expiry_date | Expiry date | Ablaufdatum  | Expiration date |

> *Default domain is `messages`.
>
> **Example of a fallback. With locale `en-US` it tries to select the translation with id `headline` in messages_en-US. The id `headline` does not exist, so it tries to select the translation with locale `en` in messages.

<!-- 
## Use @Cachable proxy


```java
import io.github.alaugks.spring.messagesourcece.xliff.XliffCacheableKeyGenerator;
import io.github.alaugks.spring.messagesourcece.xliff.XliffTranslationMessageSource;
import io.github.alaugks.spring.messagesourcece.xliff.catalog.CatalogCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.util.Locale;

class CacheableXliffTranslationMessageSource extends XliffTranslationMessageSource {
    public CacheableXliffTranslationMessageSource(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Nullable
    @Cacheable(
            value = CatalogCache.CACHE_NAME,
            keyGenerator = XliffCacheableKeyGenerator.GENERATOR_NAME,
            condition = "#args.length == 0" // Do not cache with replaced args
    )
    @Override
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        return super.getMessage(code, args, defaultMessage, locale);
    }

    @Nullable
    @Cacheable(
            value = CatalogCache.CACHE_NAME,
            keyGenerator = XliffCacheableKeyGenerator.GENERATOR_NAME,
            condition = "#args.length == 0" // Do not cache with replaced args
    )
    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return super.getMessage(code, args, locale);
    }

    @Cacheable(
            value = CatalogCache.CACHE_NAME,
            keyGenerator = XliffCacheableKeyGenerator.GENERATOR_NAME,
            condition = "#resolvable.getArguments().length == 0" // Do not cache with replaced args
    )
    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return super.getMessage(resolvable, locale);
    }
}
```
-->

## 9. Full Example

A complete example using Spring Boot, mixing XLIFF 1.2 and XLIFF 2.1 translation files: https://github.com/alaugks/spring-messagesource-xliff-example-spring-boot

## 10. Support

If you have questions, comments or feature requests please use the [Discussions](https://github.com/alaugks/spring-xliff-translation/discussions) section.
