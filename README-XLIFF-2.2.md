# XLIFF 2.2 — PGS Module (Plural, Gender and Select)

XLIFF 2.2 introduces the **PGS module** (Plural, Gender and Select). This library reads a PGS-annotated `<unit/>` and turns it into a translation that resolves to the right text depending on a runtime argument: the unit's `pgs:switch` becomes the argument and its `<segment/>`s become the cases.

The key is the resource name (`name` / `id`) as for every other unit. See the main [README](README.md) for keys, filenames and the `MessageSource` configuration.

## Table of Contents

- [Configuration](#configuration)
- [PGS Annotation](#pgs-annotation)
- [Plural](#plural)
  - [Numeric cases](#numeric-cases)
  - [CLDR plural keywords](#cldr-plural-keywords)
- [Gender](#gender)
- [Select](#select)
- [Placeholders](#placeholders)

## Configuration

PGS requires ICU4J. Enable it on the builder:

```java
@Bean
public MessageSource messageSource() {
    return XliffResourceMessageSource
        .builder(
            Locale.forLanguageTag("en"),
            new LocationPattern("translations/*")
        )
        .enableICU4j() // required for PGS
        .build();
}
```

## PGS Annotation

The PGS module is declared via its namespace and applied with two attributes:

| Attribute | On element | Meaning |
|---|---|---|
| `pgs:switch` | `<unit/>` | `type:variable`, the switch type (`plural`, `gender` or `select`) and the argument name. |
| `pgs:case` | `<segment/>` | The case this segment matches (a CLDR keyword, an exact number, or a select value). |

Declare the namespace on the root `<xliff/>` element:

```xml
xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0"
```

## Plural

A `plural:count` switch turns the unit's segments into plural cases for the ICU argument `count`. Each segment's `pgs:case` is either an **exact number**, matched as `=N`, or a **CLDR plural keyword** (`zero`, `one`, `two`, `few`, `many`, `other`) that the locale's plural rules select from the number. A `<ph disp="count"/>` [placeholder](#placeholders) inserts the count into a case's text.

### Numeric cases

A `pgs:case` that is not a CLDR keyword is matched as an exact number. The example combines the exact cases `0` and `1` with the `other` keyword as the fallback for every other number.

```xml
<?xml version="1.0" encoding="utf-8"?>
<xliff version="2.2" srcLang="en" trgLang="de"
       xmlns="urn:oasis:names:tc:xliff:document:2.0"
       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
    <file id="f1">
        <unit id="tu1" name="file_deleted" pgs:switch="plural:count">
            <segment pgs:case="0">
                <source>You deleted no files.</source>
                <target>Sie haben keine Dateien gelöscht.</target>
            </segment>
            <segment pgs:case="1">
                <source>You deleted one file.</source>
                <target>Sie haben eine Datei gelöscht.</target>
            </segment>
            <segment pgs:case="other">
                <source>You deleted <ph id="1" disp="count"/> files.</source>
                <target>Sie haben <ph id="1" disp="count"/> Dateien gelöscht.</target>
            </segment>
        </unit>
    </file>
</xliff>
```

Resolving `file_deleted` for `de` with `count = 1000` yields `Sie haben 1.000 Dateien gelöscht.`

PGS arguments are **named**, so they are passed as a single `Map` (not as positional `{0}` / `{1}` arguments). The catalog detects a lone `Map` argument and formats the pattern with it:

**getMessage()**

```java
messageSource.getMessage(
    "file_deleted",
    new Object[] { Map.of("count", 1000) },
    Locale.forLanguageTag("de")
);
```

**Result:** `Sie haben 1.000 Dateien gelöscht.`

**Thymeleaf**: the same `Map` is passed as the message parameter (the locale comes from the request); use an inline `${ … }` map literal:

```html
<p th:text="#{file_deleted(${ {'count' : 1000} })}"></p>
```

**Result:** `<p>Sie haben 1.000 Dateien gelöscht.</p>`

### CLDR plural keywords

Instead of exact numbers, a `pgs:case` can be a CLDR plural keyword: `zero`, `one`, `two`, `few`, `many`, `other`. The matching case is selected from a number using the **locale's** plural rules. English and German use only `one` (n = 1) and `other`.

Which keywords a language uses, and how each number maps to one, is defined per language in the [Unicode CLDR Language Plural Rules](https://www.unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html). PGS maps to ICU `plural`, so only the **cardinal** rules apply. The ordinal and range rules listed there are not part of the PGS module.

```xml
<unit id="tu1" name="cart_summary" pgs:switch="plural:count">
    <segment pgs:case="one">
        <source>There is one item in your shopping cart, ready for checkout.</source>
        <target>Ein Artikel liegt in Ihrem Warenkorb und ist bereit zur Kasse.</target>
    </segment>
    <segment pgs:case="other">
        <source>There are several items in your shopping cart, ready for checkout.</source>
        <target>Mehrere Artikel liegen in Ihrem Warenkorb und sind bereit zur Kasse.</target>
    </segment>
</unit>
```

Resolving `cart_summary` for `de` with `count = 1` yields `Ein Artikel liegt in Ihrem Warenkorb und ist bereit zur Kasse.`, and with `count = 5` `Mehrere Artikel liegen in Ihrem Warenkorb und sind bereit zur Kasse.`

**getMessage()**

```java
messageSource.getMessage(
    "cart_summary",
    new Object[] { Map.of("count", 1) },
    Locale.forLanguageTag("de")
);
```

**Result:** `Ein Artikel liegt in Ihrem Warenkorb und ist bereit zur Kasse.`

**Thymeleaf**

```html
<p th:text="#{cart_summary(${ {'count' : 1} })}"></p>
```

**Result:** `<p>Ein Artikel liegt in Ihrem Warenkorb und ist bereit zur Kasse.</p>`

<!-- 
### Missing case

A `<segment/>` without a `pgs:case` attribute defaults to `other`.

```xml
<unit id="tu1" name="count" pgs:switch="plural:count">
    <segment pgs:case="one">
        <source>one</source>
        <target>eine</target>
    </segment>
    <segment>
        <source>other</source>
        <target>andere</target>
    </segment>
</unit>
```

Resolving `count` with `count = 1` yields `eine`, any other number `andere`.

**getMessage()**

```java
messageSource.getMessage(
    "count",
    new Object[] { Map.of("count", 1) },
    Locale.forLanguageTag("de")
);
```

**Result:** `eine`


**Thymeleaf**

```html
<p th:text="#{count(${ {'count' : 1} })}"></p>
```

**Result:** `<p>eine</p>`

-->

## Gender

ICU has no gender construct, so a `gender:<variable>` switch is mapped to an ICU `select`: it picks the segment whose `pgs:case` matches the argument value. `gender` is therefore just a PGS-level alias for [`select`](#select). The only difference is the switch-type name.

```xml
<unit id="tu1" name="greeting" pgs:switch="gender:recipient_gender">
    <segment pgs:case="feminine">
        <source>How is she?</source>
        <target>Wie geht es ihr?</target>
    </segment>
    <segment pgs:case="masculine">
        <source>How is he?</source>
        <target>Wie geht es ihm?</target>
    </segment>
    <segment pgs:case="other">
        <source>How are they?</source>
        <target>Wie geht es ihnen?</target>
    </segment>
</unit>
```

Resolving `greeting` with `recipient_gender = "feminine"` yields `Wie geht es ihr?`.

**getMessage()**

```java
messageSource.getMessage(
    "greeting",
    new Object[] { Map.of("recipient_gender", "feminine") },
    Locale.forLanguageTag("de")
);
```

**Result:** `Wie geht es ihr?`

**Thymeleaf**

```html
<p th:text="#{greeting(${ {'recipient_gender' : 'feminine'} })}"></p>
```

**Result:** `<p>Wie geht es ihr?</p>`

## Select

A `select:<variable>` switch maps directly to an ICU `select` and is the general form of [gender](#gender): each `<segment/>`'s `pgs:case` is a value matched against the argument, and a segment without a `pgs:case` defaults to `other`. Use `select` for any value-based choice (gender is the same construct under a more specific name).

## Placeholders

A `<ph disp="..."/>` inline element inside `<source/>` / `<target/>` references the argument named by `disp` within a case body (see the [plural example](#plural), where `<ph id="1" disp="count"/>` inserts the `count` value). A `<ph/>` without a `disp` attribute contributes nothing.
