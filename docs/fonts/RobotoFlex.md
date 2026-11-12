# Roboto Flex amount font

- Source: Google Fonts `ofl/robotoflex`, pinned revision `8b0a1d0f5983c89bc2b93f1b5fb55f9e252744b5`.
- Upstream: https://github.com/google/fonts/tree/8b0a1d0f5983c89bc2b93f1b5fb55f9e252744b5/ofl/robotoflex
- Original TTF SHA-256: `9b523f7d82593df0107173849ebb8c817471a1df4b4fb2c3cbf40cfd810c8281`.
- License: SIL Open Font License 1.1, retained in `app/src/main/assets/licenses/RobotoFlex-OFL.txt`; upstream copyright/name records are preserved.
- Bundled variable font: FontTools numeric subset of `0123456789.,+- `, bullet U+2022 and minus U+2212; all variation axes remain. No runtime downloads or font provider required.
- Amount settings: weight 700, optical size 42, tabular figures; YTFI 680→788 and GRAD 0→25 for editing emphasis, with fixed width and line height. The focused endpoint uses the font’s supported YTFI maximum (788), slightly taller than the previous 760 endpoint; the resting figures remain unchanged. A bounded palette avoids creating a new font family every animation frame.
- Android 24/25 fallback: a static FontTools instance of the same subset at weight 700, optical size 42, YTFI 680 and grade 0; other axes at defaults.
- No app-wide font replacement. The currency label retains its existing style.

## Packaged artifacts

Generated with FontTools 4.67.0.

- `roboto_flex_numbers.ttf`: 61,268 bytes; SHA-256 `e72ec4a536a6ba7c451028ab837b3328d2b63cb6b7c72dbdde85c00ccc3ea488`.
- `roboto_flex_numbers_static.ttf`: 4,044 bytes; SHA-256 `fb8a06ee092393c2b39a6ff8c0e31dadeba31f4a31fc5f5600bcd1ca3fed3121`.

## Buckwheat-inspired amount hierarchy (1.3.7)

Reference: [Buckwheat TextFieldWithPaddings.kt](https://github.com/danilkinkin/buckwheat/blob/4b60102db5293059aadb7be22bf6390ae4b345a7/app/src/main/java/com/danilkinkin/buckwheat/base/TextFieldWithPaddings.kt) and its editor wrapper at revision `4b60102db5293059aadb7be22bf6390ae4b345a7`. The upstream uses adaptive 40–80sp amounts with a half-size currency label. We reference that hierarchy without copying its implementation.

VibeFinance now uses up to 80sp amounts with a 40sp currency label, in a fixed 96sp line box. Native text measurement at the focused font endpoint reserves caret space and checks candidate font sizes directly to account for Android pixel rounding, reducing the font for long values (down to 16sp, then native horizontal text editing for extreme inputs). Measuring the same tabular endpoint keeps focus animation from changing font size or the following layout. Masked amounts are measured as bullets. Existing 17-family Roboto Flex focus motion, native text selection, keyboard behavior and API 24/25 static fallback remain.
