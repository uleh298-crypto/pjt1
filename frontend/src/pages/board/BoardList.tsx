import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MdEdit, MdSearch, MdWhatshot, MdArrowDropDown } from 'react-icons/md';
import { postService, boardService } from '../../services/api';
import type { PostResponse, BoardModel } from '../../services/api';
import PostItem from './components/PostItem';

const BoardList: React.FC = () => {
    const navigate = useNavigate();
    const { boardId } = useParams<{ boardId?: string }>();
    const [posts, setPosts] = useState<PostResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [loadingMore, setLoadingMore] = useState(false);
    const [isHot, setIsHot] = useState(false);
    const [nextCursor, setNextCursor] = useState<string | undefined>(undefined);
    const [hasNext, setHasNext] = useState(false);

    const observer = useRef<IntersectionObserver | null>(null);
    const lastPostRef = useRef<HTMLDivElement>(null);

    const [boards, setBoards] = useState<BoardModel[]>([]);
    const [selectedBoardId, setSelectedBoardId] = useState<number | null>(null);
    const [boardName, setBoardName] = useState('전체보기');
    const [isSearchFocused, setIsSearchFocused] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');

    const [showBoardMenu, setShowBoardMenu] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    // 외부 클릭 시 드롭다운 닫기
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setShowBoardMenu(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // 게시판 목록 로딩
    const loadBoards = async () => {
        try {
            const boardList = await boardService.getBoards();
            setBoards(boardList);
        } catch (error) {
            console.error('게시판 목록 로딩 실패:', error);
        }
    };

    // 게시물 필터링 로직
    const filteredPosts = React.useMemo(() => {
        if (!searchQuery.trim()) return posts;
        const query = searchQuery.toLowerCase();
        return posts.filter(p =>
            p.title.toLowerCase().includes(query) ||
            p.content.toLowerCase().includes(query)
        );
    }, [posts, searchQuery]);

    // 게시글 목록 로딩
    const loadPosts = async (isFirst = true) => {
        if (isFirst) {
            setLoading(true);
        } else {
            setLoadingMore(true);
        }

        try {
            const res = isHot
                ? await postService.getHotPosts()
                : await postService.getPosts(selectedBoardId || undefined, undefined, isFirst ? undefined : nextCursor);

            if (isFirst) {
                setPosts(res.posts);
            } else {
                setPosts(prev => [...prev, ...res.posts]);
            }
            setNextCursor(res.nextCursor);
            setHasNext(res.hasNext);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    // 무한 스크롤 Observer 설정
    useEffect(() => {
        if (loading || !hasNext) return;

        if (observer.current) observer.current.disconnect();

        observer.current = new IntersectionObserver(entries => {
            if (entries[0].isIntersecting && hasNext && !loadingMore) {
                loadPosts(false);
            }
        });

        if (lastPostRef.current) {
            observer.current.observe(lastPostRef.current);
        }

        return () => observer.current?.disconnect();
    }, [hasNext, nextCursor, loading, loadingMore]);

    // 게시판 선택 핸들러
    const handleBoardSelect = (boardId: number | null, name: string) => {
        setSelectedBoardId(boardId);
        setBoardName(name);
        setIsHot(false);
    };

    // 초기 로딩
    useEffect(() => {
        loadBoards();
    }, []);

    // URL boardId 파라미터 처리
    useEffect(() => {
        if (boardId && boards.length > 0) {
            const id = Number(boardId);
            setSelectedBoardId(id);
            const board = boards.find(b => b.id === id);
            if (board) {
                setBoardName(board.name);
            }
        }
    }, [boardId, boards]);

    // 게시글 로딩
    useEffect(() => {
        loadPosts();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isHot, selectedBoardId]);

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
                        {/* Title */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '40px' }}>
                            <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>커뮤니티</h1>
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
                                    placeholder="게시판 내 검색..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    onFocus={() => setIsSearchFocused(true)}
                                    onBlur={() => setIsSearchFocused(false)}
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
                            </div>

                            <button
                                onClick={() => navigate('/board/write')}
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
                                <MdEdit size={20} />
                                글 쓰기
                            </button>
                        </div>
                    </div>

                    {/* Filter Dropdown Area */}
                    <div style={{ marginTop: '24px', position: 'relative' }} ref={menuRef}>
                        <div
                            onClick={() => setShowBoardMenu(!showBoardMenu)}
                            style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '8px',
                                padding: '10px 20px',
                                backgroundColor: 'var(--field-bg)',
                                borderRadius: '12px',
                                cursor: 'pointer',
                                border: '1px solid var(--border-color)',
                                minWidth: '180px',
                                transition: 'all 0.2s'
                            }}
                        >
                            <span style={{ fontWeight: '600', color: 'var(--on-surface)', fontSize: '15px' }}>
                                {isHot ? '🔥 인기글' : boardName}
                            </span>
                            <MdArrowDropDown
                                size={24}
                                color="var(--on-surface-variant)"
                                style={{
                                    transform: showBoardMenu ? 'rotate(180deg)' : 'rotate(0)',
                                    transition: 'transform 0.2s',
                                    marginLeft: 'auto'
                                }}
                            />
                        </div>

                        {showBoardMenu && (
                            <div style={{
                                position: 'absolute',
                                top: 'calc(100% + 8px)',
                                left: 0,
                                width: '240px',
                                backgroundColor: 'var(--surface)',
                                borderRadius: '16px',
                                boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
                                border: '1px solid var(--border-color)',
                                zIndex: 1000,
                                padding: '8px',
                                maxHeight: '400px',
                                overflowY: 'auto'
                            }}>
                                <div
                                    onClick={() => {
                                        handleBoardSelect(null, '전체보기');
                                        setShowBoardMenu(false);
                                    }}
                                    style={{
                                        padding: '12px 16px',
                                        borderRadius: '10px',
                                        cursor: 'pointer',
                                        backgroundColor: (selectedBoardId === null && !isHot) ? 'var(--primary-container)' : 'transparent',
                                        color: (selectedBoardId === null && !isHot) ? 'var(--primary)' : 'var(--on-surface)',
                                        fontWeight: (selectedBoardId === null && !isHot) ? 'bold' : 'normal',
                                        transition: 'background 0.2s'
                                    }}
                                >
                                    전체보기
                                </div>
                                <div
                                    onClick={() => {
                                        setIsHot(true);
                                        setShowBoardMenu(false);
                                    }}
                                    style={{
                                        padding: '12px 16px',
                                        borderRadius: '10px',
                                        cursor: 'pointer',
                                        backgroundColor: isHot ? 'rgba(255, 87, 34, 0.1)' : 'transparent',
                                        color: isHot ? '#FF5722' : 'var(--on-surface)',
                                        fontWeight: isHot ? 'bold' : 'normal',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '8px',
                                        transition: 'background 0.2s'
                                    }}
                                >
                                    <MdWhatshot size={18} />
                                    인기글
                                </div>
                                <div style={{ height: '1px', backgroundColor: 'var(--border-color)', margin: '4px 8px' }} />
                                {boards.map(board => (
                                    <div
                                        key={board.id}
                                        onClick={() => {
                                            handleBoardSelect(board.id, board.name);
                                            setShowBoardMenu(false);
                                        }}
                                        style={{
                                            padding: '12px 16px',
                                            borderRadius: '10px',
                                            cursor: 'pointer',
                                            backgroundColor: selectedBoardId === board.id ? 'var(--primary-container)' : 'transparent',
                                            color: selectedBoardId === board.id ? 'var(--primary)' : 'var(--on-surface)',
                                            fontWeight: selectedBoardId === board.id ? 'bold' : 'normal',
                                            transition: 'background 0.2s'
                                        }}
                                    >
                                        {board.name}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* List Content */}
            <div className="container" style={{ padding: '40px 20px' }}>
                {loading && posts.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '100px 0' }}>
                        <div className="loading-spinner" />
                        <p style={{ marginTop: '16px', color: 'var(--on-surface-variant)' }}>게시글을 불러오는 중입니다...</p>
                    </div>
                ) : filteredPosts.length === 0 ? (
                    <div style={{
                        textAlign: 'center',
                        padding: '100px 0',
                        backgroundColor: 'var(--surface)',
                        borderRadius: '24px',
                        border: '1px dashed var(--border-color)'
                    }}>
                        <p style={{ fontSize: '18px', color: 'var(--on-surface-variant)' }}>
                            {searchQuery ? '검색 결과가 없습니다.' : '등록된 게시글이 없습니다.'}
                        </p>
                    </div>
                ) : (
                    <div style={{
                        maxWidth: '900px',
                        margin: '0 auto',
                    }}>
                        {filteredPosts.map((post, index) => (
                            <div key={post.id} ref={index === filteredPosts.length - 1 ? lastPostRef : null}>
                                <PostItem
                                    post={post}
                                    isLast={index === filteredPosts.length - 1}
                                />
                            </div>
                        ))}
                        {loadingMore && (
                            <div style={{ textAlign: 'center', padding: '20px' }}>
                                <div className="loading-spinner" style={{ width: '30px', height: '30px', margin: '0 auto' }} />
                                <p style={{ fontSize: '13px', color: 'var(--on-surface-variant)', marginTop: '8px' }}>더 불러오는 중...</p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default BoardList;

