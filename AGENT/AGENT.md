# GTOHJS Git Mirror Development Entry

This is a separately downloadable GTOHJS release-source mirror. It does not depend on local active source, caches, or other workspace directories. Do not write local absolute paths into committed content.

For machines, recipes, and API code in this mirror, use the mirror's source and `../docs` as the sources of truth. Preserve native GT recipe-window constraints when modifying recipes, and read the relevant mirror documentation first.

See `JAVA_TOOLCHAIN_ZH_EN.md` for a Java toolchain quick reference. GTOLib uses a JDK 23 toolchain targeting Java 21; GTOHJS/API uses JDK 21 by default.
