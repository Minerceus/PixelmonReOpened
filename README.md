# HOW TO SET UP
- Download the version of reforged you want to use
- Put the com folder into another jar file and call it `SourceOnly.jar` and put it in the project directory
- Uncomment the line marked with IMPORTANT in the `build.gradle.kts`
- Go into your IntelliJ's External Libraries and search for the `SourceOnly` library and go to the jar file
- Paste the jar file into the projects main folder and name it `SourceOnlyRemapped.jar`
- Run `gradle decompile`
- Run `gradle setup`
- Run `gradle applyPatches`