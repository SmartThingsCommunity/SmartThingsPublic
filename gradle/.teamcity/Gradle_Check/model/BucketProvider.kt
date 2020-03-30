package Gradle_Check.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import common.Os
import configurations.FunctionalTest
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import model.BuildTypeBucket
import model.CIBuildModel
import model.GradleSubproject
import model.Stage
import model.TestCoverage
import model.TestType
import java.io.File
import java.util.LinkedList

const val MAX_PROJECT_NUMBER_IN_BUCKET = 10

// 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12
// 2.0, 2.1, 2.2.1, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12, 2.13, 2.14.1,
// 3.0, 3.1, 3.2.1, 3.3, 3.4.1, 3.5.1,
// 4.0.2, 4.1, 4.2.1, 4.3.1, 4.4.1, 4.5.1, 4.6, 4.7, 4.8.1, 4.9, 4.10.3,
// 5.0, 5.1.1, 5.2.1, 5.3.1, 5.4.1, 5.5.1, 5.6.4,
// 6.0.1, 6.1

val CROSS_VERSION_BUCKETS = listOf(
    listOf("0.0", "2.0"), // 0.0 <= version < 2.0
    listOf("2.0", "2.8"), // 2.0 <= version < 2.8
    listOf("2.8", "3.0"), // 2.8 <= version < 3.0
    listOf("3.0", "3.3"), // 3.0 <= version < 3.3
    listOf("3.3", "4.1"), // 3.3 <= version < 4.1
    listOf("4.1", "4.5"), // 4.1 <=version < 4.5
    listOf("4.5", "4.8"), // 4.5 <=version < 4.8
    listOf("4.8", "5.0"), // 4.8 <=version < 5.0
    listOf("5.0", "5.4"), // 5.0 <=version < 5.4
    listOf("5.4", "6.0"), // 5.4 <=version < 6.0
    listOf("6.0", "99.0") // 6.0 <=version < 99.0
)

typealias BuildProjectToSubprojectTestClassTimes = Map<String, Map<String, List<TestClassTime>>>

interface GradleBuildBucketProvider {
    fun createFunctionalTestsFor(stage: Stage, testCoverage: TestCoverage): List<FunctionalTest>

    fun createDeferredFunctionalTestsFor(stage: Stage): List<FunctionalTest>
}

class StatisticBasedGradleBuildBucketProvider(private val model: CIBuildModel, testTimeDataJson: File) : GradleBuildBucketProvider {
    private val buckets: Map<TestCoverage, List<BuildTypeBucket>> = buildBuckets(testTimeDataJson, model)

    override fun createFunctionalTestsFor(stage: Stage, testCoverage: TestCoverage): List<FunctionalTest> {
        return buckets.getValue(testCoverage).mapIndexed { bucketIndex: Int, bucket: BuildTypeBucket ->
            bucket.createFunctionalTestsFor(model, stage, testCoverage, bucketIndex)
        }
    }

    override fun createDeferredFunctionalTestsFor(stage: Stage): List<FunctionalTest> {
        // The first stage which doesn't omit slow projects
        val deferredStage = model.stages.find { !it.omitsSlowProjects }!!
        val deferredStageIndex = model.stages.indexOfFirst { !it.omitsSlowProjects }
        return if (stage.stageName != deferredStage.stageName) {
            emptyList()
        } else {
            val stages = model.stages.subList(0, deferredStageIndex)
            val deferredTests = mutableListOf<FunctionalTest>()
            stages.forEach { eachStage ->
                eachStage.functionalTests.forEach { testConfig ->
                    deferredTests.addAll(model.subprojects.getSlowSubprojects().map { it.createFunctionalTestsFor(model, eachStage, testConfig, -1) })
                }
            }
            deferredTests
        }
    }

