import React from 'react';
import { useNavigate } from 'react-router-dom';
import { MdGroups } from 'react-icons/md';
import { studyTypeLabels, teamTypeLabels } from '../../../utils/groupUtils';

interface MyGroupCardProps {
    id: number;
    kind: 'study' | 'team';
    title: string;
    role: string;
    isLeader: boolean;
    category: string;
    currentMembers: number;
    memberProfileImageUrls: string[];
}

const MyGroupCard: React.FC<MyGroupCardProps> = ({
    id,
    kind,
    title,
    role,
    isLeader,
    category,
    currentMembers,
    memberProfileImageUrls
}) => {
    const navigate = useNavigate();

    const getLabelFromType = (type: string) => {
        const allLabels = { ...studyTypeLabels, ...teamTypeLabels };
        const foundLabel = Object.entries(allLabels).find(([_, value]) => value === type);
        return foundLabel ? foundLabel[0] : type;
    };

    const handleClick = () => {
        navigate(`/mygroups/${kind}/${id}`);
    };

    return (
        <div
            onClick={handleClick}
            style={{
                backgroundColor: 'var(--surface)',
                borderRadius: '14px',
                padding: '14px',
                cursor: 'pointer',
                border: '1px solid var(--border-color)',
                transition: 'all 0.2s ease',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px'
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
            }}
        >
            {/* 제목 */}
            <h3 style={{
                fontSize: '16px',
                fontWeight: '600',
                margin: 0,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical'
            }}>
                {title}
            </h3>

            {/* 역할 및 카테고리 */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{
                    padding: '2px 8px',
                    borderRadius: '6px',
                    fontSize: '11px',
                    fontWeight: 'bold',
                    backgroundColor: isLeader
                        ? 'var(--primary-container)'
                        : 'var(--surface-variant)',
                    color: isLeader
                        ? 'var(--primary)'
                        : 'var(--on-surface-variant)'
                }}>
                    {role}
                </span>
                <span style={{
                    fontSize: '12px',
                    color: 'var(--primary)',
                    backgroundColor: 'rgba(100, 149, 235, 0.1)',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontWeight: '600'
                }}>
                    {getLabelFromType(category)}
                </span>
            </div>

            {/* 멤버 수 */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <MdGroups size={16} style={{ color: 'var(--on-surface-variant)' }} />
                <span style={{
                    fontSize: '14px',
                    color: 'var(--on-surface-variant)'
                }}>
                    {currentMembers}명
                </span>
            </div>

            {/* 멤버 프로필 이미지 */}
            <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                {Array.from({ length: Math.min(currentMembers, 4) }).map((_, index) => {
                    const profileUrl = memberProfileImageUrls[index];
                    return (
                        <div
                            key={index}
                            style={{
                                width: '26px',
                                height: '26px',
                                borderRadius: '50%',
                                backgroundColor: 'var(--surface-variant)',
                                overflow: 'hidden',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center'
                            }}
                        >
                            {profileUrl ? (
                                <img
                                    src={profileUrl}
                                    alt=""
                                    style={{
                                        width: '100%',
                                        height: '100%',
                                        objectFit: 'cover'
                                    }}
                                />
                            ) : (
                                <svg width="26" height="26" viewBox="0 0 24 24" fill="var(--on-surface-variant)">
                                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                                </svg>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default MyGroupCard;
