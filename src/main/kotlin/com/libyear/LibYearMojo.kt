package com.libyear

import org.apache.maven.model.Dependency
import org.apache.maven.plugins.annotations.Mojo
import org.codehaus.mojo.versions.AbstractVersionsUpdaterMojo
import org.codehaus.mojo.versions.api.ArtifactVersions
import org.codehaus.mojo.versions.api.UpdateScope
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader

@Mojo(name = "run")
class LibYearMojo: AbstractVersionsUpdaterMojo() {
    private fun Dependency?.description(): String = "${this?.groupId}:${this?.artifactId}"
    private fun ArtifactVersions?.latest(): String = this?.getNewestUpdate(this.currentVersion, UpdateScope.ANY).toString()
    private fun ArtifactVersions?.diffWithCurrentVersion(): Int = this?.getAllUpdates(this.currentVersion, UpdateScope.ANY)?.size !!

    private data class DependencyWithUpdate(
        val name: String,
        val projectVersion: String,
        val latestVersion: String,
        val versionDiff: Int) {
        override fun toString(): String = "${this.name} ${this.projectVersion} -> ${this.latestVersion} (${this.versionDiff} versions behind)"
    }

    override fun execute() {
        logResult(retrieveDependenciesWithUpdates())
    }

    private fun retrieveDependenciesWithUpdates(): List<DependencyWithUpdate> {
        return helper.lookupDependenciesUpdates(project.dependencies.toMutableSet(), false)
            .mapNotNull { (artifact, versions) -> DependencyWithUpdate(artifact.description(), artifact.version, versions.latest(), versions.diffWithCurrentVersion())}
            .filter { it.versionDiff > 0 }
    }

    private fun logResult(dependenciesWithUpdates: List<DependencyWithUpdate>) {
        if(dependenciesWithUpdates.any()) {
            log.info("The following dependencies have newer versions:")
            dependenciesWithUpdates.forEach { log.info(it.toString()) }
            log.info("You are ${dependenciesWithUpdates.sumOf { it.versionDiff }} versions behind for ${dependenciesWithUpdates.size} dependencies")
        } else {
            log.info("No dependencies updates found. WELL DONE !!!")
        }
    }

    override fun update(pom: ModifiedPomXMLEventReader?) {
        TODO("Not yet implemented")
    }
}