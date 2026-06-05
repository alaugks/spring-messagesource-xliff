# XLIFF MessageSource for Spring

This package provides a [MessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) for using translations from XLIFF files. The package support XLIFF versions 1.2, 2.0 and 2.1.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-messagesource-xliff&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-messagesource-xliff)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-xliff.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-xliff/3.0.0)

## Table of Contents

- [Dependency](#dependency)
  - [Maven](#maven)
  - [Gradle](#gradle)
- [MessageSource Configuration](#messagesource-configuration)
  - [Example](#example)
- [XLIFF Files](#xliff-files)
  - [Translation Key](#translation-key)
  - [Translation Value](#translation-value)
    - [XLIFF 1.2](#xliff-12)
    - [XLIFF 2.x — Segmentation](#xliff-2x--segmentation)
    - [Markup](#markup)
    - [Whitespace](#whitespace)
  - [Unsupported XLIFF Features](#unsupported-xliff-features)
  - [Structure of the Translation Filename](#structure-of-the-translation-filename)
  - [Example with XLIFF Files](#example-with-xliff-files)
    - [XLIFF Files](#xliff-files-1)
    - [Target value](#target-value)
- [Full Example](#full-example)
- [Related MessageSources and Examples](#related-messagesources-and-examples)
- [License](#license)

## Dependency

### Maven
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-xliff</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Gradle 

```text
implementation group: 'io.github.alaugks', name: 'spring-messagesource-xliff', version: '3.0.0'
```


## MessageSource Configuration

* `builder(Locale defaultLocale, LocationPattern locationPatterns)` (***required***)
  * Argument `Locale locale`: Defines the default locale.
  * Argument `LocationPattern locationPatterns`:
    * Defines the pattern used to select the XLIFF files (`String` or `List<String>`).
    * The package uses the [PathMatchingResourcePatternResolver](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html) to select the XLIFF files. So you can use the supported patterns.
    * Files with the extension `xliff` and `xlf` are filtered from the result list.

* `defaultDomain(String defaultDomain)`
  * Defines the default domain.
  * Default is `messages`.
  * For more information, see [XLIFF Files](#xliff-files).

* `fileExtensions(List<String> fileExtensions)`
  * Default is: `List.of("xlf", "xliff")`

* `validateSchema(boolean validateSchema)`
  * Validates each XLIFF file against its OASIS XSD schema before the translation units are read.
  * Default is `false`. Enable it with `validateSchema(true)` to reject non-conforming files; note that strict schemas reject files that are otherwise readable (for example XLIFF 1.2 files whose `<trans-unit/>` omit the schema-required `id` attribute).

### Example

* Default locale is `en`.
* The XLIFF files are stored in `src/main/resources/translations`.

```java
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
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
               new LocationPattern("translations/*")
           )
           .build();
    }

}
```

## XLIFF Files

* Translations can be separated into different files (domains). The default domain is `messages`.
* The default domain can be defined.
* Translation files must be stored in the resource folder and have the extension `xliff` or `xlf`.
* Supported XLIFF versions: `1.2`, `2.0` and `2.1`.
* Optionally, each XLIFF file can be validated against its OASIS XSD schema (XLIFF 1.2 against `xliff-core-1.2-transitional.xsd`, XLIFF 2.0/2.1 against `xliff-core-2.0.xsd`). Validation is disabled by default; enable it with `validateSchema(true)`.
* SAX parser errors are handled by an [ErrorHandler](src/main/java/io/github/alaugks/spring/messagesource/xliff/exception/SaxErrorHandler.java).
* For each translation unit a **key** (the message code) and a **value** (the translated text) are derived. The key is always the resource name attribute (`resname` / `name`) — **never** the `<source/>` text. See [Translation Key](#translation-key) and [Translation Value](#translation-value) below.

### Translation Key

The key is the application-facing resource name. The XLIFF standard separates the internal document identifier (`id`) from the original resource name (`resname` / `name`). The resource name is used as the key and the `id` serves only as fallback when no resource name is set.

| Version | Element | 1. Key from | 2. Fallback | Not used as key |
|---|---|---|---|---|
| **1.2** | `<trans-unit/>` | `resname` *(optional, resource name)* | `id` *(required, document identifier)* | — |
| **2.0 / 2.1** | `<unit/>` | `name` *(optional, resource name)* | `id` *(required, document identifier)* | `segment/@id` |

* **XLIFF 1.2:** `resname` &rarr; `id`. `resname` is the original resource name from the source format (e.g. the key used in a properties file) and is the preferred key. `id` is required and unique within the `<file/>` but serves as a document-internal identifier for tool tracking; it is only used as the key when `resname` is absent.
  * Documentation: [XLIFF 1.2 – General Identifiers](http://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers)
* **XLIFF 2.0 / 2.1:** `unit/@name` &rarr; `unit/@id`. `unit/@name` is the resource name associated with the unit (e.g. the key used in a resource file) and is the preferred key. `unit/@id` is required and unique within the `<file/>` but is a document-internal identifier; it is only used as the key when `unit/@name` is absent.
  * The `segment/@id` is **never** used as the key — it is optional and only unique within its `<unit/>`.
  * Documentation: [XLIFF 2.0](https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment), [XLIFF 2.1](https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment)
* A translation unit is **skipped** when it has neither attribute set (1.2: no `resname` and no `id`; 2.x: no `unit/@name` and no `unit/@id`).

### Translation Value

The value is the `<target/>` text and falls back to the `<source/>` text when no `<target/>` is present. It is the element's **text content** — embedded markup (e.g. HTML as `CDATA` or escaped) is kept verbatim, XLIFF inline elements are not interpreted, and the value is trimmed unless `xml:space="preserve"` is set. See [Markup](#markup) and [Whitespace](#whitespace) (both apply to XLIFF 1.2 and 2.x).

#### XLIFF 1.2

Each `<trans-unit/>` has exactly one `<source/>` and one optional `<target/>`. The value is taken directly.

```xml
<trans-unit id="1" resname="greeting">
    <source>Hello World</source>
    <target>Hallo Welt</target>
</trans-unit>
```

Result: `greeting` → `Hallo Welt`

#### XLIFF 2.x — Segmentation

A `<unit/>` contains one or more `<segment/>` elements (segmentation). A `<unit/>` with a single `<segment/>` is the standard case. When a `<unit/>` contains multiple `<segment/>` elements, the segments are reassembled into a single string for a reading application. Between segments, `<ignorable/>` holds content that is not translatable — typically whitespace.

The reassembly rules are:
* Each `<segment/>` contributes its `<target/>` text, falling back to `<source/>` when no `<target/>` is present.
* Each `<ignorable/>` contributes its `<source/>` verbatim.
* Parts are concatenated in document order.

```xml
<unit id="1" name="disclaimer">
    <segment>
        <source>All prices include VAT.</source>
        <target>Alle Preise inkl. MwSt.</target>
    </segment>
    <ignorable>
        <source> </source>
    </ignorable>
    <segment>
        <source>Errors excepted.</source>
        <target>Irrtümer vorbehalten.</target>
    </segment>
</unit>
```

Result: `disclaimer` → `Alle Preise inkl. MwSt. Irrtümer vorbehalten.`

#### Markup

Applies to both XLIFF 1.2 and 2.x. The value is the **text content** of the `<source/>` / `<target/>` element. Any embedded markup — for example HTML — is taken **verbatim**, whether it is wrapped in a `CDATA` section or escaped. XLIFF inline elements (`<g/>`, `<pc/>`, `<ph/>`, `<x/>`, …) are **not** interpreted; if you need markup or placeholders in the displayed value, put them into the text as `CDATA` or escaped characters.

Inline elements that wrap text — most notably the annotation marker `<mrk/>` (XLIFF 1.2 and 2.x) — are not processed either, but their **text content is kept** as part of the value (the `<mrk/>` tag itself is dropped, the text it spans remains). For example `Hallo <mrk ...>Welt</mrk>!` yields `Hallo Welt!`.

```xml
<unit id="1" name="teaser">
    <segment>
        <source><![CDATA[Read <strong>more</strong>]]></source>
        <target><![CDATA[<strong>Mehr</strong> lesen]]></target>
    </segment>
</unit>
```

Result: `teaser` → `<strong>Mehr</strong> lesen`

#### Whitespace

Applies to both XLIFF 1.2 and 2.x. By default the value is trimmed of leading and trailing whitespace. To keep it, set [`xml:space="preserve"`](https://www.w3.org/TR/xml/#sec-white-space) on the `<source/>` / `<target/>` (or an ancestor); the value is then taken without trimming.

```xml
<unit id="1" name="separator">
    <segment>
        <target xml:space="preserve"> &#183; </target>
    </segment>
</unit>
```

Result: `separator` → `· ` (with the surrounding spaces preserved)

### Unsupported XLIFF Features

This package focuses on **reading and displaying** translations (key → text) — not on editing XLIFF with translation tools. Everything that only matters for the translation/authoring round-trip is therefore intentionally **not** processed. A document that uses these features still loads; the features are simply ignored and only the resolved text value is returned.

Not supported, relative to the XLIFF 1.2 and 2.0/2.1 specifications. A `—` means the version has no such concept.

| Feature | XLIFF 1.2 | XLIFF 2.x | Behavior in 3.0.0 |
|---|---|---|---|
| Inline formatting / code elements | `<g/>`, `<x/>`, `<bx/>`, `<ex/>`, `<bpt/>`, `<ept/>`, `<ph/>`, `<it/>`, `<sub/>` | `<pc/>`, `<ph/>`, `<sc/>`, `<ec/>`, `<cp/>` | Not interpreted. Text-wrapping elements keep their text; standalone placeholders contribute nothing. Use `CDATA` for display markup (see [Markup](#markup)). |
| Placeholder / original-data fallback text | `equiv-text` | `equiv`, `disp`, `<originalData/>` + `dataRef` | Ignored; native code is not reconstructed. |
| Annotation markers | `<mrk/>` (`mtype`, `comment`) | `<mrk/>`, `<sm/>` / `<em/>` | Tag dropped, wrapped text kept (see [Markup](#markup)). |
| Translation state | `state`, `state-qualifier` | segment `state` | `<target/>` is always used, regardless of state. |
| Notes & alternative translations | `<note/>`, `<alt-trans/>` | `<notes/>` | Not exposed. |
| Process metadata | `approved`, `<phase-group/>` / `phase`, `tool` | `tool` / metadata | Ignored. |
| Skeleton / round-trip structure | `<skl/>` / external skeleton | `<skeleton/>` | Not read. |
| Grouping & context | `<group/>`, `restype`, `<context-group/>`, `<count-group/>` | `<group/>` | Structural metadata ignored. |
| Binary content | `<bin-unit/>`, `<bin-source/>`, `<bin-target/>` | — | Not read. |
| XLIFF 2.x modules | — | Translation Candidates, Glossary, Metadata, Resource Data, Size/Length Restriction, Format Style, Validation, Change Tracking | Not processed. |
| Segment ordering | — | `<target order="N"/>` | **Ignored** — document order. _Planned for [3.1.0](/alaugks/spring-messagesource-xliff/tree/snapshot/3.1.0)._ |
| Version | — | XLIFF 2.2 | Not supported (only 2.0 / 2.1). _Planned for [3.1.0](/alaugks/spring-messagesource-xliff/tree/snapshot/3.1.0)._ |

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
    <file original="messages"
          datatype="plaintext"
          source-language="en"
          target-language="en">
        <body>
            <trans-unit id="1" resname="headline">
                <source>Headline</source>
                <target>Headline</target>
            </trans-unit>
            <trans-unit id="2" resname="postcode">
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
    <file original="messages"
          datatype="plaintext"
          source-language="en"
          target-language="de">
        <body>
            <trans-unit id="1" resname="headline">
                <source>Headline</source>
                <target>Überschrift</target>
            </trans-unit>
            <trans-unit id="2" resname="postcode">
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
    <file original="messages"
          datatype="plaintext"
          source-language="en"
          target-language="en-US">
        <body>
            <trans-unit id="2" resname="postcode">
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
<xliff xmlns="urn:oasis:names:tc:xliff:document:2.0"
       version="2.1"
       srcLang="en"
       trgLang="en">
    <file id="payment">
        <unit id="1" name="headline">
            <segment>
                <source>Payment</source>
                <target>Payment</target>
            </segment>
        </unit>
        <unit id="2" name="expiry_date">
            <segment>
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
<xliff xmlns="urn:oasis:names:tc:xliff:document:2.0"
       version="2.1"
       srcLang="en"
       trgLang="de">
    <file id="payment_de">
        <unit id="1" name="headline">
            <segment>
                <source>Payment</source>
                <target>Zahlung</target>
            </segment>
        </unit>
        <unit id="2" name="expiry_date">
            <segment>
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
<xliff xmlns="urn:oasis:names:tc:xliff:document:2.0"
       version="2.1"
       srcLang="en"
       trgLang="en-US">
    <file id="payment_en-US">
        <unit id="2" name="expiry_date">
            <segment>
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

## Related MessageSources and Examples  
  
* [XLIFF MessageSource for Spring](https://github.com/alaugks/spring-messagesource-xliff)  
* [JSON MessageSource for Spring](https://github.com/alaugks/spring-messagesource-json)
* [Example: XLIFF MessageSource for Spring](https://github.com/alaugks/spring-messagesource-xliff-example)    
* [Example: JSON MessageSource for Spring](https://github.com/alaugks/spring-messagesource-json-example)  
* [Example: Custom Database Spring MessageSource](https://github.com/alaugks/spring-messagesource-db-example)

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
