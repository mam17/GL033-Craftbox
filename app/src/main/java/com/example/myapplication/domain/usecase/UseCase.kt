package com.example.myapplication.domain.usecase

abstract class UseCase<in P : UseCase.Param, out T> where T : Any {

    abstract suspend fun execute(param: P): T

    open class Param
}