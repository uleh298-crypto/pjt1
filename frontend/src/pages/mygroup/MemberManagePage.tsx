import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdArrowBack, MdCheck, MdClose, MdLaunch } from 'react-icons/md';
import { studyService, teamService, type StudyApplicationResponse, type TeamApplicationResponse } from '../../services/api';

export default function MemberManagePage() {
    const navigate = useNavigate();
    const { type, groupId } = useParams<{ type: string; groupId: string }>();
    const [loading, setLoading] = useState(true);
    const [applications, setApplications] = useState<(StudyApplicationResponse | TeamApplicationResponse)[]>([]);

    useEffect(() => {
        fetchApplications();
    }, [type, groupId]);

    const fetchApplications = async () => {
        if (!groupId) return;
        try {
            setLoading(true);
            const data = type === 'study'
                ? await studyService.getStudyApplications(Number(groupId))
                : await teamService.getTeamApplications(Number(groupId));
            setApplications(data);
        } catch (err) {
            console.error('Failed to fetch applications:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleAction = async (appId: number, action: 'accept' | 'reject') => {
        try {
            if (type === 'study') {
                action === 'accept'
                    ? await studyService.acceptStudyApplication(appId)
                    : await studyService.rejectStudyApplication(appId);
            } else {
                action === 'accept'
                    ? await teamService.acceptTeamApplication(appId)
                    : await teamService.rejectTeamApplication(appId);
            }
            alert(action === 'accept' ? '승인되었습니다.' : '거절되었습니다.');
            fetchApplications();
        } catch (err) {
            console.error('Action failed:', err);
            alert('처리에 실패했습니다.');
        }
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
                <div className="loading-spinner" />
            </div>
        );
    }

    return (
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '40px 24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '32px' }}>
                <button onClick={() => navigate(-1)} style={iconBtnStyle}>
                    <MdArrowBack size={24} />
                </button>
                <h1 style={{ fontSize: '24px', fontWeight: 800 }}>지원자 관리</h1>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {applications.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '60px', color: 'var(--on-surface-variant)' }}>
                        지원자가 없습니다.
                    </div>
                ) : (
                    applications.map((app) => (
                        <div key={app.id} style={appCardStyle}>
                            <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                                <img
                                    src={app.portfolio.memberProfileImageUrl || '/default-profile.png'}
                                    alt=""
                                    style={{ width: '60px', height: '60px', borderRadius: '50%', objectFit: 'cover', backgroundColor: 'var(--field-bg)' }}
                                />
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                        <div>
                                            <h3 style={{ fontSize: '18px', fontWeight: 800 }}>{app.portfolio.memberName}</h3>
                                            <span style={{ fontSize: '14px', color: 'var(--primary)', fontWeight: 600 }}>{app.position} 지원</span>
                                        </div>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <button
                                                onClick={() => handleAction(app.id, 'accept')}
                                                style={{ ...actionBtnStyle, backgroundColor: '#4CAF50', color: 'white' }}
                                            >
                                                <MdCheck /> 승인
                                            </button>
                                            <button
                                                onClick={() => handleAction(app.id, 'reject')}
                                                style={{ ...actionBtnStyle, backgroundColor: 'var(--error)', color: 'white' }}
                                            >
                                                <MdClose /> 거절
                                            </button>
                                        </div>
                                    </div>

                                    <div style={messageBoxStyle}>
                                        <div style={{ fontSize: '14px', fontWeight: 800, marginBottom: '4px' }}>{app.title}</div>
                                        <div style={{ fontSize: '14px', lineHeight: 1.6 }}>{app.message}</div>
                                    </div>

                                    <div style={{ marginTop: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                        <div style={{ display: 'flex', gap: '12px', fontSize: '13px', color: 'var(--on-surface-variant)' }}>
                                            <span>Tier: {app.portfolio.solvedacRank}</span>
                                            <span>Rating: {app.portfolio.swTestRank}</span>
                                        </div>
                                        <button
                                            onClick={() => navigate(`/portfolio/${app.portfolio.id}`)}
                                            style={portfolioLinkStyle}
                                        >
                                            포트폴리오 보기 <MdLaunch size={14} />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

const appCardStyle: React.CSSProperties = {
    backgroundColor: 'var(--surface)',
    borderRadius: '20px',
    padding: '24px',
    border: '1px solid var(--border-color)',
    boxShadow: '0 4px 12px rgba(0,0,0,0.03)'
};

const messageBoxStyle: React.CSSProperties = {
    backgroundColor: 'var(--field-bg)',
    padding: '16px',
    borderRadius: '12px',
    marginTop: '12px'
};

const actionBtnStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    padding: '8px 16px',
    borderRadius: '10px',
    border: 'none',
    fontWeight: 700,
    cursor: 'pointer',
    fontSize: '14px'
};

const iconBtnStyle: React.CSSProperties = {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    padding: '8px',
    display: 'flex',
    alignItems: 'center',
    color: 'var(--on-surface)'
};

const portfolioLinkStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    padding: '6px 12px',
    borderRadius: '8px',
    border: '1px solid var(--primary)',
    color: 'var(--primary)',
    backgroundColor: 'transparent',
    fontWeight: 600,
    fontSize: '13px',
    cursor: 'pointer'
};
