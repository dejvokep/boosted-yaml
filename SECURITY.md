# Security Policy

## Supported versions

Only the latest release, as indicated by the releases tab, receives security fixes. Please upgrade before reporting, if you can.

## Reporting a vulnerability

**Please do not report security issues in public GitHub issues, pull requests or on Discord.**

Report them privately through one of these channels:

- **GitHub** (preferred): open a report via [Report a vulnerability](https://github.com/dejvokep/boosted-yaml/security/advisories/new) on this repository's Security tab.
- **Email**: [admin@dejvokep.dev](mailto:admin@dejvokep.dev), with `[BoostedYAML security]` in the subject.

Please include:

- the BoostedYAML version, Java version and platform (e.g. Spigot, BungeeCord, Velocity or standalone);
- a minimal YAML document and code that reproduces the issue;
- the settings used (`GeneralSettings`, `LoaderSettings`, `DumperSettings`, `UpdaterSettings`), if not the defaults;
- the impact you observed or expect (e.g. crash, excessive memory or CPU use, unexpected object creation, data loss).

## What to expect

BoostedYAML is maintained by a single person, so these are targets rather than guarantees:

- acknowledgement within **7 days**;
- an initial assessment within **14 days**;
- a fix or mitigation released as soon as practical, usually as a patch release, followed by a GitHub Security Advisory.

You will be kept informed of progress and credited in the advisory unless you prefer to stay anonymous. Please give us a reasonable time to release a fix before disclosing the issue publicly.

## Scope

In scope:

- vulnerabilities in BoostedYAML's own code: loading, dumping, the updater, routes and the serialization system;
- issues in the bundled (shaded) [SnakeYAML Engine](https://github.com/snakeyaml/snakeyaml-engine) that affect BoostedYAML users. Because the engine is shaded into `dev.dejvokep.boostedyaml.libs`, its fixes only reach you through a new BoostedYAML release. Please also report engine issues upstream.

Out of scope:

- loading untrusted documents with limits deliberately raised or disabled (see below);
- custom serializers or `TypeAdapter`s registered by your application;
- vulnerabilities in your own plugin or application code.

## Loading untrusted YAML

If you load YAML from untrusted sources, keep the parser limits in `LoaderSettings` at their defaults or tighten them:

- `setMaxCollectionAliases(int)` limits alias expansion ("billion laughs" style attacks);
- `setCodePointLimit(int)` limits the size of the input document;
- `setAllowDuplicateKeys(false)` rejects documents with duplicate keys.
