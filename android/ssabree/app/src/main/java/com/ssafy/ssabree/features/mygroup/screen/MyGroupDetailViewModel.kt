package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.MemberUiModel
import com.ssafy.ssabree.features.mygroup.model.NoticeUiModel
import com.ssafy.ssabree.features.mygroup.model.TaskUiModel
import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupMemberModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class MyGroupDetailUiState(
    val title: String = "",
    val memberCountText: String = "-",
    val dDayText: String = "D-?",
    val applicationsCount: Int = 0,
    val notices: List<NoticeUiModel> = emptyList(),
    val tasks: List<TaskUiModel> = emptyList(),
    val members: List<MemberUiModel> = emptyList(),
    val leaderId: Long? = null,
    val leaderName: String? = null,
    val leaderProfileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyGroupDetailViewModel(
    private val groupRepository: GroupRepository,
    private val myGroupRepository: MyGroupRepository,
    private val memberRepository: MemberRepository,
    private val groupKind: GroupKind,
    private val groupId: Long,
    private val isLeader: Boolean
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyGroupDetailUiState())
    val uiState: StateFlow<MyGroupDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val detailDeferred = async {
            if (groupKind == GroupKind.STUDY) {
                groupRepository.getStudyDetail(groupId)
            } else {
                groupRepository.getTeamDetail(groupId)
            }
        }
        val noticesDeferred = async { myGroupRepository.getNotices(groupKind, groupId) }
        val tasksDeferred = async { myGroupRepository.getTasks(groupKind, groupId) }
        val applicationsDeferred = async {
            if (isLeader) {
                myGroupRepository.getApplications(groupKind, groupId)
            } else {
                Result.success(emptyList())
            }
        }
        val membersDeferred = async {
            if (groupKind == GroupKind.STUDY) {
                groupRepository.getStudyMembers(groupId)
            } else {
                groupRepository.getTeamMembers(groupId)
            }
        }

        val detailResult = detailDeferred.await()
        val noticesResult = noticesDeferred.await()
        val tasksResult = tasksDeferred.await()
        val applicationsResult = applicationsDeferred.await()
        val membersResult = membersDeferred.await()

        val error = listOf(
            detailResult.exceptionOrNull(),
            noticesResult.exceptionOrNull(),
            tasksResult.exceptionOrNull(),
            applicationsResult.exceptionOrNull(),
            membersResult.exceptionOrNull()
        ).firstOrNull { it != null }

        if (error != null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            return@launch
        }

        val detail = detailResult.getOrNull()
        val memberCount = detail?.currentMembers ?: 0
        val capacity = detail?.capacity ?: 0
        val endDate = detail?.endDate ?: ""
        val dDay = calculateDDay(endDate)
        val memberItems = mergeLeader(
            membersResult.getOrNull().orEmpty(),
            detail
        ).map { it.toUiModel() }
        val enrichedMembers = enrichMattermostIds(memberItems)
        val memberMap = enrichedMembers.associateBy { it.id }

        _uiState.update {
            it.copy(
                title = detail?.title ?: "",
                memberCountText = "${memberCount}/${capacity}명",
                dDayText = dDay,
                applicationsCount = applicationsResult.getOrNull()?.count { app -> app.status == "PENDING" } ?: 0,
                notices = noticesResult.getOrNull()?.map { notice -> notice.toUiModel() } ?: emptyList(),
                tasks = tasksResult.getOrNull()
                    ?.map { task ->
                        val uiTask = task.toUiModel()
                        val author = uiTask.creatorId?.let { memberMap[it] }
                        uiTask.copy(
                            authorName = author?.name,
                            authorProfileImageUrl = author?.profileImageUrl
                        )
                    }
                    ?.sortedBy { it.startDate }
                    ?: emptyList(),
                members = enrichedMembers,
                leaderId = detail?.leaderId,
                leaderName = detail?.leaderName,
                leaderProfileImageUrl = detail?.leaderProfileImageUrl,
                isLoading = false
            )
        }
    }

    fun deleteGroup(onSuccess: () -> Unit) = viewModelScope.launch {
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.deleteStudy(groupId)
        } else {
            groupRepository.deleteTeam(groupId)
        }
        result.onSuccess { onSuccess() }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun leaveGroup(onSuccess: () -> Unit) = viewModelScope.launch {
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.leaveStudy(groupId)
        } else {
            groupRepository.leaveTeam(groupId)
        }
        result.onSuccess { onSuccess() }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun calculateDDay(endDate: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return runCatching {
            val end = format.parse(endDate)
            if (end != null) {
                val diff = end.time - System.currentTimeMillis()
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                if (days >= 0) "D-$days" else "진행중"
            } else {
                "D-?"
            }
        }.getOrDefault("D-?")
    }

    private fun mergeLeader(
        members: List<GroupMemberModel>,
        detail: GroupDetailModel?
    ): List<GroupMemberModel> {
        val leaderId = detail?.leaderId ?: return members
        if (members.any { it.id == leaderId }) return members
        val leader = GroupMemberModel(
            id = leaderId,
            email = detail.leaderEmail,
            name = detail.leaderName,
            mattermostId = detail.leaderMattermostId,
            profileImageUrl = detail.leaderProfileImageUrl
        )
        return members + leader
    }

    private suspend fun enrichMattermostIds(members: List<MemberUiModel>): List<MemberUiModel> = coroutineScope {
        members.map { member ->
            async {
                if (member.mattermostId.isNotBlank() && member.mattermostId != "-") {
                    member
                } else {
                    val mattermostId = memberRepository.getMember(member.id).getOrNull()?.mattermostId
                    if (!mattermostId.isNullOrBlank()) {
                        member.copy(mattermostId = mattermostId)
                    } else {
                        member
                    }
                }
            }
        }.awaitAll()
    }
}
