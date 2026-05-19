// Group/Team utility functions

export const calculateDDay = (targetDate: string): string => {
    const target = new Date(targetDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    target.setHours(0, 0, 0, 0);

    const diff = Math.ceil((target.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diff === 0) return 'D-Day';
    if (diff > 0) return `D-${diff}`;
    return '마감';
};

export const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
};

// Android GroupTypeMapper.kt 참조
export const studyTypeLabels: Record<string, string> = {
    '전체': '',
    '알고리즘': 'ALGORITHM',
    'CS': 'CS',
    '자격증': 'CERTIFICATION',
    '기타': 'ETC',
};

export const teamTypeLabels: Record<string, string> = {
    '전체': '',
    '싸피': 'SSAFY',
    '공모전': 'CONTEST',
    '자유': 'FREE',
};

export const studyFilters = Object.keys(studyTypeLabels);
export const teamFilters = Object.keys(teamTypeLabels);

// 상태 체크
export const isGroupClosed = (status?: string, endDate?: string): boolean => {
    if (status === 'CLOSED') return true;
    if (!endDate) return false;

    const end = new Date(endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    end.setHours(0, 0, 0, 0);

    return end < today;
};

// 최근 검색어 관리
const SEARCH_KEYWORDS_KEY = 'group_search_keywords';
const MAX_KEYWORDS = 10;

export const getRecentKeywords = (): string[] => {
    try {
        const stored = localStorage.getItem(SEARCH_KEYWORDS_KEY);
        return stored ? JSON.parse(stored) : [];
    } catch {
        return [];
    }
};

export const addRecentKeyword = (keyword: string): void => {
    if (!keyword.trim()) return;

    let keywords = getRecentKeywords();
    // 중복 제거
    keywords = keywords.filter(k => k !== keyword);
    // 맨 앞에 추가
    keywords.unshift(keyword);
    // 최대 개수 제한
    keywords = keywords.slice(0, MAX_KEYWORDS);

    localStorage.setItem(SEARCH_KEYWORDS_KEY, JSON.stringify(keywords));
};

export const deleteRecentKeyword = (keyword: string): void => {
    const keywords = getRecentKeywords().filter(k => k !== keyword);
    localStorage.setItem(SEARCH_KEYWORDS_KEY, JSON.stringify(keywords));
};

export const clearRecentKeywords = (): void => {
    localStorage.removeItem(SEARCH_KEYWORDS_KEY);
};
