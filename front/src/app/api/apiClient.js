import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true,
});

// 저장된 JWT 읽기
function getAccessToken() {
  return localStorage.getItem("accessToken");
}

function setAccessToken(token) {
  localStorage.setItem("accessToken", token);
}

let isRefreshing = false;
let refreshSubscribers = [];

// 재발급 완료 후 대기 중인 요청들 처리
function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach((callback) => callback(newToken));
  refreshSubscribers = [];
}

// 재발급 동안 요청을 큐에 저장
function addRefreshSubscriber(callback) {
  refreshSubscribers.push(callback);
}

// 1) 요청 인터셉터: AccessToken 자동 첨부
api.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 2) 응답 인터셉터: 토큰 만료 시 자동 재발급
api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    // 401 + 재시도 중복 방지
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (!isRefreshing) {
        isRefreshing = true;

        try {
          const res = await axios.post(
            "http://localhost:8080/api/auth/token/refresh",
            {},
            { withCredentials: true }
          );

          // Authorization 헤더에서 새로운 accessToken 추출
          const authHeader = res.headers["authorization"];
          const newAccessToken = authHeader?.replace("Bearer ", "");

          if (!newAccessToken) {
            throw new Error("새 AccessToken이 응답 헤더에 없습니다.");
          }

          setAccessToken(newAccessToken);

          isRefreshing = false;
          onTokenRefreshed(newAccessToken);
        } catch (refreshError) {
          isRefreshing = false;
          return Promise.reject(refreshError);
        }
      }

      // 재발급 요청이 진행 중일 때 → 큐에 등록 후 기다림
      return new Promise((resolve) => {
        addRefreshSubscriber((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          resolve(api(originalRequest)); // 원래 요청 재실행
        });
      });
    }

    return Promise.reject(error);
  }
);

export default api;
