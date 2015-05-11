Change Log
==========

Version 1.2.0 *(In Development)*
--------------------------------

 * New: Support for the 1.2.x Android plugin.
 * New: Download r24.2 Android SDK.


Version 0.12.0 *(2014-07-12)*
-----------------------------

 * New: Support for the 0.12.x Android plugin.
 * New: Download r23 Android SDK.


Version 0.10.1 *(2014-04-05)*
-----------------------------

 * Fix: Correct missing method error related to benchmarking.


Version 0.10.0 *(2014-04-05)*
-----------------------------

 * New: Support for the 0.10.x Android plugin.


Version 0.9.1 *(2014-04-10)*
----------------------------

 * New: If `ANDROID_HOME` is set but the directory does not exist, the SDK will be downloaded and
   extracted to its location.
 * New: Support for projects which compile against Google API.
 * New: Platform tools will be downloaded if missing.
 * Fix: Projects applying this plugin but not the 'android' or 'android-library' will no longer
   error.
 * Fix: Update 'jarchivelib' to preserve file permissions (most notably: the executable bit) when
   extracting the SDK archive.
 * Fix: Correctly handle a `local.properties` file that is present but lacks the `sdk.dir` property.


Version 0.9.0 *(2014-03-30)*
----------------------------

Initial version for the 0.9.x Android plugin.
