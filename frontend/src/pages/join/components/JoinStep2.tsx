import React, { useState } from 'react';
import { authService } from '../../../services/api';
import type { SsafyVerifyRequest, SsafyConfirmRequest } from '../../../services/api';

interface JoinStep2Props {
    mattermostId: string;
    onMattermostIdChange: (val: string) => void;
    generation: number;
    name: string;
    onNext: () => void;
}

const JoinStep2: React.FC<JoinStep2Props> = ({
    mattermostId, onMattermostIdChange, generation, name, onNext
}) => {
    const [verificationCode, setVerificationCode] = useState('');
    const [isCodeSent, setIsCodeSent] = useState(false);
    const [isSending, setIsSending] = useState(false);
    const [isVerifying, setIsVerifying] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSendCode = async () => {
        if (!mattermostId) return;
        setIsSending(true);
        setError(null);
        try {
            const req: SsafyVerifyRequest = { targetUserId: mattermostId, generation, name };
            await authService.requestSsafyVerification(req);
            setIsCodeSent(true);
        } catch (err: any) {
            setError(err.message || "인증번호 발송 실패");
        } finally {
            setIsSending(false);
        }
    };

    const handleVerify = async () => {
        if (verificationCode.length !== 6) return;
        setIsVerifying(true);
        setError(null);
        try {
            const req: SsafyConfirmRequest = { targetUserId: mattermostId, authCode: verificationCode };
            await authService.confirmSsafyVerification(req);
            onNext(); // Move to next step on success
        } catch (err: any) {
            setError(err.message || "인증 실패");
        } finally {
            setIsVerifying(false);
        }
    };

    return (
        <div style={{ padding: '0 24px' }}>
            {/* Logo & Title Same as Step 1 (Reusable Header ideally) */}
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '16px' }}>
                <div style={{
                    width: '80px', height: '80px', background: 'var(--surface)',
                    borderRadius: '18px', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }}>
                    <img src="/vite.svg" alt="App Logo" style={{ width: '40px' }} />
                </div>
            </div>
            <h2 style={{ textAlign: 'center', fontSize: '24px', fontWeight: 'bold' }}>SSAFY MatterMost 인증</h2>
            <p style={{ textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '32px' }}>
                싸브리타임 이용을 위해 개인정보를 입력해주세요.
            </p>

            <div style={{ marginBottom: '16px' }}>
                <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>MatterMost 아이디</label>
                <input
                    className="input-field"
                    placeholder="example"
                    value={mattermostId}
                    onChange={e => onMattermostIdChange(e.target.value)}
                    disabled={isCodeSent}
                />
            </div>

            <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                선택한 기수: {generation}기
            </div>

            {!isCodeSent && (
                <button
                    className="btn btn-primary"
                    style={{ width: '100%', height: '56px', borderRadius: '8px' }}
                    onClick={handleSendCode}
                    disabled={isSending || !mattermostId}
                >
                    {isSending ? "발송 중..." : "인증번호 발송"}
                </button>
            )}

            {isCodeSent && (
                <>
                    <div style={{ marginBottom: '8px' }}>
                        <label style={{ fontSize: '13px', color: 'var(--text-primary)' }}>인증번호 6자리</label>
                        <input
                            className="input-field"
                            placeholder="123456"
                            value={verificationCode}
                            onChange={e => setVerificationCode(e.target.value.replace(/[^0-9]/g, '').slice(0, 6))}
                            style={{ marginTop: '8px' }}
                        />
                    </div>

                    <div style={{ height: '32px' }}></div>

                    <button
                        className="btn btn-primary"
                        style={{ width: '100%', height: '56px', borderRadius: '8px' }}
                        onClick={handleVerify}
                        disabled={isVerifying || verificationCode.length !== 6}
                    >
                        {isVerifying ? "인증 중..." : "인증 완료"}
                    </button>
                </>
            )}

            {error && <div style={{ color: 'var(--error)', fontSize: '12px', marginTop: '12px' }}>{error}</div>}

            <div style={{ height: '24px' }}></div>
        </div>
    );
};

export default JoinStep2;
