import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { myPageService, uploadService, authService } from '../../services/api';
import type { MyPageResponse } from '../../services/api';
import ProfileHeader from './ProfileHeader';
import StatsRow from './StatsRow';
import PortfolioCard from './PortfolioCard';
import { MdSettings, MdLogout } from 'react-icons/md';

export default function MyPage() {
    const navigate = useNavigate();
    const [myPageData, setMyPageData] = useState<MyPageResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isUploadingImage, setIsUploadingImage] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [showFullScreenImage, setShowFullScreenImage] = useState(false);
    const [showLogoutDialog, setShowLogoutDialog] = useState(false);

    // Load My Page Data
    const loadMyPage = async () => {
        try {
            setIsLoading(true);
            setErrorMessage(null);
            const data = await myPageService.getMyPage();
            setMyPageData(data);
        } catch (error: any) {
            console.error('Failed to load my page:', error);
            setErrorMessage(error?.message || '데이터를 불러오는데 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadMyPage();
    }, []);

    // Image Upload Handler
    const handleImageUpload = async (file: File) => {
        try {
            setIsUploadingImage(true);
            setErrorMessage(null);
            const imageUrl = await uploadService.uploadImage(file);
            await myPageService.updateProfileImage(imageUrl);
            await loadMyPage();
        } catch (error: any) {
            console.error('Failed to upload profile image:', error);
            setErrorMessage(error?.message || '이미지 업로드에 실패했습니다.');
        } finally {
            setIsUploadingImage(false);
        }
    };

    // Image Delete Handler
    const handleImageDelete = async () => {
        if (!window.confirm('프로필 이미지를 삭제하시겠습니까?')) {
            return;
        }
        try {
            setIsUploadingImage(true);
            setErrorMessage(null);
            await myPageService.deleteProfileImage();
            await loadMyPage();
        } catch (error: any) {
            console.error('Failed to delete profile image:', error);
            setErrorMessage(error?.message || '이미지 삭제에 실패했습니다.');
        } finally {
            setIsUploadingImage(false);
        }
    };

    // Logout Handler
    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    if (isLoading && !myPageData) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: 'var(--background)' }}>
                <div className="loading-spinner" />
            </div>
        );
    }

    return (
        <div style={{ minHeight: '100vh', backgroundColor: 'var(--background)' }}>
            {/* Top Header Section */}
            <div style={{
                backgroundColor: 'var(--surface)',
                borderBottom: '1px solid var(--border-color)',
                position: 'sticky',
                top: 0,
                zIndex: 100
            }}>
                <div className="container" style={{ padding: '24px 20px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>내 정보</h1>
                        <div style={{ display: 'flex', gap: '12px' }}>
                            <button
                                onClick={() => navigate('/setting_screen')}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    padding: '8px 16px',
                                    borderRadius: '12px',
                                    backgroundColor: 'var(--field-bg)',
                                    border: 'none',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: 500,
                                    color: 'var(--on-surface)',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--border-color)'}
                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                            >
                                <MdSettings size={18} />
                                설정
                            </button>
                            <button
                                onClick={() => setShowLogoutDialog(true)}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    padding: '8px 16px',
                                    borderRadius: '12px',
                                    backgroundColor: 'rgba(179, 38, 30, 0.1)',
                                    border: 'none',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    fontWeight: 500,
                                    color: 'var(--error)',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(179, 38, 30, 0.2)'}
                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'rgba(179, 38, 30, 0.1)'}
                            >
                                <MdLogout size={18} />
                                로그아웃
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Content Area */}
            <div className="container" style={{ padding: '40px 20px' }}>
                <div style={{ maxWidth: '900px', margin: '0 auto' }}>
                    {/* Error Message */}
                    {errorMessage && (
                        <div style={{
                            backgroundColor: 'rgba(179, 38, 30, 0.1)',
                            color: 'var(--error)',
                            padding: '16px',
                            borderRadius: '16px',
                            marginBottom: '24px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            {errorMessage}
                        </div>
                    )}

                    {myPageData && (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
                            {/* Profile Section */}
                            <div style={{
                                backgroundColor: 'var(--surface)',
                                borderRadius: '24px',
                                padding: '32px',
                                boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
                                border: '1px solid var(--border-color)'
                            }}>
                                <ProfileHeader
                                    user={myPageData.user}
                                    isUploading={isUploadingImage}
                                    onImageChange={handleImageUpload}
                                    onImageClick={() => {
                                        if (myPageData.user?.profileImageUrl) {
                                            setShowFullScreenImage(true);
                                        }
                                    }}
                                    onImageDelete={handleImageDelete}
                                />

                                <div style={{ marginTop: '32px' }}>
                                    <StatsRow
                                        counts={myPageData.counts}
                                        onPostsClick={() => navigate('/mypage/posts')}
                                        onCommentsClick={() => navigate('/mypage/comments')}
                                        onScrapsClick={() => navigate('/mypage/scraps')}
                                    />
                                </div>
                            </div>

                            {/* Portfolio Section */}
                            <div>
                                <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '16px', color: 'var(--on-surface)' }}>
                                    내 포트폴리오
                                </h2>
                                <PortfolioCard
                                    summary={myPageData.portfolioSummary}
                                    onDetailClick={() => navigate('/portfolio/detail')}
                                />
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Modals & Dialogs */}
            {showFullScreenImage && myPageData?.user?.profileImageUrl && (
                <div
                    onClick={() => setShowFullScreenImage(false)}
                    style={{
                        position: 'fixed',
                        top: 0, left: 0, right: 0, bottom: 0,
                        backgroundColor: 'rgba(0, 0, 0, 0.9)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 2000,
                        cursor: 'pointer'
                    }}
                >
                    <img
                        src={myPageData.user.profileImageUrl}
                        alt="프로필 이미지"
                        style={{ maxWidth: '90%', maxHeight: '90%', objectFit: 'contain', borderRadius: '16px' }}
                        onClick={(e) => e.stopPropagation()}
                    />
                </div>
            )}

            {showLogoutDialog && (
                <div
                    onClick={() => setShowLogoutDialog(false)}
                    style={{
                        position: 'fixed',
                        top: 0, left: 0, right: 0, bottom: 0,
                        backgroundColor: 'rgba(0, 0, 0, 0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 2000
                    }}
                >
                    <div
                        onClick={(e) => e.stopPropagation()}
                        style={{
                            backgroundColor: 'var(--surface)',
                            borderRadius: '24px',
                            padding: '32px',
                            maxWidth: '400px',
                            width: '90%',
                            boxShadow: '0 8px 32px rgba(0,0,0,0.2)'
                        }}
                    >
                        <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '12px' }}>로그아웃</h2>
                        <p style={{ color: 'var(--on-surface-variant)', marginBottom: '32px', lineHeight: '1.5' }}>
                            로그아웃 하시겠습니까? 세션이 종료됩니다.
                        </p>
                        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                            <button
                                onClick={() => setShowLogoutDialog(false)}
                                style={{
                                    padding: '12px 24px',
                                    borderRadius: '16px',
                                    border: 'none',
                                    backgroundColor: 'var(--field-bg)',
                                    color: 'var(--on-surface)',
                                    cursor: 'pointer',
                                    fontWeight: 600
                                }}
                            >
                                취소
                            </button>
                            <button
                                onClick={handleLogout}
                                style={{
                                    padding: '12px 24px',
                                    borderRadius: '16px',
                                    border: 'none',
                                    backgroundColor: 'var(--primary)',
                                    color: 'white',
                                    cursor: 'pointer',
                                    fontWeight: 600
                                }}
                            >
                                로그아웃
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
