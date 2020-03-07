## MemfaultCloud Android

The `MemfaultCloud` library provides convenience APIs for mobile applications
that interact with Memfault's web services.

### Sample App

The `sample/` directory contains a very basic Android application that
demonstrates the functionality of this library.

Before building the app, make sure to update the Project API key in
`SampleViewModel.kt`.

### Adding the SDK to your project

Add the library to your application's `build.gradle`:

```groovy
dependencies {
  implementation 'com.memfault.cloud.sdk:1.0.0-RC1'
}
```

#### Initialization

The [MemfaultApi](com.memfault.cloud.sdk/-memfault-api/index.html) class is the
main entry point for the `MemfaultCloud` SDK. We recommend creating only one
`MemfaultApi` instance and using it across your entire application.

#### Getting the latest release

Use the
[memfaultApi.getLatestRelease](com.memfault.cloud.sdk/-memfault-api/get-latest-release.html)
API to query Memfault's services to find if a new update is available for a
device.

#### Uploading Chunks

The Memfault Firmware SDK packetizes data to be sent to Memfault's cloud into
"chunks". See
[this tutorial for details](https://docs.memfault.com/docs/embedded/data-from-firmware-to-the-cloud).

Once you have established sending chunks from your device to the Android app,
use the
[MemfaultChunkSender](com.memfault.cloud.sdk/-memfault-chunk-sender/index.html)
API to upload these chunks to Memfault.

### Acknowledgements

We used Square's excellent [OKHTTP](https://square.github.io/okhttp/) library as
a reference when implementing this SDK's HTTP code.
