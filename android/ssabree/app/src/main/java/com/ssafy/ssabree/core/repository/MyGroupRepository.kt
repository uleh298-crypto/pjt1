package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.core.repository.model.GroupApplicationModel
import com.ssafy.ssabree.core.repository.model.GroupNoticeCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupNoticeModel
import com.ssafy.ssabree.core.repository.model.GroupTaskCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupTaskModel
import com.ssafy.ssabree.core.repository.model.GroupTaskStatusUpdateInfo

interface MyGroupRepository {
    suspend fun getApplications(kind: GroupKind, groupId: Long): Result<List<GroupApplicationModel>>
    suspend fun getApplicationDetail(kind: GroupKind, applicationId: Long): Result<GroupApplicationModel>
    suspend fun acceptApplication(kind: GroupKind, applicationId: Long): Result<Unit>
    suspend fun rejectApplication(kind: GroupKind, applicationId: Long): Result<Unit>

    suspend fun getNotices(kind: GroupKind, groupId: Long): Result<List<GroupNoticeModel>>
    suspend fun createNotice(kind: GroupKind, groupId: Long, info: GroupNoticeCreateInfo): Result<Unit>
    suspend fun updateNotice(kind: GroupKind, groupId: Long, noticeId: Long, info: GroupNoticeCreateInfo): Result<Unit>
    suspend fun deleteNotice(kind: GroupKind, groupId: Long, noticeId: Long): Result<Unit>

    suspend fun getTasks(kind: GroupKind, groupId: Long): Result<List<GroupTaskModel>>
    suspend fun createTask(kind: GroupKind, groupId: Long, info: GroupTaskCreateInfo): Result<GroupTaskModel>
    suspend fun updateTask(kind: GroupKind, taskId: Long, info: GroupTaskCreateInfo): Result<Unit>
    suspend fun updateTaskStatus(kind: GroupKind, taskId: Long, info: GroupTaskStatusUpdateInfo): Result<Unit>
    suspend fun deleteTask(kind: GroupKind, taskId: Long): Result<Unit>
}
