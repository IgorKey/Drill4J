package com.epam.drill.wsrouters

import com.epam.drill.common.AgentInfo
import com.epam.drill.core.ws.toggleStandby
import kotlin.test.Ignore
import kotlin.test.Test

class WsRoutersTest {

    @Test
    @Ignore
    fun toggleStandbyTest() {
        val disabledAgent = AgentInfo("id","test","test","test",true, "1.0.1")
        toggleStandby(disabledAgent)
    }


}