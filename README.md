# XLIFF MessageSource for Spring

This package provides a [MessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) for using translations from XLIFF files. The package support XLIFF versions 1.2, 2.0 and 2.1.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-xliff-translation&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-xliff-translation)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-xliff.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-xliff/2.0.0)

## Dependency

### Maven
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-xliff</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle 

```text
implementation group: 'io.github.alaugks', name: 'spring-messagesource-xliff', version: '2.0.0'
```


## MessageSource Configuration

`builder(Locale defaultLocale, String locationPattern)` or<br>
`builder(Locale defaultLocale, List<String> locationPatterns)` (***required***)
* Argument `Locale locale`: Defines the default locale.
* Argument `String locationPattern` | `List<String> locationPatterns`:
  * Defines the pattern used to select the XLIFF files.
  * The package uses the [PathMatchingResourcePatternResolver](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html) to select the XLIFF files. So you can use the supported patterns.
  * Files with the extension `xliff` and `xlf` are filtered from the result list.

`defaultDomain(String defaultDomain)`

* Defines the default domain. Default is `messages`. For more information, see [XLIFF Files](#xliff-files).


### Example

* Default locale is `en`.
* The XLIFF files are stored in `src/main/resources/translations`.

```java
import io.github.alaugks.spring.messagesource.xliff.XliffResourceMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
       return XliffResourceMessageSource
               .builder(
                   Locale.forLanguageTag("en"),
                   "translations/*"
               )
               .build();
    }

}
```

## XLIFF Files

* Translations can be separated into different files (domains). The default domain is `messages`.
* The default domain can be defined.
* Translation files must be stored in the resource folder and have the extension `xliff` or `xlf`.
* In the XLIFF files, the `<target/>` is retrieved in a `<trans-unit/>` (XLIFF 1.2) or `<segment/>` (XLIFF 2.*).
  * **XLIFF 1.2**:
    * If the attribute `resname` does not exist, the attribute `id` is used to determine the identifier.
    * Documentation identifier: [XLIFF 1.2](http://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers)
  * **XLIFF 2.&ast;**:
    * The attribute `id` is optional by standard in XLIFF 2.*. However, this package requires the `id` on a translation unit.
    * Documentation identifier: [XLIFF 2.0](https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment) and [XLIFF 2.1](https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment)
* All attributes in the `<file/>` tag are ignored.  
* For performance reasons, there is no validation of XLIFF files with an XMLSchema.
* SAX parser errors are handled by an [ErrorHandler](src/main/java/io/github/alaugks/spring/messagesource/xliff/exception/SaxErrorHandler.java).

### Structure of the Translation Filename

```
# Default language
<domain>.xlf    // <domain>_<language>.xlf also works.

# Domain + Language
<domain>[-_]<language>.xlf

# Domain + Language + Region
<domain>[-_]<language>[-_]<region>.xlf
```

### Example with XLIFF Files

* Default domain is `messages`.
* Default locale is `en` without region.
* Translations are provided for the locale `en`, `de` and `en-US`.

```
[resources]
     |-[translations]
             |-messages.xliff           // Default domain and default language. messages_en.xliff also works.
             |-messages_de.xliff
             |-messages_en-US.xliff
             |-payment.xliff            // Default language. payment_en.xliff also works.
             |-payment_de.xliff
             |-payment_en-US.xliff     
```  

#### XLIFF Files

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

#### Target value

The behaviour of resolving the target value based on the code is equivalent to the ResourceBundleMessageSource or ReloadableResourceBundleMessageSource.

<table>
  <thead>
  <tr>
    <th>id (code)</th>
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

## Full Example

A Full Example using Spring Boot, mixing XLIFF 1.2 and XLIFF 2.1 translation files:

Repository: https://github.com/alaugks/spring-messagesource-xliff-example<br>
Website: https://spring-boot-xliff-example.alaugks.dev

## Support

If you have questions, comments or feature requests please use the [Discussions](https://github.com/alaugks/spring-xliff-translation/discussions) section.

## Related MessageSources 

* [spring-messagesource-db-example](https://github.com/alaugks/spring-messagesource-db-example): Example custom Spring MessageSource from database
* [spring-messagesource-json-example](https://github.com/alaugks/spring-messagesource-json-example): Example custom Spring MessageSource from JSON
