import React, { useState, useEffect, useMemo } from 'react';
import { MdClose, MdChevronLeft, MdChevronRight, MdEvent, MdPayments } from 'react-icons/md';
import { ddayService, type DdayItem } from '../../services/api';

interface ScheduleModalProps {
    isOpen: boolean;
    onClose: () => void;
}

const ScheduleModal: React.FC<ScheduleModalProps> = ({ isOpen, onClose }) => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [events, setEvents] = useState<DdayItem[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            loadDdays();
        }
    }, [isOpen]);

    const loadDdays = async () => {
        setLoading(true);
        try {
            const res = await ddayService.getDdays();
            setEvents(res.items);
        } catch (err) {
            console.error("Failed to load D-days", err);
        } finally {
            setLoading(false);
        }
    };

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth(); // 0-indexed

    // Calculate Salary Day
    const getSalaryDay = (y: number, m: number): number => {
        const d = new Date(y, m, 15);
        while (isWeekendOrHoliday(d)) {
            d.setDate(d.getDate() + 1);
        }
        return d.getDate();
    };

    const isWeekendOrHoliday = (d: Date): boolean => {
        const day = d.getDay();
        if (day === 0 || day === 6) return true; // Sun, Sat

        const m = d.getMonth(); // 0-11
        const date = d.getDate();

        // Simple fixed holidays (Solar)
        // 1.1, 3.1, 5.5, 6.6, 8.15, 10.3, 10.9, 12.25
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

    const salaryDay = useMemo(() => getSalaryDay(year, month), [year, month]);

    // Calendar Grid Logic
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDayOfMonth = new Date(year, month, 1).getDay(); // 0(Sun) - 6(Sat)

    const gridDays = [];
    // Empty slots for previous month
    for (let i = 0; i < firstDayOfMonth; i++) {
        gridDays.push(null);
    }
    // Days actual
    for (let i = 1; i <= daysInMonth; i++) {
        gridDays.push(i);
    }

    const prevMonth = () => {
        setCurrentDate(new Date(year, month - 1, 1));
    };

    const nextMonth = () => {
        setCurrentDate(new Date(year, month + 1, 1));
    };

    const isToday = (d: number) => {
        const today = new Date();
        return today.getFullYear() === year && today.getMonth() === month && today.getDate() === d;
    };

    // calculate D-Day string
    const getDDayString = (dDay: number) => {
        if (dDay === 0) return "D-DAY";
        if (dDay > 0) return `D-${dDay}`;
        return `D+${-dDay}`;
    };

    // Filter events for this month to highlight?
    // Or just show dots.
    const getEventsForDay = (d: number) => {
        const dateStr = `${year}.${String(month + 1).padStart(2, '0')}.${String(d).padStart(2, '0')}`;
        // TargetDate format from server is usually yyyy-MM-dd, but let's check formatApiDate in Android which handles replace.
        // Let's assume server sends yyyy-MM-dd.
        const dateStrHyphen = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;

        return events.filter(e => e.targetDate === dateStrHyphen || e.targetDate.replace(/-/g, '.') === dateStr);
    };

    if (!isOpen) return null;

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000,
            display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
            <div style={{
                backgroundColor: 'var(--surface)', width: '360px', borderRadius: '16px',
                boxShadow: '0 4px 20px rgba(0,0,0,0.15)', overflow: 'hidden',
                maxHeight: '90vh', display: 'flex', flexDirection: 'column'
            }}>
                {/* Header */}
                <div style={{ padding: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: 'var(--on-surface)' }}>주요 일정</h3>
                    <div style={{ cursor: 'pointer' }} onClick={onClose}>
                        <MdClose size={24} color="var(--on-surface-variant)" />
                    </div>
                </div>

                <div style={{ padding: '20px', overflowY: 'auto' }}>
                    {/* Calendar Nav */}
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
                        <div onClick={prevMonth} style={{ cursor: 'pointer', padding: '4px' }}><MdChevronLeft size={24} /></div>
                        <div style={{ fontSize: '16px', fontWeight: 'bold' }}>{year}년 {month + 1}월</div>
                        <div onClick={nextMonth} style={{ cursor: 'pointer', padding: '4px' }}><MdChevronRight size={24} /></div>
                    </div>

                    {/* Calendar Grid */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px', textAlign: 'center', marginBottom: '8px' }}>
                        {['일', '월', '화', '수', '목', '금', '토'].map((d, i) => (
                            <div key={d} style={{ fontSize: '12px', fontWeight: 'bold', color: i === 0 ? 'var(--error)' : 'var(--on-surface-variant)' }}>{d}</div>
                        ))}
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px' }}>
                        {gridDays.map((d, i) => {
                            if (d === null) return <div key={`empty-${i}`} />;

                            const dayEvents = getEventsForDay(d);
                            const isSalary = d === salaryDay;

                            return (
                                <div key={d} style={{
                                    height: '40px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                                    borderRadius: '8px',
                                    backgroundColor: isToday(d) ? 'var(--primary)' : 'transparent',
                                    color: isToday(d) ? 'white' : 'var(--on-surface)',
                                    cursor: 'default',
                                    position: 'relative'
                                }}>
                                    <span style={{ fontSize: '13px', fontWeight: isToday(d) ? 'bold' : 'normal' }}>{d}</span>

                                    {/* Indicators */}
                                    <div style={{ display: 'flex', gap: '2px', marginTop: '2px' }}>
                                        {dayEvents.length > 0 && (
                                            <div style={{ width: '4px', height: '4px', borderRadius: '50%', backgroundColor: isToday(d) ? 'white' : 'var(--primary)' }} />
                                        )}
                                        {isSalary && (
                                            <div style={{ width: '4px', height: '4px', borderRadius: '50%', backgroundColor: isToday(d) ? '#FFE082' : 'var(--error)' }} />
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    <div style={{ height: '1px', backgroundColor: 'var(--border-color)', margin: '20px 0' }}></div>

                    {/* List Section */}
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                            <span style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--on-surface)' }}>이달의 일정</span>
                            <span style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>총 {events.length + (salaryDay ? 1 : 0)}개</span>
                        </div>

                        {loading ? (
                            <div style={{ textAlign: 'center', padding: '20px', fontSize: '13px', color: 'var(--on-surface-variant)' }}>Loading...</div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                {/* Salary Event */}
                                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px', backgroundColor: 'var(--field-bg)', borderRadius: '12px' }}>
                                    <div style={{ width: '36px', height: '36px', borderRadius: '8px', backgroundColor: 'rgba(255, 82, 82, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        <MdPayments size={20} color="var(--error)" />
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--on-surface)' }}>월급날</div>
                                        <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>{year}.{String(month + 1).padStart(2, '0')}.{String(salaryDay).padStart(2, '0')}</div>
                                    </div>
                                    <div style={{ fontSize: '12px', fontWeight: 'bold', color: 'var(--error)', backgroundColor: 'rgba(255, 82, 82, 0.1)', padding: '4px 8px', borderRadius: '6px' }}>
                                        {(() => {
                                            const today = new Date();
                                            today.setHours(0, 0, 0, 0);
                                            const sDay = new Date(year, month, salaryDay);
                                            const diff = Math.ceil((sDay.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
                                            return getDDayString(diff);
                                        })()}
                                    </div>
                                </div>

                                {/* Server Events */}
                                {events.map(event => (
                                    <div key={event.id} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px', backgroundColor: 'var(--surface)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                                        <div style={{ width: '36px', height: '36px', borderRadius: '8px', backgroundColor: 'rgba(91, 127, 255, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                            <MdEvent size={20} color="var(--primary)" />
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--on-surface)' }}>{event.title}</div>
                                            <div style={{ fontSize: '12px', color: 'var(--on-surface-variant)' }}>{event.targetDate.replace(/-/g, '.')}</div>
                                        </div>
                                        <div style={{ fontSize: '12px', fontWeight: 'bold', color: 'var(--primary)', backgroundColor: 'rgba(91, 127, 255, 0.1)', padding: '4px 8px', borderRadius: '6px' }}>
                                            {getDDayString(event.dDay)}
                                        </div>
                                    </div>
                                ))}
                                {events.length === 0 && (
                                    <div style={{ padding: '10px', textAlign: 'center', fontSize: '13px', color: 'var(--on-surface-variant)' }}>
                                        등록된 일정이 없습니다.
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ScheduleModal;
