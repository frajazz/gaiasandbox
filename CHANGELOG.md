# Change Log

## [Unreleased](https://github.com/ari-zah/gaiasandbox/tree/HEAD)

[Full Changelog](https://github.com/ari-zah/gaiasandbox/compare/0.707b...HEAD)

**Implemented enhancements:**

- Add smooth transitions between levels of detail [\#51](https://github.com/ari-zah/gaiasandbox/issues/51)
- Use view angle as priority for click-selections [\#50](https://github.com/ari-zah/gaiasandbox/issues/50)
- Get the Gaia Sanbox ready for proper motions [\#48](https://github.com/ari-zah/gaiasandbox/issues/48)

**Fixed bugs:**

- Fix Gaia scan code [\#49](https://github.com/ari-zah/gaiasandbox/issues/49)

## [0.707b](https://github.com/ari-zah/gaiasandbox/tree/0.707b) (2015-09-14)
[Full Changelog](https://github.com/ari-zah/gaiasandbox/compare/0.706b...0.707b)

**Implemented enhancements:**

- Simplify loading mechanism of data files [\#46](https://github.com/ari-zah/gaiasandbox/issues/46)
- Add sample image when choosing theme [\#38](https://github.com/ari-zah/gaiasandbox/issues/38)
- Drop old manual lo-res/hi-res texture loading and implement mipmapping [\#35](https://github.com/ari-zah/gaiasandbox/issues/35)
- Update project to libgdx 1.6.0 [\#34](https://github.com/ari-zah/gaiasandbox/issues/34)
- Add simple screenshot mode [\#32](https://github.com/ari-zah/gaiasandbox/issues/32)
- Move default location of screenshots to `$HOME/.gaiasandbox/screenshots` [\#31](https://github.com/ari-zah/gaiasandbox/issues/31)
- Add new Ceres texture from Dawn spacecraft [\#30](https://github.com/ari-zah/gaiasandbox/issues/30)
- New command to travel to focus object instantly [\#29](https://github.com/ari-zah/gaiasandbox/issues/29)
- Support for location info [\#28](https://github.com/ari-zah/gaiasandbox/issues/28)
- Migrate build system to gradle [\#2](https://github.com/ari-zah/gaiasandbox/issues/2)

**Fixed bugs:**

- Linux launcher not working if spaces in path [\#47](https://github.com/ari-zah/gaiasandbox/issues/47)
- Fix labels in Gaia Fov mode [\#45](https://github.com/ari-zah/gaiasandbox/issues/45)
- Last update date is sensible to running locale [\#43](https://github.com/ari-zah/gaiasandbox/issues/43)
- RA and DEC are wrong in binary version of HYG catalog [\#42](https://github.com/ari-zah/gaiasandbox/issues/42)
- Keyboard focus stays in input texts [\#41](https://github.com/ari-zah/gaiasandbox/issues/41)
- Fix new line rendering for perspective lines [\#37](https://github.com/ari-zah/gaiasandbox/issues/37)
- Motion blur not working with FXAA or NFAA [\#36](https://github.com/ari-zah/gaiasandbox/issues/36)
- Fix night/day blending in shader  [\#33](https://github.com/ari-zah/gaiasandbox/issues/33)
- Screenshot action \(F5\) not working well with motion blur [\#27](https://github.com/ari-zah/gaiasandbox/issues/27)

## [0.706b](https://github.com/ari-zah/gaiasandbox/tree/0.706b) (2015-05-05)
[Full Changelog](https://github.com/ari-zah/gaiasandbox/compare/0.705b...0.706b)

**Implemented enhancements:**

- General code style clean-up  [\#25](https://github.com/ari-zah/gaiasandbox/issues/25)
- Big performance improvement in star rendering [\#23](https://github.com/ari-zah/gaiasandbox/issues/23)
- New pixel renderer [\#22](https://github.com/ari-zah/gaiasandbox/issues/22)
- Add controller support [\#21](https://github.com/ari-zah/gaiasandbox/issues/21)
- Motion blur effect [\#20](https://github.com/ari-zah/gaiasandbox/issues/20)
- Interface overhaul [\#19](https://github.com/ari-zah/gaiasandbox/issues/19)
- Better looking lines [\#18](https://github.com/ari-zah/gaiasandbox/issues/18)

**Fixed bugs:**

- Handle outdated properties files in $HOME/.gaiasandbox folder [\#26](https://github.com/ari-zah/gaiasandbox/issues/26)
- Scripting implementation should reset the colour [\#24](https://github.com/ari-zah/gaiasandbox/issues/24)

**Closed issues:**

- deprecated [\#17](https://github.com/ari-zah/gaiasandbox/issues/17)

## [0.705b](https://github.com/ari-zah/gaiasandbox/tree/0.705b) (2015-04-16)
[Full Changelog](https://github.com/ari-zah/gaiasandbox/compare/0.704b...0.705b)

**Fixed bugs:**

- Gaia sandbox current releases do not work on windows [\#16](https://github.com/ari-zah/gaiasandbox/issues/16)
- Post-processing causes display output to disappear in frame output mode [\#14](https://github.com/ari-zah/gaiasandbox/issues/14)
- Make new PixelBloomRenderSystem work for frame output and screenshots [\#7](https://github.com/ari-zah/gaiasandbox/issues/7)
- Make new PixelBloomRenderSystem work in stereoscopic mode [\#6](https://github.com/ari-zah/gaiasandbox/issues/6)

## [0.704b](https://github.com/ari-zah/gaiasandbox/tree/0.704b) (2015-03-27)
[Full Changelog](https://github.com/ari-zah/gaiasandbox/compare/0.703b...0.704b)

**Implemented enhancements:**

- Remove synchronized render lists [\#12](https://github.com/ari-zah/gaiasandbox/issues/12)
- Support top speeds in GUI [\#11](https://github.com/ari-zah/gaiasandbox/issues/11)
- Show camera info in free mode [\#10](https://github.com/ari-zah/gaiasandbox/issues/10)
- Time selector [\#9](https://github.com/ari-zah/gaiasandbox/issues/9)
- Add interface tab to configuration [\#8](https://github.com/ari-zah/gaiasandbox/issues/8)
- Internationalize the application [\#5](https://github.com/ari-zah/gaiasandbox/issues/5)
- Move node data format to JSON [\#1](https://github.com/ari-zah/gaiasandbox/issues/1)

**Fixed bugs:**

- Investigate VM crash [\#4](https://github.com/ari-zah/gaiasandbox/issues/4)
- Decide fate of desktop/doc/gaiasandbox\_manual.tex [\#3](https://github.com/ari-zah/gaiasandbox/issues/3)

## [0.703b](https://github.com/ari-zah/gaiasandbox/tree/0.703b) (2014-12-17)
[Full Changelog](https://github.com/ari-zah/gaiasandbox/compare/0.700b...0.703b)

## [0.700b](https://github.com/ari-zah/gaiasandbox/tree/0.700b) (2014-12-11)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*