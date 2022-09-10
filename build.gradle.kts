import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP

plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = ofProp("package_group", "")
version = ofProp("plugin_version", "")

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

repositories {
    mavenCentral()
    maven {
        name = "paper"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

val spigotVersion = ofProp("spigot")

dependencies {
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = spigotVersion)
    testImplementation("junit", "junit", "4.12")
}

bukkit {
    author = "WhiteCat"
    description = ofProp("plugin_description", "")
    main = ofProp("plugin_main_class", "")
            .replace("\${group}", "$group", true)
            .replace("\${name}", ofProp("plugin_name", ""), true)
    apiVersion = "1.13"

    permissions {
        register("superspawnpoint.command.superspawnpoint") {
            default = OP
        }
    }
    commands {
        register("superspawnpoint") {
            permission = "superspawnpoint.command.superspawnpoint"
            usage = "/<command> <player_name> <world> <x> <y> <z>"
        }
    }
}

infix fun <A> A.toProp(name: String) = this to extra[name]
fun String.suffixIfNot(suffix: String) = if (this.endsWith(suffix)) this else "$this$suffix"
fun ofProp(propName: String, suffix: String = "_version", default: String = ""): String =
        extra[propName.suffixIfNot(suffix)] as? String ?: default

fun String.propVer(propName: String = split(":").last()) =
        "$this:${ofProp(propName)}"

fun ExternalModuleDependency.propVersion(propName: String) = version { prefer(ofProp(propName)) }
