import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdArrowBack, MdSave, MdAdd, MdDelete, MdLink, MdSearch, MdCheckCircle, MdError, MdVisibility, MdVisibilityOff } from 'react-icons/md';
import { portfolioService, stackService, type GlobalStack, type PortfolioCreateRequest, type PortfolioUpdateRequest } from '../../services/api';

interface PortfolioWritePageProps {
    mode: 'create' | 'edit';
}

export default function PortfolioWritePage({ mode }: PortfolioWritePageProps) {
    const { id } = useParams();
    const navigate = useNavigate();

    const getTierName = (tier: number) => {
        if (tier === 0) return 'Unrated';
        if (tier === 31) return 'Master';
        const tierNames = ['Bronze', 'Silver', 'Gold', 'Platinum', 'Diamond', 'Ruby'];
        const tierIndex = Math.floor((tier - 1) / 5);
        const tierLevel = 5 - ((tier - 1) % 5);
        return `${tierNames[tierIndex]} ${tierLevel}`;
    };

    const [loading, setLoading] = useState(mode === 'edit');
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Form States
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [introduction, setIntroduction] = useState('');
    const [stacks, setStacks] = useState<{ stackId: number; stackName: string; expertLevel: string }[]>([]);
    const [swTestRank, setSwTestRank] = useState('');
    const [bojHandle, setBojHandle] = useState('');
    const [urls, setUrls] = useState<string[]>([]);
    const [isVisible, setIsVisible] = useState(true);

    // Solved.ac Verification States
    const [isVerifying, setIsVerifying] = useState(false);
    const [verifyResult, setVerifyResult] = useState<{ rank: string; tier: number } | null>(null);
    const [verifyError, setVerifyError] = useState<string | null>(null);

    // Stack Search States
    const [allGlobalStacks, setAllGlobalStacks] = useState<GlobalStack[]>([]);
    const [stackSearchQuery, setStackSearchQuery] = useState('');
    const [showStackResults, setShowStackResults] = useState(false);

    useEffect(() => {
        fetchInitialData();
    }, []);

    const fetchInitialData = async () => {
        try {
            const stacksData = await stackService.getAllStacks();
            setAllGlobalStacks(stacksData);

            if (mode === 'edit' && id) {
                const p = await portfolioService.getPortfolio(Number(id));
                setTitle(p.title);
                setDescription(p.description || '');
                setIntroduction(p.introduction || '');
                // Map existing stacks
                setStacks((p.stacks || []).map(s => ({
                    stackId: s.id,
                    stackName: s.stackName,
                    expertLevel: s.proficiency || 'MID'
                })));
                setSwTestRank(p.swTestRank || '');
                setBojHandle(p.solvedAcInfo?.bojHandle || '');
                if (p.solvedAcInfo) {
                    setVerifyResult({ rank: p.solvedAcInfo.rank, tier: 0 });
                }
                setUrls((p.urls || []).map(u => u.url));
                setIsVisible(p.isVisible);
            }
        } catch (err) {
            console.error('Failed to fetch initial data:', err);
            setError('데이터를 불러오는 데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifySolvedac = async () => {
        if (!bojHandle.trim()) return;
        try {
            setIsVerifying(true);
            setVerifyError(null);
            const result = await portfolioService.verifySolvedac(bojHandle.trim());
            setVerifyResult({ rank: getTierName(result.tier), tier: result.tier });
        } catch (err: any) {
            setVerifyError(err.message || '인증에 실패했습니다.');
            setVerifyResult(null);
        } finally {
            setIsVerifying(false);
        }
    };

    const handleSave = async () => {
        if (!title.trim()) {
            alert('제목을 입력해주세요.');
            return;
        }

        try {
            setSaving(true);
            const data: PortfolioCreateRequest = {
                title,
                description,
                introduction,
                bojHandle: bojHandle.trim(),
                swTestRank,
                isVisible,
                stacks: (stacks || []).map(s => ({ stackId: s.stackId, expertLevel: s.expertLevel })),
                urls: (urls || []).map(u => ({ url: u })),
                images: [] // Simple empty list for now
            };

            if (mode === 'edit' && id) {
                await portfolioService.updatePortfolio(Number(id), data as PortfolioUpdateRequest);
            } else {
                await portfolioService.createPortfolio(data);
            }
            navigate('/portfolio/detail');
        } catch (err) {
            console.error('Save failed:', err);
            alert('저장에 실패했습니다.');
        } finally {
            setSaving(false);
        }
    };

    const filteredStacks = useMemo(() => {
        if (!stackSearchQuery) return [];
        return allGlobalStacks.filter(s =>
            s.name.toLowerCase().includes(stackSearchQuery.toLowerCase()) &&
            !stacks.some(selected => selected.stackId === s.id)
        ).slice(0, 10);
    }, [stackSearchQuery, allGlobalStacks, stacks]);

    if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: '100px' }}><div className="loading-spinner" /></div>;
    if (error) return <div style={{ padding: '40px', textAlign: 'center', color: 'var(--error)' }}>{error}</div>;

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '40px 20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
                <button onClick={() => navigate(-1)} style={secondaryBtnStyle}>
                    <MdArrowBack size={20} /> 뒤로가기
                </button>
                <h2 style={{ fontSize: '20px', fontWeight: 800 }}>
                    {mode === 'edit' ? '포트폴리오 수정' : '새 포트폴리오 작성'}
                </h2>
                <button onClick={handleSave} disabled={saving} style={primaryBtnStyle}>
                    {saving ? <div className="loading-spinner-small" /> : <MdSave size={20} />}
                    저장하기
                </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
                <div style={formCardStyle}>
                    <h3 style={formSectionTitleStyle}>기본 정보</h3>
                    <div style={inputGroupStyle}>
                        <label style={labelStyle}>포트폴리오 제목 *</label>
                        <input
                            value={title}
                            onChange={e => setTitle(e.target.value)}
                            placeholder="예: 끈기 있는 백엔드 개발자"
                            style={inputStyle}
                        />
                    </div>
                    <div style={inputGroupStyle}>
                        <label style={labelStyle}>한 줄 소개</label>
                        <input
                            value={description}
                            onChange={e => setDescription(e.target.value)}
                            placeholder="자신을 한 줄로 표현해보세요"
                            style={inputStyle}
                        />
                    </div>
                    <div style={inputGroupStyle}>
                        <label style={labelStyle}>자기소개</label>
                        <textarea
                            value={introduction}
                            onChange={e => setIntroduction(e.target.value)}
                            placeholder="상세한 자기소개를 작성해주세요."
                            style={{ ...inputStyle, minHeight: '120px', resize: 'vertical' }}
                        />
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginTop: '8px' }}>
                        <span style={labelStyle}>포트폴리오 공개</span>
                        <button
                            onClick={() => setIsVisible(!isVisible)}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 16px',
                                borderRadius: '12px', border: 'none', cursor: 'pointer',
                                backgroundColor: isVisible ? 'rgba(100, 149, 235, 0.1)' : 'var(--field-bg)',
                                color: isVisible ? 'var(--primary)' : 'var(--on-surface-variant)',
                                fontWeight: 700
                            }}
                        >
                            {isVisible ? <MdVisibility size={20} /> : <MdVisibilityOff size={20} />}
                            {isVisible ? '공개 중' : '비공개'}
                        </button>
                    </div>
                </div>

                <div style={formCardStyle}>
                    <h3 style={formSectionTitleStyle}>기술 스택</h3>
                    <div style={{ position: 'relative', marginBottom: '16px' }}>
                        <div style={{ display: 'flex', gap: '8px' }}>
                            <div style={{ position: 'relative', flex: 1 }}>
                                <MdSearch size={20} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--on-surface-variant)' }} />
                                <input
                                    value={stackSearchQuery}
                                    onChange={e => {
                                        setStackSearchQuery(e.target.value);
                                        setShowStackResults(true);
                                    }}
                                    onFocus={() => setShowStackResults(true)}
                                    placeholder="기술 스택 검색 (예: React, Spring...)"
                                    style={{ ...inputStyle, paddingLeft: '40px' }}
                                />
                                {showStackResults && filteredStacks.length > 0 && (
                                    <div style={dropdownStyle}>
                                        {filteredStacks.map(s => (
                                            <div
                                                key={s.id}
                                                onClick={() => {
                                                    setStacks([...stacks, { stackId: s.id, stackName: s.name, expertLevel: 'MID' }]);
                                                    setStackSearchQuery('');
                                                    setShowStackResults(false);
                                                }}
                                                style={dropdownItemStyle}
                                            >
                                                {s.imgUrl && <img src={s.imgUrl} alt="" style={{ width: '20px', height: '20px', objectFit: 'contain' }} />}
                                                {s.name}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
                        {stacks.map((s, idx) => (
                            <div key={idx} style={stackChipStyle}>
                                <span style={{ fontWeight: 700 }}>{s.stackName}</span>
                                <select
                                    value={s.expertLevel}
                                    onChange={e => {
                                        const next = [...stacks];
                                        next[idx].expertLevel = e.target.value;
                                        setStacks(next);
                                    }}
                                    style={proficiencySelectStyle}
                                >
                                    <option value="HIGH">상</option>
                                    <option value="MID">중</option>
                                    <option value="LOW">하</option>
                                </select>
                                <button onClick={() => setStacks(stacks.filter((_, i) => i !== idx))} style={removeBtnStyle}>
                                    <MdDelete size={16} />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                <div style={formCardStyle}>
                    <h3 style={formSectionTitleStyle}>나의 성취</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div style={inputGroupStyle}>
                            <label style={labelStyle}>SSAFY SW 역량</label>
                            <select value={swTestRank} onChange={e => setSwTestRank(e.target.value)} style={inputStyle}>
                                <option value="">선택 안 함</option>
                                <option value="A">A등급</option>
                                <option value="A+">A+등급</option>
                                <option value="B">B등급</option>
                                <option value="C">C등급</option>
                                <option value="Pro">Professional</option>
                            </select>
                        </div>
                        <div style={inputGroupStyle}>
                            <label style={labelStyle}>백준 아이디 연동</label>
                            <div style={{ display: 'flex', gap: '8px' }}>
                                <input
                                    value={bojHandle}
                                    onChange={e => {
                                        setBojHandle(e.target.value);
                                        setVerifyResult(null);
                                    }}
                                    placeholder="Solved.ac ID"
                                    style={inputStyle}
                                />
                                <button onClick={handleVerifySolvedac} disabled={isVerifying || !bojHandle.trim()} style={{ ...secondaryBtnStyle, whiteSpace: 'nowrap' }}>
                                    {isVerifying ? '연동 중...' : '연동 확인'}
                                </button>
                            </div>
                            {verifyResult && <div style={{ marginTop: '8px', color: 'var(--primary)', fontSize: '13px' }}><MdCheckCircle /> {verifyResult.rank} 티어 인증됨</div>}
                            {verifyError && <div style={{ marginTop: '8px', color: 'var(--error)', fontSize: '13px' }}><MdError /> {verifyError}</div>}
                        </div>
                    </div>
                </div>

                <div style={formCardStyle}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                        <h3 style={formSectionTitleStyle}>관련 링크</h3>
                        <button onClick={() => setUrls([...urls, ''])} style={addIconBtnStyle}><MdAdd size={20} /></button>
                    </div>
                    {urls.map((u, idx) => (
                        <div key={idx} style={{ display: 'flex', gap: '8px', marginBottom: '8px' }}>
                            <div style={{ position: 'relative', flex: 1 }}>
                                <MdLink size={18} style={{ position: 'absolute', left: '12px', top: '13px', color: 'var(--primary)' }} />
                                <input
                                    value={u}
                                    onChange={e => {
                                        const next = [...urls];
                                        next[idx] = e.target.value;
                                        setUrls(next);
                                    }}
                                    placeholder="URL"
                                    style={{ ...inputStyle, paddingLeft: '36px' }}
                                />
                            </div>
                            <button onClick={() => setUrls(urls.filter((_, i) => i !== idx))} style={removeBtnStyle}><MdDelete size={20} /></button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

// Styles (Simplified for maintenance)
const formCardStyle: React.CSSProperties = { backgroundColor: 'var(--surface)', borderRadius: '24px', padding: '32px', border: '1px solid var(--border-color)' };
const formSectionTitleStyle: React.CSSProperties = { fontSize: '18px', fontWeight: 800, marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' };
const inputGroupStyle: React.CSSProperties = { display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '20px' };
const labelStyle: React.CSSProperties = { fontSize: '14px', fontWeight: 700, color: 'var(--on-surface-variant)' };
const inputStyle: React.CSSProperties = { width: '100%', padding: '12px 16px', borderRadius: '12px', backgroundColor: 'var(--field-bg)', border: 'none', fontSize: '15px', color: 'var(--on-surface)' };
const stackChipStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px', backgroundColor: 'var(--field-bg)', borderRadius: '12px', fontSize: '14px' };
const proficiencySelectStyle: React.CSSProperties = { border: 'none', background: 'transparent', color: 'var(--primary)', fontWeight: 800, outline: 'none' };
const primaryBtnStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 24px', borderRadius: '16px', backgroundColor: 'var(--primary)', color: 'white', border: 'none', fontWeight: 700, cursor: 'pointer' };
const secondaryBtnStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 20px', borderRadius: '16px', backgroundColor: 'var(--field-bg)', color: 'var(--on-surface)', border: 'none', fontWeight: 700, cursor: 'pointer' };
const removeBtnStyle: React.CSSProperties = { background: 'none', border: 'none', color: 'var(--error)', cursor: 'pointer' };
const addIconBtnStyle: React.CSSProperties = { width: '32px', height: '32px', borderRadius: '8px', border: 'none', backgroundColor: 'rgba(100, 149, 235, 0.1)', color: 'var(--primary)', cursor: 'pointer' };
const dropdownStyle: React.CSSProperties = { position: 'absolute', top: '100%', left: 0, right: 0, backgroundColor: 'var(--surface)', border: '1px solid var(--border-color)', borderRadius: '12px', zIndex: 100, marginTop: '4px', maxHeight: '200px', overflowY: 'auto' };
const dropdownItemStyle: React.CSSProperties = { padding: '12px 16px', cursor: 'pointer', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '10px' };
