package org.delcom.pam_p5_ifs18005.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestTodo (
    val title: String,
    val description: String,
    val isDone: Boolean = false
)
