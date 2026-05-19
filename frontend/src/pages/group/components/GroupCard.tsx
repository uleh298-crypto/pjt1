import React from 'react';
import { MdGroups, MdArrowForwardIos } from 'react-icons/md';
import type { GroupSummary } from '../../../services/api';
import { calculateDDay, isGroupClosed, studyTypeLabels, teamTypeLabels } from '../../../utils/groupUtils';

interface GroupCardProps {
    group: GroupSummary;
    onClick: (id: number) => void;
}

const GroupCard: React.FC<GroupCardProps> = ({ group, onClick }) => {
    const isClosed = isGroupClosed(group.status, group.endDate);
    const dDay = calculateDDay(group.endDate);

    // 영문 타입을 한글 라벨로 변환
    const getLabelFromType = (type: string) => {
        const allLabels = { ...studyTypeLabels, ...teamTypeLabels };
        const foundLabel = Object.entries(allLabels).find(([_, value]) => value === type);
        return foundLabel ? foundLabel[0] : type;
    };

    return (
        <div
            onClick={() => !isClosed && onClick(group.id)}
            style={{
                padding: '24px',
                border: '1px solid var(--border-color)',
                borderRadius: '20px',
                backgroundColor: 'var(--surface)',
                cursor: isClosed ? 'not-allowed' : 'pointer',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                opacity: isClosed ? 0.6 : 1,
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                boxShadow: isClosed ? 'none' : '0 2px 8px rgba(0,0,0,0.04)',
            }}
            onMouseEnter={(e) => {
                if (!isClosed) {
                    e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.08)';
                    e.currentTarget.style.transform = 'translateY(-6px)';
                    e.currentTarget.style.borderColor = 'var(--primary)';
                }
            }}
            onMouseLeave={(e) => {
                if (!isClosed) {
                    e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.04)';
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.borderColor = 'var(--border-color)';
                }
            }}
        >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                <div style={{
                    fontSize: '12px',
                    fontWeight: 'bold',
                    color: 'var(--primary)',
                    backgroundColor: 'rgba(100, 149, 235, 0.1)',
                    padding: '6px 12px',
                    borderRadius: '8px'
                }}>
                    {getLabelFromType(group.type)}
                </div>
                <div style={{
                    fontSize: '13px',
                    color: 'var(--error)',
                    fontWeight: '700'
                }}>
                    {dDay}
                </div>
            </div>

            <h3 style={{
                fontSize: '18px',
                fontWeight: '700',
                color: 'var(--on-surface)',
                margin: '0 0 12px 0',
                lineHeight: '1.5',
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                flex: 1
            }}>
                {group.title}
            </h3>

            <div style={{
                marginTop: 'auto',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                paddingTop: '16px',
                borderTop: '1px solid rgba(0,0,0,0.05)'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <MdGroups size={20} color="var(--on-surface-variant)" />
                    <span style={{
                        fontSize: '14px',
                        color: 'var(--on-surface)',
                        fontWeight: '600'
                    }}>
                        {group.currentMembers || 0} / {group.capacity}명
                    </span>
                </div>

                {!isClosed ? (
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        fontSize: '13px',
                        color: 'var(--primary)',
                        fontWeight: '600'
                    }}>
                        상세보기
                        <MdArrowForwardIos size={12} />
                    </div>
                ) : (
                    <span style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                        모집 마감
                    </span>
                )}
            </div>
        </div>
    );
};
export default GroupCard;
