package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.MyGroupService
import com.ssafy.ssabree.core.datasource.remote.model.toNoticeRequest
import com.ssafy.ssabree.core.datasource.remote.model.toTaskRequest
import com.ssafy.ssabree.core.datasource.remote.model.toTaskStatusRequest
import com.ssafy.ssabree.core.repository.model.GroupApplicationModel
import com.ssafy.ssabree.core.repository.model.GroupNoticeCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupNoticeModel
import com.ssafy.ssabree.core.repository.model.GroupTaskCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupTaskModel
import com.ssafy.ssabree.core.repository.model.GroupTaskStatusUpdateInfo
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.features.group.model.GroupKind

class MyGroupRepositoryImpl : MyGroupRepository {
    private val service = RetrofitClient.instance.create(MyGroupService::class.java)

    override suspend fun getApplications(
        kind: GroupKind,
        groupId: Long
    ): Result<List<GroupApplicationModel>> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.getStudyApplications(groupId).map { it.toModel() }
                GroupKind.PROJECT -> service.getTeamApplications(groupId).map { it.toModel() }
            }
        }
    }

    override suspend fun getApplicationDetail(
        kind: GroupKind,
        applicationId: Long
    ): Result<GroupApplicationModel> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.getStudyApplicationDetail(applicationId).toModel()
                GroupKind.PROJECT -> service.getTeamApplicationDetail(applicationId).toModel()
            }
        }
    }

    override suspend fun acceptApplication(kind: GroupKind, applicationId: Long): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.acceptStudyApplication(applicationId)
                GroupKind.PROJECT -> service.acceptTeamApplication(applicationId)
            }
        }
    }

    override suspend fun rejectApplication(kind: GroupKind, applicationId: Long): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.rejectStudyApplication(applicationId)
                GroupKind.PROJECT -> service.rejectTeamApplication(applicationId)
            }
        }
    }

    override suspend fun getNotices(kind: GroupKind, groupId: Long): Result<List<GroupNoticeModel>> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.getStudyNotices(groupId)
                GroupKind.PROJECT -> service.getTeamNotices(groupId)
            }.map { it.toModel() }
        }
    }

    override suspend fun createNotice(
        kind: GroupKind,
        groupId: Long,
        info: GroupNoticeCreateInfo
    ): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.createStudyNotice(groupId, info.toNoticeRequest())
                GroupKind.PROJECT -> service.createTeamNotice(groupId, info.toNoticeRequest())
            }
        }
    }

    override suspend fun updateNotice(
        kind: GroupKind,
        groupId: Long,
        noticeId: Long,
        info: GroupNoticeCreateInfo
    ): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.updateStudyNotice(groupId, noticeId, info.toNoticeRequest())
                GroupKind.PROJECT -> service.updateTeamNotice(groupId, noticeId, info.toNoticeRequest())
            }
        }
    }

    override suspend fun deleteNotice(kind: GroupKind, groupId: Long, noticeId: Long): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.deleteStudyNotice(groupId, noticeId)
                GroupKind.PROJECT -> service.deleteTeamNotice(groupId, noticeId)
            }
        }
    }

    override suspend fun getTasks(kind: GroupKind, groupId: Long): Result<List<GroupTaskModel>> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.getStudyTasks(groupId)
                GroupKind.PROJECT -> service.getTeamTasks(groupId)
            }.map { it.toModel() }
        }
    }

    override suspend fun createTask(
        kind: GroupKind,
        groupId: Long,
        info: GroupTaskCreateInfo
    ): Result<GroupTaskModel> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.createStudyTask(groupId, info.toTaskRequest())
                GroupKind.PROJECT -> service.createTeamTask(groupId, info.toTaskRequest())
            }.toModel()
        }
    }

    override suspend fun updateTask(
        kind: GroupKind,
        taskId: Long,
        info: GroupTaskCreateInfo
    ): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.updateStudyTask(taskId, info.toTaskRequest())
                GroupKind.PROJECT -> service.updateTeamTask(taskId, info.toTaskRequest())
            }
        }
    }

    override suspend fun updateTaskStatus(
        kind: GroupKind,
        taskId: Long,
        info: GroupTaskStatusUpdateInfo
    ): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.updateStudyTaskStatus(taskId, info.toTaskStatusRequest())
                GroupKind.PROJECT -> service.updateTeamTaskStatus(taskId, info.toTaskStatusRequest())
            }
        }
    }

    override suspend fun deleteTask(kind: GroupKind, taskId: Long): Result<Unit> {
        return runCatching {
            when (kind) {
                GroupKind.STUDY -> service.deleteStudyTask(taskId)
                GroupKind.PROJECT -> service.deleteTeamTask(taskId)
            }
        }
    }
}
