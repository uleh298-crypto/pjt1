import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { MdHome, MdList, MdGroups, MdMessage, MdSupervisorAccount } from 'react-icons/md';

const Sidebar: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const menuItems = [
        { label: '홈', path: '/home', icon: MdHome },
        { label: '게시판', path: '/board', icon: MdList },
        { label: '그룹 찾기', path: '/groups/study', icon: MdGroups },
        { label: '나의 그룹', path: '/mygroups/study', icon: MdSupervisorAccount },
        { label: '쪽지함', path: '/message', icon: MdMessage },
    ];

    return (
        <aside style={{ width: '180px', flexShrink: 0, paddingTop: '24px' }}>
            <nav>
                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    {menuItems.map(item => {
                        const isActive = location.pathname.startsWith(item.path);
                        return (
                            <li
                                key={item.path}
                                onClick={() => navigate(item.path)}
                                style={{
                                    padding: '12px 12px',
                                    marginBottom: '4px',
                                    borderRadius: '8px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    backgroundColor: isActive ? 'rgba(100, 149, 235, 0.1)' : 'transparent', // Light blue tint
                                    color: isActive ? 'var(--primary)' : 'var(--on-surface)',
                                    fontWeight: isActive ? 'bold' : 'normal'
                                }}
                            >
                                <item.icon size={22} />
                                <span style={{ fontSize: '15px' }}>{item.label}</span>
                            </li>
                        );
                    })}
                </ul>
            </nav>

        </aside>
    );
};

export default Sidebar;
