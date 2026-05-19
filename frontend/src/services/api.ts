import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || '';

export const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export interface LoginRequest {
    email: string;
    password: string;
}

export interface TokenResponse {
    grantType: string;
    accessToken: string;
    refreshToken: string;
    expiresInSec?: number;
    uid?: number;
    userId?: string;
}

export interface SignUpRequest {
    email: string;
    password: string;
    name: string;
    studentNo: number;
    campus: number;
    generation: number;
    classNo: number;
    mattermostId: string;
}

export interface SsafyVerifyRequest {
    targetUserId: string;
    generation: number;
    name: string;
}

export interface SsafyConfirmRequest {
    targetUserId: string;
    authCode: string;
}

export interface Campus {
    id: number;
    name: string;
}

export interface Ban {
    id: number;
    name: string;
    campus: Campus;
    generation?: number;
    classNo?: number;
    trackType?: string;
}

// --- Home Interfaces ---
export interface DDayModel {
    title: string;
    days: number;
}

export interface RecruitThumbModel {
    id: number;
    name: string;
    count: number;
}

export interface BoardThumbModel {
    boardId: number;
    name: string;
    recentPostTitle?: string;
}

export interface CampusMealModel {
    campusId: number;
    campusName: string;
    imageUrls: string[];
}

export interface HomeModel {
    dDays: DDayModel[];
    teamThumbnail?: RecruitThumbModel;
    studyThumbnail?: RecruitThumbModel;
    campusMeals: CampusMealModel[];
    boardsList: BoardThumbModel[];
}

// --- Board & Post Interfaces ---
export interface PollOptionResponse {
    optionId: number;
    text: string;
    voteCount: number;
}

export interface PollResponse {
    pollId: number;
    totalVotes: number;
    myVotedOptionId?: number;
    options: PollOptionResponse[];
}

export interface AnonResponse {
    name: string;
    isAuthor: boolean;
    isMine: boolean;
}

export interface ReplyResponse {
    id: number;
    createdAt?: string;
    memberId: number;
    content: string;
    likeCount: number;
    isLiked: boolean;
    isBlinded: boolean;
    anon?: AnonResponse;
}

export interface CommentResponse {
    id: number;
    createdAt?: string;
    content: string;
    likeCount: number;
    isLiked: boolean;
    isBlinded: boolean;
    memberId: number;
    anon?: AnonResponse;
    replies: ReplyResponse[];
}

export interface PostResponse {
    id: number;
    boardId: number;
    boardName: string;
    memberId: number;
    title: string;
    content: string;
    viewCount: number;
    likeCount: number;
    commentCount: number;
    createdAt?: string;
    updatedAt?: string;
    imageUrls?: string[];
    isBlinded: boolean;
    isMine?: boolean; // From backend response
}

export interface PostDetailResponse {
    createdAt?: string;
    updatedAt?: string;
    id: number;
    boardId: number;
    memberId: number;
    title: string;
    content: string;
    isBlinded: boolean;
    imageUrls?: string[];
    imageUrl?: string; // Backend legacy support
    // Kotlin PostDetailResponse: val imageUrl: String?
    // But UI uses post.imageUrls.
    // Let's check PostDetailUiModel in Android.
    poll?: PollResponse;
    viewCount?: number;
    likeCount: number;
    isLiked: boolean;
    commentCount: number;
    scrapCount: number;
    isScraped: boolean;
    comments: CommentResponse[];
    authorName: string;      // Added for UI convenience if server sends it or we map it
    authorProfileUrl?: string; // Added for UI convenience
    dateText: string;        // Added for UI convenience
    isAuthor: boolean;       // Added for UI convenience, likely redundant with isMine if backend sends it.
    isMine?: boolean;        // Added to match backend response explicitly
}

export interface PollCreateRequest {
    title: string;
    options: string[];
}

export interface PostCreateRequest {
    title: string;
    content: string;
    boardId: number;
    imageUrls?: string[];
    poll?: PollCreateRequest;
}

