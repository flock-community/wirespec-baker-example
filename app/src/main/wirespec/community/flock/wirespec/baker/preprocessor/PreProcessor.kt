package community.flock.wirespec.baker.preprocessor

import community.flock.kotlinx.openapi.bindings.v3.OpenAPI

class PreProcessor : (String) -> String {
    override fun invoke(input: String) =
        OpenAPI.decodeFromString(input)
            .run { copy(paths = paths
                .filterKeys { it.value == "/pet" }
                .mapValues { it.value.copy(
                    get = it.value.get?.copy(operationId = it.value.get?.operationId?.plus("Baker")),
                    put = it.value.put?.copy(operationId = it.value.put?.operationId?.plus("Baker")),
                    post = it.value.post?.copy(operationId = it.value.post?.operationId?.plus("Baker")),
                ) }
            ) }
            .let(OpenAPI::encodeToString)

}