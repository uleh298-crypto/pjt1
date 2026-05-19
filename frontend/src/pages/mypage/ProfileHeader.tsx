import { useRef } from 'react';
import type { MyPageUserInfo } from '../../services/api';

interface ProfileHeaderProps {
    user: MyPageUserInfo | null;
    isUploading: boolean;
    onImageChange: (file: File) => void;
    onImageClick: () => void;
    onImageDelete: () => void;
}

export default function ProfileHeader({
    user,
    isUploading,
    onImageChange,
    onImageClick,
    onImageDelete
}: ProfileHeaderProps) {
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleCameraClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            onImageChange(file);
        }
    };

    const name = user?.name || '사용자';
    const mattermostId = user?.mattermostId ? `@${user.mattermostId}` : '@-';
    const campusLabel = user?.generation && user?.campus
        ? `${user.generation}기 ${user.campus} 캠퍼스`
        : user?.generation
            ? `${user.generation}기`
            : user?.campus
                ? user.campus
                : '캠퍼스 정보 없음';

    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '24px',
            width: '100%',
            position: 'relative'
        }}>
            {/* Profile Image Container */}
            <div style={{
                position: 'relative',
                width: '100px',
                height: '100px',
                flexShrink: 0
            }}>
                {/* Profile Image */}
                <div
                    onClick={onImageClick}
                    style={{
                        width: '100%',
                        height: '100%',
                        borderRadius: '32px',
                        overflow: 'hidden',
                        backgroundColor: 'var(--field-bg)',
                        cursor: 'pointer',
                        border: '4px solid var(--surface)',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    }}
                >
                    {user?.profileImageUrl ? (
                        <img
                            src={user.profileImageUrl}
                            alt="프로필 이미지"
                            style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover'
                            }}
                        />
                    ) : (
                        <div style={{
                            width: '100%',
                            height: '100%',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '40px',
                            color: 'var(--on-surface-variant)'
                        }}>
                            👤
                        </div>
                    )}
                </div>

                {/* Loading Indicator */}
                {isUploading && (
                    <div style={{
                        position: 'absolute',
                        top: 0, left: 0, width: '100%', height: '100%',
                        borderRadius: '32px',
                        backgroundColor: 'rgba(255, 255, 255, 0.7)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 1
                    }}>
                        <div className="loading-spinner-small" />
                    </div>
                )}

                {/* Camera Button */}
                <button
                    onClick={handleCameraClick}
                    disabled={isUploading}
                    style={{
                        position: 'absolute',
                        bottom: '-4px',
                        right: '-4px',
                        width: '32px',
                        height: '32px',
                        borderRadius: '12px',
                        backgroundColor: 'var(--primary)',
                        border: '3px solid var(--surface)',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
                        color: 'white'
                    }}
                >
                    📷
                </button>

                {/* Hidden File Input */}
                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleFileChange}
                    style={{ display: 'none' }}
                />
            </div>

            {/* User Info */}
            <div style={{ flex: 1 }}>
                <div style={{
                    fontSize: '28px',
                    fontWeight: 800,
                    color: 'var(--on-surface)',
                    marginBottom: '6px',
                    letterSpacing: '-0.5px'
                }}>
                    {name}
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', alignItems: 'center' }}>
                    <span style={{
                        fontSize: '14px',
                        fontWeight: 600,
                        color: 'var(--primary)',
                        backgroundColor: 'rgba(100, 149, 235, 0.1)',
                        padding: '4px 10px',
                        borderRadius: '8px'
                    }}>
                        {mattermostId}
                    </span>
                    <span style={{
                        fontSize: '14px',
                        color: 'var(--on-surface-variant)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px'
                    }}>
                        <span style={{ width: '4px', height: '4px', borderRadius: '50%', backgroundColor: 'var(--on-surface-variant)', opacity: 0.5 }} />
                        {campusLabel}
                    </span>
                </div>
            </div>
        </div>
    );
}
