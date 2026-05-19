import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdArrowBack, MdChatBubbleOutline } from 'react-icons/md';
import { myPageService } from '../../services/api';
import type { MyCommentResponse } from '../../services/api';

const MyCommentsPage: React.FC = () => {
    const navigate = useNavigate();
    const [comments, setComments] = useState<MyCommentResponse[]>([]);
    const [loading, setLoading] = useState(false);

    const loadComments = async () => {
        setLoading(true);
        try {
            const res = await myPageService.getMyComments();
            setComments(res);
        } catch (error) {
            console.error('댓글 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadComments();
    }, []);

    const formatDate = (dateStr: string) => {
        const date = new Date(dateStr);
        return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
    };

    return (
        <div style={{ minHeight: '100%', backgroundColor: 'var(--background)' }}>
            {/* Header */}
            <div style={{
                padding: '24px 20px',
                borderBottom: '1px solid var(--border-color)',
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                position: 'sticky',
                top: 0,
                backgroundColor: 'var(--surface)',
                zIndex: 100
            }}>
                <div
                    onClick={() => navigate(-1)}
                    style={{
                        cursor: 'pointer',
                        padding: '8px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        transition: 'background-color 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                    <MdArrowBack size={24} />
                </div>
                <h1 style={{ fontSize: '22px', fontWeight: 'bold', margin: 0 }}>내가 작성한 댓글</h1>
            </div>

            {/* Content Area */}
            <div className="container" style={{ padding: '40px 20px' }}>
                {loading && comments.length === 0 ? (
                    <div style={{ padding: '100px 0', textAlign: 'center', color: 'var(--on-surface-variant)' }}>
                        <div className="loading-spinner" />
                        <p style={{ marginTop: '16px' }}>댓글을 불러오는 중입니다...</p>
                    </div>
                ) : comments.length === 0 ? (
                    <div style={{
                        padding: '100px 0',
                        textAlign: 'center',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '24px',
                        border: '1px dashed var(--border-color)',
                        color: 'var(--on-surface-variant)'
                    }}>
                        <p style={{ fontSize: '18px' }}>작성한 댓글이 없습니다.</p>
                    </div>
                ) : (
                    <div style={{
                        maxWidth: '900px',
                        margin: '0 auto',
                        display: 'flex',
                        flexDirection: 'column'
                    }}>
                        {comments.map((comment, index) => (
                            <div
                                key={comment.id}
                                onClick={() => navigate(`/board/detail/${comment.postId}`)}
                                style={{
                                    padding: '24px 0',
                                    borderBottom: index === comments.length - 1 ? 'none' : '1px solid var(--border-color)',
                                    cursor: 'pointer',
                                    transition: 'background-color 0.2s',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '12px'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.backgroundColor = 'rgba(0,0,0,0.02)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.backgroundColor = 'transparent';
                                }}
                            >
                                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                                    <div style={{
                                        marginTop: '4px',
                                        color: 'var(--primary)',
                                        backgroundColor: 'rgba(100, 149, 235, 0.1)',
                                        padding: '8px',
                                        borderRadius: '8px'
                                    }}>
                                        <MdChatBubbleOutline size={20} />
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{
                                            fontSize: '16px',
                                            fontWeight: '600',
                                            color: 'var(--on-surface)',
                                            marginBottom: '8px',
                                            lineHeight: '1.5'
                                        }}>
                                            {comment.content}
                                        </div>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <div style={{ fontSize: '13px', color: 'var(--on-surface-variant)' }}>
                                                원문: <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{comment.postTitle}</span>
                                            </div>
                                            <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)', opacity: 0.7 }}>
                                                {formatDate(comment.createdAt)}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default MyCommentsPage;
