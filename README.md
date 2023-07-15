# XLIFF Translation Support for Spring Boot and Spring

This package provides a **MessageSource** for using translations from XLIFF files. The package support XLIFF versions 1.2, 2.0 and 2.1.

With the implementation and use of the MessageSource interface, the translations are also available in [Thymeleaf](https://www.thymeleaf.org/).

**Table of content**

1. [Version](#a1)
2. [Dependency](#a2)
3. [MessageSource Configuration](#a3)
4. [Minimal CacheManager Configuration](#a4)
5. [CacheManager with Supported Cache Providers](#a4)
6. [Cache warming with an ApplicationRunner (recommended)](#a6)
7. [XLIFF Translation Files](#a7)
8. [Using the MessageSource in Thymeleaf or as Dependency Injection](#a8)
9. [Full Example](#a9)
10. [Support](#a10)
11. [More Information](#a11)

<a name="a1"></a>
## 1. Versions


| Version | Description                                                                               |
|:--------|:------------------------------------------------------------------------------------------|
| 1.1.0   | [Release notes](https://github.com/alaugks/spring-messagesource-xliff/releases/tag/1.1.0) |
| 1.0.0   | First public version                                                                      |


[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-xliff-translation&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-xliff-translation) [![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-xliff.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-xliff/1.1.0)


<a name="a2"></a>
## 2. Dependency

**Maven**
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-xliff</artifactId>
    <version>1.1.0</version>
</dependency>
```

**Gradle**
```text
implementation group: 'io.github.alaugks', name: 'spring-messagesource-xliff', version: '1.1.0'
```


<a name="a3"></a>
## 3. MessageSource Configuration

The class XliffTranslationMessageSource implements the [MessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) interface. An instance of the [CacheManager](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-caching.html#boot-features-caching-provider) is required for caching the translations.

### XliffTranslationMessageSource

`setBasenamePattern(String basename)` or `setBasenamesPattern(Iterable<String> basenames)` (***required***)

* Defines the pattern used to select the XLIFF files.
* The package uses the [PathMatchingResourcePatternResolver](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html) to select the XLIFF files. So you can use the supported patterns.
* Files with the extension `xliff` and `xlf` are filtered from the result list.

`setDefaultLocale(Locale locale)` (***required***)
* Defines the default language.

`setDefaultDomain(String defaultDomain)`
* Defines the default domain. Default is `messages`. For more information, see [XlIFF Translations Files](#a7).

`setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers)`
* The Identifiers can be defined with attributes on a translation unit (`<trans-unit/>` and `<segment/>`). The attribute `id` is required for XLIFF 1.2 and 2.*. For XLIFF 1.2, the attribute `resname` can also be defined.
    * **XLIFF 2.1**:
        * Default definition is `"resname", "id"`.
        * If the attribute `resname` does not exist, the attribute `id` is used to determine the identifier.
        * Documentation identifier: [XLIFF 1.2](http://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers)
    * **XLIFF 2.\***:
        * Default definition is `"id"`.
        * The attribute `id` is optional by standard in XLIFF 2.*. However, this package requires the `id` on a translation unit.
        * Documentation identifier: [XLIFF 2.0](https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment) and [XLIFF 2.1](https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment)


> Please note the [Minimal CacheManager Configuration](#a4).

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


<a name="a4"></a>
## 4. Minimal CacheManager Configuration

You may already have an existing CacheManager configuration. If not, the following minimum CacheManager configuration is required.

The CacheName must be set with the constant `CatalogCache.CACHE_NAME`. The specific cache identifier is stored in the constant.

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


<a name="a5"></a>
## 5. CacheManager with Supported Cache Providers

[Supported Cache Providers](https://docs.spring.io/spring-boot/docs/3.1.1/reference/html/io.html#io.caching.provider) can also be used. The following example using [Caffeine](https://github.com/ben-manes/caffeine):

### CacheConfig with Caffeine

The CacheName must be set with the constant `CatalogCache.CACHE_NAME`. No ExpireDate should be set for the XLIFF translations cache.

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


<a name="a6"></a>
## 6. Cache warming with an ApplicationRunner (recommended)

In the following example, the cache of translations is warmed up after the application starts.

```java
import io.github.alaugks.spring.messagesource.xliff.XliffMessageSourcePatternResolver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

  private final MessageSource messageSource;

  public AppStartupRunner(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (this.messageSource instanceof XliffTranslationMessageSource) {
      ((XliffTranslationMessageSource) this.messageSource).initCache();
    }
  }
    
}
```


<a name="a7"></a>
## 7. XLIFF Translation Files

* Translations can be separated into different files (domains). The default domain is `messages`.
* The default domain can be defined.
* Translation files must be stored in the resource folder and have the extension `xliff` or `xlf`.
* In the XLIFF files, the `<target/>` is retrieved in a `<trans-unit/>` (XLIFF 1.2) or `<segment/>` (XLIFF 2.*).
* For performance reasons, there is no validation of XLIFF files with an XMLSchema. If there is any corrupt XML in an XLIFF file, the SAX parser will throw a [Fatal Error].

### Structure of the Translation Filename

```
# Default language
<domain>.xlf

# Domain + Language
<domain>[-_]<language>.xlf

# Domain + Language + Region
<domain>[-_]<language>[-_]<region>.xlf
```


### Example with Translations Files

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

#### Translations files

Mixing XLIFF versions is possible. Here is an example using XLIFF 1.2 and XLIFF 2.1.

##### messages.xliff
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
            <trans-unit id="headline-examples">
                <source>Examples</source>
                <target>Examples</target>
            </trans-unit>
            <trans-unit id="translation-args-label">
                <source>Translation with param</source>
                <target>Translation with param</target>
            </trans-unit>
            <trans-unit id="email-notice">
                <source>Your email {0} has been registered.</source>
                <target>Your email {0} has been registered.</target>
            </trans-unit>
            <trans-unit id="default-message">
                <source>This is a default message.</source>
                <target>This is a default message.</target>
            </trans-unit>
        </body>
    </file>
</xliff>
```

##### messages_de.xliff
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
            <trans-unit id="headline-examples">
                <source>Examples</source>
                <target>Beispiele</target>
            </trans-unit>
            <trans-unit id="translation-args-label">
                <source>Translation with param</source>
                <target>Übersetzung mit Parameter</target>
            </trans-unit>
            <trans-unit id="email-notice">
                <source>Your email {0} has been registered.</source>
                <target>Ihre E-Mail {0} wurde registriert.</target>
            </trans-unit>
            <trans-unit id="default-message">
                <source>This is a default message.</source>
                <target>Das ist ein Standardtext.</target>
            </trans-unit>
        </body>
    </file>
</xliff>
```

##### messages_en-US.xliff
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

##### payment.xliff
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

##### payment_de.xliff
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

##### payment_en-US.xliff
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

##### Target value

| id                              | en                                  | de                                 | en-US                                 |
|---------------------------------|-------------------------------------|------------------------------------|---------------------------------------|
| headline*                       | Headline                            | Überschrift                        | Headline**                            |
| messages.headline               | Headline                            | Überschrift                        | Headline**                            |
| postcode*                       | Postcode                            | Postleitzahl                       | Zip code                              |
| messages.postcode               | Postcode                            | Postleitzahl                       | Zip code                              |
| headline-examples               | Examples                            | Beispiele                          | Examples**                            |
| messages.headline-examples      | Examples                            | Beispiele                          | Examples**                            |
| translation-args-label          | Translation with param              | Übersetzung mit Parameter          | Translation with param**              |
| messages.translation-args-label | Translation with param              | Übersetzung mit Parameter          | Translation with param**              |
| email-notice                    | Your email {0} has been registered. | Ihre E-Mail {0} wurde registriert. | Your email {0} has been registered.** |
| messages.email-notice           | Your email {0} has been registered. | Ihre E-Mail {0} wurde registriert. | Your email {0} has been registered.** |
| default-message                 | This is a default message.          | Das ist ein Standardtext.          | This is a default message.**          |
| messages.default-message        | This is a default message.          | Das ist ein Standardtext.          | This is a default message.**          |
| payment.headline                | Payment                             | Zahlung                            | Payment                               |
| payment.expiry_date             | Expiry date                         | Ablaufdatum                        | Expiration date                       |

> *Default domain is `messages`.
>
> **Example of a fallback. With locale `en-US` it tries to select the translation with id `headline` in messages_en-US. The id `headline` does not exist, so it tries to select the translation with locale `en` in messages.


<a name="a8"></a>
## 8. Using the MessageSource in Thymeleaf or as Dependency Injection

### Thymeleaf

With the configured MessageSource, the translations are available in Thymeleaf. See the example in the [Full Example](#a9).


```html
<!-- "Headline" -->
<h1 th:text="#{messages.headline}"/>

<!-- "Headline" -->
<h1 th:text="#{headline}"/>

<!-- "Postcode" -->
<label th:text="#{messages.postcode}"/>

<!-- "Postcode" -->
<label th:text="#{postcode}"/>

<!-- "Payment" -->
<h2 th:text="#{payment.headline}"/>

<!-- "Expiry date" -->
<strong th:text="#{payment.expiry_date}"/>

<!-- "Your email john.doe@example.com has been registered." -->
<strong th:text="#{translation-args-label}"/>: <span th:text="#{email-notice('john.doe@example.com')}"/>

<!-- "This is a default message." -->
<span th:text="${#messages.msgOrNull('not-exists-id')} ?: #{default-message}"/>

```

### Using the MessageSource in Thymeleaf or as Dependency Injection

The MessageSource can be set via Autowire to access the translations. See the example in the [Full Example](#a9).

```java
@Controller
public class HomeController {

    private final MessageSource messageSource;

    public HomeController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    @GetMapping(value = "/translations", name = "home_translations")
    public String translations(Model model, Locale locale) {
        LinkedHashMap<String, String> translations = new LinkedHashMap<>();

        // "Headline"
        translations.put("headline", this.messageSource.getMessage("headline", null, locale));
        
        // "Headline"
        translations.put("messages.headline", this.messageSource.getMessage("messages.headline", null, locale));
        
        // "Postcode"
        translations.put("postcode", this.messageSource.getMessage("postcode", null, locale));
        
        // "Postcode"
        translations.put("messages.postcode", this.messageSource.getMessage("messages.postcode", null, locale));
        
        // "Payment"
        translations.put("payment.headline", this.messageSource.getMessage("payment.headline", null, locale));
        
        // "Expiry date"
        translations.put("payment.expiry-date", this.messageSource.getMessage("payment.expiry-date", null, locale));

        // "Your email john.doe@example.com has been registered."
        Object[] args = {"john.doe@example.com"};
        translations.put("email-notice", this.messageSource.getMessage("email-notice", args, locale));
        translations.put("messages.email-notice", this.messageSource.getMessage("email-notice", args, locale));

        // "This is a default message."
        String defaultMessage = this.messageSource.getMessage("default-message", null, locale);
        translations.put("not-exists-id", this.messageSource.getMessage("not-exists-id", null, defaultMessage, locale));

        model.addAttribute("translations", translations);
        return "home/translations";
    }
}
```


<a name="a9"></a>
## 9. Full Example

A Full Example using Spring Boot, mixing XLIFF 1.2 and XLIFF 2.1 translation files: https://github.com/alaugks/spring-messagesource-xliff-example-spring-boot


<a name="a10"></a>
## 10. Support

If you have questions, comments or feature requests please use the [Discussions](https://github.com/alaugks/spring-xliff-translation/discussions) section.


<a name="a11"></a>
## 11. More Information

### MessageSource, Internationalization and Thymeleaf
* [Guide to Internationalization in Spring Boot](https://www.baeldung.com/spring-boot-internationalization)
* [How to Internationalize a Spring Boot Application](https://reflectoring.io/spring-boot-internationalization/)
* [Spring Boot internationalization i18n: Step-by-step with examples](https://lokalise.com/blog/spring-boot-internationalization/)

### Caching
* [A Guide To Caching in Spring](https://www.baeldung.com/spring-cache-tutorial)
* [Implementing a Cache with Spring Boot](https://reflectoring.io/spring-boot-cache/)



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
