# 🔼 BoostedYAML

A simple-to-use standalone (beneficial for cross-platform and multimodule projects) Java library delivering boosted experience while working with YAML documents. Work on this
project started on Apr 8, 2021 with the target to build advanced, hence still easy-to-use library which would enable
developers to manage documents and files with ease.

If you are developing plugins for Spigot/BungeeCord API, make sure to read the post at [SpigotMC forums](https://www.spigotmc.org/threads/545585/) for more information regarding setup and other relevant useful information.

# ❓ Why to use BoostedYAML over other libraries?

BoostedYAML is single of its kind, given the following advantages:

1. **SnakeYAML Engine based** - built upon popular and trusted software.
2. **YAML 1.2 supported** - allows to reliably parse JSON files
3. **Comments everywhere** - no longer a headache for your customers, say bye to external documentation.
4. **File updater** - upgrading to the latest version of your software? Adapt the configuration files easily, with one
   line of code.
5. **Beautiful structure management** - makes your code cleaner and easier to read.
6. **Functional interfaces included** - we've got something for everyone.
7. **Amazing features** - automatic saving, updating, endless possibilities.
8. **Settings for everything** - customize it to suite your needs just perfectly.
9. **Performant and well documented** - just like a dream.
10. **Open source** - if there's anything that can be improved, feel free to let me know, or contribute.
11. **TDD (test driven development)** - don't worry about reliability, everything's verified automatically before
    production by more than 140 tests.

All of that with quick and kind support. Convinced? Let's get you onboard.

# 🔧 Quick setup
Setup takes only about 5 minutes. Example software built using this guide is available at https://github.com/dejvokep/boosted-yaml-example.
## 1. Add using Maven:
BoostedYAML is hosted by Maven Central Repository. That means, you only need to add a dependency:
```xml
<dependency>
  <groupId>dev.dejvokep</groupId>
  <artifactId>boosted-yaml</artifactId>
  <version>1.3.4</version>
</dependency>
```
Add the following shading section to prevent class loader conflicts:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.3.0</version>
      <configuration>
        <relocations>
          <relocation>
            <pattern>dev.dejvokep.boostedyaml</pattern>
            <!-- Replace this -->
            <shadedPattern>me.plugin.libs</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
## 1.1. Add using Gradle:
BoostedYAML is hosted by Maven Central Repository. That means, you only need to add a dependency (and the maven central repo):
```gradle
repositories {
   mavenCentral()
}

dependencies {
   implementation "dev.dejvokep:boosted-yaml:1.3"
}
```

Add the following shadowing section to prevent class loader conflicts:
- Add this at the very start of your build.gradle file: (Gradle 7.3)
```gradle
plugins {
  id 'com.github.johnrengelman.shadow' version '7.1.2'
  id 'java'
}
```
- To use the shadowing project, this will help with relocating,
- if this is not available for your gradle version, please [take a look here](https://imperceptiblethoughts.com/shadow/getting-started/)

Then at the end of your build.gradle file:
```gradle
// Relocating a Package
shadowJar {
   // The second string is where you will locate dev.dejvokep.boostedyaml
   relocate 'dev.dejvokep.boostedyaml', 'me.plugin.libs'
}
```
Learn more about shadow for gradle [here](https://imperceptiblethoughts.com/shadow/introduction/#benefits-of-shadow)

## 2. Routes and how to access content
Routes are immutable objects used to address content - similar to URIs (Universe Resource Identifier). Please read more about them [here](https://dejvokep.gitbook.io/boostedyaml/routing/routes).
## 3. Create your document:
Please note that this guide covers only the basics, there are a lot more variations of the method - learn more at the [Javadoc](https://javadoc.io/doc/dev.dejvokep/boosted-yaml/latest/dev/dejvokep/boostedyaml/YamlDocument.html) or [wiki](https://dejvokep.gitbook.io/boostedyaml/).
```java
YamlDocument config = YamlDocument.create(new File("config.yml"), getResource("config.yml"));
```
**The file will automatically be managed by the library** - no need to create it if it does not exist, nor copy the defaults. Everything's done automatically. **Please note** that if you chose to use [Routes (objects)](https://javadoc.io/doc/dev.dejvokep/boosted-yaml/latest/dev/dejvokep/boostedyaml/route/Route.html), you will need to configure the document to use `KeyFormat.OBJECT`. You can do so via GeneralSettings, with the final `.create()` method looking like:
```java
YamlDocument config = YamlDocument.create(new File("config.yml"), getResource("config.yml"), GeneralSettings.builder().setKeyFormat(KeyFormat.OBJECT).build(), LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
```
## 4. Updater prerequisites:
###### A)
You might already be familiar with specifying version inside your document, most common is something like this:
```yaml
config-version: 1
```
BoostedYAML defines such information as version ID. Pick a route at which the version ID of the document will be. **You will never be able to change it** and **the defaults must have it specified and valid**. Provide the route to UpdaterSettings like this:
```java
UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build();
```
Do not forget to provide the built settings to the `.create()` method.
###### B)
The first version ID the document's going to have is 1, like shown below. The next release of the file will have ID of 2, then 3...:
```yaml
config-version: 1   # 1st release
config-version: 2   # 2nd release
config-version: 3   # 3rd release
# etc.
```
###### C)
Parameters of the `.create()` might now look like this:
```java
YamlDocument config = YamlDocument.create(new File("config.yml"), getResource("config.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
```
The custom LoaderSettings with enabled auto-update will ensure the document's automatically updated initially and each time it's reloaded.

**That's it! No need to do some weird file version handling manually, let BoostedYAML do it for you.** The updater also offers some nice features like [ignored routes](https://dejvokep.gitbook.io/boostedyaml/settings/updatersettings#ignored-routes) and [relocations](https://dejvokep.gitbook.io/boostedyaml/settings/updatersettings#ignored-routes), which you can learn more about by clicking the links.
## 5. Use the document to it's fullest:
Reload/save anytime from/to the file:
```java
config.reload();
config.save();
```
Obtain data using `getX(String route)`, alternatively, use functional getters for functional method chaining:
```java
Mode m = config.getStringOptional("mode").map(mode -> Mode.valueOf(mode.toUpperCase())).orElse(Mode.PERFORMANCE);
```
**BoostedYAML also provides** it's own serialization system and other cool options, about which you can read more [at the wiki](https://dejvokep.gitbook.io/boostedyaml/).

# 🤖 Support
You can read the detailed instructions [at the wiki](https://dejvokep.gitbook.io/boostedyaml/). If you need help with anything, feel free join the [Discord server](https://discord.gg/BbhADEy). Or, just to talk with us 👋
