package com.libyear

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.codehaus.mojo.versions.AbstractVersionsUpdaterMojo
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader
import java.io.File

@Mojo(name = "run")
class LibYearMojo : AbstractVersionsUpdaterMojo() {
    @Parameter(defaultValue = "\${project.basedir}/target/libyear", readonly = true)
    private val outputDirectory: File? = null

    override fun execute() {
        retrieveDependenciesWithUpdates()
            .logResult { log.info(it) }
            .toReport()
            .runCatching {
                outputDirectory?.create()
                    .saveToFile("${project.name}.json", this)
            }.onFailure { ex -> log.error(ex) }
    }

    private fun retrieveDependenciesWithUpdates(): Dependencies {
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

    override fun update(pom: ModifiedPomXMLEventReader?) {
        TODO("Not yet implemented")
    }
}