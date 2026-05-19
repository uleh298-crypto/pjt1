import React from 'react';
import type { PollResponse, PollOptionResponse } from '../../../services/api';

interface PollCardProps {
    poll: PollResponse;
    onVote: (optionId: number) => void;
    isVoting?: boolean;
}

const PollCard: React.FC<PollCardProps> = ({ poll, onVote, isVoting = false }) => {
    const totalVotes = Math.max(poll.totalVotes, 0);

    return (
        <div style={{
            background: 'var(--surface)',
            borderRadius: '12px',
            padding: '20px',
            marginTop: '20px',
            border: '1px solid var(--border-color)',
            boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
            position: 'relative',
            opacity: isVoting ? 0.6 : 1,
            pointerEvents: isVoting ? 'none' : 'auto',
            transition: 'opacity 0.2s ease'
        }}>
            {/* Loading Spinner */}
            {isVoting && (
                <div style={{
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: 'translate(-50%, -50%)',
                    zIndex: 10
                }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        border: '4px solid var(--border-color)',
                        borderTop: '4px solid var(--primary)',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite'
                    }} />
                </div>
            )}

            <h4 style={{
                fontSize: '16px',
                fontWeight: 'bold',
                marginBottom: '16px',
                color: 'var(--on-surface)'
            }}>
                투표
            </h4>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {poll.options.map((option) => {
                    const ratio = totalVotes === 0 ? 0 : (option.voteCount / totalVotes) * 100;
                    const isSelected = poll.myVotedOptionId === option.optionId;

                    return (
                        <div
                            key={option.optionId}
                            onClick={() => !isVoting && onVote(option.optionId)}
                            style={{
                                background: isSelected ? 'rgba(100, 149, 235, 0.1)' : 'var(--field-bg)',
                                borderRadius: '10px',
                                padding: '14px 16px',
                                cursor: isVoting ? 'not-allowed' : 'pointer',
                                transition: 'all 0.2s ease',
                                border: `2px solid ${isSelected ? 'var(--primary)' : 'var(--border-color)'}`,
                            }}
                            onMouseEnter={(e) => {
                                if (!isSelected && !isVoting) {
                                    e.currentTarget.style.backgroundColor = 'var(--field-bg)';
                                    e.currentTarget.style.borderColor = 'var(--primary)';
                                }
                            }}
                            onMouseLeave={(e) => {
                                if (!isSelected && !isVoting) {
                                    e.currentTarget.style.backgroundColor = 'var(--field-bg)';
                                    e.currentTarget.style.borderColor = 'var(--border-color)';
                                }
                            }}
                        >
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                marginBottom: '10px'
                            }}>
                                <span style={{
                                    fontSize: '15px',
                                    fontWeight: isSelected ? '600' : '500',
                                    color: isSelected ? 'var(--primary)' : 'var(--on-surface)'
                                }}>
                                    {option.text}
                                </span>
                                <span style={{
                                    fontSize: '14px',
                                    color: 'var(--on-surface-variant)',
                                    fontWeight: '500'
                                }}>
                                    {option.voteCount}표 ({ratio.toFixed(1)}%)
                                </span>
                            </div>

                            {/* Progress Bar */}
                            <div style={{
                                width: '100%',
                                height: '8px',
                                backgroundColor: 'var(--border-color)',
                                borderRadius: '4px',
                                overflow: 'hidden'
                            }}>
                                <div style={{
                                    width: `${ratio}%`,
                                    height: '100%',
                                    backgroundColor: isSelected ? 'var(--primary)' : 'var(--on-surface-variant)',
                                    borderRadius: '4px',
                                    transition: 'width 0.4s ease'
                                }} />
                            </div>
                        </div>
                    );
                })}
            </div>

            <div style={{
                marginTop: '16px',
                paddingTop: '12px',
                borderTop: '1px solid var(--border-color)',
                fontSize: '14px',
                color: 'var(--on-surface-variant)',
                fontWeight: '500'
            }}>
                총 {poll.totalVotes}표
            </div>

            {/* CSS Animation */}
            <style>{`
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `}</style>
        </div>
    );
};

export default PollCard;
