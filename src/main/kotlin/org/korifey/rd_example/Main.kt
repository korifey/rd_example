package org.korifey.rd_example

import com.jetbrains.rd.framework.*
import com.jetbrains.rd.framework.util.NetUtils
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import org.korifey.rd_example.generated.Request
import org.korifey.rd_example.generated.protocolModel
import java.util.*
import kotlin.concurrent.thread

private fun p(p: String) = System.getProperty(p)

//lifetime is a cancellation token
val ldef = LifetimeDefinition()


fun main(args: Array<String>) {
    //terminate lifetime after 3 seconds
    thread {
        Thread.sleep(3000)
        ldef.terminate()
    }

    val name = "rd-example"
    val lifetime = ldef.lifetime

    //transform current thread into scheduler
    pumpCurrentThread(lifetime) { scheduler ->

        val idKind: IdKind
        val wire: IWire

        //main/server process
        if (args.isEmpty()) {

            val port = NetUtils.findFreePort(0)

            //start clone of this java process with 1 parameter - port to connct
            ProcessBuilder(
                "${p("java.home")}/bin/java",
                "-cp",
                p("java.class.path"),
                "org.korifey.rd_example.MainKt",
                port.toString()
            ).inheritIO().start()

            wire = SocketWire.Server(lifetime, scheduler, port, name, true)
            idKind = IdKind.Server

        //child/client process
        } else {
            val port = args[0].toInt()

            wire = SocketWire.Client(lifetime, scheduler, port, name)
            idKind = IdKind.Client
        }


        val protocol = Protocol(
            "rd-protocol",
            Serializers(),
            Identities(idKind),
            scheduler,
            wire,
            lifetime
        )
        //protocol is ready, now we can work with generated model

        val signal = protocol.protocolModel.request


        if (idKind == IdKind.Server) {
            val request = Request("today", Date())
            signal.fire(request)
            println("Main process send request: $request")

        } else {
            //we can ensure this advise happen before event comes from counterpart process because this event is
            // stored in scheduler's action queue after current initialization action
            signal.advise(lifetime) { request ->
                println("Child process receives request: $request")
            }
        }
    }

}