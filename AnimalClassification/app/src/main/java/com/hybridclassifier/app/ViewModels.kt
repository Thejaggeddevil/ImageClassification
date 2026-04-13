package com.hybridclassifier.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hybridclassifier.app.data.ClassifierRepository
import com.hybridclassifier.app.data.Result
import com.hybridclassifier.app.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ─────────────────────────────────────────────
//  APP VM  (dark mode, role)
// ─────────────────────────────────────────────
@HiltViewModel
class AppViewModel @Inject constructor(private val repo: ClassifierRepository) : ViewModel() {
    val darkMode  = repo.darkModeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isLoggedIn = repo.tokenFlow.map { it != null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val role = repo.roleFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "user")
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { repo.setDarkMode(enabled) }
}

// ─────────────────────────────────────────────
//  LOGIN VM
// ─────────────────────────────────────────────
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val role: String = "user"
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: ClassifierRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = LoginUiState(error = "Please fill in all fields"); return
        }
        viewModelScope.launch {
            _state.value = LoginUiState(isLoading = true)
            _state.value = when (val r = repo.login(username, password)) {
                is Result.Success -> LoginUiState(isSuccess = true, role = r.data.role)
                is Result.Error   -> LoginUiState(error = r.message)
                else -> LoginUiState()
            }
        }
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }
}

// ─────────────────────────────────────────────
//  SIGNUP VM
// ─────────────────────────────────────────────
data class SignupUiState(val isLoading: Boolean = false, val error: String? = null, val isSuccess: Boolean = false)

@HiltViewModel
class SignupViewModel @Inject constructor(private val repo: ClassifierRepository) : ViewModel() {
    private val _state = MutableStateFlow(SignupUiState())
    val state = _state.asStateFlow()

    fun signup(name: String, username: String, email: String, phone: String,
               password: String, childName: String, age: String) {
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
            _state.value = SignupUiState(error = "Please fill in all required fields"); return
        }
        if (password.length < 6) { _state.value = SignupUiState(error = "Password needs at least 6 characters"); return }
        if (!email.contains("@")) { _state.value = SignupUiState(error = "Invalid email address"); return }
        viewModelScope.launch {
            _state.value = SignupUiState(isLoading = true)
            val req = com.hybridclassifier.app.data.remote.SignupRequest(
                username=username, password=password, name=name,
                email=email, phone=phone, childName=childName, age=age.toIntOrNull() ?: 0)
            _state.value = when (val r = repo.signup(req)) {
                is Result.Success -> SignupUiState(isSuccess = true)
                is Result.Error   -> SignupUiState(error = r.message)
                else -> SignupUiState()
            }
        }
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }
}

// ─────────────────────────────────────────────
//  CLASSIFIER VM
// ─────────────────────────────────────────────
sealed class PredictState {
    object Idle    : PredictState()
    object Loading : PredictState()
    data class Success(val result: PredictionResponse) : PredictState()
    data class Error(val message: String) : PredictState()
}

data class ClassifierUiState(
    val predictState: PredictState = PredictState.Idle,
    val selectedImageBytes: ByteArray? = null
)

@HiltViewModel
class ClassifierViewModel @Inject constructor(private val repo: ClassifierRepository) : ViewModel() {
    private val _state = MutableStateFlow(ClassifierUiState())
    val state = _state.asStateFlow()
    val userName  = repo.userNameFlow
    val childName = repo.childNameFlow

    fun selectImage(bytes: ByteArray) {
        _state.value = ClassifierUiState(selectedImageBytes = bytes, predictState = PredictState.Idle)
    }

    fun predict() {
        val bytes = _state.value.selectedImageBytes ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(predictState = PredictState.Loading)
            _state.value = _state.value.copy(predictState = when (val r = repo.predict(bytes)) {
                is Result.Success -> PredictState.Success(r.data)
                is Result.Error   -> PredictState.Error(r.message)
                else -> PredictState.Idle
            })
        }
    }
    fun reset() { _state.value = ClassifierUiState() }
}

// ─────────────────────────────────────────────
//  SETTINGS VM
// ─────────────────────────────────────────────
data class SettingsUiState(
    val name: String = "", val username: String = "", val email: String = "",
    val childName: String = "", val age: Int = 0,
    val isSaved: Boolean = false, val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: ClassifierRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()
    val darkMode = repo.darkModeFlow
    val role     = repo.roleFlow

    init {
        viewModelScope.launch {
            combine(repo.userNameFlow, repo.usernameFlow, repo.emailFlow, repo.childNameFlow, repo.ageFlow)
            { name, username, email, childName, age ->
                SettingsUiState(name=name, username=username, email=email, childName=childName, age=age)
            }.collect { _state.value = it }
        }
    }

    fun updateName(v: String)      { _state.value = _state.value.copy(name = v, isSaved = false) }
    fun updateEmail(v: String)     { _state.value = _state.value.copy(email = v, isSaved = false) }
    fun updateChildName(v: String) { _state.value = _state.value.copy(childName = v, isSaved = false) }
    fun updateAge(v: String)       { _state.value = _state.value.copy(age = v.toIntOrNull() ?: 0, isSaved = false) }

    fun save() = viewModelScope.launch {
        val s = _state.value
        repo.updateProfile(s.name, s.email, s.childName, s.age)
        _state.value = _state.value.copy(isSaved = true)
    }

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { repo.setDarkMode(enabled) }
    fun logout() = viewModelScope.launch { repo.logout() }
}

