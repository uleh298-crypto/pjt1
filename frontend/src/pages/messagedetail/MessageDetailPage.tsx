import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MdArrowBack, MdSend, MdChat } from 'react-icons/md';
import { chatService, memberService, type ChatMessageResponse, type ChatRoomResponse } from '../../services/api';
import { useStomp } from '../../hooks/useStomp';

const MessageDetailPage: React.FC = () => {
    const { roomId } = useParams<{ roomId: string }>();
    const navigate = useNavigate();
    const { connect, disconnect, subscribe, publish, connected } = useStomp();

    const [room, setRoom] = useState<ChatRoomResponse | null>(null);
    const [messages, setMessages] = useState<ChatMessageResponse[]>([]);
    const [inputText, setInputText] = useState('');
    const [loading, setLoading] = useState(true);
    const [myId, setMyId] = useState<number | null>(null);

    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const loadData = async () => {
        if (!roomId) return;
        try {
            setLoading(true);
            const [roomData, msgs, myInfo] = await Promise.all([
                chatService.getChatRoom(Number(roomId)),
                chatService.getChatMessages(Number(roomId)),
                memberService.getMyInfo()
            ]);
            setRoom(roomData);
            setMessages(msgs);
            setMyId(myInfo.id);
        } catch (error) {
            console.error('Failed to load chat details:', error);
            alert('채팅방 정보를 불러올 수 없습니다.');
            navigate('/message');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [roomId]);

    useEffect(() => {
        if (myId && roomId) {
            connect(() => {
                // 특정 채팅방의 메시지 수신 구독 (개별 채널)
                subscribe(`/topic/user/${myId}/chat/${roomId}`, (payload: ChatMessageResponse) => {
                    setMessages(prev => {
                        // 중복 방지 (이미 내가 보낸 메시지가 낙관적 UI 등으로 추가되었을 수 있음)
                        if (prev.find(m => m.id === payload.id)) return prev;
                        return [...prev, payload];
                    });
                });
            });
        }
        return () => disconnect();
    }, [myId, roomId, connect, disconnect, subscribe]);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSendMessage = () => {
        if (!inputText.trim() || !connected || !roomId || !room || myId === null) return;

        const messageContent = inputText.trim();

        // 1. WebSocket을 통해 메시지 발행
        publish(`/app/chat/${roomId}/send`, { content: messageContent });

        // 2. 낙관적 업데이트 (메시지 즉시 반영)
        const tempId = Date.now();
        const newMessage: ChatMessageResponse = {
            id: tempId,
            content: messageContent,
            isMine: true,
            senderName: '나',
            sentAt: new Date().toISOString()
        };

        setMessages(prev => [...prev, newMessage]);

        // 3. 입력창 비우기
        setInputText('');

        // 참고: 서버에서 다시 WebSocket으로 실제 저장된 메시지가 내려오면
        // subscribe 내부의 중복 방지 로직(line 56)에 의해 걸러지거나 교체됨.
    };

    const formatTime = (timeStr: string) => {
        const date = new Date(timeStr);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    const renderDateDivider = (current: ChatMessageResponse, previous?: ChatMessageResponse) => {
        const currentDate = new Date(current.sentAt).toDateString();
        const previousDate = previous ? new Date(previous.sentAt).toDateString() : null;

        if (currentDate !== previousDate) {
            const date = new Date(current.sentAt);
            return (
                <div style={{
                    textAlign: 'center',
                    margin: '24px 0',
                    fontSize: '12px',
                    color: 'var(--on-surface-variant)',
                    backgroundColor: 'var(--field-bg)',
                    padding: '4px 12px',
                    borderRadius: '12px',
                    alignSelf: 'center'
                }}>
                    {date.toLocaleDateString([], { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })}
                </div>
            );
        }
        return null;
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: 'var(--background)' }}>
                <div className="loading-spinner" />
            </div>
        );
    }

    return (
        <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--background)' }}>
            {/* Header */}
            <div style={{
                backgroundColor: 'var(--surface)',
                borderBottom: '1px solid var(--border-color)',
                padding: '12px 20px',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                zIndex: 10
            }}>
                <button
                    onClick={() => navigate('/message')}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '8px' }}
                >
                    <MdArrowBack size={24} color="var(--on-surface)" />
                </button>
                <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 'bold', fontSize: '16px', color: 'var(--on-surface)' }}>
                        {room?.opponentName}
                    </div>
                    {room?.postTitle && (
                        <div style={{ fontSize: '12px', color: 'var(--primary)', fontWeight: 'bold', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '300px' }}>
                            {room.postTitle}
                        </div>
                    )}
                </div>
            </div>

            {/* Chat Body */}
            <div style={{
                flex: 1,
                overflowY: 'auto',
                padding: '20px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px'
            }}>
                {messages.length === 0 ? (
                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--on-surface-variant)', opacity: 0.6 }}>
                        <MdChat size={48} style={{ marginBottom: '16px' }} />
                        <p>대화를 시작해보세요!</p>
                    </div>
                ) : (
                    messages.map((msg, index) => (
                        <React.Fragment key={msg.id}>
                            {renderDateDivider(msg, messages[index - 1])}
                            <div style={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: msg.isMine ? 'flex-end' : 'flex-start',
                                marginBottom: '4px'
                            }}>
                                {!msg.isMine && (
                                    <span style={{ fontSize: '12px', color: 'var(--on-surface-variant)', marginLeft: '12px', marginBottom: '4px', fontWeight: '600' }}>
                                        {msg.senderName}
                                    </span>
                                )}
                                <div style={{ display: 'flex', alignItems: 'flex-end', gap: '6px', maxWidth: '80%', flexDirection: msg.isMine ? 'row' : 'row-reverse' }}>
                                    <span style={{ fontSize: '10px', color: 'var(--on-surface-variant)', marginBottom: '4px' }}>
                                        {formatTime(msg.sentAt)}
                                    </span>
                                    <div style={{
                                        padding: '12px 16px',
                                        borderRadius: msg.isMine ? '20px 4px 20px 20px' : '4px 20px 20px 20px',
                                        backgroundColor: msg.isMine ? 'var(--primary)' : 'var(--surface)',
                                        color: msg.isMine ? 'white' : 'var(--on-surface)',
                                        border: msg.isMine ? 'none' : '1px solid var(--border-color)',
                                        fontSize: '15px',
                                        lineHeight: '1.5',
                                        boxShadow: '0 2px 4px rgba(0,0,0,0.03)',
                                        wordBreak: 'break-all'
                                    }}>
                                        {msg.content}
                                    </div>
                                </div>
                            </div>
                        </React.Fragment>
                    ))
                )}
                <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div style={{
                backgroundColor: 'var(--surface)',
                borderTop: '1px solid var(--border-color)',
                padding: '16px 20px',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                paddingBottom: 'max(16px, env(safe-area-inset-bottom))'
            }}>
                <div style={{
                    flex: 1,
                    backgroundColor: 'var(--field-bg)',
                    borderRadius: '24px',
                    padding: '8px 16px',
                    display: 'flex',
                    alignItems: 'center',
                    border: '1px solid transparent',
                    transition: 'all 0.2s'
                }}>
                    <input
                        type="text"
                        value={inputText}
                        onChange={(e) => setInputText(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                        placeholder="메시지를 입력하세요..."
                        style={{
                            flex: 1,
                            background: 'none',
                            border: 'none',
                            padding: '8px 0',
                            fontSize: '15px',
                            color: 'var(--on-surface)',
                            outline: 'none'
                        }}
                    />
                </div>
                <button
                    onClick={handleSendMessage}
                    disabled={!inputText.trim() || !connected}
                    style={{
                        width: '44px',
                        height: '44px',
                        borderRadius: '50%',
                        backgroundColor: inputText.trim() && connected ? 'var(--primary)' : 'var(--field-bg)',
                        color: inputText.trim() && connected ? 'white' : 'var(--on-surface-variant)',
                        border: 'none',
                        cursor: inputText.trim() && connected ? 'pointer' : 'default',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        transition: 'all 0.2s'
                    }}
                >
                    <MdSend size={24} />
                </button>
            </div>
        </div>
    );
};

export default MessageDetailPage;
