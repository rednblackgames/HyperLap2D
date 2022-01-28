![Logo](https://raw.githubusercontent.com/rednblackgames/HyperLap2D/master/icons/HyperLap2D.png)

[![build](https://img.shields.io/github/workflow/status/rednblackgames/HyperLap2D/SNAPSHOT%20Build?logo=github)](https://github.com/rednblackgames/HyperLap2D/actions?query=workflow%3A%22SNAPSHOT+Build%22)

[![release](https://img.shields.io/github/v/release/rednblackgames/HyperLap2D)](https://github.com/rednblackgames/HyperLap2D/releases)
[![snapshot](https://img.shields.io/nexus/s/games.rednblack.hyperlap2d/runtime-libgdx?label=snapshot&server=https%3A%2F%2Foss.sonatype.org)](https://github.com/rednblackgames/HyperLap2D/actions?query=workflow%3A%22SNAPSHOT+Build%22)

[![Discord](https://img.shields.io/discord/740954840259362826?label=Discord&logo=discord)](https://discord.gg/p69rPNF)

# HyperLap2D
HyperLap2D is a powerful, platform-independent, visual editor for complex 2D worlds and scenes. 

An engine-oriented alternative for building games free from any design constraints.

Open Source ❤️

[![YouTube Video](https://user-images.githubusercontent.com/5543339/110386916-36880900-8061-11eb-872a-f04d2a3a28b9.png)](https://www.youtube.com/watch?v=VUJd7fq_J7c)

### Project Status

Currently, HyperLap2D is in Alpha stage. Many breaking changes could happen update after update without backwards compatibility. Therefore, please always check our [changelog](https://github.com/rednblackgames/HyperLap2D/tree/master/CHANGES) before updating.

### Download

| Release | Snapshot |
| ------- | -------- |
| [GitHub Releases](https://github.com/rednblackgames/HyperLap2D/releases) | [GitHub Actions](https://github.com/rednblackgames/HyperLap2D/actions?query=workflow%3A%22SNAPSHOT+Build%22) |

### Features
- Images and Sprite Animations
- Box2D Physics World
- Dynamic Lights
- [Spine support](https://github.com/rednblackgames/h2d-libgdx-spine-extension)
- Particle Effects
- Grouping complex objects into library items
- Import, Export and Share your compositions
- Live Preview
- Tiled Maps
- Built in 9-patch editor
- Actions Node Editor
- [Plugins](https://hyperlap2d.rednblack.games/wiki/plugins/)
- Open JSON output

### Getting Started
- [HyperLap2D Website](https://hyperlap2d.rednblack.games)
- [Official Wiki](https://hyperlap2d.rednblack.games/wiki)
- [HyperLab](https://hyperlab.rednblack.games)

### Runtime

Currently, HyperLap2D is mainly developed for [libGDX](https://github.com/libgdx/libgdx), but it can easily be integrated into any software thanks to its open JSON output format.

Check out the libGDX runtime [README](https://github.com/rednblackgames/hyperlap2d-runtime-libgdx) to start integrating HyperLap2D's project into your game.

See our [Wiki](https://hyperlap2d.rednblack.games/wiki/hyperlap2d/14-json-export-format/), if you want to learn more about the output format.

### Issues

Have you found a bug or unexpected behavior? Don't panic! Search for [known issues](https://github.com/rednblackgames/HyperLap2D/issues) or feel free to open a new one. Your feedback is important, please try to be as detailed as possible :)

### Contributing

We need your help! HyperLap2D is a very large and complex project, if you have some cool addition or bug fix don't hesitate to create a [Pull Request](https://github.com/rednblackgames/HyperLap2D/pulls). Any contribution, big or small, is always well received.

#### Building from source

HyperLap2D is developed using IntelliJ IDEA. Needs `Java 17`.

1. Fork this repository
2. Clone with `--recurse-submodules` flag
3. Open the main `build.gradle`, which you can find in the root directory
4. Run `runHyperLap2D` gradle task
5. If build fails: `File → Invalidate Caches / Restart`

### License

HyperLap2D Editor is licensed under the GNU Public License, Version 3. You may wish to read [HyperLap2D libGDX Runtime](https://github.com/rednblackgames/hyperlap2d-runtime-libgdx) License and [Spine Extension](https://github.com/rednblackgames/h2d-libgdx-spine-extension) License.

```
HyperLap2D Editor
Copyright (C) 2020 Francesco Marongiu

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

#### Overlap2D

HyperLap2D is a fork of [Overlap2D](https://github.com/UnderwaterApps/overlap2d). A very special thanks to UnderwaterApps's Team and all of their Contributors for creating it, as without, HyperLap2D could never be possible.
Check out original: [`OVERLAP2D-AUTHORS`](https://github.com/rednblackgames/HyperLap2D/blob/master/OVERLAP2D-AUTHORS) and [`OVERLAP2D-CONTRIBUTORS`](https://github.com/rednblackgames/HyperLap2D/blob/master/OVERLAP2D-CONTRIBUTORS)

_Overlap2D_ was licensed under `Apache 2.0`
```
Copyright 2015 Underwater Apps LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
