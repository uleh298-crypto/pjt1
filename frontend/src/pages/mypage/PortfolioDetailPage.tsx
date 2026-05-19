import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdArrowBack, MdEdit, MdCode, MdStar, MdEmojiEvents } from 'react-icons/md';
import { portfolioService, type PortfolioResponse, memberService } from '../../services/api';

export default function PortfolioDetailPage() {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null);
    const [isMine, setIsMine] = useState(false);

    useEffect(() => {
        fetchPortfolio();
    }, [id]);

    const fetchPortfolio = async () => {
        try {
            setLoading(true);
            let data: PortfolioResponse | null = null;

            if (id) {
                // 특정 ID의 포트폴리오 조회
                data = await portfolioService.getPortfolio(Number(id));

                // 본인 포트폴리오인지 확인
                try {
                    const myInfo = await memberService.getMyInfo();
                    if (data && myInfo && data.memberId === myInfo.id) {
                        setIsMine(true);
                    } else {
                        setIsMine(false);
                    }
                } catch (e) {
                    setIsMine(false);
                }
            } else {
                // 내 포트폴리오 조회
                const myPortfolios = await portfolioService.getMyPortfolios();
                if (myPortfolios && myPortfolios.length > 0) {
                    data = myPortfolios[0];
                    setIsMine(true);
                }
            }

            setPortfolio(data);
        } catch (err) {
            console.error('Failed to fetch portfolio:', err);
            setError('포트폴리오를 불러오는 데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
                <div className="loading-spinner" />
            </div>
        );
    }

    if (error) {
        return (
            <div style={{ textAlign: 'center', padding: '100px 20px' }}>
                <p style={{ color: 'var(--error)', fontSize: '18px', fontWeight: 600 }}>{error}</p>
                <button onClick={() => navigate(-1)} style={secondaryBtnStyle}>뒤로 가기</button>
            </div>
        );
    }

    if (!portfolio && !loading) {
        return (
            <div style={{ maxWidth: '800px', margin: '80px auto', padding: '40px', textAlign: 'center', backgroundColor: 'var(--surface)', borderRadius: '32px', boxShadow: '0 8px 32px rgba(0,0,0,0.05)' }}>
                <div style={{ fontSize: '64px', marginBottom: '24px' }}>📄</div>
                <h2 style={{ fontSize: '24px', fontWeight: 800, marginBottom: '16px', color: 'var(--on-surface)' }}>
                    {id ? '포트폴리오를 찾을 수 없습니다' : '아직 등록된 포트폴리오가 없습니다'}
                </h2>
                {!id && (
                    <button
                        onClick={() => navigate('/portfolio/new')}
                        style={primaryBtnStyle}
                    >
                        포트폴리오 만들기
                    </button>
                )}
                {id && <button onClick={() => navigate(-1)} style={secondaryBtnStyle}>뒤로 가기</button>}
            </div>
        );
    }

    const toLevelLabel = (level: string): string => {
        switch (level?.trim().toLowerCase()) {
            case 'high': return '상';
            case 'mid': return '중';
            case 'low': return '하';
            default: return '-';
        }
    };

    return (
        <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '40px 24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
                <button onClick={() => navigate(-1)} style={secondaryBtnStyle}>
                    <MdArrowBack size={20} /> 뒤로 가기
                </button>
                {isMine && (
                    <button
                        onClick={() => portfolio && navigate(`/portfolio/edit/${portfolio.id}`)}
                        style={primaryBtnStyle}
                    >
                        <MdEdit size={18} /> 편집하기
                    </button>
                )}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
                <div style={cardStyle}>
                    <h1 style={{ fontSize: '32px', fontWeight: 900, marginBottom: '12px', color: 'var(--on-surface)' }}>
                        {portfolio?.title}
                        {!portfolio?.isVisible && <span style={{ marginLeft: '12px', fontSize: '14px', verticalAlign: 'middle', color: 'var(--on-surface-variant)' }}>(비공개)</span>}
                    </h1>
                    <div style={{ fontSize: '18px', color: 'var(--primary)', fontWeight: 600, marginBottom: '24px' }}>
                        {portfolio?.description}
                    </div>
                    <div style={{ padding: '24px', backgroundColor: 'var(--field-bg)', borderRadius: '20px', lineHeight: 1.8, color: 'var(--on-surface)', whiteSpace: 'pre-wrap' }}>
                        {portfolio?.introduction || '자기소개가 아직 없습니다.'}
                    </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '32px' }}>
                    <div style={cardStyle}>
                        <div style={sectionHeaderStyle}><MdCode size={24} color="var(--primary)" /><h3 style={sectionTitleStyle}>기술 스택</h3></div>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                            {portfolio?.stacks?.map((stack, idx) => (
                                <div key={idx} style={tagStyle}>
                                    <span style={{ color: 'var(--primary)', fontWeight: 800, fontSize: '12px' }}>{toLevelLabel(stack.proficiency)}</span>
                                    {stack.stackName}
                                </div>
                            ))}
                        </div>
                    </div>

                    <div style={cardStyle}>
                        <div style={sectionHeaderStyle}><MdEmojiEvents size={24} color="var(--primary)" /><h3 style={sectionTitleStyle}>나의 성취</h3></div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                            <div style={statBoxStyle}>
                                <div style={statLabelStyle}>SSAFY SW 역량</div>
                                <div style={statValueStyle}>{portfolio?.swTestRank || '-'}</div>
                            </div>
                            <div style={statBoxStyle}>
                                <div style={statLabelStyle}>Solved.ac 랭크</div>
                                <div style={statValueStyle}>{portfolio?.solvedAcInfo?.rank || '-'}</div>
                            </div>
                        </div>
                    </div>

                    <div style={cardStyle}>
                        <div style={sectionHeaderStyle}><MdStar size={24} color="var(--primary)" /><h3 style={sectionTitleStyle}>프로젝트 경험</h3></div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {portfolio?.projects?.map((project, idx) => (
                                <div key={idx} style={tagStyle}>
                                    <span style={{ color: 'var(--primary)', opacity: 0.5 }}>#</span>
                                    {project.projectTitle}
                                </div>
                            ))}
                            {(!portfolio?.projects || portfolio.projects.length === 0) && (
                                <div style={{ color: 'var(--on-surface-variant)', fontSize: '14px', fontStyle: 'italic' }}>연동된 프로젝트가 없습니다.</div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

const cardStyle: React.CSSProperties = { backgroundColor: 'var(--surface)', borderRadius: '28px', padding: '32px', border: '1px solid var(--border-color)', boxShadow: '0 4px 20px rgba(0,0,0,0.04)' };
const sectionHeaderStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' };
const sectionTitleStyle: React.CSSProperties = { fontSize: '18px', fontWeight: 800 };
const tagStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 16px', backgroundColor: 'var(--field-bg)', borderRadius: '12px', fontSize: '14px', fontWeight: 500 };
const statBoxStyle: React.CSSProperties = { padding: '20px', backgroundColor: 'var(--field-bg)', borderRadius: '16px', display: 'flex', flexDirection: 'column', alignItems: 'center' };
const statLabelStyle: React.CSSProperties = { fontSize: '13px', color: 'var(--on-surface-variant)', fontWeight: 600 };
const statValueStyle: React.CSSProperties = { fontSize: '20px', color: 'var(--on-surface)', fontWeight: 800 };
const primaryBtnStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 24px', borderRadius: '16px', backgroundColor: 'var(--primary)', color: 'white', border: 'none', fontWeight: 700, cursor: 'pointer' };
const secondaryBtnStyle: React.CSSProperties = { display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 16px', borderRadius: '12px', backgroundColor: 'var(--field-bg)', border: 'none', color: 'var(--on-surface)', fontWeight: 600, cursor: 'pointer' };
