import React from 'react';
import { MdClose } from 'react-icons/md';

interface RecentSearchChipProps {
    keyword: string;
    onDelete: () => void;
    onClick: () => void;
}

const RecentSearchChip: React.FC<RecentSearchChipProps> = ({ keyword, onDelete, onClick }) => {
    return (
        <div
            style={{
                display: 'inline-flex',
                alignItems: 'center',
                padding: '6px 10px',
                border: '1px solid var(--border-color)',
                borderRadius: '8px',
                backgroundColor: 'transparent',
                cursor: 'pointer',
                transition: 'background-color 0.2s'
            }}
            onClick={onClick}
            onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = 'rgba(100, 149, 235, 0.1)';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'transparent';
            }}
        >
            <span style={{
                fontSize: '14px',
                color: 'var(--on-surface-variant)',
                marginRight: '6px',
                maxWidth: '150px',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap'
            }}>
                {keyword}
            </span>
            <MdClose
                size={14}
                color="var(--on-surface-variant)"
                style={{ opacity: 0.6, cursor: 'pointer' }}
                onClick={(e) => {
                    e.stopPropagation();
                    onDelete();
                }}
            />
        </div>
    );
};

export default RecentSearchChip;
