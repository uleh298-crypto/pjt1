import React, { useState, useEffect } from 'react';
import { requestFCMToken } from '../../firebaseConfig';
import { notificationService } from '../../services/api';

export default function SettingPage() {
    const [notificationEnabled, setNotificationEnabled] = useState(false);
    const [fcmToken, setFcmToken] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // 페이지 로드 시 FCM 토큰 요청
        const initFCM = async () => {
            const token = await requestFCMToken();
            if (token) {
                setFcmToken(token);
                // localStorage에서 이전 설정 불러오기
                const savedSetting = localStorage.getItem('notification_enabled');
                setNotificationEnabled(savedSetting === 'true');
            }
        };
        initFCM();
    }, []);

    const handleToggle = async () => {
        if (!fcmToken) {
            alert('FCM 토큰을 먼저 가져와야 합니다.');
            return;
        }

        setLoading(true);
        try {
            if (!notificationEnabled) {
                // 구독
                await notificationService.subscribe(fcmToken);
                setNotificationEnabled(true);
                localStorage.setItem('notification_enabled', 'true');
                alert('✅ 매일 아침/저녁 입퇴실 알림이 활성화되었습니다!');
            } else {
                // 구독 해지
                await notificationService.unsubscribe(fcmToken);
                setNotificationEnabled(false);
                localStorage.setItem('notification_enabled', 'false');
                alert('🔕 알림이 비활성화되었습니다.');
            }
        } catch (error) {
            console.error('Failed to toggle notification:', error);
            alert('알림 설정 변경에 실패했습니다. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container" style={{ padding: '24px', maxWidth: '600px', margin: '0 auto' }}>
            <h1 style={{ fontSize: '24px', marginBottom: '24px' }}>⚙️ 설정</h1>

            <div style={{
                backgroundColor: '#f9f9f9',
                padding: '20px',
                borderRadius: '8px',
                marginBottom: '16px'
            }}>
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <div>
                        <h3 style={{ fontSize: '18px', marginBottom: '8px' }}>
                            🔔 입퇴실 알림
                        </h3>
                        <p style={{ fontSize: '14px', color: '#666', margin: 0 }}>
                            매일 아침 9시, 저녁 6시에 입퇴실 알림을 받습니다
                        </p>
                    </div>
                    <button
                        onClick={handleToggle}
                        disabled={loading || !fcmToken}
                        style={{
                            padding: '8px 16px',
                            borderRadius: '20px',
                            border: 'none',
                            backgroundColor: notificationEnabled ? '#4CAF50' : '#ccc',
                            color: 'white',
                            cursor: loading || !fcmToken ? 'not-allowed' : 'pointer',
                            fontSize: '14px',
                            fontWeight: 'bold',
                            transition: 'all 0.3s'
                        }}
                    >
                        {loading ? '처리중...' : notificationEnabled ? 'ON' : 'OFF'}
                    </button>
                </div>
            </div>

            {!fcmToken && (
                <div style={{
                    backgroundColor: '#fff3cd',
                    color: '#856404',
                    padding: '12px',
                    borderRadius: '8px',
                    fontSize: '14px'
                }}>
                    ⚠️ 알림 권한을 허용해주세요. 브라우저에서 알림 권한을 허용해야 이 기능을 사용할 수 있습니다.
                </div>
            )}
        </div>
    );
}
