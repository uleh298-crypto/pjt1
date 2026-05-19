import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdArrowBack, MdCheck } from 'react-icons/md';
import { studyService, teamService, boardService } from '../../services/api';
import { studyTypeLabels, teamTypeLabels } from '../../utils/groupUtils';
import type { GroupCreateRequest, Campus } from '../../services/api';

interface GroupWritePageProps {
    kind: 'study' | 'team';
}

const GroupWritePage: React.FC<GroupWritePageProps> = ({ kind }) => {
    const navigate = useNavigate();
    const [campuses, setCampuses] = useState<Campus[]>([]);
    const [formData, setFormData] = useState<GroupCreateRequest>({
        title: '',
        type: '',
        capacity: 4,
        startDate: '',
        endDate: '',
        campusId: 0,
        description: ''
    });
    const [submitting, setSubmitting] = useState(false);

    const typeLabels = kind === 'study' ? studyTypeLabels : teamTypeLabels;
    // '전체' 제외한 타입 목록
    const typeOptions = Object.keys(typeLabels).filter(label => label !== '전체');

    const loadCampuses = async () => {
        try {
            // Campus 정보는 실제 API를 통해 가져와야 함 (임시 하드코딩 유지하되 UI만 개선)
            setCampuses([
                { id: 1, name: '서울' },
                { id: 2, name: '대전' },
                { id: 3, name: '구미' },
                { id: 4, name: '광주' },
                { id: 5, name: '부울경' }
            ]);
        } catch (error) {
            console.error('캠퍼스 목록 로딩 실패:', error);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // 유효성 검사
        if (!formData.title.trim()) {
            alert('제목을 입력하세요.');
            return;
        }
        if (!formData.type) {
            alert('타입을 선택하세요.');
            return;
        }
        if (formData.capacity < 2) {
            alert('정원은 2명 이상이어야 합니다.');
            return;
        }
        if (!formData.startDate || !formData.endDate) {
            alert('기간을 입력하세요.');
            return;
        }
        if (formData.startDate > formData.endDate) {
            alert('종료일은 시작일 이후여야 합니다.');
            return;
        }
        if (!formData.campusId) {
            alert('캠퍼스를 선택하세요.');
            return;
        }
        if (!formData.description.trim()) {
            alert('설명을 입력하세요.');
            return;
        }

        setSubmitting(true);
        try {
            // API 전송 시에는 라벨이 아닌 매핑된 영문 타입값 사용
            const apiData = {
                ...formData,
                type: typeLabels[formData.type]
            };

            if (kind === 'study') {
                await studyService.createStudy(apiData);
            } else {
                await teamService.createTeam(apiData);
            }

            alert('생성되었습니다!');
            navigate(`/groups/${kind}`);
        } catch (error) {
            console.error('생성 실패:', error);
            alert('생성에 실패했습니다.');
        } finally {
            setSubmitting(false);
        }
    };

    useEffect(() => {
        loadCampuses();
    }, []);

    const labelStyle: React.CSSProperties = {
        display: 'block',
        marginBottom: '10px',
        fontWeight: '600',
        fontSize: '14px',
        color: 'var(--on-surface-variant)'
    };

    const inputStyle: React.CSSProperties = {
        width: '100%',
        padding: '12px 16px',
        border: '1px solid var(--border-color)',
        borderRadius: '12px',
        fontSize: '15px',
        backgroundColor: 'var(--surface)',
        color: 'var(--on-surface)',
        outline: 'none',
        transition: 'border-color 0.2s'
    };

    return (
        <div style={{ backgroundColor: 'var(--background)', minHeight: '100vh', padding: '40px 20px' }}>
            <div className="container" style={{ maxWidth: '800px' }}>
                {/* Page Header */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '32px' }}>
                    <div
                        onClick={() => navigate(-1)}
                        style={{
                            padding: '8px',
                            borderRadius: '12px',
                            cursor: 'pointer',
                            backgroundColor: 'var(--surface)',
                            border: '1px solid var(--border-color)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}
                    >
                        <MdArrowBack size={24} />
                    </div>
                    <div>
                        <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: '0 0 4px 0' }}>
                            {kind === 'study' ? '스터디' : '프로젝트'} 만들기
                        </h1>
                        <p style={{ margin: 0, color: 'var(--on-surface-variant)', fontSize: '14px' }}>
                            함께 성장할 동료를 찾아보세요.
                        </p>
                    </div>
                </div>

                {/* Main Form Content */}
                <form
                    onSubmit={handleSubmit}
                    style={{
                        backgroundColor: 'var(--surface)',
                        padding: '40px',
                        borderRadius: '24px',
                        border: '1px solid var(--border-color)',
                        boxShadow: '0 4px 20px rgba(0,0,0,0.05)'
                    }}
                >
                    {/* 제목 */}
                    <div style={{ marginBottom: '32px' }}>
                        <label style={labelStyle}>모집 제목 *</label>
                        <input
                            type="text"
                            value={formData.title}
                            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                            placeholder="팀원들이 한눈에 알아볼 수 있는 제목을 입력하세요"
                            style={inputStyle}
                        />
                    </div>

                    {/* 2-Column Grid Area */}
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                        gap: '32px',
                        marginBottom: '32px'
                    }}>
                        {/* 타입 */}
                        <div>
                            <label style={labelStyle}>카테고리 *</label>
                            <select
                                value={formData.type}
                                onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                                style={inputStyle}
                            >
                                <option value="">선택해주세요</option>
                                {typeOptions.map((type) => (
                                    <option key={type} value={type}>{type}</option>
                                ))}
                            </select>
                        </div>

                        {/* 정원 */}
                        <div>
                            <label style={labelStyle}>모집 인원 (본인 포함) *</label>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <input
                                    type="number"
                                    min="2"
                                    max="20"
                                    value={formData.capacity}
                                    onChange={(e) => setFormData({ ...formData, capacity: Number(e.target.value) })}
                                    style={inputStyle}
                                />
                                <span style={{ whiteSpace: 'nowrap', fontSize: '14px', color: 'var(--on-surface-variant)' }}>명</span>
                            </div>
                        </div>
                    </div>

                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                        gap: '32px',
                        marginBottom: '32px'
                    }}>
                        {/* 시작일 */}
                        <div>
                            <label style={labelStyle}>활동 시작 예정일 *</label>
                            <input
                                type="date"
                                value={formData.startDate}
                                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                                style={inputStyle}
                            />
                        </div>

                        {/* 종료일 */}
                        <div>
                            <label style={labelStyle}>활동 종료 예정일 *</label>
                            <input
                                type="date"
                                value={formData.endDate}
                                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                                style={inputStyle}
                            />
                        </div>
                    </div>

                    {/* 캠퍼스 */}
                    <div style={{ marginBottom: '32px' }}>
                        <label style={labelStyle}>모집 캠퍼스 *</label>
                        <select
                            value={formData.campusId}
                            onChange={(e) => setFormData({ ...formData, campusId: Number(e.target.value) })}
                            style={inputStyle}
                        >
                            <option value={0}>지역 선택</option>
                            {campuses.map((campus) => (
                                <option key={campus.id} value={campus.id}>{campus.name} 캠퍼스</option>
                            ))}
                        </select>
                    </div>

                    {/* 설명 */}
                    <div style={{ marginBottom: '40px' }}>
                        <label style={labelStyle}>상세 설명 *</label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            placeholder="활동 계획, 모집 대상, 우대 사항 등을 자유롭게 작성해주세요"
                            rows={10}
                            style={{
                                ...inputStyle,
                                resize: 'vertical',
                                minHeight: '200px',
                                lineHeight: '1.6'
                            }}
                        />
                    </div>

                    {/* Buttons Section */}
                    <div style={{
                        display: 'flex',
                        justifyContent: 'flex-end',
                        gap: '12px',
                        paddingTop: '20px',
                        borderTop: '1px solid var(--border-color)'
                    }}>
                        <button
                            type="button"
                            onClick={() => navigate(-1)}
                            style={{
                                padding: '14px 32px',
                                backgroundColor: 'transparent',
                                color: 'var(--on-surface-variant)',
                                border: '1px solid var(--border-color)',
                                borderRadius: '12px',
                                fontSize: '16px',
                                fontWeight: '600',
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                            }}
                        >
                            취소
                        </button>
                        <button
                            type="submit"
                            disabled={submitting}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                padding: '14px 48px',
                                backgroundColor: 'var(--primary)',
                                color: 'white',
                                border: 'none',
                                borderRadius: '12px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                cursor: submitting ? 'not-allowed' : 'pointer',
                                opacity: submitting ? 0.7 : 1,
                                transition: 'all 0.2s',
                                boxShadow: '0 4px 12px rgba(100, 149, 235, 0.3)'
                            }}
                        >
                            {!submitting && <MdCheck size={20} />}
                            {submitting ? '생성 중...' : `${kind === 'study' ? '스터디' : '프로젝트'} 생성하기`}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default GroupWritePage;
