import React from 'react';
import { useNavigate } from 'react-router-dom';
import { MdSearch, MdPerson, MdNotifications } from 'react-icons/md';

const Header: React.FC = () => {
    const navigate = useNavigate();

    return (
        <header style={{
            height: '60px',
            backgroundColor: 'var(--surface)',
            borderBottom: '1px solid var(--border-color)',
            position: 'sticky',
            top: 0,
            zIndex: 1000,
            width: '100%'
        }}>
            <div style={{
                maxWidth: '1180px',
                margin: '0 auto',
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0 20px'
            }}>
                {/* Logo */}
                <div
                    onClick={() => navigate('/home')}
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer'
                    }}
                >
                    <img src="/assets/logo.png" alt="SsabriTime" style={{ height: '32px' }} />
                    <span style={{ fontSize: '20px', fontWeight: 'bold', color: 'var(--primary)' }}>싸브리타임</span>
                </div>


                {/* Right Icons */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                    <div onClick={() => navigate('/notification')} style={{ cursor: 'pointer', position: 'relative' }}>
                        <MdNotifications size={24} color="var(--on-surface)" />
                    </div>
                    <div onClick={() => navigate('/mypage')} style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', border: '1px solid var(--border-color)', padding: '4px 8px', borderRadius: '16px' }}>
                        <MdPerson size={20} color="var(--on-surface)" />
                        <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--on-surface)' }}>내 정보</span>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;
