'use client';

import { useEffect, useState } from "react";
import { useParams, useRouter } from 'next/navigation';
import "../../css/post_view.css";
import api from "../../api/apiClient";

import ConfirmModal from "@/app/components/ConfirmModal"; 

function BookDetailsView({
    bookTitle,
    authorName,
    createdAt,
    updatedAt,
    coverImgUrl,
    description,
    content,
    isOwner,
    onEdit,
    onDelete
}) {
    return (
        <div className="container mt-4 d-flex justify-content-center">
            <div className="detail-wrapper">

                <h1 className="detail-title-centered">{bookTitle}</h1>

                <div className="meta-block">
                    <div className="meta-line">
                        <span>작성자: {authorName}</span>
                        <span className="push-right">등록일: {createdAt}</span>
                    </div>

                    <div className="meta-line">
                        {updatedAt && updatedAt !== "-" ? (
                            <span className="push-right">수정일: {updatedAt}</span>
                        ) : (
                            <span className="push-right"></span>
                        )}
                    </div>
                </div>

                <hr className="content-divider2" />

                <div className="detail-main-row">

                    {coverImgUrl && (
                        <div className="detail-cover">
                            <img src={coverImgUrl} alt="cover" className="cover-img" />
                        </div>
                    )}

                    <div className="detail-right">
                        <h5 className="fw-bold mb-3">📚 책 설명</h5>
                        <p className="detail-paragraph">{description}</p>

                        {isOwner && (
                            <div className="edit-btn-row">
                                <button className="btn-edit me-2" onClick={onEdit}>수정</button>
                                <button className="btn-delete" onClick={onDelete}>삭제</button>
                            </div>
                        )}
                    </div>
                </div>

                <hr className="content-divider" />

                <h5 className="fw-bold mb-3">📖 상세 내용</h5>
                <p className="detail-paragraph">{content}</p>

            </div>
        </div>
    );
}

export default function PostView(props) {
    const router = useRouter();
    const { slug } = useParams();

    const [bookData, setBookData] = useState({
        owner_id: '',
        created_at: '',
        updated_at: '',
        cover_img_url: '',
        title: '',
        description: '',
        content: ''
    });

    const [isOwner, setIsOwner] = useState(false);

    // ConfirmModal 상태 관리
    const [showConfirm, setShowConfirm] = useState(false);
    const [confirmResolver, setConfirmResolver] = useState(null);

    const showConfirmModal = () =>
        new Promise((resolve) => {
            setShowConfirm(true);
            setConfirmResolver(() => resolve);
        });

    const handleConfirm = () => {
        confirmResolver?.(true);
        setShowConfirm(false);
    };

    const handleCancel = () => {
        confirmResolver?.(false);
        setShowConfirm(false);
    };

    const getCurrentUserId = async () => {
        const token = localStorage.getItem("accessToken");
        if (!token) return "";
        const response = await api.get(`http://localhost:8080/api/auth/user-info`);
        return response.status !== 200 ? "" : String(response.data.id);
    };

    const checkCurrentUserIs = async (id) => {
        const currentUserId = await getCurrentUserId();
        return currentUserId === String(id);
    };

    const formatDate = (isoString) => {
        if (!isoString) return "-";
        const d = new Date(isoString);
        if (isNaN(d.getTime())) return "-";
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")} ` +
               `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
    };

    const getBookDetails = async (idx) => {
        try {
            const response = await fetch(`http://localhost:8080/api/books/detail/${idx}`);
            const body = await response.json();

            if (body.status === "success") {
                setBookData({
                    owner_id: body.data.ownerUser,
                    created_at: body.data.createdAt,
                    updated_at: body.data.updatedAt,
                    cover_img_url: body.data.imageUrl,
                    title: body.data.title,
                    description: body.data.description,
                    content: body.data.content
                });

                const ownership = await checkCurrentUserIs(body.data.ownerUser);
                setIsOwner(ownership);
            } else {
                alert("존재하지 않는 도서입니다.");
                router.back();
            }
        } catch {
            alert("도서 정보를 가져올 수 없습니다.");
            router.back();
        }
    };

    const editBook = () => router.push(`/post_edit/${slug}`);

    const deleteBook = async () => {
        const ownership = await checkCurrentUserIs(bookData.owner_id);
        if (!ownership) return alert("본인이 등록한 도서만 삭제할 수 있습니다.");

        const ok = await showConfirmModal();
        if (!ok) return;

        try {
            await api.delete(`http://localhost:8080/api/books/delete/${slug}`);
            window.dispatchEvent(
                new CustomEvent("show-toast", {
                    detail: {
                    msg: "삭제되었습니다.",
                    type: "success",
                    },
                })
            );
            router.push('/');
        } catch {
            alert("삭제 중 오류가 발생했습니다.");
        }
    };

    useEffect(() => {
        props.params.then(() => getBookDetails(slug));
    }, []);

    return (
        <>
            {/* ⭐ 삭제 확인 모달 */}
            <ConfirmModal
                show={showConfirm}
                title="⚠️ 도서 삭제"
                message="정말 이 도서를 삭제하시겠습니까?"
                onConfirm={handleConfirm}
                onCancel={handleCancel}
            />

            <div className="container d-flex justify-content-center">
                <div className="w-100">
                    <BookDetailsView
                        bookTitle={bookData.title}
                        coverImgUrl={bookData.cover_img_url}
                        createdAt={formatDate(bookData.created_at)}
                        updatedAt={formatDate(bookData.updated_at)}
                        authorName={bookData.owner_id}
                        description={bookData.description}
                        content={bookData.content}
                        isOwner={isOwner}
                        onEdit={editBook}
                        onDelete={deleteBook}
                    />
                </div>
            </div>
        </>
    );
}
