# XLIFF MessageSource for Spring

This package provides a [MessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) for using translations from XLIFF files. The package support XLIFF versions 1.2, 2.0, 2.1 and 2.2. 

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-messagesource-xliff&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-messagesource-xliff)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-xliff.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-xliff/3.2.0-SNAPSHOT)

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
    - [XLIFF 2.x — Segments Order](#xliff-2x--segments-order)
    - [XLIFF 2.2 — PGS Module (Plural, Gender and Select)](#xliff-22--pgs-module-plural-gender-and-select)
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
    <version>3.2.0-SNAPSHOT</version>
</dependency>
```

### Gradle 

```text
implementation group: 'io.github.alaugks', name: 'spring-messagesource-xliff', version: '3.2.0-SNAPSHOT'
```


## MessageSource Configuration

| Method | Default | Description |
|---|---|---|
| `builder(Locale defaultLocale, LocationPattern locationPatterns)`| — | Entry point.<br><br>`defaultLocale` is the locale to fall back to when a translation is missing.<br><br>`locationPatterns` selects the XLIFF files (`String` or `List<String>`) via Spring's [PathMatchingResourcePatternResolver](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html), so all its patterns work. Only files ending in `xliff` or `xlf` are kept. |
| `defaultDomain(String defaultDomain)` | `messages` | The default domain; see [XLIFF Files](#xliff-files). |
| `fileExtensions(List<String> fileExtensions)` | `List.of("xlf", "xliff")` | File extensions recognised as XLIFF files. |
| `validateSchema(boolean validateSchema)` | `false` | Validate each file against its OASIS XSD before reading. `validateSchema(true)` rejects non-conforming files (note: strict schemas also reject otherwise-readable files, e.g. XLIFF 1.2 `<trans-unit/>` without the required `id`). |
| `enableICU4j()` | disabled | Format messages with ICU4J instead of the default `java.text.MessageFormat`. The default only understands numeric argument indices (`{0}`, `{1}`); ICU4J additionally supports named arguments and ICU plural/select/gender patterns (e.g. `{count, plural, …}`). |
| `parentMessageSource(MessageSource parentMessageSource)` | — | Sets a parent [`MessageSource`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) to delegate to. When a code cannot be resolved in the XLIFF translations, the lookup falls back to the parent source. |

> [!IMPORTANT]
> The XLIFF 2.2 PGS module generates ICU patterns with named arguments (e.g. `{count, plural, …}`). These cannot be resolved by the default `java.text.MessageFormat` and fail at `getMessage()` time. When using the PGS module you **must** enable ICU4J via `enableICU4j()`.
>
> ICU4J is the [`com.ibm.icu:icu4j`](https://mvnrepository.com/artifact/com.ibm.icu/icu4j) dependency, which is shipped transitively with this library — no extra dependency is required. Its `com.ibm.icu.text.MessageFormat` is a syntax superset of `java.text.MessageFormat`, so existing numeric-index patterns keep working.
>
> Note that the two are not fully output-compatible: ICU4J uses Unicode CLDR locale data, so the formatted result for a given locale can differ from the JDK's — for example the decimal and grouping separators in numbers (`.` vs `,`). Verify locale-sensitive output after enabling ICU4J.

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

* Translations can be split into files by domain (default domain `messages`, configurable via `defaultDomain`).
* Files live in the resource folder with extension `xliff` or `xlf`.
* Supported versions: `1.2`, `2.0`, `2.1` and `2.2`.
* Each file can optionally be validated against its OASIS XSD (1.2 → `xliff-core-1.2-transitional.xsd`, 2.0/2.1 → `xliff-core-2.0.xsd`); off by default, enable with `validateSchema(true)`.
* SAX parser errors are handled by an [ErrorHandler](src/main/java/io/github/alaugks/spring/messagesource/xliff/exception/SaxErrorHandler.java).
* Each unit yields a **key** (message code) and a **value** (translated text). The key is always the resource name (`resname` / `name`) — **never** the `<source/>` text. See [Translation Key](#translation-key) and [Translation Value](#translation-value).

### Translation Key

The key is the application-facing resource name. XLIFF separates the internal identifier (`id`) from the resource name (`resname` / `name`); the resource name is the key, with `id` as fallback when it is absent.

| Version | Element | 1. Key from | 2. Fallback | Not used as key |
|---|---|---|---|---|
| 1.2 | `<trans-unit/>` | `resname` *(optional, resource name)* | `id` *(required, document identifier)* | — |
| 2.x | `<unit/>` | `name` *(optional, resource name)* | `id` *(required, document identifier)* | `segment/@id` |

* **XLIFF 1.2:** `resname` &rarr; `id`. `resname` is the original resource name (e.g. a properties-file key) and is preferred; `id` is required and unique within the `<file/>` but is a tool-internal identifier, used as the key only when `resname` is absent. ([Docs: General Identifiers](http://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers))
* **XLIFF 2.x:** `unit/@name` &rarr; `unit/@id`, analogous to 1.2. `segment/@id` is **never** the key (optional, only unique within its `<unit/>`). ([Docs: 2.0](https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment), [2.1](https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment))
* A unit is **skipped** when neither attribute is set (1.2: no `resname`/`id`; 2.x: no `name`/`id`).

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

**Result:** `greeting` → `Hallo Welt`

#### XLIFF 2.x — Segmentation

A `<unit/>` holds one or more `<segment/>` elements (a single segment is the common case). Multiple segments are reassembled into one string; `<ignorable/>` between them holds non-translatable content, typically whitespace.

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

**Result:** `disclaimer` → `Alle Preise inkl. MwSt. Irrtümer vorbehalten.`

#### XLIFF 2.x — Segments Order

The `order` attribute on `<target/>` defines the order in which target segments are composed. Segments are sorted ascending by their `order` value; `<ignorable/>` elements always keep their document position.

```xml
<unit id="1" name="example">
    <segment>
        <source>First</source>
        <target order="2">Zweites</target>
    </segment>
    <ignorable>
        <source> </source>
    </ignorable>
    <segment>
        <source>Second</source>
        <target order="1">Erstes</target>
    </segment>
</unit>
```

**Result:** `example` → `Erstes Zweites`

#### XLIFF 2.2 — PGS Module (Plural, Gender and Select)

XLIFF 2.2 adds the PGS module, which annotates a `<unit/>` with a `pgs:switch` so its `<segment/>`s become plural, gender or select cases. Such a unit resolves to different text depending on a runtime argument (e.g. a count or a gender). This requires ICU4J via `enableICU4j()` (see [MessageSource Configuration](#messagesource-configuration)).

ℹ️ See [XLIFF 2.2 — PGS Module](README-XLIFF-2.2.md) for the annotation, all switch types and examples.

#### Markup

Applies to XLIFF 1.2 and 2.x. The value is the element's **text content**; embedded markup (e.g. HTML) is kept **verbatim**, as a `CDATA` section or escaped. XLIFF inline elements (`<g/>`, `<pc/>`, `<ph/>`, `<x/>`, …) are **not** interpreted — put display markup into the text as `CDATA` or escaped characters.

Text-wrapping inline elements — most notably the annotation marker `<mrk/>` — are not processed, but their **text is kept**: the tag is dropped, the spanned text remains. E.g. `Hallo <mrk ...>Welt</mrk>!` → `Hallo Welt!`.

```xml
<unit id="1" name="teaser">
    <segment>
        <source><![CDATA[Read <strong>more</strong>]]></source>
        <target><![CDATA[<strong>Mehr</strong> lesen]]></target>
    </segment>
</unit>
```

**Result:** `teaser` → `<strong>Mehr</strong> lesen`

#### Whitespace

Applies to XLIFF 1.2 and 2.x. The value is trimmed by default. Set [`xml:space="preserve"`](https://www.w3.org/TR/xml/#sec-white-space) on the `<source/>` / `<target/>` (or an ancestor) to keep leading and trailing whitespace.

```xml
<unit id="1" name="separator">
    <segment>
        <target xml:space="preserve"> &#183; </target>
    </segment>
</unit>
```

**Result:** `separator` → ` · ` (with the surrounding spaces preserved)

### Unsupported XLIFF Features

This package focuses on **reading and displaying** translations (key → text), not on editing XLIFF with translation tools. Features that only matter for the authoring round-trip are intentionally **not** processed: a document using them still loads, the features are ignored, and only the resolved text is returned.

Not supported, relative to the XLIFF 1.2 and 2.0/2.1 specifications (a `—` means the version has no such concept):

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
| Version | — | XLIFF 2.2 | Not supported (only 2.0 / 2.1). **Planned for 3.2.0**. |

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

XLIFF versions can be mixed. Example using XLIFF 1.2 and 2.1:

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

Resolving a value by code behaves like Spring's `ResourceBundleMessageSource` / `ReloadableResourceBundleMessageSource`.

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
