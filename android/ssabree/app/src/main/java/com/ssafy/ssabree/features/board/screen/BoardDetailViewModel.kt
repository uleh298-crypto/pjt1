package com.ssafy.ssabree.features.board.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.CommentRepository
import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.core.repository.ReportRepository
import com.ssafy.ssabree.core.repository.model.CommentCreateInfo
import com.ssafy.ssabree.core.repository.model.ReplyCreateInfo
import com.ssafy.ssabree.core.repository.model.VoteInfo
import com.ssafy.ssabree.features.board.model.CommentUiModel
import com.ssafy.ssabree.features.board.model.PostDetailUiModel
import com.ssafy.ssabree.features.board.model.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private const val TAG = "BoardDetailViewModel"
private const val DEBOUNCE_DELAY_MS = 500L

data class BoardDetailUiState(
    val post: PostDetailUiModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val commentText: String = "",
    val replyTargetComment: CommentUiModel? = null,  // 대댓글 대상 댓글
    val editingComment: CommentUiModel? = null,       // 수정 중인 댓글
    val editCommentText: String = "",                 // 수정할 댓글 내용
    val isLikeInProgress: Boolean = false,
    val isBookmarkInProgress: Boolean = false,
    val isVoteInProgress: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val isDeleteSuccess: Boolean = false,
    val isDeleting: Boolean = false,
    val isReportSuccess: Boolean = false,             // 신고 성공 여부
    val isReporting: Boolean = false,                 // 신고 진행 중
)

