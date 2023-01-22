package com.github.zibnix.droidbones.api

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class ResultCallAdapter (private val resultType: Type) : CallAdapter<Type, Call<ApiResult<Type>>> {
    override fun responseType(): Type = resultType

    override fun adapt(call: Call<Type>): Call<ApiResult<Type>> {
        return ResultCall(call)
    }
}