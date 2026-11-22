# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.9.0 - 2026-11-22

### Added

- Add qBittorrent downloads from magnet links, URLs, or `.torrent` files. Android can open magnet links and `.torrent` files directly in Subspace.
- View torrent details, files, trackers, and peers. Set individual file priorities, force a recheck, or delete a torrent with the option to remove its downloaded files.
- Show server-wide transfer speeds and toggle qBittorrent's alternative speed limits from the home screen.
- Add settings for theme, transfer units, automatic refresh interval, Wi-Fi-only refresh, and delete confirmation.

### Changed

- Improve torrent status labels and filters, including support for the stopped states used by newer qBittorrent versions. Refresh categories and tags from the filter menu.
- Run periodic refresh while the torrent list is active and respect the Wi-Fi-only setting on metered connections.
- Update Gradle to 9.9.1, Android Gradle Plugin to 9.5.2, Material 3 to 1.5.0-rc01, and Navigation 3 to 1.2.1.

### Fixed

- Improve qBittorrent session recovery when concurrent requests encounter an expired cookie.
- Improve the crash recovery screen and how the app restarts after a crash.

## 0.8.0 - 2026-10-22

- Bump Gradle to 9.8.2, AGP to 9.4.1, Compose BOM to 2026.09.00, target SDK to 37, and migrate to Gradle version catalog;
- Add auto periodical refresh and network connectivity monitoring;
- Add toggle torrent state;
- Improve paging anchor calculation and error handling.

## 0.7.1 - 2025-05-10

- Fix crash on boot

## 0.7.0 - 2025-05-09

- Bump AGP to 8.10.0, compose lib to 2025.05.00 and other libs to latest;
- Add account edition and deletion
- Add qb version check
- Add filter with category, status and tag
- Fix loop api call with pagination
- Fix multiple account cookie issue
- Fix some minor UI issues

## 0.6.0 - 2025-02-22

- Bump AGP to 8.8.1 and compose lib to 2025.02.00;
- Bump Java from 17 to 21.

## 0.5.0 - 2025-01-14

- Bump AGP to 8.8.0 and compose lib to 2024.12.01;
- Update target SDK to 35.

## 0.4.0 - 2024-07-24

- Bump AGP to 8.5.1, gradlew to 8.9;
- Bump Kotlin to 2.0;

## 0.3.0 - 2024-05-31

- rewrote paging with jetpack paging 3;
- db schema changes.

## 0.2.2 - 2024-05-12

List UI tweak

## 0.2.0 - 2024-05-02

### Fixes

- Fixed [loading issue](https://github.com/kid1412621/subspace/issues/6)

### Updates

- Bump compose lib version to 2024.05.00

## 0.1.3 - 2024-04-30

### Fixes

- Fixed drawerSheet round corner issue
- Fixed edgeToEdge when no bottomBar

## 0.1.2 - 2024-04-25

### Fixes

- Fixed showing bottom bar when no account

## 0.1.1 - 2024-04-25

### Updates

- Bump compose lib version to 2024.04.01

## 0.1.0 - 2024-04-24

- Initial release