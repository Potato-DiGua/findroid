import java.util.Properties
import java.io.FileInputStream
import java.io.FileWriter

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion = "1.5.31"
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        google()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
        val navVersion = "2.3.5"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")

        val hiltVersion = "2.38.1"
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")

        val aboutLibrariesVersion = "8.9.1"
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:$aboutLibrariesVersion")
    }
}

allprojects {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        google()
        mavenCentral()
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}

fun cmdExecute(cmd: String) {
    println("\n执行$cmd")
    println(Runtime.getRuntime().exec(cmd))
}

tasks.create("upgradeVersion") {
    group = "help"
    description = "构建新版本"
    doLast {
        println("---自动升级版本号---\n")
        val versionProps = Properties()
        val versionPropsFile = rootProject.file("version.properties")
        if (versionPropsFile.exists()) {
            versionProps.load(FileInputStream(versionPropsFile))
        }
        val oldVersionCode = versionProps.getProperty("versionCode")
        val oldVersionName = versionProps.getProperty("versionName")
        if (oldVersionCode == null || oldVersionName == null ||
            oldVersionCode.isEmpty() || oldVersionName.isEmpty()
        ) {
            println("error:版本号不能为空")
            return@doLast
        }

        versionProps.setProperty("versionCode", (oldVersionCode.toInt() + 1).toString())

        versionProps.setProperty(
            "versionName", oldVersionName.substring(0, oldVersionName.lastIndexOf('.') + 1) +
                    (oldVersionName.substring(oldVersionName.lastIndexOf('.') + 1).toInt() + 1)
        )
        val tip =
            "版本号从$oldVersionName($oldVersionCode)升级到${versionProps.getProperty("versionName")}(${versionProps.getProperty("versionCode")})"
        println(tip)

        val writer = FileWriter(versionPropsFile)
        versionProps.store(writer, null)
        writer.flush()
        writer.close()
        val tag = "v${versionProps.getProperty("versionName")}"
        cmdExecute("git pull")
        cmdExecute("git add version.properties")
        cmdExecute("git commit -m \"版本号升级为：$tag\"")
        cmdExecute("git push origin")
        cmdExecute("git tag $tag")
        cmdExecute("git push origin $tag")
    }
}