import React, { useState, useRef, useEffect } from 'react';
import FullCalendar from '@fullcalendar/react';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import AddModal from '../add/AddModal';
import CalendarEditModal from "../edit/CalendarEditModal";
import Modal from 'react-modal';
import styles from './TodayCalendar.module.css';
import api from '../../api/client';

Modal.setAppElement('#root');

function TodayCalendar() {
    const [isAddModalOpen, setAddModalOpen] = useState(false);
    const [isEditModalOpen, setEditModalOpen] = useState(false);
    const [selectedDate, setSelectedDate] = useState('');
    const [selectedEvent, setSelectedEvent] = useState(null);
    const calendarRef = useRef(null);
    const [events, setEvents] = useState([]);

    // 데이터 로드
    useEffect(() => {
        const fetchData = async () => {
            try {
                const categoryResponse = await api.get("/api/categories");
                const categories = categoryResponse.data;
                const formattedCategories = categories.map((category) => ({
                    id: category.categoryId,
                    name: category.categoryName,
                    color: category.color || '#808080',
                }));

                const eventsResponse = await api.get("/api/calendars");
                const eventData = eventsResponse.data;
                const fullCalendarEvents = eventData.map((event) => {
                    const categoryColor = formattedCategories.find((label) => label.id === event.categoryId)?.color || '#808080';
                    return {
                        id: event.calendarId,
                        title: event.title,
                        start: event.startTime,
                        end: event.end_time, // DTO의 키에 맞게 변경
                        description: event.description,
                        location: event.location,
                        color: categoryColor,
                        repeatType: event.repeatType,
                        categoryId: event.categoryId,
                    };
                });

                const today = new Date().toLocaleDateString('en-CA');
                setEvents(fullCalendarEvents.filter((event) => {
                    return new Date(event.start).toLocaleDateString('en-CA') === today;
                }));
            } catch (error) {
                console.error('Error fetching data:', error);
            }
        };

        fetchData();
    }, []);

    // 날짜 클릭 이벤트
    const handleDateClick = (arg) => {
        setSelectedDate(arg.dateStr);
        setAddModalOpen(true);
    };

    // 일정 클릭 이벤트
    const handleEventClick = (clickInfo) => {
        console.log("Clicked Event Info:", clickInfo.event); // 클릭된 이벤트의 전체 데이터
        const clickedEvent = events.find((event) => String(event.id) === String(clickInfo.event.id));
        if (clickedEvent) {
            console.log("Matched Event:", clickedEvent);
            const transformedClickedEvent = {
                id: clickedEvent.id,
                title: clickedEvent.title,
                start: null,
                end: null,
                description: clickedEvent.description,
                repeatType: clickedEvent.repeatType, // 반복 설정 전달
                categoryId: clickedEvent.categoryId,
            };

            // 상태 업데이트
            setSelectedEvent(transformedClickedEvent);
            setEditModalOpen(true); // 모달 열기
        } else {
            console.error("Event not found in the events array");
            alert("해당 이벤트를 찾을 수 없습니다."); // 사용자 알림 추가
        }
    };

    // AddModal 닫기
    const closeAddModal = () => {
        setAddModalOpen(false);
    };

    // EditModal 닫기
    const closeEditModal = () => {
        setEditModalOpen(false);
        setSelectedEvent(null);
    };

    // 추가 모달 저장 핸들러
    const handleSave = (newEvent) => {
        setEvents((prevEvents) => [...prevEvents, newEvent]); // 새 이벤트 추가
        setAddModalOpen(false); // 모달 닫기
    };

    const handleEventUpdate = (updatedEvent) => {
        console.log("Updating event:", updatedEvent);
        setEvents((prevEvents) => [...prevEvents, updatedEvent]); // 새 이벤트 추가
        setAddModalOpen(false); // 모달 닫기
    };

    // EditModal 삭제 핸들러
    const handleDelete = (eventId) => {
        setEvents((prevEvents) => prevEvents.filter((event) => event.id !== eventId));
        setEditModalOpen(false);
    };

    return (
        <div className={styles.calendarCard}>
            <div className={styles.header}>
                <div>
                    <span className={styles.eyebrow}>Today Calendar</span>
                    <h2>오늘 일정</h2>
                </div>
                <span className={styles.count}>{events.length}</span>
            </div>

            <div className={styles.calendarContainer}>
                <FullCalendar
                    ref={calendarRef}
                    plugins={[timeGridPlugin, interactionPlugin]}
                    initialView="timeGridDay" // 일간 보기 설정
                    events={events}
                    dateClick={handleDateClick}
                    eventClick={handleEventClick}
                    headerToolbar={{
                        left: '',
                        center: '',
                        right: '', // 보기 전환 버튼 제거
                    }}
                    locale="ko"
                    editable={true}
                    selectable={true}
                    height="100%"
                    initialDate={new Date()} // 오늘 날짜만 표시
                    slotMinTime="08:00:00"
                    slotMaxTime="20:00:00"
                />
            </div>

            {/* 추가 모달 */}
            <AddModal
                isOpen={isAddModalOpen}
                onRequestClose={closeAddModal}
                onSave={handleSave} // onSave 핸들러 전달
                selectedDate={selectedDate} // 선택된 날짜 전달
                defaultTab="일정" // 기본 탭 전달
            />

            {/* 수정 모달 */}
            <CalendarEditModal
                isOpen={isEditModalOpen}
                onRequestClose={closeEditModal}
                onSave={handleEventUpdate}
                onDelete={handleDelete}
                selectedEvent={selectedEvent} // 선택된 이벤트 전달
            />

        </div>
    );
}

export default TodayCalendar;
