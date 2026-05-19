import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/api';

const SplashPage: React.FC = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const timer = setTimeout(() => {
            const isLoggedIn = authService.isLoggedIn();
            if (isLoggedIn) {
                navigate('/home');
            } else {
                navigate('/login');
            }
        }, 2000);

        return () => clearTimeout(timer);
    }, [navigate]);

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh',
            background: 'linear-gradient(180deg, #2F6BFF 0%, var(--background) 100%)',
            color: 'var(--on-background)'
        }}>
            <div style={{
                background: 'rgba(255, 255, 255, 0.95)',
                borderRadius: '28px',
                padding: '16px',
                width: '120px',
                height: '120px',
                display: 'flex',
                boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
            }}>
                <img src="/vite.svg" alt="App Logo" style={{ width: '100%', objectFit: 'contain' }} />
            </div>

            <div style={{ height: '16px' }}></div>

            <div style={{ fontSize: '24px', fontWeight: 'bold' }}>싸브리타임</div>

            <div style={{ height: '4px' }}></div>

            <div style={{ fontSize: '13px', opacity: 0.7 }}>SSAFY 캠퍼스 커뮤니티</div>

            <div style={{ height: '24px' }}></div>

            {/* Simple CSS Loading Bar */}
            <div style={{ width: '200px', height: '4px', background: 'rgba(255,255,255,0.3)', borderRadius: '2px', overflow: 'hidden' }}>
                <div style={{ width: '100%', height: '100%', background: 'var(--primary)', animation: 'progress 2s linear' }}></div>
            </div>

            <style>{`
                @keyframes progress {
                    0% { width: 0%; }
                    100% { width: 100%; }
                }
            `}</style>
        </div>
    );
};

export default SplashPage;
