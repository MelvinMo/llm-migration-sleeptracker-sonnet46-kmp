package com.sleeptracker.resources

import org.jetbrains.compose.resources.ExperimentalResourceApi

// MIGRATION: TypeScript imported JSON at module level:
//   import privacyPolicyData from '../privacyPolicyData.json'
//   import privacyRegulations from '../privacyRegulations.json'
// In KMP, Compose Resources are loaded asynchronously.
// ResourceLoader is initialized once at app startup (before Koin) via
// `runBlocking { ResourceLoader.load() }` and exposes the loaded strings
// as properties consumed by TransparencyApiService.
//
// The files are embedded in the app binary via:
//   composeApp/src/commonMain/composeResources/files/privacyPolicyData.json
//   composeApp/src/commonMain/composeResources/files/privacyRegulations.json
//
// MIGRATION: Generated Res accessor class is in the package configured by
// Android namespace "com.sleeptracker". If the generated import differs,
// check: build/generated/compose/resourceGenerator/kotlin/commonMain/

object ResourceLoader {

    var privacyPolicyJson: String = "{}"
        private set

    var pipedaRegulationsJson: String = "{}"
        private set

    @OptIn(ExperimentalResourceApi::class)
    suspend fun load() {
        // MIGRATION: TypeScript `import privacyPolicyData from '...'` → Res.readBytes(path)
        // Res is the generated accessor from Compose Resources plugin.
        // The path is relative to commonMain/composeResources/
        privacyPolicyJson = sleeptracker.composeapp.generated.resources.Res
            .readBytes("files/privacyPolicyData.json")
            .decodeToString()

        pipedaRegulationsJson = sleeptracker.composeapp.generated.resources.Res
            .readBytes("files/privacyRegulations.json")
            .decodeToString()
    }
}
