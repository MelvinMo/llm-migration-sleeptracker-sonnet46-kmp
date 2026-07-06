package com.sleeptracker.presentation.ui.screens.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.resources.ResourceLoader
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

// MIGRATION: TypeScript `privacy-policy.tsx` → `PrivacyPolicyScreen.kt`.
// Parses ResourceLoader.privacyPolicyJson (pre-loaded at startup) and renders
// all 11 sections with TOC jump-to-section navigation, matching RN styles exactly.

@Composable
fun PrivacyPolicyScreen(sectionId: String? = null, onNavigateBack: () -> Unit) {
    val policy = remember {
        Json.parseToJsonElement(ResourceLoader.privacyPolicyJson)
            .jsonObject["privacyPolicy"]!!.jsonObject
    }
    val metadata = remember(policy) { policy["metadata"]!!.jsonObject }
    val toc      = remember(policy) { policy["tableOfContents"]!!.jsonArray }
    val sections = remember(policy) { policy["sections"]!!.jsonObject }

    val scrollState    = rememberScrollState()
    val scope          = rememberCoroutineScope()
    val sectionOffsets = remember { mutableStateMapOf<String, Float>() }

    // TOC title → JSON section key (matches RN tocToSectionKeyMap)
    val tocToKey = remember {
        mapOf(
            "Interpretation and Definitions"                    to "interpretationsAndDefinitions",
            "Types of Information Collected and How We Use it"  to "dataCollection",
            "Cloud vs. Local Data Storage & Processing"         to "cloudVsLocalStorage",
            "Data Encryption and Pseudonymization"              to "dataEncryptionAndPsuedonymization",
            "How We share Your information"                     to "dataSharing",
            "Retention of Your information"                     to "dataRetention",
            "Your Rights under PIPEDA"                          to "userRights",
            "Data Breach Notification"                          to "dataBreachNotification",
            "Changes to the Privacy Policy"                     to "policyChanges",
            "Contact Us"                                        to "contact"
        )
    }

    // Once all section positions are recorded, scroll to deep-linked sectionId
    LaunchedEffect(sectionId, sectionOffsets.size) {
        if (!sectionId.isNullOrEmpty() && sectionOffsets.containsKey(sectionId)) {
            scrollState.animateScrollTo(sectionOffsets[sectionId]!!.roundToInt())
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {

        // ── Back button + title ────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = AppColors.White
                )
            }
            Text("Privacy Policy", color = AppColors.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // ── Metadata line ──────────────────────────────────────────────────────
        Text(
            text      = "Version: ${metadata["version"]!!.jsonPrimitive.content} | " +
                        "Effective Date: ${metadata["effectiveDate"]!!.jsonPrimitive.content} | " +
                        "Last Updated: ${metadata["lastUpdated"]!!.jsonPrimitive.content}",
            color     = Color(0xFFBBBBBB),
            fontSize  = 12.sp,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )

        // ── Scrollable body ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 15.dp)
        ) {

            // ── Table of Contents ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text       = "Table of Contents",
                    color      = AppColors.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(bottom = 10.dp)
                )
                toc.forEach { item ->
                    val title = item.jsonPrimitive.content
                    val key   = tocToKey[title]
                    if (key != null) {
                        Text(
                            text     = "• $title",
                            color    = AppColors.Accent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W500,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, top = 4.dp, bottom = 5.dp)
                                .clickable {
                                    scope.launch {
                                        sectionOffsets[key]?.let {
                                            scrollState.animateScrollTo(it.roundToInt())
                                        }
                                    }
                                }
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(1.dp)
                        .background(Color(0xFF333333))
                )
            }

            // ── Introduction ───────────────────────────────────────────────────
            val intro = sections["introduction"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["introduction"] = it.positionInParent().y }
            ) {
                PPBodyText(intro["content"]!!.jsonPrimitive.content)
                PPDivider()
            }

            // ── Interpretation and Definitions ─────────────────────────────────
            val defs        = sections["interpretationsAndDefinitions"]!!.jsonObject
            val defsContent = defs["content"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["interpretationsAndDefinitions"] = it.positionInParent().y }
            ) {
                PPHeading1("Interpretation and Definitions")
                PPDefinitionItem("You:", defsContent["you"]!!.jsonPrimitive.content)
                PPDefinitionItem("Company:", defsContent["company"]!!.jsonPrimitive.content)
                PPDefinitionItem("App:", defsContent["app"]!!.jsonPrimitive.content)
                PPDefinitionItem("Personal Information:", defsContent["personalInformation"]!!.jsonPrimitive.content)
                PPDefinitionItem("Personal Health Information:", defsContent["personalHealthInformation"]!!.jsonPrimitive.content)
                PPDivider()
            }

            // ── Types of Information Collected ─────────────────────────────────
            val dc    = sections["dataCollection"]!!.jsonObject
            val dcPI  = dc["personalInformation"]!!.jsonObject
            val dcPHI = dc["personalHealthInformation"]!!.jsonObject
            val dcSD  = dcPHI["sensorData"]!!.jsonObject
            val dcJD  = dcPHI["journalData"]!!.jsonObject
            val dcDD  = dcPHI["derivedData"]!!.jsonObject
            val dcUD  = dc["usageData"]!!.jsonObject
            val dcTI  = dcUD["technicalInformation"]!!.jsonObject
            val acct  = dcPI["accountInformation"]!!.jsonObject
            val mic   = dcSD["microphone"]!!.jsonObject
            val accel = dcSD["accelerometer"]!!.jsonObject
            val light = dcSD["lightSensor"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["dataCollection"] = it.positionInParent().y }
            ) {
                PPHeading1(dc["title"]!!.jsonPrimitive.content)
                PPBodyText(dc["content"]!!.jsonPrimitive.content)

                // Personal Information
                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(dcPI["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(dcPI["description"]!!.jsonPrimitive.content)
                    PPHeading3("Account Information")
                    PPDataPoint("Data Type", acct["dataType"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", acct["purpose"]!!.jsonPrimitive.content)
                    PPDataPoint("Collection Method", acct["collectionMethod"]!!.jsonPrimitive.content)
                    PPDataPoint("Storage", acct["storageLocationAndMethods"]!!.jsonPrimitive.content)
                }

                // Personal Health Information
                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(dcPHI["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(dcPHI["description"]!!.jsonPrimitive.content)

                    PPHeading3(dcSD["title"]!!.jsonPrimitive.content)

                    PPSubHeading("Microphone:")
                    PPDataPoint("Data Type", mic["dataType"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", mic["purpose"]!!.jsonPrimitive.content)
                    PPDataPoint("Collection Method", mic["collectionMethod"]!!.jsonPrimitive.content)

                    PPSubHeading("Accelerometer:")
                    PPDataPoint("Data Type", accel["dataType"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", accel["purpose"]!!.jsonPrimitive.content)
                    PPDataPoint("Collection Method", accel["collectionMethod"]!!.jsonPrimitive.content)

                    PPSubHeading("Light Sensor:")
                    PPDataPoint("Data Type", light["dataType"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", light["purpose"]!!.jsonPrimitive.content)
                    PPDataPoint("Collection Method", light["collectionMethod"]!!.jsonPrimitive.content)

                    PPHeading3("Journal Data")
                    PPDataPoint("Data Type", dcJD["dataType"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", dcJD["purpose"]!!.jsonPrimitive.content)
                    PPDataPoint("Collection Method", dcJD["collectionMethod"]!!.jsonPrimitive.content)

                    PPHeading3("Derived Data")
                    PPBodyText(dcDD["content"]!!.jsonPrimitive.content)
                }

                // Usage Data
                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(dcUD["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(dcUD["description"]!!.jsonPrimitive.content)
                    PPHeading3("Technical Information")
                    PPDataPoint("Data Type", dcTI["dataType"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", dcTI["purpose"]!!.jsonPrimitive.content)
                    PPDataPoint("Collection Method", dcTI["collectionMethod"]!!.jsonPrimitive.content)
                    PPDataPoint("Storage Location", dcTI["storageLocation"]!!.jsonPrimitive.content)
                    PPDataPoint("Troubleshooting", dcTI["troubleshooting"]!!.jsonPrimitive.content)
                    PPDataPoint("General Analytics", dcTI["generalAnalytics"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── Cloud vs. Local Storage ────────────────────────────────────────
            val cvl   = sections["cloudVsLocalStorage"]!!.jsonObject
            val cloud = cvl["cloudStorage"]!!.jsonObject
            val local = cvl["localStorage"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["cloudVsLocalStorage"] = it.positionInParent().y }
            ) {
                PPHeading1(cvl["title"]!!.jsonPrimitive.content)
                PPBodyText(cvl["content"]!!.jsonPrimitive.content)

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(cloud["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(cloud["description"]!!.jsonPrimitive.content)
                    PPDataPoint("Benefits", cloud["benefits"]!!.jsonPrimitive.content)
                    PPDataPoint("Data Location", cloud["dataLocation"]!!.jsonPrimitive.content)
                    PPDataPoint("Accountability", cloud["accountability"]!!.jsonPrimitive.content)
                }

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(local["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(local["description"]!!.jsonPrimitive.content)
                    PPDataPoint("Limitations", local["limitations"]!!.jsonPrimitive.content)
                    PPDataPoint("Responsibility", local["responsibility"]!!.jsonPrimitive.content)
                    PPDataPoint("Consent", local["consent"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── Data Encryption and Pseudonymization ───────────────────────────
            val enc         = sections["dataEncryptionAndPsuedonymization"]!!.jsonObject
            val encObj      = enc["encryption"]!!.jsonObject
            val encAtRest   = encObj["atRest"]!!.jsonObject
            val encInTransit = encObj["inTransit"]!!.jsonPrimitive.content
            val pseudo      = enc["pseudonymization"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["dataEncryptionAndPsuedonymization"] = it.positionInParent().y }
            ) {
                PPHeading1(enc["title"]!!.jsonPrimitive.content)
                PPDescriptionText(enc["description"]!!.jsonPrimitive.content)

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2("Encryption")
                    PPSubHeading("At Rest:")
                    PPDataPoint("Server Data", encAtRest["serverData"]!!.jsonPrimitive.content)
                    PPDataPoint("Local Data", encAtRest["localData"]!!.jsonPrimitive.content)
                    PPSubHeading("In Transit:")
                    Text(
                        text     = encInTransit,
                        color    = AppColors.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 5.dp)
                    )
                }

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2("Pseudonymization")
                    PPDescriptionText(pseudo["description"]!!.jsonPrimitive.content)
                    PPDataPoint("Purpose", pseudo["purpose"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── How We Share Information ───────────────────────────────────────
            val ds      = sections["dataSharing"]!!.jsonObject
            val dsGC    = ds["googleCloud"]!!.jsonObject
            val dsLegal = ds["legal"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["dataSharing"] = it.positionInParent().y }
            ) {
                PPHeading1(ds["title"]!!.jsonPrimitive.content)
                PPDescriptionText(ds["description"]!!.jsonPrimitive.content)

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(dsGC["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(dsGC["description"]!!.jsonPrimitive.content)
                }
                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2(dsLegal["title"]!!.jsonPrimitive.content)
                    PPDescriptionText(dsLegal["description"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── Retention of Your Information ──────────────────────────────────
            val dr      = sections["dataRetention"]!!.jsonObject
            val drAcct  = dr["accountInformation"]!!.jsonObject
            val drPHI   = dr["personalHealthInformation"]!!.jsonObject
            val drUD    = dr["usageData"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["dataRetention"] = it.positionInParent().y }
            ) {
                PPHeading1(dr["title"]!!.jsonPrimitive.content)
                PPDescriptionText(dr["description"]!!.jsonPrimitive.content)

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2("Account Information")
                    PPDescriptionText(drAcct["description"]!!.jsonPrimitive.content)
                    PPDataPoint("Data Type", drAcct["dataType"]!!.jsonPrimitive.content)
                }

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2("Personal Health Information")
                    PPDataPoint("Cloud Stored", drPHI["cloudStored"]!!.jsonPrimitive.content)
                    PPDataPoint("User Initiated Deletion", drPHI["userInitiated"]!!.jsonPrimitive.content)
                    PPDataPoint("Local Stored", drPHI["localStored"]!!.jsonPrimitive.content)
                    PPDataPoint("Data Type", drPHI["dataType"]!!.jsonPrimitive.content)
                }

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    PPHeading2("Usage Data")
                    PPDataPoint("Pseudonymized", drUD["pseudonymized"]!!.jsonPrimitive.content)
                    PPDataPoint("Anonymized", drUD["anonymized"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── Your Rights under PIPEDA ───────────────────────────────────────
            val ur = sections["userRights"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["userRights"] = it.positionInParent().y }
            ) {
                PPHeading1(ur["title"]!!.jsonPrimitive.content)
                PPDescriptionText(ur["description"]!!.jsonPrimitive.content)

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    listOf("access", "correction", "withdrawConsent", "accountability", "challengeCompliance")
                        .forEach { key ->
                            val right = ur[key]!!.jsonObject
                            PPHeading2(right["title"]!!.jsonPrimitive.content)
                            PPBodyText(right["description"]!!.jsonPrimitive.content)
                        }
                    PPBodyText(ur["exerciseRights"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── Data Breach Notification ───────────────────────────────────────
            val dbn        = sections["dataBreachNotification"]!!.jsonObject
            val notifIndiv = dbn["notificationIndividuals"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["dataBreachNotification"] = it.positionInParent().y }
            ) {
                PPHeading1(dbn["title"]!!.jsonPrimitive.content)
                PPDescriptionText(dbn["description"]!!.jsonPrimitive.content)

                Column(Modifier.padding(start = 10.dp, top = 10.dp)) {
                    val riskAssess = dbn["riskAssessment"]!!.jsonObject
                    PPHeading2(riskAssess["title"]!!.jsonPrimitive.content)
                    PPBodyText(riskAssess["description"]!!.jsonPrimitive.content)

                    val notifOPC = dbn["notificationOPC"]!!.jsonObject
                    PPHeading2(notifOPC["title"]!!.jsonPrimitive.content)
                    PPBodyText(notifOPC["description"]!!.jsonPrimitive.content)

                    PPHeading2(notifIndiv["title"]!!.jsonPrimitive.content)
                    PPBodyText(notifIndiv["description"]!!.jsonPrimitive.content)
                    notifIndiv["content"]!!.jsonArray.forEach { item ->
                        Text(
                            text     = "- ${item.jsonPrimitive.content}",
                            color    = AppColors.White,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 25.dp, bottom = 5.dp)
                        )
                    }

                    val notifOther = dbn["notificationOtherOrganizations"]!!.jsonObject
                    PPHeading2(notifOther["title"]!!.jsonPrimitive.content)
                    PPBodyText(notifOther["description"]!!.jsonPrimitive.content)

                    val recordKeeping = dbn["recordKeeping"]!!.jsonObject
                    PPHeading2(recordKeeping["title"]!!.jsonPrimitive.content)
                    PPBodyText(recordKeeping["description"]!!.jsonPrimitive.content)
                }
                PPDivider()
            }

            // ── Changes to the Privacy Policy ──────────────────────────────────
            val pc = sections["policyChanges"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .onGloballyPositioned { sectionOffsets["policyChanges"] = it.positionInParent().y }
            ) {
                PPHeading1("Changes to the Privacy Policy")
                PPBodyText(pc["content"]!!.jsonPrimitive.content)
                PPDivider()
            }

            // ── Contact Us ────────────────────────────────────────────────────
            val contact = sections["contact"]!!.jsonObject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
                    .onGloballyPositioned { sectionOffsets["contact"] = it.positionInParent().y }
            ) {
                PPHeading1(contact["title"]!!.jsonPrimitive.content)
                PPDescriptionText(contact["description"]!!.jsonPrimitive.content)
                PPDataPoint("Email", contact["email"]!!.jsonPrimitive.content)
            }
        }
    }
}

// ── Style helpers matching RN stylesheet ───────────────────────────────────────

@Composable
private fun PPHeading1(text: String) = Text(
    text       = text,
    color      = AppColors.Accent,
    fontSize   = 22.sp,
    fontWeight = FontWeight.Bold,
    modifier   = Modifier.padding(top = 15.dp, bottom = 10.dp)
)

@Composable
private fun PPHeading2(text: String) = Text(
    text       = text,
    color      = AppColors.Accent,
    fontSize   = 18.sp,
    fontWeight = FontWeight.W600,
    modifier   = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 8.dp)
)

@Composable
private fun PPHeading3(text: String) = Text(
    text       = text,
    color      = AppColors.Accent,
    fontSize   = 16.sp,
    fontWeight = FontWeight.W500,
    modifier   = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 5.dp)
)

@Composable
private fun PPSubHeading(text: String) = Text(
    text       = text,
    color      = Color(0xFFADD8E6),
    fontSize   = 15.sp,
    fontWeight = FontWeight.Bold,
    modifier   = Modifier.padding(start = 15.dp, top = 5.dp, bottom = 3.dp)
)

@Composable
private fun PPBodyText(text: String) = Text(
    text       = text,
    color      = AppColors.White,
    fontSize   = 16.sp,
    lineHeight = 24.sp,
    modifier   = Modifier.padding(bottom = 10.dp)
)

@Composable
private fun PPDescriptionText(text: String) = Text(
    text       = text,
    color      = AppColors.White,
    fontSize   = 15.sp,
    lineHeight = 22.sp,
    fontStyle  = FontStyle.Italic,
    modifier   = Modifier.padding(bottom = 10.dp)
)

@Composable
private fun PPDataPoint(label: String, value: String) = Text(
    text = buildAnnotatedString {
        withStyle(SpanStyle(color = Color(0xFFADD8E6), fontSize = 14.sp)) {
            append("• $label: ")
        }
        withStyle(SpanStyle(color = AppColors.White, fontSize = 14.sp, fontWeight = FontWeight.Normal)) {
            append(value)
        }
    },
    modifier = Modifier.padding(start = 15.dp, bottom = 5.dp)
)

@Composable
private fun PPDefinitionItem(term: String, definition: String) {
    Column(Modifier.padding(start = 10.dp, bottom = 8.dp)) {
        Text(term, color = AppColors.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(definition, color = AppColors.White, fontSize = 15.sp, modifier = Modifier.padding(start = 5.dp))
    }
}

@Composable
private fun PPDivider() = Box(
    Modifier
        .fillMaxWidth()
        .padding(top = 10.dp)
        .height(0.5.dp)
        .background(Color(0xFF808080))
)