class BoardDetailViewModel(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardDetailUiState())
    val uiState: StateFlow<BoardDetailUiState> = _uiState.asStateFlow()

    private var currentPostId: Long? = null
    private var likeJob: Job? = null
    private var bookmarkJob: Job? = null
    private var voteJob: Job? = null

    fun loadPost(postId: Long) {
        currentPostId = postId
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            postRepository.getPostDetail(postId)
                .onSuccess { detail ->
                    Log.d(TAG, "loadPost: succeed")
                    _uiState.update { it.copy(post = detail.toUiModel(), isLoading = false) }
                }
                .onFailure { e ->
                    Log.d(TAG, "loadPost: failed (${e.message})")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun deletePost() {
        val postId = currentPostId ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            postRepository.deletePost(postId)
                .onSuccess {
                    Log.d(TAG, "deletePost: succeed")
                    _uiState.update { it.copy(isDeleting = false, isDeleteSuccess = true) }
                }
                .onFailure { e ->
                    Log.d(TAG, "deletePost: failed (${e.message})")
                    _uiState.update { it.copy(isDeleting = false, error = e.message) }
                }
        }
    }

    fun onCommentTextChange(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    fun onSubmitComment() {
        val text = _uiState.value.commentText.trim()
        val postId = currentPostId ?: return
        if (text.isEmpty() || _uiState.value.isCommentSubmitting) return

        val replyTarget = _uiState.value.replyTargetComment

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true, error = null) }
            if (replyTarget != null) {
                // 대댓글 작성
                postRepository.createReply(postId, replyTarget.id, ReplyCreateInfo(content = text))
                    .onSuccess { newReply ->
                        Log.d(TAG, "onSubmitReply: succeed")
                        _uiState.update { state ->
                            val post = state.post ?: return@update state.copy(commentText = "", replyTargetComment = null)
                            val updatedComments = post.comments.map { comment ->
                                if (comment.id == replyTarget.id) {
                                    comment.copy(replies = comment.replies + newReply.toUiModel())
                                } else {
                                    comment
                                }
                            }
                            state.copy(
                                commentText = "",
                                replyTargetComment = null,
                                post = post.copy(
                                    comments = updatedComments,
                                    commentCount = post.commentCount + 1
                                )
                            )
                        }
                    }
                    .onFailure { e ->
                        Log.d(TAG, "onSubmitReply: failed (${e.message})")
                        _uiState.update { it.copy(error = e.message) }
                    }
            } else {
                // 일반 댓글 작성
                postRepository.createComment(postId, CommentCreateInfo(content = text))
                    .onSuccess { newComment ->
                        Log.d(TAG, "onSubmitComment: succeed")
                        _uiState.update { state ->
                            val post = state.post ?: return@update state.copy(commentText = "")
                            state.copy(
                                commentText = "",
                                post = post.copy(
                                    comments = post.comments + newComment.toUiModel(),
                                    commentCount = post.commentCount + 1
                                )
                            )
                        }
                    }
                    .onFailure { e ->
                        Log.d(TAG, "onSubmitComment: failed (${e.message})")
                        _uiState.update { it.copy(error = e.message) }
                    }
            }
            _uiState.update { it.copy(isCommentSubmitting = false) }
        }
    }

    fun onLikePost() {
        val postId = currentPostId ?: return
        val post = _uiState.value.post ?: return

        // 디바운싱: 이전 요청이 진행 중이면 무시
        if (_uiState.value.isLikeInProgress) return

        likeJob?.cancel()
        likeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLikeInProgress = true) }

            val isCurrentlyLiked = post.isLiked
            val result = if (isCurrentlyLiked) {
                postRepository.unlike(postId)
            } else {
                postRepository.like(postId)
            }

            result
                .onSuccess { likeResult ->
                    Log.d(TAG, "onLikePost: succeed (liked=${likeResult.liked})")
                    _uiState.update { state ->
                        val currentPost = state.post ?: return@update state
                        state.copy(
                            post = currentPost.copy(
                                isLiked = likeResult.liked,
                                likeCount = likeResult.likeCount
                            )
                        )
                    }
                }
                .onFailure { e -> Log.d(TAG, "onLikePost: failed (${e.message})") }

            // 디바운스 딜레이 후 다시 클릭 가능
            delay(DEBOUNCE_DELAY_MS)
            _uiState.update { it.copy(isLikeInProgress = false) }
        }
    }

    fun onBookmarkPost() {
        val postId = currentPostId ?: return
        val post = _uiState.value.post ?: return

        // 디바운싱: 이전 요청이 진행 중이면 무시
        if (_uiState.value.isBookmarkInProgress) return

        bookmarkJob?.cancel()
        bookmarkJob = viewModelScope.launch {
            _uiState.update { it.copy(isBookmarkInProgress = true) }

            val isCurrentlyBookmarked = post.isBookmarked
            val result = if (isCurrentlyBookmarked) {
                postRepository.unscrap(postId)
            } else {
                postRepository.scrap(postId)
            }

            result
                .onSuccess { scrapResult ->
                    Log.d(TAG, "onBookmarkPost: succeed (success=${scrapResult.success})")
                    if (scrapResult.success) {
                        _uiState.update { state ->
                            val currentPost = state.post ?: return@update state
                            state.copy(
                                post = currentPost.copy(
                                    isBookmarked = !isCurrentlyBookmarked,
                                    bookmarkCount = if (isCurrentlyBookmarked) currentPost.bookmarkCount - 1 else currentPost.bookmarkCount + 1
                                )
                            )
                        }
                    }
                }
                .onFailure { e -> Log.d(TAG, "onBookmarkPost: failed (${e.message})") }

            // 디바운스 딜레이 후 다시 클릭 가능
            delay(DEBOUNCE_DELAY_MS)
            _uiState.update { it.copy(isBookmarkInProgress = false) }
        }
    }

    fun onLikeComment(comment: CommentUiModel) {
        val isCurrentlyLiked = comment.isLiked

        viewModelScope.launch {
            val result = if (isCurrentlyLiked) {
                commentRepository.unlike(comment.id)
            } else {
                commentRepository.like(comment.id)
            }

            result
                .onSuccess { likeResult ->
                    Log.d(TAG, "onLikeComment: succeed (liked=${likeResult.liked})")
                    _uiState.update { state ->
                        val post = state.post ?: return@update state
                        val updatedComments = post.comments.map { c ->
                            if (c.id == comment.id) {
                                c.copy(isLiked = likeResult.liked, likeCount = likeResult.likeCount)
                            } else {
                                // 대댓글 내에서 찾기
                                c.copy(replies = c.replies.map { r ->
                                    if (r.id == comment.id) {
                                        r.copy(isLiked = likeResult.liked, likeCount = likeResult.likeCount)
                                    } else r
                                })
                            }
                        }
                        state.copy(post = post.copy(comments = updatedComments))
                    }
                }
                .onFailure { e -> Log.d(TAG, "onLikeComment: failed (${e.message})") }
        }
    }

    fun onReplyComment(comment: CommentUiModel) {
        _uiState.update { it.copy(replyTargetComment = comment) }
    }

    fun cancelReply() {
        _uiState.update { it.copy(replyTargetComment = null) }
    }

    fun onVote(optionId: Long) {
        val postId = currentPostId ?: return
        if (_uiState.value.isVoteInProgress) return

        voteJob?.cancel()
        voteJob = viewModelScope.launch {
            _uiState.update { it.copy(isVoteInProgress = true, error = null) }
            postRepository.vote(postId, VoteInfo(optionId))
                .onSuccess { poll ->
                    _uiState.update { state ->
                        val post = state.post ?: return@update state.copy(isVoteInProgress = false)
                        state.copy(post = post.copy(poll = poll.toUiModel()), isVoteInProgress = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isVoteInProgress = false, error = e.message) }
                }
        }
    }

    // 댓글 수정 시작
    fun startEditComment(comment: CommentUiModel) {
        _uiState.update { it.copy(editingComment = comment, editCommentText = comment.content) }
    }

    // 댓글 수정 취소
    fun cancelEditComment() {
        _uiState.update { it.copy(editingComment = null, editCommentText = "") }
    }

    // 수정할 댓글 내용 변경
    fun onEditCommentTextChange(text: String) {
        _uiState.update { it.copy(editCommentText = text) }
    }

    // 댓글 수정 제출
    fun submitEditComment() {
        val editingComment = _uiState.value.editingComment ?: return
        val newContent = _uiState.value.editCommentText.trim()
        if (newContent.isEmpty()) return

        viewModelScope.launch {
            commentRepository.updateComment(editingComment.id, newContent)
                .onSuccess {
                    Log.d(TAG, "submitEditComment: succeed")
                    _uiState.update { state ->
                        val post = state.post ?: return@update state.copy(editingComment = null, editCommentText = "")
                        val updatedComments = post.comments.map { c ->
                            if (c.id == editingComment.id) {
                                c.copy(content = newContent)
                            } else {
                                c.copy(replies = c.replies.map { r ->
                                    if (r.id == editingComment.id) r.copy(content = newContent) else r
                                })
                            }
                        }
                        state.copy(
                            post = post.copy(comments = updatedComments),
                            editingComment = null,
                            editCommentText = ""
                        )
                    }
                }
                .onFailure { e ->
                    Log.d(TAG, "submitEditComment: failed (${e.message})")
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    // 댓글 삭제
    fun deleteComment(comment: CommentUiModel) {
        viewModelScope.launch {
            commentRepository.deleteComment(comment.id)
                .onSuccess {
                    Log.d(TAG, "deleteComment: succeed")
                    _uiState.update { state ->
                        val post = state.post ?: return@update state
                        val updatedComments = post.comments.mapNotNull { c ->
                            if (c.id == comment.id) {
                                // 삭제된 댓글 (soft delete이므로 내용만 변경)
                                c.copy(content = "삭제된 댓글입니다.")
                            } else {
                                c.copy(replies = c.replies.mapNotNull { r ->
                                    if (r.id == comment.id) {
                                        r.copy(content = "삭제된 댓글입니다.")
                                    } else r
                                })
                            }
                        }
                        state.copy(post = post.copy(comments = updatedComments))
                    }
                }
                .onFailure { e ->
                    Log.d(TAG, "deleteComment: failed (${e.message})")
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    // 게시글 신고
    fun reportPost(reason: String, detail: String?) {
        val postId = currentPostId ?: return
        if (_uiState.value.isReporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true, error = null) }
            reportRepository.reportPost(postId, reason, detail)
                .onSuccess {
                    Log.d(TAG, "reportPost: succeed")
                    _uiState.update { it.copy(isReporting = false, isReportSuccess = true) }
                }
                .onFailure { e ->
                    Log.d(TAG, "reportPost: failed (${e.message})")
                    _uiState.update { it.copy(isReporting = false, error = e.message) }
                }
        }
    }

    // 댓글 신고
    fun reportComment(commentId: Long, reason: String, detail: String?) {
        if (_uiState.value.isReporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true, error = null) }
            reportRepository.reportComment(commentId, reason, detail)
                .onSuccess {
                    Log.d(TAG, "reportComment: succeed")
                    _uiState.update { it.copy(isReporting = false, isReportSuccess = true) }
                }
                .onFailure { e ->
                    Log.d(TAG, "reportComment: failed (${e.message})")
                    _uiState.update { it.copy(isReporting = false, error = e.message) }
                }
        }
    }

    // 신고 성공 상태 초기화
    fun clearReportSuccess() {
        _uiState.update { it.copy(isReportSuccess = false) }
    }

    // 에러 상태 초기화
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
