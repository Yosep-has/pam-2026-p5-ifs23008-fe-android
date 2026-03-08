package org.delcom.pam_p5_ifs23008.network.todos.data

import kotlinx.serialization.Serializable

// ── Respons daftar todo (dengan pagination) ────────────────────────────────
@Serializable
data class ResponseTodos(
    val todos: ResponseTodosPaginated
)

@Serializable
data class ResponseTodosPaginated(
    val items: List<ResponseTodoData>,
    val page: Int,
    val perPage: Int,
    val total: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)

// ── Respons detail todo ────────────────────────────────────────────────────
@Serializable
data class ResponseTodo(
    val todo: ResponseTodoData
)

@Serializable
data class ResponseTodoData(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String,
    val isDone: Boolean = false,
    val cover: String? = null,
    val createdAt: String = "",
    var updatedAt: String = ""
)

@Serializable
data class ResponseTodoAdd(
    val todoId: String
)

// ── Respons statistik todo (untuk Home) ───────────────────────────────────
@Serializable
data class ResponseTodoStats(
    val stats: ResponseTodoStatsData
)

@Serializable
data class ResponseTodoStatsData(
    val total: Long,
    val done: Long,
    val notDone: Long,
)