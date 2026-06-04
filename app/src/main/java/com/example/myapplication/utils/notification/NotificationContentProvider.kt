package com.example.myapplication.utils.notification

import android.content.Context
import com.example.myapplication.domain.layer.NotificationContentModel
import com.example.myapplication.domain.usecase.GetNotificationContentsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

object NotificationContentProvider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationContentEntryPoint {
        fun getNotificationContentsUseCase(): GetNotificationContentsUseCase
    }

    private fun getUseCase(context: Context): GetNotificationContentsUseCase {
        return EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationContentEntryPoint::class.java
        ).getNotificationContentsUseCase()
    }

    suspend fun getRandomContent(context: Context): NotificationContentModel {
        val contents = getUseCase(context).execute(GetNotificationContentsUseCase.Param())
        return contents[Random.nextInt(contents.size)]
    }

    suspend fun getAllContents(context: Context): List<NotificationContentModel> {
        return getUseCase(context).execute(GetNotificationContentsUseCase.Param())
    }
}
