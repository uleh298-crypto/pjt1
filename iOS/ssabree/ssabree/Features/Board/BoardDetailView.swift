import SwiftUI

struct BoardDetailView: View {
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: BoardDetailViewModel
    let boardId: Int
    let postId: Int
    var onMessageClick: ((Int) -> Void)? = nil  // (postId) -> Void
    var onEditClick: ((Int) -> Void)? = nil  // postId -> Void
    var onDeleteSuccess: (() -> Void)? = nil

    // 다이얼로그 상태
    @State private var showDeletePostDialog = false
    @State private var showDeleteCommentDialog = false
    @State private var commentToDelete: CommentModel? = nil
    @State private var replyToDelete: ReplyModel? = nil

    // 댓글 수정 상태 (alert TextField 바인딩 문제 해결용)
    @State private var showEditCommentDialog = false
    @State private var editCommentText: String = ""
    @State private var commentToEdit: CommentModel? = nil
    @State private var replyToEdit: ReplyModel? = nil

    // 신고 다이얼로그 상태
    @State private var showReportDialog = false
    @State private var reportTargetType: ReportTargetType = .post
    @State private var reportTargetId: Int = 0

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
                Spacer()

                // 쪽지 버튼 - 내 글이 아닐 때만 표시
                if let post = viewModel.uiState.post, !post.isMine {
                    Button(action: {
                        onMessageClick?(post.id)
                    }) {
                        Image(systemName: "envelope.fill")
                            .font(.title3)
                            .foregroundStyle(AppColors.onBackground)
                    }
                }
            }
            .padding()
            .background(Color.clear)

            if viewModel.uiState.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let post = viewModel.uiState.post {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        // Author Info Section
                        HStack(spacing: 12) {
                            Circle()
                                .fill(AppColors.primary)
                                .frame(width: 44, height: 44)
                                .overlay(
                                    Image(systemName: "person.fill")
                                        .foregroundStyle(AppColors.onPrimary)
                                )

                            VStack(alignment: .leading, spacing: 2) {
                                Text("익명")
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                    .foregroundStyle(AppColors.onSurface)
                                Text(post.dateText)
                                    .font(.caption)
                                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                            }

                            Spacer()

                            // 더보기 메뉴
                            Menu {
                                if post.isMine {
                                    Button("수정") {
                                        onEditClick?(post.id)
                                    }
                                    Button("삭제", role: .destructive) {
                                        showDeletePostDialog = true
                                    }
                                } else {
                                    Button("신고") {
                                        reportTargetType = .post
                                        reportTargetId = post.id
                                        showReportDialog = true
                                    }
                                }
                            } label: {
                                Image(systemName: "ellipsis")
                                    .font(.title3)
                                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                                    .frame(width: 32, height: 32)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)

                        // Title
                        Text(post.title)
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundStyle(AppColors.onSurface)
                            .padding(.horizontal, 16)

                        Spacer().frame(height: 8)

                        // Content
                        Text(post.content)
                            .font(.body)
                            .foregroundStyle(AppColors.onSurface)
                            .lineSpacing(4)
                            .padding(.horizontal, 16)

                        // Poll (if exists)
                        if let poll = post.poll {
                            PollCard(poll: poll, onVoteClick: { optionId in
                                Task { await viewModel.onVote(optionId: optionId) }
                            })
                            .padding(.horizontal, 16)
                            .padding(.top, 16)
                        }

                        // Images
                        if !post.imageUrls.isEmpty {
                            VStack(spacing: 8) {
                                ForEach(post.imageUrls, id: \.self) { imageUrl in
                                    CachedAsyncImage(url: imageUrl) { image in
                                        image
                                            .resizable()
                                            .aspectRatio(contentMode: .fit)
                                            .clipShape(RoundedRectangle(cornerRadius: 12))
                                    } placeholder: {
                                        ProgressView()
                                            .frame(height: 200)
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.top, 16)
                        }

                        Spacer().frame(height: 16)

                        // Stats Row
                        HStack(spacing: 8) {
                            // Like Button
                            Button(action: {
                                Task { await viewModel.onLikePost() }
                            }) {
                                HStack(spacing: 4) {
                                    Image(systemName: post.isLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                                        .font(.subheadline)
                                    Text("\(post.likeCount)")
                                        .font(.caption)
                                }
                                .foregroundStyle(post.isLiked ? AppColors.primary : AppColors.onSurface.opacity(0.6))
                            }

                            Spacer().frame(width: 8)

                            // Comment Count
                            HStack(spacing: 4) {
                                Image(systemName: "bubble.left")
                                    .font(.subheadline)
                                Text("\(post.commentCount)")
                                    .font(.caption)
                            }
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))

                            Spacer().frame(width: 8)

                            // Bookmark Button
                            Button(action: {
                                Task { await viewModel.onBookmarkPost() }
                            }) {
                                HStack(spacing: 4) {
                                    Image(systemName: post.isScraped ? "star.fill" : "star")
                                        .font(.subheadline)
                                    Text("\(post.scrapCount)")
                                        .font(.caption)
                                }
                                .foregroundStyle(post.isScraped ? AppColors.primary : AppColors.onSurface.opacity(0.6))
                            }

                            Spacer()
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 16)

                        Divider()

                        // Comments Section
                        LazyVStack(alignment: .leading, spacing: 0) {
                            ForEach(post.comments, id: \.self) { comment in
                                CommentRow(
                                    comment: comment,
                                    onReplyClick: { viewModel.onReplyComment(comment: comment) },
                                    onLikeClick: { Task { await viewModel.onLikeComment(comment: comment) } },
                                    onEditClick: {
                                        editCommentText = comment.content
                                        commentToEdit = comment
                                        replyToEdit = nil
                                        showEditCommentDialog = true
                                    },
                                    onDeleteClick: {
                                        commentToDelete = comment
                                        showDeleteCommentDialog = true
                                    },
                                    onReportClick: {
                                        reportTargetType = .comment
                                        reportTargetId = comment.id
                                        showReportDialog = true
                                    }
                                )

                                // Replies
                                ForEach(comment.replies, id: \.self) { reply in
                                    ReplyRow(
                                        reply: reply,
                                        onLikeClick: { Task { await viewModel.onLikeReply(reply: reply, parentComment: comment) } },
                                        onEditClick: {
                                            editCommentText = reply.content
                                            replyToEdit = reply
                                            commentToEdit = nil
                                            showEditCommentDialog = true
                                        },
                                        onDeleteClick: {
                                            replyToDelete = reply
                                            showDeleteCommentDialog = true
                                        },
                                        onReportClick: {
                                            reportTargetType = .comment
                                            reportTargetId = reply.id
                                            showReportDialog = true
                                        }
                                    )
                                }

                                if comment.id != post.comments.last?.id {
                                    Divider()
                                        .padding(.horizontal, 16)
                                }
                            }
                        }
                    }
                }
                .background(AppColors.surface)

                // Comment Input Bar
                CommentInputBar(
                    text: $viewModel.commentText,
                    isSubmitting: viewModel.uiState.isCommentSubmitting,
                    replyTargetName: viewModel.uiState.replyTargetComment?.nickname,
                    onSubmit: { Task { await viewModel.onSubmitComment() } },
                    onCancelReply: { viewModel.cancelReply() }
                )

            } else if let error = viewModel.uiState.error {
                VStack(spacing: 16) {
                    Text("오류 발생")
                        .font(.headline)
                    Text(error)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    Button("재시도") {
                        Task { await viewModel.loadPost(postId: postId) }
                    }
                    .buttonStyle(.bordered)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .background(AppColors.background)
        .navigationBarHidden(true)
        .task {
            await viewModel.loadPost(postId: postId)
        }
        .onChange(of: viewModel.uiState.isDeleteSuccess) { _, success in
            if success {
                onDeleteSuccess?()
                dismiss()
            }
        }
        // 게시글 삭제 확인 다이얼로그
        .alert("게시글 삭제", isPresented: $showDeletePostDialog) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                Task { await viewModel.deletePost() }
            }
        } message: {
            Text("정말로 삭제하시겠습니까?")
        }
        // 댓글 삭제 확인 다이얼로그
        .alert("댓글 삭제", isPresented: $showDeleteCommentDialog) {
            Button("취소", role: .cancel) {
                commentToDelete = nil
                replyToDelete = nil
            }
            Button("삭제", role: .destructive) {
                if let comment = commentToDelete {
                    Task { await viewModel.deleteComment(comment: comment) }
                    commentToDelete = nil
                } else if let reply = replyToDelete {
                    Task { await viewModel.deleteReply(reply: reply) }
                    replyToDelete = nil
                }
            }
        } message: {
            Text("정말로 삭제하시겠습니까?")
        }
        // 댓글 수정 다이얼로그
        .alert("댓글 수정", isPresented: $showEditCommentDialog) {
            TextField("댓글 내용", text: $editCommentText)
            Button("취소", role: .cancel) {
                commentToEdit = nil
                replyToEdit = nil
                editCommentText = ""
            }
            Button("수정") {
                let content = editCommentText
                if let comment = commentToEdit {
                    Task { await viewModel.updateCommentContent(commentId: comment.id, content: content) }
                } else if let reply = replyToEdit {
                    Task { await viewModel.updateReplyContent(replyId: reply.id, content: content) }
                }
                commentToEdit = nil
                replyToEdit = nil
                editCommentText = ""
            }
            .disabled(editCommentText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
        } message: {
            Text("댓글 내용을 수정하세요")
        }
        // 신고 다이얼로그 오버레이
        .overlay {
            if showReportDialog {
                ReportDialog(
                    isPresented: $showReportDialog,
                    targetType: reportTargetType,
                    targetId: reportTargetId,
                    onReport: { reason, detail in
                        if reportTargetType == .post {
                            await viewModel.reportPost(reason: reason, detail: detail)
                        } else {
                            await viewModel.reportComment(commentId: reportTargetId, reason: reason, detail: detail)
                        }
                    }
                )
            }
        }
    }
}

