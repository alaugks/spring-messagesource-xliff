# XLIFF Inline Elements

XLIFF marks up placeholders, formatting codes and annotations inside `<source/>` and `<target/>` with **inline elements**. This library reconstructs them as **plain text** when it reads a message, so the value that ends up in the `MessageSource` contains the original code (e.g. `{0}` or `<a href="/x">`) instead of the XLIFF markup around it.

The rules are identical for XLIFF 1.2 and 2.x. See the main [README](../README.md) for keys, filenames and the `MessageSource` configuration.

## Table of Contents

- [Rules](#rules)
- [Behavior Change](#behavior-change)
- [XLIFF 1.2](#xliff-12)
  - [`<x/>`](#x)
  - [`<bx/>` and `<ex/>`](#bx-and-ex)
  - [`<g/>`](#g)
  - [`<ph>` with native content](#ph-with-native-content)
  - [`<bpt>` and `<ept>`](#bpt-and-ept)
  - [`<mrk/>`](#mrk)
- [XLIFF 2.x](#xliff-2x)
  - [`<ph/>` with original data](#ph-with-original-data)
  - [`<ph/>` with `equiv` fallback](#ph-with-equiv-fallback)
  - [`<pc/>` with original data](#pc-with-original-data)
  - [`<pc/>` with `equivStart` / `equivEnd` fallback](#pc-with-equivstart--equivend-fallback)
  - [`<sc/>` and `<ec/>`](#sc-and-ec)
  - [`<cp/>`](#cp)
  - [`<sm/>`, `<em/>` and `<mrk/>`](#sm-em-and-mrk)
- [CDATA and HTML](#cdata-and-html)
  - [CDATA](#cdata)
  - [HTML in `<data/>` (escaped)](#html-in-data-escaped)
  - [HTML in `<data/>` (CDATA)](#html-in-data-cdata)
- [Limits and Edge Cases](#limits-and-edge-cases)
- [Ignored Attributes](#ignored-attributes)

## Rules

| # | Rule |
|---|------|
| 1 | Text and `CDATA` are taken verbatim. |
| 2 | `<pc/>` (2.x) is wrapped in the original data referenced by `dataRefStart` / `dataRefEnd`, falling back to `equivStart` / `equivEnd`. |
| 3 | An **empty** inline element (`<ph/>`, `<sc/>`, `<ec/>`, `<x/>`, `<bx/>`, `<ex/>`, …) is replaced by the original data referenced by `dataRef` (2.x), falling back to `equiv` (2.x) or `equiv-text` (1.2), otherwise by nothing. |
| 4 | An inline element **with content** (`<g/>`, `<mrk/>`, `<ph>native</ph>`, `<bpt/>`, `<ept/>`, …) is replaced by its rendered content. The tag itself is dropped. |
| 5 | `<cp hex="…"/>` (2.x) becomes the referenced code point. |
| 6 | `<sm/>` and `<em/>` (2.x) annotation markers contribute nothing. |
| 7 | Comments and processing instructions contribute nothing. |

In XLIFF 2.x the original data lives in `<originalData/>` inside the enclosing `<unit/>` and is referenced by the `id` of a `<data/>` element.

## Behavior Change

Inline elements are always rendered. Before, only the text nodes were taken, so `equiv`, `equiv-text` and `<cp/>` contributed nothing.

```xml
<target>Hallo <x id="1" equiv-text="[IMG]"/>!</target>
```

| Version         | Result         |
|-----------------|----------------|
| before          | `Hallo !`      |
| inline renderer | `Hallo [IMG]!` |

## XLIFF 1.2

### `<x/>`

An empty placeholder is replaced by `equiv-text`.

```xml
<trans-unit id="x_equiv_text" resname="x_equiv_text">
    <source>Hello <x id="1" equiv-text="{0}"/>!</source>
    <target>Hallo <x id="1" equiv-text="{0}"/>!</target>
</trans-unit>
```

Result: `Hallo {0}!`

The placeholder is resolved by the message argument. With `Max`: `Hallo Max!`

**getMessage()**

```java
messageSource.getMessage("x_equiv_text", new Object[] { "Max" }, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{x_equiv_text('Max')}"></p>
```

Without `equiv-text` the element contributes nothing:

```xml
<trans-unit id="x_without_equiv_text" resname="x_without_equiv_text">
    <source>Hello <x id="1"/>!</source>
    <target>Hallo <x id="1"/>!</target>
</trans-unit>
```

Result: `Hallo !`

**getMessage()**

```java
messageSource.getMessage("x_without_equiv_text", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{x_without_equiv_text}"></p>
```

### `<bx/>` and `<ex/>`

Empty start and end elements are replaced by `equiv-text`.

```xml
<trans-unit id="bx_ex_equiv_text" resname="bx_ex_equiv_text">
    <source>A <bx id="1" equiv-text="&lt;b&gt;"/>text<ex id="1" equiv-text="&lt;/b&gt;"/></source>
    <target>Ein <bx id="1" equiv-text="&lt;b&gt;"/>Text<ex id="1" equiv-text="&lt;/b&gt;"/></target>
</trans-unit>
```

Result: `Ein <b>Text</b>`

**getMessage()**

```java
messageSource.getMessage("bx_ex_equiv_text", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{bx_ex_equiv_text}"></p>
```

### `<g/>`

The tag is dropped, the wrapped text is kept.

```xml
<trans-unit id="g_keeps_text" resname="g_keeps_text">
    <source>A <g id="1" ctype="bold">text</g></source>
    <target>Ein <g id="1" ctype="bold">Text</g></target>
</trans-unit>
```

Result: `Ein Text`

**getMessage()**

```java
messageSource.getMessage("g_keeps_text", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{g_keeps_text}"></p>
```

### `<ph>` with native content

The native content of the placeholder is used.

```xml
<trans-unit id="ph_native_content" resname="ph_native_content">
    <source>Hello <ph id="1">{0}</ph></source>
    <target>Hallo <ph id="1">{0}</ph></target>
</trans-unit>
```

Result: `Hallo {0}`

The placeholder is resolved by the message argument. With `Max`: `Hallo Max`

**getMessage()**

```java
messageSource.getMessage("ph_native_content", new Object[] { "Max" }, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{ph_native_content('Max')}"></p>
```

### `<bpt>` and `<ept>`

Start and end elements with native content keep that content.

```xml
<trans-unit id="bpt_ept_native_content" resname="bpt_ept_native_content">
    <source>A <bpt id="1">&lt;b&gt;</bpt>text<ept id="1">&lt;/b&gt;</ept></source>
    <target>Ein <bpt id="1">&lt;b&gt;</bpt>Text<ept id="1">&lt;/b&gt;</ept></target>
</trans-unit>
```

Result: `Ein <b>Text</b>`

**getMessage()**

```java
messageSource.getMessage("bpt_ept_native_content", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{bpt_ept_native_content}"></p>
```

### `<mrk/>`

The tag is dropped, the wrapped text is kept.

```xml
<trans-unit id="mrk_keeps_text" resname="mrk_keeps_text">
    <source>A <mrk mtype="term">term</mrk></source>
    <target>Ein <mrk mtype="term">Begriff</mrk></target>
</trans-unit>
```

Result: `Ein Begriff`

**getMessage()**

```java
messageSource.getMessage("mrk_keeps_text", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{mrk_keeps_text}"></p>
```

## XLIFF 2.x

### `<ph/>` with original data

The placeholder is replaced by the `<data/>` it references via `dataRef`.

```xml
<unit id="ph_data_ref" name="ph_data_ref">
    <originalData>
        <data id="d1">{0}</data>
    </originalData>
    <segment>
        <source>Hello <ph id="1" dataRef="d1"/>!</source>
        <target>Hallo <ph id="1" dataRef="d1"/>!</target>
    </segment>
</unit>
```

Result: `Hallo {0}!`

The placeholder is resolved by the message argument. With `Max`: `Hallo Max!`

**getMessage()**

```java
messageSource.getMessage("ph_data_ref", new Object[] { "Max" }, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{ph_data_ref('Max')}"></p>
```

Without a `<target/>` the `<source/>` is rendered the same way, result: `Hello {0}!`

### `<ph/>` with `equiv` fallback

Without `dataRef` the `equiv` attribute is used.

```xml
<unit id="ph_equiv" name="ph_equiv">
    <segment>
        <source>Line<ph id="1" equiv="&#10;"/>end</source>
        <target>Zeile<ph id="1" equiv="&#10;"/>Ende</target>
    </segment>
</unit>
```

Result: `Zeile` + line feed + `Ende`

**getMessage()**

```java
messageSource.getMessage("ph_equiv", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{ph_equiv}"></p>
```

### `<pc/>` with original data

The content of the `<pc/>` is wrapped in the original data referenced by `dataRefStart` and `dataRefEnd`.

```xml
<unit id="pc_data_ref" name="pc_data_ref">
    <originalData>
        <data id="d1">&lt;a href="/x"&gt;</data>
        <data id="d2">&lt;/a&gt;</data>
    </originalData>
    <segment>
        <source>Click <pc id="1" dataRefStart="d1" dataRefEnd="d2">here</pc></source>
        <target>Klicke <pc id="1" dataRefStart="d1" dataRefEnd="d2">hier</pc></target>
    </segment>
</unit>
```

Result: `Klicke <a href="/x">hier</a>`

**getMessage()**

```java
messageSource.getMessage("pc_data_ref", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{pc_data_ref}"></p>
```

### `<pc/>` with `equivStart` / `equivEnd` fallback

Without original data the `equivStart` and `equivEnd` attributes are used.

```xml
<unit id="pc_equiv" name="pc_equiv">
    <segment>
        <source>Click <pc id="1" equivStart="&lt;a&gt;" equivEnd="&lt;/a&gt;">here</pc></source>
        <target>Klicke <pc id="1" equivStart="&lt;a&gt;" equivEnd="&lt;/a&gt;">hier</pc></target>
    </segment>
</unit>
```

Result: `Klicke <a>hier</a>`

**getMessage()**

```java
messageSource.getMessage("pc_equiv", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{pc_equiv}"></p>
```

### `<sc/>` and `<ec/>`

Like `<pc/>`, but start and end are separate elements. They are needed when start and end overlap other elements or span segments. Each one is replaced by its `dataRef`.

```xml
<unit id="sc_ec_data_ref" name="sc_ec_data_ref">
    <originalData>
        <data id="d1">[</data>
        <data id="d2">]</data>
    </originalData>
    <segment>
        <source><sc id="1" dataRef="d1"/>Text<ec startRef="1" dataRef="d2"/></source>
        <target><sc id="1" dataRef="d1"/>Text<ec startRef="1" dataRef="d2"/></target>
    </segment>
</unit>
```

Result: `[Text]`

**getMessage()**

```java
messageSource.getMessage("sc_ec_data_ref", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{sc_ec_data_ref}"></p>
```

### `<cp/>`

Represents a character that is not allowed in XML. The `hex` attribute is turned into the code point.

```xml
<unit id="cp_code_point" name="cp_code_point">
    <segment>
        <source>A<cp hex="0007"/>B</source>
        <target>A<cp hex="0007"/>B</target>
    </segment>
</unit>
```

Result: `A` + U+0007 + `B`

**getMessage()**

```java
messageSource.getMessage("cp_code_point", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{cp_code_point}"></p>
```

### `<sm/>`, `<em/>` and `<mrk/>`

Annotation markers carry metadata (comment, glossary term, …) but no text and no original data. The markers are dropped, the text between them and the text wrapped by `<mrk/>` is kept.

```xml
<unit id="annotation_markers" name="annotation_markers">
    <segment>
        <source>A <sm id="m1"/>B<em startRef="m1"/> <mrk id="m2" type="term">C</mrk></source>
        <target>A <sm id="m1"/>B<em startRef="m1"/> <mrk id="m2" type="term">C</mrk></target>
    </segment>
</unit>
```

Result: `A B C`

**getMessage()**

```java
messageSource.getMessage("annotation_markers", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:text="#{annotation_markers}"></p>
```

A `<message/>` needs no place for a comment or a glossary reference, so nothing is lost by dropping the markers.

## CDATA and HTML

### CDATA

`CDATA` sections are taken verbatim. This works the same in XLIFF 1.2 and 2.x.

```xml
<unit id="cdata_verbatim" name="cdata_verbatim">
    <segment>
        <source><![CDATA[<b>Bold</b>]]></source>
        <target><![CDATA[<b>Fett</b>]]></target>
    </segment>
</unit>
```

Result: `<b>Fett</b>`

**getMessage()**

```java
messageSource.getMessage("cdata_verbatim", null, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{cdata_verbatim}"></p>
```

### HTML in `<data/>` (escaped)

HTML can be kept in the original data and referenced from `<pc/>` and `<ph/>`. This is the form the XLIFF 2.x standard expects.

```xml
<unit id="html-without-cdata" name="html-without-cdata">
    <originalData>
        <data id="d1">&lt;a href="https://example.com" class="btn"&gt;</data>
        <data id="d2">&lt;/a&gt;</data>
        <data id="d3">{0}</data>
    </originalData>
    <segment>
        <source>Klicken Sie <pc id="1" dataRefStart="d1" dataRefEnd="d2">hier</pc>, um das Profil von <ph id="2" dataRef="d3"/> aufzurufen.</source>
        <target>Click <pc id="1" dataRefStart="d1" dataRefEnd="d2">here</pc> to view the profile of <ph id="2" dataRef="d3"/>.</target>
    </segment>
</unit>
```

Result: `Click <a href="https://example.com" class="btn">here</a> to view the profile of {0}.`

The placeholder is resolved by the message argument. With `Max`: `Click <a href="https://example.com" class="btn">here</a> to view the profile of Max.`

**getMessage()**

```java
messageSource.getMessage("html-without-cdata", new Object[] { "Max" }, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{html-without-cdata('Max')}"></p>
```

### HTML in `<data/>` (CDATA)

The same, with `CDATA` instead of escaping. The result is identical.

```xml
<unit id="html-with-cdata" name="html-with-cdata">
    <originalData>
        <data id="d1"><![CDATA[<a href="https://example.com" class="btn">]]></data>
        <data id="d2"><![CDATA[</a>]]></data>
        <data id="d3"><![CDATA[{0}]]></data>
    </originalData>
    <segment>
        <source>Klicken Sie <pc id="1" dataRefStart="d1" dataRefEnd="d2">hier</pc>, um das Profil von <ph id="2" dataRef="d3"/> aufzurufen.</source>
        <target>Click <pc id="1" dataRefStart="d1" dataRefEnd="d2">here</pc> to view the profile of <ph id="2" dataRef="d3"/>.</target>
    </segment>
</unit>
```

Result: `Click <a href="https://example.com" class="btn">here</a> to view the profile of {0}.`

The placeholder is resolved by the message argument. With `Max`: `Click <a href="https://example.com" class="btn">here</a> to view the profile of Max.`

**getMessage()**

```java
messageSource.getMessage("html-with-cdata", new Object[] { "Max" }, Locale.forLanguageTag("de"));
```

**Thymeleaf**

```html
<p th:utext="#{html-with-cdata('Max')}"></p>
```

## Limits and Edge Cases

- **Maximum nesting depth:** Content nested deeper than 8 levels is dropped instead of rendered. This protects against a stack overflow; text above the limit is kept.
- **Invalid `hex`:** An invalid `hex` value in `<cp/>` is skipped.
- **Whitespace:** The rendered text is trimmed unless `xml:space="preserve"` is set, see the main [README](../README.md).
- **`<it/>` and `<sub/>` (1.2):** Follow the general rules (content is rendered, an empty element falls back to `equiv-text`).

## Ignored Attributes

`disp`, `dispStart` and `dispEnd` (2.x) are **not** used. They are display hints for the translation tool (e.g. "Customer name" instead of `{0}`), not text for the application. The standard keeps them apart from `equiv`, which is the text replacement for the output.

```xml
<target>Hallo <ph id="1" dataRef="d1" disp="Kundenname"/>!</target>
```

Result: `Hallo {0}!`, not `Hallo Kundenname!`.
