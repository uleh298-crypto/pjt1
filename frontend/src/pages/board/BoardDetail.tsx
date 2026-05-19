import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MdArrowBack, MdThumbUp, MdThumbUpOffAlt, MdStar, MdStarBorder, MdMoreVert, MdSend, MdChat } from 'react-icons/md';
import { postService, commentService, memberService, chatService } from '../../services/api';
import type { PostDetailResponse } from '../../services/api';
import CommentItem from './components/CommentItem';
import PollCard from './components/PollCard';

const BoardDetail: React.FC = () => {
    const { postId: id } = useParams<{ postId: string }>();
    const navigate = useNavigate();
    const [post, setPost] = useState<PostDetailResponse | null>(null);
    const [commentInput, setCommentInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [isVoting, setIsVoting] = useState(false);

    // Reply state
    const [replyTarget, setReplyTarget] = useState<{ id: number, name: string } | null>(null);

    const loadData = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const [postData, userData] = await Promise.all([
                postService.getPost(Number(id)),
                memberService.getMyInfo().catch(() => null)
            ]);
            setPost(postData);
            if (userData) {
                setCurrentUserId(userData.id);
            }
        } catch (error) {
            console.error(error);
            alert("게시글을 불러오는데 실패했습니다.");
            navigate(-1);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
        window.scrollTo(0, 0);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const handleCommentSubmit = async () => {
        if (!commentInput.trim() || !id) return;
        try {
            if (replyTarget) {
                await commentService.createReply(Number(id), replyTarget.id, commentInput, true); // default anon for now
            } else {
                await commentService.createComment(Number(id), commentInput, true);
            }
            setCommentInput('');
            setReplyTarget(null);
            // Refresh post to show new comment
            const data = await postService.getPost(Number(id));
            setPost(data);
        } catch (error) {
            console.error(error);
            alert("댓글 작성 실패");
        }
    };

    const handleVote = async (optionId: number) => {
        if (!post?.poll || !id || isVoting) return;
        setIsVoting(true);
        try {
            const updatedPoll = await postService.votePoll(Number(id), optionId);
            setPost(prev => prev ? { ...prev, poll: updatedPoll } : null);
        } catch (error) {
            console.error(error);
            alert('투표에 실패했습니다.');
        } finally {
            setIsVoting(false);
        }
    };

    const handleCommentDelete = async (commentId: number) => {
        if (!window.confirm('댓글을 삭제하시겠습니까?')) return;
        try {
            await commentService.deleteComment(commentId);
            const data = await postService.getPost(Number(id));
            setPost(data);
        } catch (e) {
            console.error(e);
            alert('삭제 실패');
        }
    };

    const handleCommentLike = async (commentId: number, isLiked: boolean) => {
        try {
            if (isLiked) {
                await commentService.unlikeComment(commentId);
            } else {
                await commentService.likeComment(commentId);
            }
            const data = await postService.getPost(Number(id));
            setPost(data);
        } catch (e) {
            console.error(e);
        }
    };

    if (loading) return <div style={{ color: 'var(--on-surface)' }}>로딩 중...</div>;
    if (!post) return <div style={{ color: 'var(--on-surface)' }}>게시글을 찾을 수 없습니다.</div>;

    const isAuthor = post.isMine !== undefined ? post.isMine : (post.memberId === currentUserId);

    return (
        <div style={{ maxWidth: '900px', margin: '0 auto' }}>
            {/* Navigation */}
            <div style={{ marginBottom: '24px' }}>
                <span
                    onClick={() => navigate(-1)}
                    style={{
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        fontSize: '15px',
                        color: 'var(--on-surface-variant)',
                        fontWeight: '600',
                        transition: 'color 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.color = 'var(--primary)'}
                    onMouseLeave={(e) => e.currentTarget.style.color = 'var(--on-surface-variant)'}
                >
                    <MdArrowBack style={{ marginRight: '8px' }} size={20} /> 목록으로 돌아가기
                </span>
            </div>

            <div>
                {/* Post Header */}
                <div style={{ marginBottom: '40px', borderBottom: '1px solid var(--border-color)', paddingBottom: '32px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                        <h1 style={{ fontSize: '32px', fontWeight: '800', margin: 0, color: 'var(--on-surface)', lineHeight: '1.3' }}>
                            {post.title}
                        </h1>
                        {isAuthor && (
                            <div style={{ display: 'flex', gap: '8px' }}>
                                <button
                                    onClick={() => navigate(`/board/edit/${post.id}`)}
                                    style={{ background: 'var(--field-bg)', border: 'none', color: 'var(--on-surface-variant)', cursor: 'pointer', fontSize: '13px', padding: '6px 12px', borderRadius: '8px', fontWeight: '600' }}
                                >
                                    수정
                                </button>
                                <button
                                    onClick={async () => {
                                        if (window.confirm('정말 삭제하시겠습니까?')) {
                                            try {
                                                await postService.deletePost(post.id);
                                                alert('삭제되었습니다.');
                                                navigate('/board');
                                            } catch (e) {
                                                console.error(e);
                                                alert('삭제 실패');
                                            }
                                        }
                                    }}
                                    style={{ background: 'var(--field-bg)', border: 'none', color: 'var(--error)', cursor: 'pointer', fontSize: '13px', padding: '6px 12px', borderRadius: '8px', fontWeight: '600' }}
                                >
                                    삭제
                                </button>
                            </div>
                        )}
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <div style={{ width: '48px', height: '48px', borderRadius: '50%', background: 'linear-gradient(135deg, var(--primary), #81C784)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 'bold' }}>
                            {(post.authorName || '익').substring(0, 1)}
                        </div>
                        <div>
                            <div style={{ fontWeight: '700', fontSize: '16px', color: 'var(--on-surface)', marginBottom: '4px' }}>
                                {post.authorName || (post.isAuthor ? '나' : '익명')}
                            </div>
                            <div style={{ display: 'flex', gap: '8px', fontSize: '13px', color: 'var(--on-surface-variant)' }}>
                                <span>{post.dateText || post.createdAt}</span>
                                <span>·</span>
                                <span>조회 {post.viewCount || 0}</span>
                            </div>
                        </div>
                        {!isAuthor && (
                            <button
                                onClick={async () => {
                                    if (window.confirm(`${post.authorName || '익명'}님과 채팅을 시작하시겠습니까?`)) {
                                        try {
                                            const roomId = await chatService.createChatRoom(post.id);
                                            navigate(`/message/detail/${roomId}`);
                                        } catch (e: any) {
                                            console.error(e);
                                            if (e.response?.data?.message) {
                                                alert(e.response.data.message);
                                            } else {
                                                alert('채팅방을 생성할 수 없습니다.');
                                            }
                                        }
                                    }
                                }}
                                style={{
                                    marginLeft: 'auto',
                                    padding: '10px 20px',
                                    borderRadius: '12px',
                                    backgroundColor: 'var(--primary-container)',
                                    color: 'var(--primary)',
                                    border: 'none',
                                    fontWeight: '700',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    fontSize: '14px',
                                    transition: 'all 0.2s'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.backgroundColor = 'var(--primary)';
                                    e.currentTarget.style.color = 'white';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.backgroundColor = 'var(--primary-container)';
                                    e.currentTarget.style.color = 'var(--primary)';
                                }}
                            >
                                <MdChat size={20} />
                                채팅하기
                            </button>
                        )}
                    </div>
                </div>

                {/* Post Content */}
                <div style={{ fontSize: '17px', lineHeight: '1.8', color: 'var(--on-surface)', whiteSpace: 'pre-wrap', marginBottom: '60px', minHeight: '200px' }}>
                    {post.isBlinded ? (
                        <div style={{
                            padding: '60px',
                            backgroundColor: 'var(--field-bg)',
                            borderRadius: '16px',
                            textAlign: 'center',
                            color: 'var(--on-surface-variant)',
                            fontWeight: '700',
                            border: '1px dashed var(--border-color)'
                        }}>
                            관리자에 의해 블라인드 처리된 게시글입니다.
                        </div>
                    ) : (
                        <>
                            {post.content}
                            {(post.imageUrls && post.imageUrls.length > 0) || post.imageUrl ? (
                                <div style={{ marginTop: '32px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                    {post.imageUrls && post.imageUrls.length > 0 ? (
                                        post.imageUrls.map((url, i) => (
                                            <img
                                                key={i}
                                                src={url.startsWith('http') || url.startsWith('/') ? url : `/uploads/${url}`}
                                                alt={`Post image ${i + 1}`}
                                                style={{ display: 'block', maxWidth: '100%', borderRadius: '16px', border: '1px solid var(--border-color)' }}
                                            />
                                        ))
                                    ) : post.imageUrl ? (
                                        <img
                                            src={post.imageUrl.startsWith('http') || post.imageUrl.startsWith('/') ? post.imageUrl : `/uploads/${post.imageUrl}`}
                                            alt="Post image"
                                            style={{ display: 'block', maxWidth: '100%', borderRadius: '16px', border: '1px solid var(--border-color)' }}
                                        />
                                    ) : null}
                                </div>
                            ) : null}
                        </>
                    )}

                    {/* Poll Section */}
                    {post.poll && (
                        <div style={{ marginTop: '40px' }}>
                            <PollCard poll={post.poll} onVote={handleVote} isVoting={isVoting} />
                        </div>
                    )}
                </div>

                {/* Stats & Actions */}
                <div style={{ display: 'flex', justifyContent: 'center', gap: '16px', marginBottom: '40px' }}>
                    <button
                        onClick={async () => {
                            try {
                                if (post.isLiked) {
                                    const res = await postService.unlikePost(post.id);
                                    setPost(prev => prev ? { ...prev, isLiked: false, likeCount: res.likeCount } : null);
                                } else {
                                    const res = await postService.likePost(post.id);
                                    setPost(prev => prev ? { ...prev, isLiked: true, likeCount: res.likeCount } : null);
                                }
                            } catch (e) {
                                console.error(e);
                            }
                        }}
                        style={{
                            borderRadius: '24px',
                            padding: '12px 24px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            border: 'none',
                            backgroundColor: post.isLiked ? 'var(--primary)' : 'var(--field-bg)',
                            color: post.isLiked ? 'white' : 'var(--on-surface)',
                            cursor: 'pointer',
                            fontWeight: '700',
                            transition: 'all 0.2s',
                            boxShadow: post.isLiked ? '0 4px 12px rgba(100, 149, 235, 0.3)' : 'none'
                        }}
                    >
                        {post.isLiked ? <MdThumbUp size={20} /> : <MdThumbUpOffAlt size={20} />}
                        <span>공감 {post.likeCount}</span>
                    </button>
                    <button
                        onClick={async () => {
                            try {
                                if (post.isScraped) {
                                    await postService.unscrapPost(post.id);
                                    setPost(prev => prev ? { ...prev, isScraped: false, scrapCount: Math.max(0, (prev.scrapCount || 0) - 1) } : null);
                                } else {
                                    await postService.scrapPost(post.id);
                                    setPost(prev => prev ? { ...prev, isScraped: true, scrapCount: (prev?.scrapCount || 0) + 1 } : null);
                                }
                            } catch (e) {
                                console.error(e);
                            }
                        }}
                        style={{
                            borderRadius: '24px',
                            padding: '12px 24px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            border: 'none',
                            backgroundColor: post.isScraped ? '#FFC107' : 'var(--field-bg)',
                            color: post.isScraped ? 'white' : 'var(--on-surface)',
                            cursor: 'pointer',
                            fontWeight: '700',
                            transition: 'all 0.2s',
                            boxShadow: post.isScraped ? '0 4px 12px rgba(255, 193, 7, 0.3)' : 'none'
                        }}
                    >
                        {post.isScraped ? <MdStar size={20} /> : <MdStarBorder size={20} />}
                        <span>스크랩 {post.scrapCount || 0}</span>
                    </button>
                </div>

                <div style={{ height: '1px', background: 'var(--border-color)', margin: '0 0 40px 0' }}></div>

                {/* Comments Section */}
                <div>
                    <h3 style={{ fontSize: '20px', fontWeight: '800', marginBottom: '24px', color: 'var(--on-surface)' }}>
                        댓글 <span style={{ color: 'var(--primary)' }}>{post.commentCount}</span>
                    </h3>

                    {/* Comment Input */}
                    <div style={{ marginBottom: '40px', border: '1px solid var(--border-color)', borderRadius: '16px', padding: '20px', background: 'var(--field-bg)', transition: 'border-color 0.2s' }}>
                        {replyTarget && (
                            <div style={{ fontSize: '13px', color: 'var(--on-surface-variant)', marginBottom: '12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', backgroundColor: 'rgba(100, 149, 235, 0.1)', padding: '8px 12px', borderRadius: '8px' }}>
                                <span>To. <b>{replyTarget.name}</b></span>
                                <span onClick={() => setReplyTarget(null)} style={{ cursor: 'pointer', fontWeight: 'bold', color: 'var(--primary)' }}>✕ 취소</span>
                            </div>
                        )}
                        <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                            <textarea
                                value={commentInput}
                                onChange={(e) => setCommentInput(e.target.value)}
                                placeholder={replyTarget ? "답글을 남겨보세요" : "댓글을 남겨보세요"}
                                style={{
                                    flex: 1, border: 'none', background: 'transparent', outline: 'none', resize: 'none', minHeight: '60px',
                                    fontFamily: 'inherit', fontSize: '15px', color: 'var(--on-surface)', lineHeight: '1.6'
                                }}
                            />
                            <button
                                onClick={handleCommentSubmit}
                                style={{
                                    height: '48px',
                                    padding: '0 24px',
                                    backgroundColor: 'var(--primary)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '12px',
                                    fontWeight: '700',
                                    cursor: commentInput.trim() ? 'pointer' : 'default',
                                    opacity: commentInput.trim() ? 1 : 0.5,
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px'
                                }}
                            >
                                <MdSend size={18} />
                                <span>등록</span>
                            </button>
                        </div>
                    </div>

                    {/* Comment List */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                        {post.comments && post.comments.length > 0 ? (
                            post.comments.map(comment => (
                                <CommentItem
                                    key={comment.id}
                                    comment={comment}
                                    currentMemberId={currentUserId || undefined}
                                    onReply={(id, name) => setReplyTarget({ id, name })}
                                    onLike={(id) => handleCommentLike(id, comment.isLiked)}
                                    onDelete={handleCommentDelete}
                                    onEditSuccess={() => postService.getPost(Number(id)).then(setPost)}
                                />
                            ))
                        ) : (
                            <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--on-surface-variant)', fontSize: '15px' }}>
                                첫 번째 댓글을 남겨보세요!
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BoardDetail;
