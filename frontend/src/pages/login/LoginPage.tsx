import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/api';

const LoginPage: React.FC = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!email || !password) return;

        setIsLoading(true);
        setError(null);

        try {
            await authService.login(email, password);
            if (authService.isLoggedIn()) {
                navigate('/home');
            } else {
                setError("로그인에 실패했습니다.");
            }
        } catch (err) {
            setError("로그인 중 오류가 발생했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{
            minHeight: '100vh',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: 'var(--background)'
        }}>
            <div style={{ width: '100%', maxWidth: '400px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <img src="/assets/login_logo.png" alt="Login Logo" style={{ width: '80px', marginBottom: '16px' }} />

                <img src="/assets/login_comment.png" alt="SSAFY Comment" style={{ width: '240px', marginBottom: '40px' }} />

                <div style={{
                    width: '100%',
                    background: 'var(--surface)',
                    borderRadius: '12px',
                    padding: '32px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
                    border: '1px solid var(--border-color)'
                }}>
                    <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        <div>
                            <input
                                className="input-field"
                                type="text"
                                placeholder="아이디(이메일)"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                style={{ background: 'var(--field-bg)', height: '48px', color: 'var(--on-surface)' }}
                            />
                        </div>
                        <div>
                            <input
                                className="input-field"
                                type="password"
                                placeholder="비밀번호"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                style={{ background: 'var(--field-bg)', height: '48px', color: 'var(--on-surface)' }}
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={isLoading || !email || !password}
                            style={{
                                height: '48px',
                                width: '100%',
                                fontSize: '15px',
                                borderRadius: '8px',
                                fontWeight: 'bold',
                                backgroundColor: 'var(--primary)',
                                opacity: (isLoading || !email || !password) ? 0.7 : 1
                            }}
                        >
                            {isLoading ? '로그인 중...' : '로그인'}
                        </button>
                    </form>

                    <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', marginTop: '20px', fontSize: '13px', color: 'var(--on-surface-variant)' }}>
                        <span onClick={() => { }} style={{ cursor: 'pointer' }}>아이디/비밀번호 찾기</span>
                        <span>|</span>
                        <span onClick={() => navigate('/join')} style={{ cursor: 'pointer', fontWeight: 'bold', color: 'var(--on-surface)' }}>회원가입</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
