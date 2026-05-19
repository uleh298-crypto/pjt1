import { useState } from 'react';
import axios from 'axios';

// Use relative path to leverage Vite Proxy (local dev) or Nginx (prod)
const API_BASE_URL = '/api';

function TestPage() {
    // Global State
    const [logs, setLogs] = useState<string[]>([]);
    const [authToken, setAuthToken] = useState<string>('');
    const [currentUser, setCurrentUser] = useState<string>('');

    // --- Step 1: Mattermost Auth State ---
    const [mmId, setMmId] = useState('');
    const [generation, setGeneration] = useState('14'); // Default 14
    const [authCode, setAuthCode] = useState('');
    const [isMmVerified, setIsMmVerified] = useState(false);

    // --- Step 2: Sign Up State ---
    const [signupEmail, setSignupEmail] = useState('');
    const [signupPassword, setSignupPassword] = useState('');
    const [signupName, setSignupName] = useState('');
    const [signupCampus, setSignupCampus] = useState('1'); // Default 1 (Seoul)
    const [signupClassNo, setSignupClassNo] = useState('1');

    // --- Step 3: Login State ---
    const [loginEmail, setLoginEmail] = useState('');
    const [loginPassword, setLoginPassword] = useState('');

    // --- Step 4: Post State ---
    const [boards, setBoards] = useState<any[]>([]);
    const [boardId, setBoardId] = useState('1');
    const [postTitle, setPostTitle] = useState('');
    const [postContent, setPostContent] = useState('');

    // --- Step 0: Classes State ---
    const [classes, setClasses] = useState<any[]>([]);

    const addLog = (msg: string, isError = false) => {
        const timestamp = new Date().toLocaleTimeString();
        const logEntry = `[${timestamp}] ${isError ? 'ERROR: ' : ''}${msg}`;
        setLogs(prev => [logEntry, ...prev]);
        console.log(logEntry);
    };

    // --- Actions ---

    // 0. Fetch Classes
    const handleFetchClasses = async () => {
        try {
            addLog('Fetching classes...');
            const res = await axios.get(`${API_BASE_URL}/classes`);
            setClasses(res.data);
            addLog(`Fetched ${res.data.length} classes.`);
        } catch (err: any) {
            addLog(`Fetch Classes Failed: ${err.response?.data?.message || err.message}`, true);
        }
    };

    // 0-1. Fetch Boards
    const handleFetchBoards = async () => {
        if (!authToken) {
            alert('Please login first to fetch boards.');
            return;
        }
        try {
            addLog('Fetching boards...');
            const res = await axios.get(`${API_BASE_URL}/boards`, {
                headers: { Authorization: `Bearer ${authToken}` }
            });
            setBoards(res.data);
            addLog(`Fetched ${res.data.length} boards.`);
        } catch (err: any) {
            addLog(`Fetch Boards Failed: ${err.response?.data?.message || err.message}`, true);
        }
    };

    // 1-1. Send Verification Code
    const handleSendMmCode = async () => {
        if (!mmId || !signupName) {
            alert('Please enter Mattermost ID and Name');
            return;
        }
        try {
            addLog(`Sending auth code to Mattermost ID: ${mmId}, Name: ${signupName} (Gen: ${generation})...`);
            // POST /api/auth/send { targetUserId: "...", generation: ..., name: "..." }
            const res = await axios.post(`${API_BASE_URL}/auth/send`, {
                targetUserId: mmId,
                generation: parseInt(generation),
                name: signupName
            });
            addLog(`Code Sent! Response: ${res.data || 'OK'}`);
        } catch (err: any) {
            addLog(`Send Code Failed: ${err.response?.data?.message || err.message}`, true);
        }
    };

    // 1-2. Verify Code
    const handleVerifyMmCode = async () => {
        if (!mmId || !authCode) {
            alert('Please enter MM ID and Auth Code');
            return;
        }
        try {
            addLog(`Verifying code ${authCode} for ${mmId}...`);
            // POST /api/auth/verify { targetUserId: "...", authCode: "..." }
            const res = await axios.post(`${API_BASE_URL}/auth/verify`, {
                targetUserId: mmId,
                authCode: authCode
            });
            // Assuming 200 OK means success. API might return "인증되었습니다." string.
            addLog(`Verification Result: ${res.data}`);
            setIsMmVerified(true);
        } catch (err: any) {
            addLog(`Verification Failed: ${err.response?.data || err.message}`, true);
            setIsMmVerified(false);
        }
    };

    // 2. Sign Up
    const handleSignup = async () => {
        if (!isMmVerified) {
            alert('Please complete Mattermost verification first.');
            return;
        }
        if (!signupEmail || !signupPassword || !signupName) {
            alert('Email, Password, and Name are required.');
            return;
        }

        try {
            addLog(`Signing up with ${signupEmail}...`);
            // POST /api/members/signup
            // Nickname removed, using Name only as per previous instruction and DTO check
            const res = await axios.post(`${API_BASE_URL}/members/signup`, {
                email: signupEmail,
                password: signupPassword,
                name: signupName,
                mattermostId: mmId, // Must match the verified ID
                generation: parseInt(generation), // Reuse generation from Step 1
                campus: parseInt(signupCampus),
                classNo: parseInt(signupClassNo)
            });
            addLog(`Sign Up Success! Response: ${JSON.stringify(res.data)}`);

            // Auto-fill login
            setLoginEmail(signupEmail);
            setLoginPassword(signupPassword);
        } catch (err: any) {
            addLog(`Sign Up Failed: ${err.response?.data || err.message}`, true);
        }
    };

    // 3. Login
    const handleLogin = async () => {
        if (!loginEmail || !loginPassword) {
            alert('Email and Password are required.');
            return;
        }
        try {
            addLog(`Logging in as ${loginEmail}...`);
            const res = await axios.post(`${API_BASE_URL}/auth/login`, {
                email: loginEmail,
                password: loginPassword
            });

            const token = res.data.accessToken;
            if (token) {
                setAuthToken(token);
                setCurrentUser(loginEmail);
                addLog(`Login Success! Token acquired.`);
            } else {
                addLog('Login Success but NO Access Token found.', true);
            }
        } catch (err: any) {
            addLog(`Login Failed: ${err.response?.data?.message || err.message}`, true);
        }
    };

    // 4. Write Post
    const handleWritePost = async () => {
        if (!authToken) {
            alert('You must be logged in first.');
            return;
        }
        if (!postTitle || !postContent) {
            alert('Title and Content are required.');
            return;
        }

        try {
            // Corrected Endpoint: POST /api/posts
            // Body includes boardId
            const res = await axios.post(
                `${API_BASE_URL}/posts`,
                {
                    title: postTitle,
                    content: postContent,
                    boardId: parseInt(boardId)
                },
                {
                    headers: {
                        Authorization: `Bearer ${authToken}`
                    }
                }
            );
            addLog(`Post Created Successfully! ID: ${res.data.id || 'Unknown'}`);
            setPostTitle('');
            setPostContent('');
        } catch (err: any) {
            // Check for AI Censorship Error (C008)
            const errorCode = err.response?.data?.code;
            if (errorCode === 'C008') {
                addLog(`⚠️ 게시글이 차단되었습니다: ${err.response?.data?.message || '부적절한 내용 감지됨'}`, true);
                alert('🚫 AI 검열: 게시글에 부적절한 내용이 포함되어 있습니다.');
            } else {
                addLog(`Write Post Failed: ${err.response?.data?.message || err.message}`, true);
            }
        }
    };

    // --- Styles ---
    const sectionStyle = {
        border: '1px solid #ddd',
        borderRadius: '8px',
        padding: '20px',
        marginBottom: '20px',
        background: '#fff'
    };

    const inputStyle = {
        display: 'block',
        width: '100%',
        padding: '8px',
        marginBottom: '10px',
        boxSizing: 'border-box' as const,
        border: '1px solid #ccc',
        borderRadius: '4px'
    };

    const buttonStyle = {
        padding: '10px 20px',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        fontSize: '16px',
        fontWeight: 'bold',
        color: 'white',
        marginRight: '10px',
        marginBottom: '5px'
    };

    return (
        <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto', fontFamily: 'Arial, sans-serif', color: '#333' }}>
            <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>Backend Test Flow v2</h2>
            <div style={{ textAlign: 'center', marginBottom: '20px', color: '#666', fontSize: '0.9em' }}>
                Using Proxy: <code>/api</code> &rarr; <code>http://localhost:8080/api</code>
            </div>

            {/* Step 0: Check Classes */}
            <div style={{ ...sectionStyle, borderLeft: '5px solid #007bff' }}>
                <h3 style={{ marginTop: 0 }}>Step 0: Check Available Classes</h3>
                <button
                    onClick={handleFetchClasses}
                    style={{ ...buttonStyle, background: '#007bff', marginBottom: '10px' }}
                >
                    Fetch Classes
                </button>
                {classes.length > 0 && (
                    <div style={{ maxHeight: '150px', overflowY: 'auto', border: '1px solid #eee', padding: '5px' }}>
                        <ul style={{ paddingLeft: '20px', margin: 0 }}>
                            {classes.map((b: any) => (
                                <li
                                    key={b.id}
                                    style={{ cursor: 'pointer', padding: '2px 0', color: '#007bff', textDecoration: 'underline' }}
                                    onClick={() => {
                                        setGeneration(String(b.generation));
                                        setSignupCampus(String(b.campusId));
                                        setSignupClassNo(String(b.classNo));
                                        addLog(`Selected: ${b.name} (Gen ${b.generation}, Campus ${b.campusId}, Class ${b.classNo})`);
                                    }}
                                >
                                    {b.name} (Gen {b.generation}, Campus {b.campusId}, Class {b.classNo}, {b.trackType})
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            {/* Step 1: Mattermost Auth */}
            <div style={{ ...sectionStyle, borderLeft: '5px solid #6610f2' }}>
                <h3 style={{ marginTop: 0 }}>Step 1: Mattermost Verification</h3>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ fontWeight: 'bold' }}>1. Request Code</label>
                    <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                        <input
                            placeholder="Mattermost ID (e.g. kim_ssafy)"
                            value={mmId}
                            onChange={e => setMmId(e.target.value)}
                            disabled={isMmVerified}
                            style={{ ...inputStyle, flex: 2, marginBottom: 0 }}
                        />
                        <input
                            placeholder="Name (e.g. 김싸피)"
                            value={signupName}
                            onChange={e => setSignupName(e.target.value)}
                            disabled={isMmVerified}
                            style={{ ...inputStyle, flex: 1, marginBottom: 0 }}
                        />
                        <input
                            type="number"
                            placeholder="Gen"
                            value={generation}
                            onChange={e => setGeneration(e.target.value)}
                            disabled={isMmVerified}
                            style={{ ...inputStyle, flex: 0.5, marginBottom: 0 }}
                        />
                    </div>
                    <button
                        onClick={handleSendMmCode}
                        style={{ ...buttonStyle, background: '#6610f2' }}
                        disabled={isMmVerified}
                    >
                        Send Code to MM
                    </button>
                </div>

                <div style={{ borderTop: '1px dashed #eee', paddingTop: '15px' }}>
                    <label style={{ fontWeight: 'bold' }}>2. Verify Code</label>
                    <input
                        placeholder="Auth Code (Check Mattermost)"
                        value={authCode}
                        onChange={e => setAuthCode(e.target.value)}
                        disabled={isMmVerified}
                        style={inputStyle}
                    />
                    <button
                        onClick={handleVerifyMmCode}
                        style={{ ...buttonStyle, background: isMmVerified ? '#28a745' : '#fd7e14' }}
                        disabled={isMmVerified}
                    >
                        {isMmVerified ? 'Verified ✅' : 'Verify Code'}
                    </button>
                </div>
            </div>

            {/* Step 2: Sign Up */}
            <div style={{ ...sectionStyle, opacity: isMmVerified ? 1 : 0.6, pointerEvents: isMmVerified ? 'auto' : 'none' }}>
                <h3 style={{ marginTop: 0 }}>Step 2: Sign Up</h3>
                {!isMmVerified && <div style={{ color: 'red', marginBottom: '10px' }}>Complete Step 1 first.</div>}

                <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                    <input
                        placeholder="Mattermost ID (Verified)"
                        value={mmId}
                        readOnly
                        style={{ ...inputStyle, background: '#e9ecef', flex: 1, marginBottom: 0 }}
                    />
                    <input
                        placeholder="Gen"
                        value={generation}
                        readOnly
                        style={{ ...inputStyle, background: '#e9ecef', flex: 1, marginBottom: 0 }}
                    />
                </div>
                <input
                    placeholder="Email"
                    value={signupEmail}
                    onChange={e => setSignupEmail(e.target.value)}
                    style={inputStyle}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={signupPassword}
                    onChange={e => setSignupPassword(e.target.value)}
                    style={inputStyle}
                />
                <input
                    placeholder="Name"
                    value={signupName}
                    onChange={e => setSignupName(e.target.value)}
                    style={inputStyle}
                />
                <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                    <input
                        type="number"
                        placeholder="Campus ID"
                        value={signupCampus}
                        onChange={e => setSignupCampus(e.target.value)}
                        style={{ ...inputStyle, flex: 1, marginBottom: 0 }}
                    />
                    <input
                        type="number"
                        placeholder="Class No"
                        value={signupClassNo}
                        onChange={e => setSignupClassNo(e.target.value)}
                        style={{ ...inputStyle, flex: 1, marginBottom: 0 }}
                    />
                </div>
                <button
                    onClick={handleSignup}
                    style={{ ...buttonStyle, background: '#17a2b8', width: '100%' }}
                >
                    Sign Up
                </button>
            </div>

            {/* Step 3: Login */}
            <div style={sectionStyle}>
                <h3 style={{ marginTop: 0 }}>Step 3: Login</h3>
                {currentUser && <div style={{ color: 'green', marginBottom: '10px' }}>Logged in as: <strong>{currentUser}</strong></div>}

                <input
                    placeholder="Email"
                    value={loginEmail}
                    onChange={e => setLoginEmail(e.target.value)}
                    style={inputStyle}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={loginPassword}
                    onChange={e => setLoginPassword(e.target.value)}
                    style={inputStyle}
                />
                <button
                    onClick={handleLogin}
                    style={{ ...buttonStyle, background: '#007bff', width: '100%' }}
                >
                    Login
                </button>
            </div>

            {/* Step 4: Write Post */}
            <div style={{ ...sectionStyle, opacity: authToken ? 1 : 0.6, pointerEvents: authToken ? 'auto' : 'none' }}>
                <h3 style={{ marginTop: 0 }}>Step 4: Write Post</h3>
                {!authToken && <div style={{ color: 'red', marginBottom: '10px' }}>Login is required to write posts.</div>}

                <div style={{ marginBottom: '10px' }}>
                    <label>Board ID: </label>
                    <button
                        onClick={handleFetchBoards}
                        style={{ padding: '2px 5px', fontSize: '10px', marginLeft: '10px', cursor: 'pointer' }}
                    >
                        Fetch Boards
                    </button>
                    {boards.length > 0 ? (
                        <div style={{ marginTop: '5px', maxHeight: '100px', overflowY: 'auto', border: '1px solid #ccc', padding: '5px' }}>
                            {boards.map((b: any) => (
                                <div
                                    key={b.id}
                                    style={{
                                        padding: '3px',
                                        cursor: 'pointer',
                                        background: boardId === String(b.id) ? '#d1ecf1' : 'transparent',
                                        fontWeight: boardId === String(b.id) ? 'bold' : 'normal'
                                    }}
                                    onClick={() => {
                                        setBoardId(String(b.id));
                                        addLog(`Selected Board: ${b.name} (ID: ${b.id})`);
                                    }}
                                >
                                    {b.name} (ID: {b.id})
                                </div>
                            ))}
                        </div>
                    ) : (
                        <input
                            value={boardId}
                            onChange={e => setBoardId(e.target.value)}
                            placeholder="Enter Board ID or Fetch"
                            style={{ width: '100%', padding: '5px', marginTop: '5px' }}
                        />
                    )}
                </div>
                <input
                    placeholder="Title"
                    value={postTitle}
                    onChange={e => setPostTitle(e.target.value)}
                    style={inputStyle}
                />
                <textarea
                    placeholder="Content"
                    value={postContent}
                    onChange={e => setPostContent(e.target.value)}
                    rows={4}
                    style={{ ...inputStyle, resize: 'vertical' }}
                />
                <button
                    onClick={handleWritePost}
                    style={{ ...buttonStyle, background: '#28a745', width: '100%' }}
                    disabled={!authToken}
                >
                    Write Post
                </button>
            </div>

            {/* Logs */}
            <div style={{ background: '#f4f4f4', padding: '15px', borderRadius: '8px', border: '1px solid #ccc' }}>
                <strong>Activity Log:</strong>
                <div style={{
                    marginTop: '10px',
                    height: '150px',
                    overflowY: 'auto',
                    background: '#222',
                    color: '#0f0',
                    padding: '10px',
                    fontFamily: 'monospace',
                    fontSize: '12px'
                }}>
                    {logs.map((log, index) => (
                        <div key={index}>{log}</div>
                    ))}
                    {logs.length === 0 && <span style={{ color: '#666' }}>Waiting for user action...</span>}
                </div>
                <button
                    onClick={() => setLogs([])}
                    style={{ marginTop: '5px', padding: '5px 10px', fontSize: '12px' }}
                >
                    Clear Logs
                </button>
            </div>
        </div>
    );
}

export default TestPage;
