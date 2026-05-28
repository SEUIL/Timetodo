import React from 'react';
import TodayTodo from './TodayTodo';
import TodayCalendar from "./TodayCalendar";
import styles from "./Home.module.css";

function Home() {

    // region  const 제목 날짜 관련
        // 현재 날짜와 요일 가져오기
        const currentDate = new Date();

        // 날짜 포맷 설정 (예: "2024년 12월 5일 목요일")
        const dateOptions = {year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'};
        const formattedDate = currentDate.toLocaleDateString('ko-KR', dateOptions);

    // endregion

        return (
            <section className={styles.page}>
                <header className={styles.header}>
                    <span className={styles.eyebrow}>Today</span>
                    <h1>{formattedDate}</h1>
                </header>

                <div className={styles.dashBoard}>
                    <div className={styles.TodoContainer}>
                        <TodayTodo/>
                    </div>

                    <div className={styles.CalendarContainer}>
                        <TodayCalendar/>
                    </div>
                </div>
            </section>
        );

    }
    export default Home;
