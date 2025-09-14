package com.walhalla.data.repository

interface RepoCallback<T> {
    fun successResult(data: T)

    fun errorResult(err: String)
}