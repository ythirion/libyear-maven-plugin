import com.libyear.DependencyWithUpdate
import com.libyear.VersionDiff
import com.libyear.logResult
import com.libyear.toReport
import org.approvaltests.Approvals
import org.junit.jupiter.api.Test

class Extensions_should {
    private val dependenciesWithUpdates = listOf(
        DependencyWithUpdate("com.libyear",
            "0.0.1",
            "1.0.0",
            VersionDiff(31, 22, 5,3)),
        DependencyWithUpdate("kotlinx.serialization",
            "0.0.1",
            "1.0.0",
            VersionDiff(10, 2, 5,3)))

    @Test
    fun export_dependency_report() {
        Approvals.verify(dependenciesWithUpdates.toReport())
    }

    @Test
    fun log_dependencies() {
        val log = StringBuilder()
        dependenciesWithUpdates.logResult{ log.appendLine(it) }

        Approvals.verify(log.toString())
    }
}