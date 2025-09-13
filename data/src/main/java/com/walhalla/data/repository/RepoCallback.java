package com.walhalla.data.repository;

public interface RepoCallback<T> {
    void successResult(T data);

    void errorResult(String err);
}
