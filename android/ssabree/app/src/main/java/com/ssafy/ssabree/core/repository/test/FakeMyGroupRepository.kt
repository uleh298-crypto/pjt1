package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.model.GroupApplicationModel
import com.ssafy.ssabree.core.repository.model.GroupNoticeCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupNoticeModel
import com.ssafy.ssabree.core.repository.model.GroupTaskCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupTaskModel
import com.ssafy.ssabree.core.repository.model.GroupTaskStatusUpdateInfo
import com.ssafy.ssabree.features.group.model.GroupKind

class FakeMyGroupRepository : MyGroupRepository {
    private val noticeSamples = mutableListOf(
        GroupNoticeModel(1, "금주 정기 회의", "회의는 4층 미팅룸에서 진행됩니다.", true, "2024-01-01T10:00:00"),
        GroupNoticeModel(2, "UI/UX 피드백", "피그마 코멘트 확인 바랍니다.", false, "2024-01-02T10:00:00")
    )
    private val taskSamples = mutableListOf(
        GroupTaskModel(1, "Repository 스켈레톤", "초기 프로젝트 구조 구성", "2024-02-01", "2024-02-10", "IN_PROGRESS"),
        GroupTaskModel(2, "API 연동 테스트", "통신 안정화 테스트", "2024-02-05", "2024-02-15", "TODO")
    )
    private val applications = mutableListOf(
        GroupApplicationModel(
            id = 1,
            title = "지원합니다",
            message = "열심히 하겠습니다",
            position = "BE",
            status = "PENDING",
            memberId = 2,
            portfolioId = 1
        ),
        GroupApplicationModel(
            id = 2,
            title = "함께하고 싶습니다",
            message = "참여 의지가 있습니다",
            position = "FE",
            status = "PENDING",
            memberId = 3,
            portfolioId = 2
        )
    )

    override suspend fun getApplications(kind: GroupKind, groupId: Long): Result<List<GroupApplicationModel>> {
        return Result.success(applications)
    }

    override suspend fun getApplicationDetail(
        kind: GroupKind,
        applicationId: Long
    ): Result<GroupApplicationModel> {
        return Result.success(applications.firstOrNull { it.id == applicationId } ?: applications.first())
    }

    override suspend fun acceptApplication(kind: GroupKind, applicationId: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun rejectApplication(kind: GroupKind, applicationId: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getNotices(kind: GroupKind, groupId: Long): Result<List<GroupNoticeModel>> {
        return Result.success(noticeSamples)
    }

    override suspend fun createNotice(
        kind: GroupKind,
        groupId: Long,
        info: GroupNoticeCreateInfo
    ): Result<Unit> {
        val newItem = GroupNoticeModel(
            id = (noticeSamples.maxOfOrNull { it.id } ?: 0) + 1,
            title = info.title,
            content = info.content,
            isPinned = info.isPinned,
            createdAt = "2024-01-03T10:00:00"
        )
        noticeSamples.add(newItem)
        return Result.success(Unit)
    }

    override suspend fun updateNotice(
        kind: GroupKind,
        groupId: Long,
        noticeId: Long,
        info: GroupNoticeCreateInfo
    ): Result<Unit> {
        noticeSamples.replaceAll {
            if (it.id == noticeId) it.copy(title = info.title, content = info.content, isPinned = info.isPinned) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteNotice(kind: GroupKind, groupId: Long, noticeId: Long): Result<Unit> {
        noticeSamples.removeIf { it.id == noticeId }
        return Result.success(Unit)
    }

    override suspend fun getTasks(kind: GroupKind, groupId: Long): Result<List<GroupTaskModel>> {
        return Result.success(taskSamples)
    }

    override suspend fun createTask(
        kind: GroupKind,
        groupId: Long,
        info: GroupTaskCreateInfo
    ): Result<GroupTaskModel> {
        val newItem = GroupTaskModel(
            id = (taskSamples.maxOfOrNull { it.id } ?: 0) + 1,
            title = info.title,
            content = info.content,
            startDate = info.startDate,
            endDate = info.endDate,
            status = info.status
        )
        taskSamples.add(newItem)
        return Result.success(newItem)
    }

    override suspend fun updateTask(kind: GroupKind, taskId: Long, info: GroupTaskCreateInfo): Result<Unit> {
        taskSamples.replaceAll {
            if (it.id == taskId) it.copy(
                title = info.title,
                content = info.content,
                startDate = info.startDate,
                endDate = info.endDate,
                status = info.status
            ) else it
        }
        return Result.success(Unit)
    }

    override suspend fun updateTaskStatus(
        kind: GroupKind,
        taskId: Long,
        info: GroupTaskStatusUpdateInfo
    ): Result<Unit> {
        taskSamples.replaceAll {
            if (it.id == taskId) it.copy(status = info.status) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteTask(kind: GroupKind, taskId: Long): Result<Unit> {
        taskSamples.removeIf { it.id == taskId }
        return Result.success(Unit)
    }
}