// Paged Response
export interface PagedPostResponse {
    posts: PostResponse[];
    nextCursor?: string;
    hasNext: boolean;
}

// --- D-Day Interfaces ---
export interface DdayItem {
    id: number;
    title: string;
    targetDate: string; // yyyy-MM-dd
    dDay: number;
    iconKey?: string;
}

export interface DdayListResponse {
    items: DdayItem[];
}

export const ddayService = {
    getDdays: async (): Promise<DdayListResponse> => {
        const response = await api.get<DdayListResponse>('/api/ddays');
        return response.data;
    }
};

export const homeService = {
    getHome: async (): Promise<HomeModel> => {
        const response = await api.get<HomeModel>('/api/home');
        return response.data;
    }
};

export interface MemberResponse {
    id: number;
    email: string;
    name: string;
    studentNo?: number;
    campus?: string;
    generation?: number;
    classNo?: number;
    mattermostId?: string;
    profileImageUrl?: string;
}

export const memberService = {
    getMyInfo: async (): Promise<MemberResponse> => {
        const response = await api.get<MemberResponse>('/api/members/me');
        return response.data;
    }
};

export const postService = {
    getPosts: async (boardId?: number, keyword?: string, cursor?: string, limit: number = 20, isMine?: boolean): Promise<PagedPostResponse> => {
        const params = { boardId, keyword, cursor, limit, isMine };
        const response = await api.get<PagedPostResponse>('/api/posts', { params });
        return response.data;
    },
    getHotPosts: async (cursor?: string, limit: number = 20): Promise<PagedPostResponse> => {
        const params = { cursor, limit };
        const response = await api.get<PagedPostResponse>('/api/posts/hot', { params });
        return response.data;
    },
    getPost: async (id: number): Promise<PostDetailResponse> => {
        const response = await api.get<PostDetailResponse>(`/api/posts/${id}`);
        // Transform or map if needed. For now assume strictly typed response.
        return response.data;
    },
    createPost: async (data: PostCreateRequest): Promise<PostResponse> => {
        const response = await api.post<PostResponse>('/api/posts', data);
        return response.data;
    },
    deletePost: async (id: number): Promise<void> => {
        await api.delete(`/api/posts/${id}`);
    },
    likePost: async (id: number): Promise<{ liked: boolean, likeCount: number }> => {
        const response = await api.post<{ liked: boolean, likeCount: number }>(`/api/posts/${id}/like`);
        return response.data;
    },
    unlikePost: async (id: number): Promise<{ liked: boolean, likeCount: number }> => {
        const response = await api.delete<{ liked: boolean, likeCount: number }>(`/api/posts/${id}/like`);
        return response.data;
    },
    scrapPost: async (id: number): Promise<{ success: boolean }> => {
        const response = await api.post<{ success: boolean }>(`/api/posts/${id}/scrap`);
        return response.data;
    },
    unscrapPost: async (id: number): Promise<{ success: boolean }> => {
        const response = await api.delete<{ success: boolean }>(`/api/posts/${id}/scrap`);
        return response.data;
    },
    votePoll: async (postId: number, optionId: number): Promise<PollResponse> => {
        const response = await api.post<PollResponse>(`/api/posts/${postId}/poll/vote`, { optionId });
        return response.data;
    }
};

export const uploadService = {
    uploadImage: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post<{ success: boolean; url: string }>('/api/uploads/images', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data.url;
    }
};