    private
    fun buildBuckets(buildClassTimeJson: File, model: CIBuildModel): Map<TestCoverage, List<BuildTypeBucket>> {
        val jsonObj = JSON.parseObject(buildClassTimeJson.readText()) as JSONObject
        val buildProjectClassTimes: BuildProjectToSubprojectTestClassTimes = jsonObj.map { buildProjectToSubprojectTestClassTime ->
            buildProjectToSubprojectTestClassTime.key to (buildProjectToSubprojectTestClassTime.value as JSONObject).map { subProjectToTestClassTime ->
                subProjectToTestClassTime.key to (subProjectToTestClassTime.value as JSONArray).map { TestClassTime(it as JSONObject) }
            }.toMap()
        }.toMap()

        val result = mutableMapOf<TestCoverage, List<BuildTypeBucket>>()
        for (stage in model.stages) {
            for (testCoverage in stage.functionalTests) {
                when (testCoverage.testType) {
                    in listOf(TestType.allVersionsCrossVersion, TestType.quickFeedbackCrossVersion) -> {
                        result[testCoverage] = splitBucketsByGradleVersionForBuildProject()
                    }
                    else -> {
                        result[testCoverage] = splitBucketsByTestClassesForBuildProject(testCoverage, stage, buildProjectClassTimes)
                    }
                }
            }
        }
        return result
    }

    // For quickFeedbackCrossVersion and allVersionsCrossVersion, the buckets are split by Gradle version
    // By default, split them by CROSS_VERSION_BUCKETS
    private fun splitBucketsByGradleVersionForBuildProject() = CROSS_VERSION_BUCKETS.map { GradleVersionRangeCrossVersionTestBucket(it[0], it[1]) }

    private
    fun splitBucketsByTestClassesForBuildProject(testCoverage: TestCoverage, stage: Stage, buildProjectClassTimes: BuildProjectToSubprojectTestClassTimes): List<BuildTypeBucket> {
        val validSubprojects = model.subprojects.getSubprojectsFor(testCoverage, stage)

        // Build project not found, don't split into buckets
        val subProjectToClassTimes: Map<String, List<TestClassTime>> = determineSubProjectClassTimes(testCoverage, buildProjectClassTimes) ?: return validSubprojects

        val subProjectTestClassTimes: List<SubprojectTestClassTime> = subProjectToClassTimes
            .entries
            .filter { "UNKNOWN" != it.key }
            .filter { model.subprojects.getSubprojectByName(it.key) != null }
            .map { SubprojectTestClassTime(model.subprojects.getSubprojectByName(it.key)!!, it.value.filter { it.sourceSet != "test" }) }
            .sortedBy { -it.totalTime }

        return split(
            LinkedList(subProjectTestClassTimes),
            SubprojectTestClassTime::totalTime,
            { largeElement: SubprojectTestClassTime, size: Int -> largeElement.split(size) },
            { list: List<SubprojectTestClassTime> -> SmallSubprojectBucket(list) },
            testCoverage.expectedBucketNumber,
            MAX_PROJECT_NUMBER_IN_BUCKET
        )
    }

    private fun determineSubProjectClassTimes(testCoverage: TestCoverage, buildProjectClassTimes: BuildProjectToSubprojectTestClassTimes): Map<String, List<TestClassTime>>? {
        val testCoverageId = testCoverage.asId(model)
        return buildProjectClassTimes[testCoverageId] ?: if (testCoverage.testType == TestType.soak) {
            null
        } else {
            val testCoverages = model.stages.flatMap { it.functionalTests }
            val foundTestCoverage = testCoverages.firstOrNull {
                it.testType == TestType.platform &&
                    it.os == testCoverage.os &&
                    it.buildJvmVersion == testCoverage.buildJvmVersion
            }
            foundTestCoverage?.let {
                buildProjectClassTimes[it.asId(model)]
            }?.also {
                println("No test statistics found for ${testCoverage.asName()} (${testCoverage.uuid}), re-using the data from ${foundTestCoverage.asName()} (${foundTestCoverage.uuid})")
            }
        }
    }
}

