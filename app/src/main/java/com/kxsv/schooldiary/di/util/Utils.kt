package com.kxsv.schooldiary.di.util

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val appDispatcher: AppDispatchers)

enum class AppDispatchers {
	Default, IO,
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class GradeNotification

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class GradeSummaryNotification

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class TaskNotification

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class TaskSummaryNotification

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ScheduleNotification

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ScheduleSummaryNotification