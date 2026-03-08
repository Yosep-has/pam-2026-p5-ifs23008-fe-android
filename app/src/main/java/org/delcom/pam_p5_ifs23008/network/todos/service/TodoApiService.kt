package org.delcom.pam_p5_ifs23008.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23008.network.data.ResponseMessage
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestAuthLogin
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestAuthLogout
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestAuthRefreshToken
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestAuthRegister
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23008.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseAuthLogin
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseAuthRegister
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseTodo
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseTodoAdd
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseTodoStats
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseTodos
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseUser
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TodoApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun postRegister(
        @Body request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    @POST("auth/login")
    suspend fun postLogin(
        @Body request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    @POST("auth/logout")
    suspend fun postLogout(
        @Body request: RequestAuthLogout
    ): ResponseMessage<String?>

    @POST("auth/refresh-token")
    suspend fun postRefreshToken(
        @Body request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ── Users ─────────────────────────────────────────────────────────────────

    @GET("users/me")
    suspend fun getUserMe(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseUser?>

    @PUT("users/me")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChange,
    ): ResponseMessage<String?>

    @PUT("users/me/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChangePassword,
    ): ResponseMessage<String?>

    @Multipart
    @PUT("users/me/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ── Todos ─────────────────────────────────────────────────────────────────

    // Statistik ringkasan todo untuk halaman Home
    @GET("todos/stats")
    suspend fun getTodoStats(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseTodoStats?>

    // Daftar todo dengan pagination, filter status, dan search
    // isDone: null = semua, "true" = selesai, "false" = belum selesai
    @GET("todos")
    suspend fun getTodos(
        @Header("Authorization") authToken: String,
        @Query("search") search: String? = null,
        @Query("isDone") isDone: String? = null,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
    ): ResponseMessage<ResponseTodos?>

    @POST("todos")
    suspend fun postTodo(
        @Header("Authorization") authToken: String,
        @Body request: RequestTodo
    ): ResponseMessage<ResponseTodoAdd?>

    @GET("todos/{todoId}")
    suspend fun getTodoById(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String
    ): ResponseMessage<ResponseTodo?>

    @PUT("todos/{todoId}")
    suspend fun putTodo(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String,
        @Body request: RequestTodo
    ): ResponseMessage<String?>

    @Multipart
    @PUT("todos/{todoId}/cover")
    suspend fun putTodoCover(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @DELETE("todos/{todoId}")
    suspend fun deleteTodo(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String
    ): ResponseMessage<String?>
}