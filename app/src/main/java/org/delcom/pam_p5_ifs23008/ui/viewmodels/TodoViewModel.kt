package org.delcom.pam_p5_ifs23008.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseTodoStatsData
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23008.network.todos.service.ITodoRepository
import javax.inject.Inject

// ── UI States ─────────────────────────────────────────────────────────────────

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface TodoStatsUIState {
    data class Success(val data: ResponseTodoStatsData) : TodoStatsUIState
    data class Error(val message: String) : TodoStatsUIState
    object Loading : TodoStatsUIState
}

sealed interface TodosUIState {
    data class Success(val data: List<ResponseTodoData>) : TodosUIState
    data class Error(val message: String) : TodosUIState
    object Loading : TodosUIState
}

sealed interface TodoUIState {
    data class Success(val data: ResponseTodoData) : TodoUIState
    data class Error(val message: String) : TodoUIState
    object Loading : TodoUIState
}

sealed interface TodoActionUIState {
    data class Success(val message: String) : TodoActionUIState
    data class Error(val message: String) : TodoActionUIState
    object Loading : TodoActionUIState
}

// ── State Holder ──────────────────────────────────────────────────────────────

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val profileChange: TodoActionUIState = TodoActionUIState.Loading,
    val profileChangePassword: TodoActionUIState = TodoActionUIState.Loading,
    val profileChangePhoto: TodoActionUIState = TodoActionUIState.Loading,

    val todoStats: TodoStatsUIState = TodoStatsUIState.Loading,

    val todos: TodosUIState = TodosUIState.Loading,
    // Pagination info
    val todosPage: Int = 1,
    val todosHasNextPage: Boolean = false,

    var todo: TodoUIState = TodoUIState.Loading,
    var todoAdd: TodoActionUIState = TodoActionUIState.Loading,
    var todoChange: TodoActionUIState = TodoActionUIState.Loading,
    var todoDelete: TodoActionUIState = TodoActionUIState.Loading,
    var todoChangeCover: TodoActionUIState = TodoActionUIState.Loading,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    // ── Profile ───────────────────────────────────────────────────────────────

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching { repository.getUserMe(authToken) }.fold(
                    onSuccess = {
                        if (it.status == "success") ProfileUIState.Success(it.data!!.user)
                        else ProfileUIState.Error(it.message)
                    },
                    onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(profile = result)
            }
        }
    }

    fun putProfile(authToken: String, name: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChange = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putUserMe(authToken, RequestUserChange(name = name, username = username))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(profileChange = result)
            }
        }
    }

    fun putProfilePassword(authToken: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChangePassword = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putUserMePassword(
                        authToken,
                        RequestUserChangePassword(password = oldPassword, newPassword = newPassword)
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(profileChangePassword = result)
            }
        }
    }

    fun putProfilePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChangePhoto = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putUserMePhoto(authToken, file)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(profileChangePhoto = result)
            }
        }
    }

    // ── Todo Stats (Home) ─────────────────────────────────────────────────────

    fun getTodoStats(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoStats = TodoStatsUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching { repository.getTodoStats(authToken) }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoStatsUIState.Success(it.data!!.stats)
                        else TodoStatsUIState.Error(it.message)
                    },
                    onFailure = { TodoStatsUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todoStats = result)
            }
        }
    }

    // ── Todos (dengan pagination & filter) ────────────────────────────────────

    // Muat halaman pertama (reset list)
    fun getAllTodos(
        authToken: String,
        search: String? = null,
        isDone: String? = null,   // null = semua, "true" = selesai, "false" = belum
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(todos = TodosUIState.Loading, todosPage = 1) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.getTodos(authToken, search, isDone, page = 1, perPage = 10)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            val paginated = it.data!!.todos
                            _uiState.value = _uiState.value.copy(
                                todosPage = paginated.page,
                                todosHasNextPage = paginated.hasNextPage,
                            )
                            TodosUIState.Success(paginated.items)
                        } else {
                            TodosUIState.Error(it.message)
                        }
                    },
                    onFailure = { TodosUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todos = result)
            }
        }
    }

    // Muat halaman berikutnya (append ke list yang sudah ada)
    fun loadMoreTodos(
        authToken: String,
        search: String? = null,
        isDone: String? = null,
    ) {
        val currentState = _uiState.value
        if (!currentState.todosHasNextPage) return
        if (currentState.todos is TodosUIState.Loading) return

        val nextPage = currentState.todosPage + 1
        val existingItems = (currentState.todos as? TodosUIState.Success)?.data ?: emptyList()

        viewModelScope.launch {
            val result = runCatching {
                repository.getTodos(authToken, search, isDone, page = nextPage, perPage = 10)
            }.fold(
                onSuccess = {
                    if (it.status == "success") {
                        val paginated = it.data!!.todos
                        _uiState.update { state ->
                            state.copy(
                                todosPage = paginated.page,
                                todosHasNextPage = paginated.hasNextPage,
                                todos = TodosUIState.Success(existingItems + paginated.items)
                            )
                        }
                    }
                },
                onFailure = { /* abaikan error load more, list tetap tampil */ }
            )
        }
    }

    // ── Todo CRUD ─────────────────────────────────────────────────────────────

    fun postTodo(authToken: String, title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.postTodo(authToken, RequestTodo(title = title, description = description))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todoAdd = result)
            }
        }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching { repository.getTodoById(authToken, todoId) }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoUIState.Success(it.data!!.todo)
                        else TodoUIState.Error(it.message)
                    },
                    onFailure = { TodoUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todo = result)
            }
        }
    }

    fun putTodo(authToken: String, todoId: String, title: String, description: String, isDone: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChange = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putTodo(authToken, todoId, RequestTodo(title = title, description = description, isDone = isDone))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todoChange = result)
            }
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putTodoCover(authToken = authToken, todoId = todoId, file = file)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todoChangeCover = result)
            }
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.deleteTodo(authToken = authToken, todoId = todoId)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(todoDelete = result)
            }
        }
    }
}