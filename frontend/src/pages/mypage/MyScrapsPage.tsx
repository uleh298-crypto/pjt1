import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdArrowBack } from 'react-icons/md';
import { myPageService } from '../../services/api';
import type { PostResponse } from '../../services/api';
import PostItem from '../board/components/PostItem';

const MyScrapsPage: React.FC = () => {
    const navigate = useNavigate();
    const [posts, setPosts] = useState<PostResponse[]>([]);
    const [loading, setLoading] = useState(false);

    const loadScraps = async () => {
        setLoading(true);
        try {
            const res = await myPageService.getMyScraps();
            setPosts(res);
        } catch (error) {
            console.error('스크랩 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadScraps();
    }, []);

    return (
        <div style={{ minHeight: '100%', backgroundColor: 'var(--background)' }}>
            {/* Header */}
            <div style={{
                padding: '24px 20px',
                borderBottom: '1px solid var(--border-color)',
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                position: 'sticky',
                top: 0,
                backgroundColor: 'var(--surface)',
                zIndex: 100
            }}>
                <div
                    onClick={() => navigate(-1)}
                    style={{
                        cursor: 'pointer',
                        padding: '8px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        transition: 'background-color 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                    <MdArrowBack size={24} />
                </div>
                <h1 style={{ fontSize: '22px', fontWeight: 'bold', margin: 0 }}>스크랩한 글</h1>
            </div>

            {/* List Content */}
            <div className="container" style={{ padding: '40px 20px' }}>
                {loading && posts.length === 0 ? (
                    <div style={{ padding: '100px 0', textAlign: 'center', color: 'var(--on-surface-variant)' }}>
                        <div className="loading-spinner" />
                        <p style={{ marginTop: '16px' }}>스크랩 정보를 불러오는 중입니다...</p>
                    </div>
                ) : posts.length === 0 ? (
                    <div style={{
                        padding: '100px 0',
                        textAlign: 'center',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '24px',
                        border: '1px dashed var(--border-color)',
                        color: 'var(--on-surface-variant)'
                    }}>
                        <p style={{ fontSize: '18px' }}>스크랩한 글이 없습니다.</p>
                    </div>
                ) : (
                    <div style={{
                        maxWidth: '900px',
                        margin: '0 auto',
                        display: 'flex',
                        flexDirection: 'column'
                    }}>
                        {posts.map((post, index) => (
                            <PostItem
                                key={post.id}
                                post={post}
                                isLast={index === posts.length - 1}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default MyScrapsPage;