// MARK: - Poll Card

private struct PollCard: View {
    let poll: PollModel
    let onVoteClick: (Int) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("투표")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)

            ForEach(poll.options, id: \.optionId) { option in
                let ratio: CGFloat = poll.totalVotes > 0 ? CGFloat(option.voteCount) / CGFloat(poll.totalVotes) : 0
                let isSelected = option.isVoted(myVotedOptionId: poll.myVotedOptionId)

                Button(action: { onVoteClick(option.optionId) }) {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text(option.text)
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundStyle(isSelected ? AppColors.primary : AppColors.onSurface)
                            Spacer()
                            Text("\(option.voteCount)표")
                                .font(.caption)
                                .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        }

                        GeometryReader { geometry in
                            ZStack(alignment: .leading) {
                                RoundedRectangle(cornerRadius: 4)
                                    .fill(AppColors.surfaceVariant)
                                RoundedRectangle(cornerRadius: 4)
                                    .fill(isSelected ? AppColors.primary : AppColors.primary.opacity(0.6))
                                    .frame(width: geometry.size.width * ratio)
                            }
                        }
                        .frame(height: 6)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(isSelected ? AppColors.primary.opacity(0.12) : AppColors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
            }

            Text("총 \(poll.totalVotes)표")
                .font(.caption)
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
        }
        .padding(16)
        .background(AppColors.surfaceVariant.opacity(0.4))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - Comment Row (안드로이드 CommentItemView 스타일)

