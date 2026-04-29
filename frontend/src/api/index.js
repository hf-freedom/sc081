import axios from 'axios';

const API_BASE_URL = 'http://localhost:8002/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const authorApi = {
  getAll: () => api.get('/authors'),
  getById: (id) => api.get(`/authors/${id}`),
  create: (data) => api.post('/authors', data),
  update: (id, data) => api.put(`/authors/${id}`, data),
  setBlacklist: (id, data) => api.put(`/authors/${id}/blacklist`, data),
  getSubmissions: (id) => api.get(`/authors/${id}/submissions`),
};

export const reviewerApi = {
  getAll: () => api.get('/reviewers'),
  getActive: () => api.get('/reviewers/active'),
  getById: (id) => api.get(`/reviewers/${id}`),
  create: (data) => api.post('/reviewers', data),
  update: (id, data) => api.put(`/reviewers/${id}`, data),
  setActive: (id, data) => api.put(`/reviewers/${id}/active`, data),
  getReviews: (id) => api.get(`/reviewers/${id}/reviews`),
  findEligible: (data) => api.post('/reviewers/eligible', data),
};

export const journalApi = {
  getMain: () => api.get('/journal'),
  getSections: () => api.get('/journal/sections'),
  getActiveSections: () => api.get('/journal/sections/active'),
  getSectionById: (id) => api.get(`/journal/sections/${id}`),
  createSection: (data) => api.post('/journal/sections', data),
  updateSection: (id, data) => api.put(`/journal/sections/${id}`, data),
  setSectionActive: (id, data) => api.put(`/journal/sections/${id}/active`, data),
  getReviewRule: () => api.get('/journal/review-rule'),
  updateReviewRule: (data) => api.put('/journal/review-rule', data),
  getFeeStandard: () => api.get('/journal/fee-standard'),
  updateFeeStandard: (data) => api.put('/journal/fee-standard', data),
};

export const submissionApi = {
  getAll: () => api.get('/submissions'),
  getByStatus: (status) => api.get(`/submissions/status/${status}`),
  getByAuthorId: (authorId) => api.get(`/submissions/author/${authorId}`),
  getById: (id) => api.get(`/submissions/${id}`),
  create: (data) => api.post('/submissions', data),
  startInitialReview: (id) => api.put(`/submissions/${id}/initial-review/start`),
  completeInitialReview: (id, data) => api.put(`/submissions/${id}/initial-review/complete`, data),
  withdraw: (id, data) => api.put(`/submissions/${id}/withdraw`, data),
};

export const reviewApi = {
  getBySubmissionId: (submissionId) => api.get(`/reviews/submission/${submissionId}`),
  getByReviewerId: (reviewerId) => api.get(`/reviews/reviewer/${reviewerId}`),
  getById: (id) => api.get(`/reviews/${id}`),
  complete: (id, data) => api.put(`/reviews/${id}/complete`, data),
  getOverdue: () => api.get('/reviews/overdue'),
  replaceReviewer: (id, data) => api.put(`/reviews/${id}/replace-reviewer`, data),
};

export const revisionApi = {
  getBySubmissionId: (submissionId) => api.get(`/revisions/submission/${submissionId}`),
  getById: (id) => api.get(`/revisions/${id}`),
  submit: (id, data) => api.put(`/revisions/${id}/submit`, data),
  getOverdue: () => api.get('/revisions/overdue'),
  create: (data) => api.post('/revisions', data),
};

export const feeApi = {
  getByStatus: (status) => api.get(`/fees/status/${status}`),
  getBySubmissionId: (submissionId) => api.get(`/fees/submission/${submissionId}`),
  getById: (id) => api.get(`/fees/${id}`),
  pay: (id, data) => api.put(`/fees/${id}/pay`, data),
  applyPenalty: (id) => api.put(`/fees/${id}/apply-penalty`),
  getOverdue: () => api.get('/fees/overdue'),
};

export const publicationApi = {
  getAll: () => api.get('/publications'),
  getByYear: (year) => api.get(`/publications/year/${year}`),
  getById: (id) => api.get(`/publications/${id}`),
  getByDOI: (doi) => api.get(`/publications/doi/${doi}`),
  getBySubmissionId: (submissionId) => api.get(`/publications/submission/${submissionId}`),
  publish: (data) => api.post('/publications', data),
};

export const reportApi = {
  getAll: () => api.get('/reports'),
  getById: (id) => api.get(`/reports/${id}`),
  getByYear: (year) => api.get(`/reports/year/${year}`),
  generate: (data) => api.post('/reports/generate', data),
};

export default api;
