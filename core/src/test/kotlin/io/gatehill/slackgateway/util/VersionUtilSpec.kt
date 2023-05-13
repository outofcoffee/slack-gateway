import io.gatehill.slackgateway.util.VersionUtil
import org.amshove.kluent.`should not be equal to`
import org.amshove.kluent.`should not be null`
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * Specification for `VersionUtil`.
 */
object VersionUtilSpec : Spek({
    describe("a version utility") {
        val version = VersionUtil.version

        it("should provide a version") {
            version.`should not be null`()
        }
        it("should provide a valid version") {
            version `should not be equal to` VersionUtil.UNSPECIFIED_VERSION
        }
    }
})
