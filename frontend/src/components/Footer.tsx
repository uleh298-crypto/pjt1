import React from 'react';

const Footer: React.FC = () => {
    const currentYear = new Date().getFullYear();

    return (
        <footer style={{
            backgroundColor: 'var(--surface)',
            borderTop: '1px solid var(--border-color)',
            padding: '20px 0',
            marginTop: '60px'
        }}>
            <div className="container" style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '16px'
            }}>
                {/* Left Section - Copyright */}
                <div style={{ fontSize: '14px', color: 'var(--on-surface-variant)' }}>
                    © {currentYear} 싸브리타임. All rights reserved.
                </div>

                {/* Center Section - Links */}
                <div style={{ display: 'flex', gap: '20px', fontSize: '14px' }}>
                    <a href="/about" style={{ color: 'var(--on-surface-variant)', textDecoration: 'none' }}>
                        서비스 소개
                    </a>
                    <a href="/terms" style={{ color: 'var(--on-surface-variant)', textDecoration: 'none' }}>
                        이용약관
                    </a>
                    <a href="/privacy" style={{ color: 'var(--on-surface-variant)', textDecoration: 'none' }}>
                        개인정보처리방침
                    </a>
                </div>

                {/* Right Section - Info */}
                <div style={{ fontSize: '13px', color: 'var(--on-surface-variant)' }}>
                    SSAFY 14기 공통 프로젝트
                </div>
            </div>
        </footer>
    );
};

export default Footer;
