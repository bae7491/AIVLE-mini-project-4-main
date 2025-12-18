// /app/components/ConfirmModal.jsx
"use client";

export default function ConfirmModal({
  show,
  title,
  message,
  onConfirm,
  onCancel,
  type = "danger", // <= 기본은 danger, 필요하면 변경 가능
}) {

  if (!show) return null;

  // Bootstrap 색상 매핑
  const COLOR_MAP = {
    danger: "bg-danger",
    warning: "bg-warning",
    success: "bg-success",
    primary: "bg-primary",
    info: "bg-info",
    default: "bg-secondary",
  };

  const BUTTON_MAP = {
    danger: "btn-danger",
    warning: "btn-warning",
    success: "btn-success",
    primary: "btn-primary",
    info: "btn-info",
    default: "btn-secondary",
  };

  const headerColor = COLOR_MAP[type] || COLOR_MAP.danger;
  const confirmButtonColor = BUTTON_MAP[type] || BUTTON_MAP.danger;

  return (
    <div
      className="modal fade show"
      style={{
        display: "block",
        backgroundColor: "rgba(0,0,0,0.5)",
        zIndex: 9999,
      }}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">

          {/* Header */}
          <div className={`modal-header text-white ${headerColor}`}>
            <h5 className="modal-title">{title || "확인"}</h5>
          </div>

          {/* Message */}
          <div className="modal-body">{message || "이 작업을 진행하시겠습니까?"}</div>

          {/* Buttons */}
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={onCancel}>
              취소
            </button>
            <button className={`btn ${confirmButtonColor}`} onClick={onConfirm}>
              확인
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}
