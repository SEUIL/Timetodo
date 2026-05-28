import React, { useState, useEffect } from 'react';
import { FaHome, FaCalendarAlt, FaClipboardList, FaChartBar, FaChevronDown, FaPlus} from 'react-icons/fa';
import { NavLink, useNavigate } from 'react-router-dom';
import Cookies from 'js-cookie'; // 쿠키 관리 라이브러리

import Profile from './Profile';
import AddModal from '../add/AddModal';
import styles from './Sidebar.module.css';

const Sidebar = ({ onAddEvent, onLogout }) => {
    const [isProfileVisible, setProfileVisible] = useState(false);
    const [isModalOpen, setModalOpen] = useState(false);
    const navigate = useNavigate();

    const [username, setUsername] = useState('');

    useEffect(() => {
        // 쿠키에서 화면 표시용 username만 읽기
        const storedUsername = Cookies.get('username'); // username 읽기

        console.log("쿠키에서 읽은 username:", storedUsername);

        if (storedUsername) setUsername(storedUsername); // 상태에 저장
    }, []);

    const toggleProfile = () => {
        setProfileVisible(!isProfileVisible);
    };

    const handleLogout = async () => {
        if (onLogout) {
            await onLogout();
        }
        navigate("/login");
    };

    const openModal = () => {
        setModalOpen(true);
    };

    const closeModal = () => {
        setModalOpen(false);
    };

    const handleSaveEvent = (eventData) => {
        onAddEvent(eventData);
        closeModal();
    };

    return (
        <div className={styles.sidebar}>

            {/* 사용자 프로필 섹션 */}
            <div className={styles.profileSection} onClick={toggleProfile}>
                <div className={styles.profileImage}>
                    {(username || 'T').slice(0, 1).toUpperCase()}
                </div>
                <div className={styles.profileInfo}>
                    <span className={styles.profileLabel}>Workspace</span>
                    <span className={styles.username}>
                        {username ? `${username}` : '로그인해주세요'}
                    </span>
                    <FaChevronDown
                        className={styles.dropdownIcon}
                        style={{
                            transform: isProfileVisible ? 'rotate(180deg)' : 'rotate(0deg)',
                            transition: 'transform 0.3s'
                        }}
                    />
                    {/*<FaBell className={styles.notificationIcon}/>*/}
                </div>
            </div>

            {isProfileVisible && <Profile handleLogout={handleLogout}/>}

            <button
                onClick={openModal}
                className={styles.addButton}>
                <FaPlus/>추가하기
            </button>

            <nav className={styles.menu}>
                <ul>
                    <li>
                        <NavLink to="/home" className={({ isActive }) => `${styles.menuLink} ${isActive ? styles.active : ''}`}>
                            <FaHome className={styles.menuIcon}/> 홈
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/calendar" className={({ isActive }) => `${styles.menuLink} ${isActive ? styles.active : ''}`}>
                            <FaCalendarAlt className={styles.menuIcon}/> 캘린더
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/todo" className={({ isActive }) => `${styles.menuLink} ${isActive ? styles.active : ''}`}>
                            <FaClipboardList className={styles.menuIcon}/> 할 일
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/stats" className={({ isActive }) => `${styles.menuLink} ${isActive ? styles.active : ''}`}>
                            <FaChartBar className={styles.menuIcon}/> 통계
                        </NavLink>
                    </li>
                </ul>
            </nav>

            <AddModal isOpen={isModalOpen} onRequestClose={closeModal} onSave={handleSaveEvent}/>
        </div>
    );
};

export default Sidebar;
