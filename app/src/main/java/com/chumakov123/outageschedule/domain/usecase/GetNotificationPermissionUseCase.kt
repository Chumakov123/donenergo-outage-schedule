package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.util.NotificationPermissionChecker

class GetNotificationPermissionUseCase(
    private val permissionChecker: NotificationPermissionChecker
) {
    operator fun invoke(): Boolean = permissionChecker.areNotificationsEnabled()
}