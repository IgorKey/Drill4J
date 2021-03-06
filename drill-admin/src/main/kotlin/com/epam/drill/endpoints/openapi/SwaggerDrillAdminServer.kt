package com.epam.drill.endpoints.openapi

import com.epam.drill.common.Message
import com.epam.drill.common.MessageType
import com.epam.drill.endpoints.AgentManager
import com.epam.drill.endpoints.agentWsMessage
import com.epam.drill.plugins.Plugins
import com.epam.drill.plugins.agentPluginPart
import com.epam.drill.plugins.serverInstance
import com.epam.drill.router.Routes
import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.patch
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.routing
import kotlinx.serialization.Serializable
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

/**
 * Swagger DrillAdmin
 *
 * This is a drill-ktor-admin-server
 */
@KtorExperimentalLocationsAPI
class SwaggerDrillAdminServer(override val kodein: Kodein) : KodeinAware {
    private val app: Application by instance()
    private val agentManager: AgentManager by instance()
    private val plugins: Plugins by kodein.instance()

    @Serializable
    data class PluginId(val pluginId: String)

    init {
        app.routing {
            registerAgent()
            registerDrillAdmin()
//            if (app.environment.config.property("ktor.dev").getString().toBoolean()) {
//                registerDevDrillAdmin()
//            }
        }
    }

    private fun Routing.registerAgent() {
        authenticate {
            patch<Routes.Api.Agent.Agent> { config ->
                agentManager[config.agentId]?.send(
                    Frame.Text(
                        Gson().toJson(
                            Message(
                                MessageType.MESSAGE,
                                "/agent/updateAgentConfig",
                                call.receive()
                            )
                        )
                    )
                )
                call.respond { if (agentManager.byId(config.agentId) != null) HttpStatusCode.OK else HttpStatusCode.NotFound }
            }
        }

        authenticate {
            post<Routes.Api.Agent.UnloadPlugin> { up ->
                val pluginId = call.receive<PluginId>()
                val drillAgent = agentManager[up.agentId]
                if (drillAgent == null) {
                    call.respond("can't find the agent '${up.agentId}'")
                    return@post
                }
                val agentPluginPartFile = plugins.plugins[pluginId.pluginId]?.agentPluginPart
                if (agentPluginPartFile == null) {
                    call.respond("can't find the plugin '${pluginId.pluginId}' in the agent '${up.agentId}'")
                    return@post
                }

                drillAgent.send(agentWsMessage("/plugins/unload", pluginId.pluginId))
//            drillAgent.agentInfo.plugins.removeIf { x -> x.id == up.pluginName }
                call.respond("event 'unload' was sent to AGENT")
            }
        }

        authenticate {
            get<Routes.Api.Agent.Agent> { up ->
                call.respond(agentManager.byId(up.agentId) ?: "can't find")
            }
        }

        authenticate {
            post<Routes.Api.Agent.AgentToggleStandby> { agent ->
                agentManager[agent.agentId]
                    ?.send(
                        agentWsMessage("agent/toggleStandBy", agent.agentId)
                    )
                call.respond { HttpStatusCode.OK }
            }
        }
    }

    /**
     * drill-admin
     */
    private fun Routing.registerDrillAdmin() {
        authenticate {
            get<Routes.Api.AllPlugins> {
                call.respond(plugins.plugins.values.map { dp -> dp.serverInstance.id })
            }
        }
    }

}