private struct CommentRow: View {
    let comment: CommentModel
    let onReplyClick: () -> Void
    let onLikeClick: () -> Void
    let onEditClick: () -> Void
    let onDeleteClick: () -> Void
    let onReportClick: () -> Void
    @State private var showBlindedContent = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 상단: 프로필 + 이름 + 액션 버튼들
            HStack(alignment: .top, spacing: 10) {
                // 프로필 이미지
                Circle()
                    .fill(AppColors.surfaceVariant)
                    .frame(width: 32, height: 32)
                    .overlay(
                        Image(systemName: "person.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    )

                // 이름
                VStack(alignment: .leading, spacing: 0) {
                    Text(comment.nickname)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(AppColors.onSurface)
                }
                .padding(.top, 4)

                Spacer()

                // 액션 버튼들
                if !comment.isDeleted {
                    HStack(spacing: 0) {
                        // 답글 버튼
                        Button(action: onReplyClick) {
                            Image(systemName: "bubble.left")
                                .font(.system(size: 16))
                                .foregroundStyle(AppColors.onSurface.opacity(0.5))
                        }
                        .frame(width: 32, height: 32)

                        // 좋아요 버튼
                        Button(action: onLikeClick) {
                            Image(systemName: comment.isLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                                .font(.system(size: 16))
                                .foregroundStyle(comment.isLiked ? AppColors.primary : AppColors.onSurface.opacity(0.5))
                        }
                        .frame(width: 32, height: 32)

                        // 더보기 메뉴
                        Menu {
                            if comment.isMine {
                                Button("수정") { onEditClick() }
                                Button("삭제", role: .destructive) { onDeleteClick() }
                            } else {
                                Button("신고") {
                                    onReportClick()
                                }
                            }
                        } label: {
                            Image(systemName: "ellipsis")
                                .font(.system(size: 16))
                                .foregroundStyle(AppColors.onSurface.opacity(0.5))
                        }
                        .frame(width: 32, height: 32)
                    }
                }
            }

            // 내용 (프로필 들여쓰기: 32 + 10 = 42)
            if comment.isDeleted {
                Text("삭제된 댓글입니다.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    .italic()
                    .padding(.leading, 42)
            } else if comment.isBlinded && !showBlindedContent {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(alignment: .bottom, spacing: 8) {
                        Text("험한 말은 싸피봇이 처리했으니 안심하라구!")
                            .font(.subheadline)
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))

                        Button("내용보기") {
                            showBlindedContent = true
                        }
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(AppColors.primary)
                    }

                    Image("bot")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 70, height: 70)
                }
                .padding(.leading, 42)
            } else {
                Text(comment.content)
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface)
                    .padding(.leading, 42)
            }

            Spacer().frame(height: 4)

            // 하단: 날짜 + 좋아요 수
            HStack(spacing: 8) {
                Text(comment.dateText)
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))

                if comment.likeCount > 0 {
                    HStack(spacing: 2) {
                        Image(systemName: "hand.thumbsup.fill")
                            .font(.system(size: 10))
                            .foregroundStyle(AppColors.primary)
                        Text("\(comment.likeCount)")
                            .font(.caption)
                            .foregroundStyle(AppColors.primary)
                    }
                }
            }
            .padding(.leading, 42)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(AppColors.surface)
    }
}

