# MemfaultCloud Android Changelog

## v2.1.0 - December 19, 2025

#### :rocket: Major Update - Modernization

- **Updated Android Gradle Plugin from 4.2.2 to 8.7.3** - Now compatible with AGP 8.x and Kotlin 2.x
- **Updated Kotlin from 1.5.20 to 2.1.0** - Full support for Kotlin 2.x language features
- **Updated Gradle from 6.7.1 to 9.2.1** - Required for AGP 8.x support
- **Updated compileSdk from 30 to 35** - Android 15 support
- **Updated minSdk from 18 to 21** - Required by modern AndroidX libraries (Android 5.0+)
- **Modernized all AndroidX dependencies**:
  - appcompat: 1.3.0 → 1.7.0
  - core-ktx: 1.6.0 → 1.15.0
  - constraintlayout: 2.0.4 → 2.2.0
  - lifecycle-viewmodel-ktx: 2.3.1 → 2.8.7
  - okio: 2.10.0 → 3.9.1
  - leakcanary: 2.7 → 2.14
  - mockk: 1.12.0 → 1.13.13
- **Added namespace declarations to build.gradle** (required by AGP 8.x)
- **Removed package attributes from AndroidManifest.xml** (migrated to namespace in build.gradle)

This major update ensures compatibility with modern Android development tools and resolves build failures when using AGP 8.x and Kotlin 2.x.

## v2.0.5 - June 6, 2023

#### :chart_with_upwards_trend: Improvements

- Differentiate between the release's and OTA artifact's `extra_info`.

## v2.0.4 - June 14, 2022

#### :chart_with_upwards_trend: Improvements

- Expose `is_forced` for OTA artifacts.

## v2.0.3 - August 12, 2021

#### :chart_with_upwards_trend: Improvements

- Expose `extra_info` for OTA artifacts.

## v2.0.2 - June 30, 2021

#### :chart_with_upwards_trend: Improvements

- Fixed a crash when using `ChunkSender`.
- Expose the MD5 for OTA artifacts.

## v2.0.1 - May 3, 2021

#### :chart_with_upwards_trend: Improvements

- Fixed a crash when checking for Android OTA updates using `getLatestRelease`.

## v2.0.0 - December 28, 2020

#### :boom: Breaking Changes

- The `SendChunksCallback` interface has been simplified by merging the
  `onError` and `onRetryAfterDelay` into a single callback. The rationale is
  that the client needs to implement the same behavior for both callbacks:
  schedule a job that calls `chunkSender.send()` after a delay.
- `chunkSender.send()` will actively check whether the requested delay from the
  `onRetryAfterDelay` callback is respected by the client code, to avoid
  hammering Memfault's servers.

#### :chart_with_upwards_trend: Improvements

- Added logic to exponentially back-off chunk upload requests in case of errors.
- Increased the HTTP chunk upload timeout from 30 seconds to 60 seconds. This
  should help in scenarios where many chunks are batched into a single request
  and/or the throughput of the network is poor.
- Fixed a bug where the `SendChunksCallback.onQueueEmpty` was always getting
  called after each successful request, regardless of whether the queue was
  empty or not. At the same time, when `chunkSender.send()` would be called
  while the queue is empty, the `SendChunksCallback.onQueueEmpty` would _not_
  get called.
- Re-formatted the code using ktlint.

## v1.0.3 - December 28, 2020

#### :chart_with_upwards_trend: Improvements

- Improves SDK performance by increasing HTTP client request timeouts and
  decreasing the number of chunks that can be uploaded in a single request
- Bump gradle plugin version

## v1.0.2 - September 25, 2020

#### :chart_with_upwards_trend: Improvements

- Changes the default ingress URL to point to `https://chunks.memfault.com`

## v1.0.1 - September 17, 2020

#### :chart_with_upwards_trend: Improvements

- Fixes an issue where the HTTP client's response body may be closed prematurely
- Migrated the configuration of the sample app to gradle properties
- Bump gradle plugin version

## v1.0.0 - March 25, 2020

- Initial release
