Child Projects
==============

This acceptance test fixture is a project with two child modules.

The root `build.gradle` applies the SDK manager plugin to all projects. One child compiles against
the latest Android API and has a dependency on the support library. The other compiles against the
latest Google API and has a dependency on the Google Play Services library. To make it interesting,
each child depends on a different version of the build tools as well.

The SDK is initialized as completely empty.


Expectations
------------

 * Both build tools downloaded
 * Android API downloaded
 * Google API downloaded
 * Support repository downloaded
 * Google Play repository downloaded
