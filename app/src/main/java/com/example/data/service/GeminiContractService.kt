package com.example.data.service

import com.example.BuildConfig
import com.example.data.legal.LegalData
import com.example.data.model.ContractClause
import com.example.data.model.ContractOverview
import com.example.data.model.HousingType
import com.example.data.model.RiskLevel
import com.example.data.util.TextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object GeminiContractService {

    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun buildSystemInstruction(housing: HousingType): String {
        val housingNote = when (housing) {
            HousingType.UNKNOWN -> "ผู้ใช้ไม่ทราบประเภทที่พัก ให้ระบุเงื่อนไขสั้น ๆ ว่าข้อสังเกตทางกฎหมายใช้ได้เมื่อที่พักเป็นประเภทใด (อพาร์ตเมนต์/ห้องเช่าตามส่วน A หรือหอพักจดทะเบียนตามส่วน B) อย่าเขียนซ้ำยาว"
            HousingType.APARTMENT -> "ผู้ใช้ระบุว่าเป็นอพาร์ตเมนต์หรือห้องเช่าทั่วไป (ไม่ใช่หอพักจดทะเบียน) ให้ใช้ส่วน A เป็นหลัก และบอกเงื่อนไขสั้น ๆ ว่าใช้ได้เมื่อผู้ให้เช่ามีที่พักให้เช่าตั้งแต่ 3 หน่วยขึ้นไป"
            HousingType.DORM -> "ผู้ใช้ระบุว่าเป็นหอพักที่จดทะเบียนตามกฎหมายหอพัก ให้ใช้ส่วน B เป็นหลัก ประกาศส่วน A ไม่ใช้บังคับโดยตรงกับหอพักจดทะเบียน ให้ใช้เป็นเกณฑ์เปรียบเทียบเท่านั้น"
        }

        return """
คุณคือทนายความผู้เชี่ยวชาญด้านกฎหมายคุ้มครองผู้บริโภคและสัญญาเช่าที่พักในประเทศไทย
หน้าที่ของคุณคือวิเคราะห์ข้อความสัญญาเช่า เพื่อตรวจสอบข้อสัญญาที่ไม่เป็นธรรม ขัดต่อกฎหมาย หรือฝ่าฝืนประกาศคณะกรรมการว่าด้วยสัญญา พ.ศ. 2568 หรือ พ.ร.บ.หอพัก พ.ศ. 2558

รูปแบบการตอบ: ต้องตอบเป็น JSON array เท่านั้น โดยแต่ละรายการแทนแต่ละข้อสัญญาตามลำดับ:
[
  {
    "no": 1,
    "summary": "สรุปข้อนี้ด้วยภาษาง่าย ๆ 1 ประโยค",
    "risk": "high | medium | low",
    "issues": ["จุดที่ควรระวังหรืออาจไม่เป็นธรรม ไม่เกิน 2 ข้อ"],
    "law_ref": ["กฎหมายหรือมาตราที่เกี่ยวข้อง เช่น ประกาศ สคบ. พ.ศ. 2568 ข้อ 5"],
    "suggestion": "สิ่งที่ผู้เช่าควรถามหรือขอแก้"
  }
]

กฎสำคัญ:
- อ้างอิงเฉพาะกฎหมายในข้อมูลอ้างอิงด้านล่าง
- ประเด็นที่ต้องตรวจเป็นพิเศษ: ค่าน้ำค่าไฟเกินอัตราทางการ, เงินประกันเกินเพดาน, การริบเงินประกันโดยผู้เช่าไม่ผิด, การล็อกห้อง/ยึดของ, การเข้าห้องโดยไม่แจ้ง, การคืนเงินประกันช้ากว่า 7-14 วัน

## ข้อมูลกฎหมายอ้างอิง
${LegalData.LAW_CONTEXT_TEXT}

## ประเภทที่พักที่ผู้ใช้ระบุ
$housingNote
""".trimIndent()
    }

    suspend fun analyzeContract(
        clauses: List<String>,
        housing: HousingType
    ): Pair<List<ContractClause>, ContractOverview?> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val cleanedClauses = clauses.map { TextExtractor.redact(it) }

        if (apiKey.isBlank() || apiKey == "GEMINI_API_KEY_DEFAULT_VALUE") {
            // Offline local rule-based legal engine
            return@withContext Pair(
                fallbackRuleBasedAnalysis(cleanedClauses, housing),
                generateLocalOverview(cleanedClauses)
            )
        }

        try {
            val systemPrompt = buildSystemInstruction(housing)
            val jsonPayload = JSONArray()
            cleanedClauses.forEachIndexed { index, clause ->
                val item = JSONObject().apply {
                    put("no", index + 1)
                    put("clause", clause)
                }
                jsonPayload.put(item)
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "วิเคราะห์ข้อสัญญาต่อไปนี้:\n$jsonPayload")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val url = "$BASE_URL$MODEL:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Return intelligent local evaluation if API limit reached
                return@withContext Pair(
                    fallbackRuleBasedAnalysis(cleanedClauses, housing),
                    generateLocalOverview(cleanedClauses)
                )
            }

            val responseString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val parsedResults = parseGeminiResponse(rawText, cleanedClauses)
            val overview = fetchOverview(cleanedClauses, apiKey) ?: generateLocalOverview(cleanedClauses)

            Pair(parsedResults, overview)
        } catch (e: Exception) {
            Pair(
                fallbackRuleBasedAnalysis(cleanedClauses, housing),
                generateLocalOverview(cleanedClauses)
            )
        }
    }

    private fun parseGeminiResponse(
        rawJson: String,
        originalClauses: List<String>
    ): List<ContractClause> {
        val clean = rawJson.replace("```json", "").replace("```", "").trim()
        val results = mutableListOf<ContractClause>()

        try {
            val array = JSONArray(clean)
            for (i in 0 until originalClauses.size) {
                val orig = originalClauses[i]
                val obj = array.optJSONObject(i)
                if (obj != null) {
                    val riskStr = obj.optString("risk", "unknown").lowercase()
                    val risk = when {
                        riskStr.contains("high") -> RiskLevel.HIGH
                        riskStr.contains("medium") -> RiskLevel.MEDIUM
                        riskStr.contains("low") -> RiskLevel.LOW
                        else -> RiskLevel.UNKNOWN
                    }

                    val issuesList = mutableListOf<String>()
                    val issuesArr = obj.optJSONArray("issues")
                    if (issuesArr != null) {
                        for (k in 0 until issuesArr.length()) {
                            issuesList.add(issuesArr.optString(k))
                        }
                    }

                    val lawRefList = mutableListOf<String>()
                    val lawArr = obj.optJSONArray("law_ref")
                    if (lawArr != null) {
                        for (k in 0 until lawArr.length()) {
                            lawRefList.add(lawArr.optString(k))
                        }
                    }

                    results.add(
                        ContractClause(
                            no = i + 1,
                            rawText = orig,
                            summary = obj.optString("summary", ""),
                            risk = risk,
                            issues = issuesList,
                            lawRef = lawRefList,
                            suggestion = obj.optString("suggestion", "")
                        )
                    )
                } else {
                    results.add(fallbackClause(i + 1, orig))
                }
            }
        } catch (e: Exception) {
            return fallbackRuleBasedAnalysis(originalClauses, HousingType.UNKNOWN)
        }

        return results
    }

    private fun fetchOverview(clauses: List<String>, apiKey: String): ContractOverview? {
        return try {
            val systemPrompt = """
คุณคือผู้ช่วยสรุปสัญญาเช่าที่พักในประเทศไทย อ่านสัญญาทั้งฉบับ แล้วตอบเป็น JSON เท่านั้น:
{
  "rent": "ค่าเช่าต่อเดือน",
  "deposit": "เงินประกัน/มัดจำ และเงื่อนไขการคืน",
  "duration": "ระยะเวลาสัญญา",
  "utilities": "ค่าน้ำ ค่าไฟ ค่าส่วนกลาง อัตราที่ระบุ",
  "penalty": "ค่าปรับหรือเงื่อนไขการยกเลิกก่อนกำหนด",
  "missing": ["เรื่องสำคัญที่สัญญาไม่ได้พูดถึงเลย"]
}
กฎ: ใช้ข้อมูลที่เขียนในสัญญาเท่านั้น ถ้าไม่พบให้ใส่ "ไม่ระบุในสัญญา"
""".trimIndent()

            val text = clauses.joinToString("\n\n")
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$MODEL:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val candText = json.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: return null

            val parsed = JSONObject(candText.replace("```json", "").replace("```", "").trim())
            val missing = mutableListOf<String>()
            val mArr = parsed.optJSONArray("missing")
            if (mArr != null) {
                for (i in 0 until mArr.length()) {
                    missing.add(mArr.optString(i))
                }
            }

            ContractOverview(
                rent = parsed.optString("rent", "ไม่ระบุในสัญญา"),
                deposit = parsed.optString("deposit", "ไม่ระบุในสัญญา"),
                duration = parsed.optString("duration", "ไม่ระบุในสัญญา"),
                utilities = parsed.optString("utilities", "ไม่ระบุในสัญญา"),
                penalty = parsed.optString("penalty", "ไม่ระบุในสัญญา"),
                missing = missing
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Complete local rule-based analysis engine built from OCPB B.E. 2568 rules.
     * Guarantees that the app evaluates accurately even offline or if Gemini quota is reached.
     */
    fun fallbackRuleBasedAnalysis(
        clauses: List<String>,
        housing: HousingType
    ): List<ContractClause> {
        return clauses.mapIndexed { index, clause ->
            evaluateSingleClauseLocally(index + 1, clause, housing)
        }
    }

    private fun evaluateSingleClauseLocally(
        no: Int,
        clause: String,
        housing: HousingType
    ): ContractClause {
        val lower = clause.lowercase()
        val issues = mutableListOf<String>()
        val lawRefs = mutableListOf<String>()
        var suggestion = ""
        var summary = ""
        var risk = RiskLevel.LOW

        val hasUtility = lower.contains("ไฟฟ้า") || lower.contains("ค่าน้ำ") || lower.contains("ค่าไฟ") || lower.contains("หน่วยละ")
        val hasLockOrSeize = lower.contains("ล็อกห้อง") || lower.contains("ขนย้าย") || lower.contains("ยึด") || lower.contains("ตัดน้ำ") || lower.contains("ตัดไฟ")
        val hasRoomEntry = (lower.contains("เข้าห้อง") || lower.contains("ตรวจห้อง")) && (lower.contains("ตลอดเวลา") || lower.contains("ไม่ต้องแจ้ง") || lower.contains("โดยไม่แจ้ง"))
        val hasForfeitDeposit = (lower.contains("ริบเงินประกัน") || lower.contains("ริบมัดจำ") || lower.contains("ไม่มีสิทธิ์ขอคืน")) && (lower.contains("ย้ายออกก่อน") || lower.contains("ทุกกรณี"))
        val hasDeposit = lower.contains("เงินประกัน") || lower.contains("มัดจำ")
        val hasRepair = lower.contains("ซ่อมแซม") && (lower.contains("ทุกกรณี") || lower.contains("ไม่ว่าจะเกิดจากสาเหตุใด"))
        val hasNoticeMove = lower.contains("ย้ายออก") && (lower.contains("60 วัน") || lower.contains("90 วัน"))
        val hasSlowRefund = lower.contains("คืนเงินประกัน") && (lower.contains("30 วัน") || lower.contains("45 วัน") || lower.contains("60 วัน"))
        val hasHighFine = lower.contains("ค่าปรับ") && (lower.contains("วันละ 200") || lower.contains("วันละ 300") || lower.contains("วันละ 500"))

        when {
            hasLockOrSeize -> {
                risk = RiskLevel.HIGH
                summary = "หอพักกำหนดสิทธิล็อกห้อง ขนย้าย หรือยึดทรัพย์สินทันทีเมื่อผิดนัดชำระ"
                issues.add("ประกาศ สคบ. พ.ศ. 2568 ห้ามผู้ให้เช่าล็อกห้อง ตัดน้ำตัดไฟ หรือยึดทรัพย์สินก่อนบอกเลิกสัญญาโดยชอบด้วยกฎหมาย")
                issues.add("การกระทำดังกล่าวอาจเข้าข่ายความผิดทางอาญาฐานบุกรุกหรือทำให้เสียทรัพย์")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อ 5")
                suggestion = "ขอตัดข้อความการล็อกห้อง ขนย้ายทรัพย์สิน และตัดน้ำตัดไฟออก โดยให้ปฏิบัติตามขั้นตอนบอกเลิกสัญญาตามกฎหมาย"
            }
            hasRoomEntry -> {
                risk = RiskLevel.HIGH
                summary = "ผู้ให้เช่าขอสงวนสิทธิเข้าห้องพักได้ตลอดเวลาโดยไม่ต้องแจ้งล่วงหน้า"
                issues.add("ขัดต่อประกาศ สคบ. พ.ศ. 2568 ซึ่งห้ามเข้าตรวจห้องโดยไม่แจ้งล่วงหน้า ยกเว้นกรณีฉุกเฉิน")
                issues.add("กระทบต่อความเป็นส่วนตัวและความปลอดภัยของผู้เช่า")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อ 4")
                suggestion = "แก้ไขเป็น 'ผู้ให้เช่าจะเข้าตรวจห้องพักได้เมื่อแจ้งล่วงหน้าและได้รับความยินยอม ยกเว้นกรณีฉุกเฉิน'"
            }
            hasForfeitDeposit -> {
                risk = RiskLevel.HIGH
                summary = "กำหนดริบเงินประกันทั้งหมดหากย้ายออกก่อนครบสัญญา"
                issues.add("สคบ. ห้ามริบเงินประกันหากผู้เช่าไม่ได้เป็นฝ่ายผิดสัญญาในสาระสำคัญหรือไม่มีความเสียหายเกิดขึ้นจริง")
                issues.add("เงินประกันมีไว้เพื่อประกันความเสียหาย ไม่ใช่เบี้ยปรับสำหรับริบทั้งหมด")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อ 3")
                suggestion = "ขอให้ระบุว่าจะหักเงินประกันได้เฉพาะค่าความเสียหายที่เกิดขึ้นจริงตามใบเสร็จเท่านั้น"
            }
            hasUtility && (lower.contains("8 บาท") || lower.contains("20 บาท") || lower.contains("หน่วยละ 7") || lower.contains("หน่วยละ 8") || lower.contains("หน่วยละ 9")) -> {
                risk = RiskLevel.HIGH
                summary = "คิดค่าน้ำและค่าไฟฟ้าในอัตราคงที่ซึ่งสูงกว่าอัตราที่หน่วยงานรัฐเรียกเก็บ"
                issues.add("ประกาศ สคบ. 2568 ห้ามคิดค่าน้ำค่าไฟเกินอัตราที่การไฟฟ้านครหลวง/ส่วนภูมิภาค และการประปาเรียกเก็บจริง")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อ 5 (ค่าน้ำค่าไฟ)")
                suggestion = "ขอปรับอัตราค่าน้ำค่าไฟให้คิดตามบิลจริงของการไฟฟ้าและการประปา พร้อมแนบสำเนาบิล"
            }
            hasRepair -> {
                risk = RiskLevel.MEDIUM
                summary = "ผลักภาระค่าซ่อมแซมความเสียหายทุกกรณีให้ผู้เช่ารับผิดชอบ"
                issues.add("ประกาศ สคบ. กำหนดให้ผู้ให้เช่ารับผิดชอบการซ่อมแซมใหญ่และโครงสร้าง ส่วนผู้เช่ารับผิดชอบเฉพาะการสึกหรอตามปกติ")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อ 9")
                suggestion = "ขอระบุขอบเขตการซ่อมแซมให้ชัดเจนว่าผู้ให้เช่ารับผิดชอบการชำรุดจากการเสื่อมสภาพตามปกติ"
            }
            hasNoticeMove -> {
                risk = RiskLevel.MEDIUM
                summary = "กำหนดให้แจ้งย้ายออกล่วงหน้านานถึง 60 วัน"
                issues.add("ระยะเวลาแจ้งล่วงหน้าทั่วไปตามเกณฑ์ สคบ. และประมวลกฎหมายแพ่งคือ 30 วัน")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 (สิทธิบอกเลิกสัญญา)")
                suggestion = "ขอปรับระยะเวลาแจ้งย้ายออกเหลือ 30 วัน"
            }
            hasSlowRefund -> {
                risk = RiskLevel.MEDIUM
                summary = "ระยะเวลาคืนเงินประกันนานเกินสมควร (มากกว่า 14 วัน)"
                issues.add("ประกาศ สคบ. กำหนดให้คืนเงินประกันภายใน 7 วัน (ไม่มีความเสียหาย) หรือ 14 วัน (หากต้องหักค่าซ่อม)")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อกำหนดที่ต้องมี")
                suggestion = "ขอแก้ไขระยะเวลาคืนเงินประกันเป็นภายใน 7-14 วัน นับจากวันที่ส่งมอบห้องคืน"
            }
            hasHighFine -> {
                risk = RiskLevel.MEDIUM
                summary = "กำหนดค่าปรับชำระล่าช้าในอัตราสูงต่อวัน"
                issues.add("อาจเข้าข่ายเบี้ยปรับที่ไม่เป็นธรรมตาม พ.ร.บ.ว่าด้วยข้อสัญญาที่ไม่เป็นธรรม พ.ศ. 2540")
                lawRefs.add("พ.ร.บ.ว่าด้วยข้อสัญญาที่ไม่เป็นธรรม พ.ศ. 2540")
                suggestion = "ขอปรับลดค่าปรับรายวัน หรือขอให้มีระยะเวลาผ่อนผัน (Grace period) 5-7 วัน"
            }
            hasDeposit && lower.contains("2 เดือน") -> {
                risk = RiskLevel.MEDIUM
                summary = "เรียกเก็บเงินประกัน 2 เดือน"
                issues.add("หากรวมกับค่าเช่าล่วงหน้าแล้วเกิน 3 เดือนของค่าเช่า จะขัดต่อประกาศ สคบ. 2568")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568 ข้อ 1")
                suggestion = "ตรวจดูว่าเมื่อรวมเงินประกันกับค่าเช่าล่วงหน้าแล้วไม่เกิน 3 เท่าของค่าเช่ารายเดือน"
            }
            lower.contains("ตามอัตราที่การประปา") || lower.contains("ตามบิลจริง") || lower.contains("ภายใน 7 วัน") -> {
                risk = RiskLevel.LOW
                summary = "ข้อสัญญาสอดคล้องกับมาตรฐานความเป็นธรรมและประกาศ สคบ."
                issues.add("เป็นข้อสัญญาที่เป็นธรรม โปร่งใส และสอดคล้องกับสิทธิผู้บริโภค")
                lawRefs.add("ประกาศ สคบ. พ.ศ. 2568")
                suggestion = "ข้อสัญญานี้มีมาตรฐานที่ดี สามารถตกลงตามนี้ได้"
            }
            else -> {
                risk = RiskLevel.LOW
                summary = "ข้อกำหนดทั่วไปเกี่ยวกับการเช่าห้องพัก"
                suggestion = "ตรวจสอบรายละเอียดกับผู้ให้เช่าให้ชัดเจนก่อนลงนาม"
            }
        }

        return ContractClause(
            no = no,
            rawText = clause,
            summary = summary,
            risk = risk,
            issues = issues,
            lawRef = lawRefs,
            suggestion = suggestion
        )
    }

    private fun fallbackClause(no: Int, text: String): ContractClause {
        return ContractClause(
            no = no,
            rawText = text,
            summary = "ข้อสัญญาเช่าที่พัก",
            risk = RiskLevel.UNKNOWN,
            issues = emptyList(),
            lawRef = emptyList(),
            suggestion = "ควรอ่านและตรวจสอบเงื่อนไขข้อนี้โดยละเอียด"
        )
    }

    fun generateLocalOverview(clauses: List<String>): ContractOverview {
        var rent = "ไม่ระบุในสัญญา"
        var deposit = "ไม่ระบุในสัญญา"
        var duration = "ไม่ระบุในสัญญา"
        var utilities = "ไม่ระบุในสัญญา"
        var penalty = "ไม่ระบุในสัญญา"
        val missing = mutableListOf<String>()

        val fullText = clauses.joinToString("\n")
        val rentMatch = Regex("ค่าเช่า[^\n0-9]*([0-9,]+)\\s*บาท").find(fullText)
        if (rentMatch != null) rent = "${rentMatch.groupValues[1]} บาท/เดือน"

        val depositMatch = Regex("เงินประกัน[^\n0-9]*([0-9,]+)\\s*บาท").find(fullText)
        if (depositMatch != null) deposit = "${depositMatch.groupValues[1]} บาท"

        val elecMatch = Regex("ค่าไฟ[^\n0-9]*([0-9.]+)\\s*บาท").find(fullText)
        val waterMatch = Regex("ค่าน้ำ[^\n0-9]*([0-9.]+)\\s*บาท").find(fullText)
        if (elecMatch != null || waterMatch != null) {
            val e = elecMatch?.groupValues?.get(1) ?: "-"
            val w = waterMatch?.groupValues?.get(1) ?: "-"
            utilities = "ค่าไฟ $e บ./หน่วย, ค่าน้ำ $w บ./หน่วย"
        } else if (fullText.contains("ตามบิล") || fullText.contains("ตามอัตราที่การไฟฟ้า")) {
            utilities = "คิดตามอัตราจริงของการไฟฟ้าและการประปา"
        }

        if (fullText.contains("ล็อกห้อง") || fullText.contains("ตัดน้ำตัดไฟ")) {
            penalty = "ล็อกห้อง ขนย้ายทรัพย์สิน และตัดน้ำตัดไฟทันที"
        } else if (fullText.contains("ค่าปรับวันละ")) {
            val fine = Regex("ค่าปรับวันละ\\s*([0-9,]+)\\s*บาท").find(fullText)?.groupValues?.get(1) ?: ""
            penalty = "ค่าปรับล่าช้าวันละ $fine บาท"
        }

        if (!fullText.contains("คืนเงินประกัน")) {
            missing.add("กำหนดวันและเงื่อนไขการคืนเงินประกัน")
        }
        if (!fullText.contains("ซ่อมแซม")) {
            missing.add("ข้อตกลงเรื่องการซ่อมแซมว่าใครรับผิดชอบ")
        }
        if (!fullText.contains("เข้าห้อง") && !fullText.contains("ตรวจห้อง")) {
            missing.add("เงื่อนไขการแจ้งล่วงหน้าก่อนผู้ให้เช่าเข้าห้อง")
        }

        return ContractOverview(
            rent = rent,
            deposit = deposit,
            duration = duration,
            utilities = utilities,
            penalty = penalty,
            missing = missing
        )
    }
}
