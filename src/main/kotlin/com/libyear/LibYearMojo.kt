package com.libyear

import org.apache.maven.model.Dependency
import org.apache.maven.plugins.annotations.Mojo
import org.codehaus.mojo.versions.AbstractVersionsUpdaterMojo
import org.codehaus.mojo.versions.api.ArtifactVersions
import org.codehaus.mojo.versions.api.UpdateScope
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader

@Mojo(name = "run")
class LibYearMojo: AbstractVersionsUpdaterMojo() {
    override fun execute() {
        logResult(retrieveDependenciesWithUpdates())
    }

    private fun retrieveDependenciesWithUpdates(): List<DependencyWithUpdate> {
        return helper.lookupDependenciesUpdates(project.dependencies.toMutableSet(), false)
            .mapNotNull { (artifact, versions) ->
                DependencyWithUpdate(
                    artifact.description(),
                    artifact.version,
                    versions.latest(),
                    versions.diffWithCurrentVersion()
                )
            }
            .filter { it.versionDiff.any > 0 }
    }

    private fun logResult(dependenciesWithUpdates: List<DependencyWithUpdate>) {
        if (dependenciesWithUpdates.any()) {
            log.info("The following dependencies have newer versions:")
            dependenciesWithUpdates.forEach { log.info(it.toString()) }
            log.info(dependenciesWithUpdates.aggReport())
        } else {
            log.info("No dependency updates found. WELL DONE !!!")
        }
    }

    //region extensions

    private fun Dependency?.description(): String = "${this?.groupId}:${this?.artifactId}"
    private fun ArtifactVersions?.latest(): String =
        this?.getNewestUpdate(currentVersion, UpdateScope.ANY).toString()

    private fun ArtifactVersions?.diffWithCurrentVersion(): VersionDiff =
        VersionDiff(
            this.diffWithCurrentVersion(UpdateScope.ANY),
            this.diffWithCurrentVersion(UpdateScope.MAJOR),
            this.diffWithCurrentVersion(UpdateScope.MINOR),
            this.diffWithCurrentVersion(UpdateScope.INCREMENTAL) + this.diffWithCurrentVersion(UpdateScope.SUBINCREMENTAL),
        )

    private fun ArtifactVersions?.diffWithCurrentVersion(scope: UpdateScope): Int =
        this?.getAllUpdates(currentVersion, scope)?.size!!

    private fun List<DependencyWithUpdate>.aggReport(): String =
        "> ${sumOf { it.versionDiff.any }} versions behind for $size dependencies" +
                "-> ${sumOf { it.versionDiff.major }} major(s) | ${sumOf { it.versionDiff.minor }} minor(s) | ${sumOf { it.versionDiff.fixes }} fix(es)"

    //endregion

    private data class VersionDiff(val any: Int, val major: Int, val minor: Int, val fixes: Int) {
        override fun toString(): String {
            return "$any version(s) behind -> $major major(s) | $minor minor(s) | $fixes fix(es)"
        }
    }

    private data class DependencyWithUpdate(
        val name: String,
        val projectVersion: String,
        val latestVersion: String,
        val versionDiff: VersionDiff
    ) {
        override fun toString(): String =
            "${this.name} ${this.projectVersion} -> ${this.latestVersion} (${this.versionDiff})"
    }

    override fun update(pom: ModifiedPomXMLEventReader?) {
        TODO("Not yet implemented")
    }
}