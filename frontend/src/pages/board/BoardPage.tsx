import React from 'react';
import { useParams, useLocation } from 'react-router-dom';
import BoardList from './BoardList';
import BoardDetail from './BoardDetail';
import BoardWrite from './BoardWrite';

const BoardPage: React.FC = () => {
    // Check if we are in list mode or detail mode based on URL params
    const { boardId, postId } = useParams<{ boardId?: string, postId?: string }>();
    const location = useLocation();

    // If path contains 'write', show Write page
    if (location.pathname.includes('/write')) {
        return <BoardWrite />;
    }

    // If postId exists, show detail
    // If we are at /board/detail/:postId, show detail
    if (location.pathname.includes('/detail/') || (postId && !boardId && !isNaN(Number(postId)))) {
        // Note: The routing in App.tsx maps /board/detail/:postId -> BoardPage
        return <BoardDetail />;
    }

    // Otherwise show list (default or specific board)
    return <BoardList />;
};

export default BoardPage;
