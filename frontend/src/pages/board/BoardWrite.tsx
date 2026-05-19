import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdArrowBack, MdClose, MdAddPhotoAlternate, MdPoll, MdCheck } from 'react-icons/md';
import { postService, boardService, uploadService } from '../../services/api';
import type { BoardModel } from '../../services/api';

const BoardWrite: React.FC = () => {
    const navigate = useNavigate();
    const fileInputRef = useRef<HTMLInputElement>(null);

    // State
    const [boards, setBoards] = useState<BoardModel[]>([]);
    const [selectedBoardId, setSelectedBoardId] = useState<number | null>(null);
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [images, setImages] = useState<File[]>([]);
    const [imagePreviews, setImagePreviews] = useState<string[]>([]);
    const [loading, setLoading] = useState(false);

    // Poll State
    const [isPollEnabled, setIsPollEnabled] = useState(false);
    const [pollTitle, setPollTitle] = useState('');
    const [pollOptions, setPollOptions] = useState<string[]>(['', '']);

    useEffect(() => {
        const loadBoards = async () => {
            try {
                const res = await boardService.getBoards();
                setBoards(res);
            } catch (e) {
                console.error("Failed to load boards", e);
                // Fallback for dev/demo if API is not ready
                setBoards([
                    { id: 1, name: '자유게시판', description: '' },
                    { id: 2, name: '질문게시판', description: '' },
                    { id: 3, name: '정보게시판', description: '' }
                ]);
            }
        };
        loadBoards();
        loadBoards();
    }, []);

    // Handlers
    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const fileArray = Array.from(e.target.files);
            setImages(prev => [...prev, ...fileArray]);

            const newPreviews = fileArray.map(file => URL.createObjectURL(file));
            setImagePreviews(prev => [...prev, ...newPreviews]);
        }
    };

    const removeImage = (index: number) => {
        setImages(prev => prev.filter((_, i) => i !== index));
        setImagePreviews(prev => prev.filter((_, i) => i !== index));
    };

    const handlePollOptionChange = (index: number, value: string) => {
        const newOptions = [...pollOptions];
        newOptions[index] = value;
        setPollOptions(newOptions);
    };

    const addPollOption = () => {
        if (pollOptions.length < 5) setPollOptions([...pollOptions, '']);
    };

    const removePollOption = (index: number) => {
        if (pollOptions.length > 2) {
            setPollOptions(pollOptions.filter((_, i) => i !== index));
        }
    };

    const handleSubmit = async () => {
        if (!selectedBoardId) {
            alert('게시판을 선택해주세요.');
            return;
        }
        if (!title.trim() || !content.trim()) {
            alert('제목과 내용을 입력해주세요.');
            return;
        }

        setLoading(true);
        try {
            // 1. Upload Images First
            const uploadedUrls: string[] = [];
            if (images.length > 0) {
                // Upload sequentially or parallel? sequential is safer for order if important, parallel faster.
                // Let's do parallel execution for speed.
                const uploadPromises = images.map(file => uploadService.uploadImage(file));
                const results = await Promise.all(uploadPromises);
                uploadedUrls.push(...results);
            }

            // 2. Prepare Poll Data
            let pollData = undefined;
            if (isPollEnabled && pollTitle.trim()) {
                const validOptions = pollOptions.filter(o => o.trim());
                if (validOptions.length >= 2) {
                    pollData = {
                        title: pollTitle,
                        options: validOptions
                    };
                }
            }

            // 3. Create Post
            const postRequest = {
                title,
                content,
                boardId: selectedBoardId,
                imageUrls: uploadedUrls,
                poll: pollData
            };

            await postService.createPost(postRequest);
            alert('게시글이 등록되었습니다.');
            navigate('/board');
        } catch (error) {
            console.error(error);
            alert('글 작성에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ minHeight: '100vh', backgroundColor: 'var(--background)', padding: '40px 20px' }}>
            <div className="container" style={{ maxWidth: '800px', margin: '0 auto' }}>
                {/* Header Card */}
                <div style={{
                    backgroundColor: 'var(--surface)',
                    borderRadius: '24px',
                    border: '1px solid var(--border-color)',
                    padding: '32px 40px',
                    marginBottom: '24px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.03)'
                }}>
                    <h2 style={{ fontSize: '24px', fontWeight: '800', color: 'var(--on-surface)', margin: 0 }}>글 쓰기</h2>
                    <div
                        onClick={() => navigate(-1)}
                        style={{
                            width: '40px',
                            height: '40px',
                            borderRadius: '12px',
                            backgroundColor: 'var(--field-bg)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            cursor: 'pointer',
                            color: 'var(--on-surface-variant)',
                            transition: 'all 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(0,0,0,0.05)'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'var(--field-bg)'}
                    >
                        <MdClose size={24} />
                    </div>
                </div>

                {/* Form Card */}
                <div style={{
                    backgroundColor: 'var(--surface)',
                    borderRadius: '24px',
                    border: '1px solid var(--border-color)',
                    padding: '40px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.03)'
                }}>
                    {/* Board Select */}
                    <div style={{ marginBottom: '24px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '700', color: 'var(--on-surface-variant)', marginBottom: '8px' }}>게시판 선택</label>
                        <select
                            style={{
                                width: '100%',
                                padding: '14px 16px',
                                borderRadius: '12px',
                                border: '2px solid var(--border-color)',
                                fontSize: '15px',
                                outline: 'none',
                                background: 'var(--field-bg)',
                                color: 'var(--on-surface)',
                                cursor: 'pointer',
                                transition: 'border-color 0.2s'
                            }}
                            value={selectedBoardId || ''}
                            onChange={(e) => setSelectedBoardId(Number(e.target.value))}
                            onFocus={(e) => e.target.style.borderColor = 'var(--primary)'}
                            onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                        >
                            <option value="" disabled>게시판을 선택하세요</option>
                            {boards.map(b => (
                                <option key={b.id} value={b.id}>{b.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* Title */}
                    <div style={{ marginBottom: '24px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '700', color: 'var(--on-surface-variant)', marginBottom: '8px' }}>제목</label>
                        <input
                            type="text"
                            placeholder="제목을 입력하세요"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '16px 20px',
                                fontSize: '18px',
                                fontWeight: '700',
                                borderRadius: '12px',
                                border: '2px solid var(--border-color)',
                                outline: 'none',
                                background: 'var(--field-bg)',
                                color: 'var(--on-surface)',
                                transition: 'border-color 0.2s'
                            }}
                            onFocus={(e) => e.target.style.borderColor = 'var(--primary)'}
                            onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                        />
                    </div>

                    {/* Content */}
                    <div style={{ marginBottom: '24px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '700', color: 'var(--on-surface-variant)', marginBottom: '8px' }}>내용</label>
                        <textarea
                            placeholder="나누고 싶은 이야기를 적어보세요."
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            style={{
                                width: '100%',
                                minHeight: '400px',
                                padding: '20px',
                                fontSize: '16px',
                                borderRadius: '12px',
                                border: '2px solid var(--border-color)',
                                resize: 'none',
                                outline: 'none',
                                lineHeight: '1.6',
                                background: 'var(--field-bg)',
                                color: 'var(--on-surface)',
                                transition: 'border-color 0.2s'
                            }}
                            onFocus={(e) => e.target.style.borderColor = 'var(--primary)'}
                            onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                        />
                    </div>

                    {/* Image Previews */}
                    {imagePreviews.length > 0 && (
                        <div style={{ display: 'flex', gap: '12px', overflowX: 'auto', padding: '16px', backgroundColor: 'var(--field-bg)', borderRadius: '12px', marginBottom: '24px' }}>
                            {imagePreviews.map((src, i) => (
                                <div key={i} style={{ position: 'relative', flexShrink: 0 }}>
                                    <img src={src} alt="preview" style={{ width: '100px', height: '100px', objectFit: 'cover', borderRadius: '12px' }} />
                                    <div
                                        onClick={() => removeImage(i)}
                                        style={{
                                            position: 'absolute', top: -6, right: -6, background: 'var(--error)', color: 'white',
                                            borderRadius: '50%', width: '24px', height: '24px', display: 'flex',
                                            alignItems: 'center', justifyContent: 'center', cursor: 'pointer', fontSize: '12px',
                                            boxShadow: '0 2px 6px rgba(0,0,0,0.2)', fontWeight: 'bold'
                                        }}
                                    >
                                        <MdClose />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Poll Section */}
                    {isPollEnabled && (
                        <div style={{ background: 'rgba(100, 149, 235, 0.05)', padding: '24px', borderRadius: '16px', marginBottom: '24px', border: '1px solid rgba(100, 149, 235, 0.2)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                                <h4 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--primary)', margin: 0 }}>투표 설정</h4>
                                <MdClose
                                    size={20}
                                    style={{ cursor: 'pointer', color: 'var(--on-surface-variant)' }}
                                    onClick={() => setIsPollEnabled(false)}
                                />
                            </div>
                            <input
                                placeholder="투표 제목을 입력하세요"
                                value={pollTitle}
                                onChange={(e) => setPollTitle(e.target.value)}
                                style={{
                                    width: '100%', padding: '12px 16px', marginBottom: '16px', borderRadius: '10px',
                                    border: '1px solid var(--border-color)', background: 'var(--surface)', color: 'var(--on-surface)',
                                    fontSize: '15px'
                                }}
                            />
                            {pollOptions.map((opt, i) => (
                                <div key={i} style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                                    <input
                                        placeholder={`항목 ${i + 1}`}
                                        value={opt}
                                        onChange={(e) => handlePollOptionChange(i, e.target.value)}
                                        style={{ flex: 1, padding: '12px 16px', borderRadius: '10px', border: '1px solid var(--border-color)', background: 'var(--surface)', color: 'var(--on-surface)', fontSize: '14px' }}
                                    />
                                    {pollOptions.length > 2 && (
                                        <button
                                            onClick={() => removePollOption(i)}
                                            style={{ color: 'var(--error)', border: 'none', background: 'none', cursor: 'pointer', fontWeight: '600' }}
                                        >
                                            삭제
                                        </button>
                                    )}
                                </div>
                            ))}
                            {pollOptions.length < 5 && (
                                <button
                                    onClick={addPollOption}
                                    style={{
                                        width: '100%', padding: '12px', fontSize: '14px', color: 'var(--primary)',
                                        background: 'var(--surface)', border: '1px dashed var(--primary)',
                                        borderRadius: '10px', cursor: 'pointer', fontWeight: '600', marginTop: '8px'
                                    }}
                                >
                                    + 선택 항목 추가
                                </button>
                            )}
                        </div>
                    )}

                    {/* Toolbar & Submit */}
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '32px', borderTop: '1px solid var(--border-color)', paddingTop: '32px' }}>
                        <div style={{ display: 'flex', gap: '24px' }}>
                            <div
                                onClick={() => fileInputRef.current?.click()}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', color: 'var(--on-surface-variant)', fontWeight: '600' }}
                            >
                                <div style={{ width: '40px', height: '40px', borderRadius: '10px', backgroundColor: 'var(--field-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)' }}>
                                    <MdAddPhotoAlternate size={22} />
                                </div>
                                <span style={{ fontSize: '14px' }}>사진 첨부</span>
                                <input type="file" multiple accept="image/*" ref={fileInputRef} style={{ display: 'none' }} onChange={handleImageChange} />
                            </div>
                            <div
                                onClick={() => setIsPollEnabled(!isPollEnabled)}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', color: isPollEnabled ? 'var(--primary)' : 'var(--on-surface-variant)', fontWeight: '600' }}
                            >
                                <div style={{ width: '40px', height: '40px', borderRadius: '10px', backgroundColor: isPollEnabled ? 'rgba(100, 149, 235, 0.1)' : 'var(--field-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: isPollEnabled ? 'var(--primary)' : 'var(--on-surface-variant)' }}>
                                    <MdPoll size={22} />
                                </div>
                                <span style={{ fontSize: '14px' }}>투표 추가</span>
                            </div>
                        </div>

                        <button
                            onClick={handleSubmit}
                            disabled={loading}
                            style={{
                                padding: '14px 40px',
                                borderRadius: '14px',
                                background: 'var(--primary)',
                                color: 'white',
                                fontWeight: '800',
                                border: 'none',
                                cursor: loading ? 'default' : 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                fontSize: '16px',
                                boxShadow: '0 4px 12px rgba(100, 149, 235, 0.3)',
                                opacity: loading ? 0.7 : 1,
                                transition: 'transform 0.2s'
                            }}
                            onMouseEnter={(e) => !loading && (e.currentTarget.style.transform = 'scale(1.02)')}
                            onMouseLeave={(e) => !loading && (e.currentTarget.style.transform = 'scale(1)')}
                        >
                            {loading ? (
                                <div className="loading-spinner" style={{ width: '18px', height: '18px', borderWidth: '2px' }} />
                            ) : (
                                <MdCheck size={22} />
                            )}
                            <span>{loading ? '등록 중...' : '등록하기'}</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BoardWrite;
