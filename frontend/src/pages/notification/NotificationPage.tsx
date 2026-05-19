import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationService } from '../../services/api';
import type { Notification } from '../../services/api';
import { MdNotifications, MdCheckCircle, MdOutlineMessage, MdComment, MdInfo, MdKeyboardArrowRight, MdSettings } from 'react-icons/md';

const NotificationPage: React.FC = () => {
    const navigate = useNavigate();
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [loading, setLoading] = useState(true);

    const loadNotifications = async () => {
        try {
            const data = await notificationService.getNotifications();
            setNotifications(data);
        } catch (error) {
            console.error('Failed to load notifications:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadNotifications();
    }, []);

    const handleRead = async (id: number, url: string) => {
        try {
            await notificationService.readNotification(id);
            if (url) {
                // Map backend URLs to frontend URLs
                let mappedUrl = url;

                if (url.startsWith('/posts/')) {
                    // Convert /posts/{id} to /board/detail/{id}
                    const postId = url.replace('/posts/', '');
                    mappedUrl = `/board/detail/${postId}`;
                } else if (url.startsWith('/chats/')) {
                    // Convert /chats/{id} to /message/detail/{id}
                    const chatId = url.replace('/chats/', '');
                    mappedUrl = `/message/detail/${chatId}`;
                } else if (url.startsWith('/teams/')) {
                    // Convert /teams/{id}/applications to /mygroups/team/{id}
                    const match = url.match(/\/teams\/(\d+)/);
                    if (match) {
                        mappedUrl = `/mygroups/team/${match[1]}`;
                    }
                } else if (url.startsWith('/studies/')) {
                    // Convert /studies/{id}/applications to /mygroups/study/{id}
                    const match = url.match(/\/studies\/(\d+)/);
                    if (match) {
                        mappedUrl = `/mygroups/study/${match[1]}`;
                    }
                }

                navigate(mappedUrl);
            } else {
                // Refresh list if no URL
                loadNotifications();
            }
        } catch (error) {
            console.error('Failed to mark as read:', error);
        }
    };

    const getTypeIcon = (type: string) => {
        switch (type) {
            case 'COMMENT': return <MdComment size={20} style={{ color: '#5B7FFF' }} />;
            case 'REPLY': return <MdComment size={20} style={{ color: '#5B7FFF' }} />;
            case 'MESSAGE': return <MdOutlineMessage size={20} style={{ color: '#4CAF50' }} />;
            case 'NOTICE': return <MdInfo size={20} style={{ color: '#FF9800' }} />;
            case 'APPLICATION': return <MdCheckCircle size={20} style={{ color: '#FF5252' }} />;
            default: return <MdNotifications size={20} style={{ color: 'var(--on-surface-variant)' }} />;
        }
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>로딩 중...</div>;

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '24px 20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                    알림 <span style={{
                        fontSize: '14px',
                        backgroundColor: 'var(--primary)',
                        color: 'white',
                        padding: '2px 8px',
                        borderRadius: '12px',
                        marginLeft: '4px'
                    }}>{notifications.filter(n => !n.isRead).length}</span>
                </h1>
            </div>

            {notifications.length === 0 ? (
                <div style={{
                    textAlign: 'center',
                    padding: '80px 20px',
                    backgroundColor: 'var(--surface)',
                    borderRadius: '20px',
                    border: '1px solid var(--border-color)',
                    color: 'var(--on-surface-variant)'
                }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px', opacity: 0.3 }}>🔔</div>
                    <p>받은 알림이 없습니다.</p>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {notifications.map((notif) => (
                        <div
                            key={notif.id}
                            onClick={() => handleRead(notif.id, notif.relatedUrl)}
                            style={{
                                backgroundColor: notif.isRead ? 'var(--surface)' : 'rgba(91, 127, 255, 0.05)',
                                padding: '20px',
                                borderRadius: '16px',
                                border: '1px solid',
                                borderColor: notif.isRead ? 'var(--border-color)' : 'rgba(91, 127, 255, 0.2)',
                                cursor: 'pointer',
                                transition: 'all 0.2s ease',
                                display: 'flex',
                                alignItems: 'flex-start',
                                gap: '16px',
                                position: 'relative',
                                overflow: 'hidden'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-2px)';
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.05)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = 'none';
                            }}
                        >
                            {!notif.isRead && (
                                <div style={{
                                    position: 'absolute',
                                    top: 0,
                                    left: 0,
                                    bottom: 0,
                                    width: '4px',
                                    backgroundColor: 'var(--primary)'
                                }} />
                            )}

                            <div style={{
                                width: '40px',
                                height: '40px',
                                borderRadius: '12px',
                                backgroundColor: notif.isRead ? 'var(--field-bg)' : 'white',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                flexShrink: 0,
                                boxShadow: notif.isRead ? 'none' : '0 2px 8px rgba(0,0,0,0.05)'
                            }}>
                                {getTypeIcon(notif.type)}
                            </div>

                            <div style={{ flex: 1 }}>
                                <div style={{
                                    fontSize: '15px',
                                    fontWeight: notif.isRead ? '500' : '700',
                                    color: 'var(--on-surface)',
                                    lineHeight: '1.4',
                                    marginBottom: '4px'
                                }}>
                                    {notif.content}
                                </div>
                                <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                                    {new Date(notif.createdAt).toLocaleString()}
                                </div>
                            </div>

                            <MdKeyboardArrowRight size={20} color="var(--border-color)" />
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default NotificationPage;
