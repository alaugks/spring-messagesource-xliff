# Migration: 3.x → 4.0 — Domain Removed

Version 4.0 removes the **domain** concept entirely. In 3.x, a translation file's domain (derived from its filename) could be prepended to the message code; 4.0 no longer does this. The message code is now always exactly the unit's `resname` / `name` (fallback `id`) — see [Translation Key](../README.md#translation-key) in the main README.

## What was removed

| Removed API                                                | Behaviour in 3.x                                                                                                                                                                                 |
|------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Builder.defaultDomain(String)`                            | Set the domain that is **not** prefixed onto the code. Default was `"messages"`.                                                                                                                 |
| `Builder.domainDivider(String)` *(deprecated since 4.0.0-SNAPSHOT)* | Separator used between domain and code when the file's domain differed from the default domain. Default was `"."`.                                                                               |
| Implicit domain from filename                              | Each file's domain was derived from the `<domain>` part of its filename (e.g. `payment_de.xlf` → domain `payment`) and prepended to every code in that file, unless it equalled `defaultDomain`. |

None of this exists anymore. The `<name>` part of a filename is now purely cosmetic — see [Structure of the Translation Filename](../README.md#structure-of-the-translation-filename).

## Migration steps

1. **Remove the calls.** Delete `.defaultDomain(...)` and `.domainDivider(...)` from your builder configuration — both methods are gone and the code will no longer compile.

   ```diff
    XliffResourceMessageSource
        .builder(Locale.forLanguageTag("en"), "translations/*")
   -    .defaultDomain("messages")
   -    .domainDivider(".")
        .build();
   ```

2. **Bake the former prefix into the file.** If a file's domain differed from your configured default domain, its codes used to be resolved as `<domain><divider><resname>`. Since that no longer happens automatically, write the full code directly into `resname` (XLIFF 1.2) / `name` (XLIFF 2.x):

   Before (3.x), `payment_de.xlf`, default domain `messages`, divider `.`:
   ```xml
   <trans-unit id="expiry_date" resname="expiry_date">
   ```
   Resolved code: `payment.expiry_date`

   After (4.0), same file:
   ```xml
   <trans-unit id="expiry_date" resname="payment.expiry_date">
   ```
   Resolved code: `payment.expiry_date` (unchanged)

3. **Files whose domain equalled the default domain need no change.** They were never prefixed, so their `resname` / `name` values already match the resulting code.

4. **A custom `domainDivider` has no replacement.** There is no separator configuration anymore; use whatever literal characters you like directly in `resname` / `name` — the code is opaque to this library.

5. **Codes must be unique across all files**, since there is no longer a per-domain namespace to keep them apart. See the note in [XLIFF Files](../README.md#xliff-files).

## See also

* [Translation Key](../README.md#translation-key)
* [Structure of the Translation Filename](../README.md#structure-of-the-translation-filename)
* [Example with XLIFF Files](../README.md#example-with-xliff-files)
