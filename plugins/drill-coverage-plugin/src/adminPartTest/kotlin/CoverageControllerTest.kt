package com.epam.drill.plugins.coverage

import com.epam.drill.common.AgentInfo
import com.epam.drill.plugin.api.end.WsService
import com.epam.drill.plugin.api.message.DrillMessage
import com.epam.drill.plugins.coverage.CoverageEventType.CLASS_BYTES
import com.epam.drill.plugins.coverage.CoverageEventType.INIT
import com.epam.drill.plugins.coverage.CoverageEventType.INITIALIZED
import com.epam.drill.plugins.coverage.test.bar.BarDummy
import com.epam.drill.plugins.coverage.test.foo.FooDummy
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class CoverageControllerTest {
    private val agentInfo = AgentInfo(
        id = "id",
        name = "test",
        groupName = "test",
        description = "test",
        ipAddress = "127.0.0.1",
        isEnable = true,
        buildVersion = "1.0.1",
        buildVersions = mutableSetOf()
    )
    private val ws = WsServiceStub()

    private val coverageController = CoverageController(ws, "test")

    @Test
    fun `should have empty state before init`() {
        assertTrue { coverageController.agentStates.isEmpty() }
    }

    @Test
    fun `should switch agent data ref to ClassDataBuilder on init`() = runBlocking {
        val initInfo = InitInfo(1, "hello")
        val message = CoverageMessage(INIT, Json.stringify(InitInfo.serializer(), initInfo))

        sendMessage(message)

        assertEquals(1, coverageController.agentStates.count())
        val agentData = coverageController.agentStates[agentInfo.id]?.dataRef?.get()
        assertTrue { agentData is ClassDataBuilder && agentData.count == initInfo.classesCount }
    }

    @Test
    fun `should add class bytes on receiving a CLASS_BYTES message`() = runBlocking {
        val dummyClass = Dummy::class.java
        val dummyBytes = dummyClass.readBytes()

        sendInit(dummyClass)
        sendClass(dummyClass)

        coverageController.agentStates[agentInfo.id]!!.run {
            val agentData = dataRef.get() as ClassDataBuilder
            val (name, bytes) = agentData.classData.poll()!!
            assertEquals(dummyClass.path, name)
            assertTrue { dummyBytes.contentEquals(bytes) }
        }
    }

    @Test
    fun `should send messages to WebSocket on empty data`() {
        prepareClasses(Dummy::class.java)
        val message = CoverageMessage(CoverageEventType.SESSION_FINISHED, "")


        runBlocking {
            coverageController.processData(
                agentInfo,
                DrillMessage("", Json.stringify(CoverageMessage.serializer(), message))
            )
        }
        assertTrue { ws.sent.any { it.first == "/coverage-new" } }
        assertTrue { ws.sent.any { it.first == "/coverage" } }
        assertTrue { ws.sent.any { it.first == "/coverage-by-packages" } }
    }

    @Test
    fun `should preserve coverage for packages`() {
        // Count of Classes in package for test
        val countClassesInPackage = 1
        // Count of packages for test
        val countPackages = 3
        // Total count of Classes for test
        val countAllClasses = 3
        // Total count of Methods for test
        val countAllMethods = 6

        prepareClasses(Dummy::class.java, BarDummy::class.java, FooDummy::class.java)
        val message = CoverageMessage(CoverageEventType.SESSION_FINISHED, "")

        runBlocking {
            coverageController.processData(
                agentInfo,
                DrillMessage("", Json.stringify(CoverageMessage.serializer(), message))
            )
        }

        assertNotNull(JavaPackageCoverage)

        val methods = ws.javaPackagesCoverage.flatMap { it.classes }.flatMap { it.methods }

        assertEquals(countClassesInPackage, ws.javaPackagesCoverage.first().classes.size)
        assertEquals("Dummy", ws.javaPackagesCoverage.first().classes.first().name)
        assertEquals(countPackages, ws.javaPackagesCoverage.size)
        assertEquals(countAllClasses, ws.javaPackagesCoverage.count { it.classes.isNotEmpty() })
        assertEquals(countAllMethods, methods.size)
    }

    private fun prepareClasses(vararg classes: Class<*>) {
        runBlocking {
            sendInit(*classes)
            for (clazz in classes) {
                sendClass(clazz)
            }
            sendMessage(CoverageMessage(INITIALIZED, "Initialized!"))
        }
    }

    private suspend fun sendClass(clazz: Class<*>) {
        val bytes = clazz.readBytes()
        val classBytes = ClassBytes(clazz.path, bytes.toList())
        val messageString = Json.stringify(ClassBytes.serializer(), classBytes)
        val message = CoverageMessage(CLASS_BYTES, messageString)
        sendMessage(message)
    }

    private suspend fun sendInit(vararg classes: Class<*>) {
        val initInfo = InitInfo(classes.count(), "Start initialization")
        sendMessage(CoverageMessage(INIT, Json.stringify(InitInfo.serializer(), initInfo)))
    }

    private suspend fun sendMessage(message: CoverageMessage) {
        coverageController.processData(
            agentInfo,
            DrillMessage("", Json.stringify(CoverageMessage.serializer(), message))
        )
    }
}

@UnstableDefault
class WsServiceStub : WsService {

    val sent = mutableListOf<Pair<String, Any>>()

    lateinit var javaPackagesCoverage: List<JavaPackageCoverage>

    override suspend fun convertAndSend(agentInfo: AgentInfo, destination: String, message: String) {
        sent.add(destination to message)
        if (destination == "/coverage-by-packages")
            javaPackagesCoverage = Json.parse(JavaPackageCoverage.serializer().list, message)
    }

    override fun getPlWsSession() = setOf<String>()

    override fun storeData(agentId: String, obj: Any) {}

    override fun getEntityBy(agentId: String, clazz: Class<Any>) {}
}


fun Class<*>.readBytes() = this.getResourceAsStream("/${this.path}.class").readBytes()

val Class<*>.path get() = this.name.replace('.', '/')