export const commentService = {
    createComment: async (postId: number, content: string, anon: boolean): Promise<CommentResponse> => {
        const response = await api.post<CommentResponse>(`/api/posts/${postId}/comments`, { content, isAnonymous: anon });
        return response.data;
    },
    createReply: async (postId: number, commentId: number, content: string, anon: boolean): Promise<ReplyResponse> => {
        const response = await api.post<ReplyResponse>(`/api/posts/${postId}/comments/${commentId}/replies`, { content, isAnonymous: anon });
        return response.data;
    },
    updateComment: async (commentId: number, content: string): Promise<CommentResponse> => {
        const response = await api.put<CommentResponse>(`/api/comments/${commentId}`, { content });
        return response.data;
    },
    deleteComment: async (commentId: number): Promise<void> => {
        await api.delete(`/api/comments/${commentId}`);
    },
    likeComment: async (commentId: number): Promise<{ liked: boolean, likeCount: number }> => {
        const response = await api.post<{ liked: boolean, likeCount: number }>(`/api/comments/${commentId}/like`);
        return response.data;
    },
    unlikeComment: async (commentId: number): Promise<{ liked: boolean, likeCount: number }> => {
        const response = await api.delete<{ liked: boolean, likeCount: number }>(`/api/comments/${commentId}/like`);
        return response.data;
    }
};

export interface BoardModel {
    id: number;
    name: string;
    description: string;
}

export const boardService = {
    getBoards: async (): Promise<BoardModel[]> => {
        const response = await api.get<BoardModel[]>('/api/boards');
        return response.data;
    }
};

export const authService = {
    login: async (email: string, password: string): Promise<TokenResponse | null> => {
        const request: LoginRequest = { email, password };
        const response = await api.post<TokenResponse>('/api/auth/login', request);
        if (response.data) {
            localStorage.setItem('accessToken', response.data.accessToken);
            localStorage.setItem('refreshToken', response.data.refreshToken);
            return response.data;
        }
        return null;
    },
    logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
    },
    isLoggedIn: (): boolean => {
        return !!localStorage.getItem('accessToken');
    },
    checkEmailAvailable: async (email: string): Promise<boolean> => {
        const response = await api.get<{ unique: boolean }>('/api/members/check-email', { params: { email } });
        return response.data.unique;
    },
    requestSsafyVerification: async (req: SsafyVerifyRequest): Promise<void> => {
        await api.post('/api/auth/send', req);
    },
    confirmSsafyVerification: async (req: SsafyConfirmRequest): Promise<void> => {
        await api.post('/api/auth/verify', req);
    },
    signUp: async (req: SignUpRequest): Promise<void> => {
        await api.post('/api/members/signup', req);
    }
};

export type NotificationType = 'COMMENT' | 'REPLY' | 'MESSAGE' | 'NOTICE' | 'APPLICATION' | 'ETC';

export interface Notification {
    id: number;
    content: string;
    isRead: boolean;
    relatedUrl: string;
    type: NotificationType;
    createdAt: string;
}

export const notificationService = {
    getNotifications: async (): Promise<Notification[]> => {
        const response = await api.get<Notification[]>('/api/notifications');
        return response.data;
    },

    readNotification: async (id: number): Promise<void> => {
        await api.put(`/api/notifications/${id}/read`);
    },

    registerFcmToken: async (token: string): Promise<void> => {
        await api.post('/api/notifications/token', { token });
    },

    subscribe: async (token: string): Promise<void> => {
        await api.post('/api/notifications/subscribe', { token });
    },
    unsubscribe: async (token: string): Promise<void> => {
        await api.post('/api/notifications/unsubscribe', { token });
    }
};

export const campusService = {
    getCampuses: async (): Promise<Campus[]> => {
        const response = await api.get<Campus[]>('/api/campuses');
        return response.data;
    },
    getClasses: async (campusId: number): Promise<Ban[]> => {
        const response = await api.get<Ban[]>(`/api/campuses/${campusId}/classes`);
        return response.data;
    }
};

// --- MyPage Interfaces ---
export interface MyPageUserInfo {
    userId: number;
    name: string;
    mattermostId: string;
    campus: string;
    generation: number;
    profileImageUrl: string | null;
}

export interface MyPageCounts {
    postCount: number;
    commentCount: number;
    scrapCount: number;
}

export interface MyPagePortfolioSummary {
    techStack: { [stackName: string]: string }; // key: 기술명, value: 숙련도 (HIGH/MID/LOW)
    ssafySwRating: string;
    solvedAcRank: string;
    links: string[];
    projects: string[];
}

