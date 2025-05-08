package community.flock.wirespec.baker.emitter

import arrow.core.NonEmptyList
import community.flock.wirespec.compiler.core.emit.KotlinEmitter
import community.flock.wirespec.compiler.core.emit.common.EmitShared
import community.flock.wirespec.compiler.core.emit.common.Emitted
import community.flock.wirespec.compiler.core.emit.common.PackageName
import community.flock.wirespec.compiler.core.parse.Endpoint
import community.flock.wirespec.compiler.core.parse.Module
import community.flock.wirespec.compiler.utils.Logger

class BakerKotlinEmitter(val packageName: PackageName, emitShared: EmitShared): KotlinEmitter(packageName, emitShared) {

    override fun emit(module: Module, logger: Logger): NonEmptyList<Emitted> {
        return super.emit(module, logger)
            .plus(module.statements.filterIsInstance<Endpoint>()
            .map {
                println("Baking endpoint ${it.identifier}")
                val name = emit(it.identifier) + "Interaction"
                Emitted("${packageName.toDir()}/interaction/${name}", """
                    package ${packageName.value}
                    
                    object ${name}
                """.trimIndent()) }
            )
    }
}