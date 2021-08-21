## HyperLap2D libGDX Typing Label Extension

HyperLap2D extension for libgdx runtime that adds [Typing Label](https://github.com/rafaskb/typing-label) support.

### Integration

#### Gradle
![maven-central](https://img.shields.io/maven-central/v/games.rednblack.hyperlap2d/libgdx-typinglabel-extension?color=blue&label=release)
![sonatype-nexus](https://img.shields.io/nexus/s/games.rednblack.hyperlap2d/libgdx-typinglabel-extension?label=sanapshot&server=https%3A%2F%2Foss.sonatype.org)

Extension needs to be included into your `core` project.
```groovy
dependencies {
    api "com.rafaskoberg.gdx:typing-label:$typingLabelVersion"
    api "games.rednblack.hyperlap2d:libgdx-typinglabel-extension:$h2dTypingLabelExtension"
}
```

#### Maven
```xml
<dependency>
  <groupId>games.rednblack.hyperlap2d</groupId>
  <artifactId>libgdx-typinglabel-extension</artifactId>
  <version>0.0.8-SNAPSHOT</version>
  <type>pom</type>
</dependency>
```

**Typing Label Runtime compatibility**

| HyperLap2D         | Typing Label       |
| ------------------ | ------------------ |
| 0.0.8-SNAPSHOT     | 1.2.0              |

### License
HyperLap2D's libGDX runtime Typing Label extension is licensed under the Apache 2.0 License. You can use it free of charge, without limitations both in commercial and non-commercial projects. We love to get (non-mandatory) credit in case you release a game or app using HyperLap2D!

```
Copyright (c) 2021 Francesco Marongiu.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.