export interface MyPageResponse {
    user: MyPageUserInfo;
    counts: MyPageCounts;
    portfolioSummary: MyPagePortfolioSummary | null;
}

export interface MyCommentResponse {
    id: number;
    content: string;
    createdAt: string;
    isReply: boolean;
    postId: number;
    postTitle: string;
    boardId: number;
    boardName: string;
}

export const myPageService = {
    getMyPage: async (): Promise<MyPageResponse> => {
        const response = await api.get<MyPageResponse>('/api/members/mypage');
        return response.data;
    },
    updateProfileImage: async (imageUrl: string): Promise<void> => {
        await api.put('/api/members/me/profile-image', { profileImageUrl: imageUrl });
    },
    deleteProfileImage: async (): Promise<void> => {
        await api.delete('/api/members/me/profile-image');
    },
    getMyPosts: async (): Promise<PostResponse[]> => {
        const response = await api.get<PostResponse[]>('/api/members/mypage/posts');
        return response.data;
    },
    getMyComments: async (): Promise<MyCommentResponse[]> => {
        const response = await api.get<MyCommentResponse[]>('/api/members/mypage/comments');
        return response.data;
    },
    getMyScraps: async (): Promise<PostResponse[]> => {
        const response = await api.get<PostResponse[]>('/api/members/mypage/scraps');
        return response.data;
    }
};

