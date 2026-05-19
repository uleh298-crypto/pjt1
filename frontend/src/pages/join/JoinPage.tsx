import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import JoinStep1 from './components/JoinStep1';
import JoinStep2 from './components/JoinStep2';
import JoinStep3 from './components/JoinStep3';
import type { Campus, Ban } from '../../services/api';

const JoinPage: React.FC = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);

    // Step 1 State
    const [name, setName] = useState('');
    const [studentId, setStudentId] = useState('');
    const [generation, setGeneration] = useState<number | null>(null);
    const [campus, setCampus] = useState<Campus | null>(null);
    const [ban, setBan] = useState<Ban | null>(null);

    // Step 2 State
    const [mattermostId, setMattermostId] = useState('');

    // Step 3 State
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');

    const handleBack = () => {
        if (step > 1) {
            setStep(step - 1);
        } else {
            navigate(-1);
        }
    };

    const handleJoinSuccess = () => {
        alert("회원가입이 완료되었습니다. 다시 로그인해 주세요.");
        navigate('/login');
    };

    return (
        <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            {/* Top Bar (Simplified) */}
            <div style={{ padding: '16px', display: 'flex', alignItems: 'center' }}>
                <button onClick={handleBack} style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer', color: 'var(--on-surface)' }}>
                    &larr;
                </button>
            </div>

            <div style={{ flex: 1, overflowY: 'auto', paddingBottom: '24px' }}>
                {step === 1 && (
                    <JoinStep1
                        name={name} onNameChange={setName}
                        studentId={studentId} onStudentIdChange={setStudentId}
                        generation={generation} onGenerationChange={setGeneration}
                        campus={campus} onCampusChange={setCampus}
                        ban={ban} onBanChange={setBan}
                        onNext={() => setStep(2)}
                    />
                )}
                {step === 2 && (
                    <JoinStep2
                        mattermostId={mattermostId} onMattermostIdChange={setMattermostId}
                        generation={generation!}
                        name={name}
                        onNext={() => setStep(3)}
                    />
                )}
                {step === 3 && (
                    <JoinStep3
                        email={email} onEmailChange={setEmail}
                        password={password} onPasswordChange={setPassword}
                        passwordConfirm={passwordConfirm} onPasswordConfirmChange={setPasswordConfirm}
                        name={name}
                        studentNo={Number(studentId)}
                        campusId={campus!.id}
                        generation={generation!}
                        classNo={ban!.classNo || 0} // Using 0 or extract classNo likely from Ban object
                        mattermostId={mattermostId}
                        onJoinSuccess={handleJoinSuccess}
                    />
                )}
            </div>
        </div>
    );
};

export default JoinPage;
