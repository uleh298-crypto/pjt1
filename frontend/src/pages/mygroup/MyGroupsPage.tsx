import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { MdSearch, MdClose, MdAssignment } from 'react-icons/md';
import { studyService, teamService, memberService } from '../../services/api';
import type { GroupSummary, GroupMember } from '../../services/api';
import FilterChips from '../group/components/FilterChips';
import RecentSearchChip from '../group/components/RecentSearchChip';
import MyGroupCard from './components/MyGroupCard';
import {
    studyFilters,
    teamFilters,
    studyTypeLabels,
    teamTypeLabels,
    getRecentKeywords,
    addRecentKeyword,
    deleteRecentKeyword
} from '../../utils/groupUtils';

const MyGroupsPage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // URL 경로에서 kind 추출
    const isStudyPath = location.pathname.includes('/study');
    const kind: 'study' | 'team' = isStudyPath ? 'study' : 'team';

    const [groups, setGroups] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [selectedFilter, setSelectedFilter] = useState('전체');
    const [searchQuery, setSearchQuery] = useState('');
    const [committedQuery, setCommittedQuery] = useState('');
    const [recentKeywords, setRecentKeywords] = useState<string[]>([]);
    const [showMinLengthError, setShowMinLengthError] = useState(false);
    const [isSearchFocused, setIsSearchFocused] = useState(false);

    const title = kind === 'study' ? '스터디' : '프로젝트';
    const filters = kind === 'study' ? studyFilters : teamFilters;
    const typeLabels = kind === 'study' ? studyTypeLabels : teamTypeLabels;

    const loadGroups = async () => {
        setLoading(true);
        try {
            const [result, myInfo] = await Promise.all([
                kind === 'study' ? await studyService.getMyStudies() : await teamService.getMyTeams(),
                memberService.getMyInfo()
            ]);

            const groupsWithMembers = await Promise.all(
                result.map(async (group) => {
                    try {
                        const members = kind === 'study' ?
                            await studyService.getStudyMembers(group.id) :
                            await teamService.getTeamMembers(group.id);

                        const profileUrls = members
                            .map(m => m.profileImageUrl)
                            .filter(url => url) as string[];

                        const isLeader = members.some(m => m.id === myInfo.id && m.isLeader);
                        const role = isLeader ? '리더' : '멤버';

                        return {
                            id: group.id,
                            title: group.title,
                            role,
                            isLeader,
                            category: group.type,
                            currentMembers: members.length,
                            memberProfileImageUrls: profileUrls.slice(0, 4)
                        };
                    } catch (error) {
                        return {
                            id: group.id,
                            title: group.title,
                            role: '멤버',
                            isLeader: false,
                            category: group.type,
                            currentMembers: 0,
                            memberProfileImageUrls: []
                        };
                    }
                })
            );

            setGroups(groupsWithMembers);
        } catch (error) {
            console.error('나의 그룹 목록 로딩 실패:', error);
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

    const filteredGroups = groups.filter(group => {
        // 필터링
        if (selectedFilter !== '전체' && group.category !== typeLabels[selectedFilter]) {
            return false;
        }
        // 검색어 필터링
        if (committedQuery) {
            return group.title.toLowerCase().includes(committedQuery.toLowerCase());
        }
        return true;
    });

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
                            <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>나의 그룹</h1>
                            <div style={{ display: 'flex', gap: '20px' }}>
                                <div
                                    onClick={() => navigate('/mygroups/study')}
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
                                    onClick={() => navigate('/mygroups/team')}
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
                                maxWidth: '300px',
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
                                    placeholder="그룹 검색..."
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
                                onClick={() => navigate('/notification')} // 임시: 지원 현황 등
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    padding: '12px 20px',
                                    backgroundColor: 'var(--surface)',
                                    color: 'var(--on-surface)',
                                    border: '1px solid var(--border-color)',
                                    borderRadius: '12px',
                                    fontWeight: '600',
                                    cursor: 'pointer'
                                }}
                            >
                                <MdAssignment size={20} />
                                지원 현황
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

                {/* Recent Searches */}
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
                ) : filteredGroups.length === 0 ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '100px 0',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '24px',
                        border: '1px dashed var(--border-color)'
                    }}>
                        <p style={{ fontSize: '18px', color: 'var(--on-surface-variant)' }}>
                            {committedQuery ? '검색 결과가 없습니다.' : `참여 중인 ${title}가 없습니다.`}
                        </p>
                    </div>
                ) : (
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))',
                        gap: '24px'
                    }}>
                        {filteredGroups.map((group) => (
                            <MyGroupCard key={group.id} kind={kind} {...group} />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default MyGroupsPage;
