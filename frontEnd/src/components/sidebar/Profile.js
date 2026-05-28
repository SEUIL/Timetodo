import React, { useState } from 'react';
import styles from './Profile.module.css';
import Modal from './SettingsModal';

const Profile = ({ handleLogout }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);

    const openModal = () => setIsModalOpen(true);
    const closeModal = () => setIsModalOpen(false);

    return (
        <div className={styles.profileContainer}>
            <button onClick={handleLogout} className={styles.logoutButton}>
                로그아웃
            </button>
            <button onClick={openModal}>
                설정
            </button>
            <Modal isOpen={isModalOpen} onClose={closeModal}>

            </Modal>
        </div>
    );
};

export default Profile;
