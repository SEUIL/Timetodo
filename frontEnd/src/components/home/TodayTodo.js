import React, { useEffect, useState } from 'react';
import { FaEdit } from 'react-icons/fa'; // 수정 아이콘 추가
import styles from './TodayTodo.module.css';
import AddModal from '../add/AddModal';
import TodoEditModal from '../edit/TodoEditModal'; // 수정 모달 추가
import api from '../../api/client';

function Todo() {
    const [todos, setTodos] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false); // 수정 모달 상태
    const [labelOptions, setLabelOptions] = useState([]);
    const [selectedTodo, setSelectedTodo] = useState(null); // 선택된 할 일


    useEffect(() => {
        const fetchData = async () => {
            try {
                const categoryResponse = await api.get("/api/categories/all");
                const categories = categoryResponse.data;
                const formattedCategories = categories.map((category) => ({
                    id: category.categoryId,
                    name: category.categoryName,
                    color: category.color || '#808080',
                }));
                setLabelOptions(formattedCategories);

                const tasksResponse = await api.get("/api/task/find");
                const tasks = tasksResponse.data;
                const formattedTasks = tasks.map((task) => {
                    const category = formattedCategories.find((category) => category.id === task.categoryId);
                    return {
                        taskId: task.taskId,
                        title: task.title,
                        dueDate: task.dueDate,
                        label: category ? category.name : '라벨 없음',
                        labelColor: category ? category.color : '#808080',
                        priority: task.priority || '중간',
                        status: task.status || '보류 중',
                        repeatType: task.repeatType,
                        categoryId: task.categoryId,
                    };
                });
                setTodos(formattedTasks);
            } catch (error) {
                console.error('Error fetching data:', error);
            }
        };

        fetchData();
    }, []);

    // 오늘 날짜 구하기 (yyyy-mm-dd 형식)
    const today = new Date().toLocaleDateString('en-CA'); // 'yyyy-mm-dd' 형식으로 오늘 날짜 반환

    // 오늘 날짜에 해당하는 할 일만 필터링
    const todosToday = todos.filter((todo) => {
        const todoDueDate = new Date(todo.dueDate).toLocaleDateString('en-CA');
        return todoDueDate === today; // 오늘 날짜와 비교
    });

    const handleStatusChange = async (taskId) => {
        const targetTask = todos.find((todo) => todo.taskId === taskId);
        if (!targetTask) {
            alert("해당 할 일을 찾을 수 없습니다.");
            return;
        }

        const newStatus =
            targetTask.status === "보류 중"
                ? "진행 중"
                : targetTask.status === "진행 중"
                    ? "완료"
                    : "보류 중";

        setTodos((prevTodos) =>
            prevTodos.map((todo) =>
                todo.taskId === taskId
                    ? { ...todo, status: newStatus }
                    : todo
            )
        );

        const updateData = {
            taskId: taskId, // 할 일 ID
            status: newStatus, // 새로운 상태
        };

        try {
            // 서버로 상태 업데이트 요청
            const response = await api.put("/api/task/updateStatus", updateData);

            console.log("Task status updated successfully:", response.data);
        } catch (error) {
            console.error("Failed to update task status:", error.response || error);
            alert("상태 업데이트 중 오류가 발생했습니다.");
        }
    };

    const handleEditTodo = (updatedTodo) => {
        setTodos((prevTodos) =>
            prevTodos.map((todo) => (todo.taskId === updatedTodo.taskId ? updatedTodo : todo))
        );
        setShowEditModal(false);
    };

    const handleDeleteTodo = (taskId) => {
        setTodos((prevTodos) => prevTodos.filter((todo) => todo.taskId !== taskId));
        setShowEditModal(false);
    };

    const handleTodoClick = (todo) => {
        console.log("Selected Todo:", todo);
        setSelectedTodo(todo);
        setShowEditModal(true);
    };

    return (
        <div className={styles.container}>
            <h2 className={styles.title}>오늘 할 일 목록</h2>
            <ul className={styles.todoList}>
                {todosToday.map((todo) => (
                    <li key={todo.taskId} className={styles.todoItem}>
                        <div className={styles.todoDetails}>
                            <span>{todo.title || '제목 없음'}</span>
                            <div className={styles.metadata}>
                                {todo.dueDate && <span>📅 {new Date(todo.dueDate).toLocaleString()}</span>}
                                {todo.label && (
                                    <span
                                        style={{
                                            backgroundColor: todo.labelColor,
                                            color: '#ffffff',
                                            padding: '2px 6px',
                                            borderRadius: '12px',
                                            fontSize: '0.8em',
                                            marginLeft: '8px',
                                            display: 'inline-block',
                                        }}
                                    >
                                        {todo.label}
                                    </span>
                                )}
                                {todo.priority && <span>⚡ {todo.priority}</span>}
                                <span
                                    className={`${styles.status} ${
                                        todo.status === "보류 중"
                                            ? styles.pending
                                            : todo.status === "진행 중"
                                                ? styles.inProgress
                                                : styles.completed
                                    }`}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleStatusChange(todo.taskId);
                                    }}
                                >
                                    {todo.status}
                                </span>
                                {/* 수정 아이콘 */}
                                <FaEdit
                                    className={styles.editIcon}
                                    onClick={(e) => {
                                        e.stopPropagation(); // 부모 이벤트 전파 방지
                                        handleTodoClick(todo);
                                    }}
                                />
                            </div>
                        </div>
                    </li>
                ))}
            </ul>

            <button onClick={() => setShowModal(true)} className={styles.addButton}>
                할 일 추가하기
            </button>

            {/* AddModal */}
            <AddModal
                isOpen={showModal}
                onRequestClose={() => setShowModal(false)}
                onSave={(newTask) => setTodos((prevTodos) => [...prevTodos, newTask])}
                defaultTab="할 일"
                labelOptions={labelOptions}
            />

            {/* TodoEditModal */}
            <TodoEditModal
                isOpen={showEditModal}
                onRequestClose={() => setShowEditModal(false)}
                onSave={handleEditTodo}
                onDelete={handleDeleteTodo}
                task={selectedTodo}
                labelOptions={labelOptions}
            />
        </div>
    );
}

export default Todo;
