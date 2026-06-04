package com.example.myapplication.domain.layer


data class OnboardingModel(
    val resImage: Int, val resTitle: Int, val resDescription: Int
) {
    companion object {
        const val FULL_NATIVE_FLAG = 1822
    }
}