## MemfaultCloud Android

The [MemfaultCloud](https://memfault.github.io/memfault-cloud-android/) library
provides convenience APIs for mobile applications that interact with Memfault's
web services.

### Sample App

The `sample/` directory contains a very basic Android application that
demonstrates the functionality of this library. Feedback from user interactions
is logged to LogCat and displayed as Toasts.

Before building the app, make sure to update the Project key in
`SampleViewModel.kt`.

### Adding the SDK to your project

Add the library to your application's `build.gradle`:

```groovy
dependencies {
  implementation 'com.memfault.cloud:cloud-android:2.0.5'
}
```

The artifact is hosted on the
[Maven Central Repository](https://mvnrepository.com/repos/central), so you may
need to add `mavenCentral()` to your list of project repositories. See the
sample app's root `build.gradle` for an example.

```groovy
repositories {
   mavenCentral()
   google()
}
```

#### Initialization

The
[MemfaultCloud](https://memfault.github.io/memfault-cloud-android/com.memfault.cloud.sdk/-memfault-cloud/index.html)
class is the main entry point for the `MemfaultCloud` SDK. We recommend creating
only one `MemfaultCloud` instance and using it across your entire application.

#### Getting the latest release

Use the
[memfaultCloud.getLatestRelease](https://memfault.github.io/memfault-cloud-android/com.memfault.cloud.sdk/-memfault-cloud/get-latest-release.html)
API to query Memfault's services to find if a new update is available for a
device.

#### Uploading Chunks

The Memfault Firmware SDK packetizes data to be sent to Memfault's cloud into
"chunks". See
[this tutorial for details](https://docs.memfault.com/docs/mcu/data-from-firmware-to-the-cloud).

Once you have established sending chunks from your device to the Android app,
use the
[ChunkSender](https://memfault.github.io/memfault-cloud-android/com.memfault.cloud.sdk/-chunk-sender/index.html)
API to upload these chunks to Memfault.

### Acknowledgements

We used Square's excellent [OKHTTP](https://square.github.io/okhttp/) library as
a reference when implementing this SDK's HTTP code.
