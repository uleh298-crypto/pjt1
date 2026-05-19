import React, { useState } from 'react';
import { authService } from '../../../services/api';
import type { SignUpRequest } from '../../../services/api'; // Ban imported for type ref if needed but not used explicitly here as passed in props

interface JoinStep3Props {
    email: string;
    onEmailChange: (val: string) => void;
    password: string;
    onPasswordChange: (val: string) => void;
    passwordConfirm: string;
    onPasswordConfirmChange: (val: string) => void;
    // Data for final submit
    name: string;
    studentNo: number;
    campusId: number;
    generation: number;
    classNo: number;
    mattermostId: string;

    onJoinSuccess: () => void;
}

const JoinStep3: React.FC<JoinStep3Props> = ({
    email, onEmailChange, password, onPasswordChange, passwordConfirm, onPasswordConfirmChange,
    name, studentNo, campusId, generation, classNo, mattermostId, onJoinSuccess
}) => {
    const [isCheckingEmail, setIsCheckingEmail] = useState(false);
    const [isEmailAvailable, setIsEmailAvailable] = useState<boolean | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleCheckEmail = async () => {
        if (!email) return;
        setIsCheckingEmail(true);
        try {
            const available = await authService.checkEmailAvailable(email);
            setIsEmailAvailable(available);
        } catch (err) {
            console.error(err);
            setIsEmailAvailable(null); // or false?
        } finally {
            setIsCheckingEmail(false);
        }
    };

    const handleJoin = async () => {
        if (!isEmailAvailable) {
            setError("이메일 중복 확인을 해주세요.");
            return;
        }
        if (password !== passwordConfirm) {
            setError("비밀번호가 일치하지 않습니다.");
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            const req: SignUpRequest = {
                email,
                password,
                name,
                studentNo,
                campus: campusId,
                generation,
                classNo,
                mattermostId
            };
            await authService.signUp(req);
            onJoinSuccess();
        } catch (err: any) {
            setError(err.message || "회원가입 실패");
        } finally {
            setIsSubmitting(false);
        }
    };

    const isValid = email && isEmailAvailable && password && passwordConfirm && (password === passwordConfirm);

    return (
        <div style={{ padding: '0 24px' }}>
            {/* Logo */}
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

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px' }}>
                    <div style={{ flex: 1 }}>
                        <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>이메일</label>
                        <input
                            className="input-field"
                            placeholder="user@example.com"
                            type="email"
                            value={email}
                            onChange={e => {
                                onEmailChange(e.target.value);
                                setIsEmailAvailable(null);
                            }}
                        />
                    </div>
                    <button
                        className="btn btn-outline"
                        style={{ height: '52px', borderRadius: '12px', fontSize: '13px', whiteSpace: 'nowrap' }}
                        onClick={handleCheckEmail}
                        disabled={isCheckingEmail || !email}
                    >
                        {isCheckingEmail ? '...' : '중복확인'}
                    </button>
                </div>
                {isEmailAvailable === true && <div style={{ fontSize: '11px', color: '#4CAF50' }}>사용 가능한 이메일입니다.</div>}
                {isEmailAvailable === false && <div style={{ fontSize: '11px', color: 'var(--error)' }}>이미 사용 중인 이메일입니다.</div>}

                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>비밀번호</label>
                    <input
                        className="input-field"
                        placeholder="비밀번호"
                        type="password"
                        value={password}
                        onChange={e => onPasswordChange(e.target.value)}
                    />
                </div>

                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>비밀번호 확인</label>
                    <input
                        className="input-field"
                        placeholder="비밀번호 확인"
                        type="password"
                        value={passwordConfirm}
                        onChange={e => onPasswordConfirmChange(e.target.value)}
                    />
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                    8자 이상, 영문, 숫자, 특수문자 포함
                </div>
                {password !== passwordConfirm && passwordConfirm && (
                    <div style={{ fontSize: '11px', color: 'var(--error)' }}>비밀번호가 일치하지 않습니다.</div>
                )}
            </div>

            <div style={{ height: '24px' }}></div>

            {error && <div style={{ color: 'var(--error)', fontSize: '12px', marginBottom: '16px' }}>{error}</div>}

            <button
                className="btn btn-primary"
                style={{ width: '100%', height: '56px', borderRadius: '8px', opacity: isValid ? 1 : 0.4 }}
                disabled={!isValid || isSubmitting}
                onClick={handleJoin}
            >
                {isSubmitting ? "가입 중..." : "가입 완료"}
            </button>

            <div style={{ height: '24px' }}></div>
        </div>
    );
};

export default JoinStep3;
