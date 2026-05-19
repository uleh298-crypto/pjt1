import type { MyPageCounts } from '../../services/api';

interface StatsRowProps {
    counts: MyPageCounts | null;
    onPostsClick: () => void;
    onCommentsClick: () => void;
    onScrapsClick: () => void;
}

export default function StatsRow({
    counts,
    onPostsClick,
    onCommentsClick,
    onScrapsClick
}: StatsRowProps) {
    const posts = counts?.postCount?.toString() || '0';
    const comments = counts?.commentCount?.toString() || '0';
    const scraps = counts?.scrapCount?.toString() || '0';

    return (
        <div style={{
            display: 'flex',
            width: '100%',
            gap: '12px'
        }}>
            <StatItem
                label="작성한 글"
                value={posts}
                onClick={onPostsClick}
            />
            <StatItem
                label="댓글"
                value={comments}
                onClick={onCommentsClick}
            />
            <StatItem
                label="스크랩"
                value={scraps}
                onClick={onScrapsClick}
            />
        </div>
    );
}

interface StatItemProps {
    label: string;
    value: string;
    onClick: () => void;
}

function StatItem({ label, value, onClick }: StatItemProps) {
    return (
        <div
            onClick={onClick}
            style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                padding: '24px 12px',
                borderRadius: '24px',
                backgroundColor: 'var(--field-bg)',
                transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                border: '1px solid transparent'
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--surface)';
                e.currentTarget.style.borderColor = 'var(--primary)';
                e.currentTarget.style.transform = 'translateY(-4px)';
                e.currentTarget.style.boxShadow = '0 8px 16px rgba(100, 149, 235, 0.15)';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--field-bg)';
                e.currentTarget.style.borderColor = 'transparent';
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
            }}
        >
            <div style={{
                fontSize: '14px',
                fontWeight: 600,
                color: 'var(--on-surface-variant)',
                marginBottom: '10px'
            }}>
                {label}
            </div>
            <div style={{
                fontSize: '28px',
                fontWeight: 800,
                color: 'var(--primary)'
            }}>
                {value}
            </div>
        </div>
    );
}
