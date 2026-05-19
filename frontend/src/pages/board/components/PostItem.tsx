import React from 'react';
import { MdThumbUp, MdChatBubbleOutline } from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import type { PostResponse } from '../../../services/api';

interface PostItemProps {
    post: PostResponse;
    isLast?: boolean;
}

const PostItem: React.FC<PostItemProps> = ({ post, isLast }) => {
    const navigate = useNavigate();

    const formatDate = (dateString?: string) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
    };

    return (
        <div
            onClick={() => navigate(`/board/detail/${post.id}`)}
            style={{
                padding: '24px 0',
                backgroundColor: 'transparent',
                borderBottom: isLast ? 'none' : '1px solid var(--border-color)',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '24px',
                transition: 'background-color 0.2s'
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = 'rgba(0,0,0,0.02)';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'transparent';
            }}
        >
            <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                    <span style={{
                        fontSize: '12px',
                        fontWeight: 700,
                        color: 'var(--primary)'
                    }}>
                        {post.boardName}
                    </span>
                    <span style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                        · {formatDate(post.createdAt)}
                    </span>
                </div>

                <h3 style={{
                    fontSize: '17px',
                    fontWeight: '700',
                    marginBottom: '6px',
                    lineHeight: '1.4',
                    color: post.isBlinded ? 'var(--on-surface-variant)' : 'var(--on-surface)',
                    display: '-webkit-box',
                    WebkitLineClamp: 1,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden'
                }}>
                    {post.isBlinded ? '블라인드 처리된 게시글입니다.' : post.title}
                </h3>

                <p style={{
                    fontSize: '14px',
                    color: 'var(--on-surface-variant)',
                    marginBottom: '12px',
                    display: '-webkit-box',
                    WebkitLineClamp: 1,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                    lineHeight: '1.5'
                }}>
                    {post.isBlinded ? '관리자에 의해 규제된 글입니다.' : post.content}
                </p>

                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--primary)', fontWeight: 600, fontSize: '13px' }}>
                        <MdThumbUp size={14} />
                        <span>{post.likeCount}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#4CAF50', fontWeight: 600, fontSize: '13px' }}>
                        <MdChatBubbleOutline size={14} />
                        <span>{post.commentCount}</span>
                    </div>
                    <span style={{ fontSize: '13px', color: 'var(--on-surface-variant)', marginLeft: '4px' }}>
                        {post.isMine ? '나' : '익명'}
                    </span>
                </div>
            </div>

            {/* Right side image thumbnail (Smaller for list style) */}
            {post.imageUrls && post.imageUrls.length > 0 && (
                <div style={{ flexShrink: 0 }}>
                    <img
                        src={post.imageUrls[0].startsWith('http') || post.imageUrls[0].startsWith('/')
                            ? post.imageUrls[0]
                            : `/uploads/${post.imageUrls[0]}`}
                        alt="Post thumbnail"
                        style={{
                            width: '64px',
                            height: '64px',
                            objectFit: 'cover',
                            borderRadius: '12px',
                            border: '1px solid var(--border-color)'
                        }}
                    />
                </div>
            )}
        </div>
    );
};

export default PostItem;
