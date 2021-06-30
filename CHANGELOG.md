# MemfaultCloud Android Changelog

## v2.0.2 - June 30, 2021

#### :chart_with_upwards_trend: Improvements

- Fixed a crash when using `ChunkSender`.

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