api.interceptors.request.use((config) => {
    let token = localStorage.getItem('accessToken');
    if (token) {
        // Fix: Strip extra quotes if they exist (common issue with JSON.stringify or backend responses)
        if (token.startsWith('"') && token.endsWith('"')) {
            token = token.slice(1, -1);
        }
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// --- Team/Study Interfaces ---
export interface Campus {
    id: number;
    name: string;
}

export interface GroupMember {
    id: number;
    email?: string;
    name: string;
    studentNo?: number;
    mattermostId?: string;
    profileImageUrl?: string;
    portfolioId?: number;
    createdAt?: string;
    updatedAt?: string;
    role?: string;
    isLeader?: boolean; // 팀장/리더 여부
}

export interface GroupSummary {
    id: number;
    title: string;
    type: string;
    capacity: number;
    startDate: string; // yyyy-MM-dd
    endDate: string;
    description?: string;
    status?: string;
    campus?: Campus;
    leader?: GroupMember;
    currentMembers?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface GroupDetail {
    id: number;
    title: string;
    type: string;
    capacity: number;
    startDate: string;
    endDate: string;
    description: string;
    status?: string;
    campus?: Campus;
    leaderId?: number;
    leaderName?: string;
    leaderEmail?: string;
    leaderMattermostId?: string;
    leaderProfileImageUrl?: string;
    members: GroupMember[];
    currentMembers?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface GroupCreateRequest {
    title: string;
    type: string;
    capacity: number;
    startDate: string;
    endDate: string;
    campusId: number;
    description: string;
}

export interface GroupUpdateRequest {
    title?: string;
    type?: string;
    capacity?: number;
    startDate?: string;
    endDate?: string;
    campusId?: number;
    description?: string;
    status?: string;
}

export interface GroupApplicationRequest {
    portfolioId: number;
    title: string;
    message: string;
    position: string;
}

export interface MyApplication {
    id: number;
    groupId: number;
    groupTitle: string;
    leaderName?: string;
    status: string;
    position: string;
    createdAt?: string;
    isGroupDeleted: boolean;
}

export interface PortfolioInfoSnippet {
    id: number;
    title: string;
    memberId: number;
    memberName: string;
    memberEmail: string;
    memberProfileImageUrl: string | null;
    introduction: string;
    bojHandle: string;
    solvedacRank: string;
    swTestRank: string;
}

export interface TeamApplicationResponse {
    id: number;
    team: {
        id: number;
        title: string;
        leaderId: number;
        leaderName: string;
    };
    portfolio: PortfolioInfoSnippet;
    title: string;
    message: string;
    position: string;
    status: string;
    createdAt: string;
    updatedAt: string;
}

export interface StudyApplicationResponse {
    id: number;
    study: {
        id: number;
        title: string;
        leaderId: number;
        leaderName: string;
    };
    portfolio: PortfolioInfoSnippet;
    title: string;
    message: string;
    position: string;
    status: string;
    createdAt: string;
    updatedAt: string;
}

// --- Portfolio Interfaces ---
export interface PortfolioStack {
    id: number;
    stackName: string;
    proficiency: string; // HIGH, MID, LOW
}

export interface PortfolioUrl {
    id: number;
    url: string;
}

export interface PortfolioImage {
    id: number;
    imageUrl: string;
}

export interface PortfolioProject {
    id: number;
    projectTitle: string;
}

export interface SolvedAcInfo {
    bojHandle: string;
    rank: string;
}

export interface SolvedAcUserResponse {
    handle: string;
    tier: number;
    rating: number;
    solvedCount: number;
    classValue: number;
    rank: number;
}

export interface PortfolioResponse {
    id: number;
    memberId: number;
    title: string;
    description: string;
    introduction: string;
    stacks: PortfolioStack[];
    swTestRank: string;
    solvedAcInfo: SolvedAcInfo | null;
    urls: PortfolioUrl[];
    images: PortfolioImage[];
    projects: PortfolioProject[];
    isVisible: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface GlobalStack {
    id: number;
    name: string;
    imgUrl?: string;
}

export interface PortfolioCreateRequest {
    title: string;
    description?: string;
    introduction?: string;
    bojHandle?: string;
    swTestRank?: string;
    isVisible?: boolean;
    stacks?: { stackId: number; expertLevel: string }[];
    urls?: { url: string }[];
    images?: { imageUrl: string; orders: number }[];
}

export interface PortfolioUpdateRequest {
    title?: string;
    description?: string;
    introduction?: string;
    bojHandle?: string;
    swTestRank?: string;
    isVisible?: boolean;
    stacks?: { stackId: number; expertLevel: string }[];
    urls?: { url: string }[];
    images?: { imageUrl: string; orders: number }[];
}

// --- Chat Interfaces ---
export interface ChatRoomResponse {
    roomId: number;
    chatRoomName: string;
    opponentName: string;
    postId: number | null;
    postTitle: string | null;
    lastMessage: string | null;
    lastMessageAt: string | null;
    isDeleted: boolean;
    createdAt: string;
}

export interface ChatMessageResponse {
    id: number;
    isMine: boolean;
    senderName: string;
    content: string;
    sentAt: string;
}

export interface ChatRoomCreateRequest {
    postId: number;
}

export interface ChatMessageSendRequest {
    content: string;
}

export interface ChatListUpdateResponse {
    roomId: number;
    lastMessage: string;
    lastMessageAt: string;
}

export interface ChatMessageWebSocketRequest {
    content: string;
}

export const chatService = {
    createChatRoom: async (postId: number): Promise<number> => {
        const response = await api.post<number>('/api/chat/rooms', { postId });
        return response.data;
    },
    getMyChatRooms: async (): Promise<ChatRoomResponse[]> => {
        const response = await api.get<ChatRoomResponse[]>('/api/chat/rooms');
        return response.data;
    },
    getChatRoom: async (roomId: number): Promise<ChatRoomResponse> => {
        const response = await api.get<ChatRoomResponse>(`/api/chat/rooms/${roomId}`);
        return response.data;
    },
    sendMessage: async (roomId: number, content: string): Promise<number> => {
        const response = await api.post<number>(`/api/chat/rooms/${roomId}/messages`, { content });
        return response.data;
    },
    getChatMessages: async (roomId: number): Promise<ChatMessageResponse[]> => {
        const response = await api.get<ChatMessageResponse[]>(`/api/chat/rooms/${roomId}/messages`);
        return response.data;
    },
    exitChatRoom: async (roomId: number): Promise<void> => {
        await api.delete(`/api/chat/rooms/${roomId}`);
    }
};

// --- Study Service ---
export const studyService = {
    getStudies: async (campusId?: number, type?: string): Promise<GroupSummary[]> => {
        const params = { campusId, type };
        const response = await api.get<GroupSummary[]>('/api/studies', { params });
        return response.data;
    },

    getStudyDetail: async (id: number): Promise<GroupDetail> => {
        const response = await api.get<any>(`/api/studies/${id}`);
        const data = response.data;
        return {
            ...data,
            leaderId: data.leader?.id,
            leaderName: data.leader?.name,
            leaderEmail: data.leader?.email,
            leaderMattermostId: data.leader?.mattermostId,
            leaderProfileImageUrl: data.leader?.profileImageUrl,
            members: (data.members || []).map((m: any) => ({
                ...m,
                name: m.memberName || m.name,
                email: m.memberEmail || m.email,
                profileImageUrl: m.memberProfileImageUrl || m.profileImageUrl,
                isLeader: m.isLeader || m.role === 'LEADER'
            }))
        };
    },

    getMyStudies: async (): Promise<GroupSummary[]> => {
        const response = await api.get<GroupSummary[]>('/api/studies/me');
        return response.data;
    },

    createStudy: async (data: GroupCreateRequest): Promise<GroupSummary> => {
        const response = await api.post<GroupSummary>('/api/studies', data);
        return response.data;
    },

    updateStudy: async (id: number, data: GroupUpdateRequest): Promise<void> => {
        await api.put(`/api/studies/${id}`, data);
    },

    deleteStudy: async (id: number): Promise<void> => {
        await api.delete(`/api/studies/${id}`);
    },

    applyStudy: async (id: number, data: GroupApplicationRequest): Promise<void> => {
        await api.post(`/api/studies/${id}/applications`, data);
    },

    getStudyMembers: async (id: number): Promise<GroupMember[]> => {
        const response = await api.get<any[]>(`/api/studies/${id}/members`);
        return response.data.map(m => ({
            ...m,
            name: m.memberName || m.name,
            email: m.memberEmail || m.email,
            profileImageUrl: m.memberProfileImageUrl || m.profileImageUrl,
            isLeader: m.role === 'LEADER'
        }));
    },

    kickStudyMember: async (studyId: number, memberId: number): Promise<void> => {
        await api.delete(`/api/studies/${studyId}/members/${memberId}`);
    },

    leaveStudy: async (id: number): Promise<void> => {
        await api.delete(`/api/studies/${id}/leave`);
    },

    getMyStudyApplications: async (): Promise<MyApplication[]> => {
        const response = await api.get<MyApplication[]>('/api/study-applications/me');
        return response.data;
    },

    cancelStudyApplication: async (applicationId: number): Promise<void> => {
        await api.delete(`/api/study-applications/${applicationId}`);
    },

    getStudyApplications: async (studyId: number): Promise<StudyApplicationResponse[]> => {
        const response = await api.get<StudyApplicationResponse[]>(`/api/studies/${studyId}/applications`);
        return response.data;
    },

    acceptStudyApplication: async (applicationId: number): Promise<void> => {
        await api.post(`/api/study-applications/${applicationId}/accept`);
    },

    rejectStudyApplication: async (applicationId: number): Promise<void> => {
        await api.post(`/api/study-applications/${applicationId}/reject`);
    },
};

// --- Team Service ---
export const teamService = {
    getTeams: async (campusId?: number, type?: string): Promise<GroupSummary[]> => {
        const params = { campusId, type };
        const response = await api.get<GroupSummary[]>('/api/teams', { params });
        return response.data;
    },

    getTeamDetail: async (id: number): Promise<GroupDetail> => {
        const response = await api.get<any>(`/api/teams/${id}`);
        const data = response.data;
        return {
            ...data,
            leaderId: data.leader?.id,
            leaderName: data.leader?.name,
            leaderEmail: data.leader?.email,
            leaderMattermostId: data.leader?.mattermostId,
            leaderProfileImageUrl: data.leader?.profileImageUrl,
            members: (data.members || []).map((m: any) => ({
                ...m,
                name: m.memberName || m.name,
                email: m.memberEmail || m.email,
                profileImageUrl: m.memberProfileImageUrl || m.profileImageUrl,
                isLeader: m.isLeader || m.role === 'LEADER'
            }))
        };
    },

    getMyTeams: async (): Promise<GroupSummary[]> => {
        const response = await api.get<GroupSummary[]>('/api/teams/me');
        return response.data;
    },

    createTeam: async (data: GroupCreateRequest): Promise<GroupSummary> => {
        const response = await api.post<GroupSummary>('/api/teams', data);
        return response.data;
    },

    updateTeam: async (id: number, data: GroupUpdateRequest): Promise<void> => {
        await api.put(`/api/teams/${id}`, data);
    },

    deleteTeam: async (id: number): Promise<void> => {
        await api.delete(`/api/teams/${id}`);
    },

    applyTeam: async (id: number, data: GroupApplicationRequest): Promise<void> => {
        await api.post(`/api/teams/${id}/applications`, data);
    },

    getTeamMembers: async (id: number): Promise<GroupMember[]> => {
        const response = await api.get<any[]>(`/api/teams/${id}/members`);
        return response.data.map(m => ({
            ...m,
            name: m.memberName || m.name,
            email: m.memberEmail || m.email,
            profileImageUrl: m.memberProfileImageUrl || m.profileImageUrl,
            isLeader: m.role === 'LEADER'
        }));
    },

    kickTeamMember: async (teamId: number, memberId: number): Promise<void> => {
        await api.delete(`/api/teams/${teamId}/members/${memberId}`);
    },

    leaveTeam: async (id: number): Promise<void> => {
        await api.delete(`/api/teams/${id}/leave`);
    },

    getMyTeamApplications: async (): Promise<MyApplication[]> => {
        const response = await api.get<MyApplication[]>('/api/team-applications/me');
        return response.data;
    },

    cancelTeamApplication: async (applicationId: number): Promise<void> => {
        await api.delete(`/api/team-applications/${applicationId}`);
    },

    getTeamApplications: async (teamId: number): Promise<TeamApplicationResponse[]> => {
        const response = await api.get<TeamApplicationResponse[]>(`/api/teams/${teamId}/applications`);
        return response.data;
    },

    acceptTeamApplication: async (applicationId: number): Promise<void> => {
        await api.post(`/api/team-applications/${applicationId}/accept`);
    },

    rejectTeamApplication: async (applicationId: number): Promise<void> => {
        await api.post(`/api/team-applications/${applicationId}/reject`);
    },
};

// --- Portfolio Service ---
export const portfolioService = {
    getMyPortfolios: async (): Promise<PortfolioResponse[]> => {
        const response = await api.get<PortfolioResponse[]>('/api/portfolios/me');
        return response.data;
    },

    getPortfolio: async (id: number): Promise<PortfolioResponse> => {
        const response = await api.get<PortfolioResponse>(`/api/portfolios/${id}`);
        return response.data;
    },

    createPortfolio: async (data: PortfolioCreateRequest): Promise<number> => {
        const response = await api.post<number>('/api/portfolios', data);
        return response.data;
    },

    updatePortfolio: async (id: number, data: PortfolioUpdateRequest): Promise<number> => {
        const response = await api.put<number>(`/api/portfolios/${id}`, data);
        return response.data;
    },

    verifySolvedac: async (handle: string): Promise<SolvedAcUserResponse> => {
        const response = await api.get<SolvedAcUserResponse>(`/api/portfolios/solvedac/verify`, { params: { handle } });
        return response.data;
    },
};

// --- Stack Service ---
export const stackService = {
    getAllStacks: async (): Promise<GlobalStack[]> => {
        const response = await api.get<GlobalStack[]>('/api/stacks');
        return response.data;
    },
};

// --- Notice & Task Types ---
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface NoticeResponse {
    id: number;
    teamId?: number;
    studyId?: number;
    title: string;
    content: string;
    isPinned: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface NoticeRequest {
    title: string;
    content: string;
    isPinned?: boolean;
    sendPushNotification?: boolean;
}

export interface TaskResponse {
    id: number;
    teamId?: number;
    studyId?: number;
    title: string;
    content: string;
    startDate: string; // LocalDate format: YYYY-MM-DD
    endDate: string;
    status: TaskStatus;
    creatorId: number;
    createdAt: string;
    updatedAt: string;
}

export interface TaskRequest {
    title: string;
    content: string;
    startDate: string; // YYYY-MM-DD
    endDate: string;
    status?: TaskStatus;
}

// --- Study Notice Service ---
export const studyNoticeService = {
    getNotices: async (studyId: number): Promise<NoticeResponse[]> => {
        const response = await api.get<NoticeResponse[]>(`/api/studies/${studyId}/notices`);
        return response.data;
    },

    createNotice: async (studyId: number, data: NoticeRequest): Promise<void> => {
        await api.post(`/api/studies/${studyId}/notices`, data);
    },

    updateNotice: async (studyId: number, noticeId: number, data: NoticeRequest): Promise<void> => {
        await api.put(`/api/studies/${studyId}/notices/${noticeId}`, data);
    },

    deleteNotice: async (studyId: number, noticeId: number): Promise<void> => {
        await api.delete(`/api/studies/${studyId}/notices/${noticeId}`);
    },
};

// --- Team Notice Service ---
export const teamNoticeService = {
    getNotices: async (teamId: number): Promise<NoticeResponse[]> => {
        const response = await api.get<NoticeResponse[]>(`/api/teams/${teamId}/notices`);
        return response.data;
    },

    createNotice: async (teamId: number, data: NoticeRequest): Promise<void> => {
        await api.post(`/api/teams/${teamId}/notices`, data);
    },

    updateNotice: async (teamId: number, noticeId: number, data: NoticeRequest): Promise<void> => {
        await api.put(`/api/teams/${teamId}/notices/${noticeId}`, data);
    },

    deleteNotice: async (teamId: number, noticeId: number): Promise<void> => {
        await api.delete(`/api/teams/${teamId}/notices/${noticeId}`);
    },
};

// --- Study Task Service ---
export const studyTaskService = {
    getTasks: async (studyId: number): Promise<TaskResponse[]> => {
        const response = await api.get<TaskResponse[]>(`/api/studies/${studyId}/tasks`);
        return response.data;
    },

    createTask: async (studyId: number, data: TaskRequest): Promise<TaskResponse> => {
        const response = await api.post<TaskResponse>(`/api/studies/${studyId}/tasks`, data);
        return response.data;
    },

    updateTask: async (taskId: number, data: TaskRequest): Promise<void> => {
        await api.put(`/api/study-tasks/${taskId}`, data);
    },

    updateTaskStatus: async (taskId: number, status: TaskStatus): Promise<void> => {
        await api.put(`/api/study-tasks/${taskId}/status`, { status });
    },

    deleteTask: async (taskId: number): Promise<void> => {
        await api.delete(`/api/study-tasks/${taskId}`);
    },
};

// --- Team Task Service ---
export const teamTaskService = {
    getTasks: async (teamId: number): Promise<TaskResponse[]> => {
        const response = await api.get<TaskResponse[]>(`/api/teams/${teamId}/tasks`);
        return response.data;
    },

    createTask: async (teamId: number, data: TaskRequest): Promise<TaskResponse> => {
        const response = await api.post<TaskResponse>(`/api/teams/${teamId}/tasks`, data);
        return response.data;
    },

    updateTask: async (taskId: number, data: TaskRequest): Promise<void> => {
        await api.put(`/api/team-tasks/${taskId}`, data);
    },

    updateTaskStatus: async (taskId: number, status: TaskStatus): Promise<void> => {
        await api.put(`/api/team-tasks/${taskId}/status`, { status });
    },

    deleteTask: async (taskId: number): Promise<void> => {
        await api.delete(`/api/team-tasks/${taskId}`);
    },
};


