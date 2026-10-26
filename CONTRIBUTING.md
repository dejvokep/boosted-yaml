# Contributing to BoostedYAML

Thanks for your interest in improving BoostedYAML! Bug reports, fixes, tests, documentation and features are all welcome. This guide explains how to get your change merged with as little back-and-forth as possible.

- **Questions and help:** join the [Discord server](https://discord.gg/BbhADEy) or read the [wiki](https://dejvokep.gitbook.io/boostedyaml/).
- **Security issues:** do **not** open a public issue. Follow [SECURITY.md](SECURITY.md) instead.

## Before you start

- **Bugs:** search the [existing issues](https://github.com/dejvokep/boosted-yaml/issues) first. If none matches, open one with the BoostedYAML and Java versions, a minimal YAML document and code sample, and the expected vs. actual result.
- **Small fixes** (typos, Javadoc, obvious bugs with a test): open a pull request directly.
- **Features, behaviour changes and refactors:** open an issue first and describe what you want to change and why. BoostedYAML is a widely used library, so API design and performance are considered carefully - agreeing on the approach up front saves you from rewriting the PR.

## Building

You need **JDK 8 or newer**. The Maven Wrapper downloads the right Maven version, so you don't need Maven installed.

```bash
./mvnw verify        # Linux, macOS, Git Bash
mvnw verify          # Windows (cmd), or .\mvnw verify in PowerShell
```

`verify` compiles the library, runs all tests, checks license headers and builds the shaded jar. It needs no credentials. CI runs the same command on Java 8, 17, 21 and 25 for every pull request.

The release artifacts (sources, Javadoc, signing) are built only with the `release` profile and are not needed for contributing. To check that your Javadoc builds, run:

```bash
./mvnw -Prelease -Dgpg.skip -DskipTests verify
```

## Making changes

### Java 8

The library targets **Java 8** and must stay compatible with it. On JDK 9+ the build compiles with `--release 8`, so using an API newer than Java 8 fails the build.

### Tests

Every change in behaviour needs tests, and every bug fix needs a test that fails without the fix.

- Tests use JUnit 5 and live in `src/test/java`, mirroring the package of the class they test (`Section` → `SectionTest`).
- Cover both overloads when a method has a `String` route and a `Route` variant.
- Make sure `./mvnw verify` passes before opening the PR.

### Public API

- **Don't remove or change public API in a pull request.** Deprecate it instead: add `@Deprecated` and a Javadoc `@deprecated` tag that names the replacement. Deprecated API is removed only in a major release.
- **Document every public class and method with Javadoc**, including what happens with `null`, missing values and defaults.
- **Annotate nullability** with `@NotNull` / `@Nullable` (from `org.jetbrains.annotations`) on all public parameters and return values.
- **Follow the existing naming.** For example, where a method has both a `Route` and a string-route variant that can't be told apart by overloading (such as two `Set` parameters), the string variant carries `String` or `StringRoute` in its name, as in `addIgnoredStringRoutes` or `addStringRouteMappers`.

### Routes and performance

String routes are resolved directly, split by the document's configured separator, without being converted to `Route` objects. This keeps both kinds of routes equally fast, and it is one of the library's core goals.

- Keep the `String` and `Route` overloads on their own lookup paths. Don't implement one by converting to the other (e.g. via `Route.fromString`).
- Always use the document's route separator (`GeneralSettings#getRouteSeparator()`), never the default `'.'`.
- Avoid extra allocations, regular expressions and stream pipelines in hot paths such as getters, setters and route traversal.

### Keep the diff focused

- Change only what your PR is about. Don't reformat, reorder or rename unrelated code.
- **License headers:** every `.java` and `.xml` file needs the header from `LICENSE-header.txt`; the build fails if one is missing. Add it to new files with `./mvnw license:format`, but don't commit header changes to files you didn't otherwise touch.
- Match the surrounding code style: 4-space indentation, and comments in the same style as the surrounding code.

## Pull requests

- Target the `main` branch, and keep one topic per PR.
- Use short, imperative commit messages, e.g. `Fix getOptionalEnum stringifying the value`.
- In the description, explain what changed and why, and link the issue (`Fixes #123`).
- CI must pass. If you change behaviour, say so explicitly, including how existing users are affected.
- Pull requests waiting on changes from the author for more than 60 days may be closed. You're welcome to reopen them when you continue.

## License

BoostedYAML is licensed under the [Apache License 2.0](LICENSE). By submitting a contribution, you agree that it is licensed under the same license, as described in section 5 of the license. There is no separate contributor agreement.