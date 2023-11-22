# GEM Droid
An Android app implementing Blue Ventures GEM.

Includes a submodule "DroidBones" that is some shared open source code.

### Firebase

You will need `google-services.json` from Firebase added here:

`gemdroid/app/google-services.json`

This file is excluded from github for privacy/security reasons.

It can be found in the Firebase console:

Gear Icon (Settings) > scroll down to Your Apps > Gem Droid


In the firebase console for this app, you must add the SHA-1/SHA-256 fingerprints used to sign
the app for any build that intends to make use of Google APIs. Currently, the SHAs listed there
inlcude my personal SHA-1 for signing debug builds and both the SHA-1 and SHA-256 of the
public certfificate for the app signing key that Goolge uses to sign each of our releases.

### local.properties

We have some secrets in `local.properties` at the root of the project. It is not checked in to git.

A Google Maps API key and our AnyChart license are entered in `local.properties`:

`MAPS_API_KEY=xyz`

`ANYCHART_LICENSE=xyz`

These secrets are also in our Secret Manager in Google Cloud. They are referenced in the
`AndroidManifest.xml` as `meta-data` tags on the `application`. Pulling the values out of
`local.properties` and filling them into the manifest is done by the secrets gradle plugin that
is part of the maps SDK:

https://developers.google.com/maps/documentation/android-sdk/config

## Building:

Before building, in `gemdroid/app/build.gradle` you'll need to bump the `versionCode` by one and update the `versionName`, which is a [semantic versioning string][semver], and commit these changes. Then you can tag that commit in git: `git tag v1.X.Y` and push that tag up to github: `git push --tags`.

Please also create a release in github, and add the `.aab` app bundle to it, providing relevant release notes. The release notes should also be pasted into the Play Console.

For release builds, we use [app bundles][app-bundle] (rather than apks) and sign them with an [upload key][app-signing].

The upload key for gemdroid, as well as the password necessary for using it (same password for both key store and key when bundling), all live in the Google Cloud Secret Manager.

You can most easily [generate a release build through Android Studio][app-signing].

### On the command line

You can build using the `gradlew` wrapper that comes with the project. OpenJDK also comes bundled up with Android Studio when you install it, so you can use that for running builds rather than installing java on your system, if you prefer. If you choose to do that, JAVA_HOME is probably not set in your environment. The location of the JDK in use by Android Studio can be found by looking in `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK` within Android Studio.

#### See available tasks
`JAVA_HOME=/home/courtf/.jdks/corretto-17.0.8.1 gradlew tasks`

#### Build app bundle (`.aab`)
`JAVA_HOME=/home/courtf/.jdks/corretto-17.0.8.1 gradlew bundle`

The app bundle will be built, and the release version will end up in `gemdroid/app/build/outputs/bundle/release`. You still need to [sign the app][sign-cmdline].

As described at the link above, signing the app requires a key (already generated, you don't need to use `keytool`) and `jarsigner`. `jarsigner` (and `keytool`) comes with Android Studio: `/opt/android-studio/jre/bin/jarsigner`. The command for signing with jarsigner looks something like this (untested): `jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore upload-keystore.jks app.aab upload`. You should be prompted for at least one of the Secret Manager-stored passwords during this process (confirmaton needed).

[semver]: https://semver.org/
[app-bundle]: https://developer.android.com/guide/app-bundle/
[app-signing]: https://developer.android.com/studio/publish/app-signing
[sign-cmdline]: https://developer.android.com/studio/build/building-cmdline#sign_cmdline
[releasing]: https://support.google.com/googleplay/android-developer/answer/7159011?hl=en
