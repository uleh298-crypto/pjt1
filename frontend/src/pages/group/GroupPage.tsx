import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { MdSearch, MdClose, MdAdd } from 'react-icons/md';
import { studyService, teamService } from '../../services/api';
import type { GroupSummary } from '../../services/api';
import GroupCard from './components/GroupCard';
import FilterChips from './components/FilterChips';
import RecentSearchChip from './components/RecentSearchChip';
import {
    studyFilters,
    teamFilters,
    studyTypeLabels,
    teamTypeLabels,
    getRecentKeywords,
    addRecentKeyword,
    deleteRecentKeyword
} from '../../utils/groupUtils';

const GroupPage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // URL 경로에서 kind 추출 (스터디/프로젝트)
    const isStudyPath = location.pathname.includes('/study');
    const kind: 'study' | 'team' = isStudyPath ? 'study' : 'team';

    const [groups, setGroups] = useState<GroupSummary[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [committedQuery, setCommittedQuery] = useState('');
    const [selectedFilter, setSelectedFilter] = useState('전체');
    const [recentKeywords, setRecentKeywords] = useState<string[]>([]);
    const [showMinLengthError, setShowMinLengthError] = useState(false);
    const [isSearchFocused, setIsSearchFocused] = useState(false);

    const title = kind === 'study' ? '스터디' : '프로젝트';
    const filters = kind === 'study' ? studyFilters : teamFilters;
    const typeLabels = kind === 'study' ? studyTypeLabels : teamTypeLabels;

    const loadGroups = async () => {
        setLoading(true);
        try {
            const typeParam = typeLabels[selectedFilter];
            const result = kind === 'study' ?
                await studyService.getStudies(undefined, typeParam) :
                await teamService.getTeams(undefined, typeParam);
            setGroups(result);
        } catch (error) {
            console.error('그룹 목록 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearchSubmit = () => {
        const trimmed = searchQuery.trim();
        if (trimmed && trimmed.length < 2) {
            setShowMinLengthError(true);
            return;
        }
        setCommittedQuery(trimmed);
        if (trimmed) {
            addRecentKeyword(trimmed);
            setRecentKeywords(getRecentKeywords());
        }
        setShowMinLengthError(false);
    };

    const clearSearch = () => {
        setSearchQuery('');
        setCommittedQuery('');
        setShowMinLengthError(false);
    };

    const filteredGroups = React.useMemo(() => {
        if (committedQuery === '') return groups;
        return groups.filter(g =>
            g.title.toLowerCase().includes(committedQuery.toLowerCase()) ||
            g.description?.toLowerCase().includes(committedQuery.toLowerCase())
        );
    }, [groups, committedQuery]);

    const sortedGroups = React.useMemo(() => {
        const active = filteredGroups.filter(g => g.status !== 'CLOSED');
        const closed = filteredGroups.filter(g => g.status === 'CLOSED');
        return [...active, ...closed];
    }, [filteredGroups]);

    useEffect(() => {
        loadGroups();
        setRecentKeywords(getRecentKeywords());
    }, [kind, selectedFilter]);

    return (
        <div style={{ minHeight: '100vh', backgroundColor: 'var(--background)' }}>
            {/* Header Area */}
            <div style={{
                backgroundColor: 'var(--surface)',
                borderBottom: '1px solid var(--border-color)',
                position: 'sticky',
                top: 0,
                zIndex: 100
            }}>
                <div className="container" style={{ padding: '20px' }}>
                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        flexWrap: 'wrap',
                        gap: '20px'
                    }}>
                        {/* Title & Tabs */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '40px' }}>
                            <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>그룹 찾기</h1>
                            <div style={{ display: 'flex', gap: '20px' }}>
                                <div
                                    onClick={() => navigate('/groups/study')}
                                    style={{
                                        fontSize: '18px',
                                        fontWeight: kind === 'study' ? 'bold' : 'normal',
                                        color: kind === 'study' ? 'var(--primary)' : 'var(--on-surface-variant)',
                                        cursor: 'pointer',
                                        padding: '8px 0',
                                        borderBottom: kind === 'study' ? '3px solid var(--primary)' : 'none'
                                    }}
                                >
                                    스터디
                                </div>
                                <div
                                    onClick={() => navigate('/groups/team')}
                                    style={{
                                        fontSize: '18px',
                                        fontWeight: kind === 'team' ? 'bold' : 'normal',
                                        color: kind === 'team' ? 'var(--primary)' : 'var(--on-surface-variant)',
                                        cursor: 'pointer',
                                        padding: '8px 0',
                                        borderBottom: kind === 'team' ? '3px solid var(--primary)' : 'none'
                                    }}
                                >
                                    프로젝트
                                </div>
                            </div>
                        </div>

                        {/* Search & Actions */}
                        <div style={{ display: 'flex', gap: '16px', alignItems: 'center', flex: 1, maxWidth: '600px', justifyContent: 'flex-end' }}>
                            <div style={{
                                position: 'relative',
                                flex: 1,
                                maxWidth: '400px',
                                display: 'flex',
                                alignItems: 'center',
                                backgroundColor: 'var(--field-bg)',
                                borderRadius: '12px',
                                padding: '0 16px',
                                border: isSearchFocused ? '2px solid var(--primary)' : '2px solid transparent',
                                transition: 'all 0.2s'
                            }}>
                                <MdSearch size={24} color="var(--on-surface-variant)" />
                                <input
                                    type="text"
                                    placeholder={`${title} 검색...`}
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    onFocus={() => setIsSearchFocused(true)}
                                    onBlur={() => setTimeout(() => setIsSearchFocused(false), 200)}
                                    onKeyPress={(e) => e.key === 'Enter' && handleSearchSubmit()}
                                    style={{
                                        flex: 1,
                                        border: 'none',
                                        backgroundColor: 'transparent',
                                        padding: '12px 8px',
                                        fontSize: '15px',
                                        outline: 'none',
                                        color: 'var(--on-surface)'
                                    }}
                                />
                                {searchQuery && (
                                    <MdClose
                                        size={20}
                                        onClick={clearSearch}
                                        style={{ cursor: 'pointer', color: 'var(--on-surface-variant)' }}
                                    />
                                )}
                            </div>

                            <button
                                onClick={() => navigate(`/groups/${kind}/write`)}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    padding: '12px 24px',
                                    backgroundColor: 'var(--primary)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '12px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    whiteSpace: 'nowrap',
                                    boxShadow: '0 4px 12px rgba(100, 149, 235, 0.2)'
                                }}
                            >
                                <MdAdd size={20} />
                                {title} 만들기
                            </button>
                        </div>
                    </div>

                    {/* Filter Area */}
                    <div style={{ marginTop: '20px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <span style={{ fontSize: '14px', color: 'var(--on-surface-variant)', fontWeight: '600' }}>필터:</span>
                        <FilterChips
                            filters={filters}
                            selectedFilter={selectedFilter}
                            onFilterChange={setSelectedFilter}
                        />
                    </div>
                </div>
            </div>

            {/* Main Content Area */}
            <div className="container" style={{ padding: '40px 20px' }}>
                {showMinLengthError && (
                    <div style={{ marginBottom: '20px', color: 'var(--error)', fontSize: '14px' }}>
                        두 글자 이상 입력해 주세요.
                    </div>
                )}

                {/* Recent Searches (Only when focused and query is empty) */}
                {isSearchFocused && !searchQuery && recentKeywords.length > 0 && (
                    <div style={{
                        marginBottom: '30px',
                        padding: '20px',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '16px',
                        border: '1px solid var(--border-color)'
                    }}>
                        <h3 style={{ fontSize: '16px', margin: '0 0 16px 0' }}>최근 검색어</h3>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                            {recentKeywords.map(keyword => (
                                <RecentSearchChip
                                    key={keyword}
                                    keyword={keyword}
                                    onDelete={() => {
                                        deleteRecentKeyword(keyword);
                                        setRecentKeywords(getRecentKeywords());
                                    }}
                                    onClick={() => {
                                        setSearchQuery(keyword);
                                        setCommittedQuery(keyword);
                                    }}
                                />
                            ))}
                        </div>
                    </div>
                )}

                {loading ? (
                    <div style={{ textAlign: 'center', padding: '100px 0' }}>
                        <div className="loading-spinner" />
                        <p style={{ marginTop: '16px', color: 'var(--on-surface-variant)' }}>데이터를 불러오는 중입니다...</p>
                    </div>
                ) : sortedGroups.length === 0 ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '100px 0',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '24px',
                        border: '1px dashed var(--border-color)'
                    }}>
                        <p style={{ fontSize: '18px', color: 'var(--on-surface-variant)' }}>
                            {committedQuery ? '검색 결과가 없습니다.' : `등록된 ${title}가 없습니다.`}
                        </p>
                    </div>
                ) : (
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
                        gap: '24px'
                    }}>
                        {sortedGroups.map((group) => (
                            <GroupCard
                                key={group.id}
                                group={group}
                                onClick={(id) => navigate(`/groups/${kind}/${id}`)}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};
export default GroupPage;
