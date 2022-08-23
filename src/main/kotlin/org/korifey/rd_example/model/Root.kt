package org.korifey.rd_example.model

import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*

// This class contains model for rdgen, written on Korlin DSL. Result of generation can be found in `build/rdgen`
// To generate execute `./gradlew generateProtocolModels`

//Root of model. Only one root per protocol is allowed.
object ProtocolRoot : Root()

//Ext is an extension of Root. There can be different extensions in different files.
object ProtocolModel: Ext(ProtocolRoot) {
    // struct `Request` with 2 fields
    val Request = structdef {
        field("key", string)
        field("data", dateTime)
    }

    init {
        //signal is basic reactive stream
        signal("request", Request).apply {
            async //can be fired from any thread (if not set, only protocol thread is allowed)
            documentation =
                "SimpleRequest"
        }
    }
}