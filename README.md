# KantanJ
### 簡単 [kantan] (Japanese "simple") + J ("Java") = "Simple Java"

A simple library consisting of various handy utilities, types and wrappers for other libraries to simplify and boost up the development of Java applications of different kinds. Created because I'm bored of copy-pasting all the same code from project to project each time I work on something new. Published because why not.


Hope this will be useful to someone.

Also hope a complete documentation will be published one day...


> **For some partial documentation, tutorials and explanation** see [kantanj Wiki](https://github.com/MeGysssTaa/kantanj/wiki)


## Usage (Maven)

```xml
<repositories>
    <repository>
        <id>reflex.public</id>
        <name>Public Reflex Repository</name>
        <url>https://archiva.reflex.rip/repository/public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.darksidecode.kantanj</groupId>
        <artifactId>kantanj</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Usage (Gradle)

```groovy
repositories {
    maven {
        name 'Public Reflex Repository'
        url 'https://archiva.reflex.rip/repository/public/'
    }
}

dependencies {
    compile group: 'me.darksidecode.kantanj', name: 'kantanj', version: '1.0.0'
}
```


## Building and Installing (Maven)

1. Clone or download this repository, `cd` into it.
2. Run `mvn clean install`.
3. Profit!

```bash
git clone https://github.com/MeGysssTaa/kantanj
cd kantanj
mvn clean install
```


## License

**[Apache License 2.0](https://github.com/MeGysssTaa/kantanj/blob/master/LICENSE)**
