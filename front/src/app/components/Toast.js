export default function Toast({ show, type, message }) {
  if (!show) return null;

  const emoji = {
    success: '✅',
    danger: '✖',
    warning: '⚠️',
    info: 'ℹ️'
  }[type] || '';

  return (
    <div
      style={{
        position: 'fixed',
        top: '60px',
        right: '20px',
        zIndex: 2000,
        minWidth: '240px'
      }}
      className={`toast align-items-center text-bg-${type} show`}
      role="alert"
    >
      <div className="d-flex">
        <div className="toast-body">
          {emoji} {message}
        </div>
      </div>
    </div>
  );
}
