import type { MyPagePortfolioSummary } from '../../services/api';

interface PortfolioCardProps {
    summary: MyPagePortfolioSummary | null;
    onDetailClick: () => void;
}

export default function PortfolioCard({ summary, onDetailClick }: PortfolioCardProps) {
    const techStacks = summary?.techStack ? Object.entries(summary.techStack) : [];
    const swRating = summary?.ssafySwRating || '-';
    const solvedAcRank = summary?.solvedAcRank || '-';
    const links = summary?.links || [];
    const projects = summary?.projects || [];

    const toLevelLabel = (level: string): string => {
        switch (level?.trim().toLowerCase()) {
            case 'high': return '상';
            case 'mid': return '중';
            case 'low': return '하';
            default: return '-';
        }
    };

    return (
        <div style={{
            width: '100%',
            borderRadius: '24px',
            backgroundColor: 'var(--surface)',
            boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
            padding: '32px',
            border: '1px solid var(--border-color)'
        }}>
            {/* Header */}
            <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '32px'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{ fontSize: '24px' }}>📄</div>
                    <div style={{
                        fontSize: '22px',
                        fontWeight: 'bold',
                        color: 'var(--on-surface)',
                        letterSpacing: '-0.5px'
                    }}>
                        포트폴리오 요약
                    </div>
                </div>
                <button
                    onClick={onDetailClick}
                    style={{
                        padding: '10px 20px',
                        borderRadius: '12px',
                        backgroundColor: 'var(--primary)',
                        color: 'white',
                        border: 'none',
                        cursor: 'pointer',
                        fontSize: '14px',
                        fontWeight: 600,
                        transition: 'transform 0.2s, background-color 0.2s'
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = '#4e7ef5';
                        e.currentTarget.style.transform = 'scale(1.05)';
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'var(--primary)';
                        e.currentTarget.style.transform = 'scale(1)';
                    }}
                >
                    상세보기
                </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '32px' }}>
                {/* Left Column */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    {/* Tech Stack Section */}
                    <Section icon="💻" label="기술 스택">
                        {techStacks.length === 0 ? (
                            <div style={{ fontSize: '13px', color: 'var(--on-surface-variant)', fontStyle: 'italic' }}>
                                등록된 기술이 없습니다.
                            </div>
                        ) : (
                            <div style={{
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: '8px'
                            }}>
                                {techStacks.map(([name, level]) => (
                                    <div
                                        key={name}
                                        style={{
                                            backgroundColor: 'var(--field-bg)',
                                            borderRadius: '8px',
                                            padding: '6px 12px',
                                            fontSize: '13px',
                                            fontWeight: 500,
                                            color: 'var(--on-surface)',
                                            border: '1px solid var(--border-color)'
                                        }}
                                    >
                                        <span style={{ color: 'var(--primary)', fontWeight: 700, marginRight: '4px' }}>{toLevelLabel(level)}</span>
                                        {name}
                                    </div>
                                ))}
                            </div>
                        )}
                    </Section>

                    {/* SW Rating & Solved.ac */}
                    <div style={{ display: 'flex', gap: '24px' }}>
                        <Section icon="🧠" label="SW 역량" value={swRating} />
                        <Section icon="🏆" label="Solved.ac" value={solvedAcRank} />
                    </div>
                </div>

                {/* Right Column */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    {/* Projects Section */}
                    <Section icon="💼" label="프로젝트 경험">
                        {projects.length === 0 ? (
                            <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--on-surface-variant)' }}>-</div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                {projects.map((title, idx) => (
                                    <div key={idx} style={{
                                        fontSize: '14px',
                                        color: 'var(--on-surface)',
                                        display: 'flex',
                                        gap: '8px'
                                    }}>
                                        <span style={{ color: 'var(--primary)', fontWeight: 700 }}>{idx + 1}.</span>
                                        {title}
                                    </div>
                                ))}
                            </div>
                        )}
                    </Section>

                    {/* Links Section */}
                    <Section icon="🔗" label="관련 링크">
                        {links.length === 0 ? (
                            <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--on-surface-variant)' }}>-</div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                {links.map((url, idx) => (
                                    <div key={idx} style={{
                                        fontSize: '13px',
                                        color: 'var(--primary)',
                                        wordBreak: 'break-all',
                                        textDecoration: 'underline',
                                        cursor: 'pointer'
                                    }}>
                                        {url}
                                    </div>
                                ))}
                            </div>
                        )}
                    </Section>
                </div>
            </div>
        </div>
    );
}

interface SectionProps {
    icon: string;
    label: string;
    value?: string;
    children?: React.ReactNode;
}

function Section({ icon, label, value, children }: SectionProps) {
    return (
        <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
            <div style={{
                fontSize: '24px',
                flexShrink: 0,
                width: '44px',
                height: '44px',
                backgroundColor: 'var(--field-bg)',
                borderRadius: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>{icon}</div>
            <div style={{ flex: 1 }}>
                <div style={{
                    fontSize: '13px',
                    color: 'var(--on-surface-variant)',
                    fontWeight: 600,
                    marginBottom: '6px'
                }}>
                    {label}
                </div>
                {value && (
                    <div style={{
                        fontSize: '18px',
                        color: 'var(--on-surface)',
                        fontWeight: 800
                    }}>
                        {value}
                    </div>
                )}
                {children}
            </div>
        </div>
    );
}
