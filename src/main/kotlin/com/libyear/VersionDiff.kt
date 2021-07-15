package com.libyear

import kotlinx.serialization.Serializable

@Serializable
data class VersionDiff(val any: Int, val major: Int, val minor: Int, val fixes: Int) {
    override fun toString(): String {
        return "$any version(s) behind -> $major major(s) | $minor minor(s) | $fixes fix(es)"
    }
}