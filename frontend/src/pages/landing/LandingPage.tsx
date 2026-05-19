import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdGroups, MdAssignment, MdRocketLaunch, MdChat, MdKeyboardArrowRight } from 'react-icons/md';

const LandingPage: React.FC = () => {
    const navigate = useNavigate();
    const [scrolled, setScrolled] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            setScrolled(window.scrollY > 50);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const features = [
        {
            icon: <MdGroups size={32} />,
            title: "팀 매칭 시스템",
            description: "함께 프로젝트를 진행할 최적의 동료를 찾아보세요. 스택과 성향을 고려한 팀 매칭을 지원합니다.",
            color: "#5B7FFF"
        },
        {
            icon: <MdAssignment size={32} />,
            title: "상세한 포트폴리오",
            description: "자신만의 강점을 담은 포트폴리오를 작성하고 공유하세요. 기업 관계자에게 어필할 수 있는 기회를 잡으세요.",
            color: "#FF5252"
        },
        {
            icon: <MdChat size={32} />,
            title: "실시간 소통",
            description: "스터디와 팀 멤버들과 실시간으로 소통하며 협업 효율을 극대화하세요.",
            color: "#4CAF50"
        },
        {
            icon: <MdRocketLaunch size={32} />,
            title: "성장하는 커뮤니티",
            description: "지식 공유와 네트워킹을 통해 함께 성장하는 경험을 시작하세요.",
            color: "#FFC107"
        }
    ];

    return (
        <div style={{
            minHeight: '100vh',
            backgroundColor: 'var(--background)',
            color: 'var(--on-surface)',
            fontFamily: "'Outfit', sans-serif",
            overflowX: 'hidden'
        }}>
            {/* Navigation */}
            <nav style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                padding: scrolled ? '16px 40px' : '24px 40px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                zIndex: 1000,
                transition: 'all 0.3s ease',
                backgroundColor: scrolled ? 'rgba(var(--surface-rgb), 0.8)' : 'transparent',
                backdropFilter: scrolled ? 'blur(10px)' : 'none',
                borderBottom: scrolled ? '1px solid var(--border-color)' : 'none'
            }}>
                <div style={{ fontSize: '24px', fontWeight: '900', color: 'var(--primary)', letterSpacing: '-1px' }}>
                    SSABRE
                </div>
                <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                    <button
                        onClick={() => navigate('/login')}
                        style={{
                            background: 'none',
                            border: 'none',
                            color: 'var(--on-surface)',
                            fontWeight: '600',
                            cursor: 'pointer',
                            fontSize: '15px',
                            padding: '10px 20px'
                        }}
                    >
                        로그인
                    </button>
                    <button
                        onClick={() => navigate('/join')}
                        style={{
                            backgroundColor: 'var(--primary)',
                            color: 'white',
                            border: 'none',
                            padding: '10px 24px',
                            borderRadius: '30px',
                            fontWeight: '700',
                            cursor: 'pointer',
                            fontSize: '15px',
                            boxShadow: '0 4px 15px rgba(91, 127, 255, 0.3)',
                            transition: 'transform 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
                        onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                    >
                        회원가입
                    </button>
                </div>
            </nav>

            {/* Hero Section */}
            <section style={{
                height: '100vh',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                textAlign: 'center',
                padding: '0 20px',
                position: 'relative',
                background: 'radial-gradient(circle at top right, rgba(91, 127, 255, 0.1), transparent), radial-gradient(circle at bottom left, rgba(255, 82, 82, 0.05), transparent)'
            }}>
                <div style={{
                    position: 'absolute',
                    top: '20%',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    width: '600px',
                    height: '600px',
                    background: 'var(--primary)',
                    filter: 'blur(150px)',
                    opacity: 0.05,
                    borderRadius: '50%',
                    zIndex: -1
                }}></div>

                <div style={{
                    backgroundColor: 'var(--surface-variant)',
                    color: 'var(--primary)',
                    padding: '8px 16px',
                    borderRadius: '20px',
                    fontSize: '14px',
                    fontWeight: '700',
                    marginBottom: '24px',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '8px',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.05)'
                }}>
                    ✨ SSAFY 커뮤니티의 새로운 기준
                </div>

                <h1 style={{
                    fontSize: 'clamp(40px, 8vw, 84px)',
                    fontWeight: '900',
                    lineHeight: '1.1',
                    letterSpacing: '-2px',
                    marginBottom: '24px',
                    background: 'linear-gradient(135deg, var(--on-surface) 0%, var(--primary) 100%)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent'
                }}>
                    SSABRE, 함께 성장하는<br />커뮤니티
                </h1>

                <p style={{
                    fontSize: 'clamp(16px, 2vw, 20px)',
                    color: 'var(--on-surface-variant)',
                    maxWidth: '800px',
                    lineHeight: '1.6',
                    marginBottom: '48px'
                }}>
                    팀 매칭부터 커뮤니티 활동까지, SSAFY 교육생을 위한 소통과 성장의 공간.<br />
                    같은 목표를 가진 동료들과 함께 프로젝트를 만들고 경험을 나누세요.
                </p>

                <button
                    onClick={() => navigate('/join')}
                    style={{
                        padding: '18px 40px',
                        borderRadius: '40px',
                        backgroundColor: 'var(--primary)',
                        color: 'white',
                        border: 'none',
                        fontSize: '18px',
                        fontWeight: '800',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        boxShadow: '0 8px 25px rgba(91, 127, 255, 0.4)',
                        transition: 'all 0.3s ease'
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.transform = 'translateY(-3px)';
                        e.currentTarget.style.boxShadow = '0 12px 30px rgba(91, 127, 255, 0.5)';
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.transform = 'translateY(0)';
                        e.currentTarget.style.boxShadow = '0 8px 25px rgba(91, 127, 255, 0.4)';
                    }}
                >
                    지금 바로 시작하기 <MdKeyboardArrowRight size={24} />
                </button>
            </section>

            {/* Features Section */}
            <section style={{ padding: '120px 40px', maxWidth: '1200px', margin: '0 auto' }}>
                <div style={{ textAlign: 'center', marginBottom: '80px' }}>
                    <h2 style={{ fontSize: '42px', fontWeight: '800', marginBottom: '16px' }}>주요 기능</h2>
                    <p style={{ color: 'var(--on-surface-variant)', fontSize: '18px' }}>커뮤니티 활동을 위해 필요한 모든 것을 담았습니다.</p>
                </div>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(2, 1fr)',
                    gap: '32px',
                    maxWidth: '900px',
                    margin: '0 auto'
                }}>
                    {features.map((feature, idx) => (
                        <div
                            key={idx}
                            style={{
                                padding: '40px',
                                borderRadius: '24px',
                                backgroundColor: 'var(--surface)',
                                border: '1px solid var(--border-color)',
                                transition: 'all 0.3s ease',
                                cursor: 'default'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-10px)';
                                e.currentTarget.style.borderColor = feature.color;
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.borderColor = 'var(--border-color)';
                            }}
                        >
                            <div style={{
                                width: '64px',
                                height: '64px',
                                borderRadius: '16px',
                                backgroundColor: `${feature.color}15`,
                                color: feature.color,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                marginBottom: '24px'
                            }}>
                                {feature.icon}
                            </div>
                            <h3 style={{ fontSize: '22px', fontWeight: '800', marginBottom: '16px' }}>{feature.title}</h3>
                            <p style={{ color: 'var(--on-surface-variant)', lineHeight: '1.6', fontSize: '15px' }}>{feature.description}</p>
                        </div>
                    ))}
                </div>
            </section>

            {/* CTA Section */}
            <section style={{
                padding: '120px 20px',
                textAlign: 'center',
                backgroundColor: 'var(--surface-variant)',
                borderRadius: '60px 60px 0 0'
            }}>
                <h2 style={{ fontSize: '48px', fontWeight: '800', marginBottom: '24px' }}>지금 바로 합류하세요</h2>
                <p style={{ color: 'var(--on-surface-variant)', fontSize: '20px', marginBottom: '48px', maxWidth: '600px', margin: '0 auto 48px auto' }}>
                    SSAFY 교육생들을 위한 커뮤니티 플랫폼에서 함께 성장하고 소통하세요.
                </p>
                <button
                    onClick={() => navigate('/join')}
                    style={{
                        padding: '20px 60px',
                        borderRadius: '40px',
                        backgroundColor: 'var(--primary)',
                        color: 'white',
                        border: 'none',
                        fontSize: '20px',
                        fontWeight: '800',
                        cursor: 'pointer',
                        boxShadow: '0 10px 30px rgba(91, 127, 255, 0.4)'
                    }}
                >
                    무료로 시작하기
                </button>
            </section>

            {/* Footer */}
            <footer style={{
                padding: '80px 40px',
                borderTop: '1px solid var(--border-color)',
                textAlign: 'center',
                backgroundColor: 'var(--surface-variant)'
            }}>
                <div style={{ fontSize: '24px', fontWeight: '900', color: 'var(--primary)', marginBottom: '32px' }}>
                    SSABRE
                </div>
                <div style={{ display: 'flex', justifyContent: 'center', gap: '40px', marginBottom: '40px', flexWrap: 'wrap' }}>
                    {['이용약관', '개인정보처리방침', '고객지원', '문의하기'].map((item, idx) => (
                        <span key={idx} style={{ color: 'var(--on-surface-variant)', fontSize: '14px', cursor: 'pointer' }}>{item}</span>
                    ))}
                </div>
                <div style={{ color: 'var(--on-surface-variant)', fontSize: '13px', opacity: 0.6 }}>
                    © 2026 SSABRE. All rights reserved. 본 서비스는 SSAFY 교육생들을 지원하기 위해 제작되었습니다.
                </div>
            </footer>
        </div>
    );
};

export default LandingPage;
