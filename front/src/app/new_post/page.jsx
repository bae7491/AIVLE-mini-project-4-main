"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

import "../css/edit_post.css";

import Toast from "@/app/components/Toast";
import ConfirmModal from "@/app/components/ConfirmModal";
import { generateCoverImage } from "@/app/api/openaiClient";

function Page() {
    const router = useRouter();

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [content, setContent] = useState("");

    // categoryId + name 저장
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [categories, setCategories] = useState([]);

    const [imageUrl, setPreviewImageUrl] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    // --------------------- 토스트 함수 ---------------------
    const showToast = (msg, type = "info") => {
        window.dispatchEvent(
            new CustomEvent("show-toast", {
                detail: { msg, type },
            })
        );
    };

    // ------------------- 토큰 없는 접근 차단 -------------------
    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (!token) {
            showToast("로그인이 필요한 페이지입니다.", "danger");
            router.replace("/");
        }
    }, []);

    // --------------------- 카테고리 불러오기 ---------------------
    useEffect(() => {
        const loadCategories = async () => {
            try {
                const res = await fetch("http://localhost:8080/api/categories");
                const json = await res.json();

                if (Array.isArray(json.data)) {
                    setCategories(json.data);
                }
            } catch (err) {
                showToast("카테고리 불러오기 실패", "danger");
            }
        };

        loadCategories();
    }, []);

    // -------------------- 이미지 생성 --------------------
    const handleGenerateImage = async () => {
        if (!title || !description || !content || !selectedCategory) {
            showToast("내용을 모두 입력해 주세요.", "warning");
            return;
        }

        const postData = {
            title,
            description,
            content,
            categoryId: selectedCategory.categoryId,
            categoryName: selectedCategory.name,
        };

        setIsLoading(true);
        setPreviewImageUrl("");

        const url = await generateCoverImage(postData);

        setIsLoading(false);

        if (!url) {
            showToast("이미지 생성에 실패했습니다.", "danger");
            return;
        }

        setPreviewImageUrl(url);
        showToast("이미지가 생성되었습니다!", "success");
    };

    // -------------------- 최종 등록 전 확인 --------------------
    const finalCheck = () => {
        if (!title || !description || !content || !selectedCategory || !imageUrl) {
            showToast("모든 값을 입력해 주세요!", "warning");
            return;
        }

        setShowConfirm(true);
    };

    // -------------------- 등록 처리 --------------------
    const handleConfirm = async () => {
        setShowConfirm(false);

        const jwt = localStorage.getItem("accessToken");
        if (!jwt) {
            showToast("로그인이 필요합니다.", "danger");
            return;
        }

        const finalPostData = {
            title,
            description,
            content,
            categoryId: selectedCategory.categoryId,
            categoryName: selectedCategory.name,
            imageUrl,
        };

        try {
            const response = await fetch("http://localhost:8080/api/books/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${jwt}`,
                },
                body: JSON.stringify(finalPostData),
            });

            if (response.ok) {
                showToast("게시물이 성공적으로 등록되었습니다!", "success");

                setTimeout(() => {
                    router.push("/");
                }, 800);
            } else {
                showToast(`등록 실패: ${response.statusText}`, "danger");
            }
        } catch (error) {
            showToast("서버 통신 오류", "danger");
        }
    };

    const handleCancel = () => setShowConfirm(false);

    return (
        <div className="edit-page-container">
            <div className="edit-wrapper">
                <h1 className="edit-title">게시물 등록</h1>

                {/* 제목 입력 */}
                <label className="label">제목</label>
                <input
                    type="text"
                    className="edit-input"
                    value={title}
                    placeholder="제목을 입력해 주세요."
                    onChange={(e) => setTitle(e.target.value)}
                />

                <div className="edit-main-row">
                    {/* 이미지 영역 */}
                    <div className="edit-image-box">
                        <div className="image-fixed-box">
                            {isLoading ? (
                                <div className="loading-spinner"></div>
                            ) : imageUrl ? (
                                <img src={imageUrl} className="edit-preview-img" />
                            ) : (
                                <div className="edit-img-placeholder">이미지 없음</div>
                            )}
                        </div>

                        <div className="image-btn-center">
                            <button
                                className="btn-generate"
                                onClick={handleGenerateImage}
                                disabled={isLoading}
                            >
                                {isLoading ? "생성 중..." : "이미지 생성"}
                            </button>
                        </div>
                    </div>

                    {/* 텍스트 입력 영역 */}
                    <div className="edit-right">
                        <label className="label">작품 설명</label>
                        <textarea
                            className="edit-textarea"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />

                        <label className="label">작품 내용</label>
                        <textarea
                            className="edit-textarea"
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                        />

                        <label className="label">카테고리</label>
                        <select
                            className="edit-select"
                            onChange={(e) => {
                                const index = e.target.selectedIndex - 1;
                                if (index >= 0) {
                                    setSelectedCategory(categories[index]);
                                } else {
                                    setSelectedCategory(null);
                                }
                            }}
                        >
                            <option value="">카테고리 선택</option>
                            {categories.map((cat) => (
                                <option key={cat.categoryId} value={cat.categoryId}>
                                    {cat.name}
                                </option>
                            ))}
                        </select>

                        <div className="edit-btn-row">
                            <button
                                className="btn-update"
                                onClick={finalCheck}
                                disabled={isLoading}
                            >
                                게시물 등록
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Confirm Modal */}
            <ConfirmModal
                show={showConfirm}
                title="📘 게시물 등록"
                type="primary"
                message="해당 내용으로 게시물을 등록하시겠습니까?"
                onConfirm={handleConfirm}
                onCancel={handleCancel}
            />
        </div>
    );
}

export default Page;
