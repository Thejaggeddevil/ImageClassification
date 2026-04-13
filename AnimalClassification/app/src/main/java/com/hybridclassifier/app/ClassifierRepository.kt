package com.hybridclassifier.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.hybridclassifier.app.data.remote.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore("kids_prefs")

private val TOKEN_KEY     = stringPreferencesKey("access_token")
private val USERNAME_KEY  = stringPreferencesKey("username")
private val NAME_KEY      = stringPreferencesKey("user_name")
private val EMAIL_KEY     = stringPreferencesKey("email")
private val ROLE_KEY      = stringPreferencesKey("role")
private val CHILD_KEY     = stringPreferencesKey("child_name")
private val AGE_KEY       = intPreferencesKey("age")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = 0) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class ClassifierRepository @Inject constructor(
    private val api: ClassifierApiService,
    @ApplicationContext private val context: Context
) {
    // ── Token stored in companion object so OkHttp interceptor can read it
    // without needing a Repository reference (avoids circular dependency)
    companion object {
        @Volatile
        var currentToken: String? = null
    }

    val tokenFlow     = context.dataStore.data.map { it[TOKEN_KEY] }
    val userNameFlow  = context.dataStore.data.map { it[NAME_KEY]     ?: "" }
    val roleFlow      = context.dataStore.data.map { it[ROLE_KEY]     ?: "user" }
    val darkModeFlow  = context.dataStore.data.map { it[DARK_MODE_KEY] ?: false }
    val childNameFlow = context.dataStore.data.map { it[CHILD_KEY]    ?: "" }
    val emailFlow     = context.dataStore.data.map { it[EMAIL_KEY]    ?: "" }
    val usernameFlow  = context.dataStore.data.map { it[USERNAME_KEY] ?: "" }
    val ageFlow       = context.dataStore.data.map { it[AGE_KEY]      ?: 0 }

    init {
        // On app start: restore token from DataStore into memory
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val saved = tokenFlow.first()
                if (saved != null) currentToken = saved
            } catch (e: Exception) { /* ignore */ }
        }
    }

    // ── Auth ──────────────────────────────────
    suspend fun login(username: String, password: String): Result<LoginResponse> = safeCall {
        val r = api.login(username, password)
        if (r.isSuccessful && r.body() != null) {
            val body = r.body()!!
            // Set token in memory IMMEDIATELY — before DataStore write
            currentToken = body.accessToken
            saveSession(body, username)
            Result.Success(body)
        } else {
            val errorBody = try { r.errorBody()?.string() ?: "" } catch (e: Exception) { "" }
            val detail: String = when {
                errorBody.contains("PENDING_APPROVAL") ->
                    "PENDING_APPROVAL:Your account is pending admin approval. Please wait."
                errorBody.contains("REJECTED") ->
                    "REJECTED:Your account has been rejected by admin. Contact support."
                r.code() == 401 -> "Wrong username or password"
                r.code() == 403 -> {
                    try {
                        org.json.JSONObject(errorBody).optString("detail", "Account not approved")
                    } catch (e: Exception) { "Account not approved" }
                }
                else -> "Login failed. Please try again."
            }
            Result.Error(detail, r.code())
        }
    }

    suspend fun signup(req: SignupRequest): Result<String> = safeCall {
        val r = api.signup(req)
        if (r.isSuccessful && r.body() != null) {
            Result.Success(r.body()!!.message)
        } else {
            val errorBody = try { r.errorBody()?.string() ?: "" } catch (e: Exception) { "" }
            val msg = when (r.code()) {
                400 -> {
                    if (errorBody.contains("username", ignoreCase = true)) "Username already taken. Try another one."
                    else if (errorBody.contains("email", ignoreCase = true)) "Email already registered."
                    else "Please check your details and try again."
                }
                else -> "Signup failed. Please try again."
            }
            Result.Error(msg, r.code())
        }
    }

    suspend fun logout() {
        currentToken = null
        context.dataStore.edit { it.clear() }
    }

    suspend fun setDarkMode(enabled: Boolean) =
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }

    suspend fun updateProfile(name: String, email: String, childName: String, age: Int) {
        context.dataStore.edit {
            it[NAME_KEY]  = name
            it[EMAIL_KEY] = email
            it[CHILD_KEY] = childName
            it[AGE_KEY]   = age
        }
    }

    private suspend fun saveSession(res: LoginResponse, username: String = "") {
        context.dataStore.edit {
            it[TOKEN_KEY]   = res.accessToken
            it[NAME_KEY]    = res.userName
            it[ROLE_KEY]    = res.role
            it[EMAIL_KEY]   = res.email
            if (username.isNotEmpty()) it[USERNAME_KEY] = username
        }
    }

    // ── Predict ───────────────────────────────
    suspend fun predict(imageBytes: ByteArray): Result<PredictionResponse> = safeCall {
        val body = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "image.jpg", body)
        val r = api.predict(part)
        if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!)
        else Result.Error(when (r.code()) {
            401 -> "Session expired. Please login again."
            503 -> "Model not ready on server."
            422 -> "Could not identify this image type."
            else -> "Prediction failed (${r.code()})"
        }, r.code())
    }

    // ── Admin ─────────────────────────────────
    suspend fun getAdminStats(): Result<AdminStats> = safeCall {
        val r = api.getAdminStats()
        if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!)
        else Result.Error("Failed to load stats", r.code())
    }

    suspend fun getAdminUsers(): Result<List<AdminUser>> = safeCall {
        val r = api.getAdminUsers()
        if (r.isSuccessful) Result.Success(r.body()?.get("users") ?: emptyList())
        else Result.Error("Failed to load users", r.code())
    }

    suspend fun toggleUser(username: String): Result<String> = safeCall {
        val r = api.toggleUser(username)
        if (r.isSuccessful) Result.Success(r.body()?.get("message") ?: "Done")
        else Result.Error("Failed", r.code())
    }

    suspend fun getAdminPredictions(): Result<List<AdminPrediction>> = safeCall {
        val r = api.getAdminPredictions(50)
        if (r.isSuccessful) Result.Success(r.body()?.get("predictions") ?: emptyList())
        else Result.Error("Failed to load predictions", r.code())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getCustomFacts(): Result<List<Map<String, Any>>> = safeCall {
        val r = api.getCustomFacts()
        if (r.isSuccessful) Result.Success(
            r.body()?.get("custom_facts") as? List<Map<String, Any>> ?: emptyList()
        ) else Result.Error("Failed", r.code())
    }

    suspend fun saveCustomFacts(req: CustomFactsRequest): Result<String> = safeCall {
        val r = api.addCustomFacts(req)
        if (r.isSuccessful) Result.Success(r.body()?.get("message") ?: "Saved")
        else Result.Error("Failed", r.code())
    }

    suspend fun deleteCustomFacts(className: String): Result<String> = safeCall {
        val r = api.deleteCustomFacts(className)
        if (r.isSuccessful) Result.Success(r.body()?.get("message") ?: "Deleted")
        else Result.Error("Failed", r.code())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getClassSettings(): Result<List<Map<String, Any>>> = safeCall {
        val r = api.getClassSettings()
        if (r.isSuccessful) Result.Success(
            r.body()?.get("class_settings") as? List<Map<String, Any>> ?: emptyList()
        ) else Result.Error("Failed", r.code())
    }

    suspend fun updateClassSetting(req: ClassSettingRequest): Result<String> = safeCall {
        val r = api.updateClassSetting(req)
        if (r.isSuccessful) Result.Success(r.body()?.get("message") ?: "Saved")
        else Result.Error("Failed", r.code())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getActivityLogs(): Result<List<Map<String, Any>>> = safeCall {
        val r = api.getActivityLogs(100)
        if (r.isSuccessful) Result.Success(
            r.body()?.get("logs") as? List<Map<String, Any>> ?: emptyList()
        ) else Result.Error("Failed", r.code())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getAddedEntries(): Result<List<Map<String, Any>>> = safeCall {
        val r = api.getAddedEntries()
        if (r.isSuccessful) Result.Success(
            r.body()?.get("entries") as? List<Map<String, Any>> ?: emptyList()
        ) else Result.Error("Failed", r.code())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getPendingUsers(): Result<List<Map<String, Any>>> = safeCall {
        val r = api.getPendingUsers()
        if (r.isSuccessful) Result.Success(
            r.body()?.get("pending_users") as? List<Map<String, Any>> ?: emptyList()
        ) else Result.Error("Failed", r.code())
    }

    suspend fun approveUser(username: String): Result<String> = safeCall {
        val r = api.approveUser(username)
        if (r.isSuccessful) Result.Success(r.body()?.get("message") ?: "Approved")
        else Result.Error("Failed to approve", r.code())
    }

    suspend fun rejectUser(username: String): Result<String> = safeCall {
        val r = api.rejectUser(username)
        if (r.isSuccessful) Result.Success(r.body()?.get("message") ?: "Rejected")
        else Result.Error("Failed to reject", r.code())
    }

    suspend fun addNewClass(
        categoryName: String,
        className: String,
        imageBytes: List<ByteArray>
    ): Result<String> = safeCall {
        val catPart = categoryName.toRequestBody("text/plain".toMediaTypeOrNull())
        val clsPart = className.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileParts = imageBytes.mapIndexed { i, bytes ->
            MultipartBody.Part.createFormData(
                "files", "image_$i.jpg",
                bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }
        val r = api.addNewClass(catPart, clsPart, fileParts)
        if (r.isSuccessful) Result.Success(r.body()?.get("message")?.toString() ?: "Class added")
        else Result.Error("Failed (${r.code()})")
    }

    suspend fun addNewCategory(
        categoryName: String,
        firstClassName: String,
        imageBytes: List<ByteArray>
    ): Result<String> = safeCall {
        val catPart = categoryName.toRequestBody("text/plain".toMediaTypeOrNull())
        val clsPart = firstClassName.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileParts = imageBytes.mapIndexed { i, bytes ->
            MultipartBody.Part.createFormData(
                "files", "image_$i.jpg",
                bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }
        val r = api.addNewCategory(catPart, clsPart, fileParts)
        if (r.isSuccessful) Result.Success(r.body()?.get("message")?.toString() ?: "Category added")
        else Result.Error("Failed (${r.code()})")
    }

    // ── Helper ────────────────────────────────
    private suspend fun <T> safeCall(block: suspend () -> Result<T>): Result<T> = try {
        block()
    } catch (e: Exception) {
        Result.Error("Network error: ${e.localizedMessage ?: "Unknown"}")
    }
}