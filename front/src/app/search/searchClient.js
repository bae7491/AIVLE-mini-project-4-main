"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import Pagination from "@mui/material/Pagination";
import "../css/books.css";

export default function SearchClient() {
  const searchParams = useSearchParams();
  const router = useRouter();

  // URL에서 검색어 가져오기
  const keyword = searchParams.get("keyword") || "";

  const [books, setBooks] = useState([]);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const [page, setPage] = useState(1);
  const size = 28;

  /* =======================================================
     ⭐ 검색 API (GET + QueryString)
     ======================================================= */
  async function searchBooks(title, currentPage) {
    try {
      setLoading(true);

      const url = `http://localhost:8080/api/books/search?title=${encodeURIComponent(
        title
      )}&page=${currentPage}&size=${size}`;

      const res = await fetch(url);

      if (!res.ok) {
        console.error("검색 실패:", await res.text());
        throw new Error("검색 API 실패");
      }

      const json = await res.json();

      setBooks(json.data?.books ?? []);
      setTotalItems(json.data?.totalItems ?? 0);
      setTotalPages(json.data?.totalPages ?? 0);
    } catch (err) {
      console.error("도서 검색 실패:", err);
    } finally {
      setLoading(false);
    }
  }

  /* =======================================================
     ⭐ keyword 없으면 메인으로 이동
     ======================================================= */
  useEffect(() => {
    if (!keyword.trim()) {
      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: {
            msg: "검색어를 입력해주세요!",
            type: "danger",
          },
        })
      );
      router.push("/");
      return;
    }

    setPage(1);
    searchBooks(keyword, 1);
  }, [keyword]);

  /* =======================================================
     ⭐ 페이지 변경 시 재검색
     ======================================================= */
  useEffect(() => {
    if (!keyword.trim()) return;
    searchBooks(keyword, page);
  }, [page]);

  return (
    <main className="container py-5 home-container">
      {/* 헤더 */}
      <div className="d-flex flex-column flex-sm-row justify-content-between align-items-center gap-3 mb-4">
        <h2 className="section-title m-0">🔍 검색 결과</h2>

        <span className="badge rounded-pill text-bg-light border books-count-badge">
          {loading ? "불러오는 중..." : `총 ${totalItems}권`}
        </span>
      </div>

      {/* 로딩 */}
      {loading && (
        <div className="d-flex align-items-center gap-2 text-secondary">
          <div className="spinner-border spinner-border-sm" role="status" />
          <span>불러오는 중...</span>
        </div>
      )}

      {/* 검색 결과 없음 */}
      {!loading && books.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">📭</div>
          <div className="empty-title">검색 결과가 없습니다.</div>
          <div className="empty-desc">다른 검색어로 다시 시도해 보세요.</div>
        </div>
      )}

      {/* 검색 결과 목록 */}
      {!loading && books.length > 0 && (
        <div className="row g-4">
          {books.map((book) => (
            <div key={book.bookId} className="col-12 col-sm-6 col-md-4 col-lg-3">
              <div
                className="book-card shadow-sm"
                role="button"
                onClick={() =>
                  (window.location.href = `/post_view/${book.bookId}`)
                }
              >
                <div className="book-thumb">
                  <img
                    src={book.imageUrl}
                    alt={book.title}
                    className="book-image"
                    loading="lazy"
                    onError={(e) => {
                      e.currentTarget.style.display = "none";
                      e.currentTarget.parentElement.classList.add(
                        "thumb-fallback"
                      );
                    }}
                  />
                </div>

                <div className="card-body py-2">
                  <h5 className="card-title book-title mb-0">
                    {book.title}
                  </h5>
                </div>

                <span
                  className="badge bg-secondary ms-3"
                  style={{
                    fontSize: "0.75rem",
                    borderRadius: "10px",
                    padding: "4px 8px",
                    opacity: 0.85,
                  }}
                >
                  {book.category || "미분류"}
                </span>

                <div className="card-footer bg-transparent border-0 pt-0">
                  <span className="read-more">자세히 보기 →</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      {!loading && totalItems > 0 && (
        <div className="pagination-container d-flex justify-content-center">
          <Pagination
            count={totalPages}
            page={page}
            onChange={(e, value) => setPage(value)}
            color="primary"
            shape="rounded"
            size="large"
          />
        </div>
      )}
    </main>
  );
}
