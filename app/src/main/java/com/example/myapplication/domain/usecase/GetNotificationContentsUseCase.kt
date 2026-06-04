package com.example.myapplication.domain.usecase

import com.example.myapplication.R
import com.example.myapplication.domain.layer.NotificationContentModel
import javax.inject.Inject

class GetNotificationContentsUseCase @Inject constructor() :
    UseCase<GetNotificationContentsUseCase.Param, List<NotificationContentModel>>() {
    open class Param : UseCase.Param()

    override suspend fun execute(param: Param): List<NotificationContentModel> = listOf(
        NotificationContentModel(R.string.txt_noti_title_1, R.string.txt_noti_body_1),
        NotificationContentModel(R.string.txt_noti_title_2, R.string.txt_noti_body_2),
        NotificationContentModel(R.string.txt_noti_title_3, R.string.txt_noti_body_3),
        NotificationContentModel(R.string.txt_noti_title_4, R.string.txt_noti_body_4),
        NotificationContentModel(R.string.txt_noti_title_5, R.string.txt_noti_body_5),
        NotificationContentModel(R.string.txt_noti_title_6, R.string.txt_noti_body_6),
        NotificationContentModel(R.string.txt_noti_title_7, R.string.txt_noti_body_7),
        NotificationContentModel(R.string.txt_noti_title_8, R.string.txt_noti_body_8),
        NotificationContentModel(R.string.txt_noti_title_9, R.string.txt_noti_body_9),
        NotificationContentModel(R.string.txt_noti_title_10, R.string.txt_noti_body_10)
    )
}
