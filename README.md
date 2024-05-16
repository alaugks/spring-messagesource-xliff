# XLIFF Translation Support for Spring Boot and Spring

This package provides a **MessageSource** for using translations from XLIFF files. The package support XLIFF versions 1.2, 2.0 and 2.1.

**Table of content**

1. [Version](#a1)
2. [Dependency](#a2)
3. [MessageSource Configuration](#a3)
4. [CacheManager Configuration](#a4)
6. [Cache warming with an ApplicationRunner (recommended)](#a5)
7. [XLIFF Translation Files](#a6)
8. [Using the MessageSource](#a7)
9. [Full Example](#a8)
10. [Support](#a9)
11. [More Information](#a10)

<a name="a1"></a>
## 1. Versions

| Version        | Description                                                                               |
|:---------------|:------------------------------------------------------------------------------------------|
| 1.2.1          | [Release notes](https://github.com/alaugks/spring-messagesource-xliff/releases/tag/1.2.1) |
| 1.2.0          | [Release notes](https://github.com/alaugks/spring-messagesource-xliff/releases/tag/1.2.0) |
| 1.1.2          | [Release notes](https://github.com/alaugks/spring-messagesource-xliff/releases/tag/1.1.2) |
| 1.1.1          | [Release notes](https://github.com/alaugks/spring-messagesource-xliff/releases/tag/1.1.1) |
| 1.1.0          | [Release notes](https://github.com/alaugks/spring-messagesource-xliff/releases/tag/1.1.0) |
| 1.0.0          | First public version                                                                      |

### Snapshots
| Version        | Description                                                                               |
|:---------------|:------------------------------------------------------------------------------------------|
| 2.0.0-SNAPSHOT | [SNAPSHOT](https://github.com/alaugks/spring-messagesource-xliff/tree/snapshot/2.0.0)     |                                                                 |


[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-xliff-translation&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-xliff-translation) [![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-xliff.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-xliff/1.2.1)


<a name="a2"></a>
## 2. Dependency

**Maven**
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-xliff</artifactId>
    <version>1.2.1</version>
</dependency>
```

**Gradle**
```text
implementation group: 'io.github.alaugks', name: 'spring-messagesource-xliff', version: '1.2.1'
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
* Defines the default domain. Default is `messages`. For more information, see [XlIFF Translations Files](#a6).

> Please note the [CacheManager Configuration](#a4).

```java
import de.alaugks.spring.XliffTranslationMessageSource;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class MessageConfig {
    
    public MessageSource messageSource(CacheManager cacheManager) {
        XliffTranslationMessageSource messageSource =  new XliffTranslationMessageSource(cacheManager);
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.setBasenamePattern("translations/*");
        return messageSource;
    }
    
}
```


<a name="a4"></a>
## 4. CacheManager Configuration

You may already have an existing CacheManager configuration. If not, the following minimum CacheManager configuration is required. All [Supported Cache Providers](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider) can also be used. Here is an [example using Caffeine](https://github.com/alaugks/spring-messagesource-xliff-example/blob/main/src/main/java/io/github/alaugks/config/CacheConfig.java).
                                                                                                                                   


The CacheName must be set with the constant `CatalogCache.CACHE_NAME`. The specific cache identifier is stored in the constant.

[ConcurrentMapCacheManager](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/concurrent/ConcurrentMapCacheManager.html) is the default cache in Spring Boot and Spring.

### CacheConfig with ConcurrentMapCacheManager

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
        return new ConcurrentMapCacheManager(CatalogCache.CACHE_NAME);
    }
    
}    
```

<a name="a5"></a>
## 5. Cache warming with an ApplicationRunner (recommended)

In the following example, the cache of translations is warmed up after the application starts.

```java
import io.github.alaugks.spring.messagesource.xliff.XliffMessageSourcePatternResolver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageSourceCacheWarmUp implements ApplicationRunner {

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


<a name="a6"></a>
## 6. XLIFF Translation Files

* Translations can be separated into different files (domains). The default domain is `messages`.
* The default domain can be defined.
* Translation files must be stored in the resource folder and have the extension `xliff` or `xlf`.
* In the XLIFF files, the `<target/>` is retrieved in a `<trans-unit/>` (XLIFF 1.2) or `<segment/>` (XLIFF 2.*).
  * **XLIFF 1.2**:
    * If the attribute `resname` does not exist, the attribute `id` is used to determine the identifier.
    * Documentation identifier: [XLIFF 1.2](http://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers)
  * XLIFF 2.*:
    * The attribute `id` is optional by standard in XLIFF 2.*. However, this package requires the `id` on a translation unit.
    * Documentation identifier: [XLIFF 2.0](https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment) and [XLIFF 2.1](https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment)
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
            <segment id="expiry_date">
                <source>Expiry date</source>
                <target>Expiration date</target>
            </segment>
        </unit>
    </file>
</xliff>
```

##### Target value

<table>
  <thead>
  <tr>
    <th>id</th>
    <th>en</th>
    <th>en-US</th>
    <th>de</th>
    <th>jp***</th>
  </tr>
  </thead>
  <tbody>
  <tr>
    <td>headline*<br>messages.headline</td>
    <td>Headline</td>
    <td>Headline**</td>
    <td>Überschrift</td>
    <td>Headline</td>
  </tr>
  <tr>
    <td>postcode*<br>messages.postcode</td>
    <td>Postcode</td>
    <td>Zip code</td>
    <td>Postleitzahl</td>
    <td>Postcode</td>
  </tr>
  <tr>
    <td>email-notice*<br>messages.email-notice</td>
    <td>Your email {0} has been registered.</td>
    <td>Your email {0} has been registered.**</td>
    <td>Ihre E-Mail {0} wurde registriert.</td>
    <td>Your email {0} has been registered.</td>
  </tr>
  <tr>
    <td>default-message*<br>messages.default-message</td>
    <td>This is a default message.</td>
    <td>This is a default message.**</td>
    <td>Das ist ein Standardtext.</td>
    <td>This is a default message.</td>
  </tr>
  <tr>
    <td>payment.headline</td>
    <td>Payment</td>
    <td>Payment**</td>
    <td>Zahlung</td>
    <td>Payment</td>
  </tr>
  <tr>
    <td>payment.expiry_date</td>
    <td>Expiry date</td>
    <td>Expiration date</td>
    <td>Ablaufdatum</td>
    <td>Expiry date</td>
  </tr>
  </tbody>
</table>

> *Default domain is `messages`.
>
> **Example of a fallback from Language_Region (`en-US`) to Language (`en`). The `id` does not exist in `en-US`, so it tries to select the translation with locale `en`.
> 
> ***There is no translation for Japanese (`jp`). The default locale translations (`en`) are selected.


<a name="a7"></a>
## 7. Using the MessageSource

With the implementation and use of the MessageSource interface, the translations are also available in [Thymeleaf](#a7.1), as [Service (Dependency Injection)](#a7.2) and [Custom Validation Messages](#a7.3). Also in packages and implementations that use the MessageSource.

<a name="a7.1"></a>
### Thymeleaf

With the configured MessageSource, the translations are available in Thymeleaf. See the example in the [Full Example](#a8).

```html
<!-- Default domain: messages -->

<!-- "Headline" -->
<h1 th:text="#{headline}"/>
<h1 th:text="#{messages.headline}"/>

<!-- "Postcode" -->
<label th:text="#{postcode}"/>
<label th:text="#{messages.postcode}"/>

<!-- "Your email john.doe@example.com has been registered." -->
<span th:text="#{email-notice('john.doe@example.com')}"/>
<span th:text="#{messages.email-notice('john.doe@example.com')}"/>

<!-- "This is a default message." -->
<span th:text="${#messages.msgOrNull('not-exists-id')} ?: #{default-message}"/>
<span th:text="${#messages.msgOrNull('not-exists-id')} ?: #{messages.default-message}"/>


<!-- Domain: payment -->

<!-- "Payment" -->
<h2 th:text="#{payment.headline}"/>

<!-- "Expiry date" -->
<strong th:text="#{payment.expiry_date}"/>
```

<a name="a7.2"></a>
### Service (Dependency Injection)

The MessageSource can be set via Autowire to access the translations. See the example in the [Full Example](#a8).

```java
import org.springframework.context.MessageSource;

private final MessageSource messageSource;

// Autowire MessageSource
public MyClass(MessageSource messageSource) {
    this.messageSource = messageSource;
}


// Default domain: messages

// "Headline"
this.messageSource.getMessage("headline", null, locale);
this.messageSource.getMessage("messages.headline", null, locale);

// "Postcode"
this.messageSource.getMessage("postcode", null, locale);
this.messageSource.getMessage("messages.postcode", null, locale);

// "Your email john.doe@example.com has been registered."
Object[] args = {"john.doe@example.com"};
this.messageSource.getMessage("email-notice", args, locale);
this.messageSource.getMessage("messages.email-notice", args, locale);

// "This is a default message."
//String defaultMessage = this.messageSource.getMessage("default-message", null, locale);
String defaultMessage = this.messageSource.getMessage("messages.default-message", null, locale);
this.messageSource.getMessage("not-exists-id", null, defaultMessage, locale);


// Domain: payment

// "Payment"
this.messageSource.getMessage("payment.headline", null, locale);

// "Expiry date"
this.messageSource.getMessage("payment.expiry-date", null, locale);
```

<a name="a7.3"></a>
### Custom Validation Messages

The article [Custom Validation MessageSource in Spring Boot](https://www.baeldung.com/spring-custom-validation-message-source) describes how to use custom validation messages.



<a name="a8"></a>
## 8. Full Example

A Full Example using Spring Boot, mixing XLIFF 1.2 and XLIFF 2.1 translation files:

Repository: https://github.com/alaugks/spring-messagesource-xliff-example<br>
Website: https://spring-boot-xliff-example.alaugks.dev


<a name="a9"></a>
## 9. Support

If you have questions, comments or feature requests please use the [Discussions](https://github.com/alaugks/spring-xliff-translation/discussions) section.


<a name="a10"></a>
## 10. More Information

### MessageSource, Internationalization and Thymeleaf
* [Guide to Internationalization in Spring Boot](https://www.baeldung.com/spring-boot-internationalization)
* [How to Internationalize a Spring Boot Application](https://reflectoring.io/spring-boot-internationalization/)
* [Spring Boot internationalization i18n: Step-by-step with examples](https://lokalise.com/blog/spring-boot-internationalization/)

### Caching
* [A Guide To Caching in Spring](https://www.baeldung.com/spring-cache-tutorial)
* [Implementing a Cache with Spring Boot](https://reflectoring.io/spring-boot-cache/)