// MARK: - Reply Row (안드로이드 스타일: 왼쪽 40dp padding)

private struct ReplyRow: View {
    let reply: ReplyModel
    let onLikeClick: () -> Void
    let onEditClick: () -> Void
    let onDeleteClick: () -> Void
    let onReportClick: () -> Void
    @State private var showBlindedContent = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 상단: 프로필 + 이름 + 액션 버튼들
            HStack(alignment: .top, spacing: 10) {
                // 프로필 이미지
                Circle()
                    .fill(AppColors.surfaceVariant)
                    .frame(width: 32, height: 32)
                    .overlay(
                        Image(systemName: "person.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    )

                // 이름
                VStack(alignment: .leading, spacing: 0) {
                    Text(reply.nickname)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(AppColors.onSurface)
                }
                .padding(.top, 4)

                Spacer()

                // 액션 버튼들 (좋아요, 더보기)
                if !reply.isDeleted {
                    HStack(spacing: 0) {
                        Button(action: onLikeClick) {
                            Image(systemName: reply.isLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                                .font(.system(size: 16))
                                .foregroundStyle(reply.isLiked ? AppColors.primary : AppColors.onSurface.opacity(0.5))
                        }
                        .frame(width: 32, height: 32)

                        Menu {
                            if reply.isMine {
                                Button("수정") { onEditClick() }
                                Button("삭제", role: .destructive) { onDeleteClick() }
                            } else {
                                Button("신고") {
                                    onReportClick()
                                }
                            }
                        } label: {
                            Image(systemName: "ellipsis")
                                .font(.system(size: 16))
                                .foregroundStyle(AppColors.onSurface.opacity(0.5))
                        }
                        .frame(width: 32, height: 32)
                    }
                }
            }

            // 내용 (프로필 들여쓰기)
            if reply.isDeleted {
                Text("삭제된 답글입니다.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    .italic()
                    .padding(.leading, 42)
            } else if reply.isBlinded && !showBlindedContent {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(alignment: .bottom, spacing: 8) {
                        Text("험한 말은 싸피봇이 처리했으니 안심하라구!")
                            .font(.subheadline)
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))

                        Button("내용보기") {
                            showBlindedContent = true
                        }
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(AppColors.primary)
                    }

                    Image("bot")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 70, height: 70)
                }
                .padding(.leading, 42)
            } else {
                Text(reply.content)
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface)
                    .padding(.leading, 42)
            }

            Spacer().frame(height: 4)

            // 하단: 날짜 + 좋아요 수
            HStack(spacing: 8) {
                Text(reply.dateText)
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))

                if reply.likeCount > 0 {
                    HStack(spacing: 2) {
                        Image(systemName: "hand.thumbsup.fill")
                            .font(.system(size: 10))
                            .foregroundStyle(AppColors.primary)
                        Text("\(reply.likeCount)")
                            .font(.caption)
                            .foregroundStyle(AppColors.primary)
                    }
                }
            }
            .padding(.leading, 42)
        }
        .padding(.leading, 40)  // 대댓글 들여쓰기
        .padding(.trailing, 16)
        .padding(.vertical, 8)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }
}

