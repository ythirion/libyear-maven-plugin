package com.libyear

import kotlinx.serialization.Serializable

@Serializable
data class DependencyWithUpdate(
    val name: String,
    val projectVersion: String,
    val latestVersion: String,
    val versionDiff: VersionDiff
) {
    override fun toString(): String =
        "${this.name} ${this.projectVersion} -> ${this.latestVersion} (${this.versionDiff})"
}