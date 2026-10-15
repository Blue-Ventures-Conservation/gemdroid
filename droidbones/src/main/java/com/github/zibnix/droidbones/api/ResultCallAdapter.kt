package com.github.zibnix.droidbones.api

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class ResultCallAdapter(private val resultType: Type): CallAdapter<Type, Call<Result<Type>>> {
    override fun responseType(): Type = resultType

    override fun adapt(call: Call<Type>): Call<Result<Type>> {
        return ResultCall(call)
    }
}