"use client";

import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from "react";
import { useParams } from "next/navigation";
import "../../css/edit_post.css";

import ConfirmModal from "@/app/components/ConfirmModal";

import { generateCoverImage } from "@/app/api/openaiClient";

function Page() {
    const router = useRouter();
    const { slug } = useParams();

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [content, setContent] = useState("");
    const [categoryId, setCategory] = useState("");
    const [imageUrl, setPreviewImageUrl] = useState("");

    const [categories, setCategories] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    const showToast = (msg, type = "error") => {
        window.dispatchEvent(
            new CustomEvent("show-toast", {
                detail: { msg, type },
            })
        );
    };

    // [추가] --- 토큰 없는 경우 접근 차단 ---
    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (!token) {
            window.dispatchEvent(
                new CustomEvent("show-toast", {
                    detail: {
                    msg: "로그인이 필요한 페이지입니다.",
                    type: "danger",
                    },
                })
            );

            router.replace("/");
            return;
        }
    }, []);
    // ---------------------------------------------------------

    // ------------------ 카테고리 불러오기 ------------------
    useEffect(() => {
        const loadCategories = async () => {
            try {
                const res = await fetch("http://localhost:8080/api/categories");
                const json = await res.json();
                if (Array.isArray(json.data)) setCategories(json.data);
            } catch (err) {
                showToast("카테고리 불러오기 실패", "error");
            }
        };

        loadCategories();
    }, []);

    // ------------------ 기존 게시물 불러오기 ------------------
    useEffect(() => {
        if (!slug) return;

        const loadPostData = async () => {
            try {
                const res = await fetch(`http://localhost:8080/api/books/detail/${slug}`);
                const json = await res.json();

                if (json.status === "success") {
                    const d = json.data;
                    setTitle(d.title);
                    setDescription(d.description);
                    setContent(d.content);
                    setCategory(d.categoryId);
                    setPreviewImageUrl(d.imageUrl);
                } else {
                    showToast("게시글 불러오기 실패", "error");
                }
            } catch (err) {
                showToast("서버 오류로 게시글을 불러올 수 없습니다.", "error");
            }
        };

        loadPostData();
    }, [slug]);

    // ------------------ 이미지 생성 ------------------
    const handleSubmit = async () => {
        if (!title || !description || !content || !categoryId) {
            showToast("제목, 설명, 내용, 카테고리를 모두 입력해 주세요.", "error");
            return;
        }

        const postData = {
            title,
            description,
            content,
            categoryId,
            categoryName: categories.find(c => c.categoryId == categoryId)?.name,
        };

        setIsLoading(true);
        setPreviewImageUrl("");

        const url = await generateCoverImage(postData);

        setIsLoading(false);

        if (!url) {
            showToast("이미지 생성에 실패했습니다.", "error");
            return;
        }

        setPreviewImageUrl(url);
        showToast("이미지가 성공적으로 생성되었습니다!", "success");
    };

    // ------------------ 최종 게시 확인 모달 ------------------
    const finalCheck = () => {
        if (!title || !description || !content || !categoryId || !imageUrl) {
            showToast("모든 값을 입력해 주세요!", "error");
            return;
        }

        setShowConfirm(true);
    };

    // ------------------ 최종 게시 실제 처리 ------------------
    const handleConfirm = async () => {
        setShowConfirm(false);

        const jwt = localStorage.getItem("accessToken");
        if (!jwt) {
            showToast("로그인이 필요합니다.", "error");
            return;
        }

        const finalPostData = {
            title,
            description,
            content,
            categoryId: Number(categoryId),
            imageUrl,
        };

        try {
            const response = await fetch(`http://localhost:8080/api/books/update/${slug}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${jwt}`,
                },
                body: JSON.stringify(finalPostData),
            });

            if (response.ok) {
                showToast("게시물이 성공적으로 수정되었습니다!", "success");
                setTimeout(() => {
                    window.location.href = "/";
                }, 800);
            } else {
                showToast(`수정 실패: ${response.statusText}`, "error");
            }
        } catch {
            showToast("서버 연결 실패", "error");
        }
    };

    const handleCancel = () => setShowConfirm(false);

    // ------------------ 이미지 전달 Listener ------------------
    useEffect(() => {
        const handleMessage = (event) => {
            if (event.data?.imageUrl) {
                setPreviewImageUrl(event.data.imageUrl);
            }
        };
        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, []);

    return (
        <div className="edit-page-container">
            <div className="edit-wrapper">
                <h1 className="edit-title">게시물 수정</h1>

                {/* 제목 입력 */}
                <label className="label">제목</label>
                <input
                    type="text"
                    className="edit-input"
                    placeholder="제목을 입력해 주세요."
                    value={title}
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
                            <button className="btn-generate" onClick={handleSubmit} disabled={isLoading}>
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
                            value={categoryId}
                            onChange={(e) => setCategory(e.target.value)}
                        >
                            <option value="">카테고리 선택</option>
                            {categories.map((cat) => (
                                <option key={cat.categoryId} value={cat.categoryId}>
                                    {cat.name}
                                </option>
                            ))}
                        </select>

                        <div className="edit-btn-row">
                            <button className="btn-update" onClick={finalCheck} disabled={isLoading}>
                                게시물 수정
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Confirm Modal */}
            <ConfirmModal
                show={showConfirm}
                title="⚠️ 게시물 수정"
                type="primary"
                message="정말 수정하시겠습니까?"
                onConfirm={handleConfirm}
                onCancel={handleCancel}
            />
        </div>
    );
}

export default Page;
