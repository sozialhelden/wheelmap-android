fastlane documentation
================
# Installation
```
sudo gem install fastlane
```
# Available Actions
## Android
### android building_pr_phase
```
fastlane android building_pr_phase
```
This lane is running during the building stage of a Pull Request

Clean, build the development variant and run the unit tests
### android releasing_pr_phase
```
fastlane android releasing_pr_phase
```
This lane is running during the releasing stage of a Pull Request

Clean, build and release on HockeyApp with the specified build variant

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [https://fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [GitHub](https://github.com/fastlane/fastlane/tree/master/fastlane).
