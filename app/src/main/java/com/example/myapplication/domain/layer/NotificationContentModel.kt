package com.example.myapplication.domain.layer

import androidx.annotation.StringRes

data class NotificationContentModel(
    @param:StringRes val resTitle: Int,
    @param:StringRes val resBody: Int
)