// ─────────────────────────────────────────────
//  ADMIN VM  — full production version
// ─────────────────────────────────────────────
data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: AdminStats? = null,
    val users: List<AdminUser> = emptyList(),
    val predictions: List<AdminPrediction> = emptyList(),
    val customFacts: List<Map<String, Any>> = emptyList(),
    val classSettings: List<Map<String, Any>> = emptyList(),
    val logs: List<Map<String, Any>> = emptyList(),
    val addedEntries: List<Map<String, Any>> = emptyList(),
    val pendingUsers: List<Map<String, Any>> = emptyList(),
    val successMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(private val repo: ClassifierRepository) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state = _state.asStateFlow()

    init { loadAll() }

    fun loadAll() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val stats    = (repo.getAdminStats()       as? Result.Success)?.data
            val users    = (repo.getAdminUsers()       as? Result.Success)?.data ?: emptyList()
            val preds    = (repo.getAdminPredictions() as? Result.Success)?.data ?: emptyList()
            val facts    = (repo.getCustomFacts()      as? Result.Success)?.data ?: emptyList()
            val settings = (repo.getClassSettings()    as? Result.Success)?.data ?: emptyList()
            val logs     = (repo.getActivityLogs()     as? Result.Success)?.data ?: emptyList()
            val entries  = (repo.getAddedEntries()     as? Result.Success)?.data ?: emptyList()
            val pending  = (repo.getPendingUsers()     as? Result.Success)?.data ?: emptyList()
            _state.value = AdminUiState(
                isLoading    = false,
                stats        = stats,
                users        = users,
                predictions  = preds,
                customFacts  = facts,
                classSettings = settings,
                logs         = logs,
                addedEntries = entries,
                pendingUsers = pending
            )
        } catch (e: Exception) {
            _state.value = AdminUiState(isLoading = false,
                error = "Failed to load data. Check network connection.")
        }
    }

    fun toggleUser(username: String) = viewModelScope.launch {
        when (val r = repo.toggleUser(username)) {
            is Result.Success -> { _state.value = _state.value.copy(successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun saveCustomFacts(req: CustomFactsRequest) = viewModelScope.launch {
        when (val r = repo.saveCustomFacts(req)) {
            is Result.Success -> { _state.value = _state.value.copy(successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun deleteCustomFacts(className: String) = viewModelScope.launch {
        when (val r = repo.deleteCustomFacts(className)) {
            is Result.Success -> { _state.value = _state.value.copy(successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun updateClassSetting(req: ClassSettingRequest) = viewModelScope.launch {
        when (val r = repo.updateClassSetting(req)) {
            is Result.Success -> { _state.value = _state.value.copy(successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun addNewClass(categoryName: String, className: String, imageBytes: List<ByteArray>) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        when (val r = repo.addNewClass(categoryName, className, imageBytes)) {
            is Result.Success -> { _state.value = _state.value.copy(isLoading=false, successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(isLoading=false, error = r.message)
            else -> _state.value = _state.value.copy(isLoading=false)
        }
    }

    fun addNewCategory(categoryName: String, firstClassName: String, imageBytes: List<ByteArray>) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        when (val r = repo.addNewCategory(categoryName, firstClassName, imageBytes)) {
            is Result.Success -> { _state.value = _state.value.copy(isLoading=false, successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(isLoading=false, error = r.message)
            else -> _state.value = _state.value.copy(isLoading=false)
        }
    }


    fun getPendingUsers() = viewModelScope.launch {
        when (val r = repo.getPendingUsers()) {
            is Result.Success -> _state.value = _state.value.copy(pendingUsers = r.data)
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun approveUser(username: String) = viewModelScope.launch {
        when (val r = repo.approveUser(username)) {
            is Result.Success -> { _state.value = _state.value.copy(successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun rejectUser(username: String) = viewModelScope.launch {
        when (val r = repo.rejectUser(username)) {
            is Result.Success -> { _state.value = _state.value.copy(successMessage = r.data); loadAll() }
            is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            else -> {}
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(successMessage=null, error=null) }
}