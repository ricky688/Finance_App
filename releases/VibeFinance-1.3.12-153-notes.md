# VibeFinance1.3.12 (153)

Moves asset thumbnail decoding and linked-app icon loading off the UI thread, removing a duplicate synchronous package query. Financial data and settings unchanged.

R8/resource shrinking enabled.429 unit tests and debug/release builds passed. Installed in place on Samsung SM-S9280, retaining data. Single four-swipe Assets check: missed deadlines8.12%→4.02%, p95 25→18ms. This is not a controlled repeated benchmark; remaining lag and other pages require further measurement.

SHA256:4658ca7d08f145c6d375ecffffd0e1592e065e4f188ebeec003186148a894a37
