package com.hybridclassifier.app.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

// ── Auth ──────────────────────────────────────
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String = "bearer",
    @SerializedName("user_name")    val userName: String,
    val role: String,
    val email: String = "",
    val phone: String = "",
    @SerializedName("user_id")      val userId: Int = 0
)

data class SignupRequest(
    val username: String,
    val password: String,
    val name: String,
    val email: String,
    val phone: String = "",
    @SerializedName("child_name") val childName: String = "",
    val age: Int = 0
)

data class SignupResponse(
    val message: String,
    val status: String = "pending",
    val username: String = ""
)

// ── Prediction ────────────────────────────────
data class PredictionItem(val label: String, val confidence: Float)

data class ExplanationData(
    val title: String?,
    val short: String?,
    val facts: List<String>?,
    val quiz: String?,
    @SerializedName("safety_note")    val safetyNote: String?,
    @SerializedName("did_you_know")   val didYouKnow: String?,
    @SerializedName("where_found")    val whereFound: String?,
    @SerializedName("cool_ability")   val coolAbility: String?,
    @SerializedName("fun_comparison") val funComparison: String?
)

data class PredictionResponse(
    val category: String,
    @SerializedName("category_confidence")    val categoryConfidence: Float,
    @SerializedName("category_top3")          val categoryTop3: List<PredictionItem>,
    @SerializedName("final_class")            val finalClass: String,
    @SerializedName("final_class_confidence") val finalClassConfidence: Float,
    @SerializedName("final_class_top3")       val finalClassTop3: List<PredictionItem>,
    @SerializedName("display_name")           val displayName: String?,
    val explanation: ExplanationData?,
    @SerializedName("inference_time_ms")      val inferenceTimeMs: Float
)

// ── Admin ─────────────────────────────────────
data class AdminStats(
    @SerializedName("total_users")        val totalUsers: Int,
    @SerializedName("total_predictions")  val totalPredictions: Int,
    @SerializedName("active_users_today") val activeUsersToday: Int,
    @SerializedName("models_loaded")      val modelsLoaded: Int,
    @SerializedName("categories_count")   val categoriesCount: Int,
    @SerializedName("pending_approvals")  val pendingApprovals: Int = 0,
    @SerializedName("top_predictions")    val topPredictions: List<Map<String, Any>>
)

data class AdminUser(
    val id: Int,
    val username: String,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("child_name") val childName: String,
    val age: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_active")  val isActive: Int,
    @SerializedName("prediction_count") val predictionCount: Int = 0
)

data class AdminPrediction(
    val id: Int,
    val username: String,
    val category: String,
    @SerializedName("final_class")    val finalClass: String,
    @SerializedName("cat_confidence") val catConfidence: Float,
    @SerializedName("cls_confidence") val clsConfidence: Float,
    @SerializedName("inference_ms")   val inferenceMs: Float,
    @SerializedName("created_at")     val createdAt: String
)

data class CustomFactsRequest(
    @SerializedName("class_name")     val className: String,
    val category: String       = "",
    val title: String          = "",
    @SerializedName("short_desc")     val shortDesc: String = "",
    val facts: List<String>    = emptyList(),
    val quiz: String           = "",
    @SerializedName("safety_note")    val safetyNote: String = "",
    @SerializedName("did_you_know")   val didYouKnow: String = "",
    @SerializedName("where_found")    val whereFound: String = "",
    @SerializedName("cool_ability")   val coolAbility: String = "",
    @SerializedName("fun_comparison") val funComparison: String = ""
)

data class ClassSettingRequest(
    @SerializedName("class_name")   val className: String,
    @SerializedName("display_name") val displayName: String = "",
    @SerializedName("is_hidden")    val isHidden: Boolean = false
)

// ── Retrofit Service ──────────────────────────
// NOTE: No @Header("Authorization") anywhere.
// Auth token is added automatically by OkHttp interceptor in NetworkModule.
interface ClassifierApiService {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") u: String,
        @Field("password") p: String
    ): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body req: SignupRequest): Response<SignupResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<Map<String, Any>>

    @PUT("auth/me")
    suspend fun updateMe(@Body data: Map<String, Any>): Response<Map<String, Any>>

    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part file: MultipartBody.Part,
        @Query("explain") explain: Boolean = true
    ): Response<PredictionResponse>

    @GET("health")
    suspend fun health(): Response<Map<String, Any>>

    @GET("categories")
    suspend fun getCategories(): Response<Map<String, Any>>

    // ── Admin ─────────────────────────────────
    @GET("admin/stats")
    suspend fun getAdminStats(): Response<AdminStats>

    @GET("admin/users")
    suspend fun getAdminUsers(): Response<Map<String, List<AdminUser>>>

    @PUT("admin/users/{username}/toggle")
    suspend fun toggleUser(@Path("username") username: String): Response<Map<String, String>>

    @GET("admin/predictions")
    suspend fun getAdminPredictions(@Query("limit") limit: Int = 50): Response<Map<String, List<AdminPrediction>>>

    @GET("admin/custom-facts")
    suspend fun getCustomFacts(): Response<Map<String, Any>>

    @POST("admin/custom-facts")
    suspend fun addCustomFacts(@Body req: CustomFactsRequest): Response<Map<String, String>>

    @DELETE("admin/custom-facts/{className}")
    suspend fun deleteCustomFacts(@Path("className") className: String): Response<Map<String, String>>

    @GET("admin/class-settings")
    suspend fun getClassSettings(): Response<Map<String, Any>>

    @POST("admin/class-settings")
    suspend fun updateClassSetting(@Body req: ClassSettingRequest): Response<Map<String, String>>

    @GET("admin/logs")
    suspend fun getActivityLogs(@Query("limit") limit: Int = 100): Response<Map<String, Any>>

    @GET("admin/added-entries")
    suspend fun getAddedEntries(): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("admin/pending-users")
    suspend fun getPendingUsers(): Response<Map<String, @JvmSuppressWildcards Any>>

    @PUT("admin/approve-user/{username}")
    suspend fun approveUser(@Path("username") username: String): Response<Map<String, String>>

    @PUT("admin/reject-user/{username}")
    suspend fun rejectUser(@Path("username") username: String): Response<Map<String, String>>

    // ── Add Class / Category (FAISS pipeline) ─
    @Multipart
    @POST("admin/add-class")
    suspend fun addNewClass(
        @Part("category_name") categoryName: okhttp3.RequestBody,
        @Part("class_name") className: okhttp3.RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @Multipart
    @POST("admin/add-category")
    suspend fun addNewCategory(
        @Part("category_name") categoryName: okhttp3.RequestBody,
        @Part("first_class_name") firstClassName: okhttp3.RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<Map<String, @JvmSuppressWildcards Any>>
}