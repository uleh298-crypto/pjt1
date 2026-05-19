import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdArrowBack, MdMoreVert, MdGroups, MdCalendarToday, MdLocationOn } from 'react-icons/md';
import { studyService, teamService, memberService } from '../../services/api';
import type { GroupDetail } from '../../services/api';
import { calculateDDay, formatDate, isGroupClosed } from '../../utils/groupUtils';

interface GroupDetailPageProps {
    kind: 'study' | 'team';
}

const GroupDetailPage: React.FC<GroupDetailPageProps> = ({ kind }) => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [group, setGroup] = useState<GroupDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [showMenu, setShowMenu] = useState(false);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [isLeader, setIsLeader] = useState(false);
    const [isMember, setIsMember] = useState(false);

    const loadGroupDetail = async () => {
        if (!id) return;

        setLoading(true);
        try {
            const [result, myInfo] = await Promise.all([
                kind === 'study' ?
                    await studyService.getStudyDetail(Number(id)) :
                    await teamService.getTeamDetail(Number(id)),
                memberService.getMyInfo().catch(() => null)
            ]);

            setGroup(result);

            if (myInfo) {
                setCurrentUserId(myInfo.id);
                // Check if leader
                setIsLeader(result.leaderId === myInfo.id);
                // Check if member
                const memberExists = result.members?.some(m => m.id === myInfo.id);
                setIsMember(memberExists || false);
            }
        } catch (error) {
            console.error('그룹 상세 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApply = () => {
        navigate(`/groups/${kind}/${id}/apply`);
    };

    const handleEdit = () => {
        navigate(`/groups/${kind}/${id}/edit`);
    };

    const handleDelete = async () => {
        if (!window.confirm('정말 삭제하시겠습니까?')) return;

        try {
            if (kind === 'study') {
                await studyService.deleteStudy(Number(id));
            } else {
                await teamService.deleteTeam(Number(id));
            }
            alert('삭제되었습니다.');
            navigate(`/groups/${kind}`);
        } catch (error) {
            console.error('삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    };

    const handleLeave = async () => {
        if (!window.confirm('정말 탈퇴하시겠습니까?')) return;

        try {
            if (kind === 'study') {
                await studyService.leaveStudy(Number(id));
            } else {
                await teamService.leaveTeam(Number(id));
            }
            alert('탈퇴되었습니다.');
            navigate(`/groups/${kind}`);
        } catch (error) {
            console.error('탈퇴 실패:', error);
            alert('탈퇴에 실패했습니다.');
        }
    };

    useEffect(() => {
        loadGroupDetail();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id, kind]);

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                로딩 중...
            </div>
        );
    }

    if (!group) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                그룹을 찾을 수 없습니다.
            </div>
        );
    }

    const isClosed = isGroupClosed(group.status, group.endDate);
    const dDay = calculateDDay(group.endDate);

    return (
        <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--background)' }}>
            {/* Top Bar */}
            <div style={{
                padding: '16px 20px',
                borderBottom: '1px solid var(--border-color)',
                backgroundColor: 'var(--surface)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <MdArrowBack
                    size={24}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(-1)}
                />
                <h1 style={{ flex: 1, fontSize: '18px', fontWeight: 'bold', margin: '0 16px' }}>
                    {kind === 'study' ? '스터디' : '프로젝트'} 상세
                </h1>
                {(isLeader || isMember) && (
                    <div style={{ position: 'relative' }}>
                        <MdMoreVert
                            size={24}
                            style={{ cursor: 'pointer' }}
                            onClick={() => setShowMenu(!showMenu)}
                        />
                        {showMenu && (
                            <div style={{
                                position: 'absolute',
                                top: '100%',
                                right: 0,
                                marginTop: '4px',
                                background: 'var(--surface)',
                                border: '1px solid var(--border-color)',
                                borderRadius: '8px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                                minWidth: '120px',
                                zIndex: 100
                            }}>
                                {isLeader && (
                                    <>
                                        <div
                                            onClick={handleEdit}
                                            style={{
                                                padding: '12px 16px',
                                                cursor: 'pointer',
                                                fontSize: '14px',
                                                borderBottom: '1px solid var(--border-color)'
                                            }}
                                        >
                                            수정
                                        </div>
                                        <div
                                            onClick={handleDelete}
                                            style={{
                                                padding: '12px 16px',
                                                cursor: 'pointer',
                                                fontSize: '14px',
                                                borderBottom: isMember ? '1px solid var(--border-color)' : 'none'
                                            }}
                                        >
                                            삭제
                                        </div>
                                    </>
                                )}
                                {isMember && (
                                    <div
                                        onClick={handleLeave}
                                        style={{
                                            padding: '12px 16px',
                                            cursor: 'pointer',
                                            fontSize: '14px',
                                            color: 'var(--error)'
                                        }}
                                    >
                                        탈퇴
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Content */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
                {/* 그룹 정보 카드 */}
                <div style={{
                    backgroundColor: 'var(--surface)',
                    border: '1px solid var(--border-color)',
                    borderRadius: '16px',
                    padding: '20px',
                    marginBottom: '16px'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                        <span style={{
                            fontSize: '12px',
                            color: 'var(--on-surface-variant)',
                            backgroundColor: 'var(--surface-variant)',
                            padding: '4px 8px',
                            borderRadius: '4px'
                        }}>
                            {group.type}
                        </span>
                        <span style={{ fontSize: '14px', color: 'var(--error)', fontWeight: '600' }}>
                            {dDay}
                        </span>
                    </div>

                    <h2 style={{ fontSize: '22px', fontWeight: 'bold', marginBottom: '16px' }}>
                        {group.title}
                    </h2>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', color: 'var(--on-surface-variant)' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <MdCalendarToday size={18} />
                            <span>{formatDate(group.startDate)} ~ {formatDate(group.endDate)}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <MdGroups size={18} />
                            <span>{group.currentMembers || 0}/{group.capacity}명</span>
                        </div>
                        {group.campus && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <MdLocationOn size={18} />
                                <span>{group.campus.name}</span>
                            </div>
                        )}
                    </div>

                    <div style={{
                        marginTop: '20px',
                        paddingTop: '16px',
                        borderTop: '1px solid var(--border-color)',
                        fontSize: '14px',
                        lineHeight: '1.6',
                        whiteSpace: 'pre-wrap'
                    }}>
                        {group.description}
                    </div>
                </div>

                {/* 리더 정보 */}
                {group.leaderName && (
                    <div style={{
                        backgroundColor: 'var(--surface)',
                        border: '1px solid var(--border-color)',
                        borderRadius: '16px',
                        padding: '20px',
                        marginBottom: '16px'
                    }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '12px' }}>
                            리더
                        </h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <div style={{
                                width: '48px',
                                height: '48px',
                                borderRadius: '50%',
                                backgroundColor: 'var(--primary)',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                color: 'white',
                                fontWeight: 'bold',
                                fontSize: '18px'
                            }}>
                                {group.leaderName[0]}
                            </div>
                            <div>
                                <div style={{ fontWeight: '600', marginBottom: '4px' }}>
                                    {group.leaderName}
                                </div>
                                <div style={{ fontSize: '14px', color: 'var(--on-surface-variant)' }}>
                                    {group.leaderMattermostId || group.leaderEmail}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* 멤버 목록 */}
                {group.members && group.members.length > 0 && (
                    <div style={{
                        backgroundColor: 'var(--surface)',
                        border: '1px solid var(--border-color)',
                        borderRadius: '16px',
                        padding: '20px'
                    }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '12px' }}>
                            멤버 ({group.members.length}명)
                        </h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {group.members.map((member) => (
                                <div key={member.id} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    <div style={{
                                        width: '40px',
                                        height: '40px',
                                        borderRadius: '50%',
                                        backgroundColor: 'var(--surface-variant)',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontWeight: '600',
                                        fontSize: '14px'
                                    }}>
                                        {member.name[0]}
                                    </div>
                                    <div>
                                        <div style={{ fontWeight: '500', marginBottom: '2px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            {member.name}
                                            {member.portfolioId && (
                                                <span
                                                    onClick={() => navigate(`/portfolio/${member.portfolioId}`)}
                                                    style={{
                                                        cursor: 'pointer',
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        color: 'var(--primary)',
                                                        fontSize: '11px',
                                                        backgroundColor: 'rgba(100, 149, 235, 0.1)',
                                                        padding: '2px 6px',
                                                        borderRadius: '4px'
                                                    }}
                                                >
                                                    포트폴리오
                                                </span>
                                            )}
                                        </div>
                                        <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                                            {member.mattermostId || member.email}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            {/* 신청 버튼 */}
            {!isClosed && !isMember && (
                <div style={{
                    padding: '16px 20px',
                    borderTop: '1px solid var(--border-color)',
                    backgroundColor: 'var(--surface)'
                }}>
                    <button
                        onClick={handleApply}
                        style={{
                            width: '100%',
                            padding: '14px',
                            backgroundColor: 'var(--primary)',
                            color: 'white',
                            border: 'none',
                            borderRadius: '8px',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}
                    >
                        신청하기
                    </button>
                </div>
            )}
        </div>
    );
};

export default GroupDetailPage;
