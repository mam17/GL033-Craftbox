package com.grl.sms_wa.domain.layer

import androidx.annotation.StringRes

data class NotificationContentModel(
    @param:StringRes val resTitle: Int,
    @param:StringRes val resBody: Int
)