// MARK: - Comment Input Bar

private struct CommentInputBar: View {
    @Binding var text: String
    let isSubmitting: Bool
    let replyTargetName: String?
    let onSubmit: () -> Void
    let onCancelReply: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Reply indicator
            if let name = replyTargetName {
                HStack {
                    Text("\(name)님에게 답글 작성 중")
                        .font(.caption)
                        .foregroundStyle(AppColors.primary)

                    Spacer()

                    Button(action: onCancelReply) {
                        Image(systemName: "xmark")
                            .font(.caption)
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(AppColors.primary.opacity(0.1))
            }

            Divider()

            HStack(spacing: 12) {
                TextField(
                    replyTargetName != nil ? "답글을 입력하세요" : "댓글을 입력하세요",
                    text: $text
                )
                .disabled(isSubmitting)
                .padding(.vertical, 10)
                .padding(.horizontal, 16)
                .background(AppColors.background)
                .clipShape(Capsule())

                Button(action: onSubmit) {
                    if isSubmitting {
                        ProgressView()
                            .frame(width: 24, height: 24)
                    } else {
                        Image(systemName: "paperplane.fill")
                            .font(.title3)
                            .foregroundStyle(text.isEmpty ? Color.gray : AppColors.primary)
                    }
                }
                .disabled(text.isEmpty || isSubmitting)
            }
            .padding()
            .background(AppColors.surface)
        }
    }
}
