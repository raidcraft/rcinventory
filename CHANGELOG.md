# [1.6.0](https://github.com/raidcraft/rcinventory/compare/v1.5.0...v1.6.0) (2020-12-30)


### Features

* Implement pre caching when player joins server but is not loaded into world. Database inventory is loaded and deserialized asynchronously. Closes [#16](https://github.com/raidcraft/rcinventory/issues/16) ([31a2b00](https://github.com/raidcraft/rcinventory/commit/31a2b008f72a63a555d8b8a82306aa6eccf9ca57))

# [1.5.0](https://github.com/raidcraft/rcinventory/compare/v1.4.2...v1.5.0) (2020-12-27)


### Features

* Added synchronization of players ender chest. Closes [#13](https://github.com/raidcraft/rcinventory/issues/13). ([650b0c8](https://github.com/raidcraft/rcinventory/commit/650b0c85b5894b9b9df4bb812c74d55feb5cf6b9))

## [1.4.2](https://github.com/raidcraft/rcinventory/compare/v1.4.1...v1.4.2) (2020-12-23)


### Bug Fixes

* Fix cleanup by keeping backups per world ([d195ccc](https://github.com/raidcraft/rcinventory/commit/d195ccc3e5a03bcda1b9d8a95e570b88c18aad06))

## [1.4.1](https://github.com/raidcraft/rcinventory/compare/v1.4.0...v1.4.1) (2020-12-22)


### Bug Fixes

* Fixes world configuration to easily create world groups ([341b1a1](https://github.com/raidcraft/rcinventory/commit/341b1a192a9e0f783bab56f15cc9584ced19636a))

# [1.4.0](https://github.com/raidcraft/rcinventory/compare/v1.3.0...v1.4.0) (2020-12-22)


### Features

* Adding world configs to prevent sync between specific worlds. Closes [#8](https://github.com/raidcraft/rcinventory/issues/8) ([c4294c6](https://github.com/raidcraft/rcinventory/commit/c4294c6f6aebed0c44ca91d3ba19e46ebd779f50))

# [1.3.0](https://github.com/raidcraft/rcinventory/compare/v1.2.0...v1.3.0) (2020-12-22)


### Features

* Implement database cleanup. Fixes [#10](https://github.com/raidcraft/rcinventory/issues/10). ([1ae9989](https://github.com/raidcraft/rcinventory/commit/1ae99896e811fab8f79a82b3d68c583d926df911))

# [1.2.0](https://github.com/raidcraft/rcinventory/compare/v1.1.1...v1.2.0) (2020-12-22)


### Features

* Implement cyclic inventory saves. Closes [#7](https://github.com/raidcraft/rcinventory/issues/7) ([5e52e83](https://github.com/raidcraft/rcinventory/commit/5e52e83ac5661b36ce70b0fbae607a75dfabf38b))

## [1.1.1](https://github.com/raidcraft/rcinventory/compare/v1.1.0...v1.1.1) (2020-12-22)


### Bug Fixes

* Fix database inventory equals method ([33abfda](https://github.com/raidcraft/rcinventory/commit/33abfda71ecc7ac98d5c05202021597238175f5b))
* Fixed equals method of database inventory to avoid duplicate entries ([b5383a1](https://github.com/raidcraft/rcinventory/commit/b5383a1f168615e1a1ab8b556f610cecda94112d))

# [1.1.0](https://github.com/raidcraft/rcinventory/compare/v1.0.0...v1.1.0) (2020-12-21)


### Bug Fixes

* Save inventories on plugin disable method call ([abfe0e3](https://github.com/raidcraft/rcinventory/commit/abfe0e34dc15c44d9e7ef93837e2668e6828cc55))


### Features

* Add check if inventory is already up to date. Fixes [#11](https://github.com/raidcraft/rcinventory/issues/11) ([bd17515](https://github.com/raidcraft/rcinventory/commit/bd175152b570e3011dd6145d7720050c0d966606))

# 1.0.0 (2020-12-21)


### Bug Fixes

* Add database migration ([b07c3dd](https://github.com/raidcraft/rcinventory/commit/b07c3dd18461dab024e155b25d17242b071c7d5f))
* Fix database layout and player listener to handle join/leave events ([4a868b8](https://github.com/raidcraft/rcinventory/commit/4a868b8d0715481af15386ee206432f4e8b0749c))
* Fix inv restore at login ([73691cb](https://github.com/raidcraft/rcinventory/commit/73691cb1b042e9a06afee3eadbd1f2f91af83c38))
* Initial adaptions to introduce new plugin from scratch ([6da568c](https://github.com/raidcraft/rcinventory/commit/6da568ce967f6e1118fcdbe6fd02653e19e70d0f))
* Introduce player holder to abstract inventory holder ([d311a1b](https://github.com/raidcraft/rcinventory/commit/d311a1ba23aafea5a0c9c111d96befd7373ca447))


### Features

* Implement basic functionality (untested) ([03771f4](https://github.com/raidcraft/rcinventory/commit/03771f45c1526452d61e91953f0cec7b4f625d2d))
