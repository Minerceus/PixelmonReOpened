pluginManagement {
    // when using additional gradle plugins like shadow,
    // add their repositories to this list!
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        gradlePluginPortal()
    }
}

rootProject.name = "PixelmonReOpened"