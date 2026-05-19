import React, { useState } from 'react';
import { MdThumbUp, MdThumbUpOffAlt, MdMoreVert, MdSubdirectoryArrowRight } from 'react-icons/md';
import type { CommentResponse, ReplyResponse } from '../../../services/api';

interface CommentItemProps {
    comment: CommentResponse;
    onReply: (commentIds: number, authorName: string) => void;
    onLike: (commentId: number) => void;
    onDelete?: (commentId: number) => void;
    onEditSuccess?: () => void;
    currentMemberId?: number; // verify ownership
}

import { commentService } from '../../../services/api';

const CommentItem: React.FC<CommentItemProps> = ({ comment, onReply, onLike, onDelete, onEditSuccess, currentMemberId }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editContent, setEditContent] = useState(comment.content);
    const [showMenu, setShowMenu] = useState(false);

    const isMine = comment.anon?.isMine || (currentMemberId !== undefined && comment.memberId === currentMemberId);

    const handleEditSubmit = async () => {
        if (!editContent.trim()) return;
        try {
            await commentService.updateComment(comment.id, editContent);
            setIsEditing(false);
            onEditSuccess?.();
        } catch (e) {
            console.error(e);
            alert('수정 실패');
        }
    };

    // Helper to render content
    const renderContent = (content: string, isBlinded: boolean) => {
        if (isBlinded) return <span style={{ color: 'var(--on-surface-variant)' }}>블라인드 처리된 댓글입니다.</span>;
        return content;
    };

    return (
        <div style={{ padding: '12px 16px', borderBottom: '1px solid var(--border-color)' }}>
            {/* Header: Author + Date + More */}
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span style={{ fontSize: '13px', fontWeight: 'bold', color: comment.anon?.isAuthor ? 'var(--primary)' : 'var(--on-surface)' }}>
                        {comment.anon?.name || (isMine ? '나' : '익명')}
                    </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', position: 'relative' }}>
                    <span style={{ fontSize: '11px', color: 'var(--on-surface-variant)' }}>
                        {comment.createdAt ? new Date(comment.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </span>
                    {isMine && !comment.isBlinded && (
                        <>
                            <MdMoreVert
                                size={18}
                                color="var(--on-surface-variant)"
                                style={{ cursor: 'pointer' }}
                                onClick={() => setShowMenu(!showMenu)}
                            />
                            {showMenu && (
                                <div style={{
                                    position: 'absolute',
                                    top: '24px',
                                    right: 0,
                                    backgroundColor: 'var(--surface)',
                                    borderRadius: '8px',
                                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                                    border: '1px solid var(--border-color)',
                                    zIndex: 10,
                                    padding: '4px',
                                    minWidth: '80px'
                                }}>
                                    <div
                                        onClick={() => { setIsEditing(true); setShowMenu(false); }}
                                        style={{ padding: '8px 12px', fontSize: '13px', cursor: 'pointer', borderRadius: '4px', textAlign: 'center' }}
                                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    >
                                        수정
                                    </div>
                                    <div
                                        onClick={() => { onDelete?.(comment.id); setShowMenu(false); }}
                                        style={{ padding: '8px 12px', fontSize: '13px', cursor: 'pointer', borderRadius: '4px', textAlign: 'center', color: 'var(--error)' }}
                                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    >
                                        삭제
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>

            {/* Content */}
            {isEditing ? (
                <div style={{ marginBottom: '12px' }}>
                    <textarea
                        value={editContent}
                        onChange={(e) => setEditContent(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '12px',
                            borderRadius: '8px',
                            border: '1px solid var(--primary)',
                            backgroundColor: 'var(--field-bg)',
                            color: 'var(--on-surface)',
                            fontSize: '14px',
                            minHeight: '80px',
                            outline: 'none',
                            resize: 'none',
                            marginBottom: '8px'
                        }}
                    />
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                        <button onClick={() => setIsEditing(false)} style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '12px', cursor: 'pointer' }}>취소</button>
                        <button onClick={handleEditSubmit} style={{ padding: '6px 12px', borderRadius: '6px', backgroundColor: 'var(--primary)', color: 'white', border: 'none', fontSize: '12px', cursor: 'pointer' }}>저장</button>
                    </div>
                </div>
            ) : (
                <div style={{ fontSize: '14px', marginBottom: '8px', lineHeight: '1.4', color: 'var(--on-surface)' }}>
                    {renderContent(comment.content, comment.isBlinded)}
                </div>
            )}

            {/* Actions */}
            <div style={{ display: 'flex', gap: '12px', fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                <div onClick={() => onReply(comment.id, comment.anon?.name || '익명')} style={{ cursor: 'pointer' }}>답글</div>
                <div onClick={() => onLike(comment.id)} style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '2px' }}>
                    {comment.isLiked ? <MdThumbUp color="var(--primary)" /> : <MdThumbUpOffAlt />}
                    <span>{comment.likeCount > 0 ? comment.likeCount : '좋아요'}</span>
                </div>
            </div>

            {/* Nested Replies */}
            {comment.replies && comment.replies.length > 0 && (
                <div style={{ marginTop: '12px', paddingLeft: '12px', borderLeft: '2px solid var(--border-color)' }}>
                    {comment.replies.map(reply => (
                        <ReplyItem
                            key={reply.id}
                            reply={reply}
                            onLike={onLike}
                            onDelete={onDelete}
                            onEditSuccess={onEditSuccess}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

const ReplyItem: React.FC<{
    reply: ReplyResponse;
    onLike: (id: number) => void;
    onDelete?: (id: number) => void;
    onEditSuccess?: () => void;
}> = ({ reply, onLike, onDelete, onEditSuccess }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editContent, setEditContent] = useState(reply.content);
    const [showMenu, setShowMenu] = useState(false);

    const isMine = reply.anon?.isMine;

    const handleEditSubmit = async () => {
        if (!editContent.trim()) return;
        try {
            await commentService.updateComment(reply.id, editContent);
            setIsEditing(false);
            onEditSuccess?.();
        } catch (e) {
            console.error(e);
            alert('수정 실패');
        }
    };

    return (
        <div style={{ padding: '8px 0', borderTop: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                <span style={{ fontSize: '13px', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '4px', color: reply.anon?.isAuthor ? 'var(--primary)' : 'var(--on-surface)' }}>
                    <MdSubdirectoryArrowRight size={14} color="var(--on-surface-variant)" />
                    {reply.anon?.name || (isMine ? '나' : '익명')}
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', position: 'relative' }}>
                    <span style={{ fontSize: '11px', color: 'var(--on-surface-variant)' }}>
                        {reply.createdAt ? new Date(reply.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </span>
                    {isMine && !reply.isBlinded && (
                        <>
                            <MdMoreVert
                                size={16}
                                color="var(--on-surface-variant)"
                                style={{ cursor: 'pointer' }}
                                onClick={() => setShowMenu(!showMenu)}
                            />
                            {showMenu && (
                                <div style={{
                                    position: 'absolute',
                                    top: '20px',
                                    right: 0,
                                    backgroundColor: 'var(--surface)',
                                    borderRadius: '8px',
                                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                                    border: '1px solid var(--border-color)',
                                    zIndex: 10,
                                    padding: '4px',
                                    minWidth: '80px'
                                }}>
                                    <div
                                        onClick={() => { setIsEditing(true); setShowMenu(false); }}
                                        style={{ padding: '6px 10px', fontSize: '12px', cursor: 'pointer', borderRadius: '4px', textAlign: 'center' }}
                                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    >
                                        수정
                                    </div>
                                    <div
                                        onClick={() => { onDelete?.(reply.id); setShowMenu(false); }}
                                        style={{ padding: '6px 10px', fontSize: '12px', cursor: 'pointer', borderRadius: '4px', textAlign: 'center', color: 'var(--error)' }}
                                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    >
                                        삭제
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>

            {isEditing ? (
                <div style={{ marginBottom: '8px', paddingLeft: '18px' }}>
                    <textarea
                        value={editContent}
                        onChange={(e) => setEditContent(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '8px',
                            borderRadius: '8px',
                            border: '1px solid var(--primary)',
                            backgroundColor: 'var(--field-bg)',
                            color: 'var(--on-surface)',
                            fontSize: '13px',
                            minHeight: '60px',
                            outline: 'none',
                            resize: 'none',
                            marginBottom: '4px'
                        }}
                    />
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '6px' }}>
                        <button onClick={() => setIsEditing(false)} style={{ padding: '4px 8px', borderRadius: '4px', border: '1px solid var(--border-color)', fontSize: '11px', cursor: 'pointer' }}>취소</button>
                        <button onClick={handleEditSubmit} style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: 'var(--primary)', color: 'white', border: 'none', fontSize: '11px', cursor: 'pointer' }}>저장</button>
                    </div>
                </div>
            ) : (
                <div style={{ fontSize: '13px', paddingLeft: '18px', color: 'var(--on-surface)', marginBottom: '4px' }}>
                    {reply.isBlinded ? '블라인드 처리된 댓글입니다.' : reply.content}
                </div>
            )}

            <div style={{ display: 'flex', gap: '12px', fontSize: '11px', color: 'var(--on-surface-variant)', paddingLeft: '18px' }}>
                <div onClick={() => onLike(reply.id)} style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '2px' }}>
                    {reply.isLiked ? <MdThumbUp size={12} color="var(--primary)" /> : <MdThumbUpOffAlt size={12} />}
                    <span>{reply.likeCount > 0 ? reply.likeCount : '좋아요'}</span>
                </div>
            </div>
        </div>
    );
};

export default CommentItem;
