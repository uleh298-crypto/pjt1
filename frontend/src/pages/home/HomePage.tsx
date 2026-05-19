import React, { useEffect, useState } from 'react';
import { homeService, ddayService, memberService, authService } from '../../services/api';
import type {
    HomeModel, DDayModel,
} from '../../services/api';
import { MdCalendarToday, MdArrowForwardIos, MdChevronLeft, MdChevronRight } from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import ScheduleModal from './ScheduleModal';

const HomePage: React.FC = () => {
    const navigate = useNavigate();
    const [ddayItems, setDdayItems] = useState<DDayModel[]>([]);
    const [data, setData] = useState<HomeModel | null>(null);
    const [loading, setLoading] = useState(false);
    const [selectedCampus, setSelectedCampus] = useState("서울");
    const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                // Fetch Home stats, D-Day list, and My Info
                const [homeRes, ddayRes, myInfoRes] = await Promise.all([
                    homeService.getHome(),
                    ddayService.getDdays(),
                    memberService.getMyInfo().catch(() => null) // Allow failure for myInfo
                ]);
                setData(homeRes);

                // Default to user's campus if available, otherwise first available meal
                if (myInfoRes && myInfoRes.campus) {
                    setSelectedCampus(myInfoRes.campus);
                } else if (homeRes.campusMeals && homeRes.campusMeals.length > 0) {
                    setSelectedCampus(homeRes.campusMeals[0].campusName);
                }

                // Calculate Salary Day & Merge logic (similar to Modal)
                const today = new Date();
                const year = today.getFullYear();
                const month = today.getMonth();

                // Salary logic
                const getSalaryDay = (y: number, m: number): number => {
                    const d = new Date(y, m, 15);
                    while (d.getDay() === 0 || d.getDay() === 6 || isHoliday(d)) {
                        d.setDate(d.getDate() + 1);
                    }
                    return d.getDate();
                };
                const isHoliday = (d: Date) => {
                    const m = d.getMonth(); const date = d.getDate();
                    // Simple fixed holidays
                    if (m === 0 && date === 1) return true;
                    if (m === 2 && date === 1) return true;
                    if (m === 4 && date === 5) return true;
                    if (m === 5 && date === 6) return true;
                    if (m === 7 && date === 15) return true;
                    if (m === 9 && date === 3) return true;
                    if (m === 9 && date === 9) return true;
                    if (m === 11 && date === 25) return true;
                    return false;
                };

                const salaryDay = getSalaryDay(year, month);
                const salaryDate = new Date(year, month, salaryDay);

                // Calculate D-Day helper
                const getDDayDays = (target: Date) => {
                    const t = new Date(target); t.setHours(0, 0, 0, 0);
                    const n = new Date(); n.setHours(0, 0, 0, 0);
                    return Math.ceil((t.getTime() - n.getTime()) / (1000 * 60 * 60 * 24));
                };

                const newDDays: DDayModel[] = [];

                // 1. Server D-Days
                ddayRes.items.forEach(item => {
                    const dateStr = item.targetDate.replace(/\./g, '-');
                    newDDays.push({ title: item.title, days: getDDayDays(new Date(dateStr)) });
                });

                // 2. Salary Day (Add if upcoming within reason)
                if (salaryDate >= new Date(new Date().setHours(0, 0, 0, 0))) {
                    newDDays.push({ title: '월급날', days: getDDayDays(salaryDate) });
                }

                // Sort by D-Day (ascending, closest first)
                newDDays.sort((a, b) => a.days - b.days);

                // Use the calculated list instead of homeRes.dDays
                setDdayItems(newDDays.slice(0, 5)); // Show top 5

            } catch (err: any) {
                console.error(err);
                if (err.response?.status === 401) {
                    navigate('/login');
                }
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [navigate]);

    if (loading) return <div style={{ padding: 20 }}>Loading...</div>;

    const displayDDays = ddayItems.length > 0 ? ddayItems : (data?.dDays || []);
    const boards = data?.boardsList || [];
    const meals = data?.campusMeals || [];
    const currentMeal = meals.find(m => m.campusName === selectedCampus) || (meals.length > 0 ? meals[0] : null);

    return (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: '24px' }}>
            <ScheduleModal isOpen={isScheduleModalOpen} onClose={() => setIsScheduleModalOpen(false)} />

            {/* Left Column: Main Content */}
            <div>
                {/* Board Section */}
                <div>
                    <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: 'var(--on-surface)', marginBottom: '12px' }}>전체 게시판</h3>
                    <div style={{ background: 'var(--surface)', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                        {boards.length > 0 ? boards.map((b, i) => (
                            <div key={b.boardId}>
                                <div
                                    onClick={() => navigate(`/board/${b.boardId}`)}
                                    style={{ padding: '16px', display: 'flex', gap: '12px', cursor: 'pointer', alignItems: 'center' }}
                                >
                                    <div style={{ fontWeight: 'bold', fontSize: '15px', width: '160px', color: 'var(--on-surface)' }}>{b.name}</div>
                                    <div style={{ fontSize: '14px', color: 'var(--on-surface-variant)', flex: 1, textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                        {b.recentPostTitle || "최근 게시물이 없습니다."}
                                    </div>
                                    <MdArrowForwardIos size={12} color="var(--border-color)" />
                                </div>
                                {i < boards.length - 1 && <div style={{ height: '1px', background: 'var(--border-color)', margin: '0 16px' }}></div>}
                            </div>
                        )) : (
                            <div style={{ padding: '32px', textAlign: 'center', color: 'var(--on-surface-variant)' }}>게시판이 없습니다.</div>
                        )}
                    </div>
                </div>
            </div>

            {/* Right Column: Widgets */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                {/* Profile / Greeting Widget */}
                <div style={{ background: 'var(--surface)', padding: '24px', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
                    <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '8px', color: 'var(--on-surface)' }}>반가워요, 싸용자님!</h2>
                    <p style={{ fontSize: '14px', color: 'var(--on-surface-variant)', marginBottom: '20px', lineHeight: '1.5' }}>
                        오늘도 싸브리타임에서<br />즐거운 커뮤니티 활동 되세요.
                    </p>
                    <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="btn btn-primary" style={{ flex: 1, fontSize: '13px', padding: '8px' }} onClick={() => navigate('/mypage')}>내 정보</button>
                        <button className="btn btn-outline" style={{ flex: 1, fontSize: '13px', padding: '8px' }} onClick={() => { authService.logout(); navigate('/login'); }}>로그아웃</button>
                    </div>
                </div>

                {/* D-Day Widget */}
                <div
                    style={{ border: '1px solid var(--border-color)', padding: '20px', borderRadius: '16px', background: 'var(--surface)', cursor: 'pointer' }}
                    onClick={() => setIsScheduleModalOpen(true)}
                >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px', color: 'var(--primary)' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <MdCalendarToday />
                            <span style={{ fontWeight: 'bold' }}>주요 일정</span>
                        </div>
                        <MdArrowForwardIos size={12} color="var(--on-surface-variant)" />
                    </div>
                    {displayDDays.length > 0 ? displayDDays.map((d, i) => (
                        <div key={i} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '14px', color: 'var(--on-surface)' }}>
                            <span>{d.title}</span>
                            <span style={{ fontWeight: 'bold', color: 'var(--primary)' }}>D{d.days > 0 ? `-${d.days}` : d.days === 0 ? '-DAY' : `+${-d.days}`}</span>
                        </div>
                    )) : (
                        <div style={{ fontSize: '13px', color: 'var(--on-surface-variant)', textAlign: 'center' }}>일정이 없습니다.</div>
                    )}
                </div>

                {/* Lunch Widget */}
                <div style={{ border: '1px solid var(--border-color)', padding: '20px', borderRadius: '16px', background: 'var(--surface)' }}>
                    <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: 'var(--on-surface)', marginBottom: '12px' }}>오늘의 점심</h3>
                    <div style={{ display: 'flex', gap: '4px', marginBottom: '16px', flexWrap: 'wrap' }}>
                        {['서울', '대전', '광주', '구미', '부울경'].map(campus => (
                            <span
                                key={campus}
                                onClick={() => { setSelectedCampus(campus); setCurrentImageIndex(0); }}
                                style={{
                                    fontSize: '11px', cursor: 'pointer', padding: '4px 8px', borderRadius: '12px',
                                    background: selectedCampus === campus ? 'var(--primary)' : 'var(--field-bg)',
                                    color: selectedCampus === campus ? 'white' : 'var(--on-surface-variant)',
                                    whiteSpace: 'nowrap'
                                }}
                            >
                                {campus}
                            </span>
                        ))}
                    </div>

                    {currentMeal ? (
                        <div>
                            <div style={{ fontSize: '14px', fontWeight: '600', marginBottom: '8px', color: 'var(--on-surface)' }}>{currentMeal.campusName} 캠퍼스</div>

                            {currentMeal.imageUrls.length > 0 ? (
                                <div style={{ position: 'relative', borderRadius: '8px', overflow: 'hidden', backgroundColor: '#000' }}>
                                    <div style={{ position: 'relative', width: '100%', paddingBottom: '133%' }}>
                                        <img
                                            src={currentMeal.imageUrls[currentImageIndex]}
                                            alt={`Lunch ${currentImageIndex + 1}`}
                                            style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', objectFit: 'contain' }}
                                        />
                                    </div>

                                    {/* Slider Controls */}
                                    {currentMeal.imageUrls.length > 1 && (
                                        <>
                                            <div
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setCurrentImageIndex(prev => prev === 0 ? currentMeal.imageUrls.length - 1 : prev - 1);
                                                }}
                                                style={{
                                                    position: 'absolute', left: '8px', top: '50%', transform: 'translateY(-50%)',
                                                    backgroundColor: 'rgba(0,0,0,0.5)', borderRadius: '50%', padding: '4px', cursor: 'pointer',
                                                    display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                                                }}
                                            >
                                                <MdChevronLeft size={20} />
                                            </div>
                                            <div
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setCurrentImageIndex(prev => prev === currentMeal.imageUrls.length - 1 ? 0 : prev + 1);
                                                }}
                                                style={{
                                                    position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)',
                                                    backgroundColor: 'rgba(0,0,0,0.5)', borderRadius: '50%', padding: '4px', cursor: 'pointer',
                                                    display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                                                }}
                                            >
                                                <MdChevronRight size={20} />
                                            </div>

                                            {/* Indicators */}
                                            <div style={{
                                                position: 'absolute', bottom: '8px', left: '50%', transform: 'translateX(-50%)',
                                                display: 'flex', gap: '4px'
                                            }}>
                                                {currentMeal.imageUrls.map((_, idx) => (
                                                    <div
                                                        key={idx}
                                                        style={{
                                                            width: '6px', height: '6px', borderRadius: '50%',
                                                            backgroundColor: idx === currentImageIndex ? 'white' : 'rgba(255,255,255,0.5)'
                                                        }}
                                                    />
                                                ))}
                                            </div>
                                        </>
                                    )}
                                </div>
                            ) : (
                                <div style={{ height: '120px', background: 'var(--field-bg)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', color: 'var(--on-surface-variant)' }}>이미지 없음</div>
                            )}
                        </div>
                    ) : (
                        <div style={{ fontSize: '13px', color: 'var(--on-surface-variant)' }}>식단 정보가 없습니다.</div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default HomePage;
