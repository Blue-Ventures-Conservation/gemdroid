# GEM Droid
An Android app implementing Blue Ventures GEM.

Includes a submodule "DroidBones" that is some shared open source code.

### local.properties

We have some secrets in `local.properties` at the root of the project. It is not checked in to git.

A Google Maps API key and our AnyChart license are entered in `local.properties`:

`MAPS_API_KEY=xyz`

`ANYCHART_LICENSE=xyz`

These secrets are also in our Secret Manager in Goole Cloud. They are referenced in the
`AndroidManifest.xml` as `meta-data` tags on the `application`. Pulling the values out of
`local.properties` and filling them into the manifest is done by the secrets gradle plugin that
is part of the maps SDK:

https://developers.google.com/maps/documentation/android-sdk/config

### Building:

You will need `google-services.json` from Firebase added here:

`gemdroid/app/google-services.json`

This file is excluded from github for privacy/security reasons.

It can be found in the Firebase console:

Gear Icon (Settings) > scroll down to Your Apps > Gem Droid
