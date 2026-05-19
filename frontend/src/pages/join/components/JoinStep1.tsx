import React, { useEffect, useState } from 'react';
import { campusService } from '../../../services/api';
import type { Campus, Ban } from '../../../services/api';

interface JoinStep1Props {
    name: string;
    onNameChange: (val: string) => void;
    studentId: string;
    onStudentIdChange: (val: string) => void;
    generation: number | null;
    onGenerationChange: (val: number) => void;
    campus: Campus | null;
    onCampusChange: (val: Campus) => void;
    ban: Ban | null;
    onBanChange: (val: Ban) => void;
    onNext: () => void;
}

const GENERATIONS = [11, 12, 13, 14, 15]; // Example generations

const JoinStep1: React.FC<JoinStep1Props> = ({
    name, onNameChange, studentId, onStudentIdChange,
    generation, onGenerationChange, campus, onCampusChange, ban, onBanChange, onNext
}) => {
    const [campuses, setCampuses] = useState<Campus[]>([]);
    const [classes, setClasses] = useState<Ban[]>([]);
    const [loadingCampuses, setLoadingCampuses] = useState(false);
    const [loadingClasses, setLoadingClasses] = useState(false);

    useEffect(() => {
        const loadCampuses = async () => {
            setLoadingCampuses(true);
            try {
                const data = await campusService.getCampuses();
                setCampuses(data);
            } catch (error) {
                console.error("Failed to load campuses", error);
            } finally {
                setLoadingCampuses(false);
            }
        };
        loadCampuses();
    }, []);

    useEffect(() => {
        if (campus) {
            const loadClasses = async () => {
                setLoadingClasses(true);
                try {
                    const data = await campusService.getClasses(campus.id);
                    setClasses(data);
                } catch (error) {
                    console.error("Failed to load classes", error);
                } finally {
                    setLoadingClasses(false);
                }
            };
            loadClasses();
        } else {
            setClasses([]);
        }
    }, [campus]);

    const filteredClasses = classes.filter(c => generation ? c.generation === generation : true);

    const isValid = name && studentId && generation && campus && ban;

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

            <h2 style={{ textAlign: 'center', fontSize: '24px', fontWeight: 'bold', marginBottom: '4px' }}>
                SSAFY MatterMost 인증
            </h2>
            <p style={{ textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '24px' }}>
                싸브리타임 이용을 위해 개인정보를 입력해주세요.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>이름</label>
                    <input
                        className="input-field"
                        placeholder="이름"
                        value={name}
                        onChange={e => onNameChange(e.target.value)}
                    />
                </div>

                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>학번</label>
                    <input
                        className="input-field"
                        placeholder="숫자만 입력"
                        type="number"
                        value={studentId}
                        onChange={e => onStudentIdChange(e.target.value)}
                    />
                </div>

                {/* Generation Select */}
                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>기수</label>
                    <select
                        className="input-field"
                        value={generation || ''}
                        onChange={e => onGenerationChange(Number(e.target.value))}
                    >
                        <option value="">기수를 선택</option>
                        {GENERATIONS.map(g => (
                            <option key={g} value={g}>{g}기</option>
                        ))}
                    </select>
                </div>

                {/* Campus Select */}
                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>캠퍼스</label>
                    <select
                        className="input-field"
                        value={campus?.id || ''}
                        onChange={e => {
                            const selected = campuses.find(c => c.id === Number(e.target.value));
                            if (selected) onCampusChange(selected);
                        }}
                        disabled={loadingCampuses}
                    >
                        <option value="">{loadingCampuses ? "로딩 중..." : "캠퍼스를 선택"}</option>
                        {campuses.map(c => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                    </select>
                </div>

                {/* Class Select */}
                <div>
                    <label style={{ fontSize: '14px', fontWeight: '600', display: 'block', marginBottom: '6px' }}>1학기 반</label>
                    <select
                        className="input-field"
                        value={ban?.id || ''}
                        onChange={e => {
                            const selected = classes.find(c => c.id === Number(e.target.value));
                            if (selected) onBanChange(selected);
                        }}
                        disabled={!campus || loadingClasses}
                    >
                        <option value="">
                            {!campus ? "캠퍼스를 먼저 선택하세요" :
                                loadingClasses ? "로딩 중..." : "반을 선택"}
                        </option>
                        {filteredClasses.map(b => (
                            <option key={b.id} value={b.id}>{b.classNo ? `${b.classNo}반` : b.name}</option>
                        ))}
                    </select>
                </div>
            </div>

            <div style={{ height: '24px' }}></div>

            <button
                className="btn btn-primary"
                style={{ width: '100%', height: '56px', borderRadius: '8px', opacity: isValid ? 1 : 0.4 }}
                disabled={!isValid}
                onClick={onNext}
            >
                다음
            </button>
        </div>
    );
};

export default JoinStep1;
