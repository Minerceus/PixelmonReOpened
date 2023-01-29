import uk.jamierocks.propatcher.task.ApplyPatchesTask
import uk.jamierocks.propatcher.task.MakePatchesTask
import uk.jamierocks.propatcher.task.ResetSourcesTask
import java.text.SimpleDateFormat
import java.util.*

plugins {
    java
    id("dev.architectury.loom") version "0.12.0-SNAPSHOT"
    id("uk.jamierocks.propatcher") version "2.0.1" apply false
    id("com.dorongold.task-tree") version "1.5"
}

version = project.properties["mod_version"]!!
group = project.properties["maven_group"]!!
val minecraftVersion = project.properties["minecraft_version"]
val forgeVersion = project.properties["forge_version"]
val modId = project.properties["mod_id"]
val modAuthor = project.properties["mod_author"]

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/snapshot")
    maven("https://jitpack.io")
    maven("https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

val decompiler by configurations.creating

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:${forgeVersion}")

    modCompileOnly("curse.maven:JourneyMap-32274:4012858")
    implementation("com.github.Vatuu:discord-rpc:1.6.2")
    implementation("io.leangen.geantyref:geantyref:1.3.13")
    implementation("org.yaml:snakeyaml:1.33")
    implementation("org.spongepowered:configurate-yaml:4.0.0")

    // FIXME: IMPORTANT. USE THIS TO REMAP YOUR PIXELMON JAR BEFORE YOU USE IT
    // modImplementation(files("SourceOnly.jar"))

    decompiler("org.quiltmc:quiltflower:1.10.0-SNAPSHOT")
}

val resetSources by tasks.registering(ResetSourcesTask::class) {
    rootDir = rootProject.file(".gradle/decompiled/sources")
    target = file("src/main/java")
}

val applySourcePatches by tasks.registering(ApplyPatchesTask::class) {
    dependsOn(resetSources)

    target = file("src/main/java")
    patches = file("patches/java")
}

val makeSourcePatches by tasks.registering(MakePatchesTask::class) {
    rootDir = rootProject.file(".gradle/decompiled/sources")
    target = file("src/main/java")
    patches = file("patches/java")
}

val resetResources by tasks.registering(ResetSourcesTask::class) {
    rootDir = rootProject.file(".gradle/decompiled/resources")
    target = file("src/main/resources")
}

val applyResourcePatches by tasks.registering(ApplyPatchesTask::class) {
    dependsOn(resetResources)

    target = file("src/main/resources")
    patches = file("patches/resources")

}

val makeResourcePatches by tasks.registering(MakePatchesTask::class) {
    rootDir = rootProject.file(".gradle/decompiled/resources")
    target = file("src/main/resources")
    patches = file("patches/resources")
}

val reset by tasks.registering {
    dependsOn(resetSources, resetResources)
}

val apply by tasks.registering {
    dependsOn(applySourcePatches, applyResourcePatches)
}

val makePatches by tasks.registering {
    dependsOn(makeSourcePatches, makeResourcePatches)
}

val decompile by tasks.registering {
    doLast {
        File(".gradle/decompiled").mkdirs()
        File("build/decompiled").mkdirs()

        logger.info("Decompiling")

        javaexec {
            main = "org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler"
            classpath(decompiler)
            args("-din=1", "-dgs=1", "-asc=1", "-rsy=1", "-ind=    ")
            args("SourceOnlyRemapped.jar", "build/decompiled/PixelmonSourceDecomp.jar")
        }
    }
}

val setup by tasks.registering {
    doLast {
        if(!rootProject.file("build/decompiled/PixelmonSourceDecomp.jar").exists()) {
            logger.error("Please run the decompile task first")
        } else {
            logger.warn("Copying sources")

            copy {
                includeEmptyDirs = false
                include("com/pixelmonmod/**/*.java")
                from(zipTree("build/decompiled/PixelmonSourceDecomp.jar"))
                into(rootProject.file(".gradle/decompiled/sources"))
            }

            logger.warn("Copying resources")

            copy {
                includeEmptyDirs = false
                include("assets/**")
                include("data/**")
                include("pack.mcmeta")
                include("META-INF/accesstransformer.cfg")
                include("META-INF/mods.toml")
                from(zipTree("Pixelmon.jar"))
                into(rootProject.file(".gradle/decompiled/resources"))
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "8"
    targetCompatibility = "8"

    if (JavaVersion.current().isJava9Compatible) logger.info("This build might not be compatible with Java 8")
}

java {
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Specification-Title" to modId,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to modAuthor,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        ))
    }
}