# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## [UNRELEASED]

- Added icon to en-US fastlane metadata
- Fixed fastlane folder names for language nb-NO
- Fixed fastlane folder names for language pt-BR
- Fixed gradle-wrapper version mismatch
- Pinned gradle to `8.4-all` with related SHA256 Sum
- Update Android Gradle Plugin to 8.3.1
- Fixed deprecated `android.defaults.buildfeatures.buildconfig=true` build option

### Added

- Galician translation thanks to josé m. ([@ghose](https://hosted.weblate.org/user/ghose/)).
- Partial Tamil translation thanks to GobinathAL ([@GobinathAL](https://hosted.weblate.org/user/GobinathAL/)).
- Partial Arabic translation thanks to DJEBBARI.ABDELHAMID ([@dxing96](https://hosted.weblate.org/user/dxing96/)).
- Partial Chinese (Traditional) translation thanks to hugoalh ([@hugoalh](https://hosted.weblate.org/user/hugoalh/)).

### Changed

- Updated Chinese (Simplified) translation thanks to @haygcao and Jane Kong ([@Myon](https://hosted.weblate.org/user/Myon/)).
- Updated Portuguese translation thanks to ssantos ([@ssantos](https://hosted.weblate.org/user/ssantos/)) and SC ([@larcansal](https://hosted.weblate.org/user/larcansal/)).
- Updated Catalan translation thanks to Maite Guix ([@maite.guix](https://hosted.weblate.org/user/maite.guix/)).
- Updated Ukrainian translation thanks to z Z z ([@ruslan.zeezu](https://hosted.weblate.org/user/ruslan.zeezu/)), DankXylese ([@dankxylese](https://hosted.weblate.org/user/dankxylese/)), and Skrripy.
- Updated Spanish translation thanks to Alexandre Díaz ([@Tardo](https://hosted.weblate.org/user/Tardo/)) and gallegonovato ([@gallegonovato](https://hosted.weblate.org/user/gallegonovato/)).
- Updated Russian translation of the app description thanks to AHOHNMYC ([@AHOHNMYC](https://hosted.weblate.org/user/AHOHNMYC/)).
- Updated Italian translation of the app description thanks to Translator-3000.
- Updated Italian translation thanks to Iginio Massari ([@darthmassari](https://hosted.weblate.org/user/darthmassari/)).
- Updated German translation thanks to Ettore Atalan ([@Atalanttore](https://hosted.weblate.org/user/Atalanttore/)).


## [0.0.1] - 2024-03-25

The project has not been updated from the previous maintainer since 2021.
As I use this app a lot, I will continue to update and distribute this app.
I am not an Android Expert but I will do my best !

Cheers

- Rename the app to Tranquille
- Switch to Gradle Kotlin
- Update Android Gradle Plugin to 8.3.0
- Add support for Kotlin
- Add .gitlab-ci.yml
- Fix some warnings and Manifest
- Update core dependencies to latest version
- Update all libs to latest version
- Do the upgrade path to target Android 14 and do very basic tests
- Bump version


## [0.5.17] - 2021-11-13

### Added

- Partial Chinese (Simplified) translation thanks to Jane Kong ([@Myon](https://hosted.weblate.org/user/Myon/)).

### Changed

- Updated dependencies (appcompat, recyclerview, material, work-runtime).


## [0.5.16] - 2021-07-31

### Added

- \[Advanced\] The "prefixes to keep" parameter (of database filtering) is prefilled (once) based on recent calls.
- Italian translation of the app description thanks to Minibus93 ([@Minibus93](https://hosted.weblate.org/user/Minibus93/)).

### Changed

- Updated Croatian translation thanks to Milo Ivir (@milotype).
- Updated Vietnamese translation thanks to bruh ([@quangtrung02hn16](https://hosted.weblate.org/user/quangtrung02hn16/)).
- Updated Polish translation thanks to Agnieszka C (@Aga-C) and Evo (@verahawk).
- Updated Spanish translation thanks to hiking ([@hiking](https://hosted.weblate.org/user/hiking/)).
- Updated Portuguese (Brazil) translation thanks to Havokdan ([@Havokdan](https://hosted.weblate.org/user/Havokdan/)).
- Updated Hebrew translation thanks to Tzvika ([@shiloni](https://hosted.weblate.org/user/shiloni/)).
- Updated Slovenian translation thanks to bojanopatje (@bojanopatje).

### Fixed

- Fixed a startup crash on [a recent update](https://review.lineageos.org/c/LineageOS/android_packages_providers_ContactsProvider/+/313438/) of LineageOS 15.1-17.1 (introduced in [0.5.7](#057-2020-10-02)).


## [0.5.15] - 2021-06-07

### Added

- \[Advanced\] Options for database filtering to save storage space.
- Vietnamese translation thanks to bruh ([@quangtrung02hn16](https://hosted.weblate.org/user/quangtrung02hn16/)).
- Esperanto translation of the app description thanks to phlostically ([@phlostically](https://hosted.weblate.org/user/phlostically/)).

### Changed

- The Lookup screen number field is prefilled from clipboard.
- The database update procedure is optimized to require less RAM.
- \[Internal\] Updated LibPhoneNumberInfo with some refactoring.
- Updated French, Italian and German translations thanks to J. Lavoie ([@Edanas](https://hosted.weblate.org/user/Edanas/)).
- Updated Portuguese translation thanks to ssantos ([@ssantos](https://hosted.weblate.org/user/ssantos/)).
- Updated Dutch translation thanks to Heimen Stoffels (@Vistaus).
- Updated Turkish translation thanks to Oğuz Ersen (@ersen).
- Updated Esperanto translation thanks to phlostically ([@phlostically](https://hosted.weblate.org/user/phlostically/)).

### Fixed

- Fixed a rare bug where the app could stuck in an update loop.
- Fixed Hebrew translation not being used by Android.


## [0.5.14] - 2021-05-05

### Added

- Esperanto translation thanks to phlostically ([@phlostically](https://hosted.weblate.org/user/phlostically/)).
- Portuguese translation thanks to José ([@lanegan](https://hosted.weblate.org/user/lanegan/)).
- Completed Ukrainian translation thanks to z Z z ([@ruslan.zeezu](https://hosted.weblate.org/user/ruslan.zeezu/)).
- Hebrew translation thanks to Tzvika ([@shiloni](https://hosted.weblate.org/user/shiloni/)).
- Partial Slovenian translation thanks to bojanopatje (@bojanopatje).

### Changed

- Updated Russian translation thanks to Andrey ([@anm](https://hosted.weblate.org/user/anm/)).
- Updated Catalan translation thanks to Raul ([@raulvo](https://hosted.weblate.org/user/raulvo/)).
- Updated Portuguese translations thanks to APL ([@APL](https://hosted.weblate.org/user/APL/)) and Caetano Santos (@caeslucio).
- Updated Italian translation thanks to Lucio Marinelli (@luciomarinelli).
- Updated dependencies (okhttp, conscrypt).


## [0.5.13] - 2021-02-23

### Added

- Catalan translation thanks to Raul ([@raulvo](https://hosted.weblate.org/user/raulvo/)).
- Completed Italian translation thanks to Lucio Marinelli (@luciomarinelli).
- Completed Spanish translation thanks to Raul ([@raulvo](https://hosted.weblate.org/user/raulvo/)).
- Completed Dutch translation thanks to Heimen Stoffels (@Vistaus).

### Changed

- `targetSdkVersion` updated to 30 (Android 11).
- Updated Polish translation thanks to Evo (@verahawk).
- Updated Norwegian Bokmål translation thanks to Allan Nordhøy (@kingu).
- Updated Ukrainian translation thanks to z Z z ([@ruslan.zeezu](https://hosted.weblate.org/user/ruslan.zeezu/)).
- Updated dependencies (recyclerview-selection, material, work-runtime).


## [0.5.12] - 2020-12-16

### Added

- Partial Swedish translation thanks to Kristoffer Grundström (@Umeaboy).
- Partial German translation thanks to Sebastian Kowalak ([@skowalak](https://hosted.weblate.org/user/skowalak/)) and J. Lavoie ([@Edanas](https://hosted.weblate.org/user/Edanas/)).
- Finnish translation thanks to Jussi Timperi ([@Ban3](https://hosted.weblate.org/user/Ban3/)).

### Changed

- Updated Greek translation thanks to Yannis T. (@azakosath).
- Updated French translation thanks to J. Lavoie ([@Edanas](https://hosted.weblate.org/user/Edanas/)).
- Updated Portuguese (Brazil) translation thanks to APL ([@APL](https://hosted.weblate.org/user/APL/)).
- Updated Croatian translation thanks to Milo Ivir (@milotype).

### Fixed

- Fixed a startup crash on Android 11+ (introduced in [0.5.7](#057-2020-10-02)).


## [0.5.11] - 2020-11-08

### Added

- An option to disable notifications for blocked calls on old Android versions.
- Portuguese (Brazil) translation thanks to APL ([@APL](https://hosted.weblate.org/user/APL/)).

### Changed

- Changed blocking behavior in Direct Boot mode: blacklisted numbers are not blocked by default.
  See #22 for details.
- \[Internal\] Settings refactoring.
- Updated Croatian translation thanks to Milo Ivir (@milotype).
- Updated French translation thanks to J. Lavoie ([@Edanas](https://hosted.weblate.org/user/Edanas/)).
- Updated Turkish translation thanks to Oğuz Ersen (@ersen).


## [0.5.10] - 2020-10-26

### Changed

- `ID` and `numberOfCalls` are now optional fields for blacklist import (`pattern` is the only required field now).
  Fields after `pattern` may be omitted.
- Updated French and Italian translations thanks to J. Lavoie ([@Edanas](https://hosted.weblate.org/user/Edanas/)).
- Updated Croatian translation thanks to Milo Ivir (@milotype).
- Updated Greek translation thanks to Yannis T. (@azakosath).
- Updated Polish translation thanks to Evo (@verahawk).

### Fixed

- Fixed importing blacklist files larger than 4000 bytes.
- Fixed occasional crash after deleting blacklist items.


## [0.5.9] - 2020-10-20

### Added

- "Name" from blacklist is displayed in the call log and in the info dialog.
- Separate horizontal layouts for some screens.
- "About" screen.

### Changed

- Improved UX of enabling/disabling the "advanced call blocking mode".
- `targetSdkVersion` updated to 29 (Android 10).
- Updated Turkish translation thanks to Oğuz Ersen (@ersen).
- Updated Polish translation thanks to Evo (@verahawk).

### Fixed

- Added another number MIME-type allowed for pasting on the Lookup screen.


## [0.5.8] - 2020-10-05

### Added

- An option to open notification channel settings from app settings.

### Changed

- Updated Croatian translation thanks to Milo Ivir (@milotype).
- Updated Greek translation thanks to Yannis T. (@azakosath).
- Updated Polish translation thanks to Evo (@verahawk).

### Fixed

- Fixed startup crash without `READ_CALL_LOG` permission (introduced in [0.5.7](#057-2020-10-02)).
- Fixed blacklist editor crash on old Android versions (introduced in [0.5.7](#057-2020-10-02)).


## [0.5.7] - 2020-10-02

### Added

- Call log: infinite scrolling.
- Call log: grouping options.
- Norwegian Bokmål translation of the app description thanks to Allan Nordhøy (@kingu).

### Changed

- Call log: new look.
- Call log: better performance.
- Default to "neutral" community rating if there is no consensus (instead of "unknown").
- Different icon for unknown calls: a question mark instead of thumbs pointing in different directions.
- Updated Turkish translation thanks to Oğuz Ersen (@ersen).


## [0.5.6] - 2020-09-23

### Added

- Button to add review from the lookup screen.
- Turkish translation thanks to Oğuz Ersen (@ersen).

### Changed

- Slightly improved notification content.
- Sort blacklist items by name first, pattern second.
- Updated Greek translation thanks to Yannis T. (@azakosath).
- Updated Polish translation thanks to Evo (@verahawk).


## [0.5.5] - 2020-09-12

### Added

- *Optional* monitoring service (allows YACB to properly [work on Xiaomi's MIUI](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/issues/12)).
- Polish translation thanks to Evo (@verahawk).

### Changed

- Improved hidden number detection in "normal" (non-"advanced") mode (#2).
  The feature is no longer considered experimental.
- Auto-hide "blocked call" notifications (when tapped).
- Fallback to standard TLS-stack if Conscrypt can't be loaded.
- Updated Croatian translation thanks to Milo Ivir (@milotype).
- Updated Norwegian Bokmål translation thanks to Allan Nordhøy (@kingu).


## [0.5.4] - 2020-09-07

### Added

- Support for numbers without international prefix (Android 5+ only) (#15).
- [Frequently Asked Questions](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/blob/master/FAQ.md).
- Option to change primary DB download URL.
- Norwegian Bokmål translation thanks to Allan Nordhøy (@kingu).
- Greek translation thanks to Yannis T. (@azakosath).
- Some Italian translations thanks to Raffaele Cecoro ([@rafscarface](https://hosted.weblate.org/user/rafscarface/)).

### Changed

- Ratings are not displayed if there's no data.
- Updated Croatian translation thanks to Milo Ivir (@milotype).
- Updated Ukrainian translation thanks to Olexandr Nesterenko (@burunduk).
- Some string corrections thanks to Allan Nordhøy (@kingu).

### Fixed

- Crash if database downloading fails.
- Minor navigation issue (activity stack).


## [0.5.3] - 2020-08-19

### Added

- Croatian translation thanks to Milo Ivir (@milotype).

### Fixed

- Crash on the blacklist screen on Android 4 (introduced in [0.5.0](#050-2020-08-07)).


## [0.5.2] - 2020-08-15

### Added

- Tranquille [can be translated on Weblate](https://hosted.weblate.org/engage/yet-another-call-blocker/).

### Changed

- Updated French translation thanks to J. Lavoie ([@Edanas](https://hosted.weblate.org/user/Edanas/)).

### Fixed

- Crash during initial database download on Android 8+ (introduced in [0.4.8](#048-2020-07-04)) (#9).


## [0.5.1] - 2020-08-08

### Added

- Proper number lookup screen.
- Ukrainian translation thanks to [AgeNT2k6](https://4pda.ru/forum/index.php?showuser=1388422).

### Changed

- "Debug activity" is renamed to "DB management" and moved to advanced settings.


## [0.5.0] - 2020-08-07

### Added

- Local blacklist with wildcard support (import of NoPhoneSpam backups is supported) (#1).
- French translation thanks to @Larnicone.

### Changed

- Logcat export now *shares* file instead of *saving* it to storage.


## [0.4.11] - 2020-07-28

### Added

- Experimental support for blocking hidden numbers (#2).


## [0.4.10] - 2020-07-25

### Added

- Dark theme.


## [0.4.9] - 2020-07-18

### Fixed

- A rare crash when call log item has no number.


## [0.4.8] - 2020-07-04

### Added

- Android's "Direct Boot" mode support. Migration on first start may take some time.
- Option to configure the number of recent calls on the main screen.
- Buttons to clear main DB or DB updates.
- Debugging options.

### Changed

- Improved startup time by initializing some subsystems lazily.

### Fixed

- Displaying recent calls with non-operational database.


## [0.4.7] - 2020-06-24

### Added

- Button to add a review (via a web browser).
- \[Advanced\] Options to configure country codes used in network requests.

### Changed

- Names in call log are ellipsized.
- Requests to the 3rd-party servers use custom headers.


## [0.4.6] - 2020-06-21

### Added

- ["Advanced call blocking mode"](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/blob/master/FAQ.md#whats-that-advanced-call-blocking-mode).

### Changed

- Moved some code into [a library](https://gitlab.com/xynngh/LibPhoneNumberInfo).

### Fixed

- Initial database downloading via Tor.


## [0.4.5] - 2020-06-14

### Changed

- Missed and rejected calls now have different icons.
- An error is displayed if online reviews can't be loaded.
- [Conscrypt](https://github.com/google/conscrypt) is used as a TLS security provider.

### Fixed

- Opening the info dialog when database is not operational.
- Added missing `FOREGROUND_SERVICE` permission.
- Some compatibility issues (crashes) with older Android versions.
- Some navigation issues (activity stack).


## [0.4.4] - 2020-06-02

### Added

- Spanish translation thanks to Diego Sanguinetti (@sguinetti).

### Changed

- Frequent database update checks are done only on Android 6+.


## [0.4.3] - 2020-05-28

### Changed

- Action to load online reviews is not displayed in notifications for contacts.

### Fixed

- Notification actions (the info dialog / online reviews activity being sometimes opened for a wrong number).


## [0.4.2] - 2020-05-23

### Added

- Proper settings screen.
- Options to disable some notifications on older Android versions.
- Dutch translation thanks to Heimen Stoffels (@Vistaus).

### Fixed

- Incoming call notifications not being removed on some devices.


## [0.4.1] - 2020-05-11

### Added

- New app icon.
- Number category is displayed in the info dialog.

### Changed

- Changed icon and color of "blocked call" notification.


## [0.4.0] - 2020-05-08

### Added

- Recent calls are now displayed on the main screen.
- An option for special treatment of contacts (never blocked, name is displayed).
- An info dialog to show extra info about a number (available from notifications and main screen).
- A confirmation before loading online reviews for contacts.
- A message about denied permissions.
- More frequent database auto-updates on unmetered networks.
- Database info on debug screen.

### Changed

- New color theme.
- Call blocking no longer depends on notifications being enabled.
- Settings are moved to menu.

### Fixed

- An issue with notification icons on some devices.


## [0.3.4] - 2020-05-02

### Fixed

- Notifications no longer use the "call" category.
- The `ANSWER_PHONE_CALLS` permission is not requested on Android 8.


## [0.3.3] - 2020-01-16

### Added

- Proper call blocking on Android 9+.

### Fixed

- Handling of new calls during active calls.


## [0.3.2] - 2019-12-01

### Added

- Call blocking on Android 9 using a system permission.

### Fixed

- Notification channels are recreated on every startup to improve localization.


## [0.3.1] - 2019-10-15

### Added

- Fastlane metadata.


## [0.3.0] - 2019-09-07

### Added

- Support for downloading the main database in the app if it's not embedded.


## [0.2.0] - 2019-09-01

### Added

- Support for altered database format.


## [0.1.0] - 2019-08-31

### Added

- Call blocking.
- Database auto-update.
- Option to disable informational notifications.


## [0.0.1] - 2019-03-25

### Added

- Informational notifications.
- Loading online reviews.
- Offline database with manual updates.


[unreleased]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.17...master
[0.5.17]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.16...v0.5.17
[0.5.16]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.15...v0.5.16
[0.5.15]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.14...v0.5.15
[0.5.14]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.13...v0.5.14
[0.5.13]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.12...v0.5.13
[0.5.12]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.11...v0.5.12
[0.5.11]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.10...v0.5.11
[0.5.10]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.9...v0.5.10
[0.5.9]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.8...v0.5.9
[0.5.8]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.7...v0.5.8
[0.5.7]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.6...v0.5.7
[0.5.6]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.5...v0.5.6
[0.5.5]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.4...v0.5.5
[0.5.4]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.3...v0.5.4
[0.5.3]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.2...v0.5.3
[0.5.2]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.1...v0.5.2
[0.5.1]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.5.0...v0.5.1
[0.5.0]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.11...v0.5.0
[0.4.11]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.10...v0.4.11
[0.4.10]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.9...v0.4.10
[0.4.9]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.8...v0.4.9
[0.4.8]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.7...v0.4.8
[0.4.7]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.6...v0.4.7
[0.4.6]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.5...v0.4.6
[0.4.5]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.4...v0.4.5
[0.4.4]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.3...v0.4.4
[0.4.3]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.2...v0.4.3
[0.4.2]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.1...v0.4.2
[0.4.1]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.4.0...v0.4.1
[0.4.0]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.3.4...v0.4.0
[0.3.4]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.3.3...v0.3.4
[0.3.3]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.3.2...v0.3.3
[0.3.2]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.3.1...v0.3.2
[0.3.1]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.3.0...v0.3.1
[0.3.0]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.2.0...v0.3.0
[0.2.0]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/v0.1.0...v0.2.0
[0.1.0]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/9818bc323b...v0.1.0
[0.0.1]: https://gitlab.com/xynngh/YetAnotherCallBlocker/-/compare/c957a0c4d8...9818bc323b
