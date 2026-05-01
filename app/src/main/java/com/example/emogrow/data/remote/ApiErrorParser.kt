package com.example.emogrow.data.remote

import com.example.emogrow.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

object ApiErrorParser {

    fun parse(error: Throwable): String {
        if (error is HttpException) {
            val errorBody = error.response()?.errorBody()?.string()

            return try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Đã xảy ra lỗi"
            } catch (e: Exception) {
                when (error.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    401 -> "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn"
                    404 -> "Không tìm thấy dữ liệu"
                    422 -> "Thông tin nhập vào chưa hợp lệ"
                    500 -> "Lỗi hệ thống, vui lòng thử lại sau"
                    else -> "Đã xảy ra lỗi"
                }
            }
        }

        return error.message ?: "Không thể kết nối đến máy chủ"
    }
}