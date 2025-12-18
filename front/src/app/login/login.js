'use client';

import axios from 'axios';

/**
 * 로그인 요청 API
 */
export async function loginRequest(id, pw) {
  if (!id.trim() || !pw.trim()) {
    throw new Error('아이디와 비밀번호를 모두 입력해주세요.');
  }

  const loginRes = await axios.post(
    'http://localhost:8080/api/auth/login',
    { id, pw }
  );

  const result = loginRes.data;

  // 헤더에서 accessToken 추출
  let authHeader = loginRes.headers['authorization'];
  let accessToken = null;

  if (authHeader && authHeader.startsWith('Bearer ')) {
    accessToken = authHeader.replace('Bearer ', '');
  }

  return {
    result,
    accessToken,
  };
}
