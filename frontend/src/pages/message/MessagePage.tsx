import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdArrowBack, MdChat, MdAccessTime } from 'react-icons/md';
import { chatService, memberService, type ChatRoomResponse, type ChatListUpdateResponse } from '../../services/api';
import { useStomp } from '../../hooks/useStomp';

const MessagePage: React.FC = () => {
    const navigate = useNavigate();
    const { connect, disconnect, subscribe, connected } = useStomp();
    const [rooms, setRooms] = useState<ChatRoomResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [myId, setMyId] = useState<number | null>(null);

    const loadRooms = async () => {
        try {
            setLoading(true);
            const [data, myInfo] = await Promise.all([
                chatService.getMyChatRooms(),
                memberService.getMyInfo()
            ]);
            setRooms(data);
            setMyId(myInfo.id);
        } catch (error) {
            console.error('Failed to load chat rooms:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRooms();
    }, []);

    useEffect(() => {
        if (myId) {
            connect(() => {
                // 채팅방 목록 업데이트 구독
                subscribe(`/topic/user/${myId}/chat-list`, (payload: ChatListUpdateResponse) => {
                    setRooms(prev => {
                        const index = prev.findIndex(r => r.roomId === payload.roomId);
                        if (index === -1) {
                            // 새로운 방이 생겼을 수도 있으니 전체 목록 다시 불러오기
                            loadRooms();
                            return prev;
                        }
                        const updated = [...prev];
                        updated[index] = {
                            ...updated[index],
                            lastMessage: payload.lastMessage,
                            lastMessageAt: payload.lastMessageAt
                        };
                        return updated;
                    });
                });
            });
        }
        return () => disconnect();
    }, [myId, connect, disconnect, subscribe]);

    const sortedRooms = useMemo(() => {
        return [...rooms].sort((a, b) => {
            const timeA = a.lastMessageAt || a.createdAt;
            const timeB = b.lastMessageAt || b.createdAt;
            return new Date(timeB).getTime() - new Date(timeA).getTime();
        });
    }, [rooms]);

    const formatTime = (timeStr?: string | null) => {
        if (!timeStr) return '';
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now.getTime() - date.getTime();

        if (diff < 60000) return '방금 전';
        if (diff < 3600000) return `${Math.floor(diff / 60000)}분 전`;
        if (diff < 86400000) return `${Math.floor(diff / 3600000)}시간 전`;
        return `${date.getMonth() + 1}월 ${date.getDate()}일`;
    };

    return (
        <div style={{ minHeight: '100vh', backgroundColor: 'var(--background)' }}>
            {/* Header */}
            <div style={{
                position: 'sticky',
                top: 0,
                zIndex: 10,
                backgroundColor: 'var(--surface)',
                borderBottom: '1px solid var(--border-color)',
                padding: '16px 20px',
                display: 'flex',
                alignItems: 'center',
                gap: '12px'
            }}>
                <h1 style={{ fontSize: '18px', fontWeight: 'bold', margin: 0, color: 'var(--on-surface)' }}>메시지</h1>
            </div>

            <div className="container" style={{ maxWidth: '700px', margin: '0 auto', padding: '20px' }}>
                {loading ? (
                    <div style={{ textAlign: 'center', padding: '100px 0' }}>
                        <div className="loading-spinner" />
                        <p style={{ marginTop: '16px', color: 'var(--on-surface-variant)' }}>메시지를 불러오는 중...</p>
                    </div>
                ) : sortedRooms.length === 0 ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '100px 20px',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '24px',
                        border: '1px dashed var(--border-color)',
                        marginTop: '40px'
                    }}>
                        <div style={{ fontSize: '48px', marginBottom: '16px' }}>💬</div>
                        <p style={{ color: 'var(--on-surface-variant)', fontSize: '16px' }}>참여 중인 대화가 없습니다.</p>
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {sortedRooms.map(room => (
                            <div
                                key={room.roomId}
                                onClick={() => navigate(`/message/detail/${room.roomId}`)}
                                style={{
                                    backgroundColor: 'var(--surface)',
                                    borderRadius: '16px',
                                    padding: '16px 20px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '16px',
                                    cursor: 'pointer',
                                    border: '1px solid var(--border-color)',
                                    transition: 'all 0.2s',
                                    position: 'relative',
                                    minHeight: '90px'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = 'translateY(-2px)';
                                    e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,0,0,0.06)';
                                    e.currentTarget.style.borderColor = 'var(--primary)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = 'translateY(0)';
                                    e.currentTarget.style.boxShadow = 'none';
                                    e.currentTarget.style.borderColor = 'var(--border-color)';
                                }}
                            >
                                <div style={{
                                    width: '52px',
                                    height: '52px',
                                    borderRadius: '16px',
                                    backgroundColor: 'var(--primary-container)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    color: 'var(--primary)',
                                    fontSize: '20px',
                                    fontWeight: 'bold'
                                }}>
                                    {room.opponentName.substring(0, 1)}
                                </div>
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                        <div style={{ fontWeight: 'bold', fontSize: '16px', color: 'var(--on-surface)' }}>
                                            {room.opponentName}
                                        </div>
                                        <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                            <MdAccessTime size={14} />
                                            {formatTime(room.lastMessageAt || room.createdAt)}
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '2px' }}>
                                        {room.postTitle && (
                                            <span style={{
                                                fontSize: '11px',
                                                backgroundColor: 'var(--field-bg)',
                                                padding: '2px 6px',
                                                borderRadius: '4px',
                                                color: 'var(--primary)',
                                                fontWeight: 'bold',
                                                whiteSpace: 'nowrap',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                maxWidth: '120px'
                                            }}>
                                                {room.postTitle}
                                            </span>
                                        )}
                                    </div>
                                    <div style={{
                                        fontSize: '14px',
                                        color: 'var(--on-surface-variant)',
                                        whiteSpace: 'nowrap',
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis'
                                    }}>
                                        {room.lastMessage || '대화가 없습니다.'}
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

export default MessagePage;
