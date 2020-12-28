# MemfaultCloud Android Changelog

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
