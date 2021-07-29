package com.libyear

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.apache.maven.model.Dependency
import org.codehaus.mojo.versions.api.ArtifactVersions
import org.codehaus.mojo.versions.api.UpdateScope
import java.io.File

typealias Dependencies = List<DependencyWithUpdate>

fun Dependency?.description() = "${this?.groupId}:${this?.artifactId}"
fun ArtifactVersions?.latest() = this?.getNewestUpdate(currentVersion, UpdateScope.ANY).toString()

fun ArtifactVersions?.diffWithCurrentVersion() =
    VersionDiff(
        this.diffWithCurrentVersion(UpdateScope.ANY),
        this.diffWithCurrentVersion(UpdateScope.MAJOR),
        this.diffWithCurrentVersion(UpdateScope.MINOR),
        this.diffWithCurrentVersion(UpdateScope.INCREMENTAL) + this.diffWithCurrentVersion(UpdateScope.SUBINCREMENTAL),
    )

fun ArtifactVersions?.diffWithCurrentVersion(scope: UpdateScope): Int =
    this?.getAllUpdates(currentVersion, scope)?.size!!

fun Dependencies.aggReport() =
    "> ${sumOf { it.versionDiff.any }} versions behind for $size dependencies" +
            " -> ${sumOf { it.versionDiff.major }} major(s) | ${sumOf { it.versionDiff.minor }} minor(s) | ${sumOf { it.versionDiff.fixes }} fix(es)"

fun Dependencies.toReport() = Json.encodeToString(this)

fun Dependencies.logResult(log: (info: String) -> Unit): Dependencies {
    when {
        this.isEmpty() -> log("No dependency updates found. WELL DONE !!!")
        else -> {
            log("The following dependencies have newer versions:")
            forEach { log(it.toString()) }
            log(aggReport())
        }
    }
    return this
}

//region File management
fun File.create(): File {
    FileUtils.cleanDirectory(this)
    FileUtils.forceMkdir(this)
    return this
}

fun File?.saveToFile(fileName: String, content: String) {
    FileUtils.writeStringToFile(
        File(this, fileName),
        content,
        Charsets.UTF_8
    )
}
//endregion