/**
 * Split a list of elements into nearly even sublist. If an element is too large, largeElementSplitFunction will be used to split the large element into several smaller pieces;
 * if some elements are too small, they will be aggregated by smallElementAggregateFunction.
 *
 * @param list the list to split, must be ordered by size desc
 * @param toIntFunction the function used to map the element to its "size"
 * @param largeElementSplitFunction the function used to further split the large element into smaller pieces
 * @param smallElementAggregateFunction the function used to aggregate tiny elements into a large bucket
 * @param expectedBucketNumber the return value's size should be expectedBucketNumber
 */
fun <T, R> split(list: LinkedList<T>, toIntFunction: (T) -> Int, largeElementSplitFunction: (T, Int) -> List<R>, smallElementAggregateFunction: (List<T>) -> R, expectedBucketNumber: Int, maxNumberInBucket: Int): List<R> {
    if (expectedBucketNumber == 1) {
        return listOf(smallElementAggregateFunction(list))
    }

    val expectedBucketSize = list.sumBy(toIntFunction) / expectedBucketNumber

    val largestElement = list.removeFirst()!!

    val largestElementSize = toIntFunction(largestElement)

    return if (largestElementSize >= expectedBucketSize) {
        val bucketsOfFirstElement = largeElementSplitFunction(largestElement, if (largestElementSize % expectedBucketSize == 0) largestElementSize / expectedBucketSize else largestElementSize / expectedBucketSize + 1)
        val bucketsOfRestElements = split(list, toIntFunction, largeElementSplitFunction, smallElementAggregateFunction, expectedBucketNumber - bucketsOfFirstElement.size, maxNumberInBucket)
        bucketsOfFirstElement + bucketsOfRestElements
    } else {
        val buckets = arrayListOf(largestElement)
        var restCapacity = expectedBucketSize - toIntFunction(largestElement)
        while (restCapacity > 0 && list.isNotEmpty() && buckets.size < maxNumberInBucket) {
            val smallestElement = list.removeLast()
            buckets.add(smallestElement)
            restCapacity -= toIntFunction(smallestElement)
        }
        listOf(smallElementAggregateFunction(buckets)) + split(list, toIntFunction, largeElementSplitFunction, smallElementAggregateFunction, expectedBucketNumber - 1, maxNumberInBucket)
    }
}

class GradleVersionRangeCrossVersionTestBucket(private val startInclusive: String, private val endExclusive: String) : BuildTypeBucket {
    override fun createFunctionalTestsFor(model: CIBuildModel, stage: Stage, testCoverage: TestCoverage, bucketIndex: Int) =
        FunctionalTest(model,
            getUuid(model, testCoverage, bucketIndex),
            "${testCoverage.asName()} ($startInclusive <= gradle <$endExclusive)",
            "${testCoverage.asName()} for gradle ($startInclusive <= gradle <$endExclusive)",
            testCoverage,
            stage,
            emptyList(),
            "-PonlyTestGradleVersion=$startInclusive-$endExclusive"
        )
}

class LargeSubprojectSplitBucket(val subproject: GradleSubproject, val number: Int, val include: Boolean, val classes: List<TestClassTime>) : BuildTypeBucket by subproject {
    val name = "${subproject.name}_$number"
    val totalTime = classes.sumBy { it.buildTimeMs }

    override fun getName(testCoverage: TestCoverage) = "${testCoverage.asName()} ($name)"

    override fun createFunctionalTestsFor(model: CIBuildModel, stage: Stage, testCoverage: TestCoverage, bucketIndex: Int): FunctionalTest =
        FunctionalTest(model,
            getUuid(model, testCoverage, bucketIndex),
            getName(testCoverage),
            getDescription(testCoverage),
            testCoverage,
            stage,
            subprojects = listOf(subproject.name),
            extraParameters = if (include) "-PincludeTestClasses=true -x ${subproject.name}:test" else "-PexcludeTestClasses=true", // Only run unit test in last bucket
            preBuildSteps = prepareTestClassesStep(testCoverage.os)
        )

    private fun prepareTestClassesStep(os: Os): BuildSteps.() -> Unit {
        val testClasses = classes.map { it.toPropertiesLine() }
        val action = if (include) "include" else "exclude"
        val unixScript = """
mkdir -p build
rm -rf build/*-test-classes.properties
cat > build/$action-test-classes.properties << EOL
${testClasses.joinToString("\n")}
EOL

echo "Tests to be ${action}d in this build"
cat build/$action-test-classes.properties
"""

        val linesWithEcho = testClasses.joinToString("\n") { "echo $it" }

        val windowsScript = """
mkdir build
del /f /q build\include-test-classes.properties
del /f /q build\exclude-test-classes.properties
(
$linesWithEcho
) > build\$action-test-classes.properties

echo "Tests to be ${action}d in this build"
type build\$action-test-classes.properties
"""

        return {
            script {
                name = "PREPARE_TEST_CLASSES"
                executionMode = BuildStep.ExecutionMode.ALWAYS
                scriptContent = if (os == Os.windows) windowsScript else unixScript
            }
        }
    }
}

class SmallSubprojectBucket(val subprojectsBuildTime: List<SubprojectTestClassTime>) : BuildTypeBucket {
    val subprojects = subprojectsBuildTime.map { it.subProject }
    val name = subprojects.joinToString(",") { it.name }
    val totalTime = subprojectsBuildTime.sumBy { it.totalTime }
    override fun createFunctionalTestsFor(model: CIBuildModel, stage: Stage, testCoverage: TestCoverage, bucketIndex: Int): FunctionalTest =
        FunctionalTest(model,
            getUuid(model, testCoverage, bucketIndex),
            getName(testCoverage),
            getDescription(testCoverage),
            testCoverage,
            stage,
            subprojects.map { it.name }
        )

    override fun getName(testCoverage: TestCoverage) = "${testCoverage.asName()} (${subprojects.joinToString(",") { it.name }})"

    override fun getDescription(testCoverage: TestCoverage) = "${testCoverage.asName()} for ${subprojects.joinToString(", ") { it.name }}"
}

class TestClassTime(var testClass: String, val sourceSet: String, var buildTimeMs: Int) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.getString("testClass"),
        jsonObject.getString("sourceSet"),
        jsonObject.getIntValue("buildTimeMs")
    )

    fun toPropertiesLine() = "$testClass=$sourceSet"
}

class SubprojectTestClassTime(val subProject: GradleSubproject, private val testClassTimes: List<TestClassTime>) {
    val totalTime: Int = testClassTimes.sumBy { it.buildTimeMs }

    fun split(expectedBucketNumber: Int): List<BuildTypeBucket> {
        return if (expectedBucketNumber == 1) {
            listOf(subProject)
        } else {
            // fun <T, R> split(list: LinkedList<T>, toIntFunction: (T) -> Int, largeElementSplitFunction: (T, Int) -> List<R>, smallElementAggregateFunction: (List<T>) -> R, expectedBucketNumber: Int, maxNumberInBucket: Int): List<R> {
            // T TestClassTime
            // R List<TestClassTime>
            val list = LinkedList(testClassTimes.sortedBy { -it.buildTimeMs })
            val toIntFunction = TestClassTime::buildTimeMs
            val largeElementSplitFunction: (TestClassTime, Int) -> List<List<TestClassTime>> = { testClassTime: TestClassTime, number: Int -> listOf(listOf(testClassTime)) }
            val smallElementAggregateFunction: (List<TestClassTime>) -> List<TestClassTime> = { it }

            val buckets: List<List<TestClassTime>> = split(list, toIntFunction, largeElementSplitFunction, smallElementAggregateFunction, expectedBucketNumber, Integer.MAX_VALUE)

            buckets.mapIndexed { index: Int, classesInBucket: List<TestClassTime> ->
                val include = index != buckets.size - 1
                val classes = if (include) classesInBucket else buckets.subList(0, buckets.size - 1).flatten()
                LargeSubprojectSplitBucket(subProject, index + 1, include, classes)
            }
        }
    }

    override fun toString(): String {
        return "SubprojectTestClassTime(subProject=${subProject.name}, totalTime=$totalTime)"
    }
}
