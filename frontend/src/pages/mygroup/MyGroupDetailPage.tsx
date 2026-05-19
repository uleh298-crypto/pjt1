import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdArrowBack, MdMoreVert, MdEdit, MdDelete, MdManageAccounts, MdCampaign, MdAddCircle, MdClose } from 'react-icons/md';
import {
    studyService, teamService, memberService,
    studyNoticeService, teamNoticeService,
    studyTaskService, teamTaskService
} from '../../services/api';
import type { GroupDetail, GroupMember, NoticeResponse, TaskResponse, TaskStatus } from '../../services/api';

interface MyGroupDetailPageProps {
    kind: 'study' | 'team';
}

const MyGroupDetailPage: React.FC<MyGroupDetailPageProps> = ({ kind }) => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [group, setGroup] = useState<GroupDetail | null>(null);
    const [members, setMembers] = useState<GroupMember[]>([]);
    const [notices, setNotices] = useState<NoticeResponse[]>([]);
    const [tasks, setTasks] = useState<TaskResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [showMenu, setShowMenu] = useState(false);
    const [isLeader, setIsLeader] = useState(false);

    // Task 필터
    const [selectedTaskTab, setSelectedTaskTab] = useState<'전체' | 'TODO' | 'IN_PROGRESS' | 'DONE'>('전체');

    // Modal states
    const [showTaskModal, setShowTaskModal] = useState(false);
    const [taskForm, setTaskForm] = useState({
        title: '',
        content: '',
        startDate: '',
        endDate: '',
        status: 'TODO' as TaskStatus
    });

    // 그룹 상세 로딩
    const loadGroupDetail = async () => {
        if (!id) return;

        setLoading(true);
        try {
            const result = kind === 'study' ?
                await studyService.getStudyDetail(Number(id)) :
                await teamService.getTeamDetail(Number(id));
            setGroup(result);

            // 멤버 목록 로딩
            const [membersResult, myInfo] = await Promise.all([
                kind === 'study' ? studyService.getStudyMembers(Number(id)) : teamService.getTeamMembers(Number(id)),
                memberService.getMyInfo()
            ]);
            setMembers(membersResult);

            // 리더 여부 확인
            setIsLeader(membersResult.some(m => m.id === myInfo.id && m.isLeader));

            // 공지사항 로딩
            await loadNotices();

            // 일정 로딩
            await loadTasks();
        } catch (error) {
            console.error('그룹 상세 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    // 공지사항 로딩
    const loadNotices = async () => {
        if (!id) return;
        try {
            const result = kind === 'study' ?
                await studyNoticeService.getNotices(Number(id)) :
                await teamNoticeService.getNotices(Number(id));
            setNotices(result);
        } catch (error) {
            console.error('공지사항 로딩 실패:', error);
        }
    };

    // 일정 로딩
    const loadTasks = async () => {
        if (!id) return;
        try {
            const result = kind === 'study' ?
                await studyTaskService.getTasks(Number(id)) :
                await teamTaskService.getTasks(Number(id));
            setTasks(result);
        } catch (error) {
            console.error('일정 로딩 실패:', error);
        }
    };

    // 일정 추가
    const handleAddTask = async () => {
        if (!id) return;
        if (!taskForm.title.trim()) {
            alert('제목을 입력하세요.');
            return;
        }
        if (!taskForm.startDate || !taskForm.endDate) {
            alert('시작일과 종료일을 입력하세요.');
            return;
        }

        try {
            if (kind === 'study') {
                await studyTaskService.createTask(Number(id), taskForm);
            } else {
                await teamTaskService.createTask(Number(id), taskForm);
            }
            alert('일정이 추가되었습니다.');
            setShowTaskModal(false);
            setTaskForm({
                title: '',
                content: '',
                startDate: '',
                endDate: '',
                status: 'TODO'
            });
            await loadTasks();
        } catch (error) {
            console.error('일정 추가 실패:', error);
            alert('일정 추가에 실패했습니다.');
        }
    };

    // 일정 삭제
    const handleDeleteTask = async (taskId: number) => {
        if (!window.confirm('정말 삭제하시겠습니까?')) return;

        try {
            if (kind === 'study') {
                await studyTaskService.deleteTask(taskId);
            } else {
                await teamTaskService.deleteTask(taskId);
            }
            alert('삭제되었습니다.');
            await loadTasks();
        } catch (error) {
            console.error('일정 삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    };

    // 일정 상태 변경
    const handleTaskStatusChange = async (taskId: number, status: TaskStatus) => {
        try {
            if (kind === 'study') {
                await studyTaskService.updateTaskStatus(taskId, status);
            } else {
                await teamTaskService.updateTaskStatus(taskId, status);
            }
            await loadTasks();
        } catch (error) {
            console.error('상태 변경 실패:', error);
            alert('상태 변경에 실패했습니다.');
        }
    };

    // 삭제
    const handleDelete = async () => {
        if (!window.confirm('정말 삭제하시겠습니까?')) return;

        try {
            if (kind === 'study') {
                await studyService.deleteStudy(Number(id));
            } else {
                await teamService.deleteTeam(Number(id));
            }
            alert('삭제되었습니다.');
            navigate(`/mygroups/${kind}`);
        } catch (error) {
            console.error('삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    };

    // 나가기
    const handleLeave = async () => {
        if (!window.confirm('정말 탈퇴하시겠습니까?')) return;

        try {
            if (kind === 'study') {
                await studyService.leaveStudy(Number(id));
            } else {
                await teamService.leaveTeam(Number(id));
            }
            alert('탈퇴되었습니다.');
            navigate(`/mygroups/${kind}`);
        } catch (error) {
            console.error('탈퇴 실패:', error);
            alert('탈퇴에 실패했습니다.');
        }
    };

    useEffect(() => {
        loadGroupDetail();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id, kind]);

    if (loading || !group) {
        return (
            <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                로딩 중...
            </div>
        );
    }

    const orderedMembers = [...members].sort((a, b) => {
        if (a.isLeader && !b.isLeader) return -1;
        if (!a.isLeader && b.isLeader) return 1;
        return 0;
    });

    // 일정 필터링
    const filteredTasks = tasks.filter(task => {
        if (selectedTaskTab === '전체') return true;
        return task.status === selectedTaskTab;
    });

    // 상태 레이블 및 색상
    const getStatusLabel = (status: TaskStatus) => {
        switch (status) {
            case 'TODO': return '예정';
            case 'IN_PROGRESS': return '진행중';
            case 'DONE': return '완료';
            default: return status;
        }
    };

    const getStatusColor = (status: TaskStatus) => {
        switch (status) {
            case 'TODO': return { bg: 'var(--secondary-container)', color: 'var(--secondary)' };
            case 'IN_PROGRESS': return { bg: 'var(--primary-container)', color: 'var(--primary)' };
            case 'DONE': return { bg: 'var(--tertiary-container)', color: 'var(--tertiary)' };
            default: return { bg: 'var(--surface-variant)', color: 'var(--on-surface-variant)' };
        }
    };

    return (
        <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--background)' }}>
            {/* Top Bar */}
            <div style={{
                padding: '16px 20px',
                borderBottom: '1px solid var(--border-color)',
                backgroundColor: 'var(--surface)',
                display: 'flex',
                alignItems: 'center',
                gap: '12px'
            }}>
                <button
                    onClick={() => navigate(-1)}
                    style={{
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        padding: 0,
                        display: 'flex',
                        alignItems: 'center'
                    }}
                >
                    <MdArrowBack size={24} />
                </button>
                <h1 style={{ flex: 1, fontSize: '18px', fontWeight: 'bold', margin: 0 }}>
                    그룹 상세
                </h1>
                <div style={{ position: 'relative' }}>
                    <button
                        onClick={() => setShowMenu(!showMenu)}
                        style={{
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '8px',
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        <MdMoreVert size={24} />
                    </button>
                    {showMenu && (
                        <div style={{
                            position: 'absolute',
                            right: 0,
                            top: '100%',
                            backgroundColor: 'var(--surface)',
                            border: '1px solid var(--border-color)',
                            borderRadius: '8px',
                            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                            minWidth: '150px',
                            zIndex: 1000
                        }}>
                            {isLeader && (
                                <>
                                    <button
                                        onClick={() => {
                                            setShowMenu(false);
                                            navigate(`/groups/${kind}/edit/${id}`);
                                        }}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '8px',
                                            width: '100%',
                                            padding: '12px 16px',
                                            border: 'none',
                                            background: 'none',
                                            cursor: 'pointer',
                                            fontSize: '14px',
                                            textAlign: 'left',
                                            borderBottom: '1px solid var(--border-color)'
                                        }}
                                    >
                                        <MdEdit size={18} />
                                        <span>수정하기</span>
                                    </button>
                                    <button
                                        onClick={() => {
                                            setShowMenu(false);
                                            navigate(`/member_manage/${kind}/${id}`);
                                        }}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '8px',
                                            width: '100%',
                                            padding: '12px 16px',
                                            border: 'none',
                                            background: 'none',
                                            cursor: 'pointer',
                                            fontSize: '14px',
                                            textAlign: 'left',
                                            borderBottom: '1px solid var(--border-color)'
                                        }}
                                    >
                                        <MdManageAccounts size={18} />
                                        <span>멤버 관리</span>
                                    </button>
                                    <button
                                        onClick={() => {
                                            setShowMenu(false);
                                            handleDelete();
                                        }}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '8px',
                                            width: '100%',
                                            padding: '12px 16px',
                                            border: 'none',
                                            background: 'none',
                                            cursor: 'pointer',
                                            fontSize: '14px',
                                            textAlign: 'left',
                                            color: 'var(--error)',
                                            borderBottom: '1px solid var(--border-color)'
                                        }}
                                    >
                                        <MdDelete size={18} />
                                        <span>삭제하기</span>
                                    </button>
                                </>
                            )}
                            <button
                                onClick={() => {
                                    setShowMenu(false);
                                    handleLeave();
                                }}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    width: '100%',
                                    padding: '12px 16px',
                                    border: 'none',
                                    background: 'none',
                                    cursor: 'pointer',
                                    fontSize: '14px',
                                    textAlign: 'left',
                                    color: 'var(--error)'
                                }}
                            >
                                <MdClose size={18} />
                                <span>탈퇴하기</span>
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Content */}
            < div style={{ flex: 1, overflowY: 'auto', padding: '24px' }}>
                {/* 그룹 정보 */}
                < div style={{
                    backgroundColor: 'var(--surface)',
                    borderRadius: '24px',
                    padding: '24px',
                    marginBottom: '32px',
                    border: '1px solid var(--border-color)'
                }}>
                    <div style={{
                        display: 'inline-block',
                        padding: '4px 10px',
                        borderRadius: '8px',
                        backgroundColor: 'var(--primary-container)',
                        color: 'var(--primary)',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        marginBottom: '12px'
                    }}>
                        진행중
                    </div>
                    <h2 style={{ fontSize: '20px', fontWeight: 'bold', margin: '0 0 20px 0' }}>
                        {group.title}
                    </h2>

                    <h3 style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--on-surface-variant)', marginBottom: '12px' }}>
                        멤버 목록
                    </h3>
                    {
                        members.length === 0 ? (
                            <p style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                                멤버 정보가 없습니다.
                            </p>
                        ) : (
                            <div>
                                {orderedMembers.map((member, index) => (
                                    <div key={member.id}>
                                        <div style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            padding: '12px 0',
                                            gap: '12px'
                                        }}>
                                            <div style={{
                                                width: '36px',
                                                height: '36px',
                                                borderRadius: '50%',
                                                backgroundColor: 'var(--surface-variant)',
                                                overflow: 'hidden',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center'
                                            }}>
                                                {member.profileImageUrl ? (
                                                    <img
                                                        src={member.profileImageUrl}
                                                        alt=""
                                                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                                    />
                                                ) : (
                                                    <svg width="36" height="36" viewBox="0 0 24 24" fill="var(--on-surface-variant)">
                                                        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                                                    </svg>
                                                )}
                                            </div>
                                            <div style={{ flex: 1 }}>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                    <span style={{ fontWeight: 'bold', fontSize: '14px' }}>
                                                        {member.name}
                                                    </span>
                                                    {member.isLeader && (
                                                        <span style={{
                                                            padding: '1px 4px',
                                                            borderRadius: '4px',
                                                            backgroundColor: 'var(--primary-container)',
                                                            color: 'var(--primary)',
                                                            fontSize: '10px'
                                                        }}>
                                                            팀장
                                                        </span>
                                                    )}
                                                    {member.portfolioId && (
                                                        <span
                                                            onClick={() => navigate(`/portfolio/${member.portfolioId}`)}
                                                            style={{
                                                                cursor: 'pointer',
                                                                fontSize: '10px',
                                                                color: 'var(--primary)',
                                                                border: '1px solid var(--primary)',
                                                                padding: '0 4px',
                                                                borderRadius: '4px',
                                                                marginLeft: '4px'
                                                            }}
                                                        >
                                                            P
                                                        </span>
                                                    )}
                                                </div>
                                                <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>
                                                    {member.email || member.name}
                                                </div>
                                            </div>
                                        </div>
                                        {index < orderedMembers.length - 1 && (
                                            <div style={{ height: '1px', backgroundColor: 'var(--border-color)', opacity: 0.3 }} />
                                        )}
                                    </div>
                                ))}
                            </div>
                        )
                    }
                </div >

                {/* 공지사항 */}
                < div style={{ marginBottom: '32px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', margin: 0 }}>공지사항</h3>
                        <button
                            onClick={() => console.log('전체보기')}
                            style={{
                                background: 'none',
                                border: 'none',
                                cursor: 'pointer',
                                fontSize: '12px',
                                color: 'var(--on-surface-variant)'
                            }}
                        >
                            전체보기
                        </button>
                    </div>
                    {
                        notices.length === 0 ? (
                            <div style={{
                                backgroundColor: 'var(--surface)',
                                borderRadius: '16px',
                                border: '1px solid var(--border-color)'
                            }}>
                                <div style={{ padding: '16px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    <MdCampaign size={24} style={{ color: 'var(--on-surface-variant)' }} />
                                    <div>
                                        <p style={{ fontSize: '14px', fontWeight: '500', margin: 0 }}>
                                            등록된 공지사항이 없습니다.
                                        </p>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                {notices.slice(0, 2).map((notice) => (
                                    <div key={notice.id} style={{
                                        backgroundColor: 'var(--surface)',
                                        borderRadius: '16px',
                                        padding: '16px',
                                        border: '1px solid var(--border-color)'
                                    }}>
                                        {notice.isPinned && (
                                            <span style={{
                                                display: 'inline-block',
                                                padding: '2px 8px',
                                                borderRadius: '6px',
                                                backgroundColor: 'var(--primary-container)',
                                                color: 'var(--primary)',
                                                fontSize: '11px',
                                                fontWeight: 'bold',
                                                marginBottom: '8px'
                                            }}>
                                                고정
                                            </span>
                                        )}
                                        <h4 style={{ fontSize: '14px', fontWeight: 'bold', margin: '0 0 8px 0' }}>
                                            {notice.title}
                                        </h4>
                                        <p style={{ fontSize: '13px', color: 'var(--on-surface-variant)', margin: 0, whiteSpace: 'pre-line' }}>
                                            {notice.content}
                                        </p>
                                        <p style={{ fontSize: '11px', color: 'var(--on-surface-variant)', marginTop: '8px', margin: 0 }}>
                                            {new Date(notice.createdAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        )
                    }
                </div >

                {/* 일정 */}
                < div >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', margin: 0 }}>일정</h3>
                        <button
                            onClick={() => setShowTaskModal(true)}
                            style={{
                                background: 'none',
                                border: 'none',
                                cursor: 'pointer',
                                padding: 0,
                                display: 'flex',
                                alignItems: 'center'
                            }}
                        >
                            <MdAddCircle size={28} style={{ color: 'var(--primary)' }} />
                        </button>
                    </div>

                    {/* 탭 */}
                    <div style={{
                        display: 'flex',
                        borderBottom: '2px solid var(--border-color)',
                        marginBottom: '12px'
                    }}>
                        {(['전체', 'TODO', 'IN_PROGRESS', 'DONE'] as const).map((tab) => (
                            <button
                                key={tab}
                                onClick={() => setSelectedTaskTab(tab)}
                                style={{
                                    flex: 1,
                                    padding: '12px',
                                    background: 'none',
                                    border: 'none',
                                    cursor: 'pointer',
                                    fontSize: '13px',
                                    borderBottom: selectedTaskTab === tab ? '2px solid var(--primary)' : 'none',
                                    color: selectedTaskTab === tab ? 'var(--primary)' : 'var(--on-surface-variant)',
                                    marginBottom: '-2px'
                                }}
                            >
                                {tab === '전체' ? '전체' : getStatusLabel(tab)}
                            </button>
                        ))}
                    </div>

                    {/* 일정 목록 */}
                    {
                        filteredTasks.length === 0 ? (
                            <div style={{ padding: '8px 0' }}>
                                <p style={{ fontSize: '14px', fontWeight: 'bold', margin: '0 0 6px 0' }}>일정 없음</p>
                                <p style={{ fontSize: '13px', color: 'var(--on-surface-variant)', margin: 0 }}>
                                    등록된 일정이 없습니다.
                                </p>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                {filteredTasks.map((task) => {
                                    const statusColor = getStatusColor(task.status);
                                    return (
                                        <div key={task.id} style={{
                                            backgroundColor: 'var(--surface)',
                                            borderRadius: '16px',
                                            padding: '16px',
                                            border: '1px solid var(--border-color)'
                                        }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                                                <span style={{
                                                    padding: '4px 12px',
                                                    borderRadius: '8px',
                                                    backgroundColor: statusColor.bg,
                                                    color: statusColor.color,
                                                    fontSize: '12px',
                                                    fontWeight: 'bold'
                                                }}>
                                                    {getStatusLabel(task.status)}
                                                </span>
                                                {isLeader && (
                                                    <button
                                                        onClick={() => handleDeleteTask(task.id)}
                                                        style={{
                                                            background: 'none',
                                                            border: 'none',
                                                            cursor: 'pointer',
                                                            padding: '4px',
                                                            color: 'var(--error)'
                                                        }}
                                                    >
                                                        <MdDelete size={18} />
                                                    </button>
                                                )}
                                            </div>
                                            <h4 style={{ fontSize: '15px', fontWeight: 'bold', margin: '0 0 8px 0' }}>
                                                {task.title}
                                            </h4>
                                            <p style={{ fontSize: '13px', color: 'var(--on-surface-variant)', margin: '0 0 8px 0' }}>
                                                {task.startDate} ~ {task.endDate}
                                            </p>
                                            {task.content && (
                                                <p style={{ fontSize: '13px', color: 'var(--on-surface-variant)', margin: '0 0 12px 0', whiteSpace: 'pre-line' }}>
                                                    {task.content}
                                                </p>
                                            )}
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                {(['TODO', 'IN_PROGRESS', 'DONE'] as TaskStatus[]).map((status) => (
                                                    <button
                                                        key={status}
                                                        onClick={() => handleTaskStatusChange(task.id, status)}
                                                        disabled={task.status === status}
                                                        style={{
                                                            padding: '6px 12px',
                                                            borderRadius: '8px',
                                                            border: 'none',
                                                            backgroundColor: task.status === status ? 'var(--surface-variant)' : 'var(--primary-container)',
                                                            color: task.status === status ? 'var(--on-surface-variant)' : 'var(--primary)',
                                                            fontSize: '11px',
                                                            cursor: task.status === status ? 'not-allowed' : 'pointer',
                                                            opacity: task.status === status ? 0.5 : 1
                                                        }}
                                                    >
                                                        {getStatusLabel(status)}
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )
                    }
                </div >
            </div >

            {/* Task Add Modal */}
            {
                showTaskModal && (
                    <div style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 2000
                    }}>
                        <div style={{
                            backgroundColor: 'var(--surface)',
                            borderRadius: '24px',
                            padding: '24px',
                            width: '90%',
                            maxWidth: '500px',
                            maxHeight: '80vh',
                            overflowY: 'auto'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                                <h3 style={{ fontSize: '18px', fontWeight: 'bold', margin: 0 }}>일정 추가</h3>
                                <button
                                    onClick={() => setShowTaskModal(false)}
                                    style={{
                                        background: 'none',
                                        border: 'none',
                                        cursor: 'pointer',
                                        padding: 0,
                                        display: 'flex'
                                    }}
                                >
                                    <MdClose size={24} />
                                </button>
                            </div>

                            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                                <div>
                                    <label style={{ fontSize: '14px', fontWeight: '600', color: 'var(--on-surface)', marginBottom: '8px', display: 'block' }}>제목 *</label>
                                    <input
                                        type="text"
                                        placeholder="일정 제목을 입력하세요"
                                        value={taskForm.title}
                                        onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })}
                                        style={{
                                            width: '100%',
                                            padding: '14px 16px',
                                            borderRadius: '12px',
                                            border: '1px solid var(--border-color)',
                                            fontSize: '15px',
                                            backgroundColor: 'var(--field-bg)',
                                            color: 'var(--on-surface)',
                                            boxSizing: 'border-box',
                                            outline: 'none',
                                            transition: 'border-color 0.2s'
                                        }}
                                        onFocus={(e) => e.target.style.borderColor = 'var(--primary)'}
                                        onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                                    />
                                </div>

                                <div>
                                    <label style={{ fontSize: '14px', fontWeight: '600', color: 'var(--on-surface)', marginBottom: '8px', display: 'block' }}>내용</label>
                                    <textarea
                                        placeholder="상세 내용을 입력하세요 (선택)"
                                        value={taskForm.content}
                                        onChange={(e) => setTaskForm({ ...taskForm, content: e.target.value })}
                                        style={{
                                            width: '100%',
                                            padding: '14px 16px',
                                            borderRadius: '12px',
                                            border: '1px solid var(--border-color)',
                                            fontSize: '15px',
                                            minHeight: '100px',
                                            resize: 'vertical',
                                            backgroundColor: 'var(--field-bg)',
                                            color: 'var(--on-surface)',
                                            boxSizing: 'border-box',
                                            outline: 'none',
                                            transition: 'border-color 0.2s'
                                        }}
                                        onFocus={(e) => e.target.style.borderColor = 'var(--primary)'}
                                        onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                                    />
                                </div>

                                <div style={{ display: 'flex', gap: '16px' }}>
                                    <div style={{ flex: 1 }}>
                                        <label style={{ fontSize: '14px', fontWeight: '600', color: 'var(--on-surface)', marginBottom: '8px', display: 'block' }}>시작일 *</label>
                                        <input
                                            type="date"
                                            value={taskForm.startDate}
                                            onChange={(e) => setTaskForm({ ...taskForm, startDate: e.target.value })}
                                            style={{
                                                width: '100%',
                                                padding: '14px 16px',
                                                borderRadius: '12px',
                                                border: '1px solid var(--border-color)',
                                                fontSize: '15px',
                                                backgroundColor: 'var(--field-bg)',
                                                color: 'var(--on-surface)',
                                                boxSizing: 'border-box',
                                                outline: 'none'
                                            }}
                                        />
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <label style={{ fontSize: '14px', fontWeight: '600', color: 'var(--on-surface)', marginBottom: '8px', display: 'block' }}>종료일 *</label>
                                        <input
                                            type="date"
                                            value={taskForm.endDate}
                                            onChange={(e) => setTaskForm({ ...taskForm, endDate: e.target.value })}
                                            style={{
                                                width: '100%',
                                                padding: '14px 16px',
                                                borderRadius: '12px',
                                                border: '1px solid var(--border-color)',
                                                fontSize: '15px',
                                                backgroundColor: 'var(--field-bg)',
                                                color: 'var(--on-surface)',
                                                boxSizing: 'border-box',
                                                outline: 'none'
                                            }}
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label style={{ fontSize: '14px', fontWeight: '600', color: 'var(--on-surface)', marginBottom: '8px', display: 'block' }}>상태</label>
                                    <select
                                        value={taskForm.status}
                                        onChange={(e) => setTaskForm({ ...taskForm, status: e.target.value as TaskStatus })}
                                        style={{
                                            width: '100%',
                                            padding: '14px 16px',
                                            borderRadius: '12px',
                                            border: '1px solid var(--border-color)',
                                            fontSize: '15px',
                                            backgroundColor: 'var(--field-bg)',
                                            color: 'var(--on-surface)',
                                            boxSizing: 'border-box',
                                            outline: 'none',
                                            appearance: 'none',
                                            backgroundImage: 'url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2224%22%20height%3D%2224%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%23666%22%20stroke-width%3D%222%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22%3E%3Cpolyline%20points%3D%226%209%2012%2015%2018%209%22%3E%3C%2Fpolyline%3E%3C%2Fsvg%3E")',
                                            backgroundRepeat: 'no-repeat',
                                            backgroundPosition: 'right 16px center',
                                            backgroundSize: '16px'
                                        }}
                                    >
                                        <option value="TODO">예정</option>
                                        <option value="IN_PROGRESS">진행중</option>
                                        <option value="DONE">완료</option>
                                    </select>
                                </div>

                                <div style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
                                    <button
                                        onClick={() => setShowTaskModal(false)}
                                        style={{
                                            flex: 1,
                                            padding: '14px',
                                            borderRadius: '12px',
                                            border: '1px solid var(--border-color)',
                                            backgroundColor: 'var(--surface)',
                                            color: 'var(--on-surface)',
                                            fontSize: '16px',
                                            fontWeight: 'bold',
                                            cursor: 'pointer',
                                            transition: 'background-color 0.2s'
                                        }}
                                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--surface-variant)'}
                                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'var(--surface)'}
                                    >
                                        취소
                                    </button>
                                    <button
                                        onClick={handleAddTask}
                                        style={{
                                            flex: 1,
                                            padding: '14px',
                                            borderRadius: '12px',
                                            border: 'none',
                                            backgroundColor: 'var(--primary)',
                                            color: 'white',
                                            fontSize: '16px',
                                            fontWeight: 'bold',
                                            cursor: 'pointer',
                                            boxShadow: '0 4px 10px rgba(91, 127, 255, 0.2)'
                                        }}
                                    >
                                        추가
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )
            }
        </div >
    );
};

export default MyGroupDetailPage;
