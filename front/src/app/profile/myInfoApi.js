import api from '@/app/api/apiClient';

export const fetchUserInfo = () => {
  return api.get('/auth/user-info');
};

export const updateUserInfo = (body, apiKey) => {
  return api.patch('/auth/update', body, {
    headers: { 'API-KEY': apiKey }
  });
};