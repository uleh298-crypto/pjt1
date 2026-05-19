import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdArrowBack } from 'react-icons/md';
import { studyService, teamService, portfolioService } from '../../services/api';
import type { GroupApplicationRequest, PortfolioResponse } from '../../services/api';

interface GroupApplyPageProps {
    kind: 'study' | 'team';
}

const GroupApplyPage: React.FC<GroupApplyPageProps> = ({ kind }) => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [portfolios, setPortfolios] = useState<PortfolioResponse[]>([]);
    const [formData, setFormData] = useState<GroupApplicationRequest>({
        portfolioId: 0,
        title: '',
        message: '',
        position: ''
    });
    const [submitting, setSubmitting] = useState(false);

    const loadPortfolios = async () => {
        try {
            const result = await portfolioService.getMyPortfolios();
            setPortfolios(result);
            if (result.length > 0) {
                setFormData({ ...formData, portfolioId: result[0].id });
            }
        } catch (error) {
            console.error('포트폴리오 목록 로딩 실패:', error);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // 유효성 검사
        if (!formData.portfolioId) {
            alert('포트폴리오를 선택하세요.');
            return;
        }
        if (!formData.title.trim()) {
            alert('지원 제목을 입력하세요.');
            return;
        }
        if (!formData.position.trim()) {
            alert('포지션을 입력하세요.');
            return;
        }
        if (!formData.message.trim()) {
            alert('지원 메시지를 입력하세요.');
            return;
        }

        setSubmitting(true);
        try {
            if (kind === 'study') {
                await studyService.applyStudy(Number(id), formData);
            } else {
                await teamService.applyTeam(Number(id), formData);
            }

            alert('신청이 완료되었습니다!');
            navigate(`/groups/${kind}/${id}`);
        } catch (error) {
            console.error('신청 실패:', error);
            alert('신청에 실패했습니다.');
        } finally {
            setSubmitting(false);
        }
    };

    useEffect(() => {
        loadPortfolios();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--background)' }}>
            {/* Top Bar */}
            <div style={{
                padding: '16px 20px',
                borderBottom: '1px solid var(--border-color)',
                backgroundColor: 'var(--surface)',
                display: 'flex',
                alignItems: 'center',
                gap: '12px'
            }}>
                <MdArrowBack
                    size={24}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(-1)}
                />
                <h1 style={{ flex: 1, fontSize: '18px', fontWeight: 'bold', margin: 0 }}>
                    {kind === 'study' ? '스터디' : '프로젝트'} 신청
                </h1>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
                <div style={{ maxWidth: '600px', margin: '0 auto' }}>
                    {portfolios.length === 0 ? (
                        <div style={{
                            padding: '40px 20px',
                            textAlign: 'center',
                            color: 'var(--on-surface-variant)'
                        }}>
                            <p>포트폴리오가 없습니다.</p>
                            <p>먼저 포트폴리오를 작성해주세요.</p>
                            <button
                                type="button"
                                onClick={() => navigate('/portfolio/new')}
                                style={{
                                    marginTop: '20px',
                                    padding: '12px 24px',
                                    backgroundColor: 'var(--primary)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    cursor: 'pointer'
                                }}
                            >
                                포트폴리오 작성하기
                            </button>
                        </div>
                    ) : (
                        <>
                            {/* 포트폴리오 선택 */}
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                    포트폴리오 *
                                </label>
                                <select
                                    value={formData.portfolioId}
                                    onChange={(e) => setFormData({ ...formData, portfolioId: Number(e.target.value) })}
                                    style={{
                                        width: '100%',
                                        padding: '14px 16px',
                                        backgroundColor: 'var(--field-bg)',
                                        border: '1px solid var(--border-color)',
                                        borderRadius: '12px',
                                        fontSize: '15px',
                                        color: 'var(--on-surface)',
                                        outline: 'none',
                                        appearance: 'none',
                                        backgroundImage: 'url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2224%22%20height%3D%2224%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%23666%22%20stroke-width%3D%222%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22%3E%3Cpolyline%20points%3D%226%209%2012%2015%2018%209%22%3E%3C%2Fpolyline%3E%3C%2Fsvg%3E")',
                                        backgroundRepeat: 'no-repeat',
                                        backgroundPosition: 'right 16px center',
                                        backgroundSize: '16px'
                                    }}
                                >
                                    {portfolios.map((portfolio) => (
                                        <option key={portfolio.id} value={portfolio.id}>
                                            {portfolio.title}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* 지원 제목 */}
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                    지원 제목 *
                                </label>
                                <input
                                    type="text"
                                    value={formData.title}
                                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                    placeholder="지원 제목을 입력하세요"
                                    style={{
                                        width: '100%',
                                        padding: '14px 16px',
                                        backgroundColor: 'var(--field-bg)',
                                        border: '1px solid var(--border-color)',
                                        borderRadius: '12px',
                                        fontSize: '15px',
                                        color: 'var(--on-surface)',
                                        outline: 'none',
                                        transition: 'border-color 0.2s'
                                    }}
                                    onFocus={(e) => e.currentTarget.style.borderColor = 'var(--primary)'}
                                    onBlur={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
                                />
                            </div>

                            {/* 포지션 */}
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                    포지션 *
                                </label>
                                <input
                                    type="text"
                                    value={formData.position}
                                    onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                                    placeholder="예: Frontend Developer, Backend Developer"
                                    style={{
                                        width: '100%',
                                        padding: '14px 16px',
                                        backgroundColor: 'var(--field-bg)',
                                        border: '1px solid var(--border-color)',
                                        borderRadius: '12px',
                                        fontSize: '15px',
                                        color: 'var(--on-surface)',
                                        outline: 'none',
                                        transition: 'border-color 0.2s'
                                    }}
                                    onFocus={(e) => e.currentTarget.style.borderColor = 'var(--primary)'}
                                    onBlur={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
                                />
                            </div>

                            {/* 지원 메시지 */}
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                                    지원 메시지 *
                                </label>
                                <textarea
                                    value={formData.message}
                                    onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                                    placeholder="지원 동기 및 역량을 작성해주세요"
                                    rows={10}
                                    style={{
                                        width: '100%',
                                        padding: '14px 16px',
                                        backgroundColor: 'var(--field-bg)',
                                        border: '1px solid var(--border-color)',
                                        borderRadius: '12px',
                                        fontSize: '15px',
                                        color: 'var(--on-surface)',
                                        outline: 'none',
                                        resize: 'vertical',
                                        lineHeight: '1.6',
                                        transition: 'border-color 0.2s'
                                    }}
                                    onFocus={(e) => e.currentTarget.style.borderColor = 'var(--primary)'}
                                    onBlur={(e) => e.currentTarget.style.borderColor = 'var(--border-color)'}
                                />
                            </div>
                        </>
                    )}
                </div>
            </form>

            {/* 하단 버튼 */}
            {portfolios.length > 0 && (
                <div style={{
                    padding: '16px 20px',
                    borderTop: '1px solid var(--border-color)',
                    backgroundColor: 'var(--surface)',
                    display: 'flex',
                    gap: '12px'
                }}>
                    <button
                        type="button"
                        onClick={() => navigate(-1)}
                        style={{
                            flex: 1,
                            padding: '14px',
                            backgroundColor: 'transparent',
                            color: 'var(--on-surface)',
                            border: '1px solid var(--border-color)',
                            borderRadius: '8px',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        onClick={handleSubmit}
                        disabled={submitting}
                        style={{
                            flex: 1,
                            padding: '14px',
                            backgroundColor: 'var(--primary)',
                            color: 'white',
                            border: 'none',
                            borderRadius: '8px',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            cursor: submitting ? 'not-allowed' : 'pointer',
                            opacity: submitting ? 0.6 : 1
                        }}
                    >
                        {submitting ? '신청 중...' : '신청'}
                    </button>
                </div>
            )}
        </div>
    );
};

export default GroupApplyPage